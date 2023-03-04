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

package org.netbeans.test.java.hints;

import java.awt.Container;
import junit.framework.Test;
import junit.textui.TestRunner;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.OptionsOperator;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.operators.ContainerOperator;
import org.netbeans.jemmy.operators.JCheckBoxOperator;
import org.netbeans.jemmy.operators.JComboBoxOperator;
import org.netbeans.jemmy.operators.JComponentOperator;
import org.netbeans.jemmy.operators.JSplitPaneOperator;
import org.netbeans.jemmy.operators.JTabbedPaneOperator;
import org.netbeans.jemmy.operators.JTreeOperator;
import org.netbeans.junit.NbModuleSuite;

/**
 *
 * @author jp159440
 */
public class SurroundTest extends HintsTestCase {

    public SurroundTest(String name) {
        super(name);
    }
    
    public void testBlock() {
        String file = "Surround";
	openSourceFile("org.netbeans.test.java.hints.HintsTest",file);
	editor = new EditorOperator(file);
	editor.setCaretPosition(52,1);
	String pattern = ".*"+
        "try \\{.*"+
        "    System.out.println\\(\"line1\"\\);.*"+
        "    URL u = new URL\\(\"a\"\\);.*"+
        "    System.out.println\\(\"line2\"\\);.*"+
        "\\} catch \\(MalformedURLException ex\\) \\{.*"+
        "    Logger.getLogger\\(Surround.class.getName\\(\\)\\).log\\(Level.SEVERE, null, ex\\);.*"+
        "\\}.*";
	useHint("Surround Block",new String[]{"Add throws","Surround Block","Surround Statement"},pattern);
    }
    
    public void testBlockFinally() {
        String file = "Surround";
	openSourceFile("org.netbeans.test.java.hints.HintsTest",file);
	editor = new EditorOperator(file);
	editor.setCaretPosition(57,1);
	String pattern = ".*"+
        "FileReader fr = null;.*"+
        "try \\{.*"+
        "    fr = new FileReader\\(\"file\"\\);.*"+
        "\\} catch \\(FileNotFoundException ex\\) \\{.*"+
        "    Logger.getLogger\\(Surround.class.getName\\(\\)\\).log\\(Level.SEVERE, null, ex\\);.*"+
        "\\} finally \\{.*"+
        "    try \\{.*"+
        "        fr.close\\(\\);.*"+
        "    \\} catch \\(IOException ex\\) \\{.*"+
        "        Logger.getLogger\\(Surround.class.getName\\(\\)\\).log\\(Level.SEVERE, null, ex\\);.*"+
        "    \\}.*"+
        "\\}.*";
	useHint("Surround Block",new String[]{"Add throws","Surround Block","Surround Statement"},pattern);        
    }
    
    public void testAddCatch() {
        String file = "Surround";
	openSourceFile("org.netbeans.test.java.hints.HintsTest",file);
	editor = new EditorOperator(file);
	editor.setCaretPosition(62,1);
	String pattern = ".*"+
        "try \\{.*"+
        "    new FileReader\\(\"b\"\\);.*"+
        "    new URL\\(\"c\"\\);.*"+
        "\\} catch \\(MalformedURLException ex\\) \\{.*"+
        "    Logger.getLogger\\(Surround.class.getName\\(\\)\\).log\\(Level.SEVERE, null, ex\\);.*"+
        "\\} catch \\(FileNotFoundException exception\\) \\{.*"+
        ".*"+    
        "\\}.*";
	useHint("Surround Block",new String[]{"Add throws","Surround Block","Surround Statement"},pattern);        
    }
    
    public void testSurroundInCatch() {
        String file = "Surround";
	openSourceFile("org.netbeans.test.java.hints.HintsTest",file);
	editor = new EditorOperator(file);
	editor.setCaretPosition(73,1);
	String pattern = ".*"+
        "\\} catch\\(FileNotFoundException exception\\) \\{.*"+
        "    try \\{.*"+
        "        new FileReader\\(\"e\"\\);.*"+
        "    \\} catch \\(FileNotFoundException ex\\) \\{.*"+
        "        Logger.getLogger\\(Surround.class.getName\\(\\)\\).log\\(Level.SEVERE, null, ex\\);.*"+
        "    \\}.*"+
        "\\}.*";
	useHint("Surround Block",new String[]{"Add throws","Surround Block","Surround Statement"},pattern);
    }
    
