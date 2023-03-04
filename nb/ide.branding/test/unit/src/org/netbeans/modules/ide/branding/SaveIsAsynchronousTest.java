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
package org.netbeans.modules.ide.branding;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import javax.swing.Action;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.RandomlyFails;
import org.netbeans.spi.actions.AbstractSavable;
import org.openide.awt.Actions;
import org.openide.util.ContextAwareAction;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
@RandomlyFails // NB-Core-Build #8077: Nothing in the registry expected:<null> but was:<displayname>
public class SaveIsAsynchronousTest extends NbTestCase {
    private ContextAwareAction saveAction;
    private Action saveAllAction;
    
    public SaveIsAsynchronousTest(String testName) {
        super(testName);
    }

    protected boolean runInEQ() {
        return true;
    }

    protected void setUp() throws Exception {
        saveAction = (ContextAwareAction) Actions.forID("System", "org.openide.actions.SaveAction");
        saveAllAction = Actions.forID("System", "org.openide.actions.SaveAllAction");
        assertNotNull("Save action found", saveAction);
        assertNotNull("Save All action found", saveAllAction);
        
        assertEquals("Nothing in the registry", null, MySavable.REGISTRY.lookup(MySavable.class));
    }
    
    public void testSaveAction() throws Exception {
        MySavable mySavable = new MySavable();
        Action a = saveAction.createContextAwareInstance(Lookups.singleton(mySavable));
        a.actionPerformed(new ActionEvent(this, 0, ""));
        mySavable.cdl.await();
        assertTrue("Handle save called", mySavable.called);
    }

    public void testSaveAllAction() throws Exception {
        MySavable mySavable = new MySavable();
        assertEquals("Is in the registry", mySavable, MySavable.REGISTRY.lookup(MySavable.class));
        saveAllAction.actionPerformed(new ActionEvent(this, 0, ""));
        mySavable.cdl.await();
        assertTrue("Handle save called", mySavable.called);
    }
    
    private static final class MySavable extends AbstractSavable {
        final CountDownLatch cdl = new CountDownLatch(1);
        volatile boolean called;

        public MySavable() {
            register();
        }
        
        
        
        protected String findDisplayName() {
            return "displayname";
        }

        protected void handleSave() throws IOException {
            called = true;
            assertFalse("No EDT", EventQueue.isDispatchThread());
            cdl.countDown();
        }

        public boolean equals(Object obj) {
            return obj == this;
        }

        public int hashCode() {
            return 555;
        }
    }
}
