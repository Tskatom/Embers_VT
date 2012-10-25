import sqlite3 as lite
import types
from Util import common

cfgPath = "../Config/config.cfg"
common.init(cfgPath)
# Evaluate the performace of the prediction result
class Evaluation:
    global con
#    con = lite.connect( "C:/Embers/Sqlite/embers.db" )
    con = common.getDBConnection()
    
    # get the total number of  raw data for events 0412
    def raw_data( self , eventCode ):
        cur = con.cursor()

        # 0412 warning type list 
        sqlString = "select count(*), stock_index from t_data_source where eventCode=? group by stock_index"
        
        cur.execute( sqlString , ( [eventCode] ) )
        
        rawRows = cur.fetchall()
        eventCodeList = []
        for rawRow in rawRows:
            eventCode = {}
            eventCode[rawRow[1]] = rawRow[0]
            eventCodeList.append( eventCode )
        return eventCodeList
    
    #get the total number of prediction result whose event code is 0412
    def prediction_result( self , eventCode ):
        cur = con.cursor()
        
        sqlString = "select count(*), stock_index from t_prediction_result where eventCode=? group by stock_index"
        
        cur.execute( sqlString , ( [eventCode] ) )
        
        predictionRows = cur.fetchall()
        predictionResultList = []
        for predictionRow in predictionRows:
            eventCode0412 = {}
            eventCode0412[predictionRow[1]] = predictionRow[0]
            predictionResultList.append( eventCode0412 )
        return predictionResultList
    
    # merge all records bwteen raw data from excel and prediction results 
    def get_all_records( self , eventCode ):
        cur = con.cursor()
        
        # get all waring lists for event type 0412 if prediction result is correct compared to the actual result
        sqlString = "select u.* from (select ds.stock_index, ds.post_date, ds.eventCode from t_data_source ds where eventCode=? union select pr.stock_index, pr.post_date, pr.eventCode from t_prediction_result pr where eventCode=?) u" 
        
        cur.execute( sqlString , ( eventCode, eventCode ) )
        
        predictionRows = cur.fetchall()
        eventCodeList = []
        
        for predictionRow in predictionRows:
                eventCode = []
                eventCode.append( predictionRow[0] )
                eventCode.append( predictionRow[1] )
                eventCode.append( predictionRow[2] )
                eventCodeList.append( eventCode )
        return eventCodeList
    
    # get all correct records based on prediction results 
    def get_all_correct_records( self , eventCode ):
        cur = con.cursor()
        
        # get all waring lists for event type 0412 if prediction result is correct compared to the actual result
        sqlString = "select ds.stock_index, ds.post_date, ds.eventCode from t_data_source ds where eventCode=? intersect select pr.stock_index, pr.post_date, pr.eventCode from t_prediction_result pr where eventCode=?" 
        
        cur.execute( sqlString , ( [eventCode, eventCode] ) )
        
        predictionRows = cur.fetchall()
        
        eventCodeList = []
        for predictionRow in predictionRows:
                eventCode = []
                eventCode.append( predictionRow[0] )
                eventCode.append( predictionRow[1] )
                eventCode.append( predictionRow[2] )
                eventCodeList.append( eventCode )
        return eventCodeList
    
    # get unpredictive records from raw data in excel 
    def get_unpredictive_records( self, eventCode ):
        cur = con.cursor()
        
        # get all waring lists for event type 0412 if prediction result is correct compared to the actual result
        sqlString = "select pr.stock_index, pr.post_date, pr.eventCode from t_prediction_result pr where eventCode=? except select ds.stock_index, ds.post_date, ds.eventCode from t_data_source ds where eventCode=?" 
        
        cur.execute( sqlString , ( [eventCode,eventCode] ) )
        
        predictionRows = cur.fetchall()
        
        eventCodeList = []
        for predictionRow in predictionRows:
                eventCode = []
                eventCode.append( predictionRow[0] )
                eventCode.append( predictionRow[1] )
                eventCodeList.append( eventCode )
        return eventCodeList
    
    # get predictive wrong records
    def get_wrong_prediction_records( self , eventCode ):
        cur = con.cursor()
        
        # get all waring lists for event type 0412 if prediction result is correct compared to the actual result
        sqlString = "select ds.stock_index, ds.post_date, ds.eventCode from t_data_source ds where eventCode=? except select pr.stock_index, pr.post_date, pr.eventCode from t_prediction_result pr where eventCode=?" 
        
        cur.execute( sqlString , ( [eventCode,eventCode] ) )
        
        predictionRows = cur.fetchall()
        
        eventCodeList = []
        for predictionRow in predictionRows:
                eventCode = []
                eventCode.append( predictionRow[0] )
                eventCode.append( predictionRow[1] )
                eventCode.append( predictionRow[2] )
                eventCodeList.append( eventCode )
        return eventCodeList
    
    # merge unpredictive records count
    def get_unpredictive_records_count( self , eventCode ):
        
        unpredictiveRecords = self.get_unpredictive_records( eventCode )
        
        unpredictiveRecordCount = {}
        for i in range( 0, len( unpredictiveRecords ) ):
            if unpredictiveRecords[i][0] in unpredictiveRecordCount:
                continue
            count = 0
            for j in range( 0, len( unpredictiveRecords ) ):
                if unpredictiveRecords[i][0] == unpredictiveRecords[j][0]:
                    count += 1
            unpredictiveRecordCount[unpredictiveRecords[i][0]] = count
        return unpredictiveRecordCount

    # merge wrong evaluation records' count
    def get_wrong_evaluation_count( self , eventCode ):
        
        wrongPredictiveRecords = self.get_wrong_prediction_records( eventCode )
        
        wrongPredictiveRecordCount = {}
        if len( wrongPredictiveRecords ) <= 0:
            print "There is no wrong evaluation data."
            return None
            
        for i in range( 0, len( wrongPredictiveRecords ) ):
            if wrongPredictiveRecords[i][0] in wrongPredictiveRecordCount:
                continue
            count = 0
            for j in range( 0, len( wrongPredictiveRecords ) ):
                if wrongPredictiveRecords[i][0] == wrongPredictiveRecords[j][0]:
                    count += 1
            wrongPredictiveRecordCount[wrongPredictiveRecords[i][0]] = count
        return wrongPredictiveRecordCount
    
    # Get all wrong evaluation count data
    def get_all_wrong_evaluation_data( self, eventCode ):
        
        unpredictiveRecordCount = self.get_unpredictive_records_count( eventCode )
        
        wrongPredictiveRecordCount = self.get_wrong_evaluation_count( eventCode )
        
        totalWrongEvaluation = {}
        if isinstance( unpredictiveRecordCount, types.NoneType ):
            if isinstance( wrongPredictiveRecordCount, types.NoneType ):
                print "No unpredictive events and wrong prediction events"
            else:
                for stockIndexWrongPredictive, countWrongPredictive in wrongPredictiveRecordCount.iteritems():
                    totalWrongEvaluation[stockIndexWrongPredictive] = countWrongPredictive
        else:
            if isinstance( wrongPredictiveRecordCount, types.NoneType ):
                for stockIndexUnpredicitive, countUnpredictive in unpredictiveRecordCount.iteritems():
                    totalWrongEvaluation[stockIndexUnpredicitive] = countUnpredictive
            else:
                totalDefectKeys = {}
                totalDefectKeys.update( unpredictiveRecordCount )
                totalDefectKeys.update( wrongPredictiveRecordCount )
                for stockIndex in  totalDefectKeys.keys():
                    if ( stockIndex in unpredictiveRecordCount ) and ( stockIndex in wrongPredictiveRecordCount ):
                        totalWrongEvaluation[stockIndex] = unpredictiveRecordCount[stockIndex] + wrongPredictiveRecordCount[stockIndex]
                    elif ( stockIndex in unpredictiveRecordCount ):
                        totalWrongEvaluation[stockIndex] = unpredictiveRecordCount[stockIndex]
                    elif stockIndex in wrongPredictiveRecordCount:
                        totalWrongEvaluation[stockIndex] = wrongPredictiveRecordCount[stockIndex]
            
        return totalWrongEvaluation
    
    # Compute the wrong records ratio
    def compute_wrong_data_ratio( self , eventCode ):
        totalWrongRecordsCount = self.get_all_wrong_evaluation_data( eventCode )
        totalRecords = self.raw_data( eventCode )
        wrongRecordsRate = {}
        
        for stock_index, wrongRecordCount in totalWrongRecordsCount.iteritems():
            for totalItem in totalRecords:
                if stock_index in totalItem:
                    wrongRecordsRate[stock_index] = float( wrongRecordCount ) / float( totalItem[stock_index] )
        return wrongRecordsRate
    
    # Get all correct prediction data 
    def evaluate_correct_records( self, eventCode ):
        cur = con.cursor()
        
        sqlString = "select count(*), ds.stock_index from t_data_source ds, t_prediction_result pr where ds.eventCode=? and ds.eventCode=pr.eventCode and ds.post_date=pr.post_date and ds.stock_index=pr.stock_index group by ds.stock_index"
        
        cur.execute( sqlString , ( eventCode ) )
        
        correctRows = cur.fetchall()
        
        eventCode0412List = []
        for correctRow in correctRows:
            eventCode0412 = {}
            eventCode0412[correctRow[1]] = correctRow[0]
        eventCode = {}
        eventCode["0412"] = eventCode0412
        eventCode0412List.append( eventCode )
        return eventCode0412List
    
    # Compute the correction rate of the evaluation result
    def compute_correction_rate( self , eventCode ):
        cur = con.cursor()
        
        sqlString = "select count(*), ds.stock_index from t_data_source ds, t_prediction_result pr where ds.eventCode = ? and ds.eventCode=pr.eventCode and ds.post_date=pr.post_date and ds.stock_index=pr.stock_index group by ds.stock_index"
        
        cur.execute( sqlString, ( [eventCode] ) )
        
        predictionRows = cur.fetchall()
        
        # 0412 warning type list 
        
        sqlString = "select count(*), stock_index from t_data_source where eventCode=? group by stock_index"
        
        cur.execute( sqlString, ( [eventCode] ) )
        
        rawRows = cur.fetchall()
        eventCode0412List = []
        eventCode0412 = {}
        for rawRow in rawRows:
            for predictionRow in predictionRows:
                if rawRow[1] == predictionRow[1]:
                    eventCode0412[rawRow[1]] = float( predictionRow[0] ) / float( rawRow[0] )
                    break
                else:
                    continue
        eventCode = {}
        eventCode["0412"] = eventCode0412
        eventCode0412List.append( eventCode )
        return eventCode0412List
    
    def evaluate_correction_rate_0412( self , eventCode ):
        cur = con.cursor()
        
        sqlString = "select count(*), ds.stock_index from t_data_source ds, t_prediction_result pr where ds.eventCode=? and ds.eventCode=pr.eventCode and ds.post_date=pr.post_date and ds.stock_index=pr.stock_index group by ds.stock_index"
        
        cur.execute( sqlString, ( eventCode ) )
        
        predictionRows = cur.fetchall()
        
        # 0412 warning type list 
        sqlString = "select count(*), stock_index from t_data_source where eventCode=? group by stock_index"
        
        cur.execute( sqlString, ( eventCode ) )
        
        rawRows = cur.fetchall()
        eventCode0412List = []
        for rawRow in rawRows:
            for predictionRow in predictionRows:
                if rawRow[1] == predictionRow[1]:
                    eventCode0412 = {}
                    eventCode0412[rawRow[1]] = predictionRow[0] / rawRow[0]
                    eventCode0412List.append( eventCode0412 )
                    break
                else:
                    continue
        return eventCode0412List
    
def Test():
    eva = Evaluation()
    print "Get all 0412 events:"
    print eva.get_all_records( '0412' )
    print "Get all GSR events:"
    print eva.raw_data( '0412' )
    print "Get all predictive events:"
    print eva.prediction_result( '0412' )
    
    print "Get all correct evaluation events:"
    print eva.get_all_correct_records( '0412' )
    print "Get all correct evaluation ratio:"
    print eva.compute_correction_rate( "0412" )
    
    print "Get all unpredictive evaluation events:"
    print eva.get_unpredictive_records( '0412' )
    print "Get wrong predictive events:"
    print eva.get_wrong_prediction_records( '0412' )
    print "Get the wrong ratio of this evaluation:"
    print eva.compute_wrong_data_ratio( '0412' )
    
Test()
