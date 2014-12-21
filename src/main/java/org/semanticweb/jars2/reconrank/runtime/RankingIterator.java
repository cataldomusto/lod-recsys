package org.semanticweb.jars2.reconrank.runtime;

import org.semanticweb.jars2.reconrank.runtime.graph.Graph;
import org.semanticweb.yars.nx.BNode;
import org.semanticweb.yars.nx.Literal;
import org.semanticweb.yars.nx.Node;
import org.semanticweb.yars.nx.Resource;

import java.util.Iterator;
import java.util.TreeSet;

public class RankingIterator implements Iterator<Node[]> {
    Iterator<Node[]> _ip;
    Iterator<Pair> _resourceRankIter = null;
    Iterator<Pair> _contextRankIter = null;
    Graph _g;
    int respos = 0;
    int conpos = 0;

    boolean quad = false;

    Resource resPred = new Resource("http://sw.deri.org/2005/05/ranking#resourceReConRankPosition");
    Resource conPred = new Resource("http://sw.deri.org/2005/05/ranking#contextReConRankPosition");
    Resource context = new Resource("http://sw.deri.org/2005/05/ranking#RankingEngine");

    public RankingIterator(Iterator<Node[]> ip) {
        _ip = ip;
        _g = new Graph();
    }

    public boolean hasNext() {

        if (_ip.hasNext())
            return true;
        else if (_resourceRankIter != null && _resourceRankIter.hasNext()) {
            return true;
        } else if (_contextRankIter != null && _contextRankIter.hasNext()) {
            return true;
        } else if (_resourceRankIter == null && _g.size() != 0) {
            return true;
        }
        return false;
    }

    public Node[] next() {
        if (_ip.hasNext()) {
            Node[] q = _ip.next();
            if (q.length == 4)
                quad = true;
            _g.add(q);
            return q;
        } else {
            if (_resourceRankIter != null && _resourceRankIter.hasNext()) {
                respos++;
                Pair p = _resourceRankIter.next();
                Node sub;
                if (p._subject.startsWith("<"))
                    sub = new Resource(p._subject.substring(1, p._subject.length() - 1));
                else
                    sub = new BNode(p._subject.substring(2));

                if (quad) {
                    Node[] n = new Node[4];
                    n[0] = sub;
                    n[1] = resPred;
                    n[2] = new Literal(respos + "");
                    n[3] = context;

                    return n;
                }

                Node[] n = new Node[3];
                n[0] = sub;
                n[1] = resPred;
                n[2] = new Literal(respos + "");

                return n;
            } else if (_contextRankIter != null && _contextRankIter.hasNext()) {
                conpos++;
                Pair p = _contextRankIter.next();
                Node sub;
                if (p._subject.startsWith("<"))
                    sub = new Resource(p._subject.substring(1, p._subject.length() - 1));
                else
                    sub = new BNode(p._subject.substring(2));

                if (quad) {
                    Node[] n = new Node[4];
                    n[0] = sub;
                    n[1] = conPred;
                    n[2] = new Literal(conpos + "");
                    n[3] = context;

                    return n;
                }

                Node[] n = new Node[2];
                n[0] = sub;
                n[1] = conPred;
                n[2] = new Literal(conpos + "");

                return n;
            } else if (_g.size() != 0 && _resourceRankIter == null) {
                _g.finalise();
                RankingConsole rc = RankingConsoleFactory.load(_g);
                rc.rank();
                TreeSet<Pair>[] results = rc.allResults();
                _resourceRankIter = results[0].iterator();
                _contextRankIter = results[1].iterator();
                return next();
            } else return null;
        }

    }

    public void remove() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("RankingIterator does not support deletes !");
    }
}
