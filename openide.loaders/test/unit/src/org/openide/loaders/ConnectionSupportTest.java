/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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

        public Enumeration loaders () {
            if (loaders == null) {
                return Enumerations.empty ();
            }
            return Collections.enumeration (loaders);
        }
    }
    
}
