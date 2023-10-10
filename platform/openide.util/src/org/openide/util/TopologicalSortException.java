/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.openide.util;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.*;


/** Exception that signals that a topological sort failed due to
* unsortable nature of the graph and that provides support for
* reporting and recovering from that state.
*
* @author Jaroslav Tulach
* @since 3.30
* @see BaseUtilities#topologicalSort
*/
public final class TopologicalSortException extends Exception {
    /** all vertexes */
    private Collection vertexes;

    /** map with edges */
    private Map<?, ? extends Collection<?>> edges;

    /** result if called twice */
    private Set[] result;

    /** counter to number the vertexes */
    private int counter;

    /** vertexes sorted by increasing value of y */
    private Stack<Vertex> dualGraph = new Stack<Vertex>();

    TopologicalSortException(Collection vertexes, Map<?,? extends Collection<?>> edges) {
        this.vertexes = vertexes;
        this.edges = edges;
    }

    /** Because the full sort was not possible, this methods
     * returns the best possible substitute for it that is available.
     *
     * @return list of partially sorted objects, the list can be freely modified
     */
    public final List partialSort() {
        Set[] all = topologicalSets();

        List<Object> res = new ArrayList<Object>(vertexes.size());

        for (int i = 0; i < all.length; i++) {
            for (Object e : all[i]) {
                res.add(e);
            }
        }

        return res;
    }

    /** The topological sort could not be finished as there
     * are some objects that are mutually refering to each other.
     * This methods finds such objects and partition them into
     * separate sets. All objects in one set (transitively) refer to
     * each other and thus prevent the sort from succeding. As
     * there can be more of such "unsortable sets" an array
     * of them is returned.
     *
     * @return array of sets that contain some of the original objects, result
     *   shall not be modified
     */
    public final Set[] unsortableSets() {
        Set[] all = topologicalSets();

        List<Set> unsort = new ArrayList<>();

        for (int i = 0; i < all.length; i++) {
            if ((all[i].size() > 1) || !(all[i] instanceof HashSet)) {
                unsort.add(all[i]);
            }
        }

        return unsort.toArray(new Set[0]);
    }

    @Override
    public String getMessage() {
        StringWriter w = new StringWriter();
        PrintWriter pw = new PrintWriter(w);
        printDebug(pw);
        pw.close();
        return w.toString();
    }
    
    @Override
    public String toString() {
        String s = getClass().getName();
        return s;
    }

    private void printDebug(java.io.PrintWriter w) {
        Set<Object> relevantVertices = new HashSet<Object>();
        Set<?>[] bad = unsortableSets();
        for (Set<?> s : bad) {
            relevantVertices.addAll(s);
        }
        Map<Object,Collection<?>> relevantEdges = new HashMap<Object,Collection<?>>();
        for (Map.Entry<?,? extends Collection<?>> entry : edges.entrySet()) {
            Set<Object> relevant = new HashSet<Object>(entry.getValue());
            relevant.add(entry.getKey());
            relevant.retainAll(relevantVertices);
            if (!relevant.isEmpty()) {
                relevantEdges.put(entry.getKey(), entry.getValue());
            }
        }
        w.print("TopologicalSortException - Collection with relevant edges "); // NOI18N
        w.print(relevantEdges);
        w.println(" cannot be sorted"); // NOI18N

        for (int i = 0; i < bad.length; i++) {
            w.print(" Conflict #"); // NOI18N
            w.print(i);
            w.print(": "); // NOI18N
            w.println(bad[i]);
        }
    }
    
    /** Adds description why the graph cannot be sorted.
     * @param w writer to write to
     */
    @Override
    public final void printStackTrace(java.io.PrintWriter w) {
        printDebug(w);
        super.printStackTrace(w);
    }

    /** Adds description why the graph cannot be sorted.
     * @param s stream to write to
     */
    @Override
    public final void printStackTrace(java.io.PrintStream s) {
        java.io.PrintWriter w = new java.io.PrintWriter(s);
        this.printStackTrace(w);
        w.flush();
    }

