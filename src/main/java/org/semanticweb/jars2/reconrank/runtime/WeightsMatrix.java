/*
 * Author: Aidan Hogan
 */
package org.semanticweb.jars2.reconrank.runtime;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.Vector;

/**
 * Count matrix, subclass of flat matrix, represents matrix with count of links
 */
public class WeightsMatrix {
    //count of non empty rows
    public int rowCount;
    //total links
    public int linksCount = 0;
    //	array of vectors simulate jagged matrix required to hold the data
    protected Vector<Double>[] CMat;

    //constructor, size specified
    public WeightsMatrix(int Size) {
        CMat = new Vector[Size];
        for (int i = 0; i < Size; i++)
            CMat[i] = new Vector<Double>();
    }

    //get summation of all entries in a row
    public double getRowSum(int rowIndex) {
        Iterator<Double> iter = getRowIterator(rowIndex);
        int sum = 0;
        while (iter.hasNext())
            sum += iter.next().doubleValue();
        return sum;
    }

    //set value at a specified index
    public void setValueAt(int rowIndex, int elementIndex, double value) {
        if (elementIndex >= getRowSize(rowIndex)) {
            addToRow(rowIndex, value);
            return;
        }

        Vector<Double> row = CMat[rowIndex];
        row.setElementAt(new Double(value), elementIndex);
    }


    //get number of rows in matrix, equivalent to number of nodes
    public int getRowCount() {
        return rowCount;
    }

    //set number of rows
    public void setRowCount(int _rowCount) {
        rowCount = _rowCount;
    }

    //get length of specified row
    public int getRowSize(int rowIndex) {
        if (rowIndex >= CMat.length)
            return -1;
        Vector row = CMat[rowIndex];
        if (row == null)
            return -1;
        return row.size();
    }

    //retrieve value from specified index
    public double getElementAt(int rowIndex, int elementIndex) {
        double value = -1;
        if (rowIndex >= CMat.length)
            return -1;
        Vector<Double> row = CMat[rowIndex];
        if (row == null)
            return -1;

        if (elementIndex >= row.size())
            return -1;

        value = row.elementAt(elementIndex).doubleValue();

        return value;

    }

    //get iterator for values of a row
    public Iterator<Double> getRowIterator(int rowIndex) {
        if (rowIndex > CMat.length)
            return null;
        Vector<Double> row = CMat[rowIndex];
        if (row == null)
            return null;

        Iterator<Double> iter = row.iterator();
        return iter;
    }

    //add a value to a row
    public int addToRow(int rowIndex, double value) {
        Vector<Double> row = CMat[rowIndex];
        if (row == null) {
            row = new Vector<Double>();
        }
        row.add(new Double(value));
        linksCount++;
        if (rowIndex + 1 > rowCount)
            rowCount = rowIndex + 1;
        return getRowSize(rowIndex) - 1;
    }

    //save matrix to a specifed file path
    public void saveMatrix(String filename) throws IOException {
        PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(filename)));
        int i;

        for (i = 0; i < rowCount; i++) {
            if (i > CMat.length)
                return;
            Vector row = CMat[i];
            if (row == null) {
                pw.print("\n");
                continue;
            }
            Iterator iter = row.iterator();
            while (iter.hasNext())
                pw.print(iter.next() + " ");
            pw.println();
        }
        pw.close();

    }

    //increment the count of rows
    public void incrementRowCount() {
        rowCount++;
    }

    //get the average number of links a node has
    public double averageRowSize() {
        double sum = 0;
        for (int i = 0; i < rowCount; i++)
            sum += getRowSize(i);
        sum /= (double) rowCount;
        return sum;
    }
}
