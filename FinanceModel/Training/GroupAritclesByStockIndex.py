import json
import numpy
import nltk
import time
import datetime

news = open('D:/filterBloombergArray.json')
jsonNews = json.load(news,encoding='ISO-8859-1')

stockNews = {}

for company in jsonNews:
    articles = company["articles"]
    stockIndex = company["stockIndex"]
    print stockIndex
    if stockIndex not in stockNews:
        stockNews[stockIndex] = {}
    #add the articles into article list and exclude the duplicated one
    for article in articles:
        #check if the article is already in the articles
        articleId = article["articelId"]
        newsData = time.strptime(articleId[0:8],"%Y%m%d")
        stockNews[stockIndex][articleId] = article

jsonString = json.dumps(stockNews)
with open("d:/embers/financemodel/BloombergNewsGroupByStock.json","w") as output:
    output.write(jsonString)

