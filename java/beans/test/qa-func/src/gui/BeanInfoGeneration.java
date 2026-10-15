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

package gui;

import java.io.*;
import junit.textui.TestRunner;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.MainWindowOperator;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.RepositoryTabOperator;
import org.netbeans.jellytools.actions.PropertiesAction;
import org.netbeans.jellytools.nodes.JavaNode;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.properties.Property;
import org.netbeans.jellytools.properties.PropertySheetOperator;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.operators.*;
import org.netbeans.jemmy.operators.Operator.DefaultStringComparator;
import org.netbeans.junit.NbTestSuite;

import org.openide.actions.SaveAllAction;
import org.openide.filesystems.FileObject;

import org.openide.filesystems.Repository;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;

public class BeanInfoGeneration extends JellyTestCase {
    
    private static final String NAME_TEST_FILE          = "TestFile";
    private static final String NAME_INDEX_PROPERTY     = "indexProperty";
    private static final String NAME_NON_INDEX_PROPERTY = "nonIndexProperty";
    
    private static final int DELAY = 2000;
    
    private static final String sampleDir = Utilities.findFileSystem("src").getDisplayName();
    
    /** Need to be defined because of JUnit */
    public BeanInfoGeneration(String name) {
        super(name);
    }
    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(new BeanInfoGeneration("testCheckNodes"));
        suite.addTest(new BeanInfoGeneration("testIncludeExclude"));
        suite.addTest(new BeanInfoGeneration("testBeanInfoNode"));
        suite.addTest(new BeanInfoGeneration("testPropertiesNode"));
        suite.addTest(new BeanInfoGeneration("testNodesDescription"));
        suite.addTest(new BeanInfoGeneration("testGenerateNewBeanInfo"));
        suite.addTest(new BeanInfoGeneration("testRegenerateBeanInfo"));
        suite.addTest(new BeanInfoGeneration("testCheckBeanInfoCompilability"));
        
