wordLines = None
with open("../bayesian_model/data/vocabulary.txt","r") as f_read:
    wordLines = f_read.readlines()
termList = {}
wordList = []
for line in wordLines:
    line = line.replace("\n","").replace("\r","")
    wordList.append(line)
    termList[line] = 0
print wordList    