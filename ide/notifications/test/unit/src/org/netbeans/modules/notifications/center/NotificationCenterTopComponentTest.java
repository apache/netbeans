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
package org.netbeans.modules.notifications.center;

import java.util.Properties;
import org.junit.Test;
import org.netbeans.junit.NbTestCase;

/**
 *
 * @author Hector Espert
 */
public class NotificationCenterTopComponentTest extends NbTestCase {
    
    private NotificationCenterTopComponent centerTopComponent;

    public NotificationCenterTopComponentTest(String name) {
        super(name);
    }

    @Override
    protected boolean runInEQ() {
        return true;
    }

    @Override
    protected void setUp() throws Exception {
        centerTopComponent = new NotificationCenterTopComponent();
        centerTopComponent.componentOpened();
    }

    @Override
    protected void tearDown() throws Exception {
        centerTopComponent.componentClosed();
    }

    @Test
    public void testAddNotify() {
        centerTopComponent.addNotify();
    }

    @Test
    public void testGetHelpCtx() {
        assertNotNull(centerTopComponent.getHelpCtx());
    }

    @Test
    public void testWriteProperties() {
        Properties properties = new Properties();
        centerTopComponent.writeProperties(properties);
        assertEquals("1.0", properties.getProperty("version"));
        
    }

    @Test
    public void testReadProperties() {
        Properties properties = new Properties();
        properties.setProperty("version", "1.0");
        centerTopComponent.readProperties(properties);
    }
    
}
