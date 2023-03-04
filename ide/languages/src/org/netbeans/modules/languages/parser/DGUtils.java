/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.languages.parser;

import java.util.*;

/**
 *
 * @author Jan Jancura
 */
public class DGUtils {


    public static <N,E,K,V> DG<N,E,K,V> cloneDG (DG<N,E,K,V> dg, boolean cloneProperties, NodeFactory<N> nodeFactory) {
        DG<N,E,K,V> ndg = DG.<N,E,K,V>createDG ();
        Map<N,N> oldToNew = new HashMap<N,N> ();
        Iterator<N> it = dg.getNodes ().iterator ();
        while (it.hasNext ()) {
            N oldNode = it.next ();
            N newNode = oldToNew.get (oldNode); 
            if (newNode == null) {
                newNode = nodeFactory.createNode ();
                ndg.addNode (newNode);
                oldToNew.put (oldNode, newNode);
                if (cloneProperties)
                    ndg.putAllProperties (newNode, dg.getProperties (oldNode));
            }
            Iterator<E> it2 = dg.getEdges (oldNode).iterator ();
            while (it2.hasNext ()) {
                E edge = it2.next ();
                N oldEnd = dg.getNode (oldNode, edge);
                N newEnd = oldToNew.get (oldEnd); 
                if (newEnd == null) {
                    newEnd = nodeFactory.createNode ();
                    ndg.addNode (newEnd);
                    oldToNew.put (oldEnd, newEnd);
                    if (cloneProperties)
                        ndg.putAllProperties (newEnd, dg.getProperties (oldEnd));
                }
                ndg.addEdge (newNode, newEnd, edge);
                if (cloneProperties)
                    ndg.putAllProperties (newNode, edge, dg.getProperties (oldNode, edge));
            }
            if (dg.getEnds ().contains (oldNode))
                ndg.addEnd (newNode);
        }
        N newStart = oldToNew.get (dg.getStartNode ());
        ndg.setStart (newStart);
        return ndg;
    }
    
    
    public static <N,E,K,V> DG<N,E,K,V> append (DG<N,E,K,V> dg1, DG<N,E,K,V> dg2, E star, NodeFactory<N> nodeFactory) {
        DG<N,E,K,V> ndg = DG.<N,E,K,V>createDG ();
        Set<N> newStart = new HashSet<N> ();
        newStart.add (dg1.getStartNode ());
        if (dg1.getEnds ().contains (dg1.getStartNode ()))
            newStart.add (dg2.getStartNode ());
        Map<Set<N>,N> newToOld = new HashMap<Set<N>,N> ();
        merge (dg1, dg2, newStart, ndg, newToOld, dg1.getEnds (), dg2.getStartNode (), false, true, star, nodeFactory);
        N nnn = newToOld.get (newStart);
        ndg.setStart (nnn);
        return ndg;
    }
    
    public static <N,E,K,V> DG<N,E,K,V> plus (DG<N,E,K,V> dg, E star, NodeFactory<N> nodeFactory) {
        DG<N,E,K,V> ndg = DG.<N,E,K,V>createDG ();
        N what = dg.getStartNode ();
        Set<N> where = dg.getEnds ();
        Set<N> nn = new HashSet<N> ();
        nn.add (dg.getStartNode ());
        if (where.contains (dg.getStartNode ()))
            nn.add (what);
        Map<Set<N>,N> newToOld = new HashMap<Set<N>,N> ();
        merge (dg, dg, nn, ndg, newToOld, where, what, true, true, star, nodeFactory);
        N nnn = newToOld.get (nn);
        ndg.setStart (nnn);
        return ndg;
    }
    
