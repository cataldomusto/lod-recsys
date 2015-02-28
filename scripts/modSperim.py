#!/usr/bin/python
import time
import subprocess
import sys
import os
import shutil
from time import gmtime, strftime, localtime
from datetime import datetime

import init
import parallelProcess
import creatorSummaries
import creatorCSV
import StatisticalTest

def sperim(allalg, allalgWEKA, topN, givenN, param,cmdThreadFS, cmdThreadRec,cmdThreadEval, metrics, best, topBest, dire, givenCSV, valMetricsCSV):
    
    cmdExecFS=[]
    cmdExecLOGFS=[]

    cmdExecREC=[]
    cmdExecLOGREC=[]

    cmdExecEV=[]
    cmdExecLOGEV=[]

    extractVal=[]
    
    init.init(topN, givenN, allalgWEKA, allalg, extractVal, cmdExecFS, cmdExecLOGFS, cmdExecREC, cmdExecLOGREC, cmdExecEV, cmdExecLOGEV, metrics)
    
#    feature process
    parallelProcess.parallelProcess(cmdExecFS, cmdExecLOGFS, cmdThreadFS, param, "Feature process")

#    recommendation Process
    parallelProcess.parallelProcess(cmdExecREC, cmdExecLOGREC, cmdThreadRec, param, "Recommendation process")
    
#    evaluation Process
    parallelProcess.parallelProcess(cmdExecEV, cmdExecLOGEV, cmdThreadEval, param, "Evaluation process")

    creatorSummaries.createSummaries(extractVal, metrics, dire)
    
#    creatorCSV.createCSVcomparisonAlg(topN, metrics, allalg, allalgWEKA, dire, givenCSV, valMetricsCSV)
#    creatorCSV.createCSVcomparisonFeatures(topN, metrics, allalg, allalgWEKA, dire, givenCSV, valMetricsCSV)
#    creatorCSV.createCSVcomparisonBestBaseline(metrics, best, topBest, dire, givenCSV, valMetricsCSV)

#    StatisticalTest.FriedmanTestcomparisonAlg(allalg, allalgWEKA, metrics, dire)
#    StatisticalTest.FriedmanTestcomparisonFeatures(topN, metrics, dire)
    
#    StatisticalTest.ComparisonBestBaseline(metrics, best, topBest, dire)

    print time.strftime("%Y-%m-%d %H:%M") + " Finished."

