package di.uniba.it.lodrecsys.utils.mapping;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

import java.io.*;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Logger;

/**
 * Created by asuglia on 7/6/14.
 */
public class CompleteTagME {
    private static Logger currLogger = Logger.getLogger(CompleteTagME.class.getName());


    public static void main(String[] args) throws IOException {
        if (args.length == 1) {
            String tagMe = args[0];

            File tagMeDir = new File(tagMe);

            File[] listSubDir = tagMeDir.listFiles(new FileFilter() {
                @Override
                public boolean accept(File pathname) {
                    return pathname.isDirectory();
                }
            });

            SPARQLClient client = new SPARQLClient();

            for (File subDir : listSubDir) {
                String itemID = subDir.getName(),
                        tagmeFile = subDir.getCanonicalPath() + File.separator + itemID + ".tagme";

                CSVParser parser = new CSVParser(new FileReader(tagmeFile), CSVFormat.TDF);

                Set<String> wikiID = new TreeSet<>();

                for (CSVRecord record : parser.getRecords()) {
                    wikiID.add(record.get(0));
                }

                parser.close();

                Map<String, String> wikiIDdbpedia = client.getURIfromWikiID(wikiID);

                serializeMap(wikiIDdbpedia, tagmeFile);
                currLogger.info("Serialized " + tagmeFile);


            }

        } else {
            currLogger.warning("Invalid number of arguments: <tagme_dir>");
        }

    }

    private static void serializeMap(Map<String, String> wikiMap, String tagmeFile) throws IOException {
        BufferedWriter printer = null;

        try {
            printer = new BufferedWriter(new FileWriter(tagmeFile));

            for (String wikiID : wikiMap.keySet()) {
                printer.write(wikiID + "\t" + wikiMap.get(wikiID));
                printer.newLine();
            }

        } finally {
            if (printer != null)
                printer.close();
        }

    }
}
