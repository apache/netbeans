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
        assertEquals(860 + lsl, model.getRootComponent().findPosition());
        assertEquals(4, model.getRootComponent().getChildren().size());
        TestComponent2 component = model.getRootComponent().getChildren().get(0);
        assertEquals("a", component.getPeer().getLocalName());
        assertEquals(955 + lsl * 4, component.findPosition());
    }

    public void testFindPositionWithPrefix() throws Exception {
        TestModel2 model = Util.loadModel("resources/test1.xml");
        TestComponent2.B b = model.getRootComponent().getChildren(TestComponent2.B.class).get(0);
        TestComponent2.Aa aa = b.getChildren(TestComponent2.Aa.class).get(0);
        assertEquals(1028 + lsl * 6, aa.findPosition());
    }

    public void testFindPositionWithElementTagInAttr() throws Exception {
        TestModel2 model = Util.loadModel("resources/test1.xml");
        TestComponent2.C c = model.getRootComponent().getChildren(TestComponent2.C.class).get(0);
        assertEquals(1067 + lsl * 8, c.findPosition());
    }

    public void testFindElement() throws Exception {
        TestModel2 model = Util.loadModel("resources/test1.xml");
        TestComponent2 component = model.getRootComponent().getChildren().get(0);
        assertEquals(component, model.findComponent(955 + lsl * 4));
        assertEquals(component, model.findComponent(969 + lsl * 4));
        assertEquals(component, model.findComponent(983 + lsl * 5));
    }
    
    public void testFindElementWithPrefix() throws Exception {
        TestModel2 model = Util.loadModel("resources/test1.xml");
        TestComponent2.B b = model.getRootComponent().getChildren(TestComponent2.B.class).get(0);
        TestComponent2.Aa aa = b.getChildren(TestComponent2.Aa.class).get(0);
        assertEquals(aa, model.findComponent(1028 + lsl * 6));
        assertEquals(aa, model.findComponent(1040 + lsl * 6));
        assertEquals(aa, model.findComponent(1054 + lsl * 6));
    }

    public void testFindElementWithTagInAttr() throws Exception {
        TestModel2 model = Util.loadModel("resources/test1.xml");
        TestComponent2.C c = model.getRootComponent().getChildren(TestComponent2.C.class).get(0);
        assertEquals(c, model.findComponent(1067 + lsl * 8));
        assertEquals(c, model.findComponent(1071 + lsl * 8));
        assertEquals(c, model.findComponent(1083 + lsl * 8));
    }
    
    public void testFindElementGivenTextPosition() throws Exception {
        TestModel2 model = Util.loadModel("resources/test1.xml");
        TestComponent2 root = model.getRootComponent();
        TestComponent2.B b = root.getChildren(TestComponent2.B.class).get(0);
        assertEquals(b, model.findComponent(1023 + lsl * 5));
        assertEquals(root, model.findComponent(1066 + lsl * 8));
        assertEquals(root, model.findComponent(1087 + lsl * 9));
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
