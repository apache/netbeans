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

package org.netbeans.modules.ant.grammar;

import java.io.File;
import java.util.HashSet;
import java.util.Set;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.xml.api.model.HintContext;
import org.openide.modules.InstalledFileLocator;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Test functionality of TestUtil.
 * @author Jesse Glick
 */
public class TestUtilTest extends NbTestCase {

    public TestUtilTest(String name) {
        super(name);
    }

    public void testCreateCompletion() throws Exception {
        HintContext c = TestUtil.createCompletion("<fooHERE/>");
        assertHintContext(c, Node.ELEMENT_NODE, "foo", null, "foo");
        assertTrue("right type acc. to instanceof", c instanceof Element);
        c = TestUtil.createCompletion("<foo/>");
        assertNull("no hint here", c);
        c = TestUtil.createCompletion("<foo><barHERE/></foo>");
        assertHintContext(c, Node.ELEMENT_NODE, "bar", null, "bar");
        Node n = c.getParentNode();
        assertEquals("parent is an element", Node.ELEMENT_NODE, n.getNodeType());
        assertEquals("parent is <foo>", "foo", n.getNodeName());
        c = TestUtil.createCompletion("<foo><bar attrHERE='whatever'/></foo>");
        assertHintContext(c, Node.ATTRIBUTE_NODE, "attr", null, "attr");
        Element owner = ((Attr)c).getOwnerElement();
        assertEquals("parent is <bar>", "bar", owner.getNodeName());
        c = TestUtil.createCompletion("<foo><bar attr='somethingHERE'/></foo>");
        assertHintContext(c, Node.ATTRIBUTE_NODE, null, "something", "something");
        owner = ((Attr)c).getOwnerElement();
        assertEquals("parent is <bar>", "bar", owner.getNodeName());
        c = TestUtil.createCompletion("<foo>somethingHERE</foo>");
        assertHintContext(c, Node.TEXT_NODE, null, "something", "something");
        n = c.getParentNode();
        assertEquals("parent is an element", Node.ELEMENT_NODE, n.getNodeType());
        assertEquals("parent is <foo>", "foo", n.getNodeName());
    }
    
    private static void assertHintContext(HintContext c, int type, String name, String value, String prefix) {
        assertNotNull("found it", c);
        assertEquals("right type", type, c.getNodeType());
        if (name != null) {
            assertEquals("right node name", name, c.getNodeName());
        }
        if (value != null) {
            assertEquals("right node value", value, c.getNodeValue());
        }
        assertEquals("right prefix", prefix, c.getCurrentPrefix());
    }
    
    public void testCreateElementInDocument() throws Exception {
        Element e = TestUtil.createElementInDocument("<foo><bar/></foo>", "bar", null);
        assertNotNull("got it", e);
        assertEquals("right one", "bar", e.getTagName());
        Node p = e.getParentNode();
        assertEquals("parent is an element too", Node.ELEMENT_NODE, p.getNodeType());
        assertEquals("parent is right", "foo", p.getNodeName());
    }
    
    private interface Foo {}
    private interface Quux extends Foo {}
    private static class Bar implements Quux {}
    private static class Baz extends Bar {}
    public void testFindAllInterfaces() throws Exception {
        Set<Class> s = new HashSet<Class>();
        TestUtil.findAllInterfaces(Baz.class, s);
        assertEquals("two interfaces here", 2, s.size());
        assertTrue("Foo included", s.contains(Foo.class));
        assertTrue("Quux included", s.contains(Quux.class));
    }
    
    public void testInstalledFileLocator() throws Exception {
        File antHome = InstalledFileLocator.getDefault().locate("ant", null, false);
        assertNotNull("found antHome", antHome);
        assertTrue(antHome + " is a directory", antHome.isDirectory());
        assertTrue("contains ant.jar", new File(new File(antHome, "lib"), "ant.jar").isFile());
        File antBridge = InstalledFileLocator.getDefault().locate("ant/nblib/bridge.jar", null, false);
        assertNotNull("found antBridge", antBridge);
        assertTrue("is a file", antBridge.isFile());
    }
    
}
