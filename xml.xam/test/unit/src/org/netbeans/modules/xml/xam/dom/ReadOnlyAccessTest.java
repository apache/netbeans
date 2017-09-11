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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.xml.xam.dom;

import junit.framework.*;
import org.netbeans.modules.xml.xam.TestComponent2;
import org.netbeans.modules.xml.xam.TestModel2;
import org.netbeans.modules.xml.xam.Util;

/**
 *
 * @author nn136682
 */
public class ReadOnlyAccessTest extends TestCase {
    
    // Length of line separator. It is 2 for Windows and 1 for Linux or MacOS
    private static int lsl = System.getProperty("line.separator").length(); // NOI18N

    public ReadOnlyAccessTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
    }

    protected void tearDown() throws Exception {
    }

    public static Test suite() {
        return new TestSuite(ReadOnlyAccessTest.class);
    }

    public void testFindPosition() throws Exception {
        TestModel2 model = Util.loadModel("resources/test1.xml");
        assertEquals(38 + lsl, model.getRootComponent().findPosition());
        assertEquals(4, model.getRootComponent().getChildren().size());
        TestComponent2 component = model.getRootComponent().getChildren().get(0);
        assertEquals("a", component.getPeer().getLocalName());
        assertEquals(133 + lsl * 4, component.findPosition());
    }

    public void testFindPositionWithPrefix() throws Exception {
        TestModel2 model = Util.loadModel("resources/test1.xml");
        TestComponent2.B b = model.getRootComponent().getChildren(TestComponent2.B.class).get(0);
        TestComponent2.Aa aa = b.getChildren(TestComponent2.Aa.class).get(0);
        assertEquals(206 + lsl * 6, aa.findPosition());
    }

    public void testFindPositionWithElementTagInAttr() throws Exception {
        TestModel2 model = Util.loadModel("resources/test1.xml");
        TestComponent2.C c = model.getRootComponent().getChildren(TestComponent2.C.class).get(0);
        assertEquals(245 + lsl * 8, c.findPosition());
    }

    public void testFindElement() throws Exception {
        TestModel2 model = Util.loadModel("resources/test1.xml");
        TestComponent2 component = model.getRootComponent().getChildren().get(0);
        assertEquals(component, model.findComponent(133 + lsl * 4));
        assertEquals(component, model.findComponent(147 + lsl * 4));
        assertEquals(component, model.findComponent(161 + lsl * 5));
    }
    
    public void testFindElementWithPrefix() throws Exception {
        TestModel2 model = Util.loadModel("resources/test1.xml");
        TestComponent2.B b = model.getRootComponent().getChildren(TestComponent2.B.class).get(0);
        TestComponent2.Aa aa = b.getChildren(TestComponent2.Aa.class).get(0);
        assertEquals(aa, model.findComponent(206 + lsl * 6));
        assertEquals(aa, model.findComponent(218 + lsl * 6));
        assertEquals(aa, model.findComponent(232 + lsl * 6));
    }

    public void testFindElementWithTagInAttr() throws Exception {
        TestModel2 model = Util.loadModel("resources/test1.xml");
        TestComponent2.C c = model.getRootComponent().getChildren(TestComponent2.C.class).get(0);
        assertEquals(c, model.findComponent(245 + lsl * 8));
        assertEquals(c, model.findComponent(249 + lsl * 8));
        assertEquals(c, model.findComponent(261 + lsl * 8));
    }
    
    public void testFindElementGivenTextPosition() throws Exception {
        TestModel2 model = Util.loadModel("resources/test1.xml");
        TestComponent2 root = model.getRootComponent();
        TestComponent2.B b = root.getChildren(TestComponent2.B.class).get(0);
        assertEquals(b, model.findComponent(201 + lsl * 5));
        assertEquals(root, model.findComponent(244 + lsl * 8));
        assertEquals(root, model.findComponent(265 + lsl * 9));
    }    
    
    public void testGetXmlFragment() throws Exception {
        TestModel2 model = Util.loadModel("resources/test1.xml");
        TestComponent2 root = model.getRootComponent();
        TestComponent2.B b = model.getRootComponent().getChildren(TestComponent2.B.class).get(0);
        String result = b.getXmlFragment();
        assertTrue(result.startsWith(" <!-- comment -->"));
        assertTrue(result.indexOf("value=\"c\"/>") > 0);
    }

}
