import sqlite3 as lite
from Util import common
import json
from datetime import datetime
import hashlib
from DataCollecting import BloombergNewsScrape as bns

# import history raw data into database        
def import_news_to_database():
    try:
        historyNews = open(common.get_configuration( "model", 'GROUP_STOCK_NEWS'))
        historyNewsJson = json.load(historyNews)
        
        for stockIndex in historyNewsJson:
            for article in historyNewsJson[stockIndex].values():
                news = {}
                news["title"] = article["title"]
                news["author"] = article["author"]
                postTime = article["postTime"].split(".")[0]
                postTime = datetime.strptime(postTime,"%Y-%m-%d %H:%M:%S")
                news["post_time"] = postTime
                news["post_date"] = postTime.date()
                news["content"] = article["content"]
                news["stock_index"] = stockIndex
                news["source"] = "Bloomberg News"
                news["update_time"] = article["queryTime"]
                news["newsUrl"] = article["newsUrl"]
                embersId = hashlib.sha1(article["content"]).hexdigest()
                news["embers_id"] = embersId
                ifExisted = bns.check_article_existed(news)
                if not ifExisted:
                    bns.insert_news(news)
                    "Insert into Mission process"
                    bns.insert_news_mission(news)
        bns.close_db_connection()
    except lite.Error, e:
        print "Error: %s" % e.args[0]
    finally:
            pass

if __name__ == "__main__":
    import_news_to_database()