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
            if (cake == null) fail("Cannot get 'ValidateXMLCookie'.");;
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
