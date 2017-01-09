# BigDataFinance Winter School Tutorial

See doc.pdf in README/ directory

### Aim

1. Grab code:
    * ```git clone https://github.com/jbglattfelder/finexus.git```
2. Neo4j preliminary:
    1. NB: Version 3.X only works with Java 8: This code is for version 2.3.8 with Java 7
    2. Download: https://neo4j.com/download/other-releases/
3. Neo4j embedded Java:
    1. Eclipse IDE https://eclipse.org/downloads/packages/eclipse-ide-java-developers/neon2
    2. Start new Java project and point to downloaded *finexus/src/ch/uzh/bf* source directory 
    3. Add Neo4j libraries (external jar-files from Neo4j *lib/* directory)
    4. Create *finexus/data/* directory in current path
    4. Add this data directory path name to the configuration in *Xmain.java*
    5. Run or debug Java class *XMain.java*
4. Neo4j Cypher query language:
    1. Link *finexus/data/* directory in Neo4j to the one created above
    2. ```neo4j-start``` and ```neo4j-shell```
    3. See code examples in *finexus/Cypher/* directory
5. Matlab and CSV: Data examples from Orbis and Matlab code in *finexus/Matlab/* directory
  * *Output/* directory contains parsed CSV files for Gephi (https://gephi.org/) and a layout example
