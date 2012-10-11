from __future__ import division
import nltk
import json
from datetime import datetime,timedelta
import time
from Util import common

"""
This script used to calculate the contribution of each term to the individual clusters
"""

def group_news_by_cluster():
    "Load the Traing news File"
    trainingNewsFile = common.get_configuration("model", "TRAINING_NEWS_FILE")
    articles = json.load(open(trainingNewsFile))
    finalStockClusterNews = {}
    "Iterately read the news"
    for index in articles:
        indexNews = articles[index]
        dayNews = {}
        #group the news by date
        for articleId in indexNews:
            day = articleId[0:8]
            if day not in dayNews:
                dayNews[day] = []
            dayNews[day].append(indexNews[articleId])
        
    
        #read the day cluster file to group the date
        clusterDays = {}
        trendFilePath = common.get_configuration("model", "TRAINING_TREND_RECORDS")
        trendFile = open(trendFilePath)
        trendJson = json.load(trendFile)
        for trend in trendJson:
            if index == trend[6]:
                cluster = trend[7]
                structDate = time.strptime(trend[2],"%Y-%m-%d")
                dtDay = datetime(structDate[0],structDate[1],structDate[2])
                for i in range(1,4):
                    day = dtDay - timedelta(days=i)
                    dayStr = day.strftime("%Y%m%d")
                    if cluster not in clusterDays:
                        clusterDays[cluster] = []
                    if dayStr not in clusterDays[cluster]:
                        clusterDays[cluster].append(dayStr)
    
        clusterNews = []
        for cluster in clusterDays:
            cNews = {}
            cNews["cluster"] = cluster;
            docs = []
            for day in clusterDays[cluster]:
                if day in dayNews:
                    for doc in dayNews[day]:
                        docs.append(doc)
            cNews["articles"] =  docs;
            clusterNews.append(cNews)
        finalStockClusterNews[index] = clusterNews
           
    return finalStockClusterNews

def compute_term_contribution():
    "Read the Vocabulary File"
    vocabularyFilePath = common.get_configuration("model", "VOCABULARY_FILE")
    vocaLines = open(vocabularyFilePath).readlines()
    vocaList = [w.replace("\n","") for w in vocaLines]
    
    stemmer = nltk.stem.snowball.SnowballStemmer('english')
    print "StartTime: ",datetime.strftime(datetime.now(),"%Y-%m-%d %H:%M:%S")
    
    finalWordContribution = {}
    "Iteratively to access each Stock Index"
    trainingFile = group_news_by_cluster()
    for index in trainingFile:
        stockNews = trainingFile[index]
        wordContribution = {}
        for cluster in stockNews:
            #computing the words count in each cluster
            articles = cluster["articles"]
            #initiate the wordFreq
            wordFreq = {}
            for term in vocaList:
                wordFreq[term] = 0
            for article in articles:
                content = article["content"]
                tokens = nltk.word_tokenize(content)
                words = [w.lower() for w in tokens if w not in [",",".",")","]","(","[","*",";","...",":","&",'"'] and not w.isdigit()]
                words = [w for w in words if w.encode("utf8") not in nltk.corpus.stopwords.words('english')]
                stemmedWords = [stemmer.stem(w) for w in words]
                fdist=nltk.FreqDist(stemmedWords)
                for term in wordFreq:
                    if term in fdist:
                        wordFreq[term] = wordFreq[term] + fdist[term]
            #computing the word contribution
            count = sum(wordFreq.values())
            contributions = {}
            for term in wordFreq:
                contribution = (wordFreq[term]+1)/(count + len(wordFreq))
                contributions[term] = "%0.4f" %contribution
        #       print "term:%s, contribution:%f" %(term,contribution)
            
            # add the contributions to each cluster
            wordContribution[cluster["cluster"]] = contributions
    
        finalWordContribution[index] = wordContribution    
    print "EndTime: ",datetime.strftime(datetime.now(),"%Y-%m-%d %H:%M:%S")
    
    "Write the Term Contribution To File"
    termContributionFile = common.get_configuration("model", "TERM_CONTRIBUTION_PATH")
    jsString = json.dumps(finalWordContribution)
    with open(termContributionFile,"w") as output:
        output.write(jsString)

if __name__=="__main__":
    print "group_news_by_cluster Start Time : ",datetime.strftime(datetime.now(),"%Y-%m-%d %H:%M:%S")
    group_news_by_cluster()
    print "group_news_by_cluster End Time : ",datetime.strftime(datetime.now(),"%Y-%m-%d %H:%M:%S")
    