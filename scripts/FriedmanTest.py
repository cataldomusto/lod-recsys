#!/usr/bin/python
import time
import subprocess
import sys
import os
import shutil
from time import gmtime, strftime, localtime
from datetime import datetime

def FriedmanTestcomparisonFeatures(topN, metrics):
    tops=""
    for top in topN:
        tops+=top+" "
    cmd = "java -cp lodrecsys.jar di.uniba.it.lodrecsys.utils.MakerFriedmanTest comparisonFeatures "+tops
    subprocess.call(cmd, shell=True)    
    dire="~/Scrivania/recThesisCopyD/datasets/ml-100k/results/UserItemExpDBPedia/CSV/comparisonFeatures/"
    cmd = "Rscript ./scripts/RcomparisonFeaturesFriedman "+dire+" ./scripts/resultcomparisonFeaturesFriedman"
    subprocess.call(cmd, shell=True)

def FriedmanTestcomparisonAlg(allalg, allalgWEKA, metrics):
    allAlg=""
    for algs in allalg:
        allAlg+=algs+" "
    for algs in allalgWEKA:
        allAlg+="RankerWeka"+algs+" "
    
    cmd = "java -cp lodrecsys.jar di.uniba.it.lodrecsys.utils.MakerFriedmanTest comparisonAlg "+allAlg
    subprocess.call(cmd, shell=True)    
    
    dire="~/Scrivania/recThesisCopyD/datasets/ml-100k/results/UserItemExpDBPedia/CSV/comparisonAlg/"
    cmd = "Rscript ./scripts/RcomparisonAlgFriedman "+dire+" ./scripts/resultcomparisonAlgFriedman"
    subprocess.call(cmd, shell=True)
