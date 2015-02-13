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
    
    init(topN, givenN, allalgWEKA, allalg, extractVal, cmdExecFS, cmdExecLOGFS, cmdExecREC, cmdExecLOGREC, cmdExecEV, cmdExecLOGEV, metrics)
    
#    feature process
#    parallelProcess(cmdExecFS, cmdExecLOGFS, cmdThreadFS, param, "Feature process")

#    recommendation Process
#    parallelProcess(cmdExecREC, cmdExecLOGREC, cmdThreadRec, param, "Recommendation process")
    
#    evaluation Process
#    parallelProcess(cmdExecEV, cmdExecLOGEV, cmdThreadEval, param, "Evaluation process")

#    createSummaries(extractVal, metrics)
#    createSummariesALL(extractVal, metrics)
    
    createCSV(topN, metrics, allalg, allalgWEKA)

    print time.strftime("%Y-%m-%d %H:%M") + " Finished."


def init(topN, givenN, allalgWEKA, allalg, extractVal, cmdExecFS, cmdExecLOGFS, cmdExecREC, cmdExecLOGREC, cmdExecEV, cmdExecLOGEV, metrics):
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
                metricString=""
                for metric in metrics:
                    metricString +=metric + " "
                cmd = "java -cp lodrecsys.jar di.uniba.it.lodrecsys.graph.GraphEvalRun "+given+" RankerWeka "+top+" "+alg+"  "+metricString+" &"
                cmdLOG = "java -cp GraphEvalRun "+given+" RankerWeka "+top+" "+alg+" "+metricString+" &"
                cmdExecEV.append(cmd)
                cmdExecLOGEV.append(cmdLOG)

    for alg in allalg:
        if (alg=="CFSubsetEval"):
    #       cmd = "java -cp lodrecsys.jar di.uniba.it.lodrecsys.graph.GraphFSRun CFSubsetEval"
            cmd = "java -cp lodrecsys.jar di.uniba.it.lodrecsys.graph.GraphFSRun "+ alg
            cmdLOG = "java -cp GraphFSRun "+ alg
            cmdExecFS.append(cmd)
            cmdExecLOGFS.append(cmdLOG)
            extractVal.append(alg+"5split")
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
                extractVal.append(alg+top+"prop5split")
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

def parallelProcess(cmdExec, cmdExecLOG, cmdThread, param, typeProcess):
    print time.strftime("%Y-%m-%d %H:%M") + " "+typeProcess + " started. \n"
    while (len(cmdExec)>1):
        numThread = subprocess.check_output(cmdThread,shell=True)
        val= int(numThread)-1
        if ((param-val) < len(cmdExec)):
            for aa in range(0,param-val):
                subprocess.call(cmdExec[aa], shell=True)
                print time.strftime("%Y-%m-%d %H:%M") + " "+cmdExecLOG[aa] +"\n"

            for a in range(0,param-val):
                del cmdExec[0]
                del cmdExecLOG[0]
        else:
            for aa in range(0,len(cmdExec)):
                subprocess.call(cmdExec[aa], shell=True)
                print time.strftime("%Y-%m-%d %H:%M") + " "+cmdExecLOG[aa] +"\n"

            for a in range(0,len(cmdExec)):
                del cmdExec[0]
                del cmdExecLOG[0]

    if (len(cmdExec)!=0):
        numThread =subprocess.check_output(cmdThread,shell=True)
        val=int(numThread)-1

        while (val >= param):
            numThread =subprocess.check_output(cmdThread,shell=True)
            val=int(numThread)-1

        print time.strftime("%Y-%m-%d %H:%M") + " "+cmdExecLOG[0] +"\n"
        subprocess.call(cmdExec[0], shell=True)
        cmdExec=[]
        cmdExecLOG=[]

    numThread = subprocess.check_output(cmdThread,shell=True)
    val= int(numThread)-1
    while (val>0):
        numThread = subprocess.check_output(cmdThread,shell=True)
        val= int(numThread)-1
    print time.strftime("%Y-%m-%d %H:%M") + " "+typeProcess + " finished. \n"

##   Extraction values
def createSummaries(extractVal, metrics):
    dire="./datasets/ml-100k/results/UserItemExpDBPedia/"
    cmd=""
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

