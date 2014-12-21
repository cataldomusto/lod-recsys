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
 * Abstract class inherited by Count and ConnectivityMatrix and their Inverted forms
 */
public abstract class FlatMatrix {
    //count of non empty rows
    public int rowCount;
    //if last link added was unique true
    public boolean uniqueLink;
    //total links
    public int linksCount = 0;
    //array of vectors simulate jagged matrix required to hold the data
    protected Vector[] CMat;
    //iterator for the rows of CMAt
    private Iterator iter;

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
    public int getElementAt(int rowIndex, int elementIndex) {
        int value = -1;
        if (rowIndex > CMat.length)
            return -1;
        Vector row = CMat[rowIndex];
        if (row == null)
            return -1;

        try {
            value = ((Integer) row.elementAt(elementIndex)).intValue();
        } catch (ClassCastException e) {
            // useless.. hopefully!
        }
        return value;

    }

    //get iterator for values of a row
    public Iterator getRowIterator(int rowIndex) {
        if (rowIndex > CMat.length)
            return null;
        Vector row = CMat[rowIndex];
        if (row == null)
            return null;

        iter = row.iterator();
        return iter;
    }

    //add a value to a row
    public int addToRow(int rowIndex, int value, boolean noRepetion) {

        if (noRepetion) {
            int temp = searchRow(rowIndex, value);
            if (temp != -1) {
                uniqueLink = false;
                return temp;
            }
            uniqueLink = true;
        }

        Vector row = CMat[rowIndex];
        if (row == null) {
            row = new Vector();
        }
        row.add(new Integer(value));
        linksCount++;
        if (rowIndex + 1 > rowCount)
            rowCount = rowIndex + 1;
        return getRowSize(rowIndex) - 1;
    }

    //search a specified row for a value
    public int searchRow(int rowIndex, int key) {
        Integer Key = new Integer(key);

        if (rowIndex >= CMat.length)
            return -1;
        Vector row = CMat[rowIndex];
        if (row == null)
            return -1;
        Iterator iter = row.iterator();
        int index = 0;
        Integer Temp;

        while (iter.hasNext()) {
            Temp = (Integer) iter.next();
            if (Temp.equals(Key))
                return index;
            index++;
        }
        return -1;

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
