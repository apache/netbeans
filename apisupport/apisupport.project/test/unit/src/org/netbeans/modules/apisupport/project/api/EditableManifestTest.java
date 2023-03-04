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

package org.netbeans.modules.apisupport.project.api;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import org.netbeans.junit.NbTestCase;

/**
 * Tests {@link EditableManifest} semantics.
 * @author Jesse Glick
 */
public class EditableManifestTest extends NbTestCase {

    static {
         // Make sure this test runs the same on Windows as on Unix.
        System.setProperty("line.separator", "\n");
    }

    public EditableManifestTest(String name) {
        super(name);
    }
    
    public void testCreateNew() throws Exception {
        EditableManifest m = new EditableManifest();
        assertEquals("Manifest-Version: 1.0\n\n", manifest2String(m));
        m.setAttribute("hello", "dolly", null);
        m.setAttribute("although", "earlier", null);
        m.setAttribute("later", "OK", null);
        m.setAttribute("unicode", "\u0950", null);
        m.setAttribute("later", "rewritten", null);
        m.addSection("some/section");
        m.setAttribute("whatever", "value", "some/section");
        m.addSection("earlier/section");
        m.setAttribute("some", "value", "earlier/section");
        m.setAttribute("whatever", "new value", "some/section");
        m.setAttribute("different", "value", "earlier/section");
        assertEquals("Manifest-Version: 1.0\n" +
                     "although: earlier\n" +
                     "hello: dolly\n" +
                     "later: rewritten\n" +
                     "unicode: \u0950\n" +
                     "\n" +
                     "Name: earlier/section\n" +
                     "different: value\n" +
                     "some: value\n" +
                     "\n" +
                     "Name: some/section\n" +
                     "whatever: new value\n" +
                     "\n",
            manifest2String(m));
    }
    
    public void testMalformedManifest() throws Exception {
        try {
            string2Manifest("something\n");
            fail("no value (main section)");
        } catch (IOException e) {}
        try {
            string2Manifest("key: val\n\nName: foo\nsomething\n");
            fail("no value (named section)");
        } catch (IOException e) {}
        try {
            string2Manifest("Name: foo\nValue: bar\n");
            fail("cannot start with section");
        } catch (IOException e) {}
        try {
            string2Manifest("something: here\nName: foo\n");
            fail("no blank line before section");
        } catch (IOException e) {}
        try {
            string2Manifest("something: here\n\nName: foo\nName: bar\n");
            fail("no blank line between sections");
        } catch (IOException e) {}
        try {
            string2Manifest("something: here\nsomething: again\n");
            fail("duplicated attrs");
        } catch (IOException e) {}
        try {
            string2Manifest("something: here\nSomething: again\n");
            fail("duplicated attrs (mixed case)");
        } catch (IOException e) {}
        try {
            string2Manifest("something: here\n\nName: foo\n\nName: foo\n");
            fail("duplicated sections");
        } catch (IOException e) {}
        try {
            string2Manifest("something: here\n\nName:\n\n");
            fail("blank Name not permitted");
        } catch (IOException e) {}
        try {
            string2Manifest("something: here\n\nbogus: val\n\n");
            fail("section must contain Name");
        } catch (IOException e) {}
        try {
            string2Manifest("something: here\n\nbogus: val\nName: x\n\n");
            fail("section must start with Name");
        } catch (IOException e) {}
    }
    
