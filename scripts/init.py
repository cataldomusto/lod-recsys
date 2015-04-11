#!/usr/bin/python
import time
import subprocess
import sys
import os
import shutil
from time import gmtime, strftime, localtime
from datetime import datetime

def init(topN, givenN, allalgWEKA, allalg, extractVal, cmdExecFS, cmdExecLOGFS, cmdExecREC, cmdExecLOGREC, cmdExecEV, cmdExecLOGEV, metrics, split, cmdExecBase, cmdExecLOGBase, algBaseline):
    for alg in algBaseline:
        for given in givenN:
            metricString=""
            for metric in metrics:
                metricString +=metric + " "
    #       cmd = "java -cp lodrecsys.jar di.uniba.it.lodrecsys.baseline.Baseline given_5 UserKNN novelty diversity &"
            cmd = "java -cp lodrecsys.jar di.uniba.it.lodrecsys.baseline.Baseline "+given+" "+alg+" "+metricString +" &"
            cmdLOG = "java -cp Baseline "+given+" "+alg+" "+metricString +" &"
            cmdExecBase.append(cmd)
            cmdExecLOGBase.append(cmdLOG)

    for alg in allalgWEKA:
        for top in topN:
    #       cmd = "java -cp lodrecsys.jar di.uniba.it.lodrecsys.graph.GraphFSRun RankerWeka 11 LatentSemanticAnalysis"
            cmd = "java -cp lodrecsys.jar di.uniba.it.lodrecsys.graph.GraphFSRun RankerWeka "+top+" "+ alg
            cmdLOG = "java -cp GraphFSRun RankerWeka "+top+" "+ alg
            cmdExecFS.append(cmd)
            cmdExecLOGFS.append(cmdLOG)
            extractVal.append("RankerWeka"+alg+top+"prop"+split+"split")
    #        print time.strftime("%Y-%m-%d %H:%M") + " "+cmdLOG +"\n"

            for given in givenN:
    #               cmd = "java -cp lodrecsys.jar di.uniba.it.lodrecsys.graph.GraphRecRun given_5 RankerWeka 11 PCA &"
                cmd = "java -cp lodrecsys.jar di.uniba.it.lodrecsys.graph.GraphRecRun "+given+" RankerWeka "+top+" "+alg+" &"
                cmdLOG = "java -cp GraphRecRun "+given+" RankerWeka "+top+" "+alg+" &"
                cmdExecREC.append(cmd)
                cmdExecLOGREC.append(cmdLOG)

            for given in givenN:
                metricString=""
                for metric in metrics:
                    metricString +=metric + " "
                cmd = "java -cp lodrecsys.jar di.uniba.it.lodrecsys.graph.GraphEvalRun "+given+" RankerWeka "+top+" "+alg+"  "+metricString+" &"
                cmdLOG = "java -cp GraphEvalRun "+given+" RankerWeka "+top+" "+alg+" "+metricString+" &"
                cmdExecEV.append(cmd)
                cmdExecLOGEV.append(cmdLOG)

    for alg in allalg:
        if (alg=="CFSubsetEval" or "Custom" in alg):
    #       cmd = "java -cp lodrecsys.jar di.uniba.it.lodrecsys.graph.GraphFSRun CFSubsetEval"
            cmd = "java -cp lodrecsys.jar di.uniba.it.lodrecsys.graph.GraphFSRun "+ alg
            cmdLOG = "java -cp GraphFSRun "+ alg
            cmdExecFS.append(cmd)
            cmdExecLOGFS.append(cmdLOG)
            extractVal.append(alg+split+"split")
            for given in givenN:
        #               cmd = "java -cp lodrecsys.jar di.uniba.it.lodrecsys.graph.GraphRecRun given_5 CFSubsetEval &"
                cmd = "java -cp lodrecsys.jar di.uniba.it.lodrecsys.graph.GraphRecRun "+given+" "+ alg+" &"
                cmdLOG = "java -cp GraphRecRun "+given+" "+ alg+" &"
                cmdExecREC.append(cmd)
                cmdExecLOGREC.append(cmdLOG)

            for given in givenN:
                metricString=""
                for metric in metrics:
                    metricString +=metric + " "
#                    cmd = "java -cp lodrecsys.jar di.uniba.it.lodrecsys.graph.GraphEvalRun given_5 CFSubsetEval F1 Novelty &"
                cmd = "java -cp lodrecsys.jar di.uniba.it.lodrecsys.graph.GraphEvalRun "+given+" "+ alg+" "+metricString +" &"
                cmdLOG = "java -cp GraphEvalRun "+given+" "+ alg+" "+metricString +" &"
                cmdExecEV.append(cmd)
                cmdExecLOGEV.append(cmdLOG)
        else:
            for top in topN:
        #       cmd = "java -cp lodrecsys.jar di.uniba.it.lodrecsys.graph.GraphFSRun MRMR 11"
                cmd = "java -cp lodrecsys.jar di.uniba.it.lodrecsys.graph.GraphFSRun "+ alg+" "+top
                cmdLOG = "java -cp GraphFSRun "+ alg+" "+top
                cmdExecFS.append(cmd)
                cmdExecLOGFS.append(cmdLOG)
                extractVal.append(alg+top+"prop"+split+"split")
        #        print time.strftime("%Y-%m-%d %H:%M") + " "+cmdLOG +"\n"

                for given in givenN:
        #               cmd = "java -cp lodrecsys.jar di.uniba.it.lodrecsys.graph.GraphRecRun given_5 MRMR 11 &"
                    cmd = "java -cp lodrecsys.jar di.uniba.it.lodrecsys.graph.GraphRecRun "+given+" "+ alg+" "+top+" &"
                    cmdLOG = "java -cp GraphRecRun "+given+" "+ alg+" "+top+" &"
                    cmdExecREC.append(cmd)
                    cmdExecLOGREC.append(cmdLOG)

                for given in givenN:
                    metricString=""
                    for metric in metrics:
                        metricString +=metric + " "
                    cmd = "java -cp lodrecsys.jar di.uniba.it.lodrecsys.graph.GraphEvalRun "+given+" "+ alg+" "+top+" "+metricString +" &"
                    cmdLOG = "java -cp GraphEvalRun "+given+" "+ alg+" "+top+" "+metricString +" &"
                    cmdExecEV.append(cmd)
                    cmdExecLOGEV.append(cmdLOG)
    print time.strftime("%Y-%m-%d %H:%M") + " Init finished. \n"
