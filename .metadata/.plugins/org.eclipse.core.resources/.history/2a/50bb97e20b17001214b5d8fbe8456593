import xlrd
import sqlite3 as lite
from datetime import datetime

# read excel data and insert database
class ImportExcelData:
    def read_data_from_excel( self ):
        
        excelData = xlrd.open_workbook( "D:\Embers\Doc\GSR August 2012 V2.xls" )
        
        sheetData = excelData.sheet_by_index( 0 )
        
        numberOfRows = sheetData.nrows 
        dataList = []
        
        # iterate each row to get the cell value and composite the data oject before inserting database
        for rowNo in range( 1, numberOfRows ):
            eventCode = sheetData.cell( rowNo, 7 ).value
            if eventCode in ['0411', '0412', '0421', '0422']:                
                dataObject = {}
                dataObject["country"] = sheetData.cell( rowNo, 4 ).value
                dataObject["eventCode"] = sheetData.cell( rowNo, 7 ).value
                dataObject["population"] = sheetData.cell( rowNo, 8 ).value
                cellObject = sheetData.cell( rowNo, 9 )
                if cellObject.ctype == xlrd.XL_CELL_DATE:
                    dateTuple = xlrd.xldate_as_tuple( cellObject.value, excelData.datemode )
                    year = dateTuple[0]
                    month = dateTuple[1]
                    day = dateTuple[2]
                    hour = dateTuple[3]
                    minute = dateTuple[4]
                    second = dateTuple[5]
                    date = datetime( year, month, day, hour, minute, second )
                dataObject["post_date"] = date.strftime( '%Y-%m-%d' )
                dataObject["source"] = sheetData.cell( rowNo, 11 ).value
                dataObject["title"] = sheetData.cell( rowNo, 12 ).value
                dataObject["content"] = sheetData.cell( rowNo, 13 ).value
                dataObject["url"] = sheetData.cell( rowNo, 14 ).value
                dataList.append( dataObject )
            else:
                continue
            
        return dataList
    
    # Insert excel data into database
    def insert_database( self ):
        try:
            
            con = lite.connect( "D:/Embers/Sqlite/embers.db" )
            cur = con.cursor()
            dataList = self.read_data_from_excel()
            
            sqlString = "insert into t_data_source(title,post_date,content,stock_index,source,update_time,country,eventCode,population,url) values(?,?,?,?,?,datetime('now','localtime'),?,?,?,?)"
            
            for item in dataList:
                title = item["title"]
                post_date = item["post_date"]
                content = item["content"]
                post_date = item["post_date"]
                stock_index = self.conver_to_stock_index( item["country"] )
                source = item["source"]
                country = item["country"]
                eventCode = item["eventCode"]
                population = item["population"]
                url = item["url"]
                cur.execute( sqlString, ( title, post_date, content, stock_index, source, country, eventCode, population, url ) )
            
            con.commit()
            return None
        except lite.Error, e:
            print "Error: %s" % e.args[0]
    
    # insert prediction result into database
    def insert_prediction_into_database( self, predictionResults ):
        con = lite.connect( "C:/Embers/Sqlite/embers.db" )
        cur = con.cursor()
        
        for predictionResult in predictionResults:
            embersId = predictionResult["embers_id"]
            eventCode = predictionResult["eventCode"]
            stock_index = predictionResult["stock_index"]
            post_date = predictionResult["post_date"]
            
            sqlString = "insert into t_prediction_result(embers_id, post_date, stock_index, update_time,eventCode) values(?,?,?,datetime('now','localtime'),?)"
            cur.execute( sqlString, ( embersId, post_date, stock_index, eventCode ) )
        
        con.commit()
    
    # Get stock_index value based on the country name from db
    def conver_to_stock_index( self, country ):
        
        con = lite.connect( "D:/Embers/Sqlite/embers.db" )
        cur = con.cursor()
        
        sqlString = "select stock_index from s_stock_country where country = ?"
        
        cur.execute( sqlString, ( [country] ) )
        
        stock_index = cur.fetchone()

        return stock_index[0]
          
def Test():
    excelData = ImportExcelData()
    
    predictionResults = [{'embers_id':'1','eventCode':'0000','stock_index':'BVPSBVPS','post_date':'2011-02-16'},{'embers_id':'2','eventCode':'0411','stock_index':'BVPSBVPS','post_date':'2011-07-13'},{'embers_id':'3','eventCode':'0412','stock_index':'BVPSBVPS','post_date':'2011-08-02'},{'embers_id':'4','eventCode':'0411','stock_index':'CHILE65','post_date':'2011-04-04'},{'embers_id':'5','eventCode':'0000','stock_index':'CHILE65','post_date':'2011-08-09'}]
    
    excelData.insert_prediction_into_database(predictionResults)
    
    #excelData.read_data_from_excel()
#    excelData.insert_database()
    
Test()

        
        
        
