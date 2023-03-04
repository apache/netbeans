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
