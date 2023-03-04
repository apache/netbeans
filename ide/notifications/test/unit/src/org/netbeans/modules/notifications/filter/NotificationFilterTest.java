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
package org.netbeans.modules.notifications.filter;

import java.util.prefs.Preferences;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import org.netbeans.modules.notifications.NotificationImpl;
import org.openide.awt.NotificationDisplayer;

/**
 *
 * @author Hector Espert
 */
public class NotificationFilterTest {
    
    private NotificationFilter notificationFilter;
    
    @Before
    public void setUp() {
        notificationFilter = new NotificationFilter("testfilter");
    }
   

    @Test
    public void testIsEnabled() {
        NotificationImpl errorNotification = new NotificationImpl(null, null, null, NotificationDisplayer.Category.ERROR, null);
        assertTrue(notificationFilter.isEnabled(errorNotification));
        
        CategoryFilter categoryFilter = new CategoryFilter();
        categoryFilter.setEnabled("default_category_error", false);
        notificationFilter.setCategoryFilter(categoryFilter);
        
        assertFalse(notificationFilter.isEnabled(errorNotification));
    }

    @Test
    public void testClone() {
        CategoryFilter categoryFilter = new CategoryFilter();
        categoryFilter.setEnabled("default_category_error", false);
        notificationFilter.setCategoryFilter(categoryFilter);
        
        NotificationFilter cloned = (NotificationFilter) notificationFilter.clone();
        assertNotNull(cloned);
        
        assertEquals(notificationFilter.getName(), cloned.getName());
        assertEquals(notificationFilter.getCategoryFilter().isEnabled("default_category_error"), cloned.getCategoryFilter().isEnabled("default_category_error"));
    }

    @Test
    public void testToString() {
        assertEquals("testfilter", notificationFilter.toString());
    }

    @Test
    public void testLoad() throws Exception {
        Preferences preferences = new TestPreferences();
        notificationFilter.load(preferences, "test");
        
        assertEquals("test_name Filter", notificationFilter.getName());
        assertTrue(notificationFilter.getCategoryFilter().isEnabled("test_category_error"));
        
    }

    @Test
    public void testSave() throws Exception {
        Preferences preferences = new TestPreferences();
        notificationFilter.save(preferences, "test");
    }
    
}
