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
import org.netbeans.jemmy.EventTool;
import org.netbeans.junit.NbModuleSuite;

/**
 *
 * @author jp159440
 */
public class AddElementHintTest extends HintsTestCase{

    public AddElementHintTest(String name) {
        super(name);
    }
    
    public void testAddElement() {
        String file = "addHint";
        openSourceFile("org.netbeans.test.java.hints.HintsTest", file);
        editor = new EditorOperator(file);
        editor.setCaretPosition(72,1);
        new EventTool().waitNoEvent(750);
        useHint("Create Field",new String[]{"Create Parameter \"a\"",
        "Create Local Variable \"a\"",
        "Create Field \"a\" in org.netbeans.test.java.hints.HintsTest.addHint"},
                ".*private int a;.*");
    }
    
    public void testAddElement2() {
        String file = "addHint";
        openSourceFile("org.netbeans.test.java.hints.HintsTest", file);
        editor = new EditorOperator(file);
        editor.setCaretPosition(66,1);
        new EventTool().waitNoEvent(750);
        useHint("Create Field",new String[]{"Create Parameter \"g\"",
        "Create Local Variable \"g\"",
        "Create Field \"g\" in org.netbeans.test.java.hints.HintsTest.addHint"},
                ".*private LinkedList<String> g;.*");
    }
    
    public void testAddElement3() {
        String file = "Element2";
        openSourceFile("org.netbeans.test.java.hints.HintsTest", "Element1");
        target = new EditorOperator("Element1");
        openSourceFile("org.netbeans.test.java.hints.HintsTest", file);
        editor = new EditorOperator(file);
        editor.setCaretPosition(45,1);                
        new EventTool().waitNoEvent(750);
        useHint("Create Field",new String[]{"Create Field \"field\" in org.netbeans.test.java.hints.HintsTest.Element1"},
                ".*int field;.*");
    }
    
    public void testAddElement4() {
        String file = "Element2";
        openSourceFile("org.netbeans.test.java.hints.HintsTest", "Element1");
        target = new EditorOperator("Element1");
        openSourceFile("org.netbeans.test.java.hints.HintsTest", file);
        editor = new EditorOperator(file);
        editor.setCaretPosition(46,1);                
        new EventTool().waitNoEvent(750);
        useHint("Create Field",new String[]{"Create Field \"statField\" in org.netbeans.test.java.hints.HintsTest.Element1"},
                ".*static String statField;.*");
    }
    
    
    
    public static void main(String[] args) {
        new TestRunner().run(AddElementHintTest.class);
    }
    
    public static Test suite() {
        return NbModuleSuite.create(
                NbModuleSuite.createConfiguration(AddElementHintTest.class).enableModules(".*").clusters(".*"));
    }
    
}
