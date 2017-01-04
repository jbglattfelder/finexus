package ch.uzh.bf;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Stack;

import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Path;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.traversal.Evaluators;
import org.neo4j.graphdb.traversal.Traverser;
import org.neo4j.graphdb.traversal.TraversalDescription;
import org.neo4j.graphdb.traversal.Uniqueness;
import org.neo4j.tooling.GlobalGraphOperations;

public class DetectBowtie {

	// SCC code taken from:
	// http://algs4.cs.princeton.edu/42directed/TarjanSCC.java.html
	private boolean[] marked;        // marked[v] = has v been visited?
	private int[] id;                // id[v] = id of strong component containing v
	private int[] low;               // low[v] = low number of v
	private int pre;                 // preorder number counter
	private int count;               // number of strongly-connected components
	private Stack<Integer> stack;

	private int size;
	private int maxId;

	public DetectBowtie() {
		System.out.println("\n### Detect bowtie");
		size = 0;
		for (@SuppressWarnings("unused") Node node : GlobalGraphOperations.at(MyNeo4j.gdbs).getAllNodes()) {
			size++;
		}
		maxId = size - 1;
		marked = new boolean[maxId + 1];
		stack = new Stack<Integer>();
		id = new int[maxId + 1]; 
		low = new int[maxId + 1];
	}

	// Find all strongly connected components (SCC) and the largest  connected component(LCC)
	public ArrayList<Integer> sccAnalysis() {
		System.out.println("# Bowtie analysis: SCC and LCC");
		// Analyze
		for (Node node : GlobalGraphOperations.at(MyNeo4j.gdbs).getAllNodes()) {
			try {
				if (!marked[(int) node.getId()])
					dfs(node);
			}
			catch (Exception e) {
				System.err.println(e);
			}
		}

		// Output
		int M = count();

		// Compute list of nodes in each strong component
		@SuppressWarnings("unchecked")
		Queue<Integer>[] components = (Queue<Integer>[]) new Queue[M];
		for (int i = 0; i < M; i++) {
			components[i] = new LinkedList<Integer>();
		}
		for (Node node : GlobalGraphOperations.at(MyNeo4j.gdbs).getAllNodes()) {
			int v = (int) node.getId();
			components[id(v)].add(v);
		}

		// Print results
		System.out.println("Ids of nodes in components (with at least two nodes)");
		int mycount = 0;
		int maxSccSize = 0;
		for (int i = 0; i < M; i++) {
			if (components[i].size() > 1) {
				int size = components[i].size();
				maxSccSize = (size > maxSccSize) ? size : maxSccSize;
				mycount++;
			}
		}

		// Histo and LCC
		HashMap<Integer, Integer> histo = new HashMap<Integer, Integer> ();
		ArrayList<Integer> lscc = new ArrayList<Integer>();
		for (int i = 0; i < M; i++) {
			int size = components[i].size();
			if (size > 1) {
				if (!histo.containsKey(size)) {
					histo.put(size, 1);
				}
				else {
					histo.put(size, histo.get(size) + 1);
				}
			}
			if (size == maxSccSize) {
				System.out.println("Found largest SCC with " + maxSccSize + " nodes");
				for (int v : components[i]) {
					lscc.add(v);
				}
			}
			// SCCs with no OUT
			// Only once per SCC
			if (size > 1) {;
			int id = components[i].element();
			Node n = MyNeo4j.gdbs.getNodeById(id);
			TraversalDescription td = MyNeo4j.gdbs.traversalDescription().breadthFirst()
					.relationships(StaticConfig.RelType.OWNS, StaticConfig.outDir).evaluator(Evaluators.excludeStartPosition());
			Traverser friendsTraverser = td.traverse(n);
			// See if there are any nodes downstream which are not in the SCC, i.e., OUT-nodes
			for (Path p : friendsTraverser) {
				if (!components[i].contains((int) p.endNode().getId())) {
					break;
				}
			}
			}
		}
		System.out.println(mycount + " component(s) (with more than one node)");
		System.out.println("Comp size histo ({size, frequ}): " + " " + histo.toString());

		System.out.println("#Bowtie analysis: SCC nodes in LCC " + lscc.size());
		System.out.print("\t");
		for (int id : lscc) {
			System.out.print(MyNeo4j.gdbs.getNodeById(id).getProperty(StaticConfig.nodeName) + "; ");
		}
		System.out.print("");
		return lscc;
	}

