package org.semanticweb.jars2.reconrank.runtime.graph;

import java.util.Hashtable;
import java.util.TreeSet;


public class Subj extends GraphNode {
    private Hashtable<Integer, Integer> _contexts;
    private TreeSet<Integer> _linksTo;
    private double _tfidf = 0;

    public Subj(int index) {
        _index = index;
        _contexts = new Hashtable<Integer, Integer>();
        _linksTo = new TreeSet<Integer>();
        _type = 1;
    }

    public Subj(Obj o) {
        _index = o.getIndex();
        _contexts = o.getContexts();
        _linksTo = new TreeSet<Integer>();
        _type = 1;
    }

    public Subj(Context c) {
        _index = c.getIndex();
        _count = c.getCount();
        _contexts = new Hashtable<Integer, Integer>();
        _linksTo = new TreeSet<Integer>();
        setCon(c.getCon());
        _type = 3;
    }

    public void addLink(int index) {
        _linksTo.add(new Integer(index));
    }

    public void addContext(int index) {
        Integer indx = new Integer(index);
        Integer count = _contexts.get(new Integer(indx));
        if (count == null) {
            _contexts.put(indx, new Integer(1));
            return;
        }
        count = new Integer(count.intValue() + 1);
        _contexts.put(indx, count);
    }

    public Hashtable<Integer, Integer> getContexts() {
        return _contexts;
    }

    public TreeSet<Integer> getLinks() {
        return _linksTo;
    }

    public void setTFIDF(double tfidf) {
        _tfidf = tfidf;
    }

    public double getTFIDF() {
        return _tfidf;
    }
}
