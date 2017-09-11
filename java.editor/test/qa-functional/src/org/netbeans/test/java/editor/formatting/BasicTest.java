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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

