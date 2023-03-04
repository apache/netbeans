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
 * Directed Graph implementation.
 *
 * @author Jan Jancura
 */
public class DG<N,E,K,V> {

    
    static <N,E,K,V> DG<N,E,K,V> createDG (N node) {
        return new DG<N,E,K,V> (node);
    }
    
    static <N,E,K,V> DG<N,E,K,V> createDG () {
        return new DG<N,E,K,V> ();
    }

    
    private Map<N,Node<N,E,K,V>> idToNode = new HashMap<N,Node<N,E,K,V>> ();
    private Map<Node<N,E,K,V>,N> nodeToId = new HashMap<Node<N,E,K,V>,N> ();
    private N start;
    private Set<N> ends = new HashSet<N> ();
    
    private DG () {
    }
    
    private DG (N node) {
        start = node;
        Node<N,E,K,V> n = new Node<N,E,K,V> ();
        idToNode.put (node, n);
        nodeToId.put (n, node);
        ends.add (node);
    }
    
    N getStartNode () {
        return start;
    }
    
    void setStart (N node) {
        if (idToNode.get (node) == null) new NullPointerException ();
        start = node;
    }
    
    Set<N> getEnds () {
        return Collections.<N>unmodifiableSet (ends);
    }
    
    void setEnds (Set<N> ends) {
        this.ends = new HashSet<N> (ends);
    }
    
    void addEnd (N end) {
        assert (end != null);
        ends.add (end);
    }
    
    void removeEnd (N end) {
        ends.remove (end);
    }
    
    void addNode (N node) {
        assert (node != null);
        if (idToNode.containsKey (node)) {
            //throw new IllegalArgumentException ();
            return;
        }
        Node<N,E,K,V> n = new Node<N,E,K,V> ();
        idToNode.put (node, n);
        nodeToId.put (n, node);
    }
    
    void removeNode (N node) {
        Node<N,E,K,V> n = idToNode.remove (node);
        nodeToId.remove (n);
    }
    
    boolean containsNode (N node) {
        return idToNode.containsKey (node);
    }
    
    Set<N> getNodes () {
        return Collections.<N>unmodifiableSet (idToNode.keySet ());
    }
    
    N getNode (N node, E edge) {
        Node<N,E,K,V> s = idToNode.get (node);
        Node<N,E,K,V> e = s.getNode (edge);
        return nodeToId.get (e);
    }
    
    void addEdge (
        N startNode,
        N endNode,
        E edge
    ) {
        assert (startNode != null);
        assert (endNode != null);
        assert (edge != null);
        Node<N,E,K,V> s = idToNode.get (startNode);
        Node<N,E,K,V> e = idToNode.get (endNode);
        assert (s != null);
        assert (e != null);
        s.addEdge (edge, e);
    }
    
    Set<E> getEdges (N node) {
        Node<N,E,K,V> n = idToNode.get (node);
        return n.edges ();
    }
    
    E getEdge (N node, E edge) {
        Node<N,E,K,V> n = idToNode.get (node);
        return n.getEdge (edge);
    }

    V getProperty (N node, K key) {
        Node<N,E,K,V> n = idToNode.get (node);
        return n.getProperty (key);
    }
    
    Map<K,V> getProperties (N node) {
        Node<N,E,K,V> n = idToNode.get (node);
        if (n.properties == null) return Collections.<K,V>emptyMap ();
        return Collections.<K,V>unmodifiableMap (n.properties);
    }
    
    void putAllProperties (N node, Map<K,V> properties) {
        if (properties.size () == 0) return;
        Node<N,E,K,V> n = idToNode.get (node);
        if (n.properties == null) n.properties = new HashMap<K,V> ();
        n.properties.putAll (properties);
    }

    void setProperty (N node, K key, V value) {
        Node<N,E,K,V> n = idToNode.get (node);
        n.setProperty (key, value);
    }

    V getProperty (N node, E edge, K key) {
        Node<N,E,K,V> n = idToNode.get (node);
        return n.getEdgeProperty (edge, key);
    }

    Map<K,V> getProperties (N node, E edge) {
        Node<N,E,K,V> n = idToNode.get (node);
        if (n.idToProperties == null ||
            n.idToProperties.get (edge) == null
        ) return Collections.<K,V>emptyMap ();
        return Collections.<K,V>unmodifiableMap (n.idToProperties.get (edge));
    }

