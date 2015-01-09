feature:
	java -cp lodrecsys.jar di.uniba.it.lodrecsys.graph.GraphFSRun RankerWeka 17 InfoGainAttributeEval

recommendation1:
	java -cp lodrecsys.jar di.uniba.it.lodrecsys.graph.GraphRecRun given_5 RankerWeka 17 InfoGainAttributeEval &
	java -cp lodrecsys.jar di.uniba.it.lodrecsys.graph.GraphRecRun given_10 RankerWeka 17 InfoGainAttributeEval &
	java -cp lodrecsys.jar di.uniba.it.lodrecsys.graph.GraphRecRun given_20 RankerWeka 17 InfoGainAttributeEval

recommendation:
	java -cp lodrecsys.jar di.uniba.it.lodrecsys.graph.GraphRecRun given_30 RankerWeka 17 InfoGainAttributeEval &
	java -cp lodrecsys.jar di.uniba.it.lodrecsys.graph.GraphRecRun given_50 RankerWeka 17 InfoGainAttributeEval &
	java -cp lodrecsys.jar di.uniba.it.lodrecsys.graph.GraphRecRun given_all RankerWeka 17 InfoGainAttributeEval

evaluation:
	java -cp lodrecsys.jar di.uniba.it.lodrecsys.graph.GraphEvalRun given_5 RankerWeka 17 InfoGainAttributeEval
	java -cp lodrecsys.jar di.uniba.it.lodrecsys.graph.GraphEvalRun given_10 RankerWeka 17 InfoGainAttributeEval
	java -cp lodrecsys.jar di.uniba.it.lodrecsys.graph.GraphEvalRun given_20 RankerWeka 17 InfoGainAttributeEval
	java -cp lodrecsys.jar di.uniba.it.lodrecsys.graph.GraphEvalRun given_30 RankerWeka 17 InfoGainAttributeEval
	java -cp lodrecsys.jar di.uniba.it.lodrecsys.graph.GraphEvalRun given_50 RankerWeka 17 InfoGainAttributeEval
	java -cp lodrecsys.jar di.uniba.it.lodrecsys.graph.GraphEvalRun given_all RankerWeka 17 InfoGainAttributeEval

recsys: feature recommendation evaluation

all: prepare

prepare: clean compile test package

#run:
#	mvn -T 4 exec:java -Dexec.mainClass="it.uniba.di.ia.ius.Main"

#dataset:
#	mvn -T 4 exec:java -Dexec.mainClass="it.uniba.di.ia.ius.GeneratoreDataset" -Dexec.args="$(numDoc)"

compile:
	mvn -T 4 compile

test:
	mvn -T 4 test

package:
	mvn -T 4 package

clean:
	mvn clean
