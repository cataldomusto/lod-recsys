package org.semanticweb.jars2.reconrank.runtime.graph;

import org.semanticweb.yars.nx.*;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;


public class Graph extends Hashtable<Node, GraphNode> {

    public static final String[] COUNTRY_SECOND_LEVEL_DOMAINS =
            {"co", "com", "ac", "net", "org", "biz", "nc", "gov", "me", "ltd", "govt"};

    private static final long serialVersionUID = 1L;
    private int index = 0;
    private boolean finalised = false;
    private Node[] inv;
    private Set<String> _country2ndLevelDomains;
    //private Hashtable<Node,Vector<PredicateObjectPair>> data;

    public Graph() {
        super();
        _country2ndLevelDomains = Collections.synchronizedSortedSet(new TreeSet<String>());
        for (String s : COUNTRY_SECOND_LEVEL_DOMAINS)
            _country2ndLevelDomains.add(s);
    }

    public void add(Node[] n) {
        if (n.length != 3 && n.length != 4) {
            System.err.println("Node array length !=3 || != 4");
        }
        if (n.length == 3) {
            add(n[0], n[1], n[2]);
        } else
            add(n[0], n[1], n[2], n[3]);
    }

    public void add(Node sub, Node pred, Node obj) {
        if (pred.toN3().equals("<http://swse.org/tfidf>")) {
            GraphNode gsub = get(sub);
            double tfidf = Double.parseDouble(obj.toString());
            if (gsub == null) {
                Subj temp = new Subj(index);
                index++;
                temp.setTFIDF(tfidf);
                gsub = temp;
                put(sub, gsub);
            } else if (gsub instanceof Obj) {
                Obj temp = (Obj) gsub;
                Subj temps = new Subj(temp);
                temps.setTFIDF(tfidf);
                gsub = temps;
            } else if (gsub instanceof Context) {
                Context temp = (Context) gsub;
                Subj temps = new Subj(temp);
                temps.setTFIDF(tfidf);
                gsub = temps;
            } else if (gsub instanceof Subj) {
                Subj temps = (Subj) gsub;
                temps.setTFIDF(tfidf);
                gsub = temps;
            } else {
                System.err.println("Error entering subject " + sub.toN3() + " Wrong class type " + sub.getClass());
                return;
            }
            return;
        }

        int objindex = -1;

        GraphNode gobj = null;
        if (obj instanceof Resource || obj instanceof BNode) {
            gobj = get(obj);
            if (gobj == null) {
                Obj temp = new Obj(index);
                temp.incrementCount();
                gobj = temp;
                index++;
                put(obj, temp);
            } else if (gobj instanceof Context) {
                Context temp = (Context) gobj;
                Obj tempo = new Obj(temp);
                tempo.incrementCount();
                gobj = tempo;
            } else if (gobj instanceof Subj) {
                Subj temp = (Subj) gobj;
                temp.incrementCount();
                gobj = temp;
            } else if (gobj instanceof Obj) {
                Obj temp = (Obj) gobj;
                temp.incrementCount();
                gobj = temp;
            } else {
                System.err.println("Error entering object " + obj.toN3());
                return;
            }
            objindex = gobj.getIndex();
        }


        GraphNode gsub = get(sub);
        if (gsub == null) {
            Subj temp = new Subj(index);
            temp.incrementCount();
//			if object is a literal
            if (objindex != -1)
                temp.addLink(objindex);
            index++;
            put(sub, temp);
        } else if (gsub instanceof Obj) {
            Obj temp = (Obj) gsub;
            Subj temps = new Subj(temp);
            temps.incrementCount();
//			if object is not a literal
            if (objindex != -1)
                temps.addLink(objindex);
            gsub = temps;
        } else if (gsub instanceof Context) {
            Context temp = (Context) gsub;
            Subj temps = new Subj(temp);
            temps.incrementCount();
            //if object is not a literal
            if (objindex != -1) {
                temps.addLink(objindex);
            }
            gsub = temps;
        } else if (gsub instanceof Subj) {
            Subj temps = (Subj) gsub;
            temps.incrementCount();
//			if object is a literal
            if (objindex != -1)
                temps.addLink(objindex);
            gsub = temps;
        } else {
            System.err.println("Error entering subject " + sub.toN3() + " Wrong class type " + sub.getClass());
            return;
        }
    }