    void putAllProperties (N node, E edge, Map<K,V> properties) {
        if (properties.size () == 0) return;
        Node<N,E,K,V> n = idToNode.get (node);
        if (n.idToProperties == null) n.idToProperties = new HashMap<E,Map<K,V>> ();
        if (n.idToProperties.get (edge) == null)
            n.idToProperties.put (edge, new HashMap<K,V> ());
        n.idToProperties.get (edge).putAll (properties);
    }
    
    void setProperty (N node, E edge, K key, V value) {
        Node<N,E,K,V> n = idToNode.get (node);
        n.setEdgeProperty (edge, key, value);
    }
    
    void changeKey (N oldNode, N newNode) {
        Node<N,E,K,V> n = idToNode.get (oldNode);
        idToNode.remove (oldNode);
        idToNode.put (newNode, n);
        nodeToId.put (n, newNode);
    }
    
    public String toString () {
        StringBuffer sb = new StringBuffer ();

        sb.append (" start: ").append (getStartNode ()).append (" end: ");
        Iterator<N> it = getEnds ().iterator ();
        while (it.hasNext ()) {
            N end = it.next ();
            sb.append (end);
            if (it.hasNext ()) sb.append (',');
        }
        sb.append ('\n');
        
        it = getNodes ().iterator ();
        while (it.hasNext ()) {
            N node = it.next ();
            sb.append (node).append ('(');
            Iterator<E> it2 = getEdges (node).iterator ();
            while (it2.hasNext ()) {
                E edge = it2.next ();
                N end = getNode (node, edge);
                sb.append (convert (edge)).append (end);
                if (it2.hasNext ()) sb.append (',');
            }
            sb.append (')');
            sb.append ('\n');
        }
        
        it = getNodes ().iterator ();
        while (it.hasNext ()) {
            N node = it.next ();
            Node<N,E,K,V> n = idToNode.get (node);
            sb.append ("  ").append (node).append (": ");
            if (n.properties != null)
                sb.append (n.properties);
            sb.append ('\n');
            if (n.idToProperties != null) {
                Iterator<E> it2 = n.idToProperties.keySet ().iterator ();
                while (it2.hasNext ()) {
                    E edge = it2.next ();
                    Map<K,V> m = n.idToProperties.get (edge);
                    sb.append ("    ").append (convert (edge)).append (": ").append (m).append ('\n');
                }
            }
        }
        return sb.toString ();
    }
    
    private static Character NN = new Character ('\n');
    private static Character NR = new Character ('\n');
    private static Character NT = new Character ('\n');
    private static Character NS = new Character ('\n');
    
    private static final Character STAR = new Character ((char)0);
    
    private String convert (E edge) {
        if (STAR.equals (edge)) return ".";
        if (NN.equals (edge)) return "\\n";
        if (NR.equals (edge)) return "\\r";
        if (NT.equals (edge)) return "\\t";
        if (NS.equals (edge)) return "' '";
        return edge.toString ();
    }
    
    private static class Node<N,E,K,V> {

        private Map<K,V>                properties;
        private Map<E,Map<K,V>>         idToProperties;
        private Map<E,Node<N,E,K,V>>    edgeToNode;
        private Map<E,E>                edges;


        V getProperty (K key) {
            if (properties == null) return null;
            return properties.get (key);
        }
        
        void setProperty (K key, V value) {
            if (properties == null) properties = new HashMap<K,V> ();
            properties.put (key, value);
        }
        
        Node<N,E,K,V> getNode (E edge) {
            if (edgeToNode == null) return null;
            return edgeToNode.get (edge);
        }

        void addEdge (E edge, Node<N,E,K,V> node) {
            if (edgeToNode == null) edgeToNode = new HashMap<E,Node<N,E,K,V>> ();
            if (edges == null) edges = new HashMap<E,E> ();
            edgeToNode.put (edge, node);
            edges.put (edge, edge);
        }

        E getEdge (E edge) {
            if (edges == null) return null;
            return edges.get (edge);
        }

        Set<E> edges () {
            if (edgeToNode == null) return Collections.<E>emptySet ();
            return edgeToNode.keySet ();
        }
        
        V getEdgeProperty (E edge, K key) {
            if (idToProperties == null) return null;
            if (idToProperties.get (edge) == null) return null;
            return idToProperties.get (edge).get (key);
        }

        void setEdgeProperty (E edge, K key, V value) {
            if (idToProperties == null) idToProperties = new HashMap<E,Map<K,V>> ();
            Map<K,V> m = idToProperties.get (edge);
            if (m == null) {
                m = new HashMap<K,V> ();
                idToProperties.put (edge, m);
            }
            m.put (key, value);
        }
    }
}
