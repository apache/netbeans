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


/////////////////////
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
