sperimentation:
	./scripts/sperimRunner

serverSperimentation:
	nohup ./scripts/sperimRunner &

cleanAll:
	./scripts/clearAll

cleanSum:
	./scripts/clearSum

serverStoredProp:
	nohup java -cp lodrecsys.jar di.uniba.it.lodrecsys.utils.mapping.PropertiesGenerator &

storedProp:
	java -cp lodrecsys.jar di.uniba.it.lodrecsys.utils.mapping.PropertiesGenerator

feature:
	java -cp lodrecsys.jar di.uniba.it.lodrecsys.graph.GraphFSRun CFSubsetEval

recommendation1:
	java -cp lodrecsys.jar di.uniba.it.lodrecsys.graph.GraphRecRun given_5 CFSubsetEval &
	java -cp lodrecsys.jar di.uniba.it.lodrecsys.graph.GraphRecRun given_10 CFSubsetEval &
	java -cp lodrecsys.jar di.uniba.it.lodrecsys.graph.GraphRecRun given_20 CFSubsetEval

recommendation: recommendation1
	java -cp lodrecsys.jar di.uniba.it.lodrecsys.graph.GraphRecRun given_30 CFSubsetEval &
	java -cp lodrecsys.jar di.uniba.it.lodrecsys.graph.GraphRecRun given_50 CFSubsetEval &
	java -cp lodrecsys.jar di.uniba.it.lodrecsys.graph.GraphRecRun given_all CFSubsetEval

evaluation:
	java -cp lodrecsys.jar di.uniba.it.lodrecsys.graph.GraphEvalRun given_5 PageRank 5
	java -cp lodrecsys.jar di.uniba.it.lodrecsys.graph.GraphEvalRun given_10 PageRank 5 
	java -cp lodrecsys.jar di.uniba.it.lodrecsys.graph.GraphEvalRun given_20 PageRank 5 
	java -cp lodrecsys.jar di.uniba.it.lodrecsys.graph.GraphEvalRun given_30 PageRank 5 
	java -cp lodrecsys.jar di.uniba.it.lodrecsys.graph.GraphEvalRun given_50 PageRank 5 
	java -cp lodrecsys.jar di.uniba.it.lodrecsys.graph.GraphEvalRun given_all PageRank 5 

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
