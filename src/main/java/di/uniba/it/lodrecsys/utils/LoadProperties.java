package di.uniba.it.lodrecsys.utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Created by simo on 21/12/14.
 */
public class LoadProperties {
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

            DBPEDIAITEMSFILE = prop.getProperty("dbpediaItemsFile");
            DBPEDIAMAPPING = prop.getProperty("dbpediaMapping");


        } catch (IOException e) {
            e.printStackTrace();
        }

        LISTRECSIZES = new int[]{5, 10, 15, 20};
        NUMSPLIT = 5;
        MASSPROB = 0.8;
    }
}