    public void add(Node sub, Node pred, Node obj, Node con) {
        Node host;
        try {
            host = new Resource(getDomain(con.toString()));
            con = host;
        } catch (MalformedURLException e) {
            ;
        }
        if (pred.toN3().equals("<http://swse.org/tfidf>")) {
            GraphNode gsub = get(sub);
            double tfidf = Double.parseDouble(obj.toString());
            if (gsub == null) {
                Subj temp = new Subj(index);
                index++;
                temp.setTFIDF(tfidf);
                gsub = temp;
                put(sub, gsub);
            } else if (gsub instanceof Obj) {
                Obj temp = (Obj) gsub;
                Subj temps = new Subj(temp);
                temps.setTFIDF(tfidf);
                gsub = temps;
            } else if (gsub instanceof Context) {
                Context temp = (Context) gsub;
                Subj temps = new Subj(temp);
                temps.setTFIDF(tfidf);
                gsub = temps;
            } else if (gsub instanceof Subj) {
                Subj temps = (Subj) gsub;
                temps.setTFIDF(tfidf);
                gsub = temps;
            } else {
                System.err.println("Error entering subject " + sub.toN3() + " Wrong class type " + sub.getClass());
                return;
            }
            return;
        }

        int objindex = -1;
        int conindex = -1;

        GraphNode gcon = get(con);
        if (gcon == null) {
            gcon = new Context(index);
            index++;
        }
//		else if(gcon instanceof Obj){
//		Context temp = (Context)gcon;
//		Subj temps  = new Subj(temp);
//		gcon = temps;
//		}
        gcon.incrementCon();
        conindex = gcon.getIndex();
        put(con, gcon);

        GraphNode gobj = null;
        if (obj instanceof Resource || obj instanceof BNode) {
            gobj = get(obj);
            if (gobj == null) {
                Obj temp = new Obj(index);
                temp.addContext(conindex);
                temp.incrementCount();
                gobj = temp;
                index++;
                put(obj, temp);
            } else if (gobj instanceof Context) {
                Context temp = (Context) gobj;
                Obj tempo = new Obj(temp);
                tempo.incrementCount();
                tempo.addContext(conindex);
                put(obj, tempo);
            } else if (gobj instanceof Subj) {
                Subj temp = (Subj) gobj;
                temp.addContext(conindex);
                temp.incrementCount();
                put(obj, temp);
            } else if (gobj instanceof Obj) {
                Obj temp = (Obj) gobj;
                temp.addContext(conindex);
                temp.incrementCount();
                put(obj, temp);
            } else {
                System.err.println("Error entering object " + obj.toN3());
                return;
            }
            objindex = gobj.getIndex();
        }


        GraphNode gsub = get(sub);
        if (gsub == null) {
            Subj temp = new Subj(index);
            temp.incrementCount();
            temp.addContext(conindex);
//			if object is a literal
            if (objindex != -1)
                temp.addLink(objindex);
            index++;
            put(sub, temp);
        } else if (gsub instanceof Obj) {
            Obj temp = (Obj) gsub;
            Subj temps = new Subj(temp);
            temps.incrementCount();
            temps.addContext(conindex);
//			if object is a literal
            if (objindex != -1)
                temps.addLink(objindex);
            put(sub, temps);
        } else if (gsub instanceof Context) {
            Context temp = (Context) gsub;
            Subj temps = new Subj(temp);
            temps.incrementCount();
            temps.addContext(conindex);
            //if object is a literal
            if (objindex != -1) {
                temps.addLink(objindex);
            }
            put(sub, temps);
        } else if (gsub instanceof Subj) {
            Subj temps = (Subj) gsub;
            temps.addContext(conindex);
            temps.incrementCount();
//			if object is a literal
            if (objindex != -1)
                temps.addLink(objindex);
            put(sub, temps);
        } else {
            System.err.println("Error entering subject " + sub.toN3() + " Wrong class type " + sub.getClass());
            return;
        }
    }

    public void add(Quad q) {

        Node sub = q.getSubject();
        Node pred = q.getPredicate();
        Node obj = q.getObject();
        Node con = q.getContext();

        add(sub, pred, obj, con);
    }

    public void add(Triple t) {
        if (t instanceof Quad) {
            add((Quad) t);
            return;
        }

        Node sub = t.getSubject();
        Node pred = t.getPredicate();
        Node obj = t.getObject();

        add(sub, pred, obj);
    }


    public Node getNode(int index) {
        if (!finalised)
            finalise();
        return inv[index];
    }

    public void finalise() {
//		System.err.println("Size "+size());
        inv = new Node[size()];
        Enumeration<Node> keys = keys();
        Enumeration<GraphNode> elements = elements();
        int i = 0;
        while (keys.hasMoreElements()) {
            Node n = keys.nextElement();
            GraphNode gn = elements.nextElement();
            i++;
            if (gn instanceof Obj) {
                Obj o = (Obj) gn;
                //drop non-rankable resources (resources which do not appear as subject or context)
                if (o.getCon() == 0) {
                    i--;
                    //System.err.println(n.toN3()+" "+o.getIndex());
                    inv[o.getIndex()] = null;
                    remove(n);
//					System.err.println(size());
                } else
                    inv[o.getIndex()] = n;
            } else {
                int index = gn.getIndex();
                if (index < 0)
                    System.err.println("Error in finalising graph");
                //else if(index>inv.size())
                //	inv.
                inv[gn.getIndex()] = n;
            }
        }
        finalised = true;
    }

    private String getDomain(String url) throws MalformedURLException {
        URL con = new URL(url);
        String newt, oldt = "", oldert = "", h = con.getHost();

        StringTokenizer tok = new StringTokenizer(h, ".");
        if (tok.countTokens() < 2)
            return null;
        if (tok.countTokens() == 2) {
            return h;
        } else while (tok.hasMoreTokens()) {
            newt = tok.nextToken();
            if (!tok.hasMoreTokens()) {
                if (!oldert.equals("") && _country2ndLevelDomains.contains(oldt))
                    return oldert + "." + oldt + "." + newt;
                else
                    return oldt + "." + newt;
            }
            oldert = oldt;
            oldt = newt;
        }

        return null;
    }
}




