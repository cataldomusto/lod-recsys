package di.uniba.it.lodrecsys.utils;

import di.uniba.it.lodrecsys.graph.Edge;
import edu.uci.ics.jung.graph.UndirectedGraph;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;

import java.io.*;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import static di.uniba.it.lodrecsys.graph.GraphRecRun.savefileLog;

/**
 * Created by simo on 25/12/14.
 */
public class GraphToMatrix {

    private static HashMap<String, Integer> getProperties() throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new FileReader("./mapping/all_prop"));
        String prop;
        int i = 1;
        HashMap<String, Integer> propertiesChoosen = new HashMap<>();
        while ((prop = bufferedReader.readLine()) != null) {
            propertiesChoosen.put(prop, i);
            i++;
        }
        bufferedReader.close();
        return propertiesChoosen;
    }

    public static String[][] convert(UndirectedGraph<String, Edge> recGraph) throws IOException {
        HashMap<String, Integer> properties = getProperties();

        Set<String> subj = new HashSet<>();
        for (Edge s : recGraph.getEdges()) {
            subj.add(s.getSubject());
        }

        String[][] matrixGraph = new String[subj.size() + 1][properties.size() + 1];

        int i = 1;

        for (String s : properties.keySet()) {
            matrixGraph[0][properties.get(s)] = s;
        }
        matrixGraph[0][0] = "Node";

        for (String s : subj) {
            matrixGraph[i][0] = s;
            for (Edge edge : recGraph.getIncidentEdges(s)) {
                if (edge.getSubject().equals(s))
                    matrixGraph[i][properties.get(edge.getProperty())] = edge.getObject();
            }
            i++;
        }

        return matrixGraph;
    }

    public static String[][] convertAdjacent(UndirectedGraph<String, Edge> recGraph) throws IOException {
        HashMap<String, Integer> properties = getProperties();

        Set<String> subj = new HashSet<>();
        for (Edge s : recGraph.getEdges()) {
            subj.add(s.getSubject());
        }

        String[][] matrixGraph = new String[subj.size() + 1][properties.size() + 1];

        int i = 1;

        for (String s : properties.keySet()) {
            matrixGraph[0][properties.get(s)] = s;
        }
        matrixGraph[0][0] = "Node";

        for (String s : subj) {
            matrixGraph[i][0] = s;
            for (Edge edge : recGraph.getIncidentEdges(s)) {
                if (edge.getSubject().equals(s))
                    matrixGraph[i][properties.get(edge.getProperty())] = "1";
            }
            i++;
        }

        for (int j = 1; j < subj.size() + 1; j++) {
            for (int k = 1; k < properties.size() + 1; k++) {
                if (matrixGraph[j][k] == null)
                    matrixGraph[j][k] = "0";
            }
        }

        return matrixGraph;
    }

    public static void convertARFFADJ(UndirectedGraph<String, Edge> recGraph) throws IOException {

        if (!new File("./serialized/graphAD.arff").exists()) {
            FileOutputStream fout = new FileOutputStream("./serialized/graphAD.arff");
            PrintWriter out = new PrintWriter(fout);

            String[][] matrixGraph = convertAdjacent(recGraph);

            FastVector atts;
            FastVector attVals;
            Instances data;
            double[] vals;

            // 1. set up attributes
            atts = new FastVector();

            // - nominal class
            attVals = new FastVector();
            for (int i = 1; i < matrixGraph.length; i++)
                attVals.addElement(matrixGraph[i][0]);
            atts.addElement(new Attribute("class", attVals));

            // - string attributes
            for (String s : getProperties().keySet()) {
                atts.addElement(new Attribute(s));
            }

            // 2. create Instances object
            data = new Instances("LODGraph", atts, 0);

            for (int i = 1; i < matrixGraph.length; i++) {

                // 3. fill with data
                // first instance
                vals = new double[data.numAttributes()];

                vals[0] = data.attribute(0).indexOfValue(matrixGraph[i][0]);
                for (int j = 1; j < data.numAttributes(); j++) {
                    if (matrixGraph[i][j] != null)
                        vals[j] = Integer.parseInt(matrixGraph[i][j]);
                    else
                        //vals[j] = data.attribute(j).addStringValue("null");
                        vals[j] = Integer.parseInt("0");
                }
                // add
                data.add(new Instance(1.0, vals));
            }
            // 4. output data
            out.println(data);
            out.close();
            fout.close();
            savefileLog(new Date() + " [INFO] ARFF created from graph.");
        } else savefileLog(new Date() + " [INFO] ARFF already created.");

    }
}