    private static <N,E,K,V> void merge (
        DG<N,E,K,V>         dg1,
        DG<N,E,K,V>         dg2,
        Set<N>              nn,
        DG<N,E,K,V>         ndg,
        Map<Set<N>,N>       newToOld,
        Set<N>              where,
        N                   what,
        boolean             setEnds1,
        boolean             setEnds2,
        E                   star,
        NodeFactory<N>      nodeFactory
    ) {
        N nnn = newToOld.get (nn);
        if (nnn != null) return;
        nnn = nodeFactory.createNode ();
        newToOld.put (nn, nnn);
        ndg.addNode (nnn);

        Map<E,Set<N>> edges = new HashMap<E,Set<N>> ();
        Map<E,Map<K,V>> properties = new HashMap<E,Map<K,V>> ();
        Iterator<N> it = nn.iterator ();
        while (it.hasNext ()) {
            N n = it.next ();
            DG<N,E,K,V> cdg = dg1.containsNode (n) ? dg1 : dg2;
            ndg.putAllProperties (nnn, cdg.getProperties (n));
            if (setEnds1 && dg1.getEnds ().contains (n))
                ndg.addEnd (nnn);
            if (setEnds2 && dg2.getEnds ().contains (n))
                ndg.addEnd (nnn);
            Iterator<E> it2 = cdg.getEdges (n).iterator ();
            while (it2.hasNext ()) {
                E edge = it2.next ();
                Set<N> ends = edges.get (edge);
                Map<K,V> props = properties.get (edge);
                if (ends == null) {
                    ends = new HashSet<N> ();
                    props = new HashMap<K,V> ();
                    edges.put (edge, ends);
                    properties.put (edge, props);
                }
                N en = cdg.getNode (n, edge);
                ends.add (en);
                props.putAll (cdg.getProperties (n, edge));
                if (where.contains (en))
                    ends.add (what);
            }
        }
        it = nn.iterator ();
        while (it.hasNext ()) {
            N n = it.next ();
            DG<N,E,K,V> cdg = dg1.containsNode (n) ? dg1 : dg2;
            N en = cdg.getNode (n, star);
            if (en == null) continue;
            Iterator<E> it2 = edges.keySet ().iterator ();
            while (it2.hasNext ()) {
                E e = it2.next ();
                if (cdg.getNode (n, e) != null) continue;
                edges.get (e).add (en);
                properties.get (e).putAll (cdg.getProperties (n, e));
                if (where.contains (en))
                    edges.get (e).add (what);
            }
        }
        
        Iterator<E> it2 = edges.keySet ().iterator ();
        while (it2.hasNext ()) {
            E edge = it2.next ();
            Set<N> en = edges.get (edge);
            merge (dg1, dg2, en, ndg, newToOld, where, what, setEnds1, setEnds2, star, nodeFactory);
            N enn = newToOld.get (en);
            ndg.addEdge (nnn, enn, edge);
            ndg.putAllProperties (nnn, edge, properties.get (edge));
        }
    }
    
    public static <N,E,K,V> DG<N,E,K,V> merge (DG<N,E,K,V> dg1, DG<N,E,K,V> dg2, E star, NodeFactory<N>  nodeFactory) {
        DG<N,E,K,V> ndg = DG.<N,E,K,V>createDG ();
        Map<Set<N>,N> newToOld = new HashMap<Set<N>,N> ();
        N startNode = merge (
            dg1, dg2, 
            dg1.getStartNode (), 
            dg2.getStartNode (), 
            ndg,
            true, true,
            star,
            nodeFactory,
            newToOld,
            1
        );
        ndg.setStart (startNode);
        return ndg;
    }
    
