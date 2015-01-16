#!/usr/bin/python
import time
import subprocess
import sys
import os
import shutil
from time import gmtime, strftime, localtime
from datetime import datetime

def sperim(allalg, allalgWEKA, topN, givenN, param, cmdThread):
    cmdExecFS=[]
    cmdExecLOGFS=[]

    cmdExecREC=[]
    cmdExecLOGREC=[]

    cmdExecEV=[]
    cmdExecLOGEV=[]

    extractVal=[]
    for alg in allalgWEKA:
        for top in topN:
    #       cmd = "java -cp lodrecsys.jar di.uniba.it.lodrecsys.graph.GraphFSRun RankerWeka 11 LatentSemanticAnalysis"
            cmd = "java -cp lodrecsys.jar di.uniba.it.lodrecsys.graph.GraphFSRun RankerWeka "+top+" "+ alg
            cmdLOG = "java -cp GraphFSRun RankerWeka "+top+" "+ alg
            cmdExecFS.append(cmd)
            cmdExecLOGFS.append(cmdLOG)
            extractVal.append("RankerWeka"+alg+top+"prop5split")
    #        print time.strftime("%Y-%m-%d %H:%M") + " "+cmdLOG +"\n"

            for given in givenN:
    #               cmd = "java -cp lodrecsys.jar di.uniba.it.lodrecsys.graph.GraphRecRun given_5 RankerWeka 11 PCA &"
                cmd = "java -cp lodrecsys.jar di.uniba.it.lodrecsys.graph.GraphRecRun "+given+" RankerWeka "+top+" "+alg+" &"
                cmdLOG = "java -cp GraphRecRun "+given+" RankerWeka "+top+" "+alg+" &"
                cmdExecREC.append(cmd)
                cmdExecLOGREC.append(cmdLOG)

            for given in givenN:
                cmd = "java -cp lodrecsys.jar di.uniba.it.lodrecsys.graph.GraphEvalRun "+given+" RankerWeka "+top+" "+alg
                cmdLOG = "java -cp GraphEvalRun "+given+" RankerWeka "+top+" "+alg
                cmdExecEV.append(cmd)
                cmdExecLOGEV.append(cmdLOG)

    for alg in allalg:
        for top in topN:
    #       cmd = "java -cp lodrecsys.jar di.uniba.it.lodrecsys.graph.GraphFSRun MRMR 11"
            cmd = "java -cp lodrecsys.jar di.uniba.it.lodrecsys.graph.GraphFSRun "+ alg+" "+top
            cmdLOG = "java -cp GraphFSRun "+ alg+" "+top
            cmdExecFS.append(cmd)
            cmdExecLOGFS.append(cmdLOG)
            extractVal.append(alg+top+"prop5split")
    #        print time.strftime("%Y-%m-%d %H:%M") + " "+cmdLOG +"\n"

            for given in givenN:
    #               cmd = "java -cp lodrecsys.jar di.uniba.it.lodrecsys.graph.GraphRecRun given_5 MRMR 11 &"
                cmd = "java -cp lodrecsys.jar di.uniba.it.lodrecsys.graph.GraphRecRun "+given+" "+ alg+" "+top+" &"
                cmdLOG = "java -cp GraphRecRun "+given+" "+ alg+" "+top+" &"
                cmdExecREC.append(cmd)
                cmdExecLOGREC.append(cmdLOG)

            for given in givenN:
                cmd = "java -cp lodrecsys.jar di.uniba.it.lodrecsys.graph.GraphEvalRun "+given+" "+ alg+" "+top
                cmdLOG = "java -cp GraphEvalRun "+given+" "+ alg+" "+top
                cmdExecEV.append(cmd)
                cmdExecLOGEV.append(cmdLOG)
    
    # Execute cmd parallel Feature

    while (len(cmdExecFS)>1):
        numThread = subprocess.check_output(cmdThread,shell=True)
        val= int(numThread)-1
        if ((param-val) < len(cmdExecFS)):
            for aa in range(0,param-val):
                subprocess.call(cmdExecFS[aa], shell=True)
                print time.strftime("%Y-%m-%d %H:%M") + " "+cmdExecLOGFS[aa] +"\n"

            for a in range(0,param-val):
                del cmdExecFS[0]
                del cmdExecLOGFS[0]
        else:
            for aa in range(0,len(cmdExecFS)):
                subprocess.call(cmdExecFS[aa], shell=True)
                print time.strftime("%Y-%m-%d %H:%M") + " "+cmdExecLOGFS[aa] +"\n"

            for a in range(0,len(cmdExecFS)):
                del cmdExecFS[0]
                del cmdExecLOGFS[0]

    if (len(cmdExecFS)!=0):
        numThread =subprocess.check_output(cmdThread,shell=True)
        val=int(numThread)-1

        while (val >= param):
            numThread =subprocess.check_output(cmdThread,shell=True)
            val=int(numThread)-1

        print time.strftime("%Y-%m-%d %H:%M") + " "+cmdExecLOGFS[0] +"\n"
        subprocess.call(cmdExecFS[0], shell=True)
        cmdExecFS=[]
        cmdExecLOGFS=[]
    print "Fine FS"

    ## Execute cmd parallel Recommendation
    while (len(cmdExecREC)>1):
        numThread = subprocess.check_output(cmdThread,shell=True)
        val= int(numThread)-1
        if ((param-val) < len(cmdExecREC)):
            for aa in range(0,param-val):
    #            subprocess.call(cmdExecREC[aa], shell=True)
                print time.strftime("%Y-%m-%d %H:%M") + " "+cmdExecLOGREC[aa] +"\n"

            for a in range(0,param-val):
                del cmdExecREC[0]
                del cmdExecLOGREC[0]
        else:
            for aa in range(0,len(cmdExecREC)):
    #            subprocess.call(cmdExecREC[aa], shell=True)
                print time.strftime("%Y-%m-%d %H:%M") + " "+cmdExecLOGREC[aa] +"\n"

            for a in range(0,len(cmdExecREC)):
                del cmdExecREC[0]
                del cmdExecLOGREC[0]

    if (len(cmdExecREC)!=0):
        numThread =subprocess.check_output(cmdThread,shell=True)
        val=int(numThread)-1

        while (val >= param):
            numThread =subprocess.check_output(cmdThread,shell=True)
            val=int(numThread)-1

        print time.strftime("%Y-%m-%d %H:%M") + " "+cmdExecLOGREC[0] +"\n"
    #    subprocess.call(cmdExecREC[0], shell=True)
        cmdExecREC=[]
        cmdExecLOGREC=[]

    numThread = subprocess.check_output(cmdThread,shell=True)
    val= int(numThread)-1
    while (val>0):
        numThread = subprocess.check_output(cmdThread,shell=True)
        val= int(numThread)-1
    print time.strftime("%Y-%m-%d %H:%M")+" Fine Rec"

    # Execute cmd parallel Eval

    while (len(cmdExecEV)>1):
        numThread = subprocess.check_output(cmdThread,shell=True)
        val= int(numThread)-1
        
        if ((param-val) < len(cmdExecEV)):
            for aa in range(0,param-val):
    #            subprocess.call(cmdExecEV[aa], shell=True)
                print time.strftime("%Y-%m-%d %H:%M") + " "+cmdExecLOGEV[aa] +"\n"

            for a in range(0,param-val):
                del cmdExecEV[0]
                del cmdExecLOGEV[0]
        else:
            for aa in range(0,len(cmdExecEV)):
    #            subprocess.call(cmdExecEV[aa], shell=True)
                print time.strftime("%Y-%m-%d %H:%M") + " "+cmdExecLOGEV[aa] +"\n"

            for a in range(0,len(cmdExecEV)):
                del cmdExecEV[0]
                del cmdExecLOGEV[0]

    if (len(cmdExecEV)!=0):
        numThread =subprocess.check_output(cmdThread,shell=True)
        val=int(numThread)-1

        while (val >= param):
            numThread =subprocess.check_output(cmdThread,shell=True)
            val=int(numThread)-1

        print time.strftime("%Y-%m-%d %H:%M") + " "+cmdExecLOGEV[0] +"\n"
    #    subprocess.call(cmdExecEV[0], shell=True)
        cmdExecEV=[]
        cmdExecLOGEV=[]
    print "Fine EV"

    #Extraction values

    dire="./datasets/ml-100k/results/UserItemExpDBPedia/"
    for alg in os.listdir(dire):
        if alg in extractVal:
            if os.path.exists(dire+alg+"/summaries/"):
	            shutil.rmtree(dire+alg+"/summaries/")
            os.makedirs(dire+alg+"/summaries/")
            for given in os.listdir(dire+alg):
	            if "given" in given: 
		            with open(dire+alg+"/summaries/"+given+".summary", "a") as myfile:
                			myfile.write("\nResult Top 50 \n")
		            cmd = "cat "+dire+alg+"/"+given+"/top_50/"+"metrics.complete"
		            cmd += " >> "+dire+alg+"/summaries/"+given+".summary"
		            subprocess.call(cmd, shell=True)    

            metrics=["F1"]
            for metric in metrics:
                valor=["5","10","15","20","30","50"]
                for elem in valor:
                    cmd = "grep \""+metric+"_"+elem+"=\" "+dire+alg+"/summaries/*.summary >> "+dire+alg+"/summaries/res"+metric+elem+".sum1"
                    subprocess.call(cmd, shell=True)
                    cmd ="sed -i 's\ "+dire+alg+"/summaries/given_\ \ ' "+dire+alg+"/summaries/res"+metric+elem+".sum1"
                    subprocess.call(cmd, shell=True)
                    cmd ="cat "+dire+alg+"/summaries/res"+metric+elem+".sum1 | awk 'BEGIN { FS = \"given_\"};{ print $2 }'| uniq > "+dire+alg+"/summaries/res"+metric+elem+".sum"
                    subprocess.call(cmd, shell=True)
                    cmd ="rm "+dire+alg+"/summaries/res"+metric+elem+".sum1"
                    subprocess.call(cmd, shell=True)
                    cmd ="sed -i 's\.summary:"+metric+"_"+elem+"=\ \ ' "+dire+alg+"/summaries/res"+metric+elem+".sum"
                    subprocess.call(cmd, shell=True)
                    cmd ="sed -i 's\\all\\100\ ' "+dire+alg+"/summaries/res"+metric+elem+".sum"
                    subprocess.call(cmd, shell=True)
                    cmd ="sed -i 's\\ 0.\\ 0,\ ' "+dire+alg+"/summaries/res"+metric+elem+".sum"
                    subprocess.call(cmd, shell=True)
                    cmd ="sed -i 's\\5 0\\005 0\ ' "+dire+alg+"/summaries/res"+metric+elem+".sum"
                    subprocess.call(cmd, shell=True)
                    cmd ="sed -i 's\\10 0\\010 0\ ' "+dire+alg+"/summaries/res"+metric+elem+".sum"
                    subprocess.call(cmd, shell=True)
                    cmd ="sed -i 's\\20 0\\020 0\ ' "+dire+alg+"/summaries/res"+metric+elem+".sum"
                    subprocess.call(cmd, shell=True)
                    cmd ="sed -i 's\\30 0\\030 0\ ' "+dire+alg+"/summaries/res"+metric+elem+".sum"
                    subprocess.call(cmd, shell=True)
                    cmd ="sed -i 's\\50 0\\050 0\ ' "+dire+alg+"/summaries/res"+metric+elem+".sum"
                    subprocess.call(cmd, shell=True)
                    cmd ="sed -i 's\\100 0\\100 0\ ' "+dire+alg+"/summaries/res"+metric+elem+".sum"
                    subprocess.call(cmd, shell=True)
                    cmd ="sort "+dire+alg+"/summaries/res"+metric+elem+".sum > "+dire+alg+"/summaries/result"+metric+elem
                    subprocess.call(cmd, shell=True)
                    cmd ="rm "+dire+alg+"/summaries/res"+metric+elem+".sum"
                    subprocess.call(cmd, shell=True)
                    cmd ="sed -i 's\\005 \\5 \ ' "+dire+alg+"/summaries/result"+metric+elem
                    subprocess.call(cmd, shell=True)
                    cmd ="sed -i 's\\010 \\10 \ ' "+dire+alg+"/summaries/result"+metric+elem
                    subprocess.call(cmd, shell=True)
                    cmd ="sed -i 's\\020 \\20 \ ' "+dire+alg+"/summaries/result"+metric+elem
                    subprocess.call(cmd, shell=True)
                    cmd ="sed -i 's\\030 \\30 \ ' "+dire+alg+"/summaries/result"+metric+elem
                    subprocess.call(cmd, shell=True)
                    cmd ="sed -i 's\\050 \\50 \ ' "+dire+alg+"/summaries/result"+metric+elem
                    subprocess.call(cmd, shell=True)
            print time.strftime("%Y-%m-%d %H:%M") + " "+ alg + " completed."
    print time.strftime("%Y-%m-%d %H:%M") + " All result completed."

    #feature:
    #	java -cp lodrecsys.jar di.uniba.it.lodrecsys.graph.GraphFSRun CFSubsetEval

    #recommendation1:
    #	java -cp lodrecsys.jar di.uniba.it.lodrecsys.graph.GraphRecRun given_5 CFSubsetEval &
    #	java -cp lodrecsys.jar di.uniba.it.lodrecsys.graph.GraphRecRun given_10 CFSubsetEval &
    #	java -cp lodrecsys.jar di.uniba.it.lodrecsys.graph.GraphRecRun given_20 CFSubsetEval

    #recommendation: recommendation1
    #	java -cp lodrecsys.jar di.uniba.it.lodrecsys.graph.GraphRecRun given_30 CFSubsetEval &
    #	java -cp lodrecsys.jar di.uniba.it.lodrecsys.graph.GraphRecRun given_50 CFSubsetEval &
    #	java -cp lodrecsys.jar di.uniba.it.lodrecsys.graph.GraphRecRun given_all CFSubsetEval

    #evaluation:
    #	java -cp lodrecsys.jar di.uniba.it.lodrecsys.graph.GraphEvalRun given_5 PageRank 5
    #	java -cp lodrecsys.jar di.uniba.it.lodrecsys.graph.GraphEvalRun given_10 PageRank 5 
    #	java -cp lodrecsys.jar di.uniba.it.lodrecsys.graph.GraphEvalRun given_20 PageRank 5 
    #	java -cp lodrecsys.jar di.uniba.it.lodrecsys.graph.GraphEvalRun given_30 PageRank 5 
    #	java -cp lodrecsys.jar di.uniba.it.lodrecsys.graph.GraphEvalRun given_50 PageRank 5 
    #	java -cp lodrecsys.jar di.uniba.it.lodrecsys.graph.GraphEvalRun given_all PageRank 5 

    #evaluationCF:
    #	java -cp lodrecsys.jar di.uniba.it.lodrecsys.graph.GraphEvalRun given_5 RankerWeka 11 LatentSemanticAnalysis
    #	java -cp lodrecsys.jar di.uniba.it.lodrecsys.graph.GraphEvalRun given_10 RankerWeka 11 LatentSemanticAnalysis
    #	java -cp lodrecsys.jar di.uniba.it.lodrecsys.graph.GraphEvalRun given_20 RankerWeka 11 LatentSemanticAnalysis
    #	java -cp lodrecsys.jar di.uniba.it.lodrecsys.graph.GraphEvalRun given_30 RankerWeka 11 LatentSemanticAnalysis
    #	java -cp lodrecsys.jar di.uniba.it.lodrecsys.graph.GraphEvalRun given_50 RankerWeka 11 LatentSemanticAnalysis
    #	java -cp lodrecsys.jar di.uniba.it.lodrecsys.graph.GraphEvalRun given_all RankerWeka 11 LatentSemanticAnalysis