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

package org.openide.nodes;

import java.lang.ref.*;
import java.util.*;
import junit.framework.*;
import org.netbeans.junit.*;

public class ChildrenKeysGarbageCollectTest extends NbTestCase
implements NodeListener {
    private Creator creator;
    private int nodeDestroyed;

    public ChildrenKeysGarbageCollectTest(java.lang.String testName) {
        super(testName);
    }

    public static Test suite () {
        NbTestSuite general = new NbTestSuite ("General GC test suite");

        general.addTest (createSuite ("Unlimited keys", new Creator () {
            public Children.Keys createChildren () {
                return new ChildrenKeysTest.Keys (false);
            }
        }));
        general.addTest (createSuite ("1-1 keys", new Creator () {
            public Children.Keys createChildren () {
                return new ChildrenKeysTest.Keys (true);
            }
        }));
        general.addTest (createSuite ("0-1 keys", new Creator () {
            public Children.Keys createChildren () {
                return new ChildrenKeysTest.Keys (true);
            }
        }));
        
        return general;
    }
    
    private static NbTestSuite createSuite (String n, Creator c) {
        NbTestSuite s = new NbTestSuite (ChildrenKeysGarbageCollectTest.class);
        s.setName (n);
        Enumeration e = s.tests ();
        while (e.hasMoreElements ()) {
            Object o = e.nextElement ();
            if (o instanceof ChildrenKeysGarbageCollectTest) {
                ChildrenKeysGarbageCollectTest t = (ChildrenKeysGarbageCollectTest)o;
                t.creator = c;
            } else {
                fail ("o is supposed to be of correct instance: " + o);
            }
        }
        return s;
    }

    protected void setUp () throws Exception {
        /*
        System.setProperty("org.openide.util.Lookup", "org.openide.nodes.ChildrenGarbageCollectTest$Lkp");
        assertNotNull ("ErrManager has to be in lookup", org.openide.util.Lookup.getDefault ().lookup (ErrManager.class));
        ErrManager.messages.delete (0, ErrManager.messages.length ());
         */
    }

    
    public void testUsuallyAllNodesAreGCedAtOnce () throws Exception {
        Children.Keys k = creator.createChildren ();
        
        
        k.setKeys (new String[] { "1", "3", "4" });
        
        doFullOperationOnNodes (k);
    }
    
    private void doFullOperationOnNodes (Children.Keys k) {
        Node[] arr = k.getNodes ();
        
        assertEquals ("Three", 3, arr.length);
        
        WeakReference[] refs = new WeakReference[arr.length];
        for (int i = 0; i < arr.length; i++) {
            refs[i] = new WeakReference (arr[i]);
            arr[i].addNodeListener (this);
        }
        
        arr[0] = null;
        try {
            assertGC ("Try to gc only one node", refs[0]);
            fail ("This should not succeed, as others are still referenced");
        } catch (Throwable t) {
            // ok
        }
        arr[1] = arr[2] = null;
        
        assertGC ("Now we gc all", refs[0]);
        assertGC ("#2", refs[1]);
        assertGC ("#3", refs[2]);
        
        assertEquals ("Still 3 children", 3, k.getNodesCount ());
        assertEquals ("No nodes notified to be destoryed", 0, nodeDestroyed);

        arr = k.getNodes ();
        assertEquals ("Again three", 3, arr.length);
    }
    
    
    //
    // Listener methods
    //

    public void childrenAdded (NodeMemberEvent ev) {
    }

    public void childrenRemoved (NodeMemberEvent ev) {
    }

    public void childrenReordered (NodeReorderEvent ev) {
    }

    public void nodeDestroyed (NodeEvent ev) {
        nodeDestroyed++;
    }
    
    public void propertyChange (java.beans.PropertyChangeEvent ev) {
    }
    
    /** Factory for creating 
     */
    private interface Creator {
        public Children.Keys createChildren ();
    }
}
