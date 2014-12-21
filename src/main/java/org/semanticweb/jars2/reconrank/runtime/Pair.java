/*
 * Author : Aidan Hogan
 */
package org.semanticweb.jars2.reconrank.runtime;

/**
 * This class stores a nodes rank and its identifier.
 * It implements comparable which bases its results on
 * the ranks of the two Pair objects being compared.
 * This is then used to order the results into a list.
 */
public class Pair implements Comparable {
    public Double _rank;
    public String _subject;

    public Pair(double rank, String subject) {
        _rank = new Double(rank);
        _subject = subject;
    }


    public String toString() {
        return _subject;
    }

    public int compareTo(Object o) {
        Pair p = (Pair) o;
        if (p._rank.compareTo(_rank) == 0)
            return p._subject.compareTo(_subject);

        return p._rank.compareTo(_rank);
    }
}
