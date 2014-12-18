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
