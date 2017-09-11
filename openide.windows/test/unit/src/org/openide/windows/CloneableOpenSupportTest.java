/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
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