        return suite;
    }
    
    /** Use for execution inside IDE */
    public static void main(java.lang.String[] args) {
        // run whole suite
        TestRunner.run(suite());
        // run only selected test case
        //junit.textui.TestRunner.run(new BeanInfoGeneration("testGenerateNewBeanInfo"));
    }
    
    /** setUp method  */
    public void setUp() {
        System.out.println("########  "+getName()+"  #######");

        new EventTool().waitNoEvent(DELAY);
        new PropertiesAction().perform();
        
        FileObject testFile = Repository.getDefault().findResource("gui/data/" + NAME_TEST_FILE + ".java");
        FileObject destination = Repository.getDefault().findFileSystem(sampleDir.replace('\\', '/')).getRoot();
        
        try {
            DataObject.find(testFile).copy(DataFolder.findFolder(destination));
        } catch (IOException e) {
            fail(e);
        }
    }
    
    /** tearDown method */
    public void tearDown() {
        
        ((SaveAllAction) SaveAllAction.findObject(SaveAllAction.class, true)).performAction();
        
        Utilities.delete(NAME_TEST_FILE + ".java");
        Utilities.delete(NAME_TEST_FILE + "BeanInfo.java");
        new RepositoryTabOperator().getRootNode().select();
        /*
        FilesystemNode fsNode = new FilesystemNode(repositoryRootNode, sampleDir);
        fsNode.unmount();
         */
    }
    
    private void createContent() {
        // Start - NonIndexProperty
        RepositoryTabOperator explorerOperator = new RepositoryTabOperator();
                
        Node repositoryRootNode = explorerOperator.getRootNode();
        Node patternsNode = new Node(repositoryRootNode, sampleDir+"|"+NAME_TEST_FILE+"|"+"class "+NAME_TEST_FILE+"|"+Bundle.getString("org.netbeans.modules.beans.Bundle", "Patterns"));
        patternsNode.select();
        patternsNode.performPopupActionNoBlock(Bundle.getString("org.openide.src.nodes.Bundle", "LAB_Add")+"|"+Bundle.getString("org.netbeans.modules.beans.Bundle", "MENU_CREATE_PROPERTY"));
        String dialogTitle = Bundle.getString("org.netbeans.modules.beans.Bundle", "CTL_TITLE_NewProperty");
        NbDialogOperator nbDialogOperator = new NbDialogOperator(dialogTitle);
        
        JTextFieldOperator jTextFieldOperator = new JTextFieldOperator(nbDialogOperator, 0);
        jTextFieldOperator.typeText(NAME_NON_INDEX_PROPERTY);
        JComboBoxOperator jComboBoxOperator = new JComboBoxOperator(nbDialogOperator, 0);
        jComboBoxOperator.typeText("String");
        jComboBoxOperator = new JComboBoxOperator(nbDialogOperator, 1);
        jComboBoxOperator.setSelectedItem(Bundle.getString("org.netbeans.modules.beans.Bundle", "LAB_ReadWriteMODE"));
        JCheckBoxOperator jCheckBoxOperator = new JCheckBoxOperator(nbDialogOperator, Bundle.getString("org.netbeans.modules.beans.Bundle","CTL_PropertyPanel_fieldCheckBox"));
        jCheckBoxOperator.push();
        jCheckBoxOperator = new JCheckBoxOperator(nbDialogOperator, Bundle.getString("org.netbeans.modules.beans.Bundle", "CTL_PropertyPanel_setCheckBox"));
        jCheckBoxOperator.push();
        jCheckBoxOperator = new JCheckBoxOperator(nbDialogOperator, Bundle.getString("org.netbeans.modules.beans.Bundle", "CTL_PropertyPanel_returnCheckBox"));
        jCheckBoxOperator.push();
        jCheckBoxOperator = new JCheckBoxOperator(nbDialogOperator, Bundle.getString("org.netbeans.modules.beans.Bundle", "CTL_PropertyPanel_constrainedCheckBox"));
        jCheckBoxOperator.push();
        jCheckBoxOperator = new JCheckBoxOperator(nbDialogOperator, Bundle.getString("org.netbeans.modules.beans.Bundle", "CTL_PropertyPanel_boundCheckBox"));
        jCheckBoxOperator.push();
        jCheckBoxOperator = new JCheckBoxOperator(nbDialogOperator, Bundle.getString("org.netbeans.modules.beans.Bundle", "CTL_PropertyPanel_supportCheckBox"));
        jCheckBoxOperator.push();
        //new EventTool().waitNoEvent(1500);
        nbDialogOperator.ok();
        // End - NonIndexProperty
        // Start - IndexProperty
        explorerOperator = new RepositoryTabOperator();
        
        repositoryRootNode = explorerOperator.getRootNode();
        patternsNode = new Node(repositoryRootNode, sampleDir+"|"+NAME_TEST_FILE+"|"+"class "+NAME_TEST_FILE+"|"+Bundle.getString("org.netbeans.modules.beans.Bundle", "Patterns"));
        patternsNode.select();
        patternsNode.performPopupActionNoBlock(Bundle.getString("org.openide.src.nodes.Bundle", "LAB_Add")+"|"+Bundle.getString("org.netbeans.modules.beans.Bundle", "MENU_CREATE_IDXPROPERTY"));
        dialogTitle = Bundle.getString("org.netbeans.modules.beans.Bundle", "CTL_TITLE_NewIdxProperty");
        nbDialogOperator = new NbDialogOperator(dialogTitle);
        
        jTextFieldOperator = new JTextFieldOperator(nbDialogOperator, 0);
        jTextFieldOperator.typeText(NAME_INDEX_PROPERTY);
        jComboBoxOperator = new JComboBoxOperator(nbDialogOperator, 0);
        jComboBoxOperator.setSelectedItem("String");
        jComboBoxOperator = new JComboBoxOperator(nbDialogOperator, 1);
        jComboBoxOperator.setSelectedItem(Bundle.getString("org.netbeans.modules.beans.Bundle", "LAB_ReadWriteMODE"));
        jCheckBoxOperator = new JCheckBoxOperator(nbDialogOperator, Bundle.getString("org.netbeans.modules.beans.Bundle","CTL_IdxPropertyPanel_fieldCheckBox"));
        jCheckBoxOperator.push();
        jCheckBoxOperator = new JCheckBoxOperator(nbDialogOperator, Bundle.getString("org.netbeans.modules.beans.Bundle", "CTL_IdxPropertyPanel_setCheckBox"));
        jCheckBoxOperator.push();
        jCheckBoxOperator = new JCheckBoxOperator(nbDialogOperator, Bundle.getString("org.netbeans.modules.beans.Bundle", "CTL_IdxPropertyPanel_returnCheckBox"));
        jCheckBoxOperator.push();
        jCheckBoxOperator = new JCheckBoxOperator(nbDialogOperator, Bundle.getString("org.netbeans.modules.beans.Bundle", "CTL_IdxPropertyPanel_niSetterCheckBox"));
        jCheckBoxOperator.push();
        jCheckBoxOperator = new JCheckBoxOperator(nbDialogOperator, Bundle.getString("org.netbeans.modules.beans.Bundle", "CTL_IdxPropertyPanel_niGetterCheckBox"));
        jCheckBoxOperator.push();
        jCheckBoxOperator = new JCheckBoxOperator(nbDialogOperator, Bundle.getString("org.netbeans.modules.beans.Bundle", "CTL_IdxPropertyPanel_niSetCheckBox"));
        jCheckBoxOperator.push();
        jCheckBoxOperator = new JCheckBoxOperator(nbDialogOperator, Bundle.getString("org.netbeans.modules.beans.Bundle", "CTL_IdxPropertyPanel_niReturnCheckBox"));
        jCheckBoxOperator.push();
        jCheckBoxOperator = new JCheckBoxOperator(nbDialogOperator, Bundle.getString("org.netbeans.modules.beans.Bundle", "CTL_IdxPropertyPanel_constrainedCheckBox"));
        jCheckBoxOperator.push();
        jCheckBoxOperator = new JCheckBoxOperator(nbDialogOperator, Bundle.getString("org.netbeans.modules.beans.Bundle", "CTL_IdxPropertyPanel_boundCheckBox"));
        jCheckBoxOperator.push();
        jCheckBoxOperator = new JCheckBoxOperator(nbDialogOperator, Bundle.getString("org.netbeans.modules.beans.Bundle", "CTL_IdxPropertyPanel_supportCheckBox"));
        jCheckBoxOperator.push();
        //new EventTool().waitNoEvent(1500);
        nbDialogOperator.ok();
        // End - IndexProperty
        // Start - UnicastEventSource
        explorerOperator = new RepositoryTabOperator();
        
        repositoryRootNode = explorerOperator.getRootNode();
        patternsNode = new Node(repositoryRootNode, sampleDir+"|"+NAME_TEST_FILE+"|"+"class "+NAME_TEST_FILE+"|"+Bundle.getString("org.netbeans.modules.beans.Bundle", "Patterns"));
        patternsNode.select();
        patternsNode.performPopupActionNoBlock(Bundle.getString("org.openide.src.nodes.Bundle", "LAB_Add")+"|"+Bundle.getString("org.netbeans.modules.beans.Bundle", "MENU_CREATE_UNICASTSE"));
        dialogTitle = Bundle.getString("org.netbeans.modules.beans.Bundle", "CTL_TITLE_NewUniCastES");
        nbDialogOperator = new NbDialogOperator(dialogTitle);
        
        jComboBoxOperator = new JComboBoxOperator(nbDialogOperator, 0);
        jComboBoxOperator.setSelectedItem("java.awt.event.ActionListener");
        JRadioButtonOperator jRadioButtonOperator = new JRadioButtonOperator(nbDialogOperator, Bundle.getString("org.netbeans.modules.beans.Bundle", "CTL_UEventSetPanel_implRadioButton"));
        jRadioButtonOperator.push();
        jCheckBoxOperator = new JCheckBoxOperator(nbDialogOperator, Bundle.getString("org.netbeans.modules.beans.Bundle","CTL_UEventSetPanel_fireCheckBox"));
        jCheckBoxOperator.push();
        jCheckBoxOperator = new JCheckBoxOperator(nbDialogOperator, Bundle.getString("org.netbeans.modules.beans.Bundle","CTL_UEventSetPanel_passEventCheckBox"));
        jCheckBoxOperator.push();
        
        //new EventTool().waitNoEvent(1500);
        
        nbDialogOperator.ok();
        // End - UnicastEventSource
        // Start - MulticastEventSourceArrayListImpl
        explorerOperator = new RepositoryTabOperator();
        
        repositoryRootNode = explorerOperator.getRootNode();
        patternsNode = new Node(repositoryRootNode, sampleDir+"|"+NAME_TEST_FILE+"|"+"class "+NAME_TEST_FILE+"|"+Bundle.getString("org.netbeans.modules.beans.Bundle", "Patterns"));
        patternsNode.select();
        patternsNode.performPopupActionNoBlock(Bundle.getString("org.openide.src.nodes.Bundle", "LAB_Add")+"|"+Bundle.getString("org.netbeans.modules.beans.Bundle", "MENU_CREATE_MULTICASTSE"));
        dialogTitle = Bundle.getString("org.netbeans.modules.beans.Bundle", "CTL_TITLE_NewMultiCastES");
        nbDialogOperator = new NbDialogOperator(dialogTitle);
        
        jComboBoxOperator = new JComboBoxOperator(nbDialogOperator, 0);
        jComboBoxOperator.setSelectedItem("java.awt.event.ItemListener");
        
        jRadioButtonOperator = new JRadioButtonOperator(nbDialogOperator, Bundle.getString("org.netbeans.modules.beans.Bundle", "CTL_EventSetPanel_alRadioButton"));
        jRadioButtonOperator.push();
        jCheckBoxOperator = new JCheckBoxOperator(nbDialogOperator, Bundle.getString("org.netbeans.modules.beans.Bundle","CTL_EventSetPanel_fireCheckBox"));
        jCheckBoxOperator.push();
        jCheckBoxOperator = new JCheckBoxOperator(nbDialogOperator, Bundle.getString("org.netbeans.modules.beans.Bundle","CTL_EventSetPanel_passEventCheckBox"));
        jCheckBoxOperator.push();
        
        //new EventTool().waitNoEvent(1500);
        
        nbDialogOperator.ok();
        // End - MulticastEventSourceArrayListImpl
        // Start - MulticastEventSourceEventListenerListImpl
        explorerOperator = new RepositoryTabOperator();
        
        repositoryRootNode = explorerOperator.getRootNode();
        patternsNode = new Node(repositoryRootNode, sampleDir+"|"+NAME_TEST_FILE+"|"+"class "+NAME_TEST_FILE+"|"+Bundle.getString("org.netbeans.modules.beans.Bundle", "Patterns"));
        patternsNode.select();
        patternsNode.performPopupActionNoBlock(Bundle.getString("org.openide.src.nodes.Bundle", "LAB_Add")+"|"+Bundle.getString("org.netbeans.modules.beans.Bundle", "MENU_CREATE_MULTICASTSE"));
        dialogTitle = Bundle.getString("org.netbeans.modules.beans.Bundle", "CTL_TITLE_NewMultiCastES");
        nbDialogOperator = new NbDialogOperator(dialogTitle);
        
        jComboBoxOperator = new JComboBoxOperator(nbDialogOperator, 0);
        jComboBoxOperator.setSelectedItem("java.awt.event.FocusListener");
        
        jRadioButtonOperator = new JRadioButtonOperator(nbDialogOperator, Bundle.getString("org.netbeans.modules.beans.Bundle", "CTL_EventSetPanel_ellRadioButton"));
        jRadioButtonOperator.push();
        jCheckBoxOperator = new JCheckBoxOperator(nbDialogOperator, Bundle.getString("org.netbeans.modules.beans.Bundle","CTL_EventSetPanel_fireCheckBox"));
        jCheckBoxOperator.push();
        new EventTool().waitNoEvent(400);
        jCheckBoxOperator = new JCheckBoxOperator(nbDialogOperator, Bundle.getString("org.netbeans.modules.beans.Bundle","CTL_EventSetPanel_passEventCheckBox"));
        jCheckBoxOperator.push();
        //new EventTool().waitNoEvent(1500);
        nbDialogOperator.ok();
        
    }
    
    public void testGenerateNewBeanInfo() {
        createContent();
        RepositoryTabOperator explorerOperator = new RepositoryTabOperator();
        Node repositoryRootNode = explorerOperator.getRootNode();
        Node patternsNode = new Node(repositoryRootNode, sampleDir+"|"+NAME_TEST_FILE+"|"+"class "+NAME_TEST_FILE+"|"+Bundle.getString("org.netbeans.modules.beans.Bundle", "Patterns"));
        patternsNode.select();
        patternsNode.performPopupActionNoBlock(Bundle.getString("org.netbeans.modules.beans.Bundle", "CTL_TITLE_GenerateBeanInfo"));
        new EventTool().waitNoEvent(DELAY);
        String dialogTitle = Bundle.getString("org.netbeans.modules.beans.beaninfo.Bundle", "CTL_TITLE_GenerateBeanInfo");
        NbDialogOperator nbDialogOperator = new NbDialogOperator(dialogTitle);
        //new EventTool().waitNoEvent(1500);
        nbDialogOperator.ok();
        //new EventTool().waitNoEvent(1500);
//        EditorOperator eo = new EditorOperator(NAME_TEST_FILE+"BeanInfo");
//        ref(eo.getText());
        writeResult(NAME_TEST_FILE+"BeanInfo");
        compareReferenceFiles();
    }
    
    
    public void testIncludeExclude() {
        createContent();
        RepositoryTabOperator explorerOperator = new RepositoryTabOperator();
        Node repositoryRootNode = explorerOperator.getRootNode();
        Node patternsNode = new Node(repositoryRootNode, sampleDir+"|"+NAME_TEST_FILE+"|"+"class "+NAME_TEST_FILE+"|"+Bundle.getString("org.netbeans.modules.beans.Bundle", "Patterns"));
        patternsNode.select();
        patternsNode.performPopupActionNoBlock(Bundle.getString("org.netbeans.modules.beans.Bundle", "CTL_TITLE_GenerateBeanInfo"));
        new EventTool().waitNoEvent(DELAY);
        String dialogTitle = Bundle.getString("org.netbeans.modules.beans.beaninfo.Bundle", "CTL_TITLE_GenerateBeanInfo");
        NbDialogOperator nbDialogOperator = new NbDialogOperator(dialogTitle);
        JTreeOperator jTreeOperator = new JTreeOperator(nbDialogOperator);
        Node node = new Node(jTreeOperator, Bundle.getString("org.netbeans.modules.beans.beaninfo.Bundle", "CTL_NODE_EventSets"));
        node.select();
        for (int i=0; i<(new Node(jTreeOperator, Bundle.getString("org.netbeans.modules.beans.beaninfo.Bundle", "CTL_NODE_EventSets")).getChildren().length); i++ ) {
            new Node(node,i).select();
            PropertySheetOperator propertySheetOperator = new PropertySheetOperator(nbDialogOperator);
            new Property(propertySheetOperator, Bundle.getString("org.netbeans.modules.beans.beaninfo.Bundle", "PROP_Bi_included")).setValue("False");
            //new EventTool().waitNoEvent(1000);
        }
        
        new Node(jTreeOperator, Bundle.getString("org.netbeans.modules.beans.beaninfo.Bundle", "CTL_NODE_Descriptor")).select();
        PropertySheetOperator propertySheetOperator = new PropertySheetOperator(nbDialogOperator);
        new Property(propertySheetOperator, Bundle.getString("org.netbeans.modules.beans.beaninfo.Bundle", "PROP_Bi_nullDescriptor")).setValue("False");
        
        nbDialogOperator.ok();
        
        //new EventTool().waitNoEvent(1500);
        explorerOperator = new RepositoryTabOperator();

        repositoryRootNode = explorerOperator.getRootNode();
        JavaNode javaNode = new JavaNode(repositoryRootNode, sampleDir+"|"+NAME_TEST_FILE+"BeanInfo");
        javaNode.select();
        javaNode.performPopupActionNoBlock(Bundle.getString("org.openide.actions.Bundle", "Open"));
        
        new EventTool().waitNoEvent(500);
        
//        EditorOperator eo = new EditorOperator(NAME_TEST_FILE+"BeanInfo");
//        ref(eo.getText());
        writeResult(NAME_TEST_FILE+"BeanInfo");
        compareReferenceFiles();
    }

    private void writeResult(String name) {
        new EventTool().waitNoEvent(1000);
        new EditorOperator(name);
        ref(Utilities.unify(Utilities.getAsString(name+".java")));
        new EventTool().waitNoEvent(500);
    }
    
    public void testRegenerateBeanInfo() {
        createContent();
        RepositoryTabOperator explorerOperator = new RepositoryTabOperator();
        Node repositoryRootNode = explorerOperator.getRootNode();
        Node patternsNode = new Node(repositoryRootNode, sampleDir+"|"+NAME_TEST_FILE+"|"+"class "+NAME_TEST_FILE+"|"+Bundle.getString("org.netbeans.modules.beans.Bundle", "Patterns"));
        patternsNode.select();
        patternsNode.performPopupActionNoBlock(Bundle.getString("org.netbeans.modules.beans.Bundle", "CTL_TITLE_GenerateBeanInfo"));
        new EventTool().waitNoEvent(DELAY);
        String dialogTitle = Bundle.getString("org.netbeans.modules.beans.beaninfo.Bundle", "CTL_TITLE_GenerateBeanInfo");
        NbDialogOperator nbDialogOperator = new NbDialogOperator(dialogTitle);
        JTreeOperator jTreeOperator = new JTreeOperator(nbDialogOperator);
        
        new Node(jTreeOperator, Bundle.getString("org.netbeans.modules.beans.beaninfo.Bundle", "CTL_NODE_Descriptor")).select();
        PropertySheetOperator propertySheetOperator = new PropertySheetOperator(nbDialogOperator);
        new Property(propertySheetOperator, Bundle.getString("org.netbeans.modules.beans.beaninfo.Bundle", "PROP_Bi_nullDescriptor")).setValue("False");
        
        nbDialogOperator.ok();
        //new EventTool().waitNoEvent(750);
        explorerOperator = new RepositoryTabOperator();
        repositoryRootNode = explorerOperator.getRootNode();
        JavaNode javaNode = new JavaNode(repositoryRootNode, sampleDir+"|"+NAME_TEST_FILE+"BeanInfo");
        javaNode.select();
        javaNode.open();
        new EventTool().waitNoEvent(100);
        try {
            EditorOperator eo = new EditorOperator(NAME_TEST_FILE+"BeanInfo");
            File workDir = getWorkDir();
            (new File(workDir,"testRegenerateBeanInfoInitial.ref")).createNewFile();
            PrintWriter out = new PrintWriter( new BufferedWriter( new FileWriter(workDir+File.separator+"testRegenerateBeanInfoInitial.ref")));
            out.print(Utilities.unify(eo.getText()));
            out.close();
        } catch(IOException exc) {
            exc.printStackTrace();
        }
        compareReferenceFiles("testRegenerateBeanInfoInitial.ref", "testRegenerateBeanInfoInitial.pass", "testRegenerateBeanInfoInitial.diff");
        Thread thread = new Thread( new java.lang.Runnable() {
            public void run() {
                System.out.println("T H R E A D");
                //new EventTool().waitNoEvent(1000);
                RepositoryTabOperator explorerOperator = new RepositoryTabOperator();
                Node repositoryRootNode = explorerOperator.getRootNode();
                Node patternsNode = new Node(repositoryRootNode, sampleDir+"|"+NAME_TEST_FILE+"|"+"class "+NAME_TEST_FILE+"|"+Bundle.getString("org.netbeans.modules.beans.Bundle", "Patterns")+"|"+NAME_NON_INDEX_PROPERTY);
                patternsNode.select();
                patternsNode.performPopupActionNoBlock(Bundle.getStringTrimmed("org.openide.actions.Bundle", "Delete"));
            }
        });
        thread.start();
        String confirmTitle = Bundle.getString("org.openide.explorer.Bundle", "MSG_ConfirmDeleteObjectTitle");
        new NbDialogOperator(confirmTitle).yes();
        //new EventTool().waitNoEvent(1500);
        String questionTitle = Bundle.getString("org.openide.Bundle", "NTF_QuestionTitle");
        nbDialogOperator =new NbDialogOperator(questionTitle);
        nbDialogOperator.yes();
        //new EventTool().waitNoEvent(2500);
        //
        explorerOperator = new RepositoryTabOperator();
        repositoryRootNode = explorerOperator.getRootNode();
        patternsNode = new Node(repositoryRootNode, sampleDir+"|"+NAME_TEST_FILE+"|"+"class "+NAME_TEST_FILE+"|"+Bundle.getString("org.netbeans.modules.beans.Bundle", "Patterns"));
        patternsNode.select();
        patternsNode.performPopupActionNoBlock(Bundle.getString("org.netbeans.modules.beans.Bundle", "CTL_TITLE_GenerateBeanInfo"));
        new EventTool().waitNoEvent(DELAY);
        dialogTitle = Bundle.getString("org.netbeans.modules.beans.beaninfo.Bundle", "CTL_TITLE_GenerateBeanInfo");
        nbDialogOperator = new NbDialogOperator(dialogTitle);
        jTreeOperator = new JTreeOperator(nbDialogOperator);

        new Node(jTreeOperator, Bundle.getString("org.netbeans.modules.beans.beaninfo.Bundle", "CTL_NODE_Descriptor")).select();
        propertySheetOperator = new PropertySheetOperator(nbDialogOperator);
        new Property(propertySheetOperator, Bundle.getString("org.netbeans.modules.beans.beaninfo.Bundle", "PROP_Bi_nullDescriptor")).setValue("False");
        
        nbDialogOperator.ok();
        //new EventTool().waitNoEvent(1500);
        explorerOperator = new RepositoryTabOperator();
        repositoryRootNode = explorerOperator.getRootNode();
        javaNode = new JavaNode(repositoryRootNode, sampleDir+"|"+NAME_TEST_FILE+"BeanInfo");
        javaNode.select();
        javaNode.performPopupActionNoBlock(Bundle.getString("org.openide.actions.Bundle", "Open"));
        try {
            EditorOperator eo = new EditorOperator(NAME_TEST_FILE+"BeanInfo");
            File workDir = getWorkDir();
            (new File(workDir,"testRegenerateBeanInfoModified.ref")).createNewFile();
            PrintWriter out = new PrintWriter( new BufferedWriter( new FileWriter(workDir+File.separator+"testRegenerateBeanInfoModified.ref")));
            out.print(Utilities.unify(eo.getText()));
            out.close();
        } catch(IOException exc) {
            exc.printStackTrace();
        }
        compareReferenceFiles("testRegenerateBeanInfoModified.ref", "testRegenerateBeanInfoModified.pass", "testRegenerateBeanInfoModified.diff");
    }
    
    public void testCheckNodes() {
        createContent();
        RepositoryTabOperator explorerOperator = new RepositoryTabOperator();
        Node repositoryRootNode = explorerOperator.getRootNode();
        Node patternsNode = new Node(repositoryRootNode, sampleDir+"|"+NAME_TEST_FILE+"|"+"class "+NAME_TEST_FILE+"|"+Bundle.getString("org.netbeans.modules.beans.Bundle", "Patterns"));
        patternsNode.select();
        patternsNode.performPopupActionNoBlock(Bundle.getString("org.netbeans.modules.beans.Bundle", "CTL_TITLE_GenerateBeanInfo"));
        new EventTool().waitNoEvent(DELAY);
        String dialogTitle = Bundle.getString("org.netbeans.modules.beans.beaninfo.Bundle", "CTL_TITLE_GenerateBeanInfo");
        NbDialogOperator nbDialogOperator = new NbDialogOperator(dialogTitle);
        System.err.println(nbDialogOperator);
        
        JTreeOperator jTreeOperator = new JTreeOperator(nbDialogOperator);
        System.err.println(jTreeOperator);
        Node node = new Node(jTreeOperator, Bundle.getString("org.netbeans.modules.beans.beaninfo.Bundle", "CTL_NODE_Descriptor") + '|' + "TestFile");
        node.select();
        //new EventTool().waitNoEvent(1000);
        node = new Node(jTreeOperator, Bundle.getString("org.netbeans.modules.beans.beaninfo.Bundle", "CTL_NODE_Properties") + '|' + "nonIndexProperty");
        node.select();
        //new EventTool().waitNoEvent(1000);
        node = new Node(jTreeOperator, Bundle.getString("org.netbeans.modules.beans.beaninfo.Bundle", "CTL_NODE_Properties") + '|' + "indexProperty");
        node.select();
        //new EventTool().waitNoEvent(1000);
        node = new Node(jTreeOperator, Bundle.getString("org.netbeans.modules.beans.beaninfo.Bundle", "CTL_NODE_EventSets") + '|' + "itemListener");
        node.select();
        new EventTool().waitNoEvent(100);
        node = new Node(jTreeOperator, Bundle.getString("org.netbeans.modules.beans.beaninfo.Bundle", "CTL_NODE_EventSets") + '|' + "focusListener");
        node.select();
        //new EventTool().waitNoEvent(1000);
        node = new Node(jTreeOperator, Bundle.getString("org.netbeans.modules.beans.beaninfo.Bundle", "CTL_NODE_EventSets") + '|' + "vetoableChangeListener");
        node.select();
        //new EventTool().waitNoEvent(1000);
        node = new Node(jTreeOperator, Bundle.getString("org.netbeans.modules.beans.beaninfo.Bundle", "CTL_NODE_EventSets") + '|' + "propertyChangeListener");
        node.select();
        //new EventTool().waitNoEvent(1000);
        node = new Node(jTreeOperator, Bundle.getString("org.netbeans.modules.beans.beaninfo.Bundle", "CTL_NODE_EventSets") + '|' + "actionListener");
        node.select();
        //new EventTool().waitNoEvent(1000);
        node = new Node(jTreeOperator, Bundle.getString("org.netbeans.modules.beans.beaninfo.Bundle", "CTL_NODE_Methods"));
        node.select();
        //new EventTool().waitNoEvent(1000);
        nbDialogOperator.close();
        
    }
    
    public void testBeanInfoNode() {
        createContent();
        RepositoryTabOperator explorerOperator = new RepositoryTabOperator();
        Node repositoryRootNode = explorerOperator.getRootNode();
        Node patternsNode = new Node(repositoryRootNode, sampleDir+"|"+NAME_TEST_FILE+"|"+"class "+NAME_TEST_FILE+"|"+Bundle.getString("org.netbeans.modules.beans.Bundle", "Patterns"));
        patternsNode.select();
        patternsNode.performPopupActionNoBlock(Bundle.getString("org.netbeans.modules.beans.Bundle", "CTL_TITLE_GenerateBeanInfo"));
        new EventTool().waitNoEvent(DELAY);
        String dialogTitle = Bundle.getString("org.netbeans.modules.beans.beaninfo.Bundle", "CTL_TITLE_GenerateBeanInfo");
        NbDialogOperator nbDialogOperator = new NbDialogOperator(dialogTitle);
        
        JTreeOperator jTreeOperator = new JTreeOperator(nbDialogOperator);
        //Node node = new Node(jTreeOperator, getTreePathHack(jTreeOperator,new TreePath(new Object[] {"BeanInfo"})));
        //node.select();
        jTreeOperator.setSelectionRow(0);
        //new EventTool().waitNoEvent(1000);
        PropertySheetOperator propertySheetOperator = new PropertySheetOperator(nbDialogOperator);
        new Property(propertySheetOperator, Bundle.getString("org.netbeans.modules.beans.beaninfo.Bundle", "PROP_Bi_defaultPropertyIndex")).setValue("123");
        //new EventTool().waitNoEvent(1000);
        new Property(propertySheetOperator, Bundle.getString("org.netbeans.modules.beans.beaninfo.Bundle", "PROP_Bi_defaultEventIndex")).setValue("456");
        //new EventTool().waitNoEvent(1000);
        
        new Node(jTreeOperator, Bundle.getString("org.netbeans.modules.beans.beaninfo.Bundle", "CTL_NODE_Descriptor")).select();
        propertySheetOperator = new PropertySheetOperator(nbDialogOperator);
        new Property(propertySheetOperator, Bundle.getString("org.netbeans.modules.beans.beaninfo.Bundle", "PROP_Bi_nullDescriptor")).setValue("False");
        nbDialogOperator.ok();
        explorerOperator = new RepositoryTabOperator();
        repositoryRootNode = explorerOperator.getRootNode();
        JavaNode javaNode = new JavaNode(repositoryRootNode, sampleDir+"|"+NAME_TEST_FILE+"BeanInfo");
        javaNode.select();
        javaNode.performPopupActionNoBlock(Bundle.getString("org.openide.actions.Bundle", "Open"));
//        EditorOperator eo = new EditorOperator(NAME_TEST_FILE+"BeanInfo");
//        ref(eo.getText());
        writeResult(NAME_TEST_FILE+"BeanInfo");
        compareReferenceFiles();
    }
    
    public void testPropertiesNode() {
        createContent();
        RepositoryTabOperator explorerOperator = new RepositoryTabOperator();
        Node repositoryRootNode = explorerOperator.getRootNode();
        Node patternsNode = new Node(repositoryRootNode, sampleDir+"|"+NAME_TEST_FILE+"|"+"class "+NAME_TEST_FILE+"|"+Bundle.getString("org.netbeans.modules.beans.Bundle", "Patterns"));
        patternsNode.select();
        patternsNode.performPopupActionNoBlock(Bundle.getString("org.netbeans.modules.beans.Bundle", "CTL_TITLE_GenerateBeanInfo"));
        new EventTool().waitNoEvent(DELAY);
        String dialogTitle = Bundle.getString("org.netbeans.modules.beans.beaninfo.Bundle", "CTL_TITLE_GenerateBeanInfo");
        NbDialogOperator nbDialogOperator = new NbDialogOperator(dialogTitle);
        
        JTreeOperator jTreeOperator = new JTreeOperator(nbDialogOperator);
        new Node(jTreeOperator, Bundle.getString("org.netbeans.modules.beans.beaninfo.Bundle", "CTL_NODE_Descriptor")).select();
        //new EventTool().waitNoEvent(1000);
        PropertySheetOperator propertySheetOperator = new PropertySheetOperator(nbDialogOperator);
        new Property(propertySheetOperator, Bundle.getString("org.netbeans.modules.beans.beaninfo.Bundle", "PROP_Bi_nullDescriptor")).setValue("True");
        
        new Node(jTreeOperator, Bundle.getString("org.openide.explorer.propertysheet.Bundle", "CTL_Properties")).select();
        //new EventTool().waitNoEvent(1000);
        propertySheetOperator = new PropertySheetOperator(nbDialogOperator);
        new Property(propertySheetOperator, Bundle.getString("org.netbeans.modules.beans.beaninfo.Bundle", "PROP_Bi_nullProperties")).setValue("True");
        
        new Node(jTreeOperator, Bundle.getString("org.netbeans.modules.beans.beaninfo.Bundle", "CTL_NODE_EventSets")).select();
        //new EventTool().waitNoEvent(1000);
        propertySheetOperator = new PropertySheetOperator(nbDialogOperator);
        new Property(propertySheetOperator, Bundle.getString("org.netbeans.modules.beans.beaninfo.Bundle", "PROP_Bi_nullEvents")).setValue("True");
        
        new Node(jTreeOperator, Bundle.getString("org.netbeans.modules.beans.beaninfo.Bundle", "CTL_NODE_Methods")).select();
        //new EventTool().waitNoEvent(1000);
        propertySheetOperator = new PropertySheetOperator(nbDialogOperator);
        new Property(propertySheetOperator, Bundle.getString("org.netbeans.modules.beans.beaninfo.Bundle", "PROP_Bi_nullProperties")).setValue("True");
        //new EventTool().waitNoEvent(1000);
        
        nbDialogOperator.ok();
        explorerOperator = new RepositoryTabOperator();
        repositoryRootNode = explorerOperator.getRootNode();
        JavaNode javaNode = new JavaNode(repositoryRootNode, sampleDir+"|"+NAME_TEST_FILE+"BeanInfo");
        javaNode.select();
        javaNode.performPopupActionNoBlock(Bundle.getString("org.openide.actions.Bundle", "Open"));
//        EditorOperator eo = new EditorOperator(NAME_TEST_FILE+"BeanInfo");
//        ref(eo.getText());
        writeResult(NAME_TEST_FILE+"BeanInfo");
        compareReferenceFiles();
    }
    
    
    public void testNodesDescription() {
        createContent();
        RepositoryTabOperator explorerOperator = new RepositoryTabOperator();
        Node repositoryRootNode = explorerOperator.getRootNode();
        Node patternsNode = new Node(repositoryRootNode, sampleDir+"|"+NAME_TEST_FILE+"|"+"class "+NAME_TEST_FILE+"|"+Bundle.getString("org.netbeans.modules.beans.Bundle", "Patterns"));
        patternsNode.select();
        patternsNode.performPopupActionNoBlock(Bundle.getString("org.netbeans.modules.beans.Bundle", "CTL_TITLE_GenerateBeanInfo"));
        new EventTool().waitNoEvent(DELAY);
        String dialogTitle = Bundle.getString("org.netbeans.modules.beans.beaninfo.Bundle", "CTL_TITLE_GenerateBeanInfo");
        NbDialogOperator nbDialogOperator = new NbDialogOperator(dialogTitle);
        
        JTreeOperator jTreeOperator = new JTreeOperator(nbDialogOperator);
        new Node(jTreeOperator, Bundle.getString("org.netbeans.modules.beans.beaninfo.Bundle", "CTL_NODE_Descriptor") + "|TestFile").select();
        //new EventTool().waitNoEvent(1000);
        PropertySheetOperator propertySheetOperator = new PropertySheetOperator(nbDialogOperator);
        new Property(propertySheetOperator, Bundle.getString("org.netbeans.modules.beans.beaninfo.Bundle", "PROP_Bi_name")).getValue();
        //new EventTool().waitNoEvent(750);
        new Property(propertySheetOperator, Bundle.getString("org.netbeans.modules.beans.beaninfo.Bundle", "PROP_Bi_expert")).setValue(new Property(propertySheetOperator, Bundle.getString("org.netbeans.modules.beans.beaninfo.Bundle", "PROP_Bi_expert")).getValue());
        //new EventTool().waitNoEvent(750);
        new Property(propertySheetOperator, Bundle.getString("org.netbeans.modules.beans.beaninfo.Bundle", "PROP_Bi_hidden")).setValue(new Property(propertySheetOperator, Bundle.getString("org.netbeans.modules.beans.beaninfo.Bundle", "PROP_Bi_hidden")).getValue());
        //new EventTool().waitNoEvent(750);
        new Property(propertySheetOperator, Bundle.getString("org.netbeans.modules.beans.beaninfo.Bundle", "PROP_Bi_preferred")).setValue(new Property(propertySheetOperator, Bundle.getString("org.netbeans.modules.beans.beaninfo.Bundle", "PROP_Bi_preferred")).getValue());
        //new EventTool().waitNoEvent(750);
//IT ISN'T POSSIBLE TO READ AND WRITE AGAIN THE SAME VALUE//        new TextFieldProperty(propertySheetTabOperator, Bundle.getString("org.netbeans.modules.beans.beaninfo.Bundle", "PROP_Bi_displayName")).setValue(new TextFieldProperty(propertySheetTabOperator, Bundle.getString("org.netbeans.modules.beans.beaninfo.Bundle", "PROP_Bi_displayName")).getValue());
        //new EventTool().waitNoEvent(750);
// //        new TextFieldProperty(propertySheetTabOperator, Bundle.getString("org.netbeans.modules.beans.beaninfo.Bundle", "PROP_Bi_shortDescription")).setValue(new TextFieldProperty(propertySheetTabOperator, Bundle.getString("org.netbeans.modules.beans.beaninfo.Bundle", "PROP_Bi_shortDescription")).getValue());
        //new EventTool().waitNoEvent(750);
        new Property(propertySheetOperator, Bundle.getString("org.netbeans.modules.beans.beaninfo.Bundle", "PROP_Bi_customizer")).getValue();
        new EventTool().waitNoEvent(400);
        
        jTreeOperator.setComparator(new DefaultStringComparator(true, true));
        new Node(jTreeOperator, Bundle.getString("org.netbeans.modules.beans.beaninfo.Bundle", "CTL_NODE_Properties") + "|indexProperty").select();
        
        //new EventTool().waitNoEvent(1000);
        propertySheetOperator = new PropertySheetOperator(nbDialogOperator);
        new Property(propertySheetOperator, Bundle.getString("org.netbeans.modules.beans.beaninfo.Bundle", "PROP_Bi_name")).getValue();
        //new EventTool().waitNoEvent(750);
        new Property(propertySheetOperator, Bundle.getString("org.netbeans.modules.beans.beaninfo.Bundle", "PROP_Bi_expert")).setValue(new Property(propertySheetOperator, Bundle.getString("org.netbeans.modules.beans.beaninfo.Bundle", "PROP_Bi_expert")).getValue());
        //new EventTool().waitNoEvent(750);
        new Property(propertySheetOperator, Bundle.getString("org.netbeans.modules.beans.beaninfo.Bundle", "PROP_Bi_hidden")).setValue(new Property(propertySheetOperator, Bundle.getString("org.netbeans.modules.beans.beaninfo.Bundle", "PROP_Bi_hidden")).getValue());
        //new EventTool().waitNoEvent(750);
        new Property(propertySheetOperator, Bundle.getString("org.netbeans.modules.beans.beaninfo.Bundle", "PROP_Bi_preferred")).setValue(new Property(propertySheetOperator, Bundle.getString("org.netbeans.modules.beans.beaninfo.Bundle", "PROP_Bi_preferred")).getValue());
        //new EventTool().waitNoEvent(750);
// //        new TextFieldProperty(propertySheetTabOperator, Bundle.getString("org.netbeans.modules.beans.beaninfo.Bundle", "PROP_Bi_shortDescription")).setValue(new TextFieldProperty(propertySheetTabOperator, Bundle.getString("org.netbeans.modules.beans.beaninfo.Bundle", "PROP_Bi_shortDescription")).getValue());
        //new EventTool().waitNoEvent(750);
        new Property(propertySheetOperator, Bundle.getString("org.netbeans.modules.beans.beaninfo.Bundle", "PROP_Bi_included")).setValue(new Property(propertySheetOperator, Bundle.getString("org.netbeans.modules.beans.beaninfo.Bundle", "PROP_Bi_included")).getValue());
        //new EventTool().waitNoEvent(750);

        new Property(propertySheetOperator, Bundle.getString("org.netbeans.modules.beans.beaninfo.Bundle", "PROP_Bi_bound")).setValue(new Property(propertySheetOperator, Bundle.getString("org.netbeans.modules.beans.beaninfo.Bundle", "PROP_Bi_bound")).getValue());
        //new EventTool().waitNoEvent(750);
        new Property(propertySheetOperator, Bundle.getString("org.netbeans.modules.beans.beaninfo.Bundle", "PROP_Bi_bound")).setValue(new Property(propertySheetOperator, Bundle.getString("org.netbeans.modules.beans.beaninfo.Bundle", "PROP_Bi_bound")).getValue());
        //new EventTool().waitNoEvent(750);
        new Property(propertySheetOperator, Bundle.getString("org.netbeans.modules.beans.beaninfo.Bundle", "PROP_Bi_mode")).setValue(new Property(propertySheetOperator, Bundle.getString("org.netbeans.modules.beans.beaninfo.Bundle", "PROP_Bi_mode")).getValue());
        //new EventTool().waitNoEvent(750);
// //        new TextFieldProperty(propertySheetTabOperator, Bundle.getString("org.netbeans.modules.beans.beaninfo.Bundle", "PROP_Bi_propertyEditorClass")).setValue(new TextFieldProperty(propertySheetTabOperator, Bundle.getString("org.netbeans.modules.beans.beaninfo.Bundle", "PROP_Bi_propertyEditorClass")).getValue());
        //new EventTool().waitNoEvent(750);
        new Property(propertySheetOperator, Bundle.getString("org.netbeans.modules.beans.beaninfo.Bundle", "PROP_Bi_niGetter")).setValue(new Property(propertySheetOperator, Bundle.getString("org.netbeans.modules.beans.beaninfo.Bundle", "PROP_Bi_niGetter")).getValue());
        //new EventTool().waitNoEvent(750);
        new Property(propertySheetOperator, Bundle.getString("org.netbeans.modules.beans.beaninfo.Bundle", "PROP_Bi_niSetter")).setValue(new Property(propertySheetOperator, Bundle.getString("org.netbeans.modules.beans.beaninfo.Bundle", "PROP_Bi_niSetter")).getValue());
        //new EventTool().waitNoEvent(750);
        
        new Node(jTreeOperator, Bundle.getString("org.netbeans.modules.beans.beaninfo.Bundle", "CTL_NODE_EventSets") + "|focusListener").select();
        //new EventTool().waitNoEvent(1000);
        propertySheetOperator = new PropertySheetOperator(nbDialogOperator);
        
        new Property(propertySheetOperator, Bundle.getString("org.netbeans.modules.beans.beaninfo.Bundle", "PROP_Bi_name")).getValue();
        //new EventTool().waitNoEvent(750);
        new Property(propertySheetOperator, Bundle.getString("org.netbeans.modules.beans.beaninfo.Bundle", "PROP_Bi_expert")).setValue(new Property(propertySheetOperator, Bundle.getString("org.netbeans.modules.beans.beaninfo.Bundle", "PROP_Bi_expert")).getValue());
        //new EventTool().waitNoEvent(750);
        new Property(propertySheetOperator, Bundle.getString("org.netbeans.modules.beans.beaninfo.Bundle", "PROP_Bi_hidden")).setValue(new Property(propertySheetOperator, Bundle.getString("org.netbeans.modules.beans.beaninfo.Bundle", "PROP_Bi_hidden")).getValue());
        //new EventTool().waitNoEvent(750);
        new Property(propertySheetOperator, Bundle.getString("org.netbeans.modules.beans.beaninfo.Bundle", "PROP_Bi_preferred")).setValue(new Property(propertySheetOperator, Bundle.getString("org.netbeans.modules.beans.beaninfo.Bundle", "PROP_Bi_preferred")).getValue());
        //new EventTool().waitNoEvent(750);
// //        new TextFieldProperty(propertySheetTabOperator, Bundle.getString("org.netbeans.modules.beans.beaninfo.Bundle", "PROP_Bi_displayName")).setValue(new TextFieldProperty(propertySheetTabOperator, Bundle.getString("org.netbeans.modules.beans.beaninfo.Bundle", "PROP_Bi_displayName")).getValue());
        //new EventTool().waitNoEvent(750);
// //        new TextFieldProperty(propertySheetTabOperator, Bundle.getString("org.netbeans.modules.beans.beaninfo.Bundle", "PROP_Bi_shortDescription")).setValue(new TextFieldProperty(propertySheetTabOperator, Bundle.getString("org.netbeans.modules.beans.beaninfo.Bundle", "PROP_Bi_shortDescription")).getValue());
        //new EventTool().waitNoEvent(750);
        new Property(propertySheetOperator, Bundle.getString("org.netbeans.modules.beans.beaninfo.Bundle", "PROP_Bi_included")).setValue(new Property(propertySheetOperator, Bundle.getString("org.netbeans.modules.beans.beaninfo.Bundle", "PROP_Bi_included")).getValue());
        //new EventTool().waitNoEvent(750);
        
        new Property(propertySheetOperator, Bundle.getString("org.netbeans.modules.beans.beaninfo.Bundle", "PROP_Bi_unicast")).getValue();
        //new EventTool().waitNoEvent(750);
        new Property(propertySheetOperator, Bundle.getString("org.netbeans.modules.beans.beaninfo.Bundle", "PROP_Bi_inDefaultEventSet")).setValue(new Property(propertySheetOperator, Bundle.getString("org.netbeans.modules.beans.beaninfo.Bundle", "PROP_Bi_inDefaultEventSet")).getValue());
        //new EventTool().waitNoEvent(750);

        nbDialogOperator.ok();

        EditorOperator eo = new EditorOperator(NAME_TEST_FILE+"BeanInfo");
    }

    public void testCheckBeanInfoCompilability() {
        createContent();
        RepositoryTabOperator explorerOperator = new RepositoryTabOperator();
        Node repositoryRootNode = explorerOperator.getRootNode();
        Node patternsNode = new Node(repositoryRootNode, sampleDir+"|"+NAME_TEST_FILE+"|"+"class "+NAME_TEST_FILE+"|"+Bundle.getString("org.netbeans.modules.beans.Bundle", "Patterns"));
        patternsNode.select();
        patternsNode.performPopupActionNoBlock(Bundle.getString("org.netbeans.modules.beans.Bundle", "CTL_TITLE_GenerateBeanInfo"));
        new EventTool().waitNoEvent(DELAY);
        String dialogTitle = Bundle.getString("org.netbeans.modules.beans.beaninfo.Bundle", "CTL_TITLE_GenerateBeanInfo");
        NbDialogOperator nbDialogOperator = new NbDialogOperator(dialogTitle);
        nbDialogOperator.ok();
        //new EventTool().waitNoEvent(1000);
        explorerOperator = new RepositoryTabOperator();
        repositoryRootNode = explorerOperator.getRootNode();
        JavaNode javaNode = new JavaNode(repositoryRootNode, sampleDir+"|"+NAME_TEST_FILE+"BeanInfo");
        javaNode.select();
        javaNode.compile();
        
        MainWindowOperator.getDefault().waitStatusText("Finished TestFileBeanInfo");
        assertNotNull("Generated BeanInfo is not compilable", Repository.getDefault().findResource("TestFileBeanInfo.class"));
    }
    
}
