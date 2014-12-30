package di.uniba.it.lodrecsys.graph;

import di.uniba.it.lodrecsys.entity.MovieMapping;
import di.uniba.it.lodrecsys.entity.Pair;
import di.uniba.it.lodrecsys.entity.RequestStruct;
import di.uniba.it.lodrecsys.graph.featureSelection.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;

import static di.uniba.it.lodrecsys.utils.LoadProperties.*;

/**
 * Factory class which generates instances for specific graph
 * configuration
 */
public class GraphFactory {

    public static void createAllFeatureSelection(Object... params) throws IOException {
        FS graphFS;
        if (!new File("./mapping/FS/PageRank").exists()) {
            graphFS = new PageRank((String) params[0],
                    (String) params[1],
                    (String) params[2],
                    (List<MovieMapping>) params[3]
            );
            graphFS.run();
        } else
            System.out.println(new Date() + " [INFO] Feature Selection with PageRank already created.");

        System.out.println("----------------------------------------------------");
        if (!new File("./mapping/FS/HITS_AUTHORITY").exists()) {
            graphFS = new HITS_AUTHORITY((String) params[0],
                    (String) params[1],
                    (String) params[2],
                    (List<MovieMapping>) params[3]
            );
            graphFS.run();
        } else
            System.out.println(new Date() + " [INFO] Feature Selection with HITS score: authority already created.");
        System.out.println("----------------------------------------------------");
        if (!new File("./mapping/FS/HITS_HUB").exists()) {
            graphFS = new HITS_HUB((String) params[0],
                    (String) params[1],
                    (String) params[2],
                    (List<MovieMapping>) params[3]
            );
            graphFS.run();
        } else
            System.out.println(new Date() + " [INFO] Feature Selection with HITS score: hub already created.");
        System.out.println("----------------------------------------------------");
        if (!new File("./mapping/FS/MRMR").exists()) {
            graphFS = new MRMR((String) params[0],
                    (String) params[1],
                    (String) params[2],
                    (List<MovieMapping>) params[3]
            );
            graphFS.run();
        } else
            System.out.println(new Date() + " [INFO] Feature Selection with mRMR already created.");
        System.out.println("----------------------------------------------------");
        for (String s : LISTEVALWEKA) {
            if (!new File("./mapping/FS/RankerWeka" + s).exists()) {
                graphFS = new RankerWeka((String) params[0],
                        (String) params[1],
                        (String) params[2],
                        (List<MovieMapping>) params[3], s
                );
                graphFS.run();
            } else
                System.out.println(new Date() + " [INFO] Feature Selection with Ranker and " + s + " already created.");
            System.out.println("----------------------------------------------------");
        }
    }

    public static void existingFile() throws IOException {
        FileOutputStream fout = new FileOutputStream("./mapping/choosen_prop");
        PrintWriter out = new PrintWriter(fout);
        String fileName;
        if (FILTERTYPE.equals("RankerWeka")) {
            fileName = "./mapping/FS/" + FILTERTYPE + EVALWEKA;
            List<String> ranks = Files.readAllLines(Paths.get(fileName),
                    Charset.defaultCharset());

            for (int i = 0; i < Integer.parseInt(NUMFILTER); i++)
                out.println(ranks.get(i));

        } else {
            fileName = "./mapping/FS/" + FILTERTYPE;
            List<String> ranks = Files.readAllLines(Paths.get(fileName),
                    Charset.defaultCharset());
            for (int i = 0; i < Integer.parseInt(NUMFILTER); i++)
                out.println(ranks.get(i).split(" ")[1]);
        }
        out.close();
        fout.close();
    }

    public static void createSubsetFeature(String type, Object... params) throws IOException {
        FS graphFS;
        switch (type) {
            case "PageRank":
                if (!new File("./mapping/FS/" + type).exists()) {
                    graphFS = new PageRank((String) params[0],
                            (String) params[1],
                            (String) params[2],
                            (List<MovieMapping>) params[3]
                    );
                    graphFS.run();
                } else
                    System.out.println(new Date() + " [INFO] Feature Selection with " + FILTERTYPE + " already created.");
                break;

            case "HITS_AUTHORITY":
                if (!new File("./mapping/FS/" + type).exists()) {
                    graphFS = new HITS_AUTHORITY((String) params[0],
                            (String) params[1],
                            (String) params[2],
                            (List<MovieMapping>) params[3]
                    );
                    graphFS.run();
                } else
                    System.out.println(new Date() + " [INFO] Feature Selection with " + FILTERTYPE + " already created.");
                break;

            case "HITS_HUB":
                if (!new File("./mapping/FS/" + type).exists()) {
                    graphFS = new HITS_HUB((String) params[0],
                            (String) params[1],
                            (String) params[2],
                            (List<MovieMapping>) params[3]
                    );
                    graphFS.run();
                } else
                    System.out.println(new Date() + " [INFO] Feature Selection with " + FILTERTYPE + " already created.");
                break;

            case "MRMR":
                if (!new File("./mapping/FS/" + type).exists()) {
                    graphFS = new MRMR((String) params[0],
                            (String) params[1],
                            (String) params[2],
                            (List<MovieMapping>) params[3]
                    );
                    graphFS.run();
                } else
                    System.out.println(new Date() + " [INFO] Feature Selection with " + FILTERTYPE + " already created.");
                break;

            case "RankerWeka":
                if (!new File("./mapping/FS/" + type + EVALWEKA).exists()) {
                    graphFS = new RankerWeka((String) params[0],
                            (String) params[1],
                            (String) params[2],
                            (List<MovieMapping>) params[3], EVALWEKA
                    );
                    graphFS.run();
                } else
                    System.out.println(new Date() + " [INFO] Feature Selection with Ranker and " + EVALWEKA + " already created.");
                break;
        }
    }

