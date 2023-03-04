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
package org.netbeans.modules.j2ee.persistence.jpqleditor.ui;

import java.beans.IntrospectionException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;

public class ReflectionInfoTest {

    public ReflectionInfoTest() {
    }

    @Test
    public void testSingleResult() throws IntrospectionException {
        List<ReflectionInfo> ri = ReflectionInfo.prepare(Collections.singletonList(1));
        assertEquals(1, ri.size());
        assertNull(ri.get(0).getIndex());
        assertNull(ri.get(0).getPropertyName());
    }

    @Test
    public void testSimpleObject() throws IntrospectionException {
        List<ReflectionInfo> ri = ReflectionInfo.prepare(Collections.singletonList(new BaseObject()));
        assertEquals(1, ri.size());
        assertNull(ri.get(0).getIndex());
        assertEquals("id", ri.get(0).getPropertyName());
    }

    @Test
    public void testInheritetObject() throws IntrospectionException {
        List<ReflectionInfo> ri = ReflectionInfo.prepare(Collections.singletonList(new DemoObject()));
        assertEquals(3, ri.size());
        assertNull(ri.get(0).getIndex());
        assertEquals("demoBool", ri.get(0).getPropertyName());
        assertNull(ri.get(1).getIndex());
        assertEquals("id", ri.get(1).getPropertyName());
        assertNull(ri.get(2).getIndex());
        assertEquals("title", ri.get(2).getPropertyName());
    }

    @Test
    public void testMixedList() throws IntrospectionException {
        List<ReflectionInfo> ri = ReflectionInfo.prepare(Arrays.asList(new BaseObject[]{new DemoObject2(), new DemoObject()}));
        assertEquals(4, ri.size());
        assertNull(ri.get(0).getIndex());
        assertEquals("demoBool", ri.get(0).getPropertyName());
        assertNull(ri.get(1).getIndex());
        assertEquals("id", ri.get(1).getPropertyName());
        assertNull(ri.get(2).getIndex());
        assertEquals("title", ri.get(2).getPropertyName());
        assertNull(ri.get(3).getIndex());
        assertEquals("titleId", ri.get(3).getPropertyName());
    }

    @Test
    public void testMixedRowArray() throws IntrospectionException {
        List<ReflectionInfo> ri = ReflectionInfo.prepare(Collections.singletonList(new Object[]{1, new DemoObject()}));
        assertEquals(4, ri.size());
        assertEquals(0, ri.get(0).getIndex().intValue());
        assertNull(ri.get(0).getPropertyName());
        assertEquals(1, ri.get(1).getIndex().intValue());
        assertEquals("demoBool", ri.get(1).getPropertyName());
        assertEquals(1, ri.get(2).getIndex().intValue());
        assertEquals("id", ri.get(2).getPropertyName());
        assertEquals(1, ri.get(3).getIndex().intValue());
        assertEquals("title", ri.get(3).getPropertyName());
    }

    @Test
    public void testNullRowLeadsToNonArray() throws IntrospectionException {
        List<ReflectionInfo> ri = ReflectionInfo.prepare(Arrays.asList(
                new Object[]{1, new DemoObject()},
                null,
                new Object[]{2, new DemoObject2()}
        ));
        assertEquals(1, ri.size());
        assertNull(ri.get(0).getPropertyName());
        assertNull(ri.get(0).getIndex());
    }
    
    @Test
    public void testNullValues() throws IntrospectionException {
        List<ReflectionInfo> ri = ReflectionInfo.prepare(Arrays.asList(
                null,
                null,
                null
        ));
        assertEquals(1, ri.size());
        assertNull(ri.get(0).getPropertyName());
        assertNull(ri.get(0).getIndex());
    }
    
    @Test
    public void testNullValueArrays() throws IntrospectionException {
        List<ReflectionInfo> ri = ReflectionInfo.prepare(Arrays.asList(
                new Object[] {null, null},
                new Object[] {null, null},
                new Object[] {null, null}
        ));
        assertEquals(2, ri.size());
        assertNull(ri.get(0).getPropertyName());
        assertEquals(0, ri.get(0).getIndex().intValue());
        assertNull(ri.get(1).getPropertyName());
        assertEquals(1, ri.get(1).getIndex().intValue());
    }
    
    @Test
    public void testPartitialNullValueArrays() throws IntrospectionException {
        List<ReflectionInfo> ri = ReflectionInfo.prepare(Arrays.asList(
                new Object[] {null, null},
                new Object[] {1, new BaseObject()},
                new Object[] {null, null}
        ));
        assertEquals(3, ri.size());
        assertNull(ri.get(0).getPropertyName());
        assertEquals(0, ri.get(0).getIndex().intValue());
        assertNull(ri.get(1).getPropertyName());
        assertEquals(1, ri.get(1).getIndex().intValue());
        assertEquals("id", ri.get(2).getPropertyName());
        assertEquals(1, ri.get(2).getIndex().intValue());
    }
    
    public static class BaseObject {

        private int id;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }
    }

    public static class DemoObject2 extends BaseObject {

        private int titleId;

        public int getTitleId() {
            return titleId;
        }

        public void setTitleId(int titleId) {
            this.titleId = titleId;
        }

    }

    public static class DemoObject extends BaseObject {

        private String title;
        private boolean demoBool;

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public boolean isDemoBool() {
            return demoBool;
        }

        public void setDemoBool(boolean demoBool) {
            this.demoBool = demoBool;
        }
    }
}
