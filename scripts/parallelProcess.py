#!/usr/bin/python
import time
import subprocess
import sys
import os
import shutil
from time import gmtime, strftime, localtime
from datetime import datetime

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

