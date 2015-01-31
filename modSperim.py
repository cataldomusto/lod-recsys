#!/usr/bin/python
import time
import subprocess
import sys
import os
import shutil
from time import gmtime, strftime, localtime
from datetime import datetime

def sperim(allalg, allalgWEKA, topN, givenN, param,cmdThreadFS, cmdThreadRec,cmdThreadEval, metrics):
    
    cmdExecFS=[]
    cmdExecLOGFS=[]

    cmdExecREC=[]
    cmdExecLOGREC=[]

    cmdExecEV=[]
    cmdExecLOGEV=[]

    extractVal=[]
    
    init(topN, givenN, allalgWEKA, allalg, extractVal, cmdExecFS, cmdExecLOGFS, cmdExecREC, cmdExecLOGREC, cmdExecEV, cmdExecLOGEV)
    
    featureProcess(cmdExecFS, cmdExecLOGFS, cmdThreadFS, param)

#    recommendationProcess(cmdExecREC, cmdExecLOGREC, cmdThreadRec, param)

    evaluationProcess(cmdExecEV, cmdExecLOGEV, cmdThreadEval, param)

    createSummaries(extractVal, metrics)
    
    createCSV(metrics, allalg, allalgWEKA)

    print time.strftime("%Y-%m-%d %H:%M") + " Finished."


def init(topN, givenN, allalgWEKA, allalg, extractVal, cmdExecFS, cmdExecLOGFS, cmdExecREC, cmdExecLOGREC, cmdExecEV, cmdExecLOGEV):
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
                cmd = "java -cp lodrecsys.jar di.uniba.it.lodrecsys.graph.GraphEvalRun "+given+" RankerWeka "+top+" "+alg+" &"
                cmdLOG = "java -cp GraphEvalRun "+given+" RankerWeka "+top+" "+alg+" &"
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
                cmd = "java -cp lodrecsys.jar di.uniba.it.lodrecsys.graph.GraphEvalRun "+given+" "+ alg+" "+top+" &"
                cmdLOG = "java -cp GraphEvalRun "+given+" "+ alg+" "+top+" &"
                cmdExecEV.append(cmd)
                cmdExecLOGEV.append(cmdLOG)
    print time.strftime("%Y-%m-%d %H:%M") + " Init finished. \n"

##   Execute cmd parallel Feature
def featureProcess(cmdExecFS, cmdExecLOGFS, cmdThreadFS, param):
    while (len(cmdExecFS)>1):
        numThread = subprocess.check_output(cmdThreadFS,shell=True)
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
        numThread =subprocess.check_output(cmdThreadFS,shell=True)
        val=int(numThread)-1

        while (val >= param):
            numThread =subprocess.check_output(cmdThreadFS,shell=True)
            val=int(numThread)-1

        print time.strftime("%Y-%m-%d %H:%M") + " "+cmdExecLOGFS[0] +"\n"
        subprocess.call(cmdExecFS[0], shell=True)
        cmdExecFS=[]
        cmdExecLOGFS=[]
    print "Fine FS"

##   Execute cmd parallel Recommendation
def recommendationProcess(cmdExecREC, cmdExecLOGREC, cmdThreadRec, param):
    while (len(cmdExecREC)>1):
        numThread = subprocess.check_output(cmdThreadRec,shell=True)
        val= int(numThread)-1
        if ((param-val) < len(cmdExecREC)):
            for aa in range(0,param-val):
                subprocess.call(cmdExecREC[aa], shell=True)
                print time.strftime("%Y-%m-%d %H:%M") + " "+cmdExecLOGREC[aa] +"\n"

            for a in range(0,param-val):
                del cmdExecREC[0]
                del cmdExecLOGREC[0]
        else:
            for aa in range(0,len(cmdExecREC)):
                subprocess.call(cmdExecREC[aa], shell=True)
                print time.strftime("%Y-%m-%d %H:%M") + " "+cmdExecLOGREC[aa] +"\n"

            for a in range(0,len(cmdExecREC)):
                del cmdExecREC[0]
                del cmdExecLOGREC[0]

    if (len(cmdExecREC)!=0):
        numThread =subprocess.check_output(cmdThreadRec,shell=True)
        val=int(numThread)-1

        while (val >= param):
            numThread =subprocess.check_output(cmdThreadRec,shell=True)
            val=int(numThread)-1

        print time.strftime("%Y-%m-%d %H:%M") + " "+cmdExecLOGREC[0] +"\n"
        subprocess.call(cmdExecREC[0], shell=True)
        cmdExecREC=[]
        cmdExecLOGREC=[]

    numThread = subprocess.check_output(cmdThreadRec,shell=True)
    val= int(numThread)-1
    while (val>0):
        numThread = subprocess.check_output(cmdThreadRec,shell=True)
        val= int(numThread)-1
    print time.strftime("%Y-%m-%d %H:%M")+" Fine Rec"


