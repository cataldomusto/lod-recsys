/*
 * Author: Aidan Hogan
 * June, '05
 */
package org.semanticweb.jars2.reconrank.runtime;

import java.util.Vector;

/**
 * Connectivity Matrix stores the links present in a graph.
 * Subclass of FlatMatrix.
 * It does so in a flat packed way, to avoid colossal square matrices.
 * The main data is stored in a jagged dynamic array, an array of vectors.
 */
public class ConnectivityMatrix extends FlatMatrix {

    //constructor, size specified
    public ConnectivityMatrix(int Size) {
        CMat = new Vector[Size];
        for (int i = 0; i < Size; i++)
            CMat[i] = new Vector();
    }
}
