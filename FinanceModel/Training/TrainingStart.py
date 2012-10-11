#-*- coding:utf8-*-
from Util import common
import sys
import json
import time
from datetime import datetime

from Training import ClusteringTraingSet as ct
from Training import CreatingTrendsContribution as ctc
from Training import CreatingVocabulary as cv
from Training import CreatingTermContribution as ctermc
from DataPreprocess import ImportNewsProcess as inp,ImportHistorialStock as ihs
"""
For the Test Phase, we need to do the following steps:
1. Cluster the time serial of stock index value
2. Create the vocabulary List 
3. Compute the Trends Contribution
4. Computing Term Contribution

The prerequisite of these 4 steps is import the Archieve News and Historical Stock index values into database Firstly
And One thing need to do is clear all the Enriched data, surrogatedata and Warning data
"""

#Clear the Testing Phase data in database to retrainng the data
def data_clear():
    con = common.getDBConnection()
    cur = con.cursor()
    
    "clear the stock index raw data"
    clearSql = "delete from t_daily_stockindex"
    cur.execute(clearSql)
    con.commit()
    
    "clear the raw news data"
    clearSql = "delete from t_daily_news"
    cur.execute(clearSql)
    con.commit()
    
    "clear the stock index enriched data"
    clearSql = "delete from t_daily_enrichedIndex"
    cur.execute(clearSql)
    con.commit()
    
    "clear the mission table data"
    clearSql = "delete from t_news_process_mission"
    cur.execute(clearSql)
    con.commit()
    
    "clear the news Enriched data"
    clearSql = "delete from t_daily_enrichednews"
    cur.execute(clearSql)
    con.commit()
    
    "clear the surrogate data"
    clearSql = "delete from t_surrogatedata"
    cur.execute(clearSql)
    con.commit()
    
    "clear the warning data"
    clearSql = "delete from t_warningmessage"
    cur.execute(clearSql)
    con.commit()
    time.sleep(3)
    if con:
        con.close()

def divide_archived_news(endDate):
    archivedNewsPath = common.get_configuration("model", "GROUP_STOCK_NEWS")
    archivedNews = json.load(open(archivedNewsPath),encoding='ISO-8859-1')
    trainingPhaseNews = {}
    testPhaseNews = {}
    
    timeLine = time.strptime(endDate, "%Y-%m-%d")
    for stock in archivedNews:
        if stock not in trainingPhaseNews:
            trainingPhaseNews[stock] = {}
        if stock not in testPhaseNews:
            testPhaseNews[stock] = {}
        for articleId in archivedNews[stock]:
            newsDate = time.strptime(articleId[0:8],"%Y%m%d")
            if newsDate < timeLine:
                trainingPhaseNews[stock][articleId] = archivedNews[stock][articleId]
            else:
                testPhaseNews[stock][articleId] = archivedNews[stock][articleId]
    
    "Write Training data and Test Data to File"
    trainingFilePath = common.get_configuration("model", "TRAINING_NEWS_FILE")
    with open(trainingFilePath,"w") as output:
        output.write(json.dumps(trainingPhaseNews))  
    
    testingFilePath = common.get_configuration("model", "TESTING_NEWS_FILE")
    with open(testingFilePath,"w") as output:
        output.write(json.dumps(testPhaseNews))           
    
def execute(startDate,endDate):
    "Clear the database data"
    print "Clear Start Time: ",datetime.strftime(datetime.now(),"%Y-%m-%d %H:%M:%S")
    data_clear()
    print "Clear End Time: ",datetime.strftime(datetime.now(),"%Y-%m-%d %H:%M:%S")
    
    
    "import archieved news data"
    print "import archieved Start Time: ",datetime.strftime(datetime.now(),"%Y-%m-%d %H:%M:%S")
    inp.import_news_to_database()
    print "import archieved End Time: ",datetime.strftime(datetime.now(),"%Y-%m-%d %H:%M:%S")
    
    "import the historical stock index"
    print "import stock Index Start Time: ",datetime.strftime(datetime.now(),"%Y-%m-%d %H:%M:%S")
    ihs.execute()
    print "import stock Index End Time: ",datetime.strftime(datetime.now(),"%Y-%m-%d %H:%M:%S")
    
    "Divide the Originial News File into Two Parts:Training part and Test Part"
    print "Divide News File Start Time: ",datetime.strftime(datetime.now(),"%Y-%m-%d %H:%M:%S")
    divide_archived_news(endDate)
    print "Divide News File End Time: ",datetime.strftime(datetime.now(),"%Y-%m-%d %H:%M:%S")
    
    "Clustering the time serial Stock index value"
    print "Clustering Start Time: ",datetime.strftime(datetime.now(),"%Y-%m-%d %H:%M:%S")
    ct.clusterSet(endDate)
    print "Clustering End Time: ",datetime.strftime(datetime.now(),"%Y-%m-%d %H:%M:%S")
    
    "Computing the trends contribution and probability"
    print "Trend Contribution Start Time: ",datetime.strftime(datetime.now(),"%Y-%m-%d %H:%M:%S")
    ctc.compute_trend_contribution()
    print "Trend Contribution End Time: ",datetime.strftime(datetime.now(),"%Y-%m-%d %H:%M:%S")
    
    "Creating the Vocabulary"
    print "Creating the Vocabulary Start Time: ",datetime.strftime(datetime.now(),"%Y-%m-%d %H:%M:%S")
    cv.create_vocabulary()
    print "Creating the Vocabulary End Time: ",datetime.strftime(datetime.now(),"%Y-%m-%d %H:%M:%S")
    
    "Computing the Term Contribution"
    print "Computing the Term Contribution Start Time: ",datetime.strftime(datetime.now(),"%Y-%m-%d %H:%M:%S")
    ctermc.compute_term_contribution()
    print "Computing the Term Contribution End Time: ",datetime.strftime(datetime.now(),"%Y-%m-%d %H:%M:%S")

if __name__ == "__main__":
    if len(sys.argv) != 3:
        print len(sys.argv) ,"Please Input Traing Start and End Time value as following format: TrainingStart.py yyyy-mm-dd yyyy-mm-dd \n"
        exit(0)
    startDate = sys.argv[1]
    endDate = sys.argv[2]
    execute(startDate,endDate)
    
    
    