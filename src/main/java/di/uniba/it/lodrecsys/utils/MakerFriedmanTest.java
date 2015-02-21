package di.uniba.it.lodrecsys.utils;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by simo on 14/02/15.
 */
public class MakerFriedmanTest {
    public static void main(String[] args) {
        if (args[0].equals("comparisonAlg")) {
            ArrayList<String> algorithms = new ArrayList<>(9);
            algorithms.addAll(Arrays.asList(args).subList(1, args.length));
            try {
                comparisonFriedman(algorithms, "Alg");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (args[0].equals("comparisonFeatures")) {
            ArrayList<String> tops = new ArrayList<>(5);
            for (int i = 1; i < args.length; i++)
                tops.add("Features" + args[i]);
            try {
                comparisonFriedman(tops, "Features");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (args[0].equals("comparisonBestBaseline")) {
            try {
                comparisonPairedTTest(args[1]);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    private static void comparisonPairedTTest(String best) throws IOException {
        new File("./scripts/RcomparisonPairedTTest").delete();
        String pathWriter = "./scripts/RcomparisonPairedTTest";
        PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(pathWriter, true)));
        out.append("#!/usr/bin/env Rscript \n" +
                "args <- commandArgs(trailingOnly = TRUE) \n" +
                "sink(args[2]) \n" +
                "temp = list.files(path = args[1], pattern=\"*.csv\",full.names=TRUE) \n" +
                "for (j in 1:length(temp)) { \n" +
                "    print(temp[j])\n" +
                "    mydata = read.csv(temp[j]) \n" +
                "    attach(mydata) \n" +
                "    normBase <- shapiro.test(Baseline)\n" +
                "    normBest <- shapiro.test(" + best + ")\n" +
                "    if ((normBase$p.value < 0.05) || (normBest$p.value < 0.05) ){ \n" +
                "        compair <- wilcox.test(Baseline, " + best + ", paired=T) \n" +
                "    } else\n" +
                "        compair <- t.test(Baseline, " + best + ", paired=T) \n" +
                "    if (compair$p.value < 0.05){\n" +
                "        means <- apply(mydata, 2, mean) # means factors\n" +
                "        maxMeans <- which.max(means)\n" +
                "        cat(paste(\"The best algorithm is \",names(mydata)[maxMeans],\"\\n\\n\"))\n" +
                "        #cat(paste(\"Max mean algorithm is \",names(mydata)[maxMeans],\": \", means[maxMeans],\"\\n\\n\"))\n" +
                "        cat(paste(compair$method,\"significative \\n\"))\n" +
                "    } else \n" +
                "        cat(paste(compair$method,\"not significative \\n\"))\n" +
                "    detach(mydata)\n" +
                "}");
        out.close();

    }

    private static void comparisonFriedman(ArrayList<String> comparison, String type) throws IOException {
        new File("./scripts/Rcomparison" + type + "Friedman").delete();
        String pathWriter = "./scripts/Rcomparison" + type + "Friedman";
        PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(pathWriter, true)));
        out.append("#!/usr/bin/env Rscript").append("\n");
        out.append("args <- commandArgs(trailingOnly = TRUE)").append("\n");
        out.append("sink(args[2])").append("\n");
        out.append("library(PMCMR)\n");
        out.append("temp = list.files(path = args[1], pattern=\"*.csv\",full.names=TRUE)").append("\n");
        out.append("for (j in 1:length(temp)) {").append("\n");
        out.append("print(temp[j])\n");
        out.append("mydata = read.csv(temp[j])").append("\n");
        out.append("attach(mydata)").append("\n");
        out.append("factor=cbind(");
        for (int i = 0; i < comparison.size() - 1; i++)
            out.append(comparison.get(i)).append(",");
        out.append(comparison.get(comparison.size() - 1)).append(")").append("\n");
        out.append("print(friedman.test(factor))\n");
        out.append(" if (friedman.test(factor)$p.value < 0.05){\n" +
                "        means <- apply(mydata, 2, mean) # means factors\n" +
                "        maxMeans <- which.max(means)\n" +
                "        cat(paste(\"Max mean algorithm is \",names(mydata)[maxMeans],\": \", means[maxMeans],\"\\n\\n\"))\n" +
                "        post <- posthoc.friedman.nemenyi.test(factor)\n" +
                "        cat(post$method,\"\\n\")\n" +
                "        conf <- post$p.value[c(maxMeans)]  # accede alla colonna p.value dell'algoritmo con valore max di metrica\n" +
                "        #    print(conf)\n" +
                "        algConf = c()\n" +
                "        for(i in 1:length(conf)){\n" +
                "           if (is.integer(conf[i]) && conf[i]> 0.05)\n" +
                "                algConf <- c(algConf,names(conf[i]))\n" +
                "        }\n" +
                "        if (length(algConf)==0){\n" +
                "            cat(paste(\"The best algorithm is \",names(mydata)[maxMeans],\"\\n\"))\n" +
                "        }\n" +
                "        else {\n" +
                "            for(i in 1:length(algConf)){\n" +
                "                cat(algConf[i],\"\\n\")\n" +
                "            }\n" +
                "        }\n" +
                "\n" +
                "    } else\n" +
                "    print (\"Not significative\")\n" +
                "\n" +
                "    detach(mydata)}");
        out.close();
    }


//    #!/usr/bin/env Rscript
//    library(PMCMR)
//    mydata = read.csv("~/Scrivania/AAconf_10Features_given_20_10Top_F1.csv")
//    attach(mydata)
//    factor=cbind(PageRank,MRMR,RankerWekaChiSquaredAttributeEval,RankerWekaGainRatioAttributeEval,RankerWekaSVMAttributeEval,RankerWekaInfoGainAttributeEval,RankerWekaPCA,RankerWekaReliefFAttributeEval)
//    print(friedman.test(factor))
//            if (friedman.test(factor)$p.value < 0.05){
//        means <- apply(mydata, 2, mean) # means factors
//        maxMeans <- which.max(means)
//        cat(paste("Max mean algorithm is ",names(mydata)[maxMeans],": ", means[maxMeans],"\n\n"))
//        post <- posthoc.friedman.nemenyi.test(factor)
//        cat(post$method,"\n")
//        conf <- post$p.value[,c(maxMeans)]  # accede alla colonna p.value dell'algoritmo con valore max di metrica
//        #    print(conf)
//        algConf = c()
//        for(i in 1:length(conf)){
//            if (conf[i] > 0.05)
//                algConf <- c(algConf,names(conf[i]))
//        }
//        if (length(algConf)==0){
//            cat(paste("The best algorithm is ",names(mydata)[maxMeans],"\n"))
//        }
//        else {
//            for(i in 1:length(algConf)){
//                cat(algConf[i],"\n")
//            }
//        }
//
//    } else
//    print ("Not significative")
//
//    detach(mydata)


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
        out.append("print(temp[j])");
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
