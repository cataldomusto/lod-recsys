package org.semanticweb.jars2.reconrank.runtime;

import org.semanticweb.jars2.reconrank.runtime.graph.Graph;
import org.semanticweb.jars2.reconrank.runtime.graph.GraphNode;
import org.semanticweb.jars2.reconrank.runtime.graph.Obj;
import org.semanticweb.jars2.reconrank.runtime.graph.Subj;
import org.semanticweb.yars.nx.Node;

import javax.naming.Context;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.TreeSet;

public class RankingConsoleFactory {

    //minimum weight for resources with no tf-idf score
    private static final double MIN_WEIGHT = 1;

    public RankingConsoleFactory() {
        ;
    }

    public static RankingConsole load(Graph g) {
        RankingConsole rc = new RankingConsole(g.size(), false);
        rc.weights = true;

        Enumeration<GraphNode> elements = g.elements();
        Enumeration<Node> keys = g.keys();
        int i = 0;
        while (keys.hasMoreElements() && elements.hasMoreElements()) {
            i++;
            Node n = keys.nextElement();
//			System.out.println(n.toN3() +" "+i);

            GraphNode gn = elements.nextElement();
//			System.out.println(gn.getType());
//			System.out.println(gn.getIndex());
//			System.out.println(gn.getCount());
//			System.out.println(gn.getCon());

            if (n == null) {
                continue;
            } else if (gn instanceof Context) {
                rc.addNode(n.toN3(), 2);
            } else if (gn instanceof Obj) {
                Obj temp = (Obj) gn;
//				System.out.println(temp.getContexts().size());
                int concount = temp.getCon();

                //not a context, not a subject therefore not rankable
                if (concount == 0) {
                    System.err.println("Non rankable resource present. Should have been dropped.");
                    continue;
                }

                //handle links to and from contexts
                Hashtable<Integer, Integer> cons = temp.getContexts();
                handleContexts(g, rc, n, temp.getType(), temp.getCount(), cons);
            } else if (gn instanceof Subj) {
                Subj sub = (Subj) gn;
//				System.out.println(sub.getContexts().size());
//				System.out.println(sub.getLinks().size());
                int type = sub.getType();

                //handle links to and from contexts first
                Hashtable<Integer, Integer> cons = sub.getContexts();
                handleContexts(g, rc, n, type, sub.getCount(), cons);


                //now handle links to other resources
                TreeSet<Integer> links = sub.getLinks();
                Iterator<Integer> iter = links.iterator();

                double weight;
                Node in;
                GraphNode gin;
                while (iter.hasNext()) {
                    int index = iter.next().intValue();
                    in = g.getNode(index);

                    //been removed, non-rankable resource
                    if (in == null)
                        continue;

                    gin = g.get(in);
                    if (gin instanceof Subj) {
                        Subj temp = (Subj) gin;
                        weight = temp.getTFIDF() + MIN_WEIGHT;
//						System.out.println("Adding link 3"+n.toN3()+" "+type+" "+in.toN3()+" "+temp.getType()+" "+weight);
                        rc.addLink(n.toN3(), type, in.toN3(), temp.getType(), weight);
                    } else if (gin instanceof Obj) {
                        Obj temp = (Obj) gin;
                        weight = MIN_WEIGHT;
                        rc.addLink(n.toN3(), type, in.toN3(), temp.getType(), weight);
                    } else {
                        System.err.println("Error, graph node " + g.getNode(gin.getIndex()) + " is a context, should be a resource.");
                    }
                }
            }
        }

        return rc;

    }

    private static void handleContexts(Graph g, RankingConsole rc, Node n, int type, int rescount, Hashtable<Integer, Integer> cons) {
        Enumeration<Integer> keys = cons.keys();
        Enumeration<Integer> elements = cons.elements();

        double concount, count;
        double weight;
        while (keys.hasMoreElements() && elements.hasMoreElements()) {
            Integer index = keys.nextElement();
            Node con = g.getNode(index.intValue());
//			System.out.println("Con "+index+" "+con);
            GraphNode gn = g.get(con);

            //add link from context to resource
            concount = gn.getCon();
            count = elements.nextElement().intValue();

            //may require fix for context to object links which have been dropped
            //also to normalise weights to 1
            weight = count / concount;
//			System.out.println("count "+ count + " concount "+concount);
//			System.out.println("Adding link 1"+con.toN3()+" "+ gn.getType()+" "+ n.toN3()+" "+ type+" "+ weight+" count"+count+" concount"+concount);
            rc.addLink(con.toN3(), gn.getType(), n.toN3(), type, weight);

            //add link from resource to context
            weight = count / rescount;
//			System.out.println("Adding link 2"+n.toN3()+" "+ type+" "+ con.toN3()+" "+ gn.getType()+" "+ weight+" count"+count+" concount"+concount);
            rc.addLink(n.toN3(), type, con.toN3(), gn.getType(), weight);
        }
    }
}
