#!/usr/bin/python
import time
import subprocess
import sys
import os
import shutil
from time import gmtime, strftime, localtime
from datetime import datetime

##   CreateCSV to execute statistical test
def createCSV(topN, metrics, allalg, allalgWEKA):
    givenN=["given_5","given_20","given_all"]
    valMetrics=["5","10","20"]
    allAlg=""
    if os.path.exists("./datasets/ml-100k/results/UserItemExpDBPedia/CSV/comparisonAlg"):
        shutil.rmtree("./datasets/ml-100k/results/UserItemExpDBPedia/CSV/comparisonAlg")
    if os.path.exists("./datasets/ml-100k/results/UserItemExpDBPedia/CSV/comparisonFeatures"):
        shutil.rmtree("./datasets/ml-100k/results/UserItemExpDBPedia/CSV/comparisonFeatures")
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
    
    DIR = './datasets/ml-100k/results/UserItemExpDBPedia/CSV/comparisonAlg'
    print "CSV created for comparison Algorithms: " + str(len([name for name in os.listdir(DIR) if os.path.isfile(os.path.join(DIR, name))])) 
    
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

    DIR='./datasets/ml-100k/results/UserItemExpDBPedia/CSV/comparisonFeatures'
    print "CSV created for comparison Features: " + str(len([name for name in os.listdir(DIR) if os.path.isfile(os.path.join(DIR, name))]))

##   CreateCSV to execute statistical test
def createCSVOLD(metrics, allalg, allalgWEKA):
    for metric in metrics: 
        for alg in allalg:
            if (alg!="CFSubsetEval"):
                cmd = "java -cp lodrecsys.jar di.uniba.it.lodrecsys.utils.MakerCSV "+alg+" "+metric + " all"
                print cmd
                subprocess.call(cmd, shell=True)
                dire="./datasets/ml-100k/results/UserItemExpDBPedia/CSV/"+metric+"/"+alg+"/"
                cmd = "Rscript scriptRtest "+dire+" "+dire+"resultTest"
                subprocess.call(cmd, shell=True)
        
        for alg in allalgWEKA:
            cmd = "java -cp lodrecsys.jar di.uniba.it.lodrecsys.utils.MakerCSV RankerWeka"+alg+" "+metric+ " all"
            print cmd
            subprocess.call(cmd, shell=True)
            dire="./datasets/ml-100k/results/UserItemExpDBPedia/CSV/"+metric+"/RankerWeka"+alg+"/"
            cmd = "Rscript scriptRtest "+dire+" "+dire+"resultTest"
            subprocess.call(cmd, shell=True)
