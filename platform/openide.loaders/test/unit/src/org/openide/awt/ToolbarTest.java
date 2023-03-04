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
package org.openide.awt;

import java.awt.EventQueue;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.ToolBarUI;
import org.netbeans.junit.NbTestCase;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class ToolbarTest extends NbTestCase  {
    
    public ToolbarTest(String n) {
        super(n);
    }

    public void testInitOutsideOfEDT() throws Exception {
        class MyToolbar extends Toolbar implements Runnable {

            @Override
            protected void setUI(ComponentUI newUI) {
                assertTrue("Can only be called in EDT", EventQueue.isDispatchThread());
                super.setUI(newUI);
            }

            @Override
            public void setUI(ToolBarUI ui) {
                assertTrue("Can only be called in EDT", EventQueue.isDispatchThread());
                super.setUI(ui);
            }

            private void assertUI() throws Exception {
                EventQueue.invokeAndWait(this);
            }

            @Override
            public void run() {
                assertNotNull("UI delegate is specified", getUI());
            }
        }
        
        assertFalse("We are not in EDT", EventQueue.isDispatchThread());
        MyToolbar mt = new MyToolbar();
        assertNotNull("Instance created", mt);
        
        mt.assertUI();
    }
}
