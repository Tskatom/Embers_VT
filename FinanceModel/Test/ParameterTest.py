import sys
from datetime import datetime,timedelta

for arg in sys.argv:
    print arg

print sys.argv[1]

startDay = "2012-01-01"
endDay = "2012-08-24"

sd = datetime.strptime(startDay,"%Y-%m-%d")
ed = datetime.strptime(endDay,"%Y-%m-%d")

while sd <= ed:
    predictiveDay = datetime.strftime(sd,"%Y-%m-%d")
    print predictiveDay
    sd = sd + timedelta(days=1)