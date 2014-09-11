package di.uniba.it.lodrecsys.utils;

import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Uses the JSON RESTful API in order to use the TAG.me service.
 * For each DBpedia entity uses TAG.me in order to extract all the concepts
 * associated to the entities the Wikipedia main page or the dbpedia:abstract
 * property
 */
public class TagMEthem {
    private static Logger logger = Logger.getLogger(TagMEthem.class.getName());

    /**
     * For each item in the abstract directory, tags each document and
     * saves the tag.me result in the tag.me json directory
     * <p/>
     * args[0] - abstract directory
     * args[1] - tag.me json directory
     * args[2] - tag.me key
     *
     * @param args
     */
    public static void main(String[] args) throws IOException {
        if (args.length == 3) {
            String key = args[2];
            File abstractDir = new File(args[0]);
            File jsonDir = new File(args[1]);

            File[] listSubDir = abstractDir.listFiles(new FileFilter() {
                @Override
                public boolean accept(File pathname) {
                    return pathname.isDirectory();
                }
            });

            int i = 0;

            for (File subDir : listSubDir) {
                String itemID = subDir.getName(),
                        textFile = subDir.getCanonicalPath() + File.separator + itemID + ".text";
                try {
                    String content = fromStream(textFile);

                    logger.info("Tagging item " + itemID);
                    TagMeResult result = tagIt(content, key, true);
                    File annotationDir = new File(jsonDir + File.separator + itemID);
                    annotationDir.mkdir();
                    String annotationResultFile = annotationDir.getCanonicalPath() + File.separator + itemID + ".tagme";
                    serializeTagMeAnnotation(result.annotationList, annotationResultFile);

                    if (++i % 50 == 0)
                        myWait(5);
                } catch (IOException e) {
                    logger.info("Unmapped item! Go ahead...");
                }

            }

        }
    }

    private static void myWait(int sec) {
        //wait
        logger.info("Ronf...");
        long cm = System.currentTimeMillis();
        while ((System.currentTimeMillis() - cm) < sec * 1000) {

        }
        logger.info("Wake-up!");
    }

    public static void serializeTagMeAnnotation(List<Annotation> annotationList, String resultFile) throws IOException {
        BufferedWriter writer = null;

        try {
            writer = new BufferedWriter(new FileWriter(resultFile));

            for (Annotation annotation : annotationList) {
                // clean and valid annotation
                if (annotation.rho > 0.1) {
                    writer.write(annotation.id + "\t" + annotation.title + "\t");
                    writer.newLine();

                }


            }
        } finally {
            if (writer != null)
                writer.close();
        }

    }

    private static String fromStream(String textFile) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(textFile));
        StringBuilder out = new StringBuilder();
        String newLine = System.getProperty("line.separator");
        String line;
        while ((line = reader.readLine()) != null) {
            out.append(line);
            out.append(newLine);
        }

        reader.close();

        return out.toString();
    }

    /**
     * Returns the result struct obtained from the TAG.me service
     * used on a specific text
     *
     * @param text       The text that will be eleborated by TAG.me
     * @param key        Secret key for the TAG.me service
     * @param isLongText true if the text is long, false otherwise
     * @return All the annotations extracted from the text
     * @throws IOException if some errors occur while retrieving data
     */
    private static TagMeResult tagIt(String text, String key, boolean isLongText) throws IOException {
        List<NameValuePair> formparams = new ArrayList<>();
        formparams.add(new BasicNameValuePair("key", key));
        formparams.add(new BasicNameValuePair("text", text));
        formparams.add(new BasicNameValuePair("lang", "en"));
        if (isLongText)
            formparams.add(new BasicNameValuePair("long_text", "0"));

        UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formparams, Consts.UTF_8);
        HttpPost httppost = new HttpPost("http://tagme.di.unipi.it/tag");
        httppost.setEntity(entity);

        CloseableHttpClient httpclient = HttpClients.createDefault();
        CloseableHttpResponse response = httpclient.execute(httppost);
        try {
            HttpEntity resultEntity = response.getEntity();
            if (resultEntity != null) {

                ObjectMapper mapper = new ObjectMapper();
                return mapper.readValue(resultEntity.getContent(), TagMeResult.class);

            }
        } finally {
            response.close();
        }

        return null;
    }
}

class Annotation {
    @JsonProperty("id")
    public String id;

    @JsonProperty("title")
    public String title;

    @JsonProperty("start")
    public int startIndex;

    @JsonProperty("rho")
    public double rho;

    @JsonProperty("end")
    public int endIndex;

    @JsonProperty("spot")
    public String spot;

    @Override
    public String toString() {
        return "Annotation{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", startIndex=" + startIndex +
                ", rho=" + rho +
                ", endIndex=" + endIndex +
                ", spot='" + spot + '\'' +
                '}';
    }
}

class TagMeResult {
    @JsonProperty("timestamp")
    String timestamp;

    @JsonProperty("time")
    String timeString;

    @JsonProperty("api")
    String apiType;

    @JsonProperty("annotations")
    public List<Annotation> annotationList;

    @JsonProperty("lang")
    String language;

    @Override
    public String toString() {
        return "TagMeResult{" +
                "timestamp='" + timestamp + '\'' +
                ", timeString='" + timeString + '\'' +
                ", apiType='" + apiType + '\'' +
                ", annotationList=" + annotationList +
                ", language='" + language + '\'' +
                '}';
    }
}