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

package org.openide.util.actions;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import org.junit.Assert;
import org.openide.nodes.Node;
import org.openide.util.ContextGlobalProvider;
import org.openide.util.Lookup;
import org.openide.util.Utilities;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.util.test.MockLookup;

/** Utilities for actions tests.
 * @author Jesse Glick
 */
public class NodeActionsInfraHid {
    
    private static Node[] currentNodes = null;
    private static final NodeLookup nodeLookup = new NodeLookup();
    private static Lookup.Result<Node> nodeResult;
    
    public static void install() {
        MockLookup.setInstances(new ContextGlobalProvider() {
            public Lookup createGlobalContext() {
                return nodeLookup;
            }
        });
        nodeResult = Utilities.actionsGlobalContext().lookupResult(Node.class);
        Assert.assertEquals(Collections.emptySet(), new HashSet<Node>(nodeResult.allInstances()));
    }

    public static void setCurrentNodes(Node[] nue) {
        currentNodes = nue;
        nodeLookup.refresh();
        Assert.assertEquals(nue != null ? new HashSet(Arrays.asList(nue)) : Collections.EMPTY_SET,
                new HashSet(nodeResult.allInstances()));
    }

    private static final class NodeLookup extends AbstractLookup implements InstanceContent.Convertor {
        private final InstanceContent content;
        public NodeLookup() {
            this(new InstanceContent());
        }
        private NodeLookup(InstanceContent content) {
            super(content);
            this.content = content;
            refresh();
        }
        public void refresh() {
            //System.err.println("NL.refresh; currentNodes = " + currentNodes);
            if (currentNodes != null) {
                content.set(Arrays.asList(currentNodes), null);
            } else {
                content.set(Collections.singleton(new Object()), this);
            }
        }
        public Object convert(Object obj) {
            return null;
        }
        public Class type(Object obj) {
            return Node.class;
        }
        public String id(Object obj) {
            return "none"; // magic, see NodeAction.NodesL.resultChanged
        }
        public String displayName(Object obj) {
            return null;
        }
    }

    /*
    private static final class NodeLookup extends ProxyLookup implements InstanceContent.Convertor {
        public NodeLookup() {
            refresh();
        }
        public void refresh() {
            //System.err.println("NL.refresh; currentNodes = " + currentNodes);
            setLookups(new Lookup[] {
                currentNodes != null ?
                    Lookups.fixed(currentNodes) :
                    Lookups.fixed(new Object[] {null}, this),
            });
        }
        public Object convert(Object obj) {
            return null;
        }
        public Class type(Object obj) {
            return Object.class;
        }
        public String id(Object obj) {
            return "none";
        }
        public String displayName(Object obj) {
            return null;
        }
    }
     */

    /** Prop listener that will tell you if it gets a change.
     */
    public static final class WaitPCL implements PropertyChangeListener {
        /** whether a change has been received, and if so count */
        public int gotit = 0;
        /** optional property name to filter by (if null, accept any) */
        private final String prop;
        public WaitPCL(String p) {
            prop = p;
        }
        public synchronized void propertyChange(PropertyChangeEvent evt) {
            if (prop == null || prop.equals(evt.getPropertyName())) {
                gotit++;
                notifyAll();
            }
        }
        public boolean changed() {
            return changed(1500);
        }
        public synchronized boolean changed(int timeout) {
            if (gotit > 0) {
                return true;
            }
            try {
                wait(timeout);
            } catch (InterruptedException ie) {
                ie.printStackTrace();
            }
            return gotit > 0;
        }
    }
    
}
