/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.api.actions;

import java.io.IOException;
import org.netbeans.junit.NbTestCase;
import org.netbeans.spi.actions.AbstractSavable;
import org.openide.util.Lookup.Result;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class SavableTest extends NbTestCase {

    public SavableTest(String n) {
        super(n);
    }

    @Override
    protected void tearDown() throws Exception {
        for (DoSave savable : Savable.REGISTRY.lookupAll(DoSave.class)) {
            savable.cleanup();
        }
    }

    public void testSavablesAreRegistered() throws IOException {
        String id = "identity";
        DoSave savable = new DoSave(id, null, null);
        assertNotNull("Savable created", savable);
        
        assertTrue(
            "Is is among the list of savables that need save", 
            Savable.REGISTRY.lookupAll(Savable.class).contains(savable)
        );
        
        savable.save();
        assertTrue("called", savable.save);
        
        assertTrue("No other pending saves", Savable.REGISTRY.lookupAll(Savable.class).isEmpty());
    }

    public void testTwoSavablesForEqual() throws IOException {
        Object id = new Object();
        
        DoSave s = new DoSave(id, null, null);
        assertEquals("The first", s, Savable.REGISTRY.lookup(Savable.class));
        DoSave s2 = new DoSave(id, null, null);
        
        assertEquals("Only one savable", 1, Savable.REGISTRY.lookupAll(Savable.class).size());
        assertEquals("The later", s2, Savable.REGISTRY.lookup(Savable.class));
        
        s.save();
        assertFalse("Calling save on replaced savables has no effect", s.save);
    }

    public void testEventDeliveredAsynchronously() throws Exception {
        class L implements LookupListener {
            int change;
            Object id = new Object();
            
            @Override
            public synchronized void resultChanged(LookupEvent ev) {
                change++;
                notifyAll();
            }
            
            public synchronized void createSavable() {
                assertEquals("No changes yet", 0, change);
                Savable s = new DoSave(id, null, null);
                assertEquals("The first", s, Savable.REGISTRY.lookup(Savable.class));
                assertEquals("Still no changes", 0, change);
            }
            
            public synchronized void waitForChange() throws InterruptedException {
                while (change == 0) {
                    wait();
                }
                assertEquals("One change delivered", 1, change);
            }
        }
        L listener = new L();
        Result<Savable> res = Savable.REGISTRY.lookupResult(Savable.class);
        
        try {
            res.addLookupListener(listener);
            listener.createSavable();
            listener.waitForChange();
        } finally {
            res.removeLookupListener(listener);
        }
    }
    
    static class DoSave extends AbstractSavable {
        boolean save;
        private final Object id;
        private final CharSequence displayName, ch2;

        public DoSave(Object id, CharSequence displayName, CharSequence ch2) {
            this.id = id;
            this.displayName = displayName;
            this.ch2 = ch2;
            register();
        }

        @Override
        public String findDisplayName() {
            return displayName.toString();
        }

        @Override
        protected void handleSave() throws IOException {
            save = true;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof DoSave) {
                return ((DoSave)obj).id.equals(id);
            }
            return false;
        }

        @Override
        public int hashCode() {
            return id.hashCode();
        }

        final void cleanup() {
            unregister();
        }
    }
    
}