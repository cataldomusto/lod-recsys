package di.uniba.it.lodrecsys.graph.featureSelection;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * SimRank is an iterative PageRank-like method for computing similarity.
 * It goes beyond direct cocitation for computing similarity much as PageRank
 * goes beyond direct linking for computing importance.
 *
 * @author Bruno Martins
 */
public class SimRank {

    /**
     * The value for the SimRank dampening factor
     */
    private double dampening = 0.85;

    /**
     * The data structure containing the Web linkage graph
     */
    private WebGraph graph;

    /**
     * A <code>Map</code> containing the SimRank values for each page
     */
    private Map scores;

    /**
     * Constructor for SimRank
     *
     * @param graph The data structure containing the Web linkage graph
     */
    public SimRank(WebGraph graph) {
        this.graph = graph;
        this.scores = new HashMap();
        int numLinks = graph.numNodes();
        Double faux = new Double(1 / graph.numNodes());
        for (int i = 0; i < numLinks; i++) {
            HashMap aux = new HashMap();
            for (int j = 0; j < i; j++)
                aux.put(new Integer(j), new Double(-1));
            scores.put(new Integer(i), aux);
        }
    }

    /**
     * Returns the dampening factor used for the SimRank Algorithm. The amount of SimRank that
     * is transferred depends on a dampening factor which stands for “the probability
     * that a random surfer will get bored”. The dampening factor generally is set to 0.85.
     *
     * @return The dampening factor
     */
    public double getDampening() {
        return this.dampening;
    }

    /**
     * Sets the value for the SimRank dampening factor. The amount of SimRank that
     * is transferred depends on a dampening factor which stands for “the probability
     * that a random surfer will get bored”. The dampening factor generally is set to 0.85.
     *
     * @param damp The dampening factor
     */
    public void setDampening(double damp) {
        this.dampening = damp;
    }

    /**
     * Computes the SimRank value for all the nodes in the Web Graph.
     * In this method, the number of iterations of the algorithm is set accordingly to
     * the number of nodes in the Web graph.
     */
    public void computeSimRank() {
        int n = graph.numNodes();
        int iter =
                iter =
                        ((int) Math.abs(Math.log((double) n) / Math.log((double) 10)))
                                + 1;
        computeSimRank(iter);
    }

    /**
     * Computes the SimRank value for all the nodes in the Web Graph.
     * The prodcedure can be
     * found on the article <a href="http://www-cs-students.stanford.edu/~glenj/simrank.pdf">SimRank: A Measure of Structural-Context Similarity</a>.
     *
     * @param iter The number of iterations for the algorithm
     */
    public void computeSimRank(int iter) {
        Double score = null;
        int numLinks = graph.numNodes();
        for (int i = 0; i < numLinks; i++) {
            HashMap aux = new HashMap();
            for (int j = 0; j < i; j++) aux.put(new Integer(j), new Double(0));
            scores.put(new Integer(i), aux);
        }
        while ((iter--) > 0) {
            Map newScores = new HashMap();
            for (int id1 = 0; id1 < numLinks; id1++) {
                Map map3 = (Map) (scores.get(new Integer(id1)));
                Map map1 = graph.inLinks(new Integer(id1));
                for (int id2 = 0; id2 < id1; id2++) {
                    Map map2 = graph.inLinks(new Integer(id2));
                    int numInLinks1 = 0;
                    int numInLinks2 = 0;
                    score = new Double(0);
                    boolean first = false;
                    Iterator it1 = map1.keySet().iterator();
                    while (it1.hasNext()) {
                        Iterator it2 = map2.keySet().iterator();
                        Integer link1 = (Integer) (it1.next());
                        Double weight1 = (Double) (map1.get(link1));
                        if (weight1 != null && weight1.doubleValue() > 0) numInLinks1++;
                        while (it2.hasNext()) {
                            Integer link2 = (Integer) (it2.next());
                            Double weight2 = (Double) (map2.get(link2));
                            if (weight2 != null && weight2.doubleValue() > 0) {
                                if (weight1 != null && weight1.doubleValue() > 0)
                                    score = new Double(simRank(link1, link2).doubleValue() + score.doubleValue());
                                if (first) numInLinks2++;
                            }
                        }
                        first = true;
                        score = new Double((dampening / (numInLinks1 + numInLinks2)) * score.doubleValue());
                        map3.put(new Integer(id2), score);
                    }
                }
                newScores.put(new Integer(id1), map3);
            }
            for (int j = 0; j < numLinks; j++) {
                scores.put(
                        new Integer(j),
                        (Double) (newScores.get(new Integer(j))));
            }
        }
    }

    /**
     * Returns the SimRank score between a given link and all other links in the Web graph
     *
     * @param link The url for the link
     * @return A Map with the Amsler score between the given link and all other
     * links in the Web graph. Keys in the Map are link identifyers for all the other links,
     * and values correspond to the Amsler score
     */
    public Map simRank(String link) {
        return simRank(graph.URLToIdentifyer(link));
    }

    /**
     * Returns the SimRank score between two given links
     *
     * @param link1 The url for one of the links
     * @param link2 The url for the other link
     * @return The Amsler score between the two given links
     */
    public Double simRank(String link1, String link2) {
        return simRank(
                graph.URLToIdentifyer(link1),
                graph.URLToIdentifyer(link2));
    }

    /**
     * Returns the SimRank score between a given link identifyer and all other links in the Web graph
     * Identifyers are Integer numberes, used in <code>WebGraph</code> to
     * represent the Web graph for efficiency reasons.
     *
     * @param link The identifyer for the link
     * @return A Map with the Amsler score between the given link and all other
     * links in the Web graph. Keys in the Map are link identifyers for all the other links,
     * and values correspond to the Amsler score
     */
    private Map simRank(Integer id) {
        if (id.intValue() != 0) {
            if (((Integer) (((Map) (scores.get(id))).get(new Integer(0))))
                    .doubleValue()
                    < 0) {
                computeSimRank();
                return simRank(id);
            }
        } else {
            if (((Integer) (((Map) (scores.get(new Integer(1))))
                    .get(new Integer(0))))
                    .doubleValue()
                    < 0) {
                computeSimRank();
                return simRank(id);
            }
        }
        return (Map) (scores.get(id));
    }

    /**
     * Returns the SimRank score between two given link identifyers
     * Identifyers are Integer numberes, used in <code>WebGraph</code> to
     * represent the Web graph for efficiency reasons.
     *
     * @param link1 The identifyer for one of the links
     * @param link2 The identifyer for the other link
     * @return The Amsler score between the two given link identifyers
     * @see WebGraph.IdentifyerToURL()
     */
    private Double simRank(Integer id1, Integer id2) {
        if (id1.equals(id2))
            return new Double(1);
        if (id2.intValue() > id1.intValue()) {
            Integer id3 = id1;
            id1 = id2;
            id2 = id1;
        }
        Double aux = (Double) (((Map) (scores.get(id1))).get(id2));
        if (aux.intValue() < 0) {
            computeSimRank();
            return simRank(id1, id2);
        }
        return aux;
    }

}
