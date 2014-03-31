package di.uniba.it.lodrecsys.utils;

import weka.core.Instances;
import weka.core.converters.CSVLoader;
import weka.core.converters.CSVSaver;

import java.io.File;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by asuglia on 3/31/14.
 */
public class ResampleDataset {
    private static Logger currLogger = Logger.getLogger(ResampleDataset.class.getName());

    public static void main(String[] args) throws Exception {
        if(args.length == 3) {
            CSVLoader loader = new CSVLoader();
            loader.setFile(new File(args[0]));
            Instances dataset = loader.getDataSet();
            CSVSaver saver = new CSVSaver();
            //11.evaluate
            //resample if needed
            dataset = dataset.resample(new Random(100));
            //split to 70:30 learn and test set
            double percent = 70.0;
            int trainSize = (int) Math.round(dataset.numInstances() * percent / 100);
            int testSize = dataset.numInstances() - trainSize;
            Instances train = new Instances(dataset, 0, trainSize);
            Instances test = new Instances(dataset, trainSize, testSize);
            train.setClassIndex(2);
            test.setClassIndex(2);

            saver.setFile(new File(args[1]));
            saver.setInstances(train);
            saver.writeBatch();

            saver.setFile(new File(args[2]));
            saver.setInstances(test);
            saver.writeBatch();


        } else {
            currLogger.log(Level.SEVERE, "Missing command line arguments.\n usage: $program_name ORIG_DATASET_FILE TRAINSET_NAME TESTSET_NAME");
        }
    }
}
