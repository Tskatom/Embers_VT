import json
import numpy
import nltk

news = open('d:/embers/financemodel/BloombergNewsGroupByStock.json')
jsonNews = json.load(news)
#remove all the duplicated articles
newsWarehouse = {}
for stockIndex in jsonNews:
    for articleId in jsonNews[stockIndex]:
        newsWarehouse[articleId] = jsonNews[stockIndex][articleId]

negKeywords = open("D:/negativeCleaned.txt").readlines()
keyWords = []
for w in negKeywords:
    keyWords.append(w.replace("\n",""))

posiKeyWords = open("D:/positiveCleaned.txt").readlines()
for w in posiKeyWords:
    keyWords.append(w.replace("\n",""))
    
print "Over Here"

wordFreq = {}
flatCount = 0
for news in newsWarehouse:
    flatCount = flatCount + 1
    print "Id=%d, ArticleId=%s" %(flatCount,news)
    doc = newsWarehouse[news]
    #print doc
    tokens = nltk.word_tokenize(doc["content"])
    stemmer = nltk.stem.snowball.SnowballStemmer('english')
    words = [w.lower() for w in tokens if w not in [",",".",")","]","(","[","*",";","...",":","&",'"'] and not w.isdigit()]
    words = [w for w in words if w.encode("utf8") not in nltk.corpus.stopwords.words('english')]
    stemmedWords = [stemmer.stem(w) for w in words]
    fdist=nltk.FreqDist(stemmedWords)
    for word in keyWords:
        if word in fdist:
            if word in wordFreq:
                wordFreq[word] = wordFreq[word] + fdist[word]
            else:
                wordFreq[word] = fdist[word]
                    
                    
print wordFreq

#sorted_obj2 = wordFreq.iteritems()
sorted_obj2 = sorted(wordFreq.items(), key=lambda x: x[1],reverse=True)
print sorted_obj2[0][1]

output = open("d:/embers/financeModel/vocabulary.txt","w")
i = 1
for word in sorted_obj2:
    if i >150:
        break
    else:
        output.write(word[0])
        output.write("\n")
        i =  i + 1

output.close()        