#!/usr/bin/python
import time
import subprocess
import sys
import os
import shutil
from time import gmtime, strftime, localtime
from datetime import datetime

def init(dataset, topN, givenN, allalgWEKA, allalg, extractVal, cmdExecFS, cmdExecLOGFS, cmdExecREC, cmdExecLOGREC, cmdExecEV, cmdExecLOGEV, metrics, cmdExecBase, cmdExecLOGBase, algBaseline):
    for db in dataset:
        print db
        if (db == "movielens"):
            split = "5"
        else:
            split = "1"
        for alg in algBaseline:
            for given in givenN:
                metricString=""
                for metric in metrics:
                    metricString +=metric + " "
        #       cmd = "java -cp lodrecsys.jar di.uniba.it.lodrecsys.baseline.Baseline movielens given_5 UserKNN novelty diversity &"
                cmd = "java -cp lodrecsys.jar di.uniba.it.lodrecsys.baseline.Baseline "+db+" "+given+" "+alg+" "+metricString +" &"
                cmdLOG = "java -cp Baseline "+db+" "+given+" "+alg+" "+metricString +" &"
                cmdExecBase.append(cmd)
                cmdExecLOGBase.append(cmdLOG)

        for alg in allalgWEKA:
            for top in topN:
        #       cmd = "java -cp lodrecsys.jar di.uniba.it.lodrecsys.graph.GraphFSRun movielens RankerWeka 11 LatentSemanticAnalysis"
                cmd = "java -cp lodrecsys.jar di.uniba.it.lodrecsys.graph.GraphFSRun "+db+" RankerWeka "+top+" "+ alg
                cmdLOG = "java -cp GraphFSRun RankerWeka "+db+" "+top+" "+ alg
                cmdExecFS.append(cmd)
                cmdExecLOGFS.append(cmdLOG)
                extractVal.append("RankerWeka"+alg+top+"prop"+split+"split")
        #        print time.strftime("%Y-%m-%d %H:%M") + " "+cmdLOG +"\n"

                for given in givenN:
        #               cmd = "java -cp lodrecsys.jar di.uniba.it.lodrecsys.graph.GraphRecRun movielens given_5 RankerWeka 11 PCA &"
                    cmd = "java -cp lodrecsys.jar di.uniba.it.lodrecsys.graph.GraphRecRun "+db+" "+given+" RankerWeka "+top+" "+alg+" &"
                    cmdLOG = "java -cp GraphRecRun "+db+" "+given+" RankerWeka "+top+" "+alg+" &"
                    cmdExecREC.append(cmd)
                    cmdExecLOGREC.append(cmdLOG)

                for given in givenN:
                    metricString=""
                    for metric in metrics:
                        metricString +=metric + " "
                    cmd = "java -cp lodrecsys.jar di.uniba.it.lodrecsys.graph.GraphEvalRun "+db+" "+given+" RankerWeka "+top+" "+alg+"  "+metricString+" &"
                    cmdLOG = "java -cp GraphEvalRun "+db+" "+given+" RankerWeka "+top+" "+alg+" "+metricString+" &"
                    cmdExecEV.append(cmd)
                    cmdExecLOGEV.append(cmdLOG)

        for alg in allalg:
            if (alg=="CFSubsetEval" or "Custom" in alg):
        #       cmd = "java -cp lodrecsys.jar di.uniba.it.lodrecsys.graph.GraphFSRun CFSubsetEval"
                cmd = "java -cp lodrecsys.jar di.uniba.it.lodrecsys.graph.GraphFSRun "+db+" "+ alg
                cmdLOG = "java -cp GraphFSRun "+db+" "+ alg
                cmdExecFS.append(cmd)
                cmdExecLOGFS.append(cmdLOG)
                extractVal.append(alg+split+"split")
                for given in givenN:
            #               cmd = "java -cp lodrecsys.jar di.uniba.it.lodrecsys.graph.GraphRecRun given_5 CFSubsetEval &"
                    cmd = "java -cp lodrecsys.jar di.uniba.it.lodrecsys.graph.GraphRecRun "+db+" "+given+" "+ alg+" &"
                    cmdLOG = "java -cp GraphRecRun "+db+" "+given+" "+ alg+" &"
                    cmdExecREC.append(cmd)
                    cmdExecLOGREC.append(cmdLOG)

                for given in givenN:
                    metricString=""
                    for metric in metrics:
                        metricString +=metric + " "
    #                    cmd = "java -cp lodrecsys.jar di.uniba.it.lodrecsys.graph.GraphEvalRun given_5 CFSubsetEval F1 Novelty &"
                    cmd = "java -cp lodrecsys.jar di.uniba.it.lodrecsys.graph.GraphEvalRun "+db+" "+given+" "+ alg+" "+metricString +" &"
                    cmdLOG = "java -cp GraphEvalRun "+db+" "+given+" "+ alg+" "+metricString +" &"
                    cmdExecEV.append(cmd)
                    cmdExecLOGEV.append(cmdLOG)
            else:
                for top in topN:
            #       cmd = "java -cp lodrecsys.jar di.uniba.it.lodrecsys.graph.GraphFSRun MRMR 11"
                    cmd = "java -cp lodrecsys.jar di.uniba.it.lodrecsys.graph.GraphFSRun "+db+" "+ alg+" "+top
                    cmdLOG = "java -cp GraphFSRun "+db+" "+ alg+" "+top
                    cmdExecFS.append(cmd)
                    cmdExecLOGFS.append(cmdLOG)
                    extractVal.append(alg+top+"prop"+split+"split")
            #        print time.strftime("%Y-%m-%d %H:%M") + " "+cmdLOG +"\n"

                    for given in givenN:
            #               cmd = "java -cp lodrecsys.jar di.uniba.it.lodrecsys.graph.GraphRecRun given_5 MRMR 11 &"
                        cmd = "java -cp lodrecsys.jar di.uniba.it.lodrecsys.graph.GraphRecRun "+db+" "+given+" "+ alg+" "+top+" &"
                        cmdLOG = "java -cp GraphRecRun "+db+" "+given+" "+ alg+" "+top+" &"
                        cmdExecREC.append(cmd)
                        cmdExecLOGREC.append(cmdLOG)

                    for given in givenN:
                        metricString=""
                        for metric in metrics:
                            metricString +=metric + " "
                        cmd = "java -cp lodrecsys.jar di.uniba.it.lodrecsys.graph.GraphEvalRun "+db+" "+given+" "+ alg+" "+top+" "+metricString +" &"
                        cmdLOG = "java -cp GraphEvalRun "+db+" "+given+" "+ alg+" "+top+" "+metricString +" &"
                        cmdExecEV.append(cmd)
                        cmdExecLOGEV.append(cmdLOG)
        print time.strftime("%Y-%m-%d %H:%M") + " Init finished. \n"
