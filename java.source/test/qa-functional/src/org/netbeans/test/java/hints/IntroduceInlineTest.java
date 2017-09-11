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

import junit.framework.Test;
import junit.textui.TestRunner;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jemmy.EventTool;
import org.netbeans.junit.NbModuleSuite;

/**
 *
 * @author jp159440
 */
public class IntroduceInlineTest extends HintsTestCase {

    public IntroduceInlineTest(String name) {
        super(name);
    }

    public void testIntroduceInline() {
        String file = "Inline";
	openSourceFile("org.netbeans.test.java.hints.HintsTest",file);        
        setInPlaceCreation(true);
        new EventTool().waitNoEvent(2000);
        editor = new EditorOperator(file);
	editor.setCaretPosition(8,1);
	String pattern = ".*"+        
        "int a = 1;.*"+
        "int b = 2;.*"+
        "int c = a \\+ b;.*";        
        useHint("Create Local",new String[]{"Create Local","Create Parameter","Create Field"},pattern);
    }
    
    public void testNotInline() {
        String file = "Inline";
	openSourceFile("org.netbeans.test.java.hints.HintsTest",file);                
        setInPlaceCreation(false);
        new EventTool().waitNoEvent(2000);
        editor = new EditorOperator(file);
	editor.setCaretPosition(8,1);
	String pattern = ".*"+        
        "int c;.*"+
        "int a = 1;.*"+
        "int b = 2;.*"+
        "c = a\\+b;.*";        
        useHint("Create Local",new String[]{"Create Local","Create Parameter","Create Field"},pattern);
    }
    
    
    public void testNotInlineSuper() {
        String file = "Inline";
	openSourceFile("org.netbeans.test.java.hints.HintsTest",file);        
        setInPlaceCreation(false);
        new EventTool().waitNoEvent(2000);
        editor = new EditorOperator(file);
	editor.setCaretPosition(15,1);
	String pattern = ".*"+
        "super\\(\\);.*"+
        "int z;.*"+
        "int x = 1;.*"+
        "int y = 2;.*"+
        "z = x\\+y;.*";        
        useHint("Create Local",new String[]{"Create Local","Create Parameter","Create Field"},pattern);
    }
    
    public static void main(String[] args) {
        TestRunner.run(IntroduceInlineTest.class);        
        
    }
    
    public static Test suite() {
        return NbModuleSuite.create(
                NbModuleSuite.createConfiguration(IntroduceInlineTest.class).enableModules(".*").clusters(".*"));
    }

    
}
