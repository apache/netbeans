/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.uihandler;

import java.awt.Dialog;
import java.awt.EventQueue;
import java.util.logging.Logger;
import javax.swing.JDialog;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.Mutex;

/**
 *
 * @author Jaroslav Tulach
 */
public class ShutdownFromAWTTest extends NbTestCase {
    Installer inst;
    
    public ShutdownFromAWTTest(String testName) {
        super(testName);
    }
    
    @Override
    protected boolean runInEQ() {
        return true;
    }
    

    protected void setUp() throws Exception {
        inst = Installer.findObject(Installer.class, true);
        inst.restored();

        MockServices.setServices(DD.class);
        Logger.getLogger(Installer.UI_LOGGER_NAME).warning("ONE_LOG");
    }

    protected void tearDown() throws Exception {
    }

    public void testShutdown() throws Exception {
        assertTrue("In EQ", EventQueue.isDispatchThread());
    
        assertTrue("Ok to close", inst.closing());
        inst.doClose();
    }
    public static final class DD extends DialogDisplayer implements Mutex.Action<Integer> {
        private int cnt;
        
        private void assertAWT() {
            int cnt = this.cnt;
            int ret = Mutex.EVENT.readAccess(this);
            assertEquals("Incremented", cnt + 1, this.cnt);
            assertEquals("Incremented2", cnt + 1, ret);
        }
        
        public Object notify(NotifyDescriptor descriptor) {
            assertAWT();
            
            // last options allows to close usually
            return descriptor.getOptions()[descriptor.getOptions().length - 1];
        }
        
        public Dialog createDialog(DialogDescriptor descriptor) {
            assertAWT();
            
            return new JDialog() {
                @Override
                public void setVisible(boolean v) {
                }
            };
        }
        
        public Integer run() {
            cnt++;
            assertTrue(EventQueue.isDispatchThread());
            return cnt;
        }
    }
    
}
