import argparse
import boto
from datetime import datetime,timedelta,time
from etool import queue,logs
import json
import math
import operator
import hashlib

__processor__ = 'bayesian_model'
log = logs.getLogger(__processor__)
log.init()

"""
Applying bayesian model to predict the stock flucturation
"""
KEYID = ""
SECRETKEY = ""
CONFIG = {}
VOCABULARY_FILE = ""
PORT = ""

def parse_args():
    ap = argparse.ArgumentParser("Apply the bayesian model to predict stock warning")
    ap.add_argument('-c',dest="model_cfg",metavar="MODEL CFG",default="./bayesian_model.cfg",type=str,nargs='?',help='the config file')
    ap.add_argument('-tf',dest="trend_file",metavar="TREND RANGE FILE",default="./trendRange.json", type=str,nargs='?',help="The trend range file")
    ap.add_argument('-z',dest="port",metavar="ZMQ PORT",default="tcp://*:30115",type=str,nargs="?",help="The zmq port")
    ap.add_argument('-k',dest="key_id",metavar="KeyId for AWS",type=str,help="The key id for aws")
    ap.add_argument('-s',dest="secret",metavar="secret key for AWS",type=str ,help="The secret key for aws")
    return ap.parse_args() 

def get_domain(domain_name):
    conn = boto.connect_sdb(KEYID,SECRETKEY)
    conn.create_domain(domain_name)
    return conn.get_domain(domain_name)

def check_if_tradingday(self,predict_date,stock_index):
        "Check if the day weekend"
        week_day = datetime.strptime(predict_date,"%Y-%m-%d").weekday()
        if week_day == 5 or week_day == 6:
            log.info("%s For %s is Weekend, Just Skip!" %(predict_date,stock_index))
            return False
        
        "Check if the day is holiday"
        domain = get_domain("s_holiday")
        sql = "select count(*) from s_holiday where stock_index='{}' and date = {}".format(stock_index,predict_date)
        result = domain.select(sql)
        count = result["Count"]
        if count == 0:
            return True
        else:
            log.info( "%s For %s is Holiday, Just Skip!" %(stock_index,stock_index))
            return False

# calculate the stock index contribution for the coming day
def compute_stock_index_probability(predict_date, cluster_type , stock_index, duration=3 ):
    try:
        "Get the clusters List"
        cluster_probability = CONFIG["clusterProbability"]
        cluster_json = {}
        cluster_contribution_json = {}
        cluster_json = cluster_probability[stock_index]
        "Get the contribution of each cluster"
        cluster_contribution_json = CONFIG["clusterContribution"]
        
        #get the past n days trend type 
        cluster_types_history = []
        stock_derived = []
        domain_name = "bloomberg_enrichedindex"
        domain = get_domain(domain_name)
        sql = "select trendType,embersId from {} where date < '{}' and stock_index = '{}' order by date desc".format(domain_name,predict_date,stock_index)
        results = domain.select(sql,max_items=duration)
        for result in results:
            cluster_types_history.append(result["trendType"])
            stock_derived.append(result["embersId"])
         
        #computing probability   
        stock_probability = 0
        for key in cluster_contribution_json[stock_index].keys():
            if key == str(cluster_type):
                "Search from the Cluster contribution Matrix to get the contribution probability"
                stock_probability = stock_probability + math.log( float( cluster_contribution_json[stock_index][key][int( cluster_types_history[0] ) - 1][2] ) ) + math.log( float( cluster_contribution_json[stock_index][key][int( cluster_types_history[1] ) - 1][1] ) ) + math.log( float( cluster_contribution_json[stock_index][key][int( cluster_types_history[2] ) - 1][0] ) ) + math.log( float( cluster_json[str( cluster_type )] ) )
        
        return stock_probability,stock_derived
    except Exception as e:
        log.info( "Error in computing stock index probability: %s" % e.args)

