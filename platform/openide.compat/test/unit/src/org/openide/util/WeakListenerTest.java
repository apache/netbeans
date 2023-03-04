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

package org.openide.util;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.WeakReference;
import java.util.Vector;
import javax.naming.event.NamingEvent;
import javax.naming.event.NamingExceptionEvent;
import javax.naming.event.ObjectChangeListener;
import org.netbeans.junit.NbTestCase;

public class WeakListenerTest extends NbTestCase {

    public WeakListenerTest(String testName) {
        super(testName);
    }

    public void testPrivateRemoveMethod() throws Exception {
        PropChBean bean = new PropChBean();
        Listener listener = new Listener();
        PropertyChangeListener weakL = new PrivatePropL(listener, bean);
        WeakReference ref = new WeakReference(listener);
        
        bean.addPCL(weakL);
        
        listener = null;
        assertGC("Listener wasn't GCed", ref);
        
        ref = new WeakReference(weakL);
        weakL = null;
        assertGC("WeakListener wasn't GCed", ref);
    }
    
    private static final class Listener
            implements PropertyChangeListener, ObjectChangeListener {
        public int cnt;
        
        public void propertyChange(PropertyChangeEvent ev) {
            cnt++;
        }
        
        public void namingExceptionThrown(NamingExceptionEvent evt) {
            cnt++;
        }
        
        public void objectChanged(NamingEvent evt) {
            cnt++;
        }
    } // end of Listener
    
    private static class PropChBean {
        private Vector listeners = new Vector();
        private void addPCL(PropertyChangeListener l) { listeners.add(l); }
        private void removePCL(PropertyChangeListener l) { listeners.remove(l); }
    } // End of PropChBean class
    
    private static class PrivatePropL extends WeakListener implements PropertyChangeListener {
        
        public PrivatePropL(PropertyChangeListener orig, Object source) {
            super(PropertyChangeListener.class, orig);
            setSource(source);
        }
        
        protected String removeMethodName() {
            return "removePCL"; // NOI18N
        }
        
        // ---- PropertyChangeListener implementation
        
        public void propertyChange(PropertyChangeEvent evt) {
            PropertyChangeListener l = (PropertyChangeListener) super.get(evt);
            if (l != null) l.propertyChange(evt);
        }
    } // End of PrivatePropL class
}
