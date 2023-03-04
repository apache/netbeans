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
package org.netbeans.modules.languages.yaml;

import java.util.Iterator;
import java.util.List;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.netbeans.modules.csl.api.StructureItem;
import org.netbeans.modules.csl.spi.DefaultError;
import org.netbeans.modules.parsing.api.Snapshot;
import org.snakeyaml.engine.v2.exceptions.ParserException;
import org.snakeyaml.engine.v2.exceptions.ScannerException;

/**
 *
 * @author lkishalmi
 */
public class YamlSectionTest {

    public YamlSectionTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of before method, of class YamlSection.
     */
    @Test
    public void testBefore() {
        YamlSection instance = new YamlSection("name: John Smith");
        YamlSection expResult = new YamlSection("name");
        YamlSection result = instance.before(4);
        assertEquals(expResult, result);
    }

    @Test
    public void testBeforeEmpty1() {
        YamlSection instance = new YamlSection("name: John Smith");
        YamlSection result = instance.before(0);
        assertTrue(result.isEmpty());
    }

    @Test
    public void testBeforeEmpty2() {
        YamlSection instance = new YamlSection("");
        YamlSection result = instance.before(0);
        assertTrue(instance.isEmpty());
        assertTrue(result.isEmpty());
    }

    /**
     * Test of after method, of class YamlSection.
     */
    @Test
    public void testAfter() {
        YamlSection instance = new YamlSection("name: John Smith");
        YamlSection expResult = new YamlSection(4, ": John Smith");
        YamlSection result = instance.after(4);
        assertEquals(expResult, result);
    }

    @Test
    public void testAfterEmpty1() {
        YamlSection instance = new YamlSection("name: John Smith");
        YamlSection result = instance.after(16);
        assertEquals(16, result.offset);
        assertTrue(result.isEmpty());
    }

    @Test
    public void testAfterEmpty2() {
        YamlSection instance = new YamlSection("");
        YamlSection result = instance.after(0);
        assertTrue(instance.isEmpty());
        assertTrue(result.isEmpty());
    }

    @Test
    public void testTrimTail() {
        YamlSection instance = new YamlSection("name: { \t\n");
        YamlSection expResult = new YamlSection("name: ");
        YamlSection result = instance.trimTail();
        assertEquals(expResult, result);
    }

    @Test
    public void testTrimTailEmpty1() {
        YamlSection instance = new YamlSection("name: ");
        YamlSection expResult = new YamlSection("");
        YamlSection result = instance.trimTail();
        assertEquals(expResult, result);
    }

    @Test
    public void testTrimTailEmpty2() {
        YamlSection instance = new YamlSection(42, "");
        YamlSection expResult = new YamlSection(42, "");
        YamlSection result = instance.trimTail();
        assertEquals(expResult, result);
    }

    @Test
    public void testTrimHead() {
        YamlSection instance = new YamlSection("name: { \t\n");
        YamlSection expResult = new YamlSection(5, " { \t\n");
        YamlSection result = instance.trimHead();
        assertEquals(expResult, result);
    }

    @Test
    public void testTrimHeadEmpty3() {
        YamlSection instance = new YamlSection("name:");
        YamlSection expResult = new YamlSection(5, "");
        YamlSection result = instance.trimHead();
        assertEquals(expResult, result);
    }

    @Test
    public void testTrimHeadEmpty1() {
        YamlSection instance = new YamlSection(" {");
        YamlSection expResult = new YamlSection(2, "");
        YamlSection result = instance.trimHead();
        assertEquals(expResult, result);
    }

    @Test
    public void testTrimHeadEmpty2() {
        YamlSection instance = new YamlSection(42, "");
        YamlSection expResult = new YamlSection(42, "");
        YamlSection result = instance.trimHead();
        assertEquals(expResult, result);
    }

    /**
     * Test of isEmpty method, of class YamlSection.
     */
    @Test
    public void testIsEmpty() {
        YamlSection instance1 = new YamlSection(42, "");
        YamlSection instance2 = new YamlSection(42, "name:");
        assertTrue(instance1.isEmpty());
        assertFalse(instance2.isEmpty());
    }

    /**
     * Test of length method, of class YamlSection.
     */
    @Test
    public void testLength() {
        YamlSection instance = new YamlSection(42, "name:");
        assertEquals(5, instance.length());
    }

    @Test
    public void testSplit1() {
        YamlSection instance = new YamlSection("name: John Smith");
        YamlSection section1 = new YamlSection("name: ");
        YamlSection section2 = new YamlSection(11, "Smith");
        Iterator<YamlSection> result = instance.split(6, 11).iterator();
        assertEquals(section2, result.next());
        assertEquals(section1, result.next());
        assertFalse(result.hasNext());
    }

    @Test
    public void testSplit2() {
        YamlSection instance = new YamlSection("name: John Smith");
        YamlSection section1 = new YamlSection("name: ");
        YamlSection section2 = new YamlSection(6, "John Smith");
        Iterator<YamlSection> result = instance.split(6).iterator();
        assertEquals(section2, result.next());
        assertEquals(section1, result.next());
        assertFalse(result.hasNext());
    }

    @Test
    public void testSplitEmpty1() {
        YamlSection instance = new YamlSection("name: John Smith");
        Iterator<YamlSection> result = instance.split(0, 11).iterator();
        YamlSection section2 = new YamlSection(11, "Smith");
        assertEquals(section2, result.next());
        assertFalse(result.hasNext());
    }

    @Test
    public void testSplitEmpty2() {
        YamlSection instance = new YamlSection("name: John Smith");
        YamlSection section1 = new YamlSection("name: ");
        Iterator<YamlSection> result = instance.split(6, 16).iterator();
        assertEquals(section1, result.next());
        assertFalse(result.hasNext());
    }

    @Test
    public void testSplitEmpty3() {
        YamlSection instance = new YamlSection("name: John Smith");
        Iterator<YamlSection> result = instance.split(0, 16).iterator();
        assertFalse(result.hasNext());
    }

    @Test
    public void testSplitEmpty4() {
        YamlSection instance = new YamlSection("name: { ");
        YamlSection section1 = new YamlSection("name: ");
        Iterator<YamlSection> result = instance.split(8).iterator();
        assertEquals(section1, result.next());
        assertFalse(result.hasNext());
    }

    @Test
    public void testSplitEmpty5() {
        YamlSection instance = new YamlSection(" \nname: {");
        YamlSection section1 = new YamlSection(7, " {");
        Iterator<YamlSection> result = instance.split(0).iterator();
        assertEquals(section1, result.next());
        assertFalse(result.hasNext());
    }

    @Test
    public void testSplitEmpty6() {
        YamlSection instance = new YamlSection("");
        assertTrue(instance.split(0).isEmpty());
    }
}
