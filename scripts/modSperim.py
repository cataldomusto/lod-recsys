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

def sperimRunner(dataset, allalg, allalgWEKA, topN, givenN, param, cmdThreadFS, cmdThreadRec, cmdThreadEval, metrics, best, topBest, givenCSV, valMetricsCSV, cmdThreadBaseline, algBaseline):
    
    cmdExecFS=[]
    cmdExecLOGFS=[]

    cmdExecREC=[]
    cmdExecLOGREC=[]

    cmdExecEV=[]
    cmdExecLOGEV=[]

    cmdExecBase=[]
    cmdExecLOGBase=[]

    extractVal=[]
    
    init.init(dataset, topN, givenN, allalgWEKA, allalg, extractVal, cmdExecFS, cmdExecLOGFS, cmdExecREC, cmdExecLOGREC, cmdExecEV, cmdExecLOGEV, metrics, cmdExecBase, cmdExecLOGBase, algBaseline)

#    Baseline Process
#    parallelProcess.parallelProcess(cmdExecBase, cmdExecLOGBase, cmdThreadBaseline, param, "Baseline process")
    
#    feature process
#    parallelProcess.parallelProcess(cmdExecFS, cmdExecLOGFS, cmdThreadFS, param, "Feature process")

#    recommendation Process
#    parallelProcess.parallelProcess(cmdExecREC, cmdExecLOGREC, cmdThreadRec, param, "Recommendation process")
    
#    evaluation Process
#    parallelProcess.parallelProcess(cmdExecEV, cmdExecLOGEV, cmdThreadEval, param, "Evaluation process")

#    creatorSummaries.createSummaries(dataset, extractVal, metrics)
#    creatorSummaries.createSummariesBaseline(dataset, metrics)
    
#    creatorCSV.createCSVcomparisonAlg(dataset, topN, metrics, allalg, allalgWEKA, givenCSV, valMetricsCSV)
#    creatorCSV.createCSVcomparisonFeatures(dataset, topN, metrics, allalg, allalgWEKA, givenCSV, valMetricsCSV)
#    creatorCSV.createCSVcomparisonBestBaseline(dataset, metrics, best, topBest, givenCSV, valMetricsCSV)

#    StatisticalTest.FriedmanTestcomparisonAlg(dataset, allalg, allalgWEKA, metrics)
#    StatisticalTest.FriedmanTestcomparisonFeatures(dataset, topN, metrics)
    
#    StatisticalTest.Comparison2Alg(dataset, allalg)

    print time.strftime("%Y-%m-%d %H:%M") + " Finished."

