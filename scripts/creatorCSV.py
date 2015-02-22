#!/usr/bin/python
import time
import subprocess
import sys
import os
import shutil
from time import gmtime, strftime, localtime
from datetime import datetime
##   CreateCSV to execute statistical test

def createCSVcomparisonBestBaseline(metrics, best, topBest, dire):
    givenN=["given_5","given_20","given_all"]
    valMetrics=["5","10","20"]
    if os.path.exists( dire + "CSV/comparisonBestBaseline"):
        shutil.rmtree( dire + "CSV/comparisonBestBaseline")
    for metric in metrics: 
        for valMetric in valMetrics:
            for given in givenN:
                cmd = "java -cp lodrecsys.jar di.uniba.it.lodrecsys.utils.MakerCSV comparisonBestBaseline "+topBest+" "+given+" "+valMetric+" "+metric+" "+ best
                subprocess.call(cmd, shell=True)
    
    DIR = dire + 'CSV/comparisonBestBaseline'
    print "CSV created for comparison Best Algorithms - Baseline: " + str(len([name for name in os.listdir(DIR) if os.path.isfile(os.path.join(DIR, name))])) 

def createCSVcomparisonAlg(topN, metrics, allalg, allalgWEKA, dire):
    givenN=["given_5","given_20","given_all"]
    valMetrics=["5","10","20"]
    allAlg=""
    if os.path.exists( dire + "CSV/comparisonAlg"):
        shutil.rmtree( dire + "CSV/comparisonAlg")
    for algs in allalg:
        allAlg+=algs+" "
    for algs in allalgWEKA:
        allAlg+="RankerWeka"+algs+" "
    for metric in metrics: 
        for top in topN:
            for valMetric in valMetrics:
                for given in givenN:
                    cmd = "java -cp lodrecsys.jar di.uniba.it.lodrecsys.utils.MakerCSV comparisonAlg "+top+" "+given+" "+valMetric+" "+metric+" "+ allAlg
                    subprocess.call(cmd, shell=True)
    
    DIR = dire + 'CSV/comparisonAlg'
    print "CSV created for comparison Algorithms: " + str(len([name for name in os.listdir(DIR) if os.path.isfile(os.path.join(DIR, name))])) 
    
def createCSVcomparisonFeatures(topN, metrics, allalg, allalgWEKA, dire ):
    givenN=["given_5","given_20","given_all"]
    valMetrics=["5","10","20"]
    allAlg=""
    if os.path.exists(dire + "CSV/comparisonFeatures"):
        shutil.rmtree(dire + "CSV/comparisonFeatures")
    
    tops=""
    for top in topN:
        tops+=top+" "
    for metric in metrics: 
        for valMetric in valMetrics:
            for given in givenN:
                for alg in allalg:
                    cmd = "java -cp lodrecsys.jar di.uniba.it.lodrecsys.utils.MakerCSV comparisonFeatures "+alg+" "+given+" "+valMetric+" "+metric+" "+ tops
                    subprocess.call(cmd, shell=True)

                for alg in allalgWEKA:
                    cmd = "java -cp lodrecsys.jar di.uniba.it.lodrecsys.utils.MakerCSV comparisonFeatures RankerWeka"+alg+" "+given+" "+valMetric+" "+metric+" "+ tops
                    subprocess.call(cmd, shell=True)

    DIR= dire + 'CSV/comparisonFeatures'
    print "CSV created for comparison Features: " + str(len([name for name in os.listdir(DIR) if os.path.isfile(os.path.join(DIR, name))]))

##   CreateCSV to execute statistical test
def createCSVOLD(metrics, allalg, allalgWEKA, direHome):
    for metric in metrics: 
        for alg in allalg:
            if (alg!="CFSubsetEval"):
                cmd = "java -cp lodrecsys.jar di.uniba.it.lodrecsys.utils.MakerCSV "+alg+" "+metric + " all"
                print cmd
                subprocess.call(cmd, shell=True)
                dire= direHome + "CSV/"+metric+"/"+alg+"/"
                cmd = "Rscript scriptRtest "+dire+" "+dire+"resultTest"
                subprocess.call(cmd, shell=True)
        
        for alg in allalgWEKA:
            cmd = "java -cp lodrecsys.jar di.uniba.it.lodrecsys.utils.MakerCSV RankerWeka"+alg+" "+metric+ " all"
            print cmd
            subprocess.call(cmd, shell=True)
            dire= direHome + "CSV/"+metric+"/RankerWeka"+alg+"/"
            cmd = "Rscript scriptRtest "+dire+" "+dire+"resultTest"
            subprocess.call(cmd, shell=True)
