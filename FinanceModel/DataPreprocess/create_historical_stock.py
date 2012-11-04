import sqlite3 as lite
from datetime import datetime
import json

conn = lite.connect('../bayesian_model/data/embers.db')
cur = conn.cursor()
sql = "select embers_id,stock_index,date,last_price,one_day_change from t_daily_stockindex order by date asc"
{"previousCloseValue":"21736.07","updateTime":"09/28/2012 16:10:05","name":"IGBVL","feed":"Bloomberg - Stock Index","date":"2012-10-01T03:00:03","queryTime":"10/01/2012 03:00:03","currentValue":"21674.79","type":"stock","embersId":"adc866f44487f212d44ee27af63936c523ef737c"}
cur.execute(sql)
results = cur.fetchall()
with open("d:/embers/historical_stock.json","w") as out:
    for result in results:
        
        embersId = result[0]
        stockIndex = result[1]
        date = result[2]
        lastPrice = result[3]
        oneDayChange = result[4]
       
        dayValue = {}
        dayValue["previousCloseValue"] = round(lastPrice-oneDayChange,4)
        updateTime = datetime.strftime(datetime.strptime(date,"%Y-%m-%d"),"%m/%d/%Y") + " 16:00:00"
        dayValue["updateTime"] = updateTime
        dayValue["name"] = stockIndex
        dayValue["type"] = "stock"
        dayValue["feed"] = "Bloomberg - Stock Index"
        dayValue["date"] = "2012-11-01T04:00:03"
        dayValue["queryTime"] = "10/01/2012 04:00:03"
        dayValue["currentValue"] = lastPrice
        dayValue["embersId"] = embersId
        
        out.write(json.dumps(dayValue))
        out.write("\n")
print updateTime
#    dayValue["updateTime"] = updateTime