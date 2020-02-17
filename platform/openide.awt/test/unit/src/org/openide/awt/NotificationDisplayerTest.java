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
package org.openide.awt;

import java.util.HashMap;
import java.util.Map;
import javax.swing.JComponent;
import org.junit.Test;
import org.netbeans.junit.NbTestCase;
import org.openide.awt.NotificationDisplayer.Category;
import org.openide.awt.NotificationDisplayer.Priority;

/**
 *
 * @author Hector Espert
 */
public class NotificationDisplayerTest extends NbTestCase {
    
    private NotificationDisplayer notificationDisplayer;

    public NotificationDisplayerTest(String name) {
        super(name);
    }
    
    @Override
    public void setUp() {
        notificationDisplayer = NotificationDisplayer.getDefault();
        assertNotNull(notificationDisplayer);
        assertEquals("SimpleNotificationDisplayer", notificationDisplayer.getClass().getSimpleName());
    }

    @Test
    public void testNotify_4args() {
        Notification notification = notificationDisplayer.notify("title", null, "details", null);
        assertNotNull(notification);
        assertEquals("NotificationImpl", notification.getClass().getSimpleName());
        assertEquals("title - details", StatusDisplayer.getDefault().getStatusText());
    }

    @Test
    public void testNotify_5args_1() {
        Notification notification = notificationDisplayer.notify("title", null, "details", null, Priority.LOW);
        assertNotNull(notification);
        assertEquals("NotificationImpl", notification.getClass().getSimpleName());
        assertEquals("title - details", StatusDisplayer.getDefault().getStatusText());
    }

    @Test
    public void testNotify_6args_1() {
        Notification notification = notificationDisplayer.notify("title", null, "details", null, Priority.LOW, Category.ERROR);
        assertNotNull(notification);
        assertEquals("NotificationImpl", notification.getClass().getSimpleName());
        assertEquals("title - details", StatusDisplayer.getDefault().getStatusText());
    }

    @Test
    public void testNotify_6args_2() {
        Notification notification = notificationDisplayer.notify("title", null, "details", null, Priority.LOW, "default_category_error");
        assertNotNull(notification);
        assertEquals("NotificationImpl", notification.getClass().getSimpleName());
        assertEquals("title - details", StatusDisplayer.getDefault().getStatusText());
    }

    @Test
    public void testNotify_5args_2() {
        Notification notification = notificationDisplayer.notify("title", null, (JComponent)null, (JComponent)null, Priority.LOW);
        assertNotNull(notification);
        assertEquals("NotificationImpl", notification.getClass().getSimpleName());
        assertEquals("title", StatusDisplayer.getDefault().getStatusText());
    }

    @Test
    public void testNotify_6args_3() {
        Notification notification = notificationDisplayer.notify("title", null, (JComponent)null, (JComponent)null, Priority.LOW, Category.ERROR);
        assertNotNull(notification);
        assertEquals("NotificationImpl", notification.getClass().getSimpleName());
        assertEquals("title", StatusDisplayer.getDefault().getStatusText());
    }

    @Test
    public void testNotify_6args_4() {
        Notification notification = notificationDisplayer.notify("title", null, (JComponent)null, (JComponent)null, Priority.LOW, "default_category_error");
        assertNotNull(notification);
        assertEquals("NotificationImpl", notification.getClass().getSimpleName());
        assertEquals("title", StatusDisplayer.getDefault().getStatusText());
    }

    @Test
    public void testCreateCategory() {
        Map<String, String> attrs = new HashMap<>();
        attrs.put("categoryName", "TestName");
        attrs.put("localizingBundle", "org.openide.awt.TestBundle");
        attrs.put("diplayNameKey", "TestDisplayName");
        attrs.put("descriptionKey", "TestDescriptionName");
        Category category = NotificationDisplayer.createCategory(attrs);
        assertNotNull(category);
        
        assertEquals("TestName", category.getName());
        assertEquals("Test Display Name", category.getDisplayName());
        assertEquals("Test Description Name", category.getDescription());
    }
    
}
