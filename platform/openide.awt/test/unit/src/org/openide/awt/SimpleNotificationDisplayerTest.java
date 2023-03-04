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
public class SimpleNotificationDisplayerTest extends NbTestCase {
    
    private NotificationDisplayer notificationDisplayer;

    public SimpleNotificationDisplayerTest(String name) {
        super(name);
    }
    
    @Override
    public void setUp() {
        notificationDisplayer = NotificationDisplayer.getDefault();
        assertNotNull(notificationDisplayer);
        assertEquals("Expected SimpleNotificationDisplayer implementation if any other is not loaded", "SimpleNotificationDisplayer", notificationDisplayer.getClass().getSimpleName());
    }

    @Test
    public void testNotify() {
        Notification notification = notificationDisplayer.notify("title", null, "details", null);
        assertNotNull(notification);
        assertEquals("title - details", StatusDisplayer.getDefault().getStatusText());
        
        notification = notificationDisplayer.notify("title", null, "details2", null, Priority.LOW);
        assertNotNull(notification);
        assertEquals("title - details2", StatusDisplayer.getDefault().getStatusText());
        
        notification = notificationDisplayer.notify("title", null, "details3", null, Priority.LOW, Category.ERROR);
        assertNotNull(notification);
        assertEquals("title - details3", StatusDisplayer.getDefault().getStatusText());
        
        notification = notificationDisplayer.notify("title", null, "details4", null, Priority.LOW, "default_category_error");
        assertNotNull(notification);
        assertEquals("title - details4", StatusDisplayer.getDefault().getStatusText());
        
        notification = notificationDisplayer.notify("jcomponent1", null, (JComponent)null, (JComponent)null, Priority.LOW);
        assertNotNull(notification);
        assertEquals("jcomponent1", StatusDisplayer.getDefault().getStatusText());
        
        notification = notificationDisplayer.notify("jcomponent2", null, (JComponent)null, (JComponent)null, Priority.LOW, Category.ERROR);
        assertNotNull(notification);
        assertEquals("jcomponent2", StatusDisplayer.getDefault().getStatusText());
        
        notification = notificationDisplayer.notify("jcomponent3", null, (JComponent)null, (JComponent)null, Priority.LOW, "default_category_error");
        assertNotNull(notification);
        assertEquals("jcomponent3", StatusDisplayer.getDefault().getStatusText());
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
