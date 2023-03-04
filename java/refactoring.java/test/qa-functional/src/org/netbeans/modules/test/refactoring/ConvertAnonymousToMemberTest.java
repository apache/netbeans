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

package org.netbeans.modules.test.refactoring;

import java.awt.*;
import java.awt.event.KeyEvent;
import javax.swing.*;
import junit.framework.Test;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jemmy.EventTool;
import org.netbeans.modules.test.refactoring.actions.*;
import org.netbeans.modules.test.refactoring.operators.*;
import org.netbeans.modules.test.refactoring.operators.ErrorOperator;

/**
 @author (stanislav.sazonov@oracle.com)
 */
public class ConvertAnonymousToMemberTest extends ModifyingRefactoring {


        private enum currentTest { testSimple_A_A,
                                   testSimple_A_B,
                                   
                                   nothing
        };

	public ConvertAnonymousToMemberTest(String name){
            super(name);
	}

	public static Test suite(){

            return JellyTestCase.emptyConfiguration().
                addTest(RenameTest.class, "testSimple_A_A").
                addTest(RenameTest.class, "testSimple_A_B").

                suite();
	}
         
	public void testSimple_A_A(){
		performTest(currentTest.testSimple_A_A);
	}
        
	public void testSimple_A_B(){
		performTest(currentTest.testSimple_A_B);
	}
        
        private void performTest(currentTest c){
            
		
            IntroduceParameterOperator ipo = null;
            ErrorOperator eo = null;
            String report = "";

            boolean debugMode = true;

            EditorOperator editor;

            // open source file
            String curClass = "";
            switch(c){
                default: curClass = "ClassA"; break;
            }

             // open source file
            switch(c){
                default:
                    openSourceFile("anonymousToMember", curClass);
                    editor = new EditorOperator(curClass + ".java");
                    break;
            }

            if(debugMode) new EventTool().waitNoEvent(2000);

            // put carret on position
            switch(c){
                case testSimple_A_A:
                    editor.setCaretPosition(13, 19);
                    break;
                case testSimple_A_B:
                    editor.setCaretPosition(44, 37);
                    break;
            }

            if(debugMode) new EventTool().waitNoEvent(1000);

            new EventTool().waitNoEvent(1000); // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!

            // call Reafctor > Convert Anonymous to Member
            switch(c){
                default:
                    new ConvertAnonymousToMemberAction().performPopup(editor);
                    break;
            }

//            // catch Introduce method dialog
//            switch(c){
//                case nothing: break;
//                default:
//                    ipo = new IntroduceParameterOperator();
//                    break;
//            }
            
            if(debugMode) new EventTool().waitNoEvent(1000);
            
            // catch Introduce method dialog
            switch(c){
                case nothing: break;
                default:
                    editor.pushKey(KeyEvent.VK_ENTER);
//                    editor.pushKey(KeyEvent.VK_C);
//                    editor.pushKey(KeyEvent.VK_D);
//                    editor.pushKey(KeyEvent.VK_BACK_SPACE);
                    break;
            }

            
            if(debugMode) new EventTool().waitNoEvent(2000);

            // evalue result and discard changes
            ref(editor.getText());
            editor.closeDiscard();
	}
}
