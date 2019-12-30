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

package org.openide.loaders;

import java.io.IOException;
import java.io.Serializable;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import junit.framework.TestCase;
import org.netbeans.modules.openide.util.NbMutexEventProvider;
import org.openide.cookies.ConnectionCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Enumerations;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

/** Some tests for the ConnectionSupport
 *
 * @author Jaroslav Tulach
 */
public class ConnectionSupportTest extends TestCase {
    static {
        System.setProperty ("org.openide.util.Lookup", "org.openide.loaders.ConnectionSupportTest$Lkp"); // NOI18N
    }
    
    public ConnectionSupportTest (String testName) {
        super (testName);
    }
    
    public void testFireEvent () throws Exception {
        FileObject root = FileUtil.getConfigRoot();
        FileObject fo = FileUtil.createData (root, "SomeData.txt");
        
        DataObject obj = DataObject.find (fo);
        if (!  (obj instanceof MultiDataObject)) {
            fail ("It should be multi data object: " + obj);
        }
        
        final T t = new T ();
        final MultiDataObject.Entry e = ((MultiDataObject)obj).getPrimaryEntry ();
        final ConnectionSupport sup = new ConnectionSupport (
            e, new T[] { t }
        );
        
        sup.register (t, MN.myNode);
        
        class BreakIt implements ConnectionSupport.Listener, Runnable {
            public boolean called;
            public boolean finished;
            
            public void notify (ConnectionCookie.Event ev) {
                called = true;
                RequestProcessor.getDefault ().post (this).waitFinished ();
                finished = true;
            }
            
            public void run () {
                try {
                    sup.unregister (t, MN.myNode);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
        
        BreakIt b = new BreakIt ();
        MN.myNode.b = b;
        
        sup.fireEvent (new ConnectionSupport.Event (e.getDataObject ().getNodeDelegate (), t));
        
        assertTrue ("Notify called", b.called);
        assertTrue ("Plus when calling notify none holds a lock that would prevent" +
                "other thread from reentering the ConnectionSupport", b.finished);
    }
    
    private static final class MN extends AbstractNode {
        public static MN myNode = new MN ();
        
        public ConnectionCookie.Listener b;
        private MN () {
            super (Children.LEAF);
        }
        
        public Node.Cookie getCookie (Class c) {
            if (c == ConnectionCookie.Listener.class) {
                return b;
            }
            return null;
        }
        
        public Node.Handle getHandle () {
            return new H ();
        }
        
    }
    
    private static final class H implements Node.Handle, Serializable {
        public Node getNode () {
            return MN.myNode;
        }
    }
    
    private static final class T implements ConnectionSupport.Type {
        public Class getEventClass () {
            return javax.swing.event.ChangeListener.class;
        }

        public boolean isPersistent () {
            return true;
        }

        public boolean overlaps(ConnectionCookie.Type type) {
            return getClass () == type.getClass ();
        }
        
    }
    
    public static final class Lkp extends AbstractLookup {
        public Lkp () {
            this (new InstanceContent ());
        }
        
        private Lkp (InstanceContent ic) {
            super (ic);
            ic.add (new Pool ());
            ic.add (new NbMutexEventProvider());
        }
    }
    
    private static final class Pool extends DataLoaderPool {
        static List loaders;
        
        public Pool () {
        }

        public Enumeration<? extends DataLoader> loaders() {
            if (loaders == null) {
                return Enumerations.empty ();
            }
            return Collections.enumeration (loaders);
        }
    }
    
}
