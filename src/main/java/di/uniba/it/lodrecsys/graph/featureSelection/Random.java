package di.uniba.it.lodrecsys.graph.featureSelection;

import di.uniba.it.lodrecsys.entity.MovieMapping;
import di.uniba.it.lodrecsys.utils.LoadProperties;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static di.uniba.it.lodrecsys.graph.GraphRecRun.savefileLog;

/**
 * Created by Simone Rutigliano on 29/06/15.
 */
public class Random extends FS {

    public Random(String trainingFileName, String testFile, String proprIndexDir, List<MovieMapping> mappedItems) throws IOException {
        super(trainingFileName, testFile, proprIndexDir, mappedItems);
    }

    public void run() throws IOException {
        savefileLog(new Date() + " [INFO] Feature Selection with Random inizialized.");

        new File(LoadProperties.MAPPINGPATH + "/FS").mkdirs();
        FileOutputStream fout = new FileOutputStream(LoadProperties.MAPPINGPATH + "/FS/Random");
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
        Collections.shuffle(lines);

        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            out.println(i + " " + line);
        }
        out.close();
        fout.close();

        savefileLog(new Date() + " [INFO] Feature Selection with Random Completed.");
        savefileLog("---------------------------------------------------");
    }

}
