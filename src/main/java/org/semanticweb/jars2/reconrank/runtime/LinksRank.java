/*
 * Created on 21-Jun-2005
 * Author: Aidan Hogan
 */
package org.semanticweb.jars2.reconrank.runtime;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Class representing PageRanking of a Connectivity Matrix
 * Uses methods explained at http://sw.deri.org/2005/05/ranking/
 */
public class LinksRank {

    //holds current eigenvector, or current ranking table
    public double[] rankTable;

    //
    private double summation;

    //sum of dangling nodes
    private double emptySum;

    //number of nodes
    private int size;


    private double SIZE;

    private double EMPTY;

    //the measure of convergence
    public double l1residual;

    //count of iterations done
    public int iterationsDone = 0;

    //tolerance used to determine fixpoint
    //when l1 norm of residual drops below tol, iterations ended
    private static final double tol = 0.001;

    //mu is the weight of the link to nodes not explicity linked to
    // (a function of d)
    private double mu;

    //number of dangling nodes
    private int countEmpty = 0;

    //d is the damping factor, the probability of following the links, not teleporting
    private final double d = 0.85;

    //the number of iterations between which quadratic extrapolation is performed
    // (min should be 4)
    private int QEPeriod = 4;

    /**
     * Constructor for this class
     *
     * @param CM - Connectivity Matrix of graph to be ranked
     */
    public LinksRank(RankingConsole rc) {
        int i;
        this.size = rc.CM.getRowCount();
        this.SIZE = (double) size;
        this.EMPTY = 1 / SIZE;

        mu = (1 - d) / SIZE;

        rankTable = new double[size];

        int numberSaved;
        if (rc.qe)
            numberSaved = 3;
        else numberSaved = 1;

        double[][] oldRT = new double[size][numberSaved];


        int totalLinks = rc.CM.linksCount;
        for (i = 0; i < size; i++) {
            oldRT[i][0] = (d * (double) rc.CM.getRowSize(i) / (double) totalLinks) + ((1 - d) / (double) size);
            rankTable[i] = oldRT[i][0];

            if (rc.outlinks[i] == 0) {
                emptySum += rankTable[i];
            } else summation += rankTable[i];
        }

//		summation = 1 - (double)countEmpty/size;
//		emptySum = (double)countEmpty/size;

//		System.out.println("Init "+getSummation()+" "+size);
//		print();

        rankGraph(rc, oldRT);
    }

    public void print() {
        for (int i = 0; i < size; i++)
            System.out.println(rankTable[i]);
    }

    public double getSummation() {
        double sum = 0;
        for (int i = 0; i < size; i++)
            sum += rankTable[i];
        return sum;
    }

    public double getRankOfNode(int nodeIndex) {
        return rankTable[nodeIndex];
    }


    /**
     * Saves the ranking table to a file specified by parameter.
     *
     * @param filename - Filename and path of destination file
     */
    public void save(String filename) {
        PrintWriter pw;
        try {
            pw = new PrintWriter(new FileWriter(filename));
            for (int i = 0; i < size; i++)
                pw.println(rankTable[i]);
            pw.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    public double getMaxRankValue() {
        double max = 0;
        for (int i = 0; i < size; i++)
            if (rankTable[i] > max)
                max = rankTable[i];
        return max;
    }

    /**
     * Workhouse method. Ranks the graph.
     */
    public void rankGraph(RankingConsole rc, double[][] oldRT) {
        QuadraticExtrapolation QE = null;
        if (rc.qe)
            QE = new QuadraticExtrapolation(size);

        int iters = 0;
        do {
            if (iters % QEPeriod == 0 && iters != 0 && rc.qe) //if period over, call QuadraticExtrapolation
                rankTable = QE.performQE(oldRT, rankTable);

            for (int i = 0; i < size; i++) {
                if (rc.qe) {
                    oldRT[i][2] = oldRT[i][1];
                    oldRT[i][1] = oldRT[i][0];
                }
                oldRT[i][0] = rankTable[i];
            }

            eigvCalcIteration(rc, oldRT);

            iters++;
            iterationsDone = iters;

            if (iterationsDone == 5)
                break;
        } while (l1residual > tol);
    }

    /**
     * Main section of the calculations require eigenvector determination.
     * Represents one main iteration of the PageRank calculation.
     */
    private void eigvCalcIteration(RankingConsole rc, double[][] oldRT) {
        int i, j, rowSize, inLink;

        //System.out.println(summation+" "+emptySum+" "+getSummation()+" "+(summation+emptySum));

        double sum1 = 0;
        double sum2 = 0;
        l1residual = 0;
        double minimum;
        minimum = EMPTY * emptySum;
        minimum += summation * mu;

        double share;

        for (i = 0; i < size; i++) {
            rankTable[i] = 0;
            rowSize = rc.CM.getRowSize(i);
            for (j = 0; j < rowSize; j++) {
                inLink = rc.CM.getElementAt(i, j);


                if (rc.weights) {
                    share = d / (double) rc.outlinks[inLink];
                    rankTable[i] += (share * rc.weightMatrix.getElementAt(i, j)) * oldRT[inLink][0];

                } else {
                    share = d / (double) rc.outlinks[inLink];
                    rankTable[i] += share * oldRT[inLink][0];
                }
            }

            rankTable[i] += minimum;

            //System.out.println("    "+i+" "+rankTable[i]);


            if (rc.outlinks[i] == 0)
                sum1 += rankTable[i];
            else
                sum2 += rankTable[i];

            l1residual += Math.abs(rankTable[i] - oldRT[i][0]);
        }

        emptySum = sum1;
        summation = sum2;
    }
}
