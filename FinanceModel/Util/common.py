# Functions in this module will only be common for the whole probject
import ConfigParser
import sqlite3 as lite

def get_configuration( section_name, configuration, cfgFilename= '../Config/config.cfg'):
    config = ConfigParser.ConfigParser()
    with open( cfgFilename, 'r' ) as cfgFile:
        config.readfp( cfgFile )
    configuration = config.get( section_name, configuration )
    return configuration

def getLocationByStockIndex(stockIndex):
    con = getDBConnection()
    cur = con.cursor()
    sql = "select country from s_stock_country where stock_index=?"
    cur.execute(sql,(stockIndex,))
    result = cur.fetchone()
    country = result[0]
    return country

def getDBConnection():
    dbPath = get_configuration("info","DB_FILE_PATH")
    con = lite.connect(dbPath)
    con.text_factory = str
    return con
