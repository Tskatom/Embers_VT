import nltk
import json
import cProfile

def enrich_news(file_path):
    g_news = json.load(open(file_path,"r"))
    print g_news.keys()
    stemmer = nltk.stem.snowball.SnowballStemmer('english')
    for k,v in g_news.items():
        for k1,v1 in v.items():
            content = v1["content"]
            tokens = nltk.word_tokenize(content)
            words = [w.lower() for w in tokens if w not in [",",".",")","]","(","[","*",";","...",":","&",'"'] and not w.isdigit()]
            words = [w for w in words if w.encode("utf8") not in nltk.corpus.stopwords.words('english')]
            stemmedWords = [stemmer.stem(w) for w in words]
            fdist=nltk.FreqDist(stemmedWords)
            v1["words"] = fdist
        print "Done: ",k
    "Write words back to file"
    with open("d:/embers/financeModel/output/enriched_with_country_BBNews-Group-Stock.json","w") as out_q:
        out_q.write(json.dumps(g_news))
    
def main():
    f = "d:/embers/financeModel/output/with_country_BBNews-Group-Stock.json"
    enrich_news(f)
    pass

if __name__ == "__main__":
    main()