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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.test.java.hints;

import junit.framework.Test;
import junit.textui.TestRunner;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.junit.NbModuleSuite;

/**
 *
 * @author jp159440
 */
public class AddImportTest extends HintsTestCase{

    public AddImportTest(String name) {
        super(name);
    }
    
    
    public void testAddImport1() {
	String file = "Imports";
	openSourceFile("org.netbeans.test.java.hints.HintsTest",file);
	editor = new EditorOperator(file);
	editor.setCaretPosition(46,1);
	String pattern = ".*import java.net.URL;.*";
	useHint("Add import for java.net",new String[]{"Add import for java.net.URL","Add import for javax.print.DocFlavor.URL","Create class \"URL\""},pattern);
    }
    
    public void testAddImport2() {
        String file = "Imports";
        openSourceFile("org.netbeans.test.java.hints.HintsTest", file);
        editor = new EditorOperator(file);
        editor.setCaretPosition(47,1);        
        String pattern = ".*";
        useHint("Create",new String[]{"Create class \"NonExisting\""},pattern);
    }
    
    public void testAddImport3() {
        String file = "Imports";
        openSourceFile("org.netbeans.test.java.hints.HintsTest", file);
        editor = new EditorOperator(file);
        editor.setCaretPosition(48,1);        
        String pattern = ".*import javax.swing.JButton;.*";
        useHint("Add import",new String[]{"Add import for javax.swing.JButton"},pattern);
    }
       
    public void testRemoveImport() {
        String file = "RemoveImport";
        openSourceFile("org.netbeans.test.java.hints.HintsTest", file);
        editor = new EditorOperator(file);
        editor.setCaretPosition(45,1);        
        String pattern = ".*import java\\.net\\.URL;\\simport java\\.util\\.List;.*";
        useHint("Remove Unused Import",new String[]{"Remove Unused Import","Remove All Unused Imports"},pattern);
    }
    
    public void testRemoveAllImport() {
        String file = "RemoveImport";
        openSourceFile("org.netbeans.test.java.hints.HintsTest", file);
        editor = new EditorOperator(file);
        editor.setCaretPosition(45,1);        
        String pattern = ".*package org.netbeans\\.test\\.java\\.hints\\.HintsTest;\\s*import java\\.io\\.FileReader;\\s*public class RemoveImport \\{.*";
        useHint("Remove All Unused Imports",new String[]{"Remove Unused Import","Remove All Unused Imports"},pattern);
    }
    
    public static void main(String[] args) {
        new TestRunner().run(AddImportTest.class);                
    }
    
    public static Test suite() {
        return NbModuleSuite.create(
                NbModuleSuite.createConfiguration(AddImportTest.class).enableModules(".*").clusters(".*"));
    }

}
