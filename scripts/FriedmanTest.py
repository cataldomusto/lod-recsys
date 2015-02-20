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
    dire="./datasets/ml-100k/results/UserItemExpDBPedia/CSV/comparisonFeatures/"
    cmd = "Rscript ./scripts/RcomparisonFeaturesFriedman "+dire+" ./datasets/ml-100k/results/UserItemExpDBPedia/results/ComparisonFeaturesFriedman"
    subprocess.call(cmd, shell=True)
    print time.strftime("%Y-%m-%d %H:%M") + " Comparison features finished."

def FriedmanTestcomparisonAlg(allalg, allalgWEKA, metrics):
    allAlg=""
    for algs in allalg:
        allAlg+=algs+" "
    for algs in allalgWEKA:
        allAlg+="RankerWeka"+algs+" "
    
    cmd = "java -cp lodrecsys.jar di.uniba.it.lodrecsys.utils.MakerFriedmanTest comparisonAlg "+allAlg
    subprocess.call(cmd, shell=True)    
    
    dire="./datasets/ml-100k/results/UserItemExpDBPedia/CSV/comparisonAlg/"
    cmd = "Rscript ./scripts/RcomparisonAlgFriedman "+dire+" ././datasets/ml-100k/results/UserItemExpDBPedia/results/ComparisonAlgFriedman"
    subprocess.call(cmd, shell=True)
    print time.strftime("%Y-%m-%d %H:%M") + " Comparison Algorithms finished."
