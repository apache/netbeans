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
package org.netbeans.xml.test.actions;

import java.io.IOException;
import javax.swing.JComboBox;
import javax.swing.text.StyledDocument;
import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.OptionsOperator;
import org.netbeans.jellytools.OutputOperator;
import org.netbeans.jellytools.actions.ActionNoBlock;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JComboBoxOperator;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.jemmy.operators.JLabelOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.xml.test.core.XMLTest;

/**
 *
 * @author jindra
 */
public class DTDActionsTest extends XMLTest{
    private static boolean generateGoldenFiles = false;
    private static String projectName = "DTDActionsTestProject";
    private static String dtdfileName = "testDTD.dtd";
    private static final String cssFileName = "cssFile.css";
    
    /** Creates a new instance of DTDActionsTest */
    
    public DTDActionsTest(String testName) {
        super(testName);
    }
    //-----------------tests--------------------//
    public void testGenerateCSS()throws Exception{
        System.out.println("running testGenerateCSS");
        Node node = WebPagesNode.getInstance(projectName).getChild(dtdfileName, Node.class);
        generateCSS(node);
        ref(new EditorOperator(cssFileName).getText());
        ending();
    }
    
    public void testCheckDTD() throws Exception{
        System.out.println("running testCheckDTD");
        final String err = "<!ELEMENT POKUS  >";
        final String err2 = "(a  b)";
        final String err3 = ",";
        Node node = WebPagesNode.getInstance(projectName).getChild(dtdfileName, Node.class);
        StyledDocument doc = openFile(projectName, dtdfileName);
        checkDTD(node);
        doc.insertString(doc.getLength()-1, err, null);
        checkDTD(node);
        doc.insertString(doc.getLength()-3, err2, null);
        checkDTD(node);
        doc.insertString(doc.getLength()-5, err3, null);
        checkDTD(node);
        ending();
    }
    
    public void testCheckCSS() throws Exception{
        final String err1 = "\nNEW {";
        final String err2 = "{font-size: 12px; \n _color:black}";
        Node node = WebPagesNode.getInstance(projectName).getChild(cssFileName, Node.class);
        StyledDocument doc = openFile(projectName, cssFileName);
        checkCSS(node);
        doc.insertString(doc.getLength()-1, err1, null);
        checkCSS(node);
        doc.insertString(doc.getLength()-1, err2, null);
        checkCSS(node);
        int errPos = doc.getText(0, doc.getLength()).indexOf("{{");
        doc.remove(errPos, 1);
        checkCSS(node);
        errPos = doc.getText(0, doc.getLength()).indexOf("_");
        doc.remove(errPos, 1);
        checkCSS(node);
        ending();
    }
    
    public void testGenerateDocumentation() throws Exception{
        final String docName = "dokumentace";
        final String fileName = docName+".html";
        final String generated = "Generated.*\n";
        //dissable showing browser
        setSwingBrowser();
        Node node = WebPagesNode.getInstance(projectName).getChild(dtdfileName, Node.class);
        new ActionNoBlock(null, Bundle.getString(TOOLS_DOCLET_BUNDLE, "NAME_Generate_Documentation")).perform(node);
        JDialogOperator op = new JDialogOperator(Bundle.getString(TOOLS_GENERATOR_BUNDLE, "PROP_fileNameTitle"));
        new JTextFieldOperator(op, 0).setText(docName);
        new JButtonOperator(op, "OK").push();
        Node docNode = WebPagesNode.getInstance(projectName).getChild(fileName, Node.class);
        new ActionNoBlock(null, "Open").performPopup(docNode);
        Thread.sleep(5000);//wait opening the window
        String text = new EditorOperator(fileName).getText();
        text = text.replaceFirst(generated, generated);
        ref(text);
        ending();
    }
    
    //---------------private------------------//
    private void checkCSS(Node node) throws InterruptedException{
        new ActionNoBlock(null, Bundle.getString(CSS_ACTIONS_BUNDLE, "NAME_check_CSS")).perform(node);
        Thread.sleep(1000);//wait finishing action
        ref("-------checkCSS---------\n");
        //css output is in different pane then XML 
        String text =OutputOperator.invoke().getOutputTab(Bundle.getString(CSS_ACTIONS_BUNDLE, "TITLE_CSS_Check")).getText();
        ref(text);
    }
    
    private void checkDTD(Node node) throws InterruptedException{
        new ActionNoBlock(null, Bundle.getString(TOOLS_ACTIONS_BUNDLE, "NAME_Validate_DTD")).perform(node);
        Thread.sleep(1000);//wait finishing action
        writeIn();
    }
    
    private void generateCSS(Node node) throws IOException{
        new ActionNoBlock(null, Bundle.getString(TOOLS_ACTIONS_BUNDLE, "NAME_Generate_CSS")).perform(node);
        JDialogOperator op = new JDialogOperator(Bundle.getString(TOOLS_GENERATOR_BUNDLE, "PROP_fileNameTitle"));
        new JTextFieldOperator(op).setText(cssFileName.substring(0, cssFileName.length()-4));//removing ".css"
        new JButtonOperator(op, "OK").push();
    }
    
    private void setSwingBrowser() {
        OptionsOperator optionsOper = OptionsOperator.invoke();
        optionsOper.selectGeneral();
        // "Web Browser:"
        String webBrowserLabel = Bundle.getStringTrimmed(OPTIONS_GENERAL_BUNDLE, "CTL_Web_Browser");
        JLabelOperator jloWebBrowser = new JLabelOperator(optionsOper, webBrowserLabel);
        JComboBoxOperator combo = new JComboBoxOperator((JComboBox)jloWebBrowser.getLabelFor());
        combo.selectItem("Swing HTML Browser");
        optionsOper.ok();
    }
    
    public boolean generateGoldenFiles() {
        return generateGoldenFiles;
    }
    //-------------------main------------------//
    
    
    public static Test suite() {
        TestSuite suite = new NbTestSuite();
        initialization(projectName);
        suite.addTest(new DTDActionsTest("testGenerateCSS"));
        suite.addTest(new DTDActionsTest("testCheckDTD"));
        suite.addTest(new DTDActionsTest("testCheckCSS"));
       // suite.addTest(new DTDActionsTest("testGenerateDocumentation"));
        return suite;
    }
    
    public static void main(String[] args) throws Exception {
        //DEBUG = true;
        //JemmyProperties.getCurrentTimeouts().loadDebugTimeouts();
        TestRunner.run(suite());
    }
    
}
