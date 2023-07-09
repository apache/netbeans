/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.xml.tools.actions;

import junit.framework.Test;
import junit.textui.TestRunner;
import org.netbeans.api.xml.cookies.ValidateXMLCookie;
import org.netbeans.junit.NbTestSuite;
import org.openide.nodes.Node;

public class ValidateActionTest extends AbstractCheckTest {

    /** Creates new ValidateActionTest */
    public ValidateActionTest(String testName) {
        super(testName);
    }

    // TESTS ///////////////////////////////////////////////////////////////////

    // *** Not  well-formed ***

    /** Validates document with incorrectly nested tags */
    public void testIncorrectlyNestedTags() throws Exception {
        performAction("IncorrectlyNestedTags.xml", new int[] {6});
    }
    
    /** Validates document where missing closing tags */
    public void testMissingClosingTag() throws Exception {
        performAction("MissingClosingTag.xml", new int[] {7});
    }
    
    /** Validates document without root element */
    public void testMissingRootElement() throws Exception {
        performAction("MissingRootElement.xml", new int[] {-1});
    }

    // *** Not valid but well-formed (DTD) ***
    
    /** Validates document with undeclared element */
    public void testInvalidElementName() throws Exception {
        performAction("InvalidElementName.xml", new int[] {9, 11});
    }
    
    /** Validates document with inaccessble DTD */
    public void testInaccessbleDTD() throws Exception {
        performAction("InaccessbleDTD.xml", new int[] {3});
    }
    
    // *** Not valid but well-formed (Schema) ***

    /** Validates document according to schema */
    public void testInvalidElementNameSD() throws Exception {
        performAction("InvalidElementNameSD.xml", new int[] {15});
    }

    /** Validates document according to schema */
    public void testInvalidSchemaLocationSD() throws Exception {
        performAction("InvalidSchemaLocationSD.xml", new int[] {6});
    }
    
    // *** Valid (DTD) ***
    
    /** Validates document where DTD is distributed in several folders*/
    public void testDistributedDTD() throws Exception {
        performAction("DistributedDTD.xml", 0);
    }
    
    // *** Valid (Schema) ***
    
    /** Validates document according to schema */
    public void testValidSD() throws Exception {
        performAction("ValidSD.xml", 0);
    }
    
    // LIBS ////////////////////////////////////////////////////////////////////
    
    /** Check all selected nodes. */
    protected QaIOReporter performAction(Node[] nodes) {
        if ((nodes == null) || (nodes.length == 0))
            fail("Ileegal argumet 'null'");
        
        QaIOReporter reporter = new QaIOReporter();
        for (int i = 0; i<nodes.length; i++) {
            ValidateXMLCookie cake = (ValidateXMLCookie) nodes[i].getCookie(ValidateXMLCookie.class);
            if (cake == null) fail("Cannot get 'ValidateXMLCookie'.");
            cake.validateXML(reporter);
        }
        return reporter;
    }
    
    // MAIN ////////////////////////////////////////////////////////////////////
    
//    public static Test suite() {
//        NbTestSuite suite = new NbTestSuite();
//        suite.addTest(new ValidateActionTest("testInvalidSchemaLocationSD"));
//        return suite;
//    }
    
    /**
     * Performs this testsuite.
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        DEBUG = true;
        // TestRunner.run(suite());
        TestRunner.run(ValidateActionTest.class);
    }
}