    public static Pair<RecGraph, RequestStruct> create(String specificModel, Object... params) throws IOException {
        RecGraph graph = null;
        RequestStruct requestStruct = null;

        switch (specificModel) {
//            case "UserItemGraph":
//                graph = new UserItemGraph((String) params[0], (String) params[1]);
//                requestStruct = RequestStructFactory.create(specificModel);
//                break;
//            case "UserItemPriorGraph":
//                graph = new UserItemPriorGraph((String) params[0], (String) params[1]);
//                requestStruct = RequestStructFactory.create(specificModel, (double) params[2]);
//                break;
//
//            case "UserItemProperty":
//                graph = new UserItemProperty((String) params[0],
//                        (String) params[1],
//                        (String) params[3],
//                        (List<MovieMapping>) params[4]);
//                requestStruct = RequestStructFactory.create(specificModel, (double) params[2]);
//                break;
//
//            case "UserItemPropTag":
//                graph = new UserItemPropTag((String) params[0],
//                        (String) params[1],
//                        (String) params[3],
//                        (List<MovieMapping>) params[4],
//                        (Map<String, List<String>>) params[5]);
//                requestStruct = RequestStructFactory.create(specificModel, (double) params[2]);
//                break;
//            case "UserItemTag":
//                graph = new UserItemTag((String) params[0],
//                        (String) params[1],
//                        (List<MovieMapping>) params[4],
//                        (Map<String, List<String>>) params[5]);
//                requestStruct = RequestStructFactory.create(specificModel, (double) params[2]);
//                break;

            case "UserItemExpDBPedia":
                graph = new UserItemExpDBPedia((String) params[0],
                        (String) params[1],
                        (String) params[3],
                        (List<MovieMapping>) params[4]
                );
                requestStruct = RequestStructFactory.create(specificModel, (double) params[2]);
                break;

//            case "UserItemOneExp":
//                graph = new UserItemOneExp((String) params[0],
//                        (String) params[1],
//                        (String) params[3],
//                        (List<MovieMapping>) params[4]
//                );
//                requestStruct = RequestStructFactory.create(specificModel, (double) params[2]);
//                break;
//            case "UserItemComplete":
//                graph = new UserItemComplete((String) params[0],
//                        (String) params[1],
//                        (String) params[3],
//                        (List<MovieMapping>) params[4],
//                        (Map<String, List<String>>) params[5]
//                );
//                requestStruct = RequestStructFactory.create(specificModel, (double) params[2]);
//                break;
//
//            case "UserItemJaccardScore":
//                graph = new UserItemJaccardScore((String) params[0],
//                        (String) params[1],
//                        (String) params[3],
//                        (List<MovieMapping>) params[4]);
//                requestStruct = RequestStructFactory.create(specificModel, (double) params[2]);
//                break;
//
//            case "UserItemJaccard":
//                graph = new UserItemJaccard((String) params[0],
//                        (String) params[1],
//                        (String) params[3],
//                        (List<MovieMapping>) params[4]);
//                requestStruct = RequestStructFactory.create(specificModel, (double) params[2]);
//                break;
        }

        return new Pair<>(graph, requestStruct);


    }

}

/**
 * Constructs specific request needed by some specific
 * algorithm's configuration.
 */
class RequestStructFactory {
    public static RequestStruct create(String specificModel, Object... params) throws IOException {

        switch (specificModel) {
            case "UserItemGraph":
                return new RequestStruct();
            /*case "UserItemPriorGraph":
                return new RequestStruct((double) params[0]);
            case "UserItemProperty":
                return new RequestStruct((double) params[0]);
            case "UserItemPropTag":
                return new RequestStruct((double) params[0]);
            case "UserItemTag":
                return new RequestStruct((double) params[0]);
            case "UserItemOneExp":
                return new RequestStruct((double) params[0]);
            case "UserItemComplete":
                return new RequestStruct((double) params[0]);
            case "UserItemJaccardScore":
                return new RequestStruct((double) params[0]); */
            default:
                return new RequestStruct((double) params[0]);

        }


    }
}