##   Execute cmd parallel Eval
def evaluationProcess(cmdExecEV, cmdExecLOGEV, cmdThreadEval, param):
    while (len(cmdExecEV)>1):
        numThread = subprocess.check_output(cmdThreadEval,shell=True)
        val= int(numThread)-1
        
        if ((param-val) < len(cmdExecEV)):
            for aa in range(0,param-val):
                subprocess.call(cmdExecEV[aa], shell=True)
                print time.strftime("%Y-%m-%d %H:%M") + " "+cmdExecLOGEV[aa] +"\n"

            for a in range(0,param-val):
                del cmdExecEV[0]
                del cmdExecLOGEV[0]
        else:
            for aa in range(0,len(cmdExecEV)):
                subprocess.call(cmdExecEV[aa], shell=True)
                print time.strftime("%Y-%m-%d %H:%M") + " "+cmdExecLOGEV[aa] +"\n"

            for a in range(0,len(cmdExecEV)):
                del cmdExecEV[0]
                del cmdExecLOGEV[0]

    if (len(cmdExecEV)!=0):
        numThread =subprocess.check_output(cmdThreadEval,shell=True)
        val=int(numThread)-1

        while (val >= param):
            numThread =subprocess.check_output(cmdThreadEval,shell=True)
            val=int(numThread)-1

#        print time.strftime("%Y-%m-%d %H:%M") + " "+cmdExecLOGEV[0] +"\n"
        subprocess.call(cmdExecEV[0], shell=True)
        cmdExecEV=[]
        cmdExecLOGEV=[]
    print "Fine EV"

##   Extraction values
def createSummaries(extractVal, metrics):
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

            for metric in metrics:
                if metric == 'alpha-nDCG' or metric == 'P-IA':
                    valor=["5","10","20"]
                else:
                    valor=["5","10","15","20"]
                for elem in valor:
                    extractResult(metric,elem,dire,alg)
                cmd ="awk '1;!(NR%6){print \" \";}' "+dire+alg+"/summaries/"+metric+"Temp > "+dire+alg+"/summaries/"+metric+"Sum"
                subprocess.call(cmd, shell=True)
                cmd ="rm "+dire+alg+"/summaries/"+metric+"Temp"
                subprocess.call(cmd, shell=True)
            print time.strftime("%Y-%m-%d %H:%M") + " "+ alg + " completed."
    print time.strftime("%Y-%m-%d %H:%M") + " All result completed."

##   CreateCSV to execute Kruskal-wallis test
def createCSV(metrics, allalg, allalgWEKA):
    for metric in metrics: 
        for alg in allalg:
#            cmd = "java -cp lodrecsys.jar di.uniba.it.lodrecsys.utils.MakerCSV "+alg+" "+metric+" baseline"
            cmd = "java -cp lodrecsys.jar di.uniba.it.lodrecsys.utils.MakerCSV "+alg+" "+metric
            print cmd
            subprocess.call(cmd, shell=True)
            dire="./datasets/ml-100k/results/UserItemExpDBPedia/CSV/"+metric+"/"+alg+"/"
            cmd = "Rscript scriptRtest "+dire+" "+dire+"resultTest"
            subprocess.call(cmd, shell=True)
        
        for alg in allalgWEKA:
#            cmd = "java -cp lodrecsys.jar di.uniba.it.lodrecsys.utils.MakerCSV RankerWeka"+alg+" "+metric+" baseline"
            cmd = "java -cp lodrecsys.jar di.uniba.it.lodrecsys.utils.MakerCSV RankerWeka"+alg+" "+metric
            print cmd
            subprocess.call(cmd, shell=True)
            dire="./datasets/ml-100k/results/UserItemExpDBPedia/CSV/"+metric+"/RankerWeka"+alg+"/"
            cmd = "Rscript scriptRtest "+dire+" "+dire+"resultTest"
            subprocess.call(cmd, shell=True)

##   Extract result from file
def extractResult(metric,elem,dire,alg):
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
    cmd ="cat "+dire+alg+"/summaries/result"+metric+elem+" | awk 'BEGIN { FS = \" \"};{ print $2 }' | uniq >> "+dire+alg+"/summaries/"+metric+"Temp"
    subprocess.call(cmd, shell=True)
