import statsmodels.tsa.api as tsa
import numpy as np
import sqlite3 as lite
import argparse
from Util import calculator

def arg_parser():
    ap = argparse.ArgumentParser("The auto_correlation model")
    ap.add_argument('-db',dest='db_file',metavar='DATA BASE',type=str,help='The Database path')
    return ap.parse_args()

def initiate_data(conn,start,end,target_indices):
    cur = conn.cursor()
    datas = {}
    for stock_index in target_indices:
        sql = "select name,post_date,change_percent from t_enriched_bloomberg_prices where post_date>='{}' and post_date<='{}' and name='{}' order by post_date asc".format(start,end,stock_index)
        cur.execute(sql)
        datas[stock_index] = {}
        results = cur.fetchall()
        for result in results:
            d_value = {"post_date":result[1],"change_percent":result[2],"name":result[0]}
            datas[stock_index][result[1]] = d_value
    return  datas   

def get_cor_data(datas,t_index,p_index):
    t_datas = datas[t_index]
    p_datas = datas[p_index]
    
    c_t_datas = []
    c_p_datas = []
    
    t_keys = t_datas.keys()
    for t_k in t_keys:
        if t_k in p_datas:
            c_t_datas.append(t_datas[t_k])
            c_p_datas.append(p_datas[t_k])
    
    "Sort the data"
    c_t_datas.sort(key = lambda x:x['post_date'])
    c_p_datas.sort(key = lambda x:x['post_date'])        
    
    return c_t_datas,c_p_datas

def getZscore(conn,cur_date,stock_index,cur_diff,duration):
    cur = conn.cursor()
    scores = []
    sql = "select one_day_change from t_enriched_bloomberg_prices where post_date<? and name = ? order by post_date desc limit ?"
    cur.execute(sql,(cur_date,stock_index,duration))
    rows = cur.fetchall()
    for row in rows:
        scores.append(row[0])
    zscore = calculator.calZscore(scores, cur_diff)
    return zscore

def fit_model(data,order):
    var_model = tsa.VAR(data)
    var_model_fit = var_model.fit(maxlags=order)
    return var_model_fit

def test_phase(start,end,t_index,v_inices,conn):
    "Get the list of day to predict"
    cur = conn.cursor()
    date_list = []
    sql = "select post_date from t_enriched_bloomberg_prices where name='{}' and post_date>='{}' and post_date<='{}' order by post_date asc".format(t_index,start,end)
    cur.execute(sql)
    rs = cur.fetchall()
    for r in rs:
        date_list.append(r[0])
    
def forcast(var_model_fit,p_values,p_date):
    prediction = var_model_fit.forecast(p_values,1)
    return prediction

def main():
    args = arg_parser()
    db_file = args.db_file
    conn = lite.connect(db_file)
    
    target_list = ['MERVAL','MEXBOL','IBOV','CHILE65','COLCAP','CRSMBCT','BVPSBVPS','IGBVL','IBVC','AEX','AS51','CAC','CCMP','DAX','FTSMIB','HSI','IBEX','INDU','NKY','OMX','SMI','SPTSX','SX5E','UKX']
    start = '2003-01-01'
    end = '2012-10-31'
    datas = initiate_data(conn,start,end,target_list)
    c_t_datas,c_p_datas = get_cor_data(datas,"MEXBOL","INDU")
    print c_t_datas[0:10],"\n",c_p_datas[-10:]

if __name__ == "__main__":
    main()


#data=<time* variables>
data=np.array([[1,2,3,20,30],[1,3,4,20,30],[1,4,5,100,200],[5,8,10,10,10]]).T
data = np.array([[1,2,3,4,5,6],[1,2,3,4,5,6]]).T
order = 1
#np.

#Training
var_model = tsa.VAR(data)
var_model_fit = var_model.fit(maxlags=order)
#var_model = var_model_fit.model


#Training results
intercept =var_model_fit.intercept
params=var_model_fit.params

#print intercept
#print params

#Prediction
#print var_model.y[-2:]
print var_model.y
out_of_sample_prediction=var_model_fit.forecast([[7,7]],3)
#out_of_sample_prediction = var_model.predict(params, 5,8)
print out_of_sample_prediction

