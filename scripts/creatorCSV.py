#!/usr/bin/python
import time
import subprocess
import sys
import os
import shutil
from time import gmtime, strftime, localtime
from datetime import datetime
##   CreateCSV to execute statistical test

def createCSVcomparisonBestBaseline(dataset, metrics, best, topBest, givenN, valMetrics):
    for db in dataset:
        if (db == "movielens"):
            nsplit ="5"
            dire = "./datasets/ml-100k/results/UserItemExpDBPedia/"
        else:
            nsplit ="1"
            dire = "./datasets/books-8k/results/UserItemExpDBPedia/"
        if os.path.exists( dire + "CSV/comparisonBestBaseline"):
            shutil.rmtree( dire + "CSV/comparisonBestBaseline")
        for metric in metrics: 
            for valMetric in valMetrics:
                for given in givenN:
                    cmd = "java -cp lodrecsys.jar di.uniba.it.lodrecsys.utils.MakerCSV comparisonBestBaseline "+nsplit+" "+topBest+" "+given+" "+valMetric+" "+metric+" "+ best
                    subprocess.call(cmd, shell=True)
        
        DIR = dire + 'CSV/comparisonBestBaseline'
        print "CSV created for comparison Best Algorithms - Baseline: " + str(len([name for name in os.listdir(DIR) if os.path.isfile(os.path.join(DIR, name))])) 

def createCSVcomparisonAlg(dataset, topN, metrics, allalg, allalgWEKA, givenN, valMetrics):
    for db in dataset:
        if (db == "movielens"):
            nsplit ="5"
            dire = "./datasets/ml-100k/results/UserItemExpDBPedia/"
        else:
            dire = "./datasets/books-8k/results/UserItemExpDBPedia/"
            nsplit ="1"
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
                        cmd = "java -cp lodrecsys.jar di.uniba.it.lodrecsys.utils.MakerCSV comparisonAlg "+db+" "+nsplit+" "+top+" "+given+" "+valMetric+" "+metric+" "+ allAlg
                        print cmd
                        subprocess.call(cmd, shell=True)
        
        DIR = dire + 'CSV/comparisonAlg'
        print "CSV created for comparison Algorithms: " + str(len([name for name in os.listdir(DIR) if os.path.isfile(os.path.join(DIR, name))])) 
    
def createCSVcomparisonFeatures(dataset, topN, metrics, allalg, allalgWEKA, givenN, valMetrics):
    for db in dataset:
        if (db == "movielens"):
            dire = "./datasets/ml-100k/results/UserItemExpDBPedia/"
            nsplit ="5"
        else:
            dire = "./datasets/books-8k/results/UserItemExpDBPedia/"
            nsplit ="1"
        if os.path.exists(dire + "CSV/comparisonFeatures"):
            shutil.rmtree(dire + "CSV/comparisonFeatures")
        if "CFSubsetEval" in allalg:
            allalg.remove("CFSubsetEval")
        if "Custom" in allalg:
            allalg.remove("Custom")
        tops=""
        for top in topN:
            tops+=top+" "
        for metric in metrics: 
            for valMetric in valMetrics:
                for given in givenN:
                    for alg in allalg:
                        cmd = "java -cp lodrecsys.jar di.uniba.it.lodrecsys.utils.MakerCSV comparisonFeatures "+nsplit+" "+alg+" "+given+" "+valMetric+" "+metric+" "+ tops
                        subprocess.call(cmd, shell=True)

                    for alg in allalgWEKA:
                        cmd = "java -cp lodrecsys.jar di.uniba.it.lodrecsys.utils.MakerCSV comparisonFeatures "+nsplit+" RankerWeka"+alg+" "+given+" "+valMetric+" "+metric+" "+ tops
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
