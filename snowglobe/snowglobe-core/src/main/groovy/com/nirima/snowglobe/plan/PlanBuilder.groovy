package com.nirima.snowglobe.plan

import com.nirima.snowglobe.core.Resource
import com.nirima.snowglobe.core.ResourceContext
import com.nirima.snowglobe.core.ResourceState
import com.nirima.snowglobe.core.SnowGlobeContext
import com.nirima.snowglobe.graph.DependencyList_DFS
import com.nirima.snowglobe.graph.Graph
import com.nirima.snowglobe.graph.GraphBuilder
import com.nirima.snowglobe.graph.SGNode

/**
 * Created by magnayn on 05/09/2016.
 */

enum PlanActionActivity {
    Create,
    Update,
    Delete,
    Recreate
}


interface PlanAction<T extends Resource, V extends ResourceState> {

    //void execute(Plan plan, String phase);

    T getResource();


    public V create(V t);
    public V read(V t);
    public V update(V old,V newState);
    public V delete(V t);

}

abstract class PlanActionBase<T extends Resource, V extends ResourceState> implements PlanAction<T,V> {

    private final T resource;

    // TODO: Groovy bug? PlanActionBase(T resource) {
    PlanActionBase(Resource resource) {
        this.resource = resource
    }

    T getResource() {
        resource;
    }

    public V create(V t) { return t; }
    public V read(V t) { return t;}
    public V update(V old, V newState) { return newState; }
    public V delete(V t) { return null;}


}


public class Plan {
    List<PlanAction<?,?>> actions = [];
    PlanType type;

    private final SnowGlobeContext sgContext;

    def phases = ["validate",
                  "read",
                  "create",
    "update","delete"]

    Plan(SnowGlobeContext sgContext, PlanType type) {
        this.sgContext = sgContext
        this.type = type
    }

    public void execute() {
        if (type == PlanType.Apply) {
            apply();
        } else {
            destroy();
        }
    }

    private void apply() {
        /*
         * For each action, if there was some saved state, check it against the
         * actual situation in case there are parameters that are not persisted
         * but can be recovered from live.
         */
        actions.each() { action ->

            Resource resource = action.getResource();

            if( resource.savedState != null ) {
                action.read( resource.savedState );
            }
        }

        actions.each() { action ->

            Resource<? extends ResourceState> resource = action.getResource();

            if( resource.state != null ) {
                // Reevaluate as deps may now be ready
                processResourceState(resource);

                if( resource.savedState == null ) {

                    // nothing saved. these are creates
                    resource.savedState = action.create(resource.state);
                } else {
                    resource.savedState = action.update(resource.savedState, resource.state);
                }

            } else {
                // No state, these are deletes.
                resource.savedState = action.delete(resource.savedState);
            }


        }


    }

    private void destroy() {
        actions.each() { action ->

            Resource resource = action.getResource();

            if( resource.savedState != null ) {
                action.read( resource.savedState );
            }
        }

        // Deletes happen in reverse order
        actions.reverse().each() { action ->

            Resource<? extends ResourceState> resource = action.getResource();

            if( resource.savedState != null ) {
                // Reevaluate as deps may now be ready
                processResourceState(resource);
                resource.savedState = action.delete(resource.savedState);

            } else {

            }
        }


    }

    private void processResourceState(Resource<? extends ResourceState> resource) {
        // Between first eval and now, a value may have changed (or become resolvable).

        // Need to get the ResourceContext for this resource
        ResourceContext ctx = sgContext.getContextFor(resource)

        ctx.stateContext.build();


    }

}

public enum NodePairStatus {
    Create,Update,Delete
}

public class NodePair
{
    final SGNode oldNode, newNode;

    NodePair(SGNode oldNode, SGNode newNode) {
        assert( oldNode != null || newNode != null)
        this.oldNode = oldNode
        this.newNode = newNode
    }

    NodePairStatus getStatus() {
        if( oldNode == null ) {
            return NodePairStatus.Create;
        }
        if( newNode == null )
            return NodePairStatus.Delete;

        return NodePairStatus.Update;
    }

    Object getElement() {
        if( newNode != null )
            return newNode.item;
        return oldNode.item;
    }

    boolean equals(o) {
        if (this.is(o)) {
            return true
        }
        if (getClass() != o.class) {
            return false
        }

        NodePair nodePair = (NodePair) o

        if (newNode != nodePair.newNode) {
            return false
        }
        if (oldNode != nodePair.oldNode) {
            return false
        }

        return true
    }

    int hashCode() {
        int result
        result = (oldNode != null ? oldNode.hashCode() : 0)
        result = 31 * result + (newNode != null ? newNode.hashCode() : 0)
        return result
    }
}

public class PlanBuilder {

    Plan plan;
    DependencyList_DFS list = new DependencyList_DFS();

    public Plan buildPlan(SnowGlobeContext sgContext, PlanType type) {
        assert sgContext != null
        plan = new Plan(sgContext,type);

        Graph stateGraph = new GraphBuilder().build(sgContext);

        list.insert(stateGraph.getRootNode());

        // Create, Update (or, same)
        list.nodes.each { node ->
            Object o = node.item
            if( o instanceof Resource ) {

                PlanAction action = o.assess()
                if( action != null )
                    plan.actions << action;
            }
        }



        return plan;
    }


}

