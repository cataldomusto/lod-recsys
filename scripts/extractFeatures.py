#!/usr/bin/python
import time
import subprocess
import sys
import os
import shutil
from time import gmtime, strftime, localtime
from datetime import datetime

allFeatures=["Features10","Features30","Features50"]

for feat in allFeatures:
    cmd ="cat datasets/books-8k/results/UserItemExpDBPedia/results/ComparisonFeaturesFriedman | grep 'The best algorithm is  "+feat+"'| wc -l"
    subprocess.call(cmd, shell=True)
