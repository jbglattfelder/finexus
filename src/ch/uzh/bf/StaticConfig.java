package ch.uzh.bf;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.RelationshipType;

public final class StaticConfig {

	//private  StaticConfig() {}

	// *** Relations
	public static enum RelType implements RelationshipType {OWNS};
	public static RelType relType = RelType.OWNS;

	public static enum RelProps {WEIGHT_MERGED};
	public static String weight = RelProps.WEIGHT_MERGED.toString();

	public static Direction outDir = Direction.OUTGOING;
	public static Direction inDir = Direction.INCOMING;
	public static Direction bothDir = Direction.BOTH;

	// *** Nodes
	public static enum NodeLabel implements Label {MYNODE};

	public static enum NodeProps {NAME, VALUE, BOWTIE};
	public static String nodeName = NodeProps.NAME.toString();
	public static String nodeBT = NodeProps.BOWTIE.toString();
	public static String nodeVal = NodeProps.VALUE.toString();
	
	// *** Bowtie
	public static enum BowTie {IN, OUT, SCC, TT, OCC};
}
