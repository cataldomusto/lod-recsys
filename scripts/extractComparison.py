#!/usr/bin/python
import time
import subprocess
import sys
import os
import shutil
from time import gmtime, strftime, localtime
from datetime import datetime

allalg=["CFSubsetEval","PageRank","MRMR"]
allalgWEKA=["ChiSquaredAttributeEval","GainRatioAttributeEval","SVMAttributeEval", "InfoGainAttributeEval","PCA","ReliefFAttributeEval"]

for algs in allalg:
    cmd ="cat datasets/ml-100k/results/UserItemExpDBPedia/results/ComparisonAlgFriedman | grep 'Max mean algorithm is  "+algs+"'| wc -l"
    subprocess.call(cmd, shell=True)
for algs in allalgWEKA:
    cmd ="cat datasets/ml-100k/results/UserItemExpDBPedia/results/ComparisonAlgFriedman | grep 'Max mean algorithm is  RankerWeka"+algs+"'| wc -l"
    subprocess.call(cmd, shell=True)
