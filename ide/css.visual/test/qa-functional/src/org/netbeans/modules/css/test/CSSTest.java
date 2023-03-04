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
package org.netbeans.modules.css.test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javax.swing.JComboBox;
import javax.swing.JList;
import junit.framework.Test;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.modules.j2ee.J2eeTestCase;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.JemmyException;
import org.netbeans.jemmy.operators.JComboBoxOperator;
import org.netbeans.jemmy.operators.JListOperator;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.junit.NbTestSuite;


/**
 *
 * @author Jindrich Sedek
 */
public class CSSTest extends J2eeTestCase {
    protected static final String newFileName = "newFileName";
    protected static final int rootRuleLineNumber = 15;
    protected static final String projectName = "CSSTestProject";
    
    /** Creates new CSS Test */
    public CSSTest(String testName) {
        super(testName);
    }
    
    @Override
    public void setUp() throws Exception{
        super.setUp();
        System.out.println("running" + this.getName());
    }

    @Override
    protected void tearDown() throws Exception {
        EditorOperator.closeDiscardAll();
        super.tearDown();
    }

    protected void waitUpdate(){
        try{
            Thread.sleep(1000);
        }catch (InterruptedException exc){
            throw new JemmyException("INTERUPTION", exc);
        }
    }
    
    protected EditorOperator openFile(String fileName){
        Node rootNode = new ProjectsTabOperator().getProjectRootNode(projectName);
        Node node = new Node(rootNode,"Web Pages|css|"+fileName);
        node.select();
        node.performPopupAction("Open");
        return new EditorOperator(fileName);
    }
    
    protected String getRootRuleText(){
        waitUpdate();
        String content = new EditorOperator(newFileName).getText();
        String root = content.substring(content.indexOf("root"));
        String rule = root.substring(root.indexOf('{'), root.indexOf('}'));
        return rule;
    }
    
    protected List<String> getItems(JComboBoxOperator boxOperator){
        JComboBox box = (JComboBox) boxOperator.getSource();
        int boxSize = box.getItemCount();
        List<String> result = new ArrayList<String>(boxSize);
        for(int i = 0;i < boxSize; i++){
            result.add(box.getModel().getElementAt(i).toString());
        }
        return result;
    }
    
    protected List<String> getItems(JListOperator listOperator){
        JList jList = (JList) listOperator.getSource();
        int listOperatorSize = getSize(listOperator);
        List<String> result = new ArrayList<String>(listOperatorSize);
        for (int i=0; i <listOperatorSize ;i++){
            result.add(jList.getModel().getElementAt(i).toString());
        }
        return result;
    }
    
    protected int getSize(JListOperator listOperator){
        return ((JList)listOperator.getSource()).getModel().getSize();
    }
    
    protected int getSize(JComboBoxOperator listOperator){
        return ((JComboBox)listOperator.getSource()).getModel().getSize();
    }
    
    protected void checkAtrribute(String attributeName, JComboBoxOperator operator) {
        checkAtrribute(attributeName,operator, false);
    }
    
    protected void checkAtrribute(String attributeName, JComboBoxOperator operator, boolean ignoreLastItem) {
        openFile(newFileName);
        int size = getSize(operator);
        assertFalse("SOME ITEMS", size == 0);
        //--------INSERT ONCE--------//
        if (ignoreLastItem) --size;
        int order = new Random().nextInt(size-1)+1;
        operator.selectItem(order);
        waitUpdate();
        String selected = getItems(operator).get(order);
        //String selected = operator.getSelectedItem().toString();
        assertTrue("INSERTING", getRootRuleText().contains(attributeName + ": " + selected));
        //--------  UPDATE   --------//
        order = new Random().nextInt(size-1)+1;
        operator.selectItem(order);
        waitUpdate();
        selected = getItems(operator).get(order);
        //selected = operator.getSelectedItem().toString();
        assertTrue("UPDATING", getRootRuleText().contains(attributeName + ": "+selected));
        //-------- REMOVE -----------//
        operator.selectItem(0);//<NOT SET>
        waitUpdate();
        assertFalse("REMOVING", getRootRuleText().contains(attributeName));
    }

    public static Test suite() {
        NbModuleSuite.Configuration conf = NbModuleSuite.createConfiguration(CSSTest.class);
        addServerTests(conf, new String[0]);//register server
        conf = conf.enableModules(".*").clusters(".*");
        return NbModuleSuite.create(conf.addTest(SuiteCreator.class));
    }

    public static final class SuiteCreator extends NbTestSuite {

        public SuiteCreator() {
            super();
            addTest(new TestBasic("testNewCSS"));
            addTest(new TestBasic("testAddRule"));
//            addTest(new TestBasic("testNavigator"));
            addTest(new TestFontSettings("testSetFontFamily"));
            addTest(new TestFontSettings("testChangeFontFamily"));
            addTest(new TestFontSettings("testChangeFontSize"));
            addTest(new TestFontSettings("testChangeFontWeight"));
            addTest(new TestFontSettings("testChangeFontStyle"));
            addTest(new TestFontSettings("testChangeFontVariant"));
            addTest(new TestFontSettings("testDecoration"));
            addTest(new TestFontSettings("testChangeFontColor"));
            addTest(new TestIssues("test105562"));
//            addTest(new TestIssues("test105568"));
            addTest(new TestBackgroundSettings("testTile"));
            addTest(new TestBackgroundSettings("testScroll"));
            addTest(new TestBackgroundSettings("testHPosition"));
        }
    }
}
