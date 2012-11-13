import statsmodels.tsa.api as tsa
import numpy as np
import sqlite3 as lite
import argparse
from Util import calculator

def arg_parser():
    ap = argparse.ArgumentParser("The auto_correlation model")
    ap.add_argument('-db',dest='db_file',metavar='DATA BASE',type=str,help='The Database path')
    ap.add_argument('-o',dest='order',metavar='ORDER',type=int,help='The order of VAR')
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
    v_indices = []
    p_datas = datas[p_index]
    
    c_t_datas = []
    c_p_datas = []
    
    t_keys = t_datas.keys()
    for t_k in t_keys:
        if t_k in p_datas:
            c_t_datas.append(t_datas[t_k])
            c_p_datas.append(p_datas[t_k])
    
    common_days = []
    "Get the common dayList"
    for t_k in t_keys:
        flag = True
        for v_i in v_indices:
            d = datas[v_i]
            if t_k not in d:
                flag = False
                break
        if flag:
            common_days.append(t_k)
    
    "get the data array"
    for day in common_days:
        c_t_datas.append(t_datas[day])
        
    c_t_datas.sort(key = lambda x:x['post_date'])
    
    for v_i in v_indices:
        d = datas[v_i]
        d_v = []
        for day in common_days:
            d_v.append(d[day])
        
        d_v.sort(key = lambda x:x['post_date'])
        c_p_datas.append(d_v)
            
        
                
    
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

def test_phase(start,end,t_index,v_inices,conn,order,var_model_fit):
    "Get the list of day to predict"
    cur = conn.cursor()
    date_list = []
    sql = "select post_date from t_enriched_bloomberg_prices where name='{}' and post_date>='{}' and post_date<='{}' order by post_date asc".format(t_index,start,end)
    cur.execute(sql)
    rs = cur.fetchall()
    for r in rs:
        date_list.append(r[0])
    
    "forcast the stock index day by day"
    for p_date in date_list:
        "initiate the data matrix"
        data_matrix = []
        "get the past target value"
        sql = "select change_percent from t_enriched_bloomberg_prices where name=? and post_date<? order by post_date desc limit ?"
        
        cur.execute(sql,(t_index,p_date,order,))
        rs = cur.fetchall()
        t_value = [r[0] for r in rs]
        t_value.reverse()
        data_matrix.append(t_value)
        
        "get the indicator values"
        for v_index in v_inices:
            cur.execute(sql,(v_index,p_date,order,))
            rs = cur.fetchall()
            v_value = [r[0] for r in rs]
            v_value.reverse()
            data_matrix.append(v_value)
            
        "construct the matrix"
        data_matrix = np.array(data_matrix).T
        prediction = forcast(var_model_fit,data_matrix,1)
        
        p_1 = prediction[0]
        "compute the one_day_change"
        sql = "select current_value from t_enriched_bloomberg_prices where name=? and post_date<? order by post_date desc limit 1"
        cur.execute(sql,(t_index,p_date,))
        r = cur.fetchone()
        last_price = r[0]
        p_1_change = last_price * p_1
        p_price = last_price * (1 + p_1)
        
        "compute the predicting zscore"
        zscore30 = getZscore(conn,p_date,t_index,p_1_change,30)
        zscore90 = getZscore(conn,p_date,t_index,p_1_change,90)
        event_type = "0000"
        
        if zscore30 >= 4 or zscore90 >= 3:
            event_type = "0411"
        elif zscore30 <= -4 or zscore90 <= -3:
            event_type = "0412"
        
        
        "Insert into the prediction model"
        sql = "insert into t_ar_prediction (post_date,stock_index,zscore30,zscore90,change_percent,price,event_type) values (?,?,?,?,?,?,?)"
        cur.execute(sql,(p_date,t_index,zscore30,zscore90,p_1,p_price,event_type,))
        print p_date
    conn.commit()
        
    
def forcast(var_model_fit,p_values,day):
    prediction = var_model_fit.forecast(p_values,day)
    return prediction[:][0]

def clear(conn):
    sql = "delete from t_ar_prediction"
    conn.cursor().execute(sql)
    conn.commit()
    
def main():
    args = arg_parser()
    db_file = args.db_file
    order = args.order
    conn = lite.connect(db_file)
    
    "clear the prediction"
    clear(conn)
    
    target_list = ['MERVAL','MEXBOL','IBOV','CHILE65','COLCAP','CRSMBCT','BVPSBVPS','IGBVL','IBVC','AEX','AS51','CAC','CCMP','DAX','FTSMIB','HSI','IBEX','INDU','NKY','OMX','SMI','SPTSX','SX5E','UKX']
    start = '2003-01-01'
    end = '2010-12-31'
    datas = initiate_data(conn,start,end,target_list)
    t_index = "MEXBOL"
    v_inices = ["INDU"]
    c_t_datas,c_p_datas = get_cor_data(datas,t_index,v_inices)
    
    w_d = []
    w_d.append(c_t_datas)
    for da in c_p_datas:
        w_d.append.append(da)
    
    
    "start to fit the model"
    data_matrix = np.array([[i['change_percent'] for i in c_t_datas],[i['change_percent'] for i in c_p_datas]]).T
    var_model_fit = fit_model(data_matrix,order)
    
    "Move to Test stage"
    
    t_start = "2011-01-01"
    t_end = "2012-10-31"
    test_phase(t_start,t_end,t_index,v_inices,conn,order,var_model_fit)
    
    if conn:
        conn.close()
    

if __name__ == "__main__":
    main()

