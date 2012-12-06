import nltk
import sqlite3 as lite
from datetime import datetime
import json
from math import sqrt,radians,cos,sin,asin,ceil
import numpy as np
import threading
import argparse

"""
compute the similarity between two crime records
properties:
Time: day of Month, day of week, AM/PM 0.1
Location: latx, lony 0.1
Category: URC_category, category 0.2
Description: List of words without stopwords 0.6
"""
class CompareThread(threading.Thread):
    def __init__(self,thread_name,f_name,o_name):
        self.thread_name = thread_name
        self.f_name = f_name
        self.o_name = o_name
        threading.Thread.__init__(self)
    def run(self):
        compute_similarity(self.f_name,self.o_name)

class ProcessThread(threading.Thread):
    def __init__(self,thread_name,db_file,out_r,category):
        self.thread_name = thread_name
        self.category = category
        self.out_r = out_r
        self.db_file = db_file
        threading.Thread.__init__(self)
    def run(self):
        pre_process(self.db_file,self.out_r,self.category)
        
def pre_process(db_file,out_r,category):
    
    print "start: ",datetime.strftime(datetime.now(),"%Y-%m-%d %H:%M:%S")
    conn = lite.connect(db_file)
    cur = conn.cursor()
    
    "get the count of all records"
    sql = "select count(*) from t_crime where URC_catagorie = '{}'".format(category)
    cur.execute(sql)
    rs = cur.fetchone()
    count = rs[0]
    
    sql = "select start_date,URC_catagorie,category,latx,lony,description,id from t_crime where URC_catagorie = '{}'".format(category)
    cur.execute(sql)
    rs = cur.fetchall()
    
    d_format = "%Y-%m-%d"
    crime_records = []
    i = 0
    for r in rs:
        s_d = r[0]
        urc_c = r[1]
        c = r[2]
        latx = float(r[3])
        lony = float(r[4])
        des = r[5]
        e_id = r[6]
        
        try:
            "tokenize and stem"
            tokens = nltk.word_tokenize(des)
            stemmer = nltk.stem.snowball.SnowballStemmer('english')
            words = [w.lower().strip() for w in tokens if w not in [",",".",")","]","(","[","*",";","...",":","&",'"',"'"] and not w.isdigit()]
            words = [w for w in words if w.encode('utf8') not in nltk.corpus.stopwords.words('english')]
            stemwords = [stemmer.stem(w) for w in words]
            fdist = nltk.FreqDist(stemwords)
            
            "Construct Time info"
            s_d = datetime.strptime(s_d,d_format)
            day_of_month = s_d.day
            day_of_week = s_d.weekday()
            "extract am/pm info"
            if "a.m." in fdist:
                flag = 1
            elif "p.m." in fdist:
                flag = 2
            else:
                flag = 0
            t_info = [day_of_month,day_of_week,flag]
            
            "construct location info"
            l_info = [latx,lony]
            
            "construct category info"
            c_info = [urc_c,c]
            
            "construct the description info"
            max_c = max(fdist.values())
            for k,v in fdist.items():
                fdist[k] = 1.0*v/max_c
            d_info = fdist.copy()
            
            crime_records.append({"id":e_id,"time":t_info,"location":l_info,"category":c_info,"desc":d_info})
        except Exception:
            pass
        finally:
            i = i + 1
            for j in np.arange(0.1,1.01,0.01):
                if i == ceil(count*j):
                    print category," %",str(100*j)," done"
                
    "write to file"
    file_name = out_r + "/" + category.replace(" ","_") + ".json"
    with open(file_name,"w") as out_q:
        out_q.write(json.dumps(crime_records))
    
    print "end: ",datetime.strftime(datetime.now(),"%Y-%m-%d %H:%M:%S")

def haversine(lon1,lat1,lon2,lat2):
    """
    Calculate the great circle distance between two points 
    on the earth (specified in decimal degrees)
    """
    # convert decimal degrees to radians 
    lon1, lat1, lon2, lat2 = map(radians, [lon1, lat1, lon2, lat2])
    # haversine formula 
    dlon = lon2 - lon1 
    dlat = lat2 - lat1 
    a = sin(dlat/2)**2 + cos(lat1) * cos(lat2) * sin(dlon/2)**2
    c = 2 * asin(sqrt(a)) 
    mile = 6367 * c * 0.621371
    return mile 

def compute_similarity(r_file,o_name):
    c_records = json.load(open(r_file))
    length = len(c_records)
    "Write the result to file"
    f_w = open(o_name,"w")
    
    for i in range(0,length-1):
        r1 = c_records[i]
        f_w.write(str(r1["id"]) + "\t")
        for j in range(i+1,length):
            r2 = c_records[j]
            similarity = sigle_compare(r1,r2)
            if similarity > 0.5:
                f_w.write(str(r2["id"]) + "\t" + str(similarity) + "\t")
        f_w.write("\n")
        
        for s in np.arange(0.,1.01,0.01):
                if i == ceil(length*s):
                    print r_file, " %",str(100*s)," done"
        f_w.flush()
    f_w.close()        
           
        
