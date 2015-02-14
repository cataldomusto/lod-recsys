package di.uniba.it.lodrecsys.utils;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by simo on 14/02/15.
 */
public class MakerANOVATest {
    public static void main(String[] args) {
        if (args[0].equals("comparisonAlg")) {
            ArrayList<String> algorithms = new ArrayList<>(9);
            algorithms.addAll(Arrays.asList(args).subList(1, args.length));
            try {
                comparisonAlgANOVA(algorithms);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (args[0].equals("comparisonFeatures")) {
            ArrayList<String> tops = new ArrayList<>(5);
            tops.addAll(Arrays.asList(args).subList(1, args.length));
            try {
                comparisonFeaturesANOVA(tops);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    private static void comparisonFeaturesANOVA(ArrayList<String> tops) throws IOException {
        new File("./scripts/RcomparisonFeaturesANOVA").delete();
        String pathWriter = "./scripts/RcomparisonFeaturesANOVA";
        PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(pathWriter, true)));
        out.append("#!/usr/bin/env Rscript").append("\n");
        out.append("args <- commandArgs(trailingOnly = TRUE)").append("\n");
        out.append("sink(args[2])").append("\n");
        out.append("library(car)").append("\n");
        out.append("options(contrasts=c(\"contr.sum\",\"contr.poly\"))").append("\n");
        out.append("temp = list.files(path = args[1], pattern=\"*.csv\",full.names=TRUE)").append("\n");
        out.append("for (j in 1:length(temp)) {").append("\n");
        out.append("mydata = read.csv(temp[j])").append("\n");
        out.append("attach(mydata)").append("\n");
        out.append("multimodel=lm(cbind(");
        for (int i = 0; i < tops.size() - 1; i++)
            out.append(tops.get(i)).append(",");
        out.append(tops.get(tops.size() - 1)).append(") ~ 1)").append("\n");
        out.append("Trials=factor(c(");
        for (int i = 0; i < tops.size() - 1; i++)
            out.append("\"").append(tops.get(i)).append("\",");
        out.append("\"").append(tops.get(tops.size() - 1)).append("\"), ordered = F)").append("\n");

        out.append("model1=Anova(multimodel,idata=data.frame(Trials),idesign=~Trials,type=\"III\")").append("\n");
        out.append("print(temp[j])").append("\n");
        out.append("print(summary(model1,multivariate=F))").append("\n");
        out.append("detach(mydata)").append("\n");
        out.append("}").append("\n");
        out.close();

//          temp = list.files(path = args[1], pattern="*.csv",full.names=TRUE)
//        for (j in 1:length(temp)) {
//            mydata=read.csv(temp[j])
//            print(temp[j])
//            print(mydata)
//            #    print(friedman.test(as.matrix(mydata)))
//            print(friedman.test(as.matrix(mydata)))
//        }
    }

    private static void comparisonAlgANOVA(ArrayList<String> algorithms) throws IOException {

        new File("./scripts/RcomparisonAlgANOVA").delete();
        String pathWriter = "./scripts/RcomparisonAlgANOVA";
        PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(pathWriter, true)));
        out.append("#!/usr/bin/env Rscript").append("\n");
        out.append("args <- commandArgs(trailingOnly = TRUE)").append("\n");
        out.append("sink(args[2])").append("\n");
        out.append("library(car)").append("\n");
        out.append("options(contrasts=c(\"contr.sum\",\"contr.poly\"))").append("\n");
        out.append("temp = list.files(path = args[1], pattern=\"*.csv\",full.names=TRUE)").append("\n");
        out.append("for (j in 1:length(temp)) {").append("\n");
        out.append("mydata = read.csv(temp[j])").append("\n");
        out.append("attach(mydata)").append("\n");
        out.append("multimodel=lm(cbind(");
        for (int i = 0; i < algorithms.size() - 1; i++)
            out.append(algorithms.get(i)).append(",");
        out.append(algorithms.get(algorithms.size() - 1)).append(") ~ 1)").append("\n");
        out.append("Trials=factor(c(");
        for (int i = 0; i < algorithms.size() - 1; i++)
            out.append("\"").append(algorithms.get(i)).append("\",");
        out.append("\"").append(algorithms.get(algorithms.size() - 1)).append("\"), ordered = F)").append("\n");

        out.append("model1=Anova(multimodel,idata=data.frame(Trials),idesign=~Trials,type=\"III\")").append("\n");
        out.append("print(temp[j])").append("\n");
        out.append("print(summary(model1,multivariate=F))").append("\n");
        out.append("detach(mydata)").append("\n");
        out.append("}").append("\n");
        out.close();

//          temp = list.files(path = args[1], pattern="*.csv",full.names=TRUE)
//        for (j in 1:length(temp)) {
//            mydata=read.csv(temp[j])
//            print(temp[j])
//            print(mydata)
//            #    print(friedman.test(as.matrix(mydata)))
//            print(friedman.test(as.matrix(mydata)))
//        }

    }
}
