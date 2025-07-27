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

import java.io.File;
import junit.textui.TestRunner;
import org.netbeans.jellytools.*;
import org.netbeans.jellytools.actions.NewTemplateAction;
import org.netbeans.jellytools.actions.PropertiesAction;
import org.netbeans.jellytools.nodes.FolderNode;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.operators.Operator.DefaultStringComparator;

import org.netbeans.junit.NbTestSuite;
import org.openide.actions.SaveAllAction;


public class BeansTemplates extends JellyTestCase {
    
    private static final String NAME_JAVA_BEAN          = "MyBean";
    private static final String NAME_BEAN_INFO          = "MyBeanInfo";
    private static final String NAME_BEAN_INFO_NO_ICON  = "MyBeanInfoNoIcon";
    private static final String NAME_CUSTOMIZER         = "MyCustomizer";
    private static final String NAME_PROPERTY_EDITOR    = "MyPropertyEditor";
    
    private static final String sampleDir = Utilities.findFileSystem("src").getDisplayName();
    
    /** Need to be defined because of JUnit */
    public BeansTemplates(String name) {
        super(name);
    }
    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(new BeansTemplates("testJavaBean"));
        suite.addTest(new BeansTemplates("testBeanInfo"));
        suite.addTest(new BeansTemplates("testBeanInfoNoIcon"));
        suite.addTest(new BeansTemplates("testCustomizer"));
        suite.addTest(new BeansTemplates("testPropertyEditor"));
        return suite;
    }
    
    /** Use for execution inside IDE */
    public static void main(java.lang.String[] args) {
        // run whole suite
        TestRunner.run(suite());
        // run only selected test case
        //junit.textui.TestRunner.run(new BeansTemplates("testJavaBean"));
    }
    
    public void setUp() {
        System.out.println("########  "+getName()+"  #######");
        new PropertiesAction().perform();
    }
    
    public void tearDown() {
        ((SaveAllAction) SaveAllAction.findObject(SaveAllAction.class, true)).performAction();
        
        Utilities.delete(NAME_JAVA_BEAN + ".java");
        Utilities.delete(NAME_BEAN_INFO + ".java");
        Utilities.delete(NAME_BEAN_INFO_NO_ICON + ".java");
        Utilities.delete(NAME_CUSTOMIZER + ".java");
        Utilities.delete(NAME_PROPERTY_EDITOR + ".java");
    }
    
    public void testJavaBean() {
        RepositoryTabOperator explorerOperator = new RepositoryTabOperator();
        Node repositoryRootNode = new RepositoryTabOperator().getRootNode();
        FolderNode examplesFolderNode = new FolderNode(repositoryRootNode.tree(), sampleDir); // NOI18N
        examplesFolderNode.select();
        DefaultStringComparator comparator = new DefaultStringComparator(true, true);
        new NewTemplateAction().perform();
        NewWizardOperator newWizardOper = new NewWizardOperator();
        ChooseTemplateStepOperator ctso = new ChooseTemplateStepOperator();
        ctso.setComparator(comparator);
        String bean = Bundle.getString("org.netbeans.modules.beans.Bundle", "Templates/Beans") + "|" + Bundle.getString("org.netbeans.modules.beans.Bundle", "Templates/Beans/Bean.java");
        new EventTool().waitNoEvent(1000);
        ctso.selectTemplate(bean);
        ctso.next();
        TargetLocationStepOperator tlso = new TargetLocationStepOperator();
        tlso.setName(NAME_JAVA_BEAN);
        tlso.tree().setComparator(comparator);
        tlso.selectLocation(sampleDir);
        tlso.finish();
        new EventTool().waitNoEvent(10000);
        
        writeResult(NAME_JAVA_BEAN);
        compareReferenceFiles();
    }
    
    private void writeResult(String name) {
        new EventTool().waitNoEvent(1000);
        new EditorOperator(name);
        ref(Utilities.unify(Utilities.getAsString(name+".java")));
        new EventTool().waitNoEvent(500);
    }
    
    public void testBeanInfo() {
        RepositoryTabOperator explorerOperator = new RepositoryTabOperator();
        Node repositoryRootNode = new RepositoryTabOperator().getRootNode();
        FolderNode examplesFolderNode = new FolderNode(repositoryRootNode.tree(), sampleDir); // NOI18N
        examplesFolderNode.select();
        DefaultStringComparator comparator = new DefaultStringComparator(true, true);
        new NewTemplateAction().perform();
        NewWizardOperator newWizardOper = new NewWizardOperator();
        ChooseTemplateStepOperator ctso = new ChooseTemplateStepOperator();
        String bean = Bundle.getString("org.netbeans.modules.beans.Bundle", "Templates/Beans") + "|" + Bundle.getString("org.netbeans.modules.beans.Bundle","Templates/Beans/BeanInfo.java");
        new EventTool().waitNoEvent(1000);
        ctso.selectTemplate(bean);
        ctso.next();
        TargetLocationStepOperator tlso = new TargetLocationStepOperator();
        tlso.setName(NAME_BEAN_INFO);
        tlso.tree().setComparator(comparator);
        tlso.selectLocation(sampleDir);
        tlso.finish();
        
        writeResult(NAME_BEAN_INFO);
        compareReferenceFiles();
    }
    
    public void testBeanInfoNoIcon() {
        RepositoryTabOperator explorerOperator = new RepositoryTabOperator();
        Node repositoryRootNode = new RepositoryTabOperator().getRootNode();
        FolderNode examplesFolderNode = new FolderNode(repositoryRootNode.tree(), sampleDir); // NOI18N
        examplesFolderNode.select();
        DefaultStringComparator comparator = new DefaultStringComparator(true, true);
        new NewTemplateAction().perform();
        NewWizardOperator newWizardOper = new NewWizardOperator();
        ChooseTemplateStepOperator ctso = new ChooseTemplateStepOperator();
        String bean = Bundle.getString("org.netbeans.modules.beans.Bundle", "Templates/Beans") + "|" + Bundle.getString("org.netbeans.modules.beans.Bundle","Templates/Beans/BeanInfoNoIcon.java");
        new EventTool().waitNoEvent(1000);
        ctso.selectTemplate(bean);
        ctso.next();
        TargetLocationStepOperator tlso = new TargetLocationStepOperator();
        tlso.setName(NAME_BEAN_INFO_NO_ICON);
        tlso.tree().setComparator(comparator);
        tlso.selectLocation(sampleDir);
        tlso.finish();
        
        writeResult(NAME_BEAN_INFO_NO_ICON);
        compareReferenceFiles();
    }
    
    public void testCustomizer() {
        MainWindowOperator mainWindowOper  = MainWindowOperator.getDefault();
//        mainWindowOper.switchToGUIEditingWorkspace();
        RepositoryTabOperator explorerOperator = new RepositoryTabOperator();
        Node repositoryRootNode = new RepositoryTabOperator().getRootNode();
        FolderNode examplesFolderNode = new FolderNode(repositoryRootNode.tree(), sampleDir); // NOI18N
        examplesFolderNode.select();
        DefaultStringComparator comparator = new DefaultStringComparator(true, true);
        new NewTemplateAction().perform();
        NewWizardOperator newWizardOper = new NewWizardOperator();
        ChooseTemplateStepOperator ctso = new ChooseTemplateStepOperator();
        String bean = Bundle.getString("org.netbeans.modules.beans.Bundle", "Templates/Beans") + "|" + Bundle.getString("org.netbeans.modules.beans.Bundle","Templates/Beans/Customizer.java");
        new EventTool().waitNoEvent(1000);
        ctso.selectTemplate(bean);
        ctso.next();
        TargetLocationStepOperator tlso = new TargetLocationStepOperator();
        tlso.setName(NAME_CUSTOMIZER);
        tlso.tree().setComparator(comparator);
        tlso.selectLocation(sampleDir);
        tlso.finish();
        
        writeResult(NAME_CUSTOMIZER);
        compareReferenceFiles();
    }
    
    public void testPropertyEditor() {
        RepositoryTabOperator explorerOperator = new RepositoryTabOperator();
        Node repositoryRootNode = new RepositoryTabOperator().getRootNode();
        FolderNode examplesFolderNode = new FolderNode(repositoryRootNode.tree(), sampleDir); // NOI18N
        examplesFolderNode.select();
        DefaultStringComparator comparator = new DefaultStringComparator(true, true);
        new NewTemplateAction().perform();
        NewWizardOperator newWizardOper = new NewWizardOperator();
        ChooseTemplateStepOperator ctso = new ChooseTemplateStepOperator();
        String bean = Bundle.getString("org.netbeans.modules.beans.Bundle", "Templates/Beans") + "|" + Bundle.getString("org.netbeans.modules.beans.Bundle","Templates/Beans/PropertyEditor.java");
        new EventTool().waitNoEvent(1000);
        ctso.selectTemplate(bean);
        ctso.next();
        TargetLocationStepOperator tlso = new TargetLocationStepOperator();
        tlso.setName(NAME_PROPERTY_EDITOR);
        tlso.tree().setComparator(comparator);
        tlso.selectLocation(sampleDir);
        tlso.finish();

        writeResult(NAME_PROPERTY_EDITOR);
        compareReferenceFiles();
    }
    
}
