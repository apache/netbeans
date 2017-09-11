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
