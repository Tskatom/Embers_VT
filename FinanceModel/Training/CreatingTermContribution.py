from __future__ import division
import nltk
import json
from datetime import datetime

"""
This script used to calculate the contribution of each term to the individual clusters
"""
vocaLines = open("d:/embers/financeModel/vocabulary.txt").readlines()
vocaList = [w.replace("\n","") for w in vocaLines]
print vocaList

stemmer = nltk.stem.snowball.SnowballStemmer('english')
print "StartTime: ",datetime.strftime(datetime.now(),"%Y-%m-%d %H:%M:%S")

finalWordContribution = {}
"Iteratively to access each Stock Index"
trainingFile = json.load(open("d:/embers/financeModel/TrainingSet.txt"))
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

jsString = json.dumps(finalWordContribution)
with open("d:/embers/financemodel/output/termContribution.json","w") as output:
    output.write(jsString)
