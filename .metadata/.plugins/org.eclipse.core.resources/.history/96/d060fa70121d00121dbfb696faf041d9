import json
import numpy
import nltk
import sys
from Util import calculator
import sqlite3 as lite



con = None
try:
    con = lite.connect("d:/sqlite/embers.db")
    cur = con.cursor()
    cur.execute("update t_news_process_mission set mission_status=? where embers_id=?",("1","0fa41b5f0e8a43bdf083643d2506a6fc3e03a1a3",))
    con.commit()
    
except lite.Error, e:
    print "Error %s:" % e.args[0]
    sys.exit(1)
finally:
    if con:
        con.close()    