/*
 * Author : Aidan Hogan
 */
package org.semanticweb.jars2.reconrank.runtime;

/**
 * Class written to implement the PageRank optimisation method
 * Quadratic Extrapolation which takes four previous values for
 * eigenvector estimation and estimates the values the eigenvector
 * is converging towards by subtracting the second and third eigenvectors
 */
public class QuadraticExtrapolation {

    private int size;

    public QuadraticExtrapolation(int size) {
        this.size = size;
    }

    public double[] performQE(double[][] oldRT, double[] currentRT) {

        double[][] R = new double[2][2];
        double[][] Q = new double[size][2];

        double[][] Z = new double[size][2];
        double[] newZ = new double[size];

        double[] U = new double[2];

        double[] rho = new double[3];
        double[] beta = new double[3];
        double sum;

        int i, j;


        for (i = 0; i < size; i++) {
            newZ[i] = currentRT[i] - oldRT[i][2];
            Z[i][0] = oldRT[i][1] - oldRT[i][2];
            Z[i][1] = oldRT[i][0] - oldRT[i][2];
        }

        gramSchmidt(Z, Q, R);
        U[0] = 0;
        U[1] = 0;

        for (i = 0; i < 2; i++)
            for (j = 0; j < size; j++)
                U[i] += Q[j][i] * newZ[j] * -1;

        rho[2] = 1;
        rho[1] = U[1] / R[1][1];
        rho[0] = (U[0] - R[0][1] * rho[1]) / R[0][0];

        beta[2] = rho[2];
        beta[1] = beta[2] + rho[1];
        beta[0] = beta[1] + rho[0];

        sum = beta[0] + beta[1] + beta[2];

        for (i = 0; i < 3; i++)
            beta[i] /= sum;

        for (i = 0; i < size; i++)
            currentRT[i] = beta[0] * oldRT[i][1] + beta[1] * oldRT[i][0] + beta[2] * currentRT[i];

        return currentRT;
    }

    private void gramSchmidt(double[][] Z, double Q[][], double R[][]) {
        double norm = 0;
        double dot = 0;
        double[] V = new double[size];
        int i;

        for (i = 0; i < size; i++)
            norm += Math.pow(Z[i][0], 2);

        norm = Math.pow(norm, 0.5);

        for (i = 0; i < size; i++) {
            Q[i][0] = Z[i][0] / norm;
            dot += Z[i][1] * Q[i][0];
            V[i] = Z[i][1] - (dot * Q[i][0]);
        }

        norm = 0;
        for (i = 0; i < size; i++)
            norm += Math.pow(V[i], 2);

        norm = Math.pow(norm, 0.5);

        for (i = 0; i < size; i++)
            Q[i][1] = V[i] / norm;

        for (i = 0; i < 2; i++)
            for (int j = 0; j < 2; j++)
                for (int k = 0; k < size; k++)
                    R[i][j] += Q[k][i] * Z[k][j];

    }

}
