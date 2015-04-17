#!/usr/bin/python
import time
import subprocess
import sys
import os
import shutil
from time import gmtime, strftime, localtime
from datetime import datetime

def Comparison2Alg(dataset, allalg):
    for db in dataset:
        if (db == "movielens"):
            direHome = "./datasets/ml-100k/results/UserItemExpDBPedia/"
        else:
            direHome = "./datasets/books-8k/results/UserItemExpDBPedia/"
        algs=""
        for alg in allalg:
            algs += alg + " "
        cmd = "java -cp lodrecsys.jar di.uniba.it.lodrecsys.utils.MakerStatisticalTest comparison2Alg "+db +" "+ algs
        print cmd
        subprocess.call(cmd, shell=True)    
        dire = direHome + "CSV/comparisonAlg/"
        if not(os.path.exists(direHome + "results")):
            os.makedirs(direHome + "results")
        cmd = "Rscript ./scripts/Rcomparison2Alg "+dire+" "+direHome+"results/comparison2Alg"
        print cmd
        subprocess.call(cmd, shell=True)
        print time.strftime("%Y-%m-%d %H:%M") + " Comparison Algs finished."

def ComparisonBestBaseline(dataset, metrics, best, topBest, nsplit):
    for db in dataset:
        if (db == "movielens"):
            direHome = "./datasets/ml-100k/results/UserItemExpDBPedia/"
        else:
            direHome = "./datasets/books-8k/results/UserItemExpDBPedia/"
        cmd = "java -cp lodrecsys.jar di.uniba.it.lodrecsys.utils.MakerStatisticalTest comparisonBestBaseline "+ best + " " + topBest
        subprocess.call(cmd, shell=True)    
        dire = direHome + "CSV/comparisonBestBaseline/"
        if not(os.path.exists(direHome + "results")):
            os.makedirs(direHome + "results")
        cmd = "Rscript ./scripts/RcomparisonBestBaseline "+dire+" "+direHome+"results/ComparisonBestBaseline"
        subprocess.call(cmd, shell=True)
        print time.strftime("%Y-%m-%d %H:%M") + " Comparison Best - Baseline finished."


def FriedmanTestcomparisonFeatures(dataset, topN, metrics):
    for db in dataset:
        if (db == "movielens"):
            direHome = "./datasets/ml-100k/results/UserItemExpDBPedia/"
        else:
            direHome = "./datasets/books-8k/results/UserItemExpDBPedia/"    
        tops=""
        for top in topN:
            tops+=top+" "
        cmd = "java -cp lodrecsys.jar di.uniba.it.lodrecsys.utils.MakerStatisticalTest comparisonFeatures "+tops
        subprocess.call(cmd, shell=True)    
        dire= direHome + "CSV/comparisonFeatures/"
        if not(os.path.exists(direHome + "results")):
            os.makedirs(direHome + "results")
        cmd = "Rscript ./scripts/RcomparisonFeaturesFriedman "+dire+" "+direHome+"results/ComparisonFeaturesFriedman"
        subprocess.call(cmd, shell=True)
        print time.strftime("%Y-%m-%d %H:%M") + " Comparison features finished."

def FriedmanTestcomparisonAlg(dataset, allalg, allalgWEKA, metrics):
    for db in dataset:
        if (db == "movielens"):
            direHome = "./datasets/ml-100k/results/UserItemExpDBPedia/"
        else:
            direHome = "./datasets/books-8k/results/UserItemExpDBPedia/"
        allAlg=""
        for algs in allalg:
            allAlg+=algs+" "
        for algs in allalgWEKA:
            allAlg+="RankerWeka"+algs+" "
        
        cmd = "java -cp lodrecsys.jar di.uniba.it.lodrecsys.utils.MakerStatisticalTest comparisonAlg "+allAlg
        subprocess.call(cmd, shell=True)    
        if not(os.path.exists(direHome+ "results")):
            os.makedirs(direHome+ "results")
        dire= direHome+ "CSV/comparisonAlg/"
        cmd = "Rscript ./scripts/RcomparisonAlgFriedman "+dire+" "+direHome+"results/ComparisonAlgFriedman"
        subprocess.call(cmd, shell=True)
        print time.strftime("%Y-%m-%d %H:%M") + " Comparison Algorithms finished."
