/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
