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
import org.netbeans.jellytools.ChooseTemplateStepOperator;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.MainWindowOperator;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.NewWizardOperator;
import org.netbeans.jellytools.OptionsOperator;
import org.netbeans.jellytools.RepositoryTabOperator;
import org.netbeans.jellytools.TargetLocationStepOperator;
import org.netbeans.jellytools.actions.DeleteAction;
import org.netbeans.jellytools.actions.NewTemplateAction;
import org.netbeans.jellytools.actions.PropertiesAction;
import org.netbeans.jellytools.nodes.FolderNode;
import org.netbeans.jellytools.nodes.JavaNode;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.properties.Property;
import org.netbeans.jellytools.properties.PropertySheetOperator;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.operators.JCheckBoxOperator;
import org.netbeans.jemmy.operators.JComboBoxOperator;
import org.netbeans.jemmy.operators.JRadioButtonOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;
import org.netbeans.jemmy.operators.Operator.DefaultStringComparator;
import org.netbeans.junit.NbTestSuite;

import org.openide.actions.SaveAllAction;
import org.openide.filesystems.FileObject;

import org.openide.filesystems.Repository;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;


public class ChangingOfBeanPropertyProperties  extends JellyTestCase {
    
    private static final String NAME_TEST_FILE          = "TestFile";
    private static final String NAME_INDEX_PROPERTY     = "indexProperty";
    private static final String NAME_NON_INDEX_PROPERTY = "nonIndexProperty";
    
    private static final String sampleDir = Utilities.findFileSystem("src").getDisplayName();
    
