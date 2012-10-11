import json
import nltk
from Util import common

def create_vocabulary():
    "Read the Negative Finance Dictionary"
    negativeFilePath = common.get_configuration("model", "NEGATIVE_DIC")
    negativeDoc  = open(negativeFilePath).readlines()
    stemmer = nltk.stem.snowball.SnowballStemmer('english')
    negativeWords = []
    for l in negativeDoc:
        negativeWords.append(stemmer.stem(l.replace("\n","")))
    
    fdist = nltk.FreqDist(negativeWords)
    negKeywords = []
    for k in fdist:
        negKeywords.append(k)
    
    "Read the Positive Finance Dictionary"
    positiveFilePath = common.get_configuration("model", "POSITIVE_DIC")
    positiveDoc = open(positiveFilePath).readlines()
    postiveWords = []
    for line in positiveDoc:
        postiveWords.append(stemmer.stem(line.replace("\n","")))
    
    fdist = nltk.FreqDist(postiveWords)
    posiKeyWords = []
    for posWord in fdist:
        posiKeyWords.append(posWord)
    
    
    "Read the archived news to count the top words"
    BBNewsPath = common.get_configuration("model", "TRAINING_NEWS_FILE")
    news = open(BBNewsPath)
    jsonNews = json.load(news)
    #remove all the duplicated articles
    newsWarehouse = {}
    for stockIndex in jsonNews:
        for articleId in jsonNews[stockIndex]:
            newsWarehouse[articleId] = jsonNews[stockIndex][articleId]
    
    keyWords = []
    for w in negKeywords:
        keyWords.append(w)
    
    for w in posiKeyWords:
        keyWords.append(w)
        
    print "Over Here"
    
    wordFreq = {}
    flatCount = 0
    for news in newsWarehouse:
        flatCount = flatCount + 1
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
    
    "Write the vocabulary list to File"
    vocabularyFile = common.get_configuration("model", "VOCABULARY_FILE")
    output = open(vocabularyFile,"w")
    i = 1
    for word in sorted_obj2:
        if i > 150:
            break
        else:
            output.write(word[0])
            output.write("\n")
            i =  i + 1
    
    output.close()        