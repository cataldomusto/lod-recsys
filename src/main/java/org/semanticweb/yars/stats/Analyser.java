package org.semanticweb.yars.stats;

import org.semanticweb.yars.nx.Node;

import java.util.Iterator;

public interface Analyser extends Iterator<Node[]>{
	public void analyse(Node[] in);
	public void stats();
}
