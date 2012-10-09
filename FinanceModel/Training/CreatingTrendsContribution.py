from __future__ import division
import string
import json
import nltk
"""
This program is used to computing the trendsContribution 

"""
#read the trend segments file
trendFile = open("d:/embers/financemodel/output/TrainingSetRecords.json")
jsonTrend = json.load(trendFile)
print jsonTrend

#Group Trend By StockIndex
stockGroupTrend = {}
for trend in jsonTrend:
    stockIndex = trend[6]
    if stockIndex not in stockGroupTrend:
        stockGroupTrend[stockIndex] = []
    stockGroupTrend[stockIndex].append(trend)

for item in stockGroupTrend:
    stockGroupTrend[item].sort()
    stockGroupTrend[item] = [w[7] for w in stockGroupTrend[item]]
print stockGroupTrend

finalClusterMatrix = {}
finalClusterProbability = {}
for item in stockGroupTrend:
    #read all the line and skip the first line
    trendsSerial = stockGroupTrend[item]
    print trendsSerial
    clusterDist = nltk.FreqDist(trendsSerial)
    clusterProbability = {}
    for cl in clusterDist:
        clusterProbability[cl] = "%0.4f" %(clusterDist[cl]/sum(clusterDist.values()))
    print clusterProbability
    finalClusterProbability[item] = clusterProbability
    
    #Define the ultimated json object
    clusterMatrix = {}
    for cluster in range(1,21):
        #create matrix for each cluster
        matrix = [[0 for col in range(3)] for row in range(20)]
        for i in range(0,len(trendsSerial)):
            if cluster == trendsSerial[i]:
                t1 = 0
                t2 = 0
                t3 = 0
                if i - 1 >= 0:
                    t1 = trendsSerial[i-1]
                    matrix[t1-1][0] = matrix[t1-1][0] + 1
                if i - 2 >= 0:
                    t2 = trendsSerial[i-2]
                    matrix[t2-1][1] = matrix[t2-1][1] + 1
                if i - 3 >= 0:
                    t3 = trendsSerial[i-3]
                    matrix[t3-1][2] = matrix[t3-1][2] + 1
        
        #calculating the contribution matrix
        contributionMatrix = [[0 for col in range(3)] for row in range(20)]
        sumCol = [0,0,0]
        for col in range(3):
            for row in range(20):
                sumCol[col] = sumCol[col] + matrix[row][col]
        
        for col in range(3):
            for row in range(20):
                contributionMatrix[row][col] = "%0.4f" %((matrix[row][col] + 1)/(sumCol[col]+20))
        print contributionMatrix
        clusterMatrix[cluster] = contributionMatrix
        finalClusterMatrix[item] = clusterMatrix
        
with open("d:/embers/financeModel/output/FinalclusterContribution.json","w") as output:
    jsString = json.dumps(finalClusterMatrix)
    output.write(jsString)
    
with open("d:/embers/financeModel/output/FinalclusterProbability.json","w") as output2:
    jsString = json.dumps(finalClusterProbability)
    output2.write(jsString)