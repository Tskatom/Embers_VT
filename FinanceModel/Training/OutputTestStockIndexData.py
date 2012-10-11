#-*- coding:utf8-*-
from Util import common
import json
import hashlib

def export_test_stock_data(startDate):
    
    con = common.getDBConnection()
    cur = con.cursor()
    sql = "select embers_id,sub_sequence,stock_index,date,last_price,one_day_change from t_daily_stockindex where date>=?"
    cur.execute(sql,(startDate,))
    results = cur.fetchall()
    
    for row in results:
        derEmbersId = row[0]
        subSequence = row[1]
        stockIndex = row[2]
        date = row[3]
        lastPrice = row[4]
        oneDayChange = row[5]
        
        derivedFrom = "[" + derEmbersId + "]"
        changePercent = round(oneDayChange/(lastPrice-oneDayChange),4)
        trendType = get_trend_type(stockIndex,changePercent)
        
        enrichedData = {}
        enrichedData["derivedFrom"] = derivedFrom
        enrichedData["stockIndex"] = stockIndex
        enrichedData["date"] = date
        enrichedData["lastPrice"] = lastPrice
        enrichedData["oneDayChange"] = oneDayChange
        enrichedData["changePercent"] = changePercent
        enrichedData["trendType"] = trendType
        enrichedData["subsequenceId"] = subSequence
        enrichedDataEmID = hashlib.sha1(json.dumps(enrichedData)).hexdigest()
        enrichedData["embersId"] = enrichedDataEmID
        
        insertSql = "insert into t_daily_enrichedindex (embers_id,derived_from,sub_sequence,stock_index,date,last_price,one_day_change,change_percent,trend_type) values (?,?,?,?,?,?,?,?,?)"
    
        cur.execute(insertSql,(enrichedDataEmID,derivedFrom,subSequence,stockIndex,date,lastPrice,oneDayChange,changePercent,trendType))

    con.commit() 
        
      
def get_trend_type(stockIndex,changePercent):
    """
    Computing current day's trend type, compareing change percent to the trend range,
    Choose the nearnes trend as current day's type    
    """
    "Load the trend type range file"
    rangeFilePath = common.get_configuration("model", "TREND_RANGE_FILE")
    tFile = open(rangeFilePath)
    trendsJson = json.load(tFile)
    tJson = trendsJson[stockIndex]
    
    distance = 10000
    trendType = None
    for type in tJson:
        tmpDistance = min(abs(changePercent-tJson[type][0]),abs(changePercent-tJson[type][1]))
        if tmpDistance < distance:
            distance = tmpDistance
            trendType = type
    return trendType  

if __name__=="__main__":
    export_test_stock_data("2012-01-01") 