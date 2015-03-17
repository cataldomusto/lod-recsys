package di.uniba.it.lodrecsys.utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Created by Simone Rutigliano on 21/12/14.
 */
public class LoadProperties {
    public static String FILTERTYPE;
    public static String TRAINPATH;
    public static String TESTPATH;
    public static String TESTTRECPATH;
    public static String RESPATH;
    public static String PROPERTYINDEXDIR;
    public static String TAGMEDIR;
    public static String MAPPEDITEMFILE;
    public static String METHOD;
    public static int[] LISTRECSIZES;
    public static int NUMSPLIT;
    public static double MASSPROB;
    public static String CHOOSENPROP;
    public static String MISSEDPROP;
    public static String ITEMFILE;
    public static String DBPEDIAITEMSFILE;
    public static String DBPEDIAMAPPING;
    public static String RATINGFILE;
    public static String NUMFILTER;
    public static String EVALWEKA;
    public static String[] LISTEVALWEKA;
    public static String DATASETPATH;
    public static String MAPPINGPATH;

    static {
        Properties prop = new Properties();
        try {
            prop.load(new FileInputStream("properties/my.properties"));
            TRAINPATH = prop.getProperty("trainPath");
            TESTPATH = prop.getProperty("testPath");
            TESTTRECPATH = prop.getProperty("testTrecPath");
            RESPATH = prop.getProperty("resPath");
            PROPERTYINDEXDIR = prop.getProperty("propertyIndexDir");
            TAGMEDIR = prop.getProperty("tagmeDir");
            MAPPEDITEMFILE = prop.getProperty("mappedItemFile");
            METHOD = prop.getProperty("methodName");
            CHOOSENPROP = prop.getProperty("choosenProp");
            MISSEDPROP = prop.getProperty("missedProp");
            ITEMFILE = prop.getProperty("itemFile");
            DATASETPATH= prop.getProperty("datasetPath");
            MAPPINGPATH= prop.getProperty("mappingPath");
            DBPEDIAITEMSFILE = prop.getProperty("dbpediaItemsFile");
            DBPEDIAMAPPING = prop.getProperty("dbpediaMapping");
            RATINGFILE = prop.getProperty("ratingFile");
            FILTERTYPE = prop.getProperty("filterType");
            NUMFILTER = prop.getProperty("numFilter");
            EVALWEKA = prop.getProperty("evalWeka");
            NUMSPLIT = Integer.parseInt(prop.getProperty("numSplit"));

        } catch (IOException e) {
            e.printStackTrace();
        }

        LISTRECSIZES = new int[]{
                5,
                10,
                15,
                20,
                30,
                50
        };

        LISTEVALWEKA = new String[]{
                "ChiSquaredAttributeEval",
                "InfoGainAttributeEval",
                "GainRatioAttributeEval",
                "PCA",
                "ReliefFAttributeEval",
                "SVMAttributeEval"
        };
        MASSPROB = 0.8;
    }
}
