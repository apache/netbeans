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

package org.openide.nodes;

import java.util.Collections;
import org.netbeans.junit.*;

/** Tests whether notification to NodeListener is fired under Mutex.writeAccess
 *
 * @author Jaroslav Tulach
 */
public class NodeListenerTest extends NbTestCase {
    public NodeListenerTest(String name) {
        super(name);
    }

    /** Creates a node with children, attaches a listener and tests whether
     * notifications are delivered under correct lock.
     */
    public void testCorrectMutexUsage () throws Exception {
        Children.Array ch = new Children.Array ();
        AbstractNode n = new AbstractNode (ch);
        
        class L extends Object implements NodeListener, Runnable {
            private boolean run;
            
            public void childrenAdded (NodeMemberEvent ev) {
                ChildFactoryTest.assertNodeAndEvent(ev, ev.getSnapshot());
                runNows ();
            }
            public void childrenRemoved (NodeMemberEvent ev) {
                ChildFactoryTest.assertNodeAndEvent(ev, ev.getSnapshot());
                runNows ();
            }
            public void childrenReordered(NodeReorderEvent ev) {
                ChildFactoryTest.assertNodeAndEvent(ev, ev.getSnapshot());
            }
            public void nodeDestroyed (NodeEvent ev) {
                ChildFactoryTest.assertNodeAndEvent(ev, Collections.<Node>emptyList());
            }
            
            public void propertyChange (java.beans.PropertyChangeEvent ev) {
            }
            
            public void run () {
                run = true;
            }
            
            private void runNows () {
                L read = new L ();
                Children.MUTEX.postReadRequest (read);
                if (read.run) {
                    fail ("It is possible to run read access request");
                }
                
                L write = new L ();
                Children.MUTEX.postWriteRequest (write);
                if (!write.run) {
                    fail ("It is not possible to run write access request");
                }
            }
        }
        
        
        L l = new L ();
        
        n.addNodeListener (l);
        Node t = new AbstractNode (Children.LEAF);
        ch.add (new Node[] { t });
        
        ch.remove (new Node[] { t });
    }
}
