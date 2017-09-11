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
package org.netbeans.xml.test.actions;

import java.io.IOException;
import javax.swing.text.StyledDocument;
import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.EditorWindowOperator;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.actions.ActionNoBlock;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.operators.JTextFieldOperator;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.xml.test.core.XMLTest;
import org.netbeans.xml.test.core.wizardoperator.TransformationWizardOperator;

/**
 * <P>
 * <P>
 * <FONT COLOR="#CC3333" FACE="Courier New, Monospaced" SIZE="+1">
 * <B>
 * <BR> XML Module Jemmy Test: NewFromTemplate
 * </B>
 * </FONT>
 * <BR><BR><B>What it tests:</B><BR>
 *
 * This test tests New From Template action on all XML's templates.
 *
 * <BR><BR><B>How it works:</B><BR>
 *
 * 1) create new documents from template<BR>
 * 2) write the created documents to output<BR>
 * 3) close source editor<BR>
 *
 * <BR><BR><B>Settings:</B><BR>
 * none<BR>
 *
 * <BR><BR><B>Output (Golden file):</B><BR>
 * Set XML documents.<BR>
 *
 * <BR><B>To Do:</B><BR>
 * none<BR>
 *
 * <P>Created on Januar 09, 2001, 12:33 PM
 * <P>
 */

/**
 *
 * @author jindra
 */
public class XMLActionsTest extends XMLTest {
    private static String projectName = "ActionsTestProject";
    
    private static boolean generateGoldenFiles = false;
    
    /** Creates new CoreTemplatesTest */
    public XMLActionsTest(String testName) {
        super(testName);
    }
    
    
    //----------------- TESTS ------------------//
    public void testXMLWellFormed() throws Exception{
        System.out.println("running testXMLWellFormed");
        String fileName = "well.xml";
        Node node = WebPagesNode.getInstance(projectName).getChild(fileName, Node.class);
        StyledDocument doc = openFile(projectName, fileName);
        checkXML(node);
        int error = doc.getText(0, doc.getLength()).indexOf("notes")+4;
        doc.remove(error, 1);
        checkXML(node);
        validateXML(node);
        error = doc.getText(0, doc.getLength()).indexOf("a=");
        doc.remove(error, 5);
        validateXML(node);
        ending();
    }
    
    public void testXMLDTDFormed() throws Exception{
        final String err = "<!!!>";
        System.out.println("running testDTDWellFormed");
        String fileName = "DTDformed.xml";
        Node node = WebPagesNode.getInstance(projectName).getChild(fileName, Node.class);
        StyledDocument doc = openFile(projectName, fileName);
        checkXML(node);
        int error = doc.getText(0, doc.getLength()).indexOf(err);
        doc.remove(error, err.length());
        checkXML(node);
        validateXML(node);
        error = doc.getText(0,doc.getLength()).indexOf("<collection>")-1;
        doc.insertString(error, "<!DOCTYPE collection SYSTEM 'DTDformed.dtd'>", null);
        validateXML(node);
        error = doc.getText(0,doc.getLength()).indexOf("jmeno");
        doc.remove(error, 15);
        validateXML(node);
        error = doc.getText(0,doc.getLength()).indexOf("alcohol");
        doc.insertString(error, "calories = \"nut\" ", null);
        validateXML(node);
        ending();
    }
    
    public void testXMLXSDFormed() throws Exception{
        final String err1 = "shiporders";
        final String err2 = "title";
        final String err3 = "orderid=";
        System.out.println("running testXMLXSDFormed");
        String fileName = "shiporders.xml";
        Node node = WebPagesNode.getInstance(projectName).getChild(fileName, Node.class);
        StyledDocument doc = openFile(projectName, fileName);
        checkXML(node);
        validateXML(node);
        int error = doc.getText(0, doc.getLength()).indexOf(err1)+err1.length()+1;
        doc.insertString(error, "xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' xsi:noNamespaceSchemaLocation='shiporder.xsd'", null);
        error = doc.getText(0, doc.getLength()).indexOf(err2);
        doc.insertString(error,"s", null);
        checkXML(node);
        doc.remove(error, 1);
        error = doc.getText(0, doc.getLength()).indexOf(err3)+err3.length()+1;
        doc.insertString(error, "1", null);
        checkXML(node);
        validateXML(node);
        doc.remove(error+2,1);
        validateXML(node);
        ending();
    }
    
