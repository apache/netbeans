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
package org.openide.explorer.view;

import java.util.Enumeration;
import org.openide.nodes.*;

import java.util.EventObject;
import java.util.LinkedList;
import java.util.List;


/** Event describing change in a visualizer. Runnable to be added into
* the event queue.
*
* @author Jaroslav Tulach
*/
abstract class VisualizerEvent extends EventObject {
    /** indices */
    int[] array;
    NodeEvent originalEvent;
    List<Node> snapshot;

    public VisualizerEvent(VisualizerChildren ch, int[] array, NodeEvent originalEvent, List<Node> snapshot) {
        super(ch);
        this.array = array;
        this.originalEvent = originalEvent;
        this.snapshot = snapshot;
    }

    /** Getter for changed indexes */
    public final int[] getArray() {
        return array;
    }

    /** Getter for the children list.
    */
    public final VisualizerChildren getChildren() {
        return (VisualizerChildren) getSource();
    }

    /** Getter for the visualizer.
    */
    public final VisualizerNode getVisualizer() {
        return getChildren().parent;
    }
    
    public final List<Node> getSnapshot() {
        return snapshot;
    }

    /** Class for notification of adding of nodes that can be passed into
    * the event queue and in such case notifies all listeners in Swing Dispatch Thread
    */
    static final class Added extends VisualizerEvent implements Runnable {
        static final long serialVersionUID = 5906423476285962043L;

        /** Constructor for nodes adding notification.
        * @param ch children
        * @param idxs indicies of added nodes
        */
        public Added(VisualizerChildren ch, int[] idxs, NodeMemberEvent originalEvent) {
            super(ch, idxs, originalEvent, originalEvent.getSnapshot());
        }

        /** Process the event
        */
        public void run() {
            super.getChildren().added(this);
        }
    }

    /** Class for notification of removing of nodes that can be passed into
    * the event queue and in such case notifies all listeners in Swing Dispatch Thread
    */
    static final class Removed extends VisualizerEvent implements Runnable {
        static final long serialVersionUID = 5102881916407672392L;

        /** linked list of removed nodes, that is filled in getChildren ().removed () method
        */
        public LinkedList<VisualizerNode> removed = new LinkedList<VisualizerNode>();

        /** Constructor for nodes removal notification.
        * @param ch children
        * @param idxs indicies of added nodes
        */
        public Removed(VisualizerChildren ch, int[] idxs, NodeMemberEvent originalEvent) {
            super(ch, idxs, originalEvent, originalEvent.getSnapshot());
        }

        /** Process the event
        */
        public void run() {
            super.getChildren().removed(this);
        }
    }

    /** Class for notification of reordering of nodes that can be passed into
    * the event queue and in such case notifies all listeners in Swing Dispatch Thread
    */
    static final class Reordered extends VisualizerEvent implements Runnable {
        static final long serialVersionUID = -4572356079752325870L;

        /** Constructor for nodes reordering notification.
        * @param ch children
        * @param indx indicies of added nodes
        */
        public Reordered(VisualizerChildren ch, int[] idxs, NodeReorderEvent originalEvent) {
            super(ch, idxs, originalEvent, originalEvent.getSnapshot());
        }

        /** Process the event
        */
        public void run() {
            super.getChildren().reordered(this);
        }
    }
    
    static final class Destroyed extends VisualizerEvent implements Runnable {

        private final VisualizerNode vn;

        public Destroyed(VisualizerChildren ch, NodeEvent ev, VisualizerNode vn) {
            super(ch, null, ev, null);
            this.vn = vn;
        }

        @Override
        public void run() {
            // Reset the node, to free the original from memory.
            vn.node = Node.EMPTY;
            Enumeration<VisualizerNode> ch = getChildren().children(false);
            while (ch.hasMoreElements()) {
                final VisualizerNode v = ch.nextElement();
                if (v != null) {
                    v.nodeDestroyed(originalEvent);
                }
            }
        }
    }
}
