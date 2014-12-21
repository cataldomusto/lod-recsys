/*
 * Author : Aidan Hogan
 */
package org.semanticweb.jars2.reconrank.runtime;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.Vector;

/**
 * @author aidhog
 *         <p/>
 *         Class allows node indexes to be assigned to node URIs.
 *         For instance the node URI in index 4 of the Vector has
 *         a node index of 4.
 */
public class NamesVector extends Vector<String> {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    //public Vector names = new Vector();

    public NamesVector(int size) {
        super(size);
    }

    public NamesVector() {
        super();
    }

    public void addName(String name) {
        add(name);
    }

    //save vector to a file
    public void saveVector(String filename) throws IOException {
        PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(filename)));

        Iterator iter = iterator();
        while (iter.hasNext())
            pw.println(iter.next());
        pw.close();
    }

    public Iterator<String> getIterator() {
        return iterator();
    }

    public int size() {
        return super.size();
    }

    public int findName(String name) {
        int index = -1;
        int i = 0;
        String temp;
        boolean found = false;
        Iterator iter = iterator();
        while (iter.hasNext()) {
            temp = iter.next().toString();
            if (temp.compareTo(name) == 0) {
                found = true;
                index = i;
                break;
            }
            i++;
        }
        if (found)
            return index;
        return -1;
    }

    public String getName(int index) {
        return elementAt(index).toString();
    }

    public void print() {
        Iterator<String> iter = iterator();
        System.out.println("\nPrinting names vector ....");
        while (iter.hasNext())
            System.out.println(iter.next());
    }
}
