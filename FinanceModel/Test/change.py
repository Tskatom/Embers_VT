import json
file = open("d:/result.json")
jsonObj = json.load(file)
for stock in jsonObj:
    print stock,"\n\n"
    for row in jsonObj[stock]:
        print row["date"],"\t", row["cBottom"],"\t", row["cUpper"], "\t", row["currentValue"]