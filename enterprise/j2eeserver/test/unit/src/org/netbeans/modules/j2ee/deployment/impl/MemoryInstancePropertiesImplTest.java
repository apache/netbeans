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
package org.netbeans.modules.j2ee.deployment.impl;

import java.util.Enumeration;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import org.netbeans.junit.NbTestCase;

/**
 *
 * @author Petr Hejl
 */
public class MemoryInstancePropertiesImplTest extends NbTestCase {

    public MemoryInstancePropertiesImplTest(String name) {
        super(name);
    }

    public void testProperties() {
        MemoryInstancePropertiesImpl props = new MemoryInstancePropertiesImpl("something");
        props.setProperty("A", "A");
        assertEquals("A", props.getProperty("A"));
        props.setProperty("A", "B");
        assertEquals("B", props.getProperty("A"));
        props.setProperty("B", "B");
        assertEquals("B", props.getProperty("B"));

        Set names = new HashSet();
        for (Enumeration e = props.propertyNames(); e.hasMoreElements(); ) {
            names.add(e.nextElement());
        }
        assertEquals(2, names.size());
        assertTrue(names.contains("A"));
        assertTrue(names.contains("B"));

        Properties toLoad = new Properties();
        toLoad.setProperty("C", "C");
        toLoad.setProperty("D", "D");
        props.setProperties(toLoad);
        assertEquals("B", props.getProperty("A"));
        assertEquals("B", props.getProperty("B"));
        assertEquals("C", props.getProperty("C"));
        assertEquals("D", props.getProperty("D"));

        names = new HashSet();
        for (Enumeration e = props.propertyNames(); e.hasMoreElements(); ) {
            names.add(e.nextElement());
        }
        assertEquals(4, names.size());
        assertTrue(names.contains("A"));
        assertTrue(names.contains("B"));
        assertTrue(names.contains("C"));
        assertTrue(names.contains("D"));
    }

    public void testDeletion() {
        MemoryInstancePropertiesImpl props = new MemoryInstancePropertiesImpl("something");
        props.setProperty("A", "A");
        props.setProperty("B", "B");
        props.instanceRemoved("something");

        assertTrue(props.isDeleted());
        try {
            props.getProperty("A");
            fail("Does not throw ISE when deleted");
        } catch (IllegalStateException ex) {
            // expected
        }

        try {
            props.propertyNames();
            fail("Does not throw ISE when deleted");
        } catch (IllegalStateException ex) {
            // expected
        }

        try {
            props.setProperty("C", "C");
            fail("Does not throw ISE when deleted");
        } catch (IllegalStateException ex) {
            // expected
        }

        try {
            props.setProperties(new Properties());
            fail("Does not throw ISE when deleted");
        } catch (IllegalStateException ex) {
            // expected
        }
    }
}