	// Find other bowtie components: IN, OUT, TT
	public void bowTie(ArrayList<Integer> lscc, Transaction tx) {
		HashSet<Integer> in = new HashSet<Integer>();
		HashSet<Integer> out = new HashSet<Integer>();
		HashSet<Integer> tt = new HashSet<Integer>();
		HashSet<Integer> occ = new HashSet<Integer>();

		// All
		for (Node node : GlobalGraphOperations.at(MyNeo4j.gdbs).getAllNodes()) {
			int test = (int) node.getId();
			occ.add(test);
		}
		HashSet<Integer> all = new HashSet<Integer>(occ);
		for (int id : lscc) {
			occ.remove(id);
		}

		// OUT
		System.out.println("");
		System.out.println("# Bowtie analysis: OUT");
		Node seed = MyNeo4j.gdbs.getNodeById(lscc.get(0));
		TraversalDescription td = MyNeo4j.gdbs.traversalDescription().breadthFirst()
				.relationships(StaticConfig.relType, StaticConfig.outDir)
				.evaluator( Evaluators.excludeStartPosition());
		Traverser traverser = td.traverse(seed);

		for (Path outPath : traverser) {
			int test = (int) outPath.endNode().getId();
			out.add(test);
		}

		for (int s : lscc) {
			out.remove(s);
		}

		for (int o : out) {
			occ.remove(o);
		}
		System.out.println("OUT nodes " + out.size());
		printNodes(out);

		// IN
		System.out.println("# Bowtie analysis: IN");
		td = MyNeo4j.gdbs.traversalDescription().breadthFirst()
				.relationships(StaticConfig.relType, StaticConfig.inDir)
				.evaluator( Evaluators.excludeStartPosition() );
		traverser = td.traverse(seed);

		for (Path inPath : traverser) {
			int test = (int) inPath.endNode().getId();
			if (!lscc.contains(test)) {
				in.add(test);
				occ.remove(test);
			}
		}
		System.out.println("IN nodes " + in.size());
		printNodes(in);

		// LCC
		td = MyNeo4j.gdbs.traversalDescription().breadthFirst()
				.relationships(StaticConfig.relType, StaticConfig.bothDir).uniqueness(Uniqueness.NODE_GLOBAL);
		Traverser nodeTraverser = td.traverse(seed);
		for (Path myLinks : nodeTraverser) {
			int id1 = (int) myLinks.endNode().getId();
			int id2 = (int) myLinks.startNode().getId();
			tt.add(id1);
			tt.add(id2);
		}

		// TT
		System.out.println("# Bowtie analysis: TT and OCC");
		for (int test : all) {
			if (lscc.contains(test) || in.contains(test) || out.contains(test)) {
				tt.remove(test);
			}
		}

		Integer[] del = occ.toArray(new Integer[occ.size()]);
		for (int i : del) {
			if (tt.contains(i)) {
				occ.remove(i);
			}
		}

		System.out.println("TT nodes " + tt.size());
		printNodes(tt);

		System.out.println("# Bowtie analysis: number of nodes");

		System.out.println("TT nodes: " + tt.size());
		int lcc = tt.size() + in.size() + lscc.size() + out.size();
		System.out.println("LCC nodes: " + lcc);
		System.out.println("OCC nodes: " + occ.size());
		System.out.println("All nodes: " + (occ.size()+lcc));

		// Set properties
		for (Node node : GlobalGraphOperations.at(MyNeo4j.gdbs).getAllNodes()) {
			int myId = (int) node.getId();
			try {
				if (in.contains(myId)) {
					if (node.hasProperty(StaticConfig.nodeBT))
						throw new Exception(myId + " already has prop " + node.getProperty(StaticConfig.nodeBT));
					node.setProperty(StaticConfig.nodeBT, StaticConfig.BowTie.IN.toString());
				}
			} catch (Exception e) {
				System.err.println(e.getMessage());
			}
			try {
				if (lscc.contains(myId)) {
					if (node.hasProperty(StaticConfig.nodeBT))
						throw new Exception(myId + " already has prop " + node.getProperty(StaticConfig.nodeBT));
					node.setProperty(StaticConfig.nodeBT, StaticConfig.BowTie.SCC.toString());
				}
			} catch (Exception e) {
				System.err.println(e.getMessage());
			}
			try {
				if (out.contains(myId)) {
					if (node.hasProperty(StaticConfig.nodeBT))
						throw new Exception(myId + " already has prop " + node.getProperty(StaticConfig.nodeBT));
					node.setProperty(StaticConfig.nodeBT, StaticConfig.BowTie.OUT.toString());
				}
			} catch (Exception e) {
				System.err.println(e.getMessage());
			}
			try {
				if (tt.contains(myId)) {
					if (node.hasProperty(StaticConfig.nodeBT))
						throw new Exception(myId + " already has prop " + node.getProperty(StaticConfig.nodeBT));
					node.setProperty(StaticConfig.nodeBT, StaticConfig.BowTie.TT.toString());
				}
			} catch (Exception e) {
				System.err.println(e.getMessage());
			}
			try {
				if (occ.contains(myId)) {
					if (node.hasProperty(StaticConfig.nodeBT))
						throw new Exception(myId + " already has prop " + node.getProperty(StaticConfig.nodeBT));
					node.setProperty(StaticConfig.nodeBT, StaticConfig.BowTie.OCC.toString());
				}
			} catch (Exception e) {
				System.err.println(e.getMessage());
			}
		}
	}

	// *** SCC methods
	
	// Depth first search
	private void dfs(Node n) {
		// Depth first search
		int v = (int) n.getId();
		marked[v] = true;
		low[v] = pre++;
		int min = low[v];
		stack.push(v);

		// Traverse one step downstream
		TraversalDescription td = MyNeo4j.gdbs.traversalDescription().breadthFirst()
				.relationships(StaticConfig.relType, StaticConfig.outDir).evaluator(Evaluators.excludeStartPosition());
		Traverser friendsTraverser = td.traverse(n);

		// Collect all adjacent nodes
		Iterable<Node> adj = new ArrayList<Node>();
		for (Path p : friendsTraverser) {
			if (p.length() == 1) {
				Node mynode = p.endNode();
				((ArrayList<Node>)adj).add(mynode);
			}
		}

		// Loop
		for (Node node : adj) {
			int w = (int) node.getId();
			if (!marked[w])
				dfs(node);
			if (low[w] < min) min = low[w];
		}
		if (min < low[v]) { low[v] = min; return; }
		int w;
		do {
			w = stack.pop();
			id[w] = count;
			low[w] = size;
		} while (w != v);
		count++;
	}

	private int count() {
		return count;
	}

	private int id(int v) {
		return id[v];
	}

	private static void printNodes(HashSet<Integer> set) {
		System.out.print("\t");
		for (int id : set) {
			System.out.print(MyNeo4j.gdbs.getNodeById(id).getProperty(StaticConfig.nodeName) + "; ");
		}
		System.out.println("");
	}
}
