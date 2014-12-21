/*
 * Author : Aidan Hogan
 */
package org.semanticweb.jars2.reconrank.runtime;

/**
 * @author aidhog
 *
 * This class can be called to create a graph from an N3 file.
 * It can perform ranking a create and return an instance of itself
 * with a subgraph speified
 */

import java.util.Iterator;
import java.util.TreeSet;


public class RankingConsole {

    public NamesVector NV;

    public NamesHashTable NHT;

    public ConnectivityMatrix CM;

    public WeightsMatrix weightMatrix;

    public double[] outlinks;

    public LinksRank LR; //ranking methods

    public boolean qe = false;
    public boolean weights = false;
    public boolean quads = false;
    public int _size;

    //public Timer t;
    //defines whether a node is...
    //-1 not rankable
    //0 included in ranking but not returned in results
    //1 rankable resource
    //2 rankable context
    //3 rankable resource and context
    private int[] type;

    public RankingConsole(int size, boolean _qe) {
        _size = size;
        qe = _qe;

        outlinks = new double[_size];
        type = new int[_size];

        for (int i = 0; i < _size; i++) {
            outlinks[i] = 0;
            type[i] = -1;
        }

        NV = new NamesVector(size);
        NHT = new NamesHashTable(size);
        CM = new ConnectivityMatrix(size);
        weightMatrix = new WeightsMatrix(size);
    }

    public static void main(String args[]) {
        RankingConsole rc = new RankingConsole(5, false);
        rc.addLink("a", "b");
        rc.addLink("b", "c");
        rc.addLink("b", "d");
        rc.addLink("c", "a");
        rc.addLink("c", "b");
        rc.addLink("e", "e");
        rc.rank();
        System.out.println(rc.LR.getSummation());
        rc.LR.print();
        System.out.println(rc.LR.iterationsDone + " " + rc.LR.l1residual);
    }

    public void rank() {
        LR = new LinksRank(this);
    }

    //dumps an ordered list of results of the entire graph of the instance
    public TreeSet[] allResults() {
        TreeSet<Pair>[] ordered = new TreeSet[2];
        ordered[0] = new TreeSet<Pair>();
        ordered[1] = new TreeSet<Pair>();

        Iterator iter = NV.getIterator();
        int index = 0;
        double rank;
        String temp;


        while (iter.hasNext()) {

            temp = (String) iter.next();
            rank = LR.getRankOfNode(index);
            Pair p = new Pair(rank, temp);

            if (type[index] == 1) {
                ordered[0].add(p);
            } else if (type[index] == 2) {
                ordered[1].add(p);
            } else if (type[index] == 3) {
                ordered[0].add(p);
                ordered[1].add(p);
            }
            index++;
        }
        return ordered;
    }

    //This method takes the link specified by the two arguments
    //and uses it to update the relevant matrices
    public int[] addLink(String out, String in) {
        int[] links = new int[2];
        links[0] = NHT.findName(out);
        links[1] = NHT.findName(in);

        if (links[0] < 0) {
            links[0] = NHT.addName(out);
            if (NHT.unique) {
                NV.addName(out);
            }
        }

        if (links[1] < 0) {
            links[1] = NHT.addName(in);
            if (NHT.unique) {
                NV.addName(in);
            }
        }


        int elementIndex = -1;
        if (links[0] != links[1]) {
            elementIndex = CM.addToRow(links[1], links[0], true);
            if (CM.uniqueLink) {
                outlinks[links[0]]++;
            }
        }

        CM.setRowCount(NV.size());

        int[] result = new int[3];
        result[0] = links[0];
        result[1] = links[1];
        result[2] = elementIndex;
        return result;
    }

    //This method takes the link specified by the two arguments
    //and uses it to update the relevant matrices
    public int[] addLink(String out, int outtype, String in, int intype) {
        int[] links = addLink(out, in);
        type[links[0]] = outtype;
        type[links[1]] = intype;

        return links;
    }

    //This method takes the link specified by the two arguments
    //and uses it to update the relevant matrices
    public int[] addLink(String out, int outtype, String in, int intype, double weight) {
        int[] links = addLink(out, outtype, in, intype);
        if (links[2] < 0)
            return links;

        //System.out.println(out+" "+in+" "+weight);

        weights = true;

        if (CM.uniqueLink)
            outlinks[links[0]]--;

        double w = weightMatrix.getElementAt(links[1], links[2]);

        weightMatrix.setRowCount(NV.size());

        if (w == -1)
            weightMatrix.setValueAt(links[1], links[2], weight);
        else
            weightMatrix.setValueAt(links[1], links[2], weight + w);


        outlinks[links[0]] += weight;

        //System.out.println(outlinks[links[0]]);

        return links;
    }

    public int addNode(String node) {
        int index = NHT.addName(node);
        if (NHT.unique)
            NV.addName(node);
        return index;
    }

	
	/*
    //print results in n3 format
	public void printN3Results(PrintWriter pw) throws IOException{
		System.out.println("Ordering and printing resource results");
		t.reset();
		TreeSet[] ts = allResults();

		Iterator iter = ts[0].iterator();
		int i = 1;
		while(iter.hasNext()){
			Pair p = (Pair) iter.next();
			pw.print(p._subject);
			
			pw.print(" <http://sw.deri.org/2005/05/ranking#resourceReConRankScore> \""+p._rank+"\" .\n");
			i++;
		}
		t.print("Sort and output resources to file :");
		
		System.out.println("Ordering and printing context results");
		t.reset();
		
		if(conoutfile!=null){
			pw.close();
			pw = new PrintWriter(new FileWriter(conoutfile));
		}

		iter = ts[1].iterator();
		i = 1;
		while(iter.hasNext()){
			Pair p = (Pair) iter.next();

			pw.print(p._subject);
			
			pw.print(" <http://sw.deri.org/2005/05/ranking#contextReConRankScore> \""+p._rank+"\" .\n");
			i++;
		}
		pw.flush();
		t.print("Sort and output resources to file :");
	}
	
	//print results in n4 format
	public void printN4Results(PrintWriter pw) throws IOException{
		System.out.println("Ordering and printing resource results");
		t.reset();
		TreeSet[] ts = allResults();

		Iterator iter = ts[0].iterator();
		int i = 1;
		while(iter.hasNext()){
			Pair p = (Pair) iter.next();
			pw.print("{ ");
			pw.print(p._subject);
			
			pw.print(" <http://sw.deri.org/2005/05/ranking#resourceReConRankScore> \""+p._rank+"\" .}  <http://sw.deri.org/2004/06/yars#context> <http://sw.deri.org/2005/05/ranking#RankingEngine> .\n");
			i++;
		}
		t.print("Sort and output resources to file :");
		
		System.out.println("Ordering and printing context results");
		t.reset();
		
		if(conoutfile!=null){
			pw.close();
			pw = new PrintWriter(new FileWriter(conoutfile));
		}

		iter = ts[1].iterator();
		i = 1;
		while(iter.hasNext()){
			Pair p = (Pair) iter.next();
			pw.print("{ ");
			pw.print(p._subject);
			pw.print(" <http://sw.deri.org/2005/05/ranking#contextReConRankScore> \""+p._rank+"\" .} <http://sw.deri.org/2004/06/yars#context> <http://sw.deri.org/2005/05/ranking#RankingEngine> .\n");
			i++;
		}
		pw.flush();
		pw.close();
		t.print("Sort and output resources to file :");
	}
	*/

    public int addNode(String node, int _type) {
        int index = addNode(node);
        type[index] = _type;
        return index;
    }
}



