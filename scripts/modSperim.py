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
import anovaTest

def sperim(allalg, allalgWEKA, topN, givenN, param,cmdThreadFS, cmdThreadRec,cmdThreadEval, metrics):
    
    cmdExecFS=[]
    cmdExecLOGFS=[]

    cmdExecREC=[]
    cmdExecLOGREC=[]

    cmdExecEV=[]
    cmdExecLOGEV=[]

    extractVal=[]
    
    init.init(topN, givenN, allalgWEKA, allalg, extractVal, cmdExecFS, cmdExecLOGFS, cmdExecREC, cmdExecLOGREC, cmdExecEV, cmdExecLOGEV, metrics)
    
#    feature process
#    parallelProcess.parallelProcess(cmdExecFS, cmdExecLOGFS, cmdThreadFS, param, "Feature process")

#    recommendation Process
#    parallelProcess.parallelProcess(cmdExecREC, cmdExecLOGREC, cmdThreadRec, param, "Recommendation process")
    
#    evaluation Process
    parallelProcess.parallelProcess(cmdExecEV, cmdExecLOGEV, cmdThreadEval, param, "Evaluation process")

    creatorSummaries.createSummaries(extractVal, metrics)
    
    creatorCSV.createCSV(topN, metrics, allalg, allalgWEKA)

#    anovaTest.anovaTestcomparisonAlg(allalg, allalgWEKA, metrics)
#    anovaTest.anovaTestcomparisonFeatures(topN, metrics)

    print time.strftime("%Y-%m-%d %H:%M") + " Finished."

