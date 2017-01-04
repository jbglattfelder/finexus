/* Code examples for the Big Data Finance Winter School
 * http://www.finexus.uzh.ch/en/events/bigdatafinance-school.html
 * 
 * Stefano Battiston
 * Department of Banking and Finance
 * University of Zurich 
 * Andreasstrasse 15, 8050 Zurich
 * http://www.bf.uzh.ch/
 * 
 * Author: 
 * James B. Glattfelder
 * james.glattfelder@uzh.ch
 * 
 * 2017
 * 
 * Dependencies:
 * 			Graph database: Neo4j version 2.3.8 https://neo4j.com/download/other-releases/
 * 
 * Built using Oracle's Java 7 on Ubuntu Trusty
 */

package ch.uzh.bf;

import org.neo4j.graphdb.Node;

public class XMain {

	private MyNeo4j neo;
	private MyDatabasePath cfg;

	// Main method
	public static void main(String[] args) {
		XMain inst = new XMain();
		inst.Run();
	}

	// Instance method
	public void Run() {
		cfg = new MyDatabasePath();
		neo = new MyNeo4j(cfg);

		// Neo4j infrastructure
		neo.startup();

		// Create sample bowtie network
		neo.dropDatabase();
		neo.create();

		// Find bowtie components
		neo.bowtie();

		// Basic Neo4j stuff
		neo.listAllNodesAndRels();
		Node start = neo.getNodeByName("i1");
		Node end = neo.getNodeByName("o6");
		neo.shortestPath(start, end);
		neo.listBowtieComponent("SCC");

		//
		neo.shutdown();
	}

	// *** Inner class for dynamic config
	public class MyDatabasePath {

		public String name;

		public MyDatabasePath () {
			// Neo4j infrastructure
			name = "/home/jbg/work2/SIMPOL/code/eclipse/Neo4jFinexusTutorial/data/graph.db";
		}
	}
}