    public void testStatement() {
        String file = "Surround";
	openSourceFile("org.netbeans.test.java.hints.HintsTest",file);
	editor = new EditorOperator(file);
	editor.setCaretPosition(52,1);
	String pattern = ".*"+
        "System.out.println\\(\"line1\"\\);.*"+
        "try \\{.*"+
        "    URL u = new URL\\(\"a\"\\);.*"+
        "\\} catch \\(MalformedURLException ex\\) \\{.*"+
        "    Logger.getLogger\\(Surround.class.getName\\(\\)\\).log\\(Level.SEVERE, null, ex\\);.*"+
        "\\}.*"+
        "System.out.println\\(\"line2\"\\);.*"+
        ".*";
	useHint("Surround Statement",new String[]{"Add throws","Surround Block","Surround Statement"},pattern);
    }
    
    public void testNoLogging() {
        String file = "Surround";
	openSourceFile("org.netbeans.test.java.hints.HintsTest",file);        
        OptionsOperator oo = OptionsOperator.invoke();
        oo.selectEditor();
        JTabbedPaneOperator jtpo = new JTabbedPaneOperator(oo);
        jtpo.selectPage("Hints");
        JSplitPaneOperator jspo = new JSplitPaneOperator(oo);       
        JTreeOperator jto = new JTreeOperator(jtpo);                        
        selectHintNode(jto,"errors","Surround with try-catch");
        JCheckBoxOperator chbox1 = new JCheckBoxOperator(new ContainerOperator((Container)jspo.getRightComponent()),"Use org.openide.util.Exceptions.printStackTrace" );        
        chBoxSetSelected(chbox1, false);        
        JCheckBoxOperator chbox2 = new JCheckBoxOperator(new ContainerOperator((Container)jspo.getRightComponent()),"Use java.util.logging.Logger");        
        chBoxSetSelected(chbox2, false);        
        oo.ok();        
        new EventTool().waitNoEvent(1000);
        editor = new EditorOperator(file);
	editor.setCaretPosition(18,1);
	String pattern = ".*"+
        "try \\{.*"+
        "    FileReader fr = new FileReader\\(\"file\"\\);.*"+
        "\\} catch \\(FileNotFoundException ex\\) \\{.*"+
        "    ex.printStackTrace\\(\\);.*"+
        "\\}.*";        
        useHint("Surround Statement",new String[]{"Add throws","Surround Block","Surround Statement"},pattern);
    }

    
    public void testLoggerLogging() {
        String file = "Surround";
	openSourceFile("org.netbeans.test.java.hints.HintsTest",file);        
        OptionsOperator oo = OptionsOperator.invoke();
        oo.selectEditor();
        JTabbedPaneOperator jtpo = new JTabbedPaneOperator(oo);
        jtpo.selectPage("Hints");
        JSplitPaneOperator jspo = new JSplitPaneOperator(oo);       
        JTreeOperator jto = new JTreeOperator(jtpo);
        selectHintNode(jto,"errors","Surround with try-catch");                
        JCheckBoxOperator chbox1 = new JCheckBoxOperator(new ContainerOperator((Container)jspo.getRightComponent()),"Use org.openide.util.Exceptions.printStackTrace" );
        chBoxSetSelected(chbox1, false);
        JCheckBoxOperator chbox2 = new JCheckBoxOperator(new ContainerOperator((Container)jspo.getRightComponent()),"Use java.util.logging.Logger");
        chBoxSetSelected(chbox2, true);
        oo.ok();
        new EventTool().waitNoEvent(1000);
        editor = new EditorOperator(file);
	editor.setCaretPosition(18,1);
	String pattern = ".*"+
        "try \\{.*"+
        "    FileReader fr = new FileReader\\(\"file\"\\);.*"+
        "\\} catch \\(FileNotFoundException ex\\) \\{.*"+
        "    Logger.getLogger\\(Surround.class.getName\\(\\)\\).log\\(Level.SEVERE, null, ex\\);.*"+
        "\\}.*";        
        useHint("Surround Statement",new String[]{"Add throws","Surround Block","Surround Statement"},pattern);
    }
    
    public static void main(String[] args) {
        TestRunner.run(SurroundTest.class);       
    }
    
    public static Test suite() {        
        return NbModuleSuite.create(
                NbModuleSuite.createConfiguration(SurroundTest.class).enableModules(".*").clusters(".*"));
    }
}