def createSummariesALL(extractVal, metrics):
    dire="./datasets/ml-100k/results/UserItemExpDBPedia/"
    cmd=""
    for alg in os.listdir(dire):
        if alg in extractVal:
            if not(os.path.exists(dire+alg+"/summaries/")):
                os.makedirs(dire+alg+"/summaries/")
            for given in os.listdir(dire+alg):
	            if "given" in given: 
		            with open(dire+alg+"/summaries/"+given+".summaryALL", "a") as myfile:
                			myfile.write("\nResult Top 50 \n")
		            cmd = "cat "+dire+alg+"/"+given+"/top_50/"+"metrics.completeALL"
		            cmd += " > "+dire+alg+"/summaries/"+given+".summaryALL"
    	            subprocess.call(cmd, shell=True)    

            for metric in metrics:
                valor=["5","10","20"]
                for elem in valor:
                    extractResultALL(metric,elem,dire,alg)
                for sparsity in valor:
                    cmd ="cat "+dire+alg+"/summaries/result"+metric+sparsity+"ALL | grep '5 ' | awk 'BEGIN { FS = \" \"};{ print $2 }' > "+dire+alg+"/summaries/result"+metric+"_Top_"+sparsity+"given_5.ALL" 
                    subprocess.call(cmd, shell=True)
                    cmd ="cat "+dire+alg+"/summaries/result"+metric+sparsity+"ALL | grep '20 ' | awk 'BEGIN { FS = \" \"};{ print $2 }' > "+dire+alg+"/summaries/result"+metric+"_Top_"+sparsity+"given_20.ALL" 
                    subprocess.call(cmd, shell=True)
                    cmd ="cat "+dire+alg+"/summaries/result"+metric+sparsity+"ALL | grep '100 ' | awk 'BEGIN { FS = \" \"};{ print $2 }' > "+dire+alg+"/summaries/result"+metric+"_Top_"+sparsity+"given_all.ALL" 
                    subprocess.call(cmd, shell=True)
            print time.strftime("%Y-%m-%d %H:%M") + " "+ alg + " completed."
    print time.strftime("%Y-%m-%d %H:%M") + " All result completed."

##   CreateCSV to execute statistical test
def createCSVOLD(metrics, allalg, allalgWEKA):
    for metric in metrics: 
        for alg in allalg:
            if (alg!="CFSubsetEval"):
    #            cmd = "java -cp lodrecsys.jar di.uniba.it.lodrecsys.utils.MakerCSV "+alg+" "+metric+" baseline"
                cmd = "java -cp lodrecsys.jar di.uniba.it.lodrecsys.utils.MakerCSV "+alg+" "+metric + " all"
                print cmd
                subprocess.call(cmd, shell=True)
                dire="./datasets/ml-100k/results/UserItemExpDBPedia/CSV/"+metric+"/"+alg+"/"
                cmd = "Rscript scriptRtest "+dire+" "+dire+"resultTest"
                subprocess.call(cmd, shell=True)
        
        for alg in allalgWEKA:
#            cmd = "java -cp lodrecsys.jar di.uniba.it.lodrecsys.utils.MakerCSV RankerWeka"+alg+" "+metric+" baseline"
            cmd = "java -cp lodrecsys.jar di.uniba.it.lodrecsys.utils.MakerCSV RankerWeka"+alg+" "+metric+ " all"
            print cmd
            subprocess.call(cmd, shell=True)
            dire="./datasets/ml-100k/results/UserItemExpDBPedia/CSV/"+metric+"/RankerWeka"+alg+"/"
            cmd = "Rscript scriptRtest "+dire+" "+dire+"resultTest"
            subprocess.call(cmd, shell=True)

##   CreateCSV to execute statistical test
def createCSV(topN, metrics, allalg, allalgWEKA):
    givenN=["given_5","given_20","given_all"]
    valMetrics=["5","10","20"]
    allAlg=""
    for algs in allalg:
        allAlg+=algs+" "
    for algs in allalgWEKA:
        allAlg+="RankerWeka"+algs+" "
    for metric in metrics: 
        for top in topN:
            for valMetric in valMetrics:
                for given in givenN:
            #            cmd = "java -cp lodrecsys.jar di.uniba.it.lodrecsys.utils.MakerCSV "+alg+" "+metric+" baseline"
                    cmd = "java -cp lodrecsys.jar di.uniba.it.lodrecsys.utils.MakerCSV comparisonAlg "+top+" "+given+" "+valMetric+" "+metric+" "+ allAlg
                    subprocess.call(cmd, shell=True)
#                        dire="./datasets/ml-100k/results/UserItemExpDBPedia/CSV/"+metric+"/"+alg+"/"
#                        cmd = "Rscript scriptRtest "+dire+" "+dire+"resultTest"
#                        subprocess.call(cmd, shell=True)
    tops=""
    for top in topN:
        tops+=top+" "
    for metric in metrics: 
        for valMetric in valMetrics:
            for given in givenN:
                for alg in allalg:
            #            cmd = "java -cp lodrecsys.jar di.uniba.it.lodrecsys.utils.MakerCSV "+alg+" "+metric+" baseline"
                    cmd = "java -cp lodrecsys.jar di.uniba.it.lodrecsys.utils.MakerCSV comparisonFeatures "+alg+" "+given+" "+valMetric+" "+metric+" "+ tops
                    subprocess.call(cmd, shell=True)
#                        dire="./datasets/ml-100k/results/UserItemExpDBPedia/CSV/"+metric+"/"+alg+"/"
#                        cmd = "Rscript scriptRtest "+dire+" "+dire+"resultTest"
#                        subprocess.call(cmd, shell=True)

                for alg in allalgWEKA:
            #            cmd = "java -cp lodrecsys.jar di.uniba.it.lodrecsys.utils.MakerCSV "+alg+" "+metric+" baseline"
                    cmd = "java -cp lodrecsys.jar di.uniba.it.lodrecsys.utils.MakerCSV comparisonFeatures RankerWeka"+alg+" "+given+" "+valMetric+" "+metric+" "+ tops
                    subprocess.call(cmd, shell=True)
