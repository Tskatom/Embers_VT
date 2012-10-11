import json
from datetime import datetime
from Util import common
import os
import re

def create_match_rule():
    comListFile = common.get_configuration("model", "COMPANY_LIST")
    comList = json.load(open(comListFile))
    rule = "("
    for stock in comList:
        for company in comList[stock]:
            company.replace("\\.","\\\\.")
            "check If the company name only contain one word, then we will add blank before and after the name"
            if company.find(" ") < 0:
                eachRule = " " + company + " " + "|"
            else:
                eachRule = company + "|"
            rule += eachRule
    rule = rule[0:len(rule)-1] + ")"
    return rule

def group_daily_articles():
    
    stockArticles = {}
    
    archiveDir = common.get_configuration("model", "ARCHIVE_NEWS_DIR")
    dailyFileNames = os.listdir(archiveDir)
    matchRule = create_match_rule()
    pattern = re.compile(matchRule,re.I)
    
    "Construct company-stock object"
    comListFile = common.get_configuration("model", "COMPANY_LIST")
    comList = json.load(open(comListFile))
    comStock = {}
    for stock in comList:
        for company in comList[stock]:
            comStock[company.strip()] = stock
            
    i = 0
    print "StartTime: ",datetime.strftime(datetime.now(),"%Y-%m-%d %H:%M:%S")
    for dailyFile in dailyFileNames:
        dailyNews = json.load(open(archiveDir+ "/" + dailyFile),encoding='ISO-8859-1')
        for news in dailyNews:
            content = news["content"]
            matchedList = pattern.findall(content)
            matchedGroup = []
            if matchedList:
                i = i + 1
                for item in matchedList:
                    matchedGroup.append(item)
            matchedGroup = {}.fromkeys(matchedGroup).keys()
            
            "Group the news to matched stock"
            for item in matchedGroup:
                item = item.strip()
                if item in comStock:
                    stockIndex = comStock[item]
                    if stockIndex not in stockArticles:
                        stockArticles[stockIndex] = {}
                    articleId = news["articleId"]
                    stockArticles[stockIndex][articleId] = news
    print i
    
    "Write the grouped articles to file"
    groupedFile = common.get_configuration("model","GROUP_STOCK_NEWS")
    with open(groupedFile,"w") as output:
        output.write(json.dumps(stockArticles))
    print "EndTime: ",datetime.strftime(datetime.now(),"%Y-%m-%d %H:%M:%S")   
    
def test():
    group_daily_articles()

if __name__ == "__main__":
    test()        

#news = open('D:/filterBloombergArray.json')
#jsonNews = json.load(news,encoding='ISO-8859-1')
#
#stockNews = {}
#
#for company in jsonNews:
#    articles = company["articles"]
#    stockIndex = company["stockIndex"]
#    print stockIndex
#    if stockIndex not in stockNews:
#        stockNews[stockIndex] = {}
#    #add the articles into article list and exclude the duplicated one
#    for article in articles:
#        #check if the article is already in the articles
#        articleId = article["articelId"]
#        newsData = time.strptime(articleId[0:8],"%Y%m%d")
#        stockNews[stockIndex][articleId] = article
#
#jsonString = json.dumps(stockNews)
#with open("d:/embers/financemodel/BloombergNewsGroupByStock.json","w") as output:
#    output.write(jsonString)