    /** Need to be defined because of JUnit */
    public ChangingOfBeanPropertyProperties(String name) {
        super(name);
    }
    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(new ChangingOfBeanPropertyProperties("testChangePropertyNameAndType"));
        suite.addTest(new ChangingOfBeanPropertyProperties("testChangeMode"));
        suite.addTest(new ChangingOfBeanPropertyProperties("testDeleteAnyPropertiesAndEvents"));
        suite.addTest(new ChangingOfBeanPropertyProperties("testChangeSourceCode"));
        suite.addTest(new ChangingOfBeanPropertyProperties("testChangeOfStyleOfDeclaredVariable"));
        return suite;
    }
    
    /** Use for execution inside IDE */
    public static void main(java.lang.String[] args) {
        // run whole suite
        TestRunner.run(suite());
        // run only selected test case
        //junit.textui.TestRunner.run(new ChangingOfBeanPropertyProperties("testChangeMode"));
    }
    
    /** setUp method  */
    public void setUp() {
        System.out.println("########  "+getName()+"  #######");
        if (!getName().equals("testChangeSourceCode") && !getName().equals("testDeleteAnyPropertiesAndEvents")) {
            
            FileObject testFile = Repository.getDefault().findResource("gui/data/" + NAME_TEST_FILE + ".java");
            FileObject destination = Repository.getDefault().findFileSystem(sampleDir.replace('\\', '/')).getRoot();
            
            try {
                DataObject.find(testFile).copy(DataFolder.findFolder(destination));
            } catch (IOException e) {
                fail(e);
            }
        }
        new PropertiesAction().perform();
    }
    
    /** tearDown method */
    public void tearDown() {
        ((SaveAllAction) SaveAllAction.findObject(SaveAllAction.class, true)).performAction();
        
        Utilities.delete(NAME_TEST_FILE + ".java");
    }
    
    /** - Create an empty class
     *  - Set Tools|Options|Editing|Beans Property|Style of Declared Variable = this.property_Value
     *  - add a new property
     *  - Set Tools|Options|Editing|Beans Property|Style of Declared Variable = _property_Value
     *  - add a new property
     */
    public void testChangeOfStyleOfDeclaredVariable() {
        MainWindowOperator mainWindowOper  = MainWindowOperator.getDefault();
        
        
        OptionsOperator optionsOperator = OptionsOperator.invoke();
        optionsOperator.selectOption(Bundle.getString("org.netbeans.core.Bundle", "UI/Services/Editing")+ "|" + Bundle.getString("org.netbeans.modules.beans.Bundle", "PROP_Option_Menu"));
        PropertySheetOperator propertySheetTabOperator = new PropertySheetOperator(optionsOperator);
        new Property(propertySheetTabOperator, Bundle.getString("org.netbeans.modules.beans.Bundle", "PROP_Option_Prop_Style")).setValue(Bundle.getString("org.netbeans.modules.beans.Bundle", "MSG_Option_Gen_This"));
        
        new EventTool().waitNoEvent(3000);
        
        RepositoryTabOperator explorerOperator = new RepositoryTabOperator();
        Node repositoryRootNode = explorerOperator.getRootNode();
        Node patternsNode = new Node(repositoryRootNode, sampleDir+"|"+NAME_TEST_FILE+"|"+"class "+NAME_TEST_FILE+"|"+Bundle.getString("org.netbeans.modules.beans.Bundle", "Patterns"));
        
        patternsNode.select();
        patternsNode.performPopupActionNoBlock(Bundle.getString("org.openide.src.nodes.Bundle", "LAB_Add")+"|"+Bundle.getString("org.netbeans.modules.beans.Bundle", "MENU_CREATE_PROPERTY"));
        String dialogTitle = Bundle.getString("org.netbeans.modules.beans.Bundle", "CTL_TITLE_NewProperty");
        NbDialogOperator nbDialogOperator = new NbDialogOperator(dialogTitle);
        
        JTextFieldOperator jTextFieldOperator = new JTextFieldOperator(nbDialogOperator, 0);
        jTextFieldOperator.typeText("firstName");
        JComboBoxOperator jComboBoxOperator = new JComboBoxOperator(nbDialogOperator, 0);
        jComboBoxOperator.typeText("int");
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
        new EventTool().waitNoEvent(2000);
        nbDialogOperator.ok();
        
        new Property(propertySheetTabOperator, Bundle.getString("org.netbeans.modules.beans.Bundle", "PROP_Option_Prop_Style")).setValue(Bundle.getString("org.netbeans.modules.beans.Bundle", "MSG_Option_Gen_Undescored"));
        new EventTool().waitNoEvent(3000);
        explorerOperator = new RepositoryTabOperator();
        repositoryRootNode = explorerOperator.getRootNode();
        patternsNode = new Node(repositoryRootNode, sampleDir+"|"+NAME_TEST_FILE+"|"+"class "+NAME_TEST_FILE+"|"+Bundle.getString("org.netbeans.modules.beans.Bundle", "Patterns"));
        
        patternsNode.select();
        patternsNode.performPopupActionNoBlock(Bundle.getString("org.openide.src.nodes.Bundle", "LAB_Add")+"|"+Bundle.getString("org.netbeans.modules.beans.Bundle", "MENU_CREATE_PROPERTY"));
        dialogTitle = Bundle.getString("org.netbeans.modules.beans.Bundle", "CTL_TITLE_NewProperty");
        nbDialogOperator = new NbDialogOperator(dialogTitle);
        
        jTextFieldOperator = new JTextFieldOperator(nbDialogOperator, 0);
        jTextFieldOperator.typeText("secondName");
        jComboBoxOperator = new JComboBoxOperator(nbDialogOperator, 0);
        jComboBoxOperator.typeText("String");
        jComboBoxOperator = new JComboBoxOperator(nbDialogOperator, 1);
        jComboBoxOperator.setSelectedItem(Bundle.getString("org.netbeans.modules.beans.Bundle", "LAB_ReadWriteMODE"));
        jCheckBoxOperator = new JCheckBoxOperator(nbDialogOperator, Bundle.getString("org.netbeans.modules.beans.Bundle","CTL_PropertyPanel_fieldCheckBox"));
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
        new EventTool().waitNoEvent(2000);
        nbDialogOperator.ok();
        optionsOperator.close();
        
        new JavaNode(repositoryRootNode, sampleDir + "|" + NAME_TEST_FILE).open();
        
        EditorOperator eo = new EditorOperator(NAME_TEST_FILE);
        new EventTool().waitNoEvent(500);
        ref(eo.getText());
        compareReferenceFiles();
        
        
    }
    
    /** - Create an empty class
     *  - Set Tools|Options|Editing|Beans Property|Style of Declared Variable = 0
     *  - add a new property with an initial value
     *  - change of property type a name
     */
    public void testChangePropertyNameAndType() {
        MainWindowOperator mainWindowOper  = MainWindowOperator.getDefault();
        
        
        OptionsOperator optionsOperator = OptionsOperator.invoke();
        optionsOperator.selectOption(Bundle.getString("org.netbeans.core.Bundle", "UI/Services/Editing")+ "|" + Bundle.getString("org.netbeans.modules.beans.Bundle", "PROP_Option_Menu"));
        PropertySheetOperator propertySheetTabOperator = new PropertySheetOperator(optionsOperator);
        new Property(propertySheetTabOperator, Bundle.getString("org.netbeans.modules.beans.Bundle", "PROP_Option_Prop_Style")).setValue("this.property_Value");
        
        new EventTool().waitNoEvent(3000);
        
        RepositoryTabOperator explorerOperator = new RepositoryTabOperator();
        Node repositoryRootNode = explorerOperator.getRootNode();
        Node patternsNode = new Node(repositoryRootNode, sampleDir+"|"+NAME_TEST_FILE+"|"+"class "+NAME_TEST_FILE+"|"+Bundle.getString("org.netbeans.modules.beans.Bundle", "Patterns"));
        
        patternsNode.select();
        patternsNode.performPopupActionNoBlock(Bundle.getString("org.openide.src.nodes.Bundle", "LAB_Add")+"|"+Bundle.getString("org.netbeans.modules.beans.Bundle", "MENU_CREATE_PROPERTY"));
        String dialogTitle = Bundle.getString("org.netbeans.modules.beans.Bundle", "CTL_TITLE_NewProperty");
        NbDialogOperator nbDialogOperator = new NbDialogOperator(dialogTitle);
        
        JTextFieldOperator jTextFieldOperator = new JTextFieldOperator(nbDialogOperator, 0);
        jTextFieldOperator.typeText("initialName");
        JComboBoxOperator jComboBoxOperator = new JComboBoxOperator(nbDialogOperator, 0);
        jComboBoxOperator.typeText("initialType");
        jComboBoxOperator = new JComboBoxOperator(nbDialogOperator, 1);
        jComboBoxOperator.setSelectedItem(Bundle.getString("org.netbeans.modules.beans.Bundle", "LAB_ReadWriteMODE"));
        JCheckBoxOperator jCheckBoxOperator = new JCheckBoxOperator(nbDialogOperator, Bundle.getString("org.netbeans.modules.beans.Bundle","CTL_PropertyPanel_fieldCheckBox"));
        jCheckBoxOperator.push();
        jCheckBoxOperator = new JCheckBoxOperator(nbDialogOperator, Bundle.getString("org.netbeans.modules.beans.Bundle", "CTL_PropertyPanel_setCheckBox"));
        jCheckBoxOperator.push();
        jCheckBoxOperator = new JCheckBoxOperator(nbDialogOperator, Bundle.getString("org.netbeans.modules.beans.Bundle", "CTL_PropertyPanel_returnCheckBox"));
        jCheckBoxOperator.push();
        new EventTool().waitNoEvent(2000);
        nbDialogOperator.ok();
        
        new Property(propertySheetTabOperator, Bundle.getString("org.netbeans.modules.beans.Bundle", "PROP_Option_Prop_Style")).setValue("_property_Value");
        new EventTool().waitNoEvent(3000);
        optionsOperator.close();
        explorerOperator = new RepositoryTabOperator();
        patternsNode = new Node(repositoryRootNode, sampleDir+"|"+NAME_TEST_FILE+"|"+"class "+NAME_TEST_FILE+"|"+Bundle.getString("org.netbeans.modules.beans.Bundle", "Patterns")+"|"+"initialName");
        patternsNode.select();

        propertySheetTabOperator = new PropertySheetOperator();
        answerYes();
        new Property(propertySheetTabOperator, Bundle.getString("org.netbeans.modules.beans.beaninfo.Bundle", "PROP_Bi_name")).setValue("requiredName");
        new EventTool().waitNoEvent(1500);
        
        new JavaNode(repositoryRootNode, sampleDir + "|" + NAME_TEST_FILE).open();

        EditorOperator eo = new EditorOperator(NAME_TEST_FILE);
        ref(eo.getText());
        compareReferenceFiles();
        
    }
    
    private void answerYes() {
        new Thread() {
            public void run () {
               String questionTitle = Bundle.getString("org.openide.Bundle", "NTF_QuestionTitle");
               NbDialogOperator nbDialogOperator =new NbDialogOperator(questionTitle);
               nbDialogOperator.yes();
            }
        }.start();
    }

               

                
            
    
    /** - Create an empty class
     *  - Set Tools|Options|Editing|Beans Property|Style of Declared Variable = this.property_Value
     *  - add a new property
     *  - Set Tools|Options|Editing|Beans Property|Style of Declared Variable = _property_Value
     *  - Add a new property
     *  - Change of the first property mode to Read Only
     *  - Change of the second property mode to Write Only
     */
    public void testChangeMode() {
        //
        MainWindowOperator mainWindowOper  = MainWindowOperator.getDefault();
        
        
        OptionsOperator optionsOperator = OptionsOperator.invoke();
        optionsOperator.selectOption(Bundle.getString("org.netbeans.core.Bundle", "UI/Services/Editing")+"|"+Bundle.getString("org.netbeans.modules.beans.Bundle", "PROP_Option_Menu"));
        PropertySheetOperator propertySheetTabOperator = new PropertySheetOperator(optionsOperator);
        new Property(propertySheetTabOperator, Bundle.getString("org.netbeans.modules.beans.Bundle", "PROP_Option_Prop_Style")).setValue("this.property_Value");
        
        new EventTool().waitNoEvent(3000);
        
        RepositoryTabOperator explorerOperator = new RepositoryTabOperator();
        Node repositoryRootNode = explorerOperator.getRootNode();
        Node patternsNode = new Node(repositoryRootNode, sampleDir+"|"+NAME_TEST_FILE+"|"+"class "+NAME_TEST_FILE+"|"+Bundle.getString("org.netbeans.modules.beans.Bundle", "Patterns"));
        
        patternsNode.select();
        patternsNode.performPopupActionNoBlock(Bundle.getString("org.openide.src.nodes.Bundle", "LAB_Add")+"|"+Bundle.getString("org.netbeans.modules.beans.Bundle", "MENU_CREATE_PROPERTY"));
        String dialogTitle = Bundle.getString("org.netbeans.modules.beans.Bundle", "CTL_TITLE_NewProperty");
        NbDialogOperator nbDialogOperator = new NbDialogOperator(dialogTitle);
        
        JTextFieldOperator jTextFieldOperator = new JTextFieldOperator(nbDialogOperator, 0);
        jTextFieldOperator.typeText("firstName");
        JComboBoxOperator jComboBoxOperator = new JComboBoxOperator(nbDialogOperator, 0);
        jComboBoxOperator.typeText("int");
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
        new EventTool().waitNoEvent(1000);
        nbDialogOperator.ok();
        
        new Property(propertySheetTabOperator, Bundle.getString("org.netbeans.modules.beans.Bundle", "PROP_Option_Prop_Style")).setValue("_property_Value");
        new EventTool().waitNoEvent(1000);
        optionsOperator.close();
        explorerOperator = new RepositoryTabOperator();
        repositoryRootNode = explorerOperator.getRootNode();
        patternsNode = new Node(repositoryRootNode, sampleDir+"|"+NAME_TEST_FILE+"|"+"class "+NAME_TEST_FILE+"|"+Bundle.getString("org.netbeans.modules.beans.Bundle", "Patterns"));
        
        patternsNode.select();
        patternsNode.performPopupActionNoBlock(Bundle.getString("org.openide.src.nodes.Bundle", "LAB_Add")+"|"+Bundle.getString("org.netbeans.modules.beans.Bundle", "MENU_CREATE_PROPERTY"));
        dialogTitle = Bundle.getString("org.netbeans.modules.beans.Bundle", "CTL_TITLE_NewProperty");
        nbDialogOperator = new NbDialogOperator(dialogTitle);
        
        jTextFieldOperator = new JTextFieldOperator(nbDialogOperator, 0);
        jTextFieldOperator.typeText("secondName");
        jComboBoxOperator = new JComboBoxOperator(nbDialogOperator, 0);
        jComboBoxOperator.typeText("String");
        jComboBoxOperator = new JComboBoxOperator(nbDialogOperator, 1);
        jComboBoxOperator.setSelectedItem(Bundle.getString("org.netbeans.modules.beans.Bundle", "LAB_ReadWriteMODE"));
        jCheckBoxOperator = new JCheckBoxOperator(nbDialogOperator, Bundle.getString("org.netbeans.modules.beans.Bundle","CTL_PropertyPanel_fieldCheckBox"));
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
        new EventTool().waitNoEvent(1000);
        nbDialogOperator.ok();

        explorerOperator = new RepositoryTabOperator();
        
        patternsNode = new Node(repositoryRootNode, sampleDir+"|"+NAME_TEST_FILE+"|"+"class "+NAME_TEST_FILE+"|"+Bundle.getString("org.netbeans.modules.beans.Bundle", "Patterns")+"|"+"firstName");
        patternsNode.select();
        new EventTool().waitNoEvent(1000);
        answerYes();
        propertySheetTabOperator = new PropertySheetOperator();
        //new ComboBoxProperty(propertySheetTabOperator, Bundle.getString("org.netbeans.modules.beans.Bundle", "PROP_mode")).setValue(Bundle.getString("org.netbeans.modules.beans.Bundle", "LAB_ReadOnlyMODE"));
        new Property(propertySheetTabOperator, Bundle.getString("org.netbeans.modules.beans.Bundle", "PROP_mode")).setValue(1);
        new EventTool().waitNoEvent(1000);
        
        patternsNode = new Node(repositoryRootNode, sampleDir+"|"+NAME_TEST_FILE+"|"+"class "+NAME_TEST_FILE+"|"+Bundle.getString("org.netbeans.modules.beans.Bundle", "Patterns")+"|"+"secondName");
        patternsNode.select();
        new EventTool().waitNoEvent(1000);
        answerYes();
        propertySheetTabOperator = new PropertySheetOperator();
        //new ComboBoxProperty(propertySheetTabOperator, Bundle.getString("org.netbeans.modules.beans.Bundle", "PROP_mode")).setValue(Bundle.getString("org.netbeans.modules.beans.Bundle", "LAB_WriteOnlyMODE"));
        new Property(propertySheetTabOperator, Bundle.getString("org.netbeans.modules.beans.Bundle", "PROP_mode")).setValue(2);
        new EventTool().waitNoEvent(1000);
        
        new JavaNode(repositoryRootNode, sampleDir + "|" + NAME_TEST_FILE).open();
        
        EditorOperator eo = new EditorOperator(NAME_TEST_FILE);
        ref(eo.getText());
        compareReferenceFiles();
        
    }
    
    public void testChangeSourceCode() {
        
        Node repositoryRootNode = RepositoryTabOperator.invoke().getRootNode();
        
        FolderNode examplesFolderNode = new FolderNode(repositoryRootNode, sampleDir); // NOI18N
        examplesFolderNode.select();
        DefaultStringComparator comparator = new DefaultStringComparator(true, true);
        new NewTemplateAction().perform();
        NewWizardOperator newWizardOper = new NewWizardOperator();
        ChooseTemplateStepOperator ctso = new ChooseTemplateStepOperator();
        String bean = Bundle.getString("org.netbeans.modules.beans.Bundle", "Templates/Beans") + "|" + Bundle.getString("org.netbeans.modules.beans.Bundle", "Templates/Beans/Bean.java");
        ctso.selectTemplate(bean);
        ctso.next();
        TargetLocationStepOperator tlso = new TargetLocationStepOperator();
        tlso.setName(NAME_TEST_FILE);
        tlso.tree().setComparator(comparator);
        tlso.selectLocation(sampleDir);
        tlso.finish();
        
        EditorOperator eo = new EditorOperator(NAME_TEST_FILE);
        eo.setCaretPosition(1,1);
        eo.insert("    private static final String PROP_MY_PROPERTY = \"MyProperty\";\n", 16, 1);
        new EventTool().waitNoEvent(500);
        
        eo.insert("    private String myProperty;\n", 19, 1);
        new EventTool().waitNoEvent(500);
        
        eo.insert("    public String getMyProperty() {\n", 38, 1);
        new EventTool().waitNoEvent(500);
        eo.insert("        return myProperty;\n", 39, 1);
        new EventTool().waitNoEvent(500);
        eo.insert("    }\n", 40, 1);
        new EventTool().waitNoEvent(500);
        eo.insert("\n", 41, 1);
        new EventTool().waitNoEvent(500);
        
        eo.insert("    public void setMyProperty(String value) {\n", 42, 1);
        new EventTool().waitNoEvent(500);
        eo.insert("        String oldValue = myProperty;\n", 43, 1);
        new EventTool().waitNoEvent(500);
        eo.insert("        myProperty = value;\n", 44, 1);
        new EventTool().waitNoEvent(500);
        eo.insert("        propertySupport.firePropertyChange(PROP_MY_PROPERTY, oldValue, myProperty);\n", 45, 1);
        new EventTool().waitNoEvent(500);
        eo.insert("    }\n", 46, 1);
        new EventTool().waitNoEvent(500);
        eo.insert("\n", 47, 1);
        new EventTool().waitNoEvent(500);
        
        RepositoryTabOperator explorerOperator = new RepositoryTabOperator();
        Node patternsNode = new Node(repositoryRootNode, sampleDir+"|"+NAME_TEST_FILE+"|"+"class "+NAME_TEST_FILE+"|"+Bundle.getString("org.netbeans.modules.beans.Bundle", "Patterns")+"|"+"myProperty");
        patternsNode.select();
        new EventTool().waitNoEvent(1000);
        PropertySheetOperator propertySheetTabOperator = new PropertySheetOperator();
        
        assertEquals("Estimated Field" ,"String myProperty",new Property(propertySheetTabOperator, Bundle.getString("org.netbeans.modules.beans.Bundle", "PROP_estimatedField")).getValue());
        assertEquals("Getter" ,"getMyProperty ()",new Property(propertySheetTabOperator, Bundle.getString("org.netbeans.modules.beans.Bundle", "PROP_getter")).getValue());
        assertEquals("Mode" ,Bundle.getString("org.netbeans.modules.beans.Bundle", "LAB_ReadWriteMODE") ,new Property(propertySheetTabOperator, Bundle.getString("org.netbeans.modules.beans.Bundle", "PROP_mode")).getValue());
        assertEquals("Name of Property","myProperty",new Property(propertySheetTabOperator, Bundle.getString("org.netbeans.modules.beans.Bundle", "PROP_name")).getValue());
        assertEquals("Setter","setMyProperty (String)",new Property(propertySheetTabOperator, Bundle.getString("org.netbeans.modules.beans.Bundle", "PROP_setter")).getValue());
        assertEquals("Type","String",new Property(propertySheetTabOperator, Bundle.getString("org.netbeans.modules.beans.Bundle", "PROP_type")).getValue());
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
        new EventTool().waitNoEvent(1500);
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
        new EventTool().waitNoEvent(1500);
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
        
        new EventTool().waitNoEvent(1500);
        
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
        
        new EventTool().waitNoEvent(1500);
        
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
        jCheckBoxOperator = new JCheckBoxOperator(nbDialogOperator, Bundle.getString("org.netbeans.modules.beans.Bundle","CTL_EventSetPanel_passEventCheckBox"));
        jCheckBoxOperator.push();
        new EventTool().waitNoEvent(1500);
        nbDialogOperator.ok();
        
    }
    
    public void testDeleteAnyPropertiesAndEvents() {
        Node repositoryRootNode = RepositoryTabOperator.invoke().getRootNode();

        FolderNode examplesFolderNode = new FolderNode(repositoryRootNode, sampleDir); // NOI18N
        examplesFolderNode.select();
        DefaultStringComparator comparator = new DefaultStringComparator(true, true);
        new NewTemplateAction().perform();
        NewWizardOperator newWizardOper = new NewWizardOperator();
        ChooseTemplateStepOperator ctso = new ChooseTemplateStepOperator();
        String bean = Bundle.getString("org.netbeans.modules.beans.Bundle", "Templates/Beans") + "|" + Bundle.getString("org.netbeans.modules.beans.Bundle", "Templates/Beans/Bean.java");
        ctso.selectTemplate(bean);
        ctso.next();
        TargetLocationStepOperator tlso = new TargetLocationStepOperator();
        tlso.setName(NAME_TEST_FILE);
        tlso.tree().setComparator(comparator);
        tlso.selectLocation(sampleDir);
        tlso.finish();
        
        EditorOperator eo = new EditorOperator(NAME_TEST_FILE);
        eo.select(1,6);
        new DeleteAction().performAPI(eo);
        new EventTool().waitNoEvent(1500);
        eo.select(3,6);
        
        new DeleteAction().performAPI(eo);
        new EventTool().waitNoEvent(1500);

        try {
            File workDir = getWorkDir();
            (new File(workDir,"testDeleteAnyPropertiesAndEventsInitial.ref")).createNewFile();
            PrintWriter out = new PrintWriter( new BufferedWriter( new FileWriter(workDir+File.separator+"testDeleteAnyPropertiesAndEventsInitial.ref")));
            out.print(eo.getText());
            out.close();
        } catch(IOException exc) {
            exc.printStackTrace();
        }
        compareReferenceFiles("testDeleteAnyPropertiesAndEventsInitial.ref", "testDeleteAnyPropertiesAndEventsInitial.pass", "testDeleteAnyPropertiesAndEventsInitial.diff");
        
        createContent();
        
        // Delete nonIndexProperty
        
        JavaNode patternsNode = new JavaNode(sampleDir+"|"+NAME_TEST_FILE+"|"+"class "+NAME_TEST_FILE+"|"+Bundle.getString("org.netbeans.modules.beans.Bundle", "Patterns")+"|"+NAME_NON_INDEX_PROPERTY);
        patternsNode.select();
        patternsNode.delete();
        
        String confirmTitle = Bundle.getString("org.openide.explorer.Bundle", "MSG_ConfirmDeleteObjectTitle");
        new NbDialogOperator(confirmTitle).yes();
        String questionTitle = Bundle.getString("org.openide.Bundle", "NTF_QuestionTitle");
        NbDialogOperator nbDialogOperator =new NbDialogOperator(questionTitle);
        nbDialogOperator.yes();
        
        patternsNode.waitNotPresent();
        // Delete indexProperty
        JavaNode patternsNode2 = new JavaNode(sampleDir+"|"+NAME_TEST_FILE+"|"+"class "+NAME_TEST_FILE+"|"+Bundle.getString("org.netbeans.modules.beans.Bundle", "Patterns")+"|"+NAME_INDEX_PROPERTY);
        patternsNode2.select();
        patternsNode2.delete();
 
        confirmTitle = Bundle.getString("org.openide.explorer.Bundle", "MSG_ConfirmDeleteObjectTitle");
        new NbDialogOperator(confirmTitle).yes();
        questionTitle = Bundle.getString("org.openide.Bundle", "NTF_QuestionTitle");
        nbDialogOperator =new NbDialogOperator(questionTitle);
        nbDialogOperator.yes();
        
        patternsNode2.waitNotPresent();

        // Delete action listener
        JavaNode patternsNode3 = new JavaNode(sampleDir+"|"+NAME_TEST_FILE+"|"+"class "+NAME_TEST_FILE+"|"+Bundle.getString("org.netbeans.modules.beans.Bundle", "Patterns")+"|"+"actionListener");
        patternsNode3.select();
        patternsNode3.delete();
        
        confirmTitle = Bundle.getString("org.openide.explorer.Bundle", "MSG_ConfirmDeleteObjectTitle");
        new NbDialogOperator(confirmTitle).yes();
        questionTitle = Bundle.getString("org.openide.Bundle", "NTF_QuestionTitle");
        nbDialogOperator =new NbDialogOperator(questionTitle);
        nbDialogOperator.yes();

        patternsNode3.waitNotPresent();

        // Delete focus listener
        JavaNode patternsNode4 = new JavaNode(sampleDir+"|"+NAME_TEST_FILE+"|"+"class "+NAME_TEST_FILE+"|"+Bundle.getString("org.netbeans.modules.beans.Bundle", "Patterns")+"|"+"focusListener");
        patternsNode4.select();
        patternsNode4.delete();

        confirmTitle = Bundle.getString("org.openide.explorer.Bundle", "MSG_ConfirmDeleteObjectTitle");
        new NbDialogOperator(confirmTitle).yes();
        questionTitle = Bundle.getString("org.openide.Bundle", "NTF_QuestionTitle");
        nbDialogOperator =new NbDialogOperator(questionTitle);
        nbDialogOperator.yes();

        patternsNode4.waitNotPresent();
        new EventTool().waitNoEvent(1500);
        try {
            File workDir = getWorkDir();
            (new File(workDir,"testDeleteAnyPropertiesAndEventsModified.ref")).createNewFile();
            PrintWriter out = new PrintWriter( new BufferedWriter( new FileWriter(workDir+File.separator+"testDeleteAnyPropertiesAndEventsModified.ref")));
            out.print(eo.getText());
            out.close();
        } catch(IOException exc) {
            exc.printStackTrace();
        }
        compareReferenceFiles("testDeleteAnyPropertiesAndEventsModified.ref", "testDeleteAnyPropertiesAndEventsModified.pass", "testDeleteAnyPropertiesAndEventsModified.diff");
        
    }
    
}
