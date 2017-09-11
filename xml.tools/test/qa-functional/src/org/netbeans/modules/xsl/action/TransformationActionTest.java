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
package org.netbeans.modules.xsl.action;

import java.awt.event.KeyEvent;
import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.EditorWindowOperator;
import org.netbeans.jellytools.actions.OpenAction;
import org.netbeans.jellytools.modules.xml.XSLTransformationDialog;
import org.netbeans.jellytools.modules.xsl.actions.TransformAction;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.tests.xml.JXTest;
import org.openide.loaders.DataObject;

/** Checks XSL Transformation action. */

public class TransformationActionTest extends JXTest {
    
    /** Creates new XMLNodeTest */
    public TransformationActionTest(String testName) {
        super(testName);
    }
    
    // TESTS ///////////////////////////////////////////////////////////////////
    
    /** Performs 'XSL Transformation...' action and checks output. */
    public void testTransformation() throws Exception {
        
        final String OUT_FILE = "../out/document.html";
        //final String OUT_FILE = "output.html"; //!!!
        final String OUT_NODE = "out" + DELIM + "document";
        //final String OUT_NODE = "sources" + DELIM + "output"; //!!!
        
        // clear output and display Transformation Dialog
        DataObject dao = TestUtil.THIS.findData("out/document.html");
        if (dao != null) /* then */ dao.delete();
        XSLTransformationDialog dialog = transformXML("sources" + DELIM + "document");
        
        // fill in the TransformationDialog and execute transformation
        dialog.cboXSLTScript().clearText();
        dialog.cboXSLTScript().typeText("../styles/doc2html.xsl");
        dialog.cboXSLTScript().pressKey(KeyEvent.VK_TAB);
        
        dialog.cboOutput().clearText();
        dialog.cboOutput().typeText(OUT_FILE);
        dialog.cboJComboBox().selectItem(dialog.ITEM_DONOTHING);
        dialog.oK();
        
        // check the transformation's output
        char[] cbuf = new char[4000];
        Node htmlNode = findDataNode(OUT_NODE);
        new OpenAction().perform(htmlNode);
        // force editor to reload document
        EditorWindowOperator ewo = new EditorWindowOperator();
        EditorOperator eo = ewo.getEditor(htmlNode.getText());
        eo.setCaretPositionToLine(1);
        eo.insert("\n");
        eo.waitModified(true);
        eo.deleteLine(1);
        eo.save();
        
        String substring = "<h1>Testing Document</h1>";
        boolean result = eo.getText().indexOf(substring) != -1;
        assertTrue("Cannot find control substring:\n" + substring, (result));
        //ewo.close(); //!!! on test machines throws JemmyException: Exception in setClosed
    }
    
    /** Displays XSL Transformation Dialog and vrerifies it */
    public void testTransformationDialog() throws Exception {
        // display Transformation Dialog
        XSLTransformationDialog dialog = transformXML("sources" + DELIM + "document");
        dialog.verify();
        dialog.close();
    }
    
    // LIB ////////////////////////////////////////////////////////////////////
    
    /**
     * Performs 'XSL Transformation...' action on a XML.
     * @param path relative to the 'data' folder delimited by 'DELIM'
     */
    private XSLTransformationDialog transformXML(String path) throws Exception {
        TransformAction transform =  new TransformAction();
        transform.perform(findDataNode(path));
        XSLTransformationDialog dialog =  new XSLTransformationDialog();
        dialog.activate();
        return dialog;
    }
    
    // MAIN ////////////////////////////////////////////////////////////////////
    
    public static Test suite() {
        TestSuite suite = new NbTestSuite();
        suite.addTest(new TransformationActionTest("testTransformationDialog"));
        suite.addTest(new TransformationActionTest("testTransformation"));
        return suite;
    }
    
    public static void main(String[] args) throws Exception {
        System.setProperty("xmltest.dbgTimeouts", "true");
        //TestRunner.run(TransformationActionTest.class);
        TestRunner.run(suite());
    }
}
