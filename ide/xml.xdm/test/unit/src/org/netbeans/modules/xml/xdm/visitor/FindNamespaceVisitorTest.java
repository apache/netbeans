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

/*
 * FindNamespaceVisitorTest.java
 * JUnit based test
 *
 * Created on November 18, 2005, 10:34 AM
 */

package org.netbeans.modules.xml.xdm.visitor;

import junit.framework.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.netbeans.modules.xml.xdm.nodes.Attribute;
import org.netbeans.modules.xml.xdm.nodes.Node;
import org.netbeans.modules.xml.xdm.nodes.Document;
import org.w3c.dom.NamedNodeMap;
import org.netbeans.modules.xml.xdm.XDMModel;
import org.netbeans.modules.xml.xdm.Util;

/**
 *
 * @author ajit
 */
public class FindNamespaceVisitorTest extends TestCase {
    
    public FindNamespaceVisitorTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
    }

    public static Test suite() {
        TestSuite suite = new TestSuite();
//        Disabled as referenced files were partly not donated by oracle to apache
//        suite.addTestSuite(FindNamespaceVisitorTest.class);
        return suite;
    }

    /**
     * Test of findNamespace method, of class org.netbeans.modules.xml.xdm.visitor.FindNamespaceVisitor.
     */
    public void testFindNamespace() throws Exception {
        
        XDMModel xdmModel = Util.loadXDMModel("diff/TravelItinerary1.xsd");
        Document root = xdmModel.getDocument();
        FindNamespaceVisitor instance = new FindNamespaceVisitor(root);
        
        Node target = (Node)root.getDocumentElement().getChildNodes().item(19).
                getChildNodes().item(3).getChildNodes().item(3);
        String expResult = "http://www.w3.org/2001/XMLSchema";
        String result = instance.findNamespace(target);
        assertEquals(expResult, result);

        target = (Node)root.getDocumentElement().getChildNodes().item(19).
                getChildNodes().item(3).getChildNodes().item(3).getAttributes().item(0);
        expResult = null;
        result = instance.findNamespace(target);
        assertEquals(expResult, result);
    }
    
}