    public void testReadGeneral() throws Exception {
        EditableManifest m = string2Manifest("");
        assertEquals(Collections.EMPTY_SET, m.getAttributeNames(null));
        assertEquals(Collections.EMPTY_SET, m.getSectionNames());
        m = string2Manifest("Foo: val1\nBar: val2");
        assertEquals(new HashSet<String>(Arrays.asList("Foo", "Bar")), m.getAttributeNames(null));
        assertEquals("val1", m.getAttribute("Foo", null));
        assertEquals("val2", m.getAttribute("Bar", null));
        assertEquals(Collections.emptySet(), m.getSectionNames());
        m = string2Manifest("Foo: val1\nBar: val2\n\nName: something.class\nAttr: val\n\nName: other.class\nAttr: val2\n\n");
        assertEquals(new HashSet<String>(Arrays.asList("Foo", "Bar")), m.getAttributeNames(null));
        assertEquals("val1", m.getAttribute("foo", null));
        assertEquals("val2", m.getAttribute("bar", null));
        assertEquals(new HashSet<String>(Arrays.asList("something.class", "other.class")), m.getSectionNames());
        assertEquals(Collections.singleton("Attr"), m.getAttributeNames("something.class"));
        assertEquals("val", m.getAttribute("Attr", "something.class"));
        assertEquals(Collections.singleton("Attr"), m.getAttributeNames("other.class"));
        assertEquals("val2", m.getAttribute("Attr", "other.class"));
        m = string2Manifest("Foo :  bar \nBaz:quux");
        assertEquals(new HashSet<String>(Arrays.asList("Foo", "Baz")), m.getAttributeNames(null));
        assertEquals("bar ", m.getAttribute("Foo", null));
        assertEquals("quux", m.getAttribute("Baz", null));
    }
    
    public void testReadMissingSectionsAndAttributes() throws Exception {
        EditableManifest m = string2Manifest("Foo: val1\n\nName: something.class\nAttr: val\n\n");
        assertEquals(null, m.getAttribute("dummy", null));
        assertEquals(null, m.getAttribute("dummy", "something.class"));
        try {
            m.getAttribute("dummy", "nonexistent.class");
            fail("nonexistent section");
        } catch (IllegalArgumentException e) {}
    }
    
    public void testReadContinuationLines() throws Exception {
        EditableManifest m = string2Manifest("Attr: some long value which\n  cannot fit on one line\n\nName: foo\nAttr: again \n here");
        assertEquals("some long value which cannot fit on one line", m.getAttribute("Attr", null));
        assertEquals("again here", m.getAttribute("Attr", "foo"));
    }
    
    public void testEdit() throws Exception {
        EditableManifest m = string2Manifest("A1: v1\nA2: v2\n\nName: n1\nA1: v1\nA2: v2\n\nName: n2\nA1: v1\n\n");
        m.removeSection("n2");
        try {
            m.removeSection("n3");
            fail("Cannot remove nonexistent section");
        } catch (IllegalArgumentException e) {}
        m.removeAttribute("A2", null);
        try {
            m.removeAttribute("A3", null);
            fail("Cannot remove nonexistent attr");
        } catch (IllegalArgumentException e) {}
        m.removeAttribute("A1", "n1");
        try {
            m.removeAttribute("A3", "n1");
            fail("Cannot remove nonexistent attr");
        } catch (IllegalArgumentException e) {}
        try {
            m.removeAttribute("A1", "n2");
            fail("Cannot remove attr from nonexistent section");
        } catch (IllegalArgumentException e) {}
        m.addSection("n3");
        m.setAttribute("A3", "v3", null);
        m.setAttribute("A3", "v3", "n1");
        m.setAttribute("A3", "v3", "n3");
        m.setAttribute("A3", "v3a", "n3");
        try {
            m.setAttribute("A1", "v1", "n2");
            fail("cannot set attr in nonexistent section");
        } catch (IllegalArgumentException e) {}
        assertEquals("A1: v1\n" +
                     "A3: v3\n" +
                     "\n" +
                     "Name: n1\n" +
                     "A2: v2\n" +
                     "A3: v3\n" +
                     "\n" +
                     "Name: n3\n" +
                     "A3: v3a\n" +
                     "\n",
            manifest2String(m));
    }
    
    public void testModifyOutOfOrderAttr() throws Exception {
        EditableManifest m = string2Manifest("A2: v2\nA1: v1\n\n");
        m.setAttribute("A1", "v1a", null);
        assertEquals("A2: v2\nA1: v1a\n\n", manifest2String(m));
        m.setAttribute("A1", "v1a", null);
        assertEquals("A2: v2\nA1: v1a\n\n", manifest2String(m));
    }
    
