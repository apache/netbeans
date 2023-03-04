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

package org.netbeans.test.java.editor.formatting;


import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import junit.framework.Test;
import junit.textui.TestRunner;
import org.netbeans.test.java.editor.lib.EditorTestCase;
import org.netbeans.test.java.editor.lib.LineDiff;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.MainWindowOperator;
import org.netbeans.jellytools.actions.Action;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.junit.NbTestSuite;


/**
 *
 * @author jp159440
 */
public class BasicTest extends EditorTestCase{
    
    private boolean generateGoldenFiles = false;
    
    private String curPackage;
    
    private String testClass;
    
    protected EditorOperator oper;
    
    /** Creates a new instance of BasicTest */
    public BasicTest(String name) {
        super(name);
        curPackage = getClass().getPackage().getName();
        
    }
        
    public File getGoldenFile() {
        String fileName = "goldenfiles/"+curPackage.replace('.', '/')+ "/" + testClass + ".pass";
        File f = new java.io.File(getDataDir(),fileName);
        if(!f.exists()) fail("Golden file "+f.getAbsolutePath()+ " does not exist");
        return f;
    }
    
    public File getNewGoldenFile() {
        String fileName = "qa-functional/data/goldenfiles/"+curPackage.replace('.', '/')+ "/" + testClass + ".pass";
        File f = new File(getDataDir().getParentFile().getParentFile().getParentFile(),fileName);
        return f;
    }
    
    public void compareGoldenFile() throws IOException {
        File fGolden = null;
        if(!generateGoldenFiles) {
            fGolden = getGoldenFile();
        } else {
            fGolden = getNewGoldenFile();
        }
        String refFileName = getName()+".ref";
        String diffFileName = getName()+".diff";
        File fRef = new File(getWorkDir(),refFileName);
        FileWriter fw = new FileWriter(fRef);
        fw.write(oper.getText());
        fw.close();
        LineDiff diff = new LineDiff(false);
        if(!generateGoldenFiles) {
            File fDiff = new File(getWorkDir(),diffFileName);
            if(diff.diff(fGolden, fRef, fDiff)) fail("Golden files differ");
        } else {
            FileWriter fwgolden = new FileWriter(fGolden);
            fwgolden.write(oper.getText());
            fwgolden.close();
            fail("Golden file generated");
        }
    }
    
    protected void setUp() throws Exception {
        super.setUp();
        openDefaultProject();
        testClass = getName();
        System.out.println(testClass );
        System.out.println(curPackage);
        openSourceFile(curPackage, testClass);
        oper =  new EditorOperator(testClass);
        oper.txtEditorPane().setVerification(false);
    }
    
    protected void tearDown() throws Exception {
        compareGoldenFile();
        super.tearDown();
    }
    
    public void testBasicIndentation() {
        oper.setCaretPosition(20,1);
        oper.pushEndKey();
        oper.pushKey(KeyEvent.VK_ENTER);
        oper.pushKey(KeyEvent.VK_ENTER);
        oper.txtEditorPane().typeText("public void method(boolean cond1");
        oper.pushEndKey();
        oper.txtEditorPane().typeText("{");
        oper.pushKey(KeyEvent.VK_ENTER);
        oper.txtEditorPane().typeText("if(cond1");
        oper.pushEndKey();
        oper.txtEditorPane().typeText("sout");
        oper.pushTabKey();
        oper.txtEditorPane().typeText("Hello");        
        oper.pushEndKey();
    }

    private void doReformat() {
        MainWindowOperator.getDefault().menuBar().pushMenu("Source|Format", "|");
    }
    
    private void end() {
        oper.pushEndKey();
    }
    
    private void enter() {
        oper.pushKey(KeyEvent.VK_ENTER);
    }
    
    private void type(String text) {
        oper.txtEditorPane().typeText(text);
    }
    
    
    public void testAdvancedIndentation() {
        oper.setCaretPosition(20,1);
        end();
        enter();
        enter();
        type("public void method(");
        end();
        type(" {");
        enter();
        type("while(true");
        end();
        type(" {");
        enter();
        type("if(true");
        end();
        enter();
        type("if(false");
        end();
        type(" {");
        enter();
        type("int i = ");
        enter();
        type("1;");
    }
    
    public void testReformat() {
        doReformat();
    }
    
    /**
     * Annotations, anonymous classes, inner classes formatting test
     * testReformat2.java, testReformat.pass
     */ 
    public void testReformat2() {
        doReformat();
    }
    
    public void testReformatAnnotation() {
        doReformat();
    }
    
    public void testReformatIncompleteStatement() {
        doReformat();
    }
    
    public void testReformatIncompleteStatement2() {
        doReformat();
    }
    
    public static void main(String[] args) {
        TestRunner.run(new NbTestSuite(BasicTest.class));
    }
    
    public static Test suite() {
        return NbModuleSuite.create(
                NbModuleSuite.createConfiguration(BasicTest.class).enableModules(".*").clusters(".*"));
    }
    
}