def sigle_compare(r1,r2):
    global CONFIG_FILE
    "get the coefficient of each property"
    cof = json.load(open(CONFIG_FILE))
    time_cof = cof["time"]
    location_cof = cof["location"]
    category_cof = cof["category"]
    des_cof = cof["desc"]
    
    
    "compare the time info"
    r1_t_info = r1["time"]
    r2_t_info = r2["time"]   
    
    "compare day of month"
    if abs(r1_t_info[0]-r2_t_info[0]) <= 3:
        md_flag = 1
    else:
        md_flag = 0
    
    "compare day of week"
    if abs(r1_t_info[1]-r2_t_info[1]) <= 1:
        wd_flag = 1
    else:
        wd_flag = 0
    
    "compare am or pm"
    if r1_t_info[2] == r1_t_info[2] and r1_t_info[2] != 0 and r2_t_info[2] != 0:
        ap_flag = 1
    else:
        ap_flag = 0
    
    time_weight = time_cof * (md_flag*0.3  + wd_flag*0.4 + ap_flag*0.3)
    
    "Compare the Location similarity"
    r1_l_info = r1["location"]
    r2_l_info = r2["location"]
    lon1 = r1_l_info[0]
    lat1 = r1_l_info[1]
    lon2 = r2_l_info[0]
    lat2 = r2_l_info[1]
    
    distance = haversine(lon1,lat1,lon2,lat2)
    
    if distance > 25.0:
        dis_w = 0
    else:
        dis_w = 1 - distance/25.0
    
    location_weight = location_cof * dis_w

    "Compute the category similarity"
    r1_c_info = r1["category"]
    r2_c_info = r2["category"]
    
    "First level of category"
    if r1_c_info[0] == r2_c_info[0]:
        urc_flag = 1
    else:
        urc_flag = 0
    
    "Second level of category"
    if r1_c_info[1] == r2_c_info[1]:
        c_flag = 1
    else:
        c_flag = 0
    
    category_weight = category_cof * (urc_flag * 0.4 + c_flag *0.6)
    
    "Compute the similarity of Description"
    r1_des = r1["desc"]
    r2_des = r2["desc"]
    
    com_keys = []
    for k in r1_des.keys():
        com_keys.append(k)
    
    for k in r2_des.keys():
        com_keys.append(k)
    
    com_des = []
    for k in com_keys:
        c_r1 = r1_des.get(k,0)
        c_r2 = r2_des.get(k,0)
        com_des.append([c_r1,c_r2])
    
    des_sim = sum([x[0]*x[1] for x in com_des]) / (sqrt(sum([x[0]**2 for x in com_des])) * sqrt(sum([x[1]**2 for x in com_des])))
    
    des_weight = des_cof * des_sim
    
    similarity = time_weight + location_weight + category_weight + des_weight
    return similarity

def arg_parser():
    ap = argparse.ArgumentParser()
    ap.add_argument('-c',dest="model_cfg",metavar="MODEL CFG",default="./crime.conf",type=str,nargs='?',help='the config file')
    ap.add_argument('-db',dest="db_file",metavar="DATABASE",default="c:/crime/crime-cleaned.db",type=str,nargs='?',help='db file')
    ap.add_argument('-pd',dest="p_dir",metavar="PROCESS OUT DIR",default="c:/crime/data",type=str,nargs='?',help='process out dir')
    ap.add_argument('-cd',dest="c_dir",metavar="COMPARE OUT DIR",default="c:/crime/result",type=str,nargs='?',help='compare out dir')
    return ap.parse_args()
    
def main():
    global CONFIG_FILE
    args = arg_parser()
    CONFIG_FILE = args.model_cfg
    db_file = args.db_file
    conn = lite.connect(db_file)
    out_r = args.p_dir
    com_r = args.c_dir
    
    cur = conn.cursor()
    sql = "select distinct URC_catagorie from t_crime"
    cur.execute(sql)
    rs = cur.fetchall()
    c_list = [r[0] for r in rs]
    
    #create multil thread to process file
    p_threads = []
    for ca in c_list:
        thread = ProcessThread(ca.replace(" ","_"),db_file,out_r,ca)
        thread.start()
        p_threads.append(thread)
    
    for t in p_threads:
        t.join()
    
    #compute the similarity
    c_threads = []
    for ca in c_list:
        o_name = com_r + "/result_" + ca.replace(" ","_") + ".json"
        f_name = out_r + "/" + ca.replace(" ","_") + ".json"
        t_name = "com_" + ca.replace(" ","_")
        thread = CompareThread(t_name,f_name,o_name)
        thread.start()
        c_threads.append(thread)
    
    for t in c_threads:
        t.join()
        
    if conn:
        conn.close()

CONFIG_FILE = ""    
if __name__ == "__main__":
    print "start: ",datetime.strftime(datetime.now(),"%Y-%m-%d %H:%M:%S")
    main()
    print "End: ",datetime.strftime(datetime.now(),"%Y-%m-%d %H:%M:%S")