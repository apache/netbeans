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
package org.openide.windows;

import java.beans.PropertyChangeListener;
import java.beans.VetoableChangeListener;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.Serializable;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;
import org.netbeans.junit.NbTestCase;
import org.openide.util.io.NbMarshalledObject;
import org.openide.windows.CloneableOpenSupport.Env;
import org.openide.windows.CloneableTopComponent.Ref;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class CloneableOpenSupportTest extends NbTestCase {
    
    
    public CloneableOpenSupportTest(String n) {
        super(n);
    }

    @Override
    protected boolean runInEQ() {
        return true;
    }
    
    
    
    public void testDeserAndGC() throws Exception {
        MyCOS cos = MyCOS.find("test1");
        CloneableTopComponent ctc = cos.openCloneableTopComponent();
        assertEquals("Associated", cos.allEditors, ctc.getReference());
        
        NbMarshalledObject mar = new NbMarshalledObject(ctc);
        
        Reference<MyCOS> first = new WeakReference<MyCOS>(cos);
        cos = null;
        assertTrue("Closed", ctc.close());
        ctc = null;
        assertGC("Can GC away", first);
        
        
        CloneableTopComponent newCtc = (CloneableTopComponent)mar.get();
        Ref newRef = newCtc.getReference();
        MyCOS newOne = MyCOS.find("test1");
        
        assertEquals("Just two created", 2, MyCOS.cnt);
        assertEquals("Associated 2", newOne.allEditors, newRef);
    }
    
    private static final class MyCOS extends CloneableOpenSupport {
        private static final Map<String,Reference<MyCOS>> existing = new HashMap<String, Reference<MyCOS>>();
        public static synchronized MyCOS find(String id) {
            final Reference<MyCOS> ref = existing.get(id);
            MyCOS my = ref == null ? null : ref.get();
            if (my == null) {
                existing.put(id, new WeakReference<MyCOS>(my = new MyCOS(new MyEnv(id))));
            }
            return my;
        }
        static int cnt;
        
        private final int myCnt;
        private MyCOS(MyEnv env) {
            super(env);
            myCnt = cnt++;
        }
        
        @Override
        protected CloneableTopComponent createCloneableTopComponent() {
            return new MyTC();
        }

        @Override
        protected String messageOpening() {
            return "Open";
        }

        @Override
        protected String messageOpened() {
            return "OpenDone";
        }

        @Override
        public String toString() {
            return "MyCOS[" + myCnt + "]";
        }
        
        
    }
    
    private static final class MyTC extends CloneableTopComponent {
        @Override
        public void readExternal(ObjectInput oi) throws IOException, ClassNotFoundException {
            super.readExternal(oi);
            Reference<Object> r = new WeakReference<Object>(MyCOS.find("test1"));
            try {
                assertGC("Can disappear", r);
            } catch (Throwable ex) {
                // OK, just try, don't have to really disappear
                // (if the original problem is fixed)
            }
        }
    }
    
    private static final class MyEnv implements Env, Serializable {
        static final long serialVersionUID = 1L;
        private final String id;
        public MyEnv(String id) {
            this.id = id;
        }

        @Override
        public void addPropertyChangeListener(PropertyChangeListener l) {
        }

        @Override
        public void removePropertyChangeListener(PropertyChangeListener l) {
        }

        @Override
        public void addVetoableChangeListener(VetoableChangeListener l) {
        }

        @Override
        public void removeVetoableChangeListener(VetoableChangeListener l) {
        }

        @Override
        public boolean isValid() {
            return true;
        }

        @Override
        public boolean isModified() {
            return false;
        }

        @Override
        public void markModified() throws IOException {
            throw new IOException();
        }

        @Override
        public void unmarkModified() {
        }

        @Override
        public CloneableOpenSupport findCloneableOpenSupport() {
            return MyCOS.find(id);
        }
    }
}
