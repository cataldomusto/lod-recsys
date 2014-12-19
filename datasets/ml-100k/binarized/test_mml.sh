#!/bin/bash

rec_methods="UserKNN ItemKNN"
#for method in $rec_methods
#do
	for index in {1..5}
	do
		echo "Test for the split "$index
		item_recommendation --training-file=u$index.base --test-file=u$index.test --recommender=ItemKNN --in-test-items --predict-items-number=10 --measures=prec@5,prec@10
	done
#done