    private static <N,E,K,V> N merge (
        DG<N,E,K,V>     dg1,
        DG<N,E,K,V>     dg2,
        N               n1,
        N               n2,
        DG<N,E,K,V>     ndg,
        boolean         addEnds1,
        boolean         addEnds2,
        E               star,
        NodeFactory<N>  nodeFactory,
        Map<Set<N>,N>   newToOld,
        int depth
    ) {
        Set<N> nNode = new HashSet<N> ();
        nNode.add (n1);
        nNode.add (n2);
        if (newToOld.containsKey (nNode)) 
            return newToOld.get (nNode);
        N dnode = nodeFactory.createNode ();
        newToOld.put (nNode, dnode);
        ndg.addNode (dnode);
        ndg.putAllProperties (dnode, dg1.getProperties (n1));
        ndg.putAllProperties (dnode, dg2.getProperties (n2));
        if (addEnds1 && dg1.getEnds ().contains (n1))
            ndg.addEnd (dnode);
        if (addEnds2 && dg2.getEnds ().contains (n2))
            ndg.addEnd (dnode);
        
        Set<E> edges2 = new HashSet<E> (dg2.getEdges (n2));
        Iterator<E> it = dg1.getEdges (n1).iterator ();
        while (it.hasNext ()) {
            E edge = it.next ();
            N nn1 = dg1.getNode (n1, edge);
            N nn2 = dg2.getNode (n2, edge);
            Map<K,V> properties = null;
            if ( !edge.equals (star) && 
                 edges2.contains (star) &&
                 nn2 == null
            ) {
                nn2 = dg2.getNode (n2, star);
                properties = dg2.getProperties (n2, star);
            } else
            if (nn2 != null)
                properties = dg2.getProperties (n2, edge);
            N nnode = nn2 == null ?
                merge (dg1, nn1, ndg, addEnds1) :
                merge (dg1, dg2, nn1, nn2, ndg, addEnds1, addEnds2, star, nodeFactory, newToOld, depth + 1);
            ndg.addEdge (dnode, nnode, edge);
            ndg.putAllProperties (dnode, edge, dg1.getProperties (n1, edge));
            if (properties != null)
                ndg.putAllProperties (dnode, edge, properties);
            edges2.remove (edge);
        }
        it = edges2.iterator ();
        while (it.hasNext ()) {
            E edge = it.next ();
            N nn2 = dg2.getNode (n2, edge);
            N nnode = null;
            Map<K,V> properties = null;
            if ( !edge.equals (star) && 
                 dg1.getEdges (n1).contains (star)
            ) {
                nnode = merge (dg1, dg2, dg1.getNode (n1, star), nn2, ndg, addEnds1, addEnds2, star, nodeFactory, newToOld, depth + 1);
                properties = dg1.getProperties (n1, star);
            } else
                nnode = merge (dg2, nn2, ndg, addEnds2);
            ndg.addEdge (dnode, nnode, edge);
            ndg.putAllProperties (dnode, edge, dg2.getProperties (n2, edge));
            if (properties != null)
                ndg.putAllProperties (dnode, edge, properties);
        }
        return dnode;
    }
    
    private static <N,E,K,V> N merge (
        DG<N,E,K,V>     dg,
        N               n,
        DG<N,E,K,V>     ndg,
        boolean         addEnds
    ) {
        if (ndg.containsNode (n)) return n;
        ndg.addNode (n);
        ndg.putAllProperties (n, dg.getProperties (n));
        if (addEnds && dg.getEnds ().contains (n))
            ndg.addEnd (n);
        
        Iterator<E> it = dg.getEdges (n).iterator ();
        while (it.hasNext ()) {
            E edge = it.next ();
            N nn = dg.getNode (n, edge);
            N endN = merge (dg, nn, ndg, addEnds);
            ndg.addEdge (n, endN, edge);
            ndg.putAllProperties (n, edge, dg.getProperties (n, edge));
        }
        return n;
    }
    
