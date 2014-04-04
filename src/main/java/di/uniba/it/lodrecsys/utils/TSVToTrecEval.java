package di.uniba.it.lodrecsys.utils;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.*;
import java.util.logging.Logger;

/**
 * Created by asuglia on 4/4/14.
 */
public class TSVToTrecEval {

    private static final Logger currLogger = Logger.getLogger(TSVToTrecEval.class.getName());

    /**
     * Converts the current test set which contains only binary ratings
     * into a format useful for TrecEval which has this struture:
     * <p/>
     * TREC_EVAL RATING FORMAT: <id_user> 0 <id_item> <binary_rating>
     * <p/>
     * args[0] -> dataset filename
     * args[1] -> converted dataset filename
     */

    public static void main(String args[]) throws IOException {

        CSVParser parser = null;
        BufferedWriter writer = null;

        try {
            parser = new CSVParser(new FileReader(args[0]), CSVFormat.TDF);
            writer = new BufferedWriter(new FileWriter(args[1]));

            for (CSVRecord currLine : parser.getRecords()) {
                String line = currLine.get(0) + " " + "0" + " " + currLine.get(1) + " " + currLine.get(2);
                writer.write(line);
                writer.newLine();

            }
        } catch (IOException ex) {
            currLogger.severe(ex.getMessage());

        } finally {
            if (parser != null && !parser.isClosed()) {
                parser.close();
            }

            if (writer != null) {
                writer.close();
            }


        }
    }

}
