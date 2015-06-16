#!/usr/bin/python
import time
import subprocess
import os

# inserire nel file "all_prop" tutte le proprieta da estrarre
# inserire nel file "resourcesURI" tutte le risorse di cui si vogliono estrarre le proprieta

# inserire il parametro "all" alla classe PropertiesGenerator per estrarre tutte le proprieta "all_prop" di tutte le "resourcesURI"
# altrimenti verranno estratte le "all_prop" delle risorse presenti nel file "missed"

def extractProperties(datasets):
	for dataset in datasets:
		if dataset == "movielens":
			dirProp = "datasets/ml-100k/stored_prop"
		elif dataset == "dbbooks":
			dirProp = "datasets/books-8k/stored_prop"
		else:
			dirProp = "datasets/lastfm/stored_prop"
		if not (os.path.exists(dirProp)):
			cmd = "java -cp lodrecsys.jar di.uniba.it.lodrecsys.utils.mapping.PropertiesGenerator " + dataset + " all"
			subprocess.call(cmd, shell=True)
			print time.strftime("%Y-%m-%d %H:%M") + " " + dataset + " properties created."
		else:
			print time.strftime("%Y-%m-%d %H:%M") + " " + dataset + " properties loaded."
