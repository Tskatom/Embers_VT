from Util import common

def process():
    termConFile = common.get_configuration("model", "TERM_CONTRIBUTION_PATH")
    clustConFile = common.get_configuration("model", "CLUSTER_CONTRIBUTION_PATH")
    clustProFile = common.get_configuration("model", "CLUSTER_PROBABILITY_PATH")
    keyWordsFile = common.get_configuration("training", "VOCABULARY_FILE")
    trendFile = common.get_configuration("training", "TREND_RANGE_FILE")
    