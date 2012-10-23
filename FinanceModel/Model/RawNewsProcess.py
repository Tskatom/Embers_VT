#-*- coding:utf8 -*-
import nltk
import json
import sqlite3 as lite
import sys
import exceptions
import hashlib
from datetime import datetime
from Util import common
from etool import queue

con = None
cur = None

def get_db_connection():
    global cur
    global con
    try:
        con = common.getDBConnection()
        con.text_factory = str
        cur = con.cursor()
    except lite.Error, e:
        print "Error: %s" % e.args[0]

def close_db_connection():
    global con
    con.commit()
    if con:
        con.close()  

def insert_news(article):
    try:
        global con
        sql = "insert into t_daily_news(embers_id,title,author,post_time,post_date,content,stock_index,source,update_time,url) values (?,?,?,?,?,?,?,?,?,?)"
        embersId = article["emberdId"]
        title = article["title"]
        author = article["author"]
        postTime = article["postTime"]
        postDate = article["postDate"]
        content = article["content"]
        stockIndex = article["stockIndex"]
        source = article["source"]
        updateTime = article["updateTime"]
        url = article["url"]
        cur.execute(sql,(embersId,title,author,postTime,postDate,content,stockIndex,source,updateTime,url))
        
    except lite.Error, e:
        print "Error insert_news: %s" % e.args[0]
    finally:
        pass

def insert_news_mission(article):
    try:
        global con
        global cur
        sql = "insert into t_news_process_mission(embers_id,mission_name,mission_status,insert_time) values (?,?,?,datetime('now','localtime'))"
        
        embersId = article["embers_id"]
        missionName = "Bag of Words"
        missionStatus = "0"
        cur.execute(sql,(embersId,missionName,missionStatus))
        
    except lite.Error, e:
        print "Error Insert news Mission: %s" % e.args[0]
    finally:
        pass
    
def check_article_existed(article):
    try:
        global con
        global cur
        flag = True
        title = article["title"]
        postDay = article["post_date"]
        sql = "select count(*) count from t_daily_news where post_date=? and title=?"
        cur.execute(sql,(postDay,title,))
        count = cur.fetchone()[0]
        count = int(count)
        if count == 0:
            flag = False
        else:
            flag = True
    except lite.ProgrammingError as e:
        print e
    except:
        print "Error+++++: %s" %sys.exc_info()[0]
    finally:
        return flag  
    
def check_enrichedata_existed(embersId):
    try:
        global con
        global cur
        flag = True
        sql = "select count(*) count from t_daily_enrichednews where embers_id=?"
        cur.execute(sql,(embersId,))
        count = cur.fetchone()[0]
        count = int(count)
        if count == 0:
            flag = False
        else:
            flag = True
    except lite.ProgrammingError as e:
        print e
    except:
        print "Error: %s" %sys.exc_info()[0]
    finally:
        return flag    
    
def import_to_database(rawNewsFilePath):
    global con
    stockNews = json.load(open(rawNewsFilePath,"r"))
    for stock in stockNews:
        i = 0
        for article in stockNews[stock]:
            article["stock_index"] = stock
            "Check if the article has being collected: if so,just skip, otherwise insert into database"
            "commit to database for each 10 records"
            ifExisted = check_article_existed(article)
            if ifExisted:
                continue
            else:
                insert_news(article)
                insert_news_mission(article)
                i = i +1
                if i >= 100:
                    con.commit()
                    i = 0
    con.commit()

def get_uncompleted_mission():
    global con
    global cur
    try:
        sql = "select embers_id from t_news_process_mission where mission_status = '0'"
        cur.execute(sql)
        rows = cur.fetchall()
        i = 0
        
        port = common.get_configuration("info", "ZMQ_PORT")
        with queue.open(port, 'w', capture=True) as outq:
            for row in rows:
                sql2 = "select embers_id,title,author,post_time,post_date,stock_index,content,source,update_time from t_daily_news where embers_id=?"
                cur2 = con.cursor()
                cur2.execute(sql2,(row[0],))
                rows2 = cur2.fetchall()
                for row2 in rows2:
                    insertSql = "insert into t_daily_enrichednews (embers_id,derived_from,title,author,post_time,post_date,content,stock_index,source,raw_update_time,update_time) values (?,?,?,?,?,?,?,?,?,?,?)"
                    updateSql = "update t_news_process_mission set mission_status=? where embers_id=?"
                    derivedFrom = "["+row2[0]+"]"
                    title = row2[1]
                    author = row2[2]
                    postTime = row2[3]
                    postDate = row2[4]
                    stockIndex = row2[5]
                    content = row2[6]
                    source = row2[7]
                    rawUpdateTime = row2[8]
                    try:
                        tokens = nltk.word_tokenize(content)
                        stemmer = nltk.stem.snowball.SnowballStemmer('english')
                        words = [w.lower() for w in tokens if w not in [",",".",")","]","(","[","*",";","...",":","&",'"'] and not w.isdigit()]
                        words = [w for w in words if w.encode("utf8") not in nltk.corpus.stopwords.words('english')]
                        stemmedWords = [stemmer.stem(w) for w in words]
                        fdist=nltk.FreqDist(stemmedWords)
                        jsonStr = json.dumps(fdist)
                        embersId = hashlib.sha1(jsonStr).hexdigest()
                        updateTime = datetime.strftime(datetime.now(),"%Y-%m-%d %H:%M:%S")
                        
                        enrichedData = {}
                        enrichedData["emberdId"] = embersId
                        enrichedData["derivedFrom"] = derivedFrom
                        enrichedData["title"] = title
                        enrichedData["author"] = author
                        enrichedData["postTime"] = postTime
                        enrichedData["postDate"] =  postDate
                        enrichedData["content"] = jsonStr
                        enrichedData["stockIndex"] = stockIndex
                        enrichedData["source"] = source
                        enrichedData["updateTime"] = updateTime
                        enrichedData["rawUpdateTime"] = rawUpdateTime
                        
                        cur3 = con.cursor()
                        if not check_enrichedata_existed(embersId):
                            cur3.execute(insertSql,(embersId,derivedFrom,title,author,postTime,postDate,jsonStr,stockIndex,source,rawUpdateTime,updateTime))
                            outq.write(json.dumps(enrichedData, encoding='utf8'))
                            
                        cur3.execute(updateSql,("1",row2[0],)) 
                        i = i + 1
                        if i%100 == 0:
                            con.commit()
                    except lite.ProgrammingError as e:
                        print "Error:",e               
                    except:
                        print "Error:", sys.exc_info()
                        continue
    except exceptions.IndexError as e:
        print e            
    except lite.OperationalError as e:
        print e
    except:
        print "Error****: ", sys.exc_info()[0]

def execute(rawNewsFilePath):
    get_db_connection()
    import_to_database(rawNewsFilePath)
    get_uncompleted_mission()
    close_db_connection()
    
if __name__ == "__mian__":
    if len(sys.argv)!= 2:
        print "Please Enter the RawNewsFilePath as parameter: "
        exit(0)
    
    rawNewsFilePath = sys.argv[1]
    execute(rawNewsFilePath)