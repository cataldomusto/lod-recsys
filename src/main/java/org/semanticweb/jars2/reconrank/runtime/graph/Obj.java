package org.semanticweb.jars2.reconrank.runtime.graph;

import java.util.Hashtable;

public class Obj extends GraphNode {
    private Hashtable<Integer, Integer> _contexts;

    public Obj(int index) {
        _index = index;
        _contexts = new Hashtable<Integer, Integer>();
        _type = -1;
    }

    public Obj(Context c) {
        _index = c.getIndex();
        _count = c.getCount();
        _contexts = new Hashtable<Integer, Integer>();
        _type = 2;
        setCon(c.getCon());
    }

    public void addContext(int index) {
        Integer count = _contexts.get(index);
        if (count == null) {
            _contexts.put(index, new Integer(1));
            return;
        }
        count = new Integer(count.intValue() + 1);
    }

    public Hashtable<Integer, Integer> getContexts() {
        return _contexts;
    }


}
