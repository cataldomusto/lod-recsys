feature:
	java -cp lodrecsys.jar di.uniba.it.lodrecsys.graph.GraphFSRun

recommendation1:
	java -cp lodrecsys.jar di.uniba.it.lodrecsys.graph.GraphRecRun given_5 &
	java -cp lodrecsys.jar di.uniba.it.lodrecsys.graph.GraphRecRun given_10 &
	java -cp lodrecsys.jar di.uniba.it.lodrecsys.graph.GraphRecRun given_20

recommendation: recommendation1
	java -cp lodrecsys.jar di.uniba.it.lodrecsys.graph.GraphRecRun given_30 &
	java -cp lodrecsys.jar di.uniba.it.lodrecsys.graph.GraphRecRun given_50 &
	java -cp lodrecsys.jar di.uniba.it.lodrecsys.graph.GraphRecRun given_all 

evaluation:
	java -cp lodrecsys.jar di.uniba.it.lodrecsys.graph.GraphEvalRun given_5 PageRank 30
	java -cp lodrecsys.jar di.uniba.it.lodrecsys.graph.GraphEvalRun given_10 PageRank 30
	java -cp lodrecsys.jar di.uniba.it.lodrecsys.graph.GraphEvalRun given_20 PageRank 30
	java -cp lodrecsys.jar di.uniba.it.lodrecsys.graph.GraphEvalRun given_30 PageRank 30
	java -cp lodrecsys.jar di.uniba.it.lodrecsys.graph.GraphEvalRun given_50 PageRank 30
	java -cp lodrecsys.jar di.uniba.it.lodrecsys.graph.GraphEvalRun given_all PageRank 30

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
