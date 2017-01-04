package ch.uzh.bf;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;

import org.neo4j.graphalgo.GraphAlgoFactory;
import org.neo4j.graphalgo.PathFinder;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Path;
import org.neo4j.graphdb.PathExpanders;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.ResourceIterator;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.tooling.GlobalGraphOperations;

import ch.uzh.bf.XMain.MyDatabasePath;

public class MyNeo4j {
	
	/*
	 * Based on Neo4j 2.3.8
	 */

	public static GraphDatabaseService gdbs;

	private MyDatabasePath cfg;
	private MyBowtieNetwork nw;

	public MyNeo4j(MyDatabasePath cfg){
		this.cfg = cfg;
	}

	// Create
	public void create() {
		nw = new MyBowtieNetwork();
		nw.build();
	}

	// Simple stuff
	public void listAllNodesAndRels() {
		System.out.println("\n### List all nodes and rels");
		Transaction tx = gdbs.beginTx();
		try{
			for (Node node : GlobalGraphOperations.at(gdbs).getAllNodes()) {
				System.out.print(node.getProperty(StaticConfig.nodeName) + "; ");
			}
			System.out.println("");
			for (Relationship rel : GlobalGraphOperations.at(gdbs).getAllRelationships()) {
				System.out.println(rel.getStartNode().getProperty(StaticConfig.nodeName) + " --[ " + rel.getProperty(StaticConfig.weight) + " ]-> " + rel.getEndNode().getProperty(StaticConfig.nodeName));
			}
			tx.success();
		}
		finally {
			tx.close();
		}
	}

	public Node getNodeByName(String name) {
		Node node = null;
		Transaction tx = gdbs.beginTx();
		try{
			node = gdbs.findNode(StaticConfig.NodeLabel.MYNODE, StaticConfig.nodeName, name);
			if (node == null) {
				System.out.println("Didn't find " + name);
			}
			tx.success();
		}
		finally {
			tx.close();
		}
		return node;
	}

	// Console output
	public void listBowtieComponent(String name) {
		System.out.println("\n### List bowtie components");
		ResourceIterator<Node> nodes = null;
		Transaction tx = gdbs.beginTx();
		try{
			nodes = gdbs.findNodes(StaticConfig.NodeLabel.MYNODE, StaticConfig.nodeBT, name);
			System.out.println(StaticConfig.nodeBT + " = " + name);
			while (nodes.hasNext()) {
				Node n = nodes.next();
				System.out.print(n.getProperty(StaticConfig.nodeName) + "; ");
			}
			System.out.println("");
			tx.success();
		}
		finally {
			tx.close();
		}
	}

	public void shortestPath(Node start, Node end) {
		Transaction tx = gdbs.beginTx();
		try{
			System.out.println("\n### Shortest path between " + start.getProperty(StaticConfig.nodeName) + " and " + end.getProperty(StaticConfig.nodeName));

			int maxlength = 100;

			// Set up 
			PathFinder<Path> finder = GraphAlgoFactory.shortestPath(PathExpanders.forTypeAndDirection(StaticConfig.relType, StaticConfig.outDir), maxlength);
			// Find
			Iterator<Path> paths = finder.findAllPaths(start, end).iterator();
			int i = 1;
			while (paths.hasNext()) {
				Path path = paths.next();
				printPath(path, i);
				i++;
			}
			tx.success();
		}
		finally {
			tx.close();
		}
	}

	// Bowtie
	public void bowtie() {
		Transaction tx = gdbs.beginTx();
		try{
			DetectBowtie bt = new DetectBowtie();
			// SCC analysis and LCC
			ArrayList<Integer> lscc = bt.sccAnalysis();

			// Bowtie
			if (lscc.size() > 0)
				bt.bowTie(lscc, tx);
			tx.success();
		}
		finally {
			tx.close();
		}
	}

	// Drop database
	public void dropDatabase() {
		System.out.println("Dropping db in " + cfg.name);
		shutdown();
		dropDatabase(new File(cfg.name));
		startup();
	}

	private void dropDatabase(final File file) {
		if (file.exists()) {
			if (file.isDirectory()) {
				for (File child : file.listFiles()) {
					dropDatabase(child);
				}
			}
			file.delete();
		}
	}

	// Console output
	private void printPath(Path myLinks, int no) {
		System.out.println("Path " + no + ":");
		Iterator<Relationship> rels = myLinks.relationships().iterator();
		while (rels.hasNext()) {
			Relationship rel = rels.next();
			System.out.println(rel.getStartNode().getProperty(StaticConfig.nodeName) + " --[ " + rel.getProperty(StaticConfig.weight) + " ]-> " + rel.getEndNode().getProperty(StaticConfig.nodeName));
		}
	}

	// Set up Neo4j database
	// Version 2.3.8
	public void startup() {
		System.out.println("*** Starting on " + cfg.name + "\n");
		File db = new File(cfg.name);
		gdbs = new GraphDatabaseFactory().newEmbeddedDatabaseBuilder(db).
				newGraphDatabase();
	}

	public void shutdown() {
		gdbs.shutdown();
		System.out.println("*** Shutdown");
	}
}
