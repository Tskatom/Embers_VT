import json
from math import asin,sin,cos,sqrt,radians,ceil
import numpy as np
from datetime import datetime

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

def compute_similarity(r_file,o_name):
    c_records = json.load(open(r_file))
    length = len(c_records)
    "Write the result to file"
    f_w = open(o_name,"w")
    
    for i in range(0,1):
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

def main():
    global CONFIG_FILE
    CONFIG_FILE = "c:/crime/crime.conf"
    r_file = "c:/crime/data/ROBBERY.json"
    o_file = "c:/test.out"
    compute_similarity(r_file,o_file)

CONFIG_FILE = ""     
if __name__ == "__main__":
    print "start: ",datetime.strftime(datetime.now(),"%Y-%m-%d %H:%M:%S")
    main()
    print "End: ",datetime.strftime(datetime.now(),"%Y-%m-%d %H:%M:%S")