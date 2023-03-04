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

import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.util.HashMap;
import java.util.Map;
import javax.swing.Action;
import javax.swing.Icon;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.netbeans.junit.NbTestCase;

/**
 *
 * @author Jaroslav Tulach
 */
public class OpenComponentActionTest extends NbTestCase {
    
    public static Test suite() {
        return GraphicsEnvironment.isHeadless() ? new TestSuite() : new TestSuite(OpenComponentActionTest.class);
    }

    public OpenComponentActionTest(String testName) {
        super(testName);
    }

    @Override
    protected boolean runInEQ() {
        return true;
    }
    
    

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        
        TC.instance = null;
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testMethodCallDirectly() {
        ActionEvent e = new ActionEvent(this, 0, "");
        TC tc = new TC();
        final String img = "org/openide/windows/icon.png";
        Action instance = TopComponent.openAction(tc, "Ahoj", img, false);
        instance.actionPerformed(e);
        
        tc.close();
        
        assertEquals("Opened once", 1, tc.cntOpen);
        assertEquals("Activated once", 1, tc.cntRequest);
        
        Icon icon = (Icon) instance.getValue(Action.SMALL_ICON);
        assertEquals("Width", 133, icon.getIconWidth());
        assertEquals("Height", 133, icon.getIconHeight());
        assertEquals("Name", "Ahoj", instance.getValue(Action.NAME));
    }

    public void testMapInstantiation() {
        final String img = "org/openide/windows/icon.png";
        
        Map<String,Object> m = new HashMap<String,Object>() {
            @Override
            public Object get(Object key) {
                if ("component".equals(key)) {
                    return new TC();
                }
                if ("displayName".equals(key)) {
                    return "Ahoj";
                }
                if ("iconBase".equals(key)) {
                    return img;
                }
                return null;
            }
        };
        
        ActionEvent e = new ActionEvent(this, 0, "");
        Action instance = TopComponent.openAction(m);
        
        assertNull("No instance yet", TC.instance);
        instance.actionPerformed(e);
        
        assertNotNull("Instance created", TC.instance);
        TC tc = TC.instance;
        tc.close();
        
        assertEquals("Opened once", 1, tc.cntOpen);
        assertEquals("Activated once", 1, tc.cntRequest);
        
        Icon icon = (Icon) instance.getValue(Action.SMALL_ICON);
        assertEquals("Width", 133, icon.getIconWidth());
        assertEquals("Height", 133, icon.getIconHeight());
        assertEquals("Name", "Ahoj", instance.getValue(Action.NAME));
    }

    
    public static final class TC extends TopComponent {
        static TC instance;

        int cntOpen;
        int cntRequest;
        
        public TC() {
            assertNull("No previous one", instance);
            instance = this;
        }

        @Override
        public void open() {
            super.open();
            cntOpen++;
        }

        @Override
        public void requestActive() {
            super.requestActive();
            cntRequest++;
        }
    }
}
