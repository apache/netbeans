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

import java.util.Iterator;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Hector Espert
 */
public class FilterRepositoryTest {
    
    private FilterRepository filterRepository;
    
    @Before
    public void setUp() {
        filterRepository = new FilterRepository();
    }

    @Test
    public void testAssign() {
        FilterRepository instance = new FilterRepository();
        instance.setActive(NotificationFilter.EMPTY);
        filterRepository.assign(instance);
        assertEquals(instance.getAllFilters(), filterRepository.getAllFilters());
        assertEquals(NotificationFilter.EMPTY, filterRepository.getActive());
    }

    @Test
    public void testClone() {
        FilterRepository result = (FilterRepository) filterRepository.clone();
        assertEquals(filterRepository.getAllFilters(), result.getAllFilters());
        assertEquals(filterRepository.getActive(), result.getActive());
    }

    @Test
    public void testGetAllFilters() {
        assertNotNull(filterRepository.getAllFilters());
    }

    @Test
    public void testAddAndRemoveFilter() {
        filterRepository.add(NotificationFilter.EMPTY);
        assertTrue(filterRepository.getFilters().contains(NotificationFilter.EMPTY));
        filterRepository.remove(NotificationFilter.EMPTY);
        assertFalse(filterRepository.getFilters().contains(NotificationFilter.EMPTY));
    }

    @Test
    public void testIterator() {
        assertTrue(filterRepository.iterator() instanceof Iterator);
    }

    @Test
    public void testSize() {
        assertEquals(0, filterRepository.size());
        filterRepository.add(NotificationFilter.EMPTY);
        assertEquals(1, filterRepository.size());
    }

    @Test
    public void testGetFilterByName() {
        NotificationFilter filter = NotificationFilter.EMPTY;
        filter.setName("EMPTY");
        filterRepository.add(filter);
        assertEquals(filter, filterRepository.getFilterByName("EMPTY"));
    }

    @Test
    public void testGetActive() {
        NotificationFilter filter = NotificationFilter.EMPTY;
        filterRepository.setActive(filter);
        assertEquals(filter, filterRepository.getActive());
    }

    @Test
    public void testSaveAndLoad() throws Exception {
        NotificationFilter filter = NotificationFilter.EMPTY;
        filter.setName("EMPTY");
        filterRepository.add(filter);
        assertEquals(1, filterRepository.size());
        
        filterRepository.save();
        
        filterRepository.clear();
        assertEquals(0, filterRepository.size());
        
        filterRepository.load();
        assertEquals(1, filterRepository.size());
        assertEquals("EMPTY", filterRepository.getActive().getName());
    }

    @Test
    public void testCreateNewFilter() {
        NotificationFilter result = filterRepository.createNewFilter();
        assertNotNull(result);
        assertEquals("New Filter", result.getName());
    }

    @Test
    public void testGetFilters() {
        assertNotNull(filterRepository.getFilters());
    }
    
}
