/*
 * Author : Aidan Hogan
 */
package org.semanticweb.jars2.reconrank.runtime;

import java.util.Hashtable;

/**
 * @author aidhog
 *         <p/>
 *         This class is used to implement an inverted index, that is to
 *         find the node index given the node identifer (URI).
 */
public class NamesHashTable {
    private int value;

    //flag specifying if last entry attempted to be added was unique
    public boolean unique;

    //hashtable to store data
    private Hashtable namesHT;

    public NamesHashTable() {
        value = 0;
        namesHT = new Hashtable();
    }

    public NamesHashTable(int size) {
        value = 0;
        namesHT = new Hashtable(size);
    }

    public NamesHashTable(int size, float load) {
        value = 0;
        namesHT = new Hashtable(size, load);
    }

    //add a name to the hashtable
    //returns index name is found at
    public int addName(String name) {
        unique = false;
        if (namesHT.get(name) != null) {
            Integer Temp = (Integer) namesHT.get(name);
            return Temp.intValue();
        }

        unique = true;
        Integer Value = new Integer(value);
        value++;

        namesHT.put(name, Value);
        return value - 1;
    }

    //get the size of the hashtable
    public int getSize() {
        return namesHT.size();
    }

    //get the index assigned to a name
    public int findName(String name) {
        Integer Temp = (Integer) namesHT.get(name);
        if (Temp != null)
            return Temp.intValue();
        else
            return -1;
    }
}
