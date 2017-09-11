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

package org.netbeans.lib.editor.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EventListener;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import org.netbeans.junit.NbTest;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;
import org.openide.util.io.NbMarshalledObject;


/**
 * Test of ListenerList correctness.
 *
 * @author mmetelka
 */
public class ListenerListTest extends NbTestCase {
    
    private static final boolean debug = false;
    
    public ListenerListTest(java.lang.String testName) {
        super(testName);
    }
    
    public void testAddAndRemoveListeners() {
        ListenerListCouple<MyL> couple = new ListenerListCouple<MyL>();
        MyL l1 = new MyL();
        MyL l11 = new MyL();
        MyL l2 = new MyL();
        MyL l21 = new MyL();
        MyL l3 = new MyL();
        couple.add(l1);
        couple.add(l2);
        couple.add(l3);
        couple.add(l21);
        couple.add(l11);
        couple.remove(l1);
        couple.remove(l2);
        couple.remove(l2);
        couple.remove(l21);
        couple.remove(l3);
        couple.add(l3);
    }
    
    public void testSerialization() throws Exception {
        ListenerList<MyL> ll = new ListenerList<MyL>();
        ll.add(new MyL());
        ll.add(new MyL());
        ll.add(new MyL());
        
        NbMarshalledObject mo = new NbMarshalledObject(ll);
        @SuppressWarnings("unchecked")
        ListenerList<MyL> sll = (ListenerList<MyL>)mo.get();
        List<MyL> lla = ll.getListeners();
        List<MyL> slla = sll.getListeners();
        assertEquals(lla.size(), slla.size());
        for (int i = lla.size() - 1; i >= 0; i--) {
            assertEquals(lla.get(i), slla.get(i));
        }
    }
    
    private static final class MyL implements EventListener, Serializable {
        
        static final long serialVersionUID = 12345L;
        
        static int cntr; // static counter
        
        private int id = cntr++; // should be restored during deserialization
        
        private int notified;
        
        public void notifyChange() {
            notified++;
        }
        
        public int getNotified() {
            return notified;
        }
        
        public int hashCode() {
            return id;
        }
        
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o instanceof MyL)
                return (id == ((MyL) o).id);
            return false;
        }
        
    }

    private static final class ListenerListCouple<T extends EventListener> {
        
        ListenerList<T> listenerList;
        
        List<T> imitation;
        
        public ListenerListCouple() {
            listenerList = new ListenerList<T>();
            imitation = new ArrayList<T>();
        }
        
        public void add(T listener) {
            listenerList.add(listener);
            imitation.add(listener);
            checkListsEqual();
        }
        
        public void remove(T listener) {
            listenerList.remove(listener);
            imitation.remove(listener);
            checkListsEqual();
        }
        
        public void checkListsEqual() {
            // Check the same listeners are stored in imitation
            assertEquals(imitation.size(), listenerList.getListenerCount());
            int i = 0;
            List<T> listeners = listenerList.getListeners();
            for (EventListener l : imitation) {
                assertSame(l, listeners.get(i++));
            }
        }
        
    }
    
}