    public void testAlphabetization() throws Exception {
        EditableManifest m = string2Manifest("aa: x\nM: x\nz: x\n\nName: aa\n\nName: m\n\nName: z\n\n");
        m.setAttribute("a", "x", null);
        m.setAttribute("B", "x", null);
        m.setAttribute("n", "x", null);
        m.addSection("a");
        m.addSection("k");
        m.addSection("z2");
        m.setAttribute("z", "x", "m");
        m.setAttribute("a", "x", "m");
        assertEquals("a: x\n" +
                     "aa: x\n" +
                     "B: x\n" +
                     "M: x\n" +
                     "n: x\n" +
                     "z: x\n" +
                     "\n" +
                     "Name: a\n" +
                     "\n" +
                     "Name: aa\n" +
                     "\n" +
                     "Name: k\n" +
                     "\n" +
                     "Name: m\n" +
                     "a: x\n" +
                     "z: x\n" +
                     "\n" +
                     "Name: z\n" +
                     "\n" +
                     "Name: z2\n" +
                     "\n",
            manifest2String(m));
    }
    
    public void testCaseInsensitivityOfAttributeNames() throws Exception {
        EditableManifest m = string2Manifest("a: x\nB: x\n\nName: n\na: x\nB: x\n\n");
        assertEquals("x", m.getAttribute("A", null));
        assertEquals("x", m.getAttribute("b", null));
        assertEquals("x", m.getAttribute("A", "n"));
        assertEquals("x", m.getAttribute("b", "n"));
        try {
            m.getAttribute("a", "N");
            fail("section names case sensitive");
        } catch (IllegalArgumentException e) {}
        m.setAttribute("A", "x2", null);
        m.setAttribute("b", "x2", "n");
        m.removeAttribute("b", null);
        m.removeAttribute("A", "n");
        assertEquals("A: x2\n\nName: n\nb: x2\n\n", manifest2String(m));
    }
    
    public void testManifestVersionAlwaysInsertedFirst() throws Exception {
        EditableManifest m = string2Manifest("b: x\n\n");
        m.setAttribute("Manifest-Version", "1.0", null);
        assertEquals("Manifest-Version: 1.0\nb: x\n\n", manifest2String(m));
        m.setAttribute("a", "x", null);
        assertEquals("Manifest-Version: 1.0\na: x\nb: x\n\n", manifest2String(m));
        m.setAttribute("Manifest-Version", "1.1", null);
        assertEquals("Manifest-Version: 1.1\na: x\nb: x\n\n", manifest2String(m));
    }
    
    public void testPreserveFormatting() throws Exception {
        EditableManifest m = string2Manifest("A:x\nB : x\nC: lo\n ng \n as heck\nD: x\nE :lo\n ng\n\n\nName: n");
        m.setAttribute("a", "x", null);
        m.setAttribute("c", "long as heck", null);
        m.setAttribute("d", "x2", null);
        m.setAttribute("E", "longer", null);
        assertEquals("A:x\nB : x\nC: lo\n ng \n as heck\nd: x2\nE: longer\n\n\nName: n\n", manifest2String(m));
    }
    
    public void test66341() throws Exception {
        EditableManifest m = string2Manifest("A: x\nB: y\n");
        m.addSection("x");
        //System.err.println(manifest2String(m));
        assertEquals("adding a section always inserts a blank line", "A: x\nB: y\n\nName: x\n\n", manifest2String(m));
    }
    
    private static EditableManifest string2Manifest(String text) throws Exception {
        return new EditableManifest(new ByteArrayInputStream(text.getBytes(StandardCharsets.UTF_8)));
    }
    
    private static String manifest2String(EditableManifest em) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        em.write(baos);
        return baos.toString("UTF-8");
    }
    
}
