#!/bin/sh

# Run at 9:30 each day to capture the current day news from bloomberg
# To run the script, we need to indicate a config file firstly
# Then we need to configure the following parameters if need change:
# DAILY_NEWS_DIR=../Config/Data/DailyNews
# COMPANY_LIST = ../Config/Data/Company List
# ZMQ_PORT = tcp://*:30115
# NEWS_ALREADY_DOWNLOADED = ../Config/Data/BloombergNewsDownloaded.json

# After running the script, the collected news will be stored in the $DAILY_NEWS_DIR

CONFIG_FILE='../Config/config.cfg'

python ./BloombergNewsScrape.py -c ${CONFIG_FILE}

