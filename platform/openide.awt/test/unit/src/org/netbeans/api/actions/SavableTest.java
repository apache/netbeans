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