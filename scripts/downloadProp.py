#!/usr/bin/python
import time
import subprocess
import sys
import os
import shutil
from time import gmtime, strftime, localtime
from datetime import datetime

# inserire nel file "all_prop" tutte le proprieta da estrarre
# inserire nel file "resourcesURI" tutte le risorse di cui si vogliono estrarre le proprieta

# inserire il parametro "all" alla classe PropertiesGenerator per estrarre tutte le proprieta "all_prop" di tutte le "resourcesURI"
# altrimenti verranno estratte le "all_prop" delle risorse presenti nel file "missed"

def extractProperties(dirProp):
    if not(os.path.exists(dirProp)):
        cmd = "java -cp lodrecsys.jar di.uniba.it.lodrecsys.utils.mapping.PropertiesGenerator all"
        subprocess.call(cmd, shell=True)
        print time.strftime("%Y-%m-%d %H:%M") + " Properties created."
    else:    
        print time.strftime("%Y-%m-%d %H:%M") + " Properties loaded."

