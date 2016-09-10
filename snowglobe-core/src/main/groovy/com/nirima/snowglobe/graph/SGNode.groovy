package com.nirima.snowglobe.graph

import com.google.common.base.Objects
import com.nirima.snowglobe.core.Context
import com.nirima.snowglobe.core.Dependency
import com.nirima.snowglobe.core.Module
import com.nirima.snowglobe.core.Provider
import com.nirima.snowglobe.core.Resource
import com.nirima.snowglobe.core.SnowGlobe
import com.nirima.snowglobe.core.SnowGlobeContext

/**
 * Created by magnayn on 05/09/2016.
 */
class SGNode {

    SGNode parentNode;

    //Context represents;

    final Object item;

    List<SGNode> dependencies = []
    List<SGNode> children = []

    protected SGNode() {}

    protected SGNode(SGNode parentNode, Object item) {
        this.parentNode = parentNode;
        this.item = item;

        if( parentNode != null )
            parentNode.children << this
    }

    public accept(Object visitor) {
        visitor.visitNode(this);
        children.each { it.accept(visitor) }
    }

    public String getId() {
        String id;
        try {
            id = item.getClass().simpleName;
            id += ":" + item.id;
        } catch(Exception ex) {

        }

        if( parentNode != null )
            return "${parentNode.id}.${id}"
        else
            return id;
    }

    public String getLabel() {
        try {

            return "${item.getClass().getSimpleName()}:${item.id}";
        }
        catch(Exception e)   {
            return item.toString();
        }
    }

    boolean equals(o) {
        if (this.is(o)) {
            return true
        }
        if (getClass() != o.class) {
            return false
        }

        SGNode node = (SGNode) o

        if (getId() != node.getId()) {
            return false
        }

        return true
    }

    int hashCode() {
        return (item != null ? item.hashCode() : 0)
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("item", item)
                .toString();
    }
}

class Graph {
    SGNode rootNode;

    // Should be transient as can re-calc
    private Map<Object, SGNode> graphNodes = [:];
    private Map<String, SGNode> graphNodesById = [:];


    public static Graph Empty() {
        new Graph(new SGNode())
    }

    public SGNode newNode(SGNode parentNode, Context represents) {
        return newNodeObject(parentNode, represents.getProxy());
    }

    public SGNode newNodeObject(SGNode parentNode, Object object) {
        //this.represents = represents

        SGNode node = new SGNode(parentNode, object);

        graphNodes[ object ] = node;
        graphNodesById[ idForNode(node) ] = node;

        return node;
    }

    public SGNode getNodeForItem(Object item) {
        SGNode node = graphNodes[item];
        if( node == null )
            throw new IllegalStateException("No node for ${item}");
        return node;
    }

    Graph(SGNode rootNode) {
        this.rootNode = rootNode
    }

    public void removeNode(SGNode node) {
        assert node != null

        String id = idForNode(node);

        if( node.parentNode != null ) {
            node.parentNode.children.remove(node);
        }

        // Remove the children too???
        if( node.children.size() > 0 ) {
            println "CHILDREN ARE HERE. NOT SURE."
        }

        node.children.each {
            node.parentNode = null;
        }

        graphNodes[node.item] = null;
        graphNodesById[id] = null;
    }



    public void replaceNodeWithThisNode(SGNode node) {
        assert node != null

        SGNode current = graphNodesById[ idForNode(node) ];
        if( current != null )
            removeNode(current);

        SGNode parent = graphNodesById[ idForNode(node.parentNode) ];
        SGNode inserted = newNode(parent, node.item);

        // place in the node
        node.children.each {
            inserted
        }


    }

    public accept(Object visitor) {
        rootNode.accept(visitor);
    }

    List<SGNode> getNodes() {
        List<SGNode> items = [];
        items.addAll(graphNodes.values());
        return items;
    }

    public static String idForNode(SGNode item) {

        String id = "";
        try {
            id = item.getClass().simpleName;
            id += ":" + item.id;
        } catch(Exception ex) {

        }

        if( item.parentNode != null )
            return "${idForNode(item.parentNode)}.${id}"
        else
            return id;
    }
}

class GViz {
    String nodes = "";
    public void visitNode(SGNode n) {

        if( n.item instanceof SnowGlobe ) {
            return;
        }

        if( n.item instanceof Module ) {

            if( nodes.length() > 0 )
                nodes += " }\n";

            nodes = nodes + "\nsubgraph cluster_${n.item.id} {\n";
            nodes += " label = \"${n.id}\"\n";
        }

        nodes = nodes + "\"[root] ${n.id}\" [label = \"${n.getLabel()}\""
        if( n.item instanceof Module ) {
            nodes += ", shape=box"
        }
        if( n.item instanceof Provider ) {
            nodes += ",  style=filled, fillcolor=blue"
        }
        if( n.item instanceof Resource ) {
            Resource res = n.item;

            if( res.savedState == null ) // Create
                nodes += ",  style=filled, fillcolor=green"
            else if( res.state == null ) // Delete
                nodes += ",  style=filled, fillcolor=red"
        }
        nodes += "]\n";

    }

    public String toString() {
        return nodes + "}\n";
    }
}

public class DependencyList_DFS {
    List<SGNode> nodes = [];

    public void insert(SGNode node) {
        if( nodes.contains(node) )
            return; // already seen

        node.dependencies.each {
            insert(it)
        }

        // Try the children too
        node.children.each {
            insert(it)
        }

        nodes << node;
    }

}

public class GraphBuilder {

    public String graphViz(Graph graph) {
        GViz viz = new GViz();
        graph.accept(viz);


        String nodes = "";



        graph.getNodes().each { n ->
            n.dependencies.each { n2 ->
                nodes = nodes + "\"[root] ${n.getId()}\" -> \"[root] ${n2.getId()}\"\n";
            }
            }

        String g = """
digraph snowglobe {

            ${viz}

            ${nodes}

}
"""
       return g;
    }

    public Graph build(SnowGlobeContext snowGlobeContext) {

        // Create all the nodes
        SGNode root = new SGNode(null, snowGlobeContext.getProxy());

        Graph graph = new Graph(root);

        snowGlobeContext.getModules().each {
            SGNode moduleNode = graph.newNode(root, it);

            root.dependencies << moduleNode;

            it.providers.each { p ->
                graph.newNode(moduleNode, p);
            }

            it.dataSources.each { p ->
                graph.newNode(moduleNode, p);
            }

            it.resources.each { r ->
                graph.newNode(moduleNode, r);
            }

        }


        // Create all the edges

        processDependencies(graph, snowGlobeContext.dependencies);

        snowGlobeContext.getModules().each {

            module ->
                processDependencies(graph, module.dependencies);
        }


        return graph;
    }

    private void processDependencies(Graph g, Collection<Dependency> deps ) {
        deps.each {
            dep ->
                SGNode source = g.getNodeForItem(dep.from.getProxy());
                SGNode target = g.getNodeForItem(dep.to.getProxy());

                source.dependencies.add(target);

        }
    }
}