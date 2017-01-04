// Simple queries
MATCH (n) WHERE n.BOWTIE='SCC' RETURN COUNT(n);

MATCH (n) WHERE n.BOWTIE='IN' RETURN n ORDER BY n.VAL DESC LIMIT 3;

// Links: SCC -> OUT
MATCH (n) WHERE n.BOWTIE='SCC' 
MATCH (m) WHERE m.`BOWTIE`='OUT' 
MATCH (n)-[r]-m RETURN COUNT(r);

// WITH
MATCH (n) WHERE n.BOWTIE='IN'
WITH COUNT(n) AS noIn
MATCH (n) WHERE n.BOWTIE='OUT'
WITH  noIn, COUNT(n) AS noOut
RETURN noIn+noOut;

// Direct portfolio value
MATCH n-[r]->m
WITH n, m, m.VALUE * r.WEIGHT_MERGED as dirval
RETURN n.NAME, SUM(dirval) as dirpf, COUNT(m) AS cnt ORDER BY dirpf DESC;

// Roots
MATCH (root) WHERE NOT (root)<-[]-() RETURN (root.NAME);

// Shortest path
MATCH (a), (b) WHERE id(a)=0 and id(b)=23 RETURN a,b;

MATCH (a), (b) WHERE id(a)=0 and id(b)=23
MATCH p=shortestPath((a)-[:OWNS*]->(b))
RETURN length(p) AS len;
 
MATCH (a), (b) WHERE id(a)=0 and id(b)=23
MATCH p=shortestPath((a)-[:OWNS*]->(b))
RETURN EXTRACT(x IN NODES(p) | x.NAME) AS path;