    /** As the full topological sort cannot be finished due to cycles
     * in the graph this methods performs a partition topological sort.
     * <P>
     * First of all it identifies unsortable parts of the graph and
     * partitions the graph into sets of original objects. Each set contains
     * objects that are mutually unsortable (there is a cycle between them).
     * Then the topological sort is performed again on those sets, this
     * sort succeeds because the graph of sets is DAG (all problematic edges
     * were only between objects now grouped inside the sets) and the
     * result forms the return value of this method.
     *
     * @return array of sorted sets that contain the original objects, each
     *   object from the original collection is exactly in one set, result
     *   shall not be modified
     */
    public final Set[] topologicalSets() {
        if (result != null) {
            return result;
        }

        HashMap<Object,Vertex> vertexInfo = new HashMap<Object,Vertex>();

        // computes value X and Y for each vertex
        counter = 0;

        Iterator<?> it = vertexes.iterator();

        while (it.hasNext()) {
            constructDualGraph(counter, it.next(), vertexInfo);
        }

        // now connect vertexes that cannot be sorted into own
        // sets
        // map from the original objects to 
        Map<Object, Set<Object>> objectsToSets = new HashMap<>();

        List<Set<Object>> sets = new ArrayList<>();

        while (!dualGraph.isEmpty()) {
            Vertex v = dualGraph.pop();

            if (!v.visited) {
                Set<Object> set = new HashSet<>();
                visitDualGraph(v, set);

                if ((set.size() == 1) && v.edgesFrom.contains(v)) {
                    // mark if there is a self reference and the
                    // set is only one element big, it means that there
                    // is a self cycle
                    //
                    // do not use HashSet but Collections.singleton
                    // to recognize such cycles
                    set = Collections.singleton(v.object);
                }

                sets.add(set);

                // fill the objectsToSets mapping
                it = set.iterator();

                while (it.hasNext()) {
                    objectsToSets.put(it.next(), set);
                }
            }
        }

        // now topologically sort the sets
        // 1. prepare the map
        Map<Set, Collection<Set>> edgesBetweenSets = new HashMap<>();
        it = edges.entrySet().iterator();

        while (it.hasNext()) {
            Map.Entry entry = (Map.Entry) it.next();
            Collection leadsTo = (Collection) entry.getValue();

            if ((leadsTo == null) || leadsTo.isEmpty()) {
                continue;
            }

            Set from = objectsToSets.get(entry.getKey());

            Collection<Set> setsTo = edgesBetweenSets.get(from);

            if (setsTo == null) {
                setsTo = new ArrayList<>();
                edgesBetweenSets.put(from, setsTo);
            }

            Iterator convert = leadsTo.iterator();

            while (convert.hasNext()) {
                Set to = objectsToSets.get(convert.next());

                if (from != to) {
                    // avoid self cycles
                    setsTo.add(to);
                }
            }
        }

        // 2. do the sort
        try {
            List<Set> listResult = BaseUtilities.topologicalSort(sets, edgesBetweenSets);
            result = listResult.toArray(new Set[0]);
        } catch (TopologicalSortException ex) {
            throw new IllegalStateException("Cannot happen"); // NOI18N
        }

        return result;
    }

    /** Traverses the tree
     * @param counter current value
     * @param vertex current vertex
     * @param vertexInfo the info
     */
    private Vertex constructDualGraph(int counter, Object vertex, HashMap<Object,Vertex> vertexInfo) {
        Vertex info = vertexInfo.get(vertex);

        if (info == null) {
            info = new Vertex(vertex, counter++);
            vertexInfo.put(vertex, info);
        } else {
            // already (being) processed
            return info;
        }

        // process children
        Collection c = (Collection) edges.get(vertex);

        if (c != null) {
            Iterator<?> it = c.iterator();

            while (it.hasNext()) {
                Vertex next = constructDualGraph(counter, it.next(), vertexInfo);
                next.edgesFrom.add(info);
            }
        }

        // leaving the vertex
        info.y = counter++;

        dualGraph.push(info);

        return info;
    }

    /** Visit dual graph. Decreasing value of Y gives the order.
     * Number
     *
     * @param vertex vertex to start from
     * @param visited list of all objects that we've been to
     */
    private void visitDualGraph(Vertex vertex, Collection<Object> visited) {
        if (vertex.visited) {
            return;
        }

        visited.add(vertex.object);
        vertex.visited = true;

        Iterator<Vertex> it = vertex.edges();

        while (it.hasNext()) {
            Vertex v = it.next();
            visitDualGraph(v, visited);
        }
    }

    /** Represents info about a vertex in the graph. Vertexes are
     * comparable by the value of Y, but only after the value is set,
     * so the sort has to be done latelly.
     */
    private static final class Vertex implements Comparable<Vertex> {
        /** the found object */
        public Object object;

        /** list of vertexes that point to this one */
        public List<Vertex> edgesFrom = new ArrayList<Vertex>();

        /** the counter state when we entered the vertex */
        public final int x;

        /** the counter when we exited the vertex */
        public int y;

        /** already sorted, true if the edges has been sorted */
        public boolean sorted;

        /** true if visited in dual graph */
        public boolean visited;

        public Vertex(Object obj, int x) {
            this.x = x;
            this.object = obj;
        }

        /** Iterator over edges
         * @return iterator of Vertex items
         */
        public Iterator<Vertex> edges() {
            if (!sorted) {
                Collections.sort(edgesFrom);
                sorted = true;
            }

            return edgesFrom.iterator();
        }

        /** Comparing based on value of Y.
         *
         * @param   o the Object to be compared.
         * @return  a negative integer, zero, or a positive integer as this object
         *                 is less than, equal to, or greater than the specified object.
         */
        public int compareTo(Vertex o) {
            return o.y - y;
        }
    }
}
