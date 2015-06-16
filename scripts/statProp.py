#!/usr/bin/python
import time
import subprocess

# inserire nel file "all_prop" tutte le proprieta da estrarre
# output: mapping/*datasetname*/stat indicante la frequenza di ogni proprieta estratta dalle resoource

def extractPropertiesStat(datasets):
    for dataset in datasets:
        splitting = range(0, 8)
        for count in splitting:
            cmd = "java -cp lodrecsys.jar di.uniba.it.lodrecsys.utils.mapping.MappingStats " + dataset + " " + str(
                count) + " &"
            subprocess.call(cmd, shell=True)
        print time.strftime("%Y-%m-%d %H:%M") + " " + dataset + " properties created."