    public void testGenerateDTD()throws Exception{
        System.out.println("running testGenerateDTD");
        String fileName = "shiporders.xml";
        Node node = WebPagesNode.getInstance(projectName).getChild(fileName, Node.class);
        generateDTD(node);
        ref(EditorWindowOperator.getEditor().getText());
        ending();
    }
    
    public void testXSLT() throws Exception{
        System.out.println("runnning testXSLT");
        String fileName = "sampleXMLSchema.xml";
        String outputName = "transform.xml";
        Node node = WebPagesNode.getInstance(projectName).getChild(fileName, Node.class);
        transformXSLT(node, outputName);
        node = WebPagesNode.getInstance(projectName).getChild(outputName, Node.class);
        new ActionNoBlock(null, "Edit").perform(node);//open output
        Thread.sleep(1000);//wait for opening a window
        String text = new EditorOperator(outputName).getText();
        //create one line because of Windows are adding few empty lines
        text = text.replaceAll("\n", "");
        ref(text);
        ending();
    }
    
    // ------------- LIB --------------------------//
    
    public boolean generateGoldenFiles() {
        return generateGoldenFiles;
    }
    
    
    private void checkXML(Node node) throws InterruptedException{
        new ActionNoBlock(null, Bundle.getString(TOOLS_ACTIONS_BUNDLE, "NAME_Check_XML")).perform(node);
        writeIn();
    }
    
    private void validateXML(Node node) throws InterruptedException{
        new ActionNoBlock(null, Bundle.getString(TOOLS_ACTIONS_BUNDLE, "NAME_Validate_XML")).perform(node);
        writeIn();
    }
    
    private void generateDTD(Node node) throws IOException {
        final String nameDTD = "newGeneratedDTD";
        new ActionNoBlock(null, Bundle.getString(TOOLS_GENERATOR_BUNDLE, "PROP_GenerateDTD")).perform(node);
        NbDialogOperator dOp = new NbDialogOperator(Bundle.getString(TOOLS_GENERATOR_BUNDLE, "PROP_fileNameTitle"));
        new JTextFieldOperator(dOp, 0).setText(nameDTD);
        dOp.ok();
    }
    
    private void transformXSLT(Node node, String outputName) throws InterruptedException{
        String XSLTName = "XMLSchema2GUI.xslt";
        new ActionNoBlock(null, Bundle.getString(XSL_ACTIONS_BUNDLE, "NAME_transform_action")).perform(node);
        TransformationWizardOperator twiz = new TransformationWizardOperator(Bundle.getString(XSL_TRANSFORM_BUNDLE, "NAME_transform_panel_title"));
        assertTrue("source dialog is enabled!!", !twiz.source().isEnabled());
        twiz.output().addItem(outputName);
        twiz.skript().addItem(XSLTName);
        assertEquals("processOutput checkbox doesn't contain 3 possibilities", twiz.processOutput().getItemCount(), 3);
        twiz.processOutput().selectItem(0);
        twiz.overwrite().changeSelection(true);
        twiz.ok();
    }
    
    // ----------------------- MAIN ---------------------------//
    
    public static Test suite() {
        TestSuite suite = new NbTestSuite();
        initialization(projectName);
        suite.addTest(new XMLActionsTest("testXMLWellFormed"));
        suite.addTest(new XMLActionsTest("testXMLDTDFormed"));
        suite.addTest(new XMLActionsTest("testXMLXSDFormed"));
        suite.addTest(new XMLActionsTest("testGenerateDTD"));
        suite.addTest(new XMLActionsTest("testXSLT"));
        return suite;
    }
    
    public static void main(String[] args) throws Exception {
        //DEBUG = true;
        //JemmyProperties.getCurrentTimeouts().loadDebugTimeouts();
        TestRunner.run(suite());
    }
    
}