#                        dire="./datasets/ml-100k/results/UserItemExpDBPedia/CSV/"+metric+"/"+alg+"/"
#                        cmd = "Rscript scriptRtest "+dire+" "+dire+"resultTest"
#                        subprocess.call(cmd, shell=True)


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

##   Extract result from file
def extractResultALL(metric,elem,dire,alg):
    cmd = "grep \""+metric+"_"+elem+"=\" "+dire+alg+"/summaries/*.summaryALL >> "+dire+alg+"/summaries/res"+metric+elem+".sum1ALL"
    subprocess.call(cmd, shell=True)
    cmd ="sed -i 's\ "+dire+alg+"/summaries/given_\ \ ' "+dire+alg+"/summaries/res"+metric+elem+".sum1ALL"
    subprocess.call(cmd, shell=True)
    cmd ="cat "+dire+alg+"/summaries/res"+metric+elem+".sum1ALL | awk 'BEGIN { FS = \"given_\"};{ print $2 }'| uniq > "+dire+alg+"/summaries/res"+metric+elem+".sumALL"
    subprocess.call(cmd, shell=True)
    cmd ="rm "+dire+alg+"/summaries/res"+metric+elem+".sum1ALL"
    subprocess.call(cmd, shell=True)
    cmd ="sed -i 's\.summaryALL:\ \ ' "+dire+alg+"/summaries/res"+metric+elem+".sumALL"
    subprocess.call(cmd, shell=True)
    cmd ="sed -i 's\\"+metric+"_"+elem+"=\ \ ' "+dire+alg+"/summaries/res"+metric+elem+".sumALL"
    subprocess.call(cmd, shell=True)
    cmd ="mv "+dire+alg+"/summaries/res"+metric+elem+".sumALL "+dire+alg+"/summaries/res"+metric+elem+".sumALLTEMP"
    subprocess.call(cmd, shell=True)
    cmd ="cat "+dire+alg+"/summaries/res"+metric+elem+".sumALLTEMP | awk 'BEGIN { FS = \" \"};{ print $1 (\" \") $3 }' | uniq > "+dire+alg+"/summaries/res"+metric+elem+".sumALL"
    subprocess.call(cmd, shell=True)
    cmd ="rm "+dire+alg+"/summaries/res"+metric+elem+".sumALLTEMP"
    subprocess.call(cmd, shell=True)
    cmd ="sed -i 's\\all\\100\ ' "+dire+alg+"/summaries/res"+metric+elem+".sumALL"
    subprocess.call(cmd, shell=True)
    cmd ="sed -i 's\\ 0.\\ 0,\ ' "+dire+alg+"/summaries/res"+metric+elem+".sumALL"
    subprocess.call(cmd, shell=True)
    cmd ="sed -i 's\\5 0\\005 0\ ' "+dire+alg+"/summaries/res"+metric+elem+".sumALL"
    subprocess.call(cmd, shell=True)
    cmd ="sed -i 's\\10 0\\010 0\ ' "+dire+alg+"/summaries/res"+metric+elem+".sumALL"
    subprocess.call(cmd, shell=True)
    cmd ="sed -i 's\\20 0\\020 0\ ' "+dire+alg+"/summaries/res"+metric+elem+".sumALL"
    subprocess.call(cmd, shell=True)
    cmd ="sed -i 's\\30 0\\030 0\ ' "+dire+alg+"/summaries/res"+metric+elem+".sumALL"
    subprocess.call(cmd, shell=True)
    cmd ="sed -i 's\\50 0\\050 0\ ' "+dire+alg+"/summaries/res"+metric+elem+".sumALL"
    subprocess.call(cmd, shell=True)
    cmd ="sed -i 's\\100 0\\100 0\ ' "+dire+alg+"/summaries/res"+metric+elem+".sumALL"
    subprocess.call(cmd, shell=True)
    cmd ="sort "+dire+alg+"/summaries/res"+metric+elem+".sumALL > "+dire+alg+"/summaries/result"+metric+elem+"ALL"
    subprocess.call(cmd, shell=True)
    cmd ="rm "+dire+alg+"/summaries/res"+metric+elem+".sumALL"
    subprocess.call(cmd, shell=True)
    cmd ="sed -i 's\\005 \\5 \ ' "+dire+alg+"/summaries/result"+metric+elem+"ALL"
    subprocess.call(cmd, shell=True)
    cmd ="sed -i 's\\010 \\10 \ ' "+dire+alg+"/summaries/result"+metric+elem+"ALL"
    subprocess.call(cmd, shell=True)
    cmd ="sed -i 's\\020 \\20 \ ' "+dire+alg+"/summaries/result"+metric+elem+"ALL"
    subprocess.call(cmd, shell=True)
    cmd ="sed -i 's\\030 \\30 \ ' "+dire+alg+"/summaries/result"+metric+elem+"ALL"
    subprocess.call(cmd, shell=True)
    cmd ="sed -i 's\\050 \\50 \ ' "+dire+alg+"/summaries/result"+metric+elem+"ALL"
    subprocess.call(cmd, shell=True)
