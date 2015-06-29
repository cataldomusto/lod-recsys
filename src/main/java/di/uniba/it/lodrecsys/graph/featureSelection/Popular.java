package di.uniba.it.lodrecsys.graph.featureSelection;

import di.uniba.it.lodrecsys.entity.MovieMapping;
import di.uniba.it.lodrecsys.graph.Edge;
import di.uniba.it.lodrecsys.graph.VertexScored;
import di.uniba.it.lodrecsys.utils.GraphToMatrix;
import di.uniba.it.lodrecsys.utils.LoadProperties;
import weka.core.Instances;
import weka.core.converters.ArffLoader;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

import static di.uniba.it.lodrecsys.graph.GraphRecRun.savefileLog;

/**
 * Created by Simone Rutigliano on 29/06/15.
 */
public class Popular extends FS {

    class PopularItem implements Comparable {
        String nameProp;
        int count;

        public PopularItem(String nameProp, int count) {
            this.nameProp = nameProp;
            this.count = count;
        }

        @Override
        public int compareTo(Object o) {
            if (this.count > ((PopularItem) o).count)
                return -1;
            if (this.count < ((PopularItem) o).count)
                return 1;
            return 0;
        }
    }

    public Popular(String trainingFileName, String testFile, String proprIndexDir, List<MovieMapping> mappedItems) throws IOException {
        super(trainingFileName, testFile, proprIndexDir, mappedItems);
    }

    public void run() throws IOException {
        savefileLog(new Date() + " [INFO] Feature Selection with Popular inizialized.");

        new File(LoadProperties.MAPPINGPATH + "/FS").mkdirs();
        FileOutputStream fout = new FileOutputStream(LoadProperties.MAPPINGPATH + "/FS/Popular");
        PrintWriter out = new PrintWriter(fout);

        String dir = LoadProperties.MAPPINGPATH + "/all_prop";
        List<String> lines = null;
        try {
            lines = Files.readAllLines(Paths.get(dir),
                    Charset.defaultCharset());
        } catch (IOException e) {
            e.printStackTrace();
        }

        assert lines != null;
        HashMap<String, Integer> propCount = new HashMap<>(lines.size());
        for (String line : lines) {
            propCount.put(line, 0);
        }

        assert recGraph != null;
        Collection<Edge> recGraphEdges = recGraph.getEdges();

        for (Edge s : recGraphEdges) {
            if (propCount.containsKey(s.getProperty())) {
                int count = propCount.get(s.getProperty());
                count++;
                propCount.put(s.getProperty(), count);
            }
        }

        TreeSet<PopularItem> propOrdered = new TreeSet<>();
        for (String s : propCount.keySet()) {
            PopularItem p = new PopularItem(s, propCount.get(s));
            propOrdered.add(p);
        }

        for (PopularItem popularItem : propOrdered) {
            out.println(popularItem.count + " " + popularItem.nameProp);
        }

        out.close();
        fout.close();

        savefileLog(new Date() + " [INFO] Feature Selection with Popular Completed.");
        savefileLog("---------------------------------------------------");
    }

}
