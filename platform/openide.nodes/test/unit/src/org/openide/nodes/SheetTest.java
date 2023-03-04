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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import org.netbeans.junit.NbTestCase;
import org.openide.nodes.Node.PropertySet;



/**
 * @author Some Czech
 */
public class SheetTest extends NbTestCase {

    public SheetTest(String name) {
        super(name);
    }
    
    public void testSheetSetEquals() {
        Sheet.Set s1 = new Sheet.Set();
        Sheet.Set s2 = new Sheet.Set();
        
        assertTrue("Equal as they don't have a name", s1.equals(s2));
    }

    public void testSheetSetEqualsNull() {
        Sheet.Set s1 = new Sheet.Set();
        Sheet.Set s2 = new Sheet.Set();
        s2.setName("Different name");
        
        assertFalse("Not equal. One has name, one does not", s1.equals(s2));
    }

    public void testSheetSetNullEquals() {
        Sheet.Set s1 = new Sheet.Set();
        Sheet.Set s2 = new Sheet.Set();
        s1.setName("Different name");
        
        assertFalse("Not equal. One has name, one does not", s1.equals(s2));
    }

    public void testSheetEvents() {

        AbstractNode node = new AbstractNode( Children.LEAF );
        
        Sheet sheet = node.getSheet();
        
        SheetListener sl = new SheetListener();
        TestNodeListener tnl = new TestNodeListener();
        
        node.addNodeListener( tnl );
        node.addPropertyChangeListener( sl );
                
        Sheet.Set ss = new Sheet.Set();
        ss.setName("Karel");
        sheet.put( ss );
        
        tnl.assertEvents( "NodePropertySets change", new PropertyChangeEvent[] {
            new PropertyChangeEvent( node, Node.PROP_PROPERTY_SETS, null, null )
        } );
        
        sl.assertEvents( "No events", new PropertyChangeEvent[] {} ); 
        
        PropertySupport.Name prop = new PropertySupport.Name (node);
        ss.put (prop);
        
        tnl.assertEvents( "NodePropertySets change again", new PropertyChangeEvent[] {
            new PropertyChangeEvent( node, Node.PROP_PROPERTY_SETS, null, null )
        } );
        
        sl.assertEvents( "No events fired", new PropertyChangeEvent[] {} ); 
        
        sheet.remove(ss.getName());
        
        tnl.assertEvents( "NodePropertySets change", new PropertyChangeEvent[] {
            new PropertyChangeEvent( node, Node.PROP_PROPERTY_SETS, null, null )
        } );
        
        sl.assertEvents( "No events", new PropertyChangeEvent[] {} ); 
        
        ss.remove (prop.getName());
        
        tnl.assertEvents( "No change in Node, as the set is removed", new PropertyChangeEvent[] {} );
        sl.assertEvents( "No events fired", new PropertyChangeEvent[] {} ); 
    }
    

    public void testIncorrectSynchronization() {
        final CountDownLatch cont = new CountDownLatch(1);
        final CountDownLatch finish = new CountDownLatch(1);
        final AtomicBoolean testStarted = new AtomicBoolean();
        final Thread[] dontBlock = { null };
        final Sheet test = new Sheet(new ArrayList<Sheet.Set>() {
            @Override
            public <T> T[] toArray(T[] a) {
                if (testStarted.get() && dontBlock[0] != Thread.currentThread()) {
                    cont.countDown();
                    try {
                        finish.await();
                    } catch (InterruptedException ex) {
                        throw new IllegalStateException(ex);
                    }
                }
                return super.toArray(a);
            }
        });
        
        test.put(new Sheet.Set());
        
        final boolean[] wasNull = new boolean[1];
        
        final Thread t = new Thread() {
            @Override public void run() {
                try {
                    cont.await();
                    for (PropertySet set : test.toArray()) {
                        wasNull[0] |= set == null;
                    }
                } catch (InterruptedException ex) {
                    throw new IllegalStateException(ex);
                }
                finish.countDown();
            }
        };
        dontBlock[0] = t;
        t.start();

        testStarted.set(true);
        
        test.toArray();
        
        assertFalse("No JavaNode in sight, and still returns a null property set?", wasNull[0]);
    }    
    
    private static class SheetListener implements PropertyChangeListener {
        
        List events = new ArrayList();
        
        public void propertyChange(java.beans.PropertyChangeEvent evt) {            
            events.add( evt );            
        }
        
        public void assertEvents( String message, PropertyChangeEvent[] pevents ) {
            
            if ( events.size() != pevents.length ) {
                fail( message );
            }
            
            int i = 0;
            for( Iterator it = events.iterator(); it.hasNext(); i++ ) {
                PropertyChangeEvent pche = (PropertyChangeEvent)it.next();
                assertEquals( message + " [" + i + "] ", pevents[i].getSource(), pche.getSource ());
                assertEquals( message + " [" + i + "] ", pevents[i].getPropertyName(), pche.getPropertyName());
                assertEquals( message + " [" + i + "] ", pevents[i].getOldValue (), pche.getOldValue());
                assertEquals( message + " [" + i + "] ", pevents[i].getNewValue(), pche.getNewValue ());
                assertEquals( message + " [" + i + "] ", pevents[i].getPropagationId(), pche.getPropagationId());
            }
            
            events.clear();
        }
        
    }
    
    private static class TestNodeListener extends SheetListener implements NodeListener {
        
        public void childrenAdded(org.openide.nodes.NodeMemberEvent ev) {
        }
        
        public void childrenRemoved(org.openide.nodes.NodeMemberEvent ev) {
        }
        
        public void childrenReordered(org.openide.nodes.NodeReorderEvent ev) {
        }
        
        public void nodeDestroyed(org.openide.nodes.NodeEvent ev) {
        }
        
    }

}
