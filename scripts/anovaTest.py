#!/usr/bin/python
import time
import subprocess
import sys
import os
import shutil
from time import gmtime, strftime, localtime
from datetime import datetime

def anovaTestcomparisonFeatures(topN, metrics):
    tops=""
    for top in topN:
        tops+=top+" "
    cmd = "java -cp lodrecsys.jar di.uniba.it.lodrecsys.utils.MakerANOVATest comparisonFeatures "+tops
    subprocess.call(cmd, shell=True)    
    dire="~/Scrivania/recThesisCopyD/datasets/ml-100k/results/UserItemExpDBPedia/CSV/comparisonFeatures/"
    cmd = "Rscript ./scripts/RcomparisonFeaturesANOVA "+dire+" ./scripts/resultcomparisonFeaturesANOVA"
    subprocess.call(cmd, shell=True)

def anovaTestcomparisonAlg(allalg, allalgWEKA, metrics):
    allAlg=""
    for algs in allalg:
        allAlg+=algs+" "
    for algs in allalgWEKA:
        allAlg+="RankerWeka"+algs+" "
    
    cmd = "java -cp lodrecsys.jar di.uniba.it.lodrecsys.utils.MakerANOVATest comparisonAlg "+allAlg
    subprocess.call(cmd, shell=True)    
    
    dire="~/Scrivania/recThesisCopyD/datasets/ml-100k/results/UserItemExpDBPedia/CSV/comparisonAlg/"
    cmd = "Rscript ./scripts/RcomparisonAlgANOVA "+dire+" ./scripts/resultcomparisonAlgANOVA"
    subprocess.call(cmd, shell=True)
