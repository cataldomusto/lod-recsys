package di.uniba.it.lodrecsys.utils;

import weka.core.Instances;
import weka.core.converters.CSVLoader;
import weka.core.converters.CSVSaver;
import weka.filters.Filter;
import weka.filters.supervised.instance.StratifiedRemoveFolds;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by asuglia on 3/31/14.
 */
public class ResampleDataset {
    private static Logger currLogger = Logger.getLogger(ResampleDataset.class.getName());

    public static void main(String[] args) throws Exception {
        if(args.length == 3) {
            stratifiedSplitting(new File(args[0]), new File(args[1]), new File(args[2]));
        } else {
            currLogger.log(Level.SEVERE, "Missing command line arguments.\n usage: $program_name ORIG_DATASET_FILE TRAINSET_NAME TESTSET_NAME");
        }
    }

    public static void stratifiedSplitting(File origDataset, File trainSplit, File testSplit) throws Exception {
        // Reads the original dataset from a csv/tsv file
        CSVLoader loader = new CSVLoader();
        loader.setFile(origDataset);
        Instances dataset = loader.getDataSet();
        CSVSaver saver = new CSVSaver();
        dataset.setClassIndex(2);

        // Applies a stratification on the current dataset
        StratifiedRemoveFolds strat = new StratifiedRemoveFolds();
        strat.setNumFolds(2);
        strat.setInputFormat(dataset);

        Instances stratified = Filter.useFilter(dataset, strat);

        // Splits the stratified dataset into two different folds
        double percent = 70.0;
        int trainSize = (int) Math.round(stratified.numInstances() * percent / 100);
        int testSize = stratified.numInstances() - trainSize;
        Instances train = new Instances(stratified, 0, trainSize);
        Instances test = new Instances(stratified, trainSize, testSize);

        train.setClassIndex(2);
        test.setClassIndex(2);

        // Saves the two folds in two different files
        saver.setFile(trainSplit);
        saver.setInstances(train);
        saver.writeBatch();

        saver.setFile(testSplit);
        saver.setInstances(test);
        saver.writeBatch();
    }

}
