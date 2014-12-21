package di.uniba.it.lodrecsys.utils;

import de.l3s.boilerpipe.BoilerpipeProcessingException;
import de.l3s.boilerpipe.extractors.ArticleExtractor;
import di.uniba.it.lodrecsys.utils.mapping.SPARQLClient;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Another utility class used in order to download Wikipedia pages associated
 * to the DBpedia entities
 */
public class DownloadEntityData {

    private static final Logger logger = Logger.getLogger(DownloadEntityData.class.getName());
    private static final int MAX_ATTEMPT = 3;

    public static void main(String[] args) throws IOException, BoilerpipeProcessingException, URISyntaxException {
        if (args.length == 2) {
            try {
                DownloadEntityData dd = new DownloadEntityData();
                dd.download(args[0], args[1]);
            } catch (IOException ex) {
                Logger.getLogger(DownloadEntityData.class.getName()).log(Level.SEVERE, "Error in downloading data", ex);
            }
        } else {
            logger.log(Level.WARNING, "Number of arguments not valid {0}", args.length);
            System.exit(1);
        }


    }

    private void download(String startFile, String outputDir) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(startFile));

        int numPages = 1;

        SPARQLClient client = new SPARQLClient();
        while (reader.ready()) {
            if (numPages % 50 == 0) {
                //dirty solution to limit request to wikipedia
                logger.info("Waiting for 30 seconds...");
                myWait(30);
            }

            String line = reader.readLine();
            String[] lineValues = line.split("\t");

            // remove not mapped items
            if (!lineValues[2].equals("null")) {
                logger.log(Level.INFO, "Processing {0}", lineValues[0]);
                logger.log(Level.INFO, "Make dir");
                String bookDirname = outputDir + "/" + lineValues[0];
                new File(bookDirname).mkdir();

                String wikiURI = client.getWikipediaURI(lineValues[2]);
                String text = null;

                if (wikiURI != null) {
                    numPages++;
                    int t = 0;
                    while (text == null && t < MAX_ATTEMPT) {

                        try {

                            text = ArticleExtractor.getInstance().getText(new URL(wikiURI));
                            if (text != null) {
                                if (text.contains("[ edit ]")) {
                                    text = text.replace("[ edit ]", "");
                                }
                            } else {
                                text = client.getResourceAbstract(lineValues[2]);
                            }
                        } catch (BoilerpipeProcessingException ex) {
                            Logger.getLogger(DownloadEntityData.class.getName()).log(Level.WARNING, "Error to extract text", ex);
                            logger.warning("Re-try downloading...");
                            myWait(15);
                            t++;
                        }
                    }
                    if (text == null) {
                        logger.log(Level.WARNING, "No text for {0}", lineValues[0]);
                        text = "";
                    }
                } else {
                    text = client.getResourceAbstract(lineValues[2]);
                }

                if (text == null) {
                    text = "";
                }


                FileWriter writer = new FileWriter(bookDirname + File.separator + lineValues[0] + ".text");
                writer.write(text);
                writer.close();
            }

        }
        reader.close();
    }

    private void myWait(int sec) {
        //wait
        logger.info("Ronf...");
        long cm = System.currentTimeMillis();
        while ((System.currentTimeMillis() - cm) < sec * 1000) {

        }
        logger.info("Wake-up!");
    }


}
