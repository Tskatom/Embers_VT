import WarningCreate
import RawNewsProcess
import RawStockProcess

def execute(predictionDate,rawStockFilePath,rawNewsFilePath):
    #process raw Stock Process data
    RawStockProcess.execute(rawStockFilePath)
    #process raw news data
    RawNewsProcess.execute(rawNewsFilePath)
    #Warning Create
    WarningCreate.execute(predictionDate)
    