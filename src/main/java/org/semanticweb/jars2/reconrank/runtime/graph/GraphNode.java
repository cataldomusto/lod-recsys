package org.semanticweb.jars2.reconrank.runtime.graph;

public abstract class GraphNode {
    protected int _index;
    protected int _count = 0;
    protected int _con = 0;
    protected int _type = -1;

    public int getIndex() {
        return _index;
    }

    public void setIndex(int index) {
        _index = index;
    }

    public int getType() {
        return _type;
    }

    public void setType(int type) {
        _type = type;
    }

    public void setCount(int count) {
        _count = count;
    }

    public void incrementCount() {
        _count++;
    }

    public int getCount() {
        return _count;
    }

    public void setCon(int con) {
        _con = con;
    }

    public void incrementCon() {
        _con++;
    }

    public int getCon() {
        return _con;
    }

}
