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
package org.netbeans.modules.notifications.linux;

import com.sun.jna.Platform;
import javax.swing.JComponent;
import org.netbeans.junit.NbTestCase;
import org.openide.awt.Notification;
import org.openide.util.Lookup;

/**
 * Test LinuxNotificationDisplayer
 * @author Hector Espert
 */
public class LinuxNotificationDisplayerTest extends NbTestCase {

    private LinuxNotificationDisplayer notificationDisplayer;

    public LinuxNotificationDisplayerTest(String name) {
        super(name);
    }
    
    @Override
    public boolean canRun() {
        return super.canRun() && Platform.LINUX == Platform.getOSType();
    }

    @Override
    protected void setUp() throws Exception {
        notificationDisplayer = Lookup.getDefault().lookup(LinuxNotificationDisplayer.class);
        assertNotNull(notificationDisplayer);
        assertEquals(LinuxNotificationDisplayer.class, notificationDisplayer.getClass());
        
        if (notificationDisplayer.notLoaded()) {
            notificationDisplayer.load();
        }

        assertTrue(notificationDisplayer.isLoaded());
        assertFalse(notificationDisplayer.notLoaded());
        
        assertFalse(notificationDisplayer.isStarted());
        assertTrue(notificationDisplayer.notStarted());
        
        notificationDisplayer.start();
        assertTrue(notificationDisplayer.isStarted());
        assertFalse(notificationDisplayer.notStarted());
    }

    @Override
    protected void tearDown() throws Exception {
        assertTrue(notificationDisplayer.isStarted());
        assertFalse(notificationDisplayer.notStarted());
        
        notificationDisplayer.stop();
        assertFalse(notificationDisplayer.isStarted());
        assertTrue(notificationDisplayer.notStarted());
    }

    public void testNotify() {
        Notification notification = notificationDisplayer.notify("title", null, "details", null, null);
        assertNotNull(notification);
        assertEquals(TestNotification.class, notification.getClass());
    }

    public void testNotifyWithJComponents() {
        Notification notification = notificationDisplayer.notify("title", null, (JComponent) null, (JComponent) null, null);
        assertNotNull(notification);
        assertEquals(TestNotification.class, notification.getClass());
    }

}