    static <N,E,K,V> DG<N,E,K,V> reduce (DG<N,E,K,V> dg, NodeFactory<N> nodeFactory) {
        Map<Map<K,V>,Set<N>> ends = new HashMap<Map<K,V>,Set<N>> ();
        Set<N> other = new HashSet<N> ();
        Iterator<N> it = dg.getNodes ().iterator ();
        while (it.hasNext ()) {
            N node = it.next ();
            if (!dg.getEnds ().contains (node))
                other.add (node);
            else {
                Set<N> e = ends.get (dg.getProperties (node));
                if (e == null) {
                    e = new HashSet<N> ();
                    ends.put (dg.getProperties (node), e);
                }
                e.add (node);
            }
        }
        Set<Set<N>> newNodes = new HashSet<Set<N>> ();
        if (other.size () > 0) newNodes.add (other);
        newNodes.addAll (ends.values ());
        Map<Set<N>,Map<E,Set<N>>> ng = reduce (dg, newNodes);

        DG<N,E,K,V> ndg = DG.<N,E,K,V>createDG ();
        Map<Set<N>,N> oldToNewNode = new HashMap<Set<N>,N> ();
        Iterator<Set<N>> it2 = ng.keySet ().iterator ();
        while (it2.hasNext ()) {
            Set<N> node = it2.next ();
            N newNode = oldToNewNode.get (node);
            if (newNode == null) {
                newNode = nodeFactory.createNode ();
                oldToNewNode.put (node, newNode);
                ndg.addNode (newNode);
            }
            Map<E,Set<N>> edgeToNode = ng.get (node);
            Iterator<E> it3 = edgeToNode.keySet ().iterator ();
            while (it3.hasNext ()) {
                E edge = it3.next ();
                Set<N> end = edgeToNode.get (edge);
                N newNode2 = oldToNewNode.get (end);
                if (newNode2 == null) {
                    newNode2 = nodeFactory.createNode ();
                    oldToNewNode.put (end, newNode2);
                    ndg.addNode (newNode2);
                }
                ndg.addEdge (newNode, newNode2, edge);
            }
        }
        ndg.setEnds (new HashSet<N> ());
        it2 = ng.keySet ().iterator ();
        while (it2.hasNext ()) {
            Set<N> node = it2.next ();
            N newNode = oldToNewNode.get (node);
            Iterator<N> it3 = node.iterator ();
            while (it3.hasNext ()) {
                N n = it3.next ();
                if (dg.containsNode (n) && dg.getProperties (n) != null)
                    ndg.putAllProperties (newNode, dg.getProperties (n));
                Iterator<E> it4 = ndg.getEdges (newNode).iterator ();
                while (it4.hasNext ()) {
                    E edge = it4.next ();
                    if (dg.containsNode (n) && dg.getProperties (n, edge) != null)
                        ndg.putAllProperties (newNode, edge, dg.getProperties (n, edge));
                }
                if (dg.getEnds ().contains (n))
                    ndg.addEnd (newNode);
                if (dg.getStartNode ().equals (n))
                    ndg.setStart (newNode);
            }
        }
        return ndg;
    }
    
    private static <N,E,K,V> Map<Set<N>,Map<E,Set<N>>> reduce (DG<N,E,K,V> dg, Set<Set<N>> s) {
        Map<N,Set<N>> m = new HashMap<N,Set<N>> ();
        Iterator<Set<N>> it = s.iterator ();
        while (it.hasNext ()) {
            Set<N> nnode = it.next ();
            Iterator<N> it2 = nnode.iterator ();
            while (it2.hasNext ()) {
                N node = it2.next ();
                m.put (node, nnode);
            }
        }
        
        Map<Set<N>,Map<E,Set<N>>> nnodes = new HashMap<Set<N>,Map<E,Set<N>>> ();
        it = s.iterator ();
        while (it.hasNext ()) {
            Set<N> nnode = it.next ();
            Map<Map<E,Set<N>>,Set<N>> nodes = new HashMap<Map<E,Set<N>>,Set<N>> ();
            Iterator<N> it2 = nnode.iterator ();
            while (it2.hasNext ()) {
                N node = it2.next ();
                Map<E,Set<N>> edges = new HashMap<E,Set<N>> ();
                Iterator<E> it3 = dg.getEdges (node).iterator ();
                while (it3.hasNext ()) {
                    E edge = it3.next ();
                    N endNode = dg.getNode (node, edge);
                    edges.put (edge, m.get (endNode));
                }
                Set<N> n = nodes.get (edges);
                if (n == null) {
                    n = new HashSet<N> ();
                    nodes.put (edges, n);
                }
                n.add (node);
            }
            Iterator<Map<E,Set<N>>> it3 = nodes.keySet ().iterator ();
            while (it3.hasNext ()) {
                Map<E,Set<N>> edges = it3.next ();
                Set<N> newState = nodes.get (edges);
                nnodes.put (newState, edges);
            }
        }
        if (nnodes.size () > s.size ())
            return reduce (dg, nnodes.keySet ());
        return nnodes;
    }
}
