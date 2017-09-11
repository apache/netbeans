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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
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