# calculate the stock news contribution for the coming day
def compute_stock_news_probability(predict_date, cluster_type , stock_index,duraiton=3 ):
    try:
        term_contribution_json = CONFIG["termContribution"]
        #get the past n day's news
        domain_name = "enriched_news"
        domain = get_domain(domain_name)
        "Get past 3 day's news before Predictive Day "
        predict_date = datetime.strptime(predict_date, "%Y-%m-%d" )
        start_day = ( predict_date - timedelta( days = 3 ) ).strftime( "%Y-%m-%d" )
        end_day = ( predict_date - timedelta( days = 1 ) ).strftime( "%Y-%m-%d" )
        sqlquery = "select content,embers_id from {} where post_date>='{}' and post_date<='{}' and stock_index='{}'".format(domain_name,start_day,end_day,stock_index)
        results = domain.select(sqlquery)
        
        "Initiate the words List"
        wordLines = None
        with open(VOCABULARY_FILE,"r") as f_read:
            wordLines = f_read.readlines()
        termList = {}
        for line in wordLines:
            line = line.replace("\n","").replace("\r","")
            termList[line] = 0
            
        news_derived = []
        "Merge all the term in each record"
        for record in results:
            jsonRecord = record["content"]
            news_derived.append(record["embersId"])
            for curWord in jsonRecord:
                if curWord in termList:
                    termList[curWord] = termList[curWord] + jsonRecord[curWord]
        
        term_probability = 0
        if stock_index in term_contribution_json:
            for term_cluster_type in term_contribution_json[stock_index].keys():
                if term_cluster_type == str(cluster_type):    
                    stermlist = term_contribution_json[stock_index][term_cluster_type]
                    #print stermlist                            
                    for word, count in termList.iteritems():                    
                        if word in stermlist:                        
                            #print word
                            term_probability =  count * math.log( float( term_contribution_json[stock_index][term_cluster_type][word] ) )
        
        return term_probability,news_derived
    except IOError:
        log.info( "Can't open the file:stock_raw_data.json.")
    except Exception as e:
        log.info( "Error in computing stock news probability: %s" % e.message)    
    return None

#predict the stock change type
def enrich_single_stock( predict_date , stock_index ):
    try:
        "Check if the predictive Day is trading day, if so continue, otherwise just return None"
        if_trading_day = check_if_tradingday(predict_date,stock_index) 
        if if_trading_day is False:
            return None
        
        predictiveResults = {}
        finalRatio = {}
        clusterProbability = {}
        predictiveProbability = 0
        stockDerived = []
        newsDerived = []
        
        "Iteratively compute the probabilty of each cluster for the stock "
        cluster_pro_list = CONFIG["clusterProbability"][stock_index]
        for cluster_type in cluster_pro_list:
            "compute the contribution of 3 past day's trend "
            stockIndexProbability,stockDerived = compute_stock_index_probability(predict_date, cluster_type , stock_index )
            "compute the contribution of 3 past day's news"
            newsProbability,newsDerived = compute_stock_news_probability(predict_date, cluster_type , stock_index )
            "combine two contribution together"
            predictiveProbability = math.exp( stockIndexProbability + newsProbability ) * float( 1e90 )
            predictiveResults[cluster_type] = predictiveProbability
        
        sumProbability = sum( predictiveResults.itervalues() ) 
        
        "Get the maximum probability between the predictive values"
        for item_key, item_value in predictiveResults.iteritems():
            finalRatio[item_key] = item_value / sumProbability
        sorted_ratio = sorted( finalRatio.iteritems(), key = operator.itemgetter( 1 ), reverse = True )
        clusterProbability[stock_index] = {}
        clusterProbability[stock_index][predict_date] = sorted_ratio[0]
        
        "Construct the Surrogate data"
        surrogateData = {}
        date = time.strftime('%Y-%m-%d',time.localtime(time.time()))
        "Merge News Derived and Stock Derived"
        derivedFrom = []
        for item in stockDerived:
            derivedFrom.append(item)
        for item in newsDerived:
            derivedFrom.append(item)
        "construct surrogate data"    
        model = 'Bayesian - Time serial Model'
        location = CONFIG["location"][stock_index]
        population = stock_index
        confidence = sorted_ratio[0][1]
        confidenceIsProbability = True
        shiftType = "Trend"
        valueSpectrum = "changePercent"
        strength = sorted_ratio[0][0]
        shiftDate = predict_date
        
        surrogateData["date"] = date
        surrogateData["derivedFrom"] = derivedFrom
        surrogateData["model"] = model
        surrogateData["location"] = location
        surrogateData["population"] = population
        surrogateData["confidence"] = confidence
        surrogateData["confidenceIsProbability"] = confidenceIsProbability
        surrogateData["shiftType"] = shiftType
        surrogateData["valueSpectrum"] = valueSpectrum
        surrogateData["strength"] = strength
        surrogateData["shiftDate"] = shiftDate
        
        "Generate Embers Id"
        jsonStr = json.dumps(surrogateData)
        embersId = hashlib.sha1(json.dumps(jsonStr)).hexdigest()
        surrogateData["embersId"] = embersId
        
        "Insert the surrogatedata to simple DB"
        domain_name = "finance_surrogatedata"
        domain = get_domain(domain_name)
        domain.put_attributes(embersId,surrogateData)
        
        #push surrodate data into ZMQ
        with queue.open(PORT, 'w', capture=True) as outq:
            outq.write(surrogateData)
        
        return surrogateData
    except Exception as e:
        log.info( "Error: %s" % e.args)

