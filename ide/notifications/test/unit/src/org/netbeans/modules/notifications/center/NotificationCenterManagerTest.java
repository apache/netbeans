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

import java.util.logging.Level;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.notifications.NotificationDisplayerImpl;
import org.netbeans.modules.notifications.NotificationImpl;
import org.netbeans.modules.notifications.filter.NotificationFilter;
import org.openide.awt.Notification;
import org.openide.awt.NotificationDisplayer;
import org.openide.awt.NotificationDisplayer.Category;
import org.openide.util.ImageUtilities;

/**
 *
 * @author jpeska
 */
public class NotificationCenterManagerTest extends NbTestCase {

    private NotificationCenterManager manager;

    public NotificationCenterManagerTest(String arg0) {
        super(arg0);
    }

    @Override
    protected Level logLevel() {
        return Level.ALL;
    }

    @Override
    protected void setUp() throws Exception {
        System.setProperty("netbeans.user", getWorkDir().getAbsolutePath());
        manager = NotificationCenterManager.getInstance();
        manager.deleteAll();
        manager.setActiveFilter(NotificationFilter.EMPTY);
        manager.setMessageFilter("");
    }

    public void testCreateNotification() {
        int size = manager.getTotalCount();
        assertEquals(0, size);

        NotificationImpl n = (NotificationImpl) createNotification(Category.INFO, "Title1");
        size = manager.getTotalCount();
        assertEquals(1, size);

        NotificationImpl last = manager.getLastUnreadNotification();
        assertNotNull(last);
        assertEquals(n, last);

        assertEquals(n.getTitle(), last.getTitle());
        assertEquals(n.getCategory(), last.getCategory());
    }

    public void testClearNotification() {
        int size = manager.getTotalCount();
        assertEquals(0, size);

        Notification n1 = createNotification();
        Notification n2 = createNotification();
        size = manager.getTotalCount();
        assertEquals(2, size);

        n2.clear();
        size = manager.getTotalCount();
        assertEquals(1, size);
    }

    public void testMarkAsRead() {
        int unread = manager.getUnreadCount();
        assertEquals(0, unread);

        Notification n1 = createNotification();
        NotificationImpl n2 = (NotificationImpl) createNotification();
        unread = manager.getUnreadCount();
        assertEquals(2, unread);

        n2.markAsRead(true);
        unread = manager.getUnreadCount();
        assertEquals(1, unread);
    }

    public void testTitleFilter() {
        int filtered = manager.getFilteredCount();
        assertEquals(0, filtered);

        Notification n1 = createNotification(Category.INFO, "Title1");
        NotificationImpl n2 = (NotificationImpl) createNotification(Category.INFO, "Title2");
        filtered = manager.getFilteredCount();
        assertEquals(2, filtered);

        manager.setMessageFilter("Title2");
        filtered = manager.getFilteredCount();
        assertEquals(1, filtered);
        NotificationImpl get = manager.getFilteredNotifications().get(0);
        assertEquals(n2, get);

        manager.setMessageFilter("Title12");
        filtered = manager.getFilteredCount();
        assertEquals(0, filtered);
    }

    public void testLastNotification() {
        assertEquals(null, manager.getLastUnreadNotification());
        
        NotificationImpl n1 = (NotificationImpl) createNotification(Category.INFO, "Title1");
        assertEquals(n1, manager.getLastUnreadNotification());

        NotificationImpl n2 = (NotificationImpl) createNotification(Category.INFO, "Title2");
        assertEquals(n2, manager.getLastUnreadNotification());

        n2.markAsRead(true);
        assertEquals(n1, manager.getLastUnreadNotification());
    }

    private Notification createNotification() {
        String title = "Title";
        return createNotification(Category.INFO, title);
    }

    private Notification createNotification(Category category, String title) {
        String dummyText = "<html>The Netbeans IDE has detected that your system is using most of your available system resources. We recommend shutting down other applications and windows.</html>";
        return NotificationDisplayerImpl.getInstance().notify(title,
                ImageUtilities.loadIcon("org/netbeans/modules/notifications/resources/filter.png"),
                new JLabel(dummyText), new JLabel(dummyText),
                NotificationDisplayer.Priority.NORMAL,
                category);
    }
}
