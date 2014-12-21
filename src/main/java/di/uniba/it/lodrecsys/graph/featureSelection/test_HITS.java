package di.uniba.it.lodrecsys.graph.featureSelection;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;

public class test_HITS {

    public static void main(String[] args) throws IOException, NoSuchElementException {

        File file = new File("file.txt");
        String url1 = "http://url1.com";
        String url2 = "http://url2.com";
        String url3 = "http://url3.com";
        String url4 = "http://url4.com";

        Map hubScores = new HashMap();
        Map authorityScores = new HashMap();

        WebGraph graph = new WebGraph(file);

        int numLinks = graph.numNodes();
        for (int i = 0; i < numLinks; i++) {
            hubScores.put(new Integer(i), new Double(1));
            authorityScores.put(new Integer(i), new Double(1));
        }


        //HITS hits_test = new HITS(graph);

        System.out.println(graph.numNodes());

        int numIterator = 10;
        for (int i = 0; i < graph.numNodes(); i++) {
            Map inlinks = graph.inLinks(i);
            Map outlinks = graph.outLinks(i);
            Iterator inIter = inlinks.keySet().iterator();
            Iterator outIter = outlinks.keySet().iterator();

            double authorityScore = 0;
            double hubScore = 0;

            //System.out.println(inlinks.size());
            //System.out.println(outlinks.size());
            while (inIter.hasNext()) {
                authorityScore += ((Double) (hubScores.get((Integer) (inIter.next())))).doubleValue();

            }
            /*
            while (outIter.hasNext()) {
					//hubScore += ((Double)(authorityScores.get((Integer)(outIter.next())))).doubleValue();
					System.out.println("test");
				}
			*/
        }
    }
}