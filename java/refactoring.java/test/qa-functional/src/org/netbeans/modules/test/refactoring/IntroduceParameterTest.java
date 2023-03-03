/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
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
public class IntroduceParameterTest extends ModifyingRefactoring {


        private enum currentTest { testSimple_A_A,
                                   testSimple_A_B,
                                   testSimple_A_C,
                                   testSimple_A_D,
                                   
                                   testSimple_A_E,
                                   testSimple_A_F,
                                   testSimple_A_G,
                                   testSimple_A_H,
                                   
                                   testSimple_A_I,
                                   testSimple_A_J,
                                   testSimple_A_K,
                                   testSimple_A_L,
                                   
                                   testSimple_A_M,
                                   
                                   nothing
        };

	public IntroduceParameterTest(String name){
		super(name);
	}

	public static Test suite(){

		return JellyTestCase.emptyConfiguration().
                    addTest(IntroduceParameterTest.class, "testSimple_A_A").
                    addTest(IntroduceParameterTest.class, "testSimple_A_B").
                    addTest(IntroduceParameterTest.class, "testSimple_A_C").
                    addTest(IntroduceParameterTest.class, "testSimple_A_D").
                    addTest(IntroduceParameterTest.class, "testSimple_A_E").
                    addTest(IntroduceParameterTest.class, "testSimple_A_F").
                    addTest(IntroduceParameterTest.class, "testSimple_A_G").
                    addTest(IntroduceParameterTest.class, "testSimple_A_H").
                    addTest(IntroduceParameterTest.class, "testSimple_A_I").
                    addTest(IntroduceParameterTest.class, "testSimple_A_J").
//                    addTest(IntroduceParameterTest.class, "testSimple_A_K").
                    addTest(IntroduceParameterTest.class, "testSimple_A_L").
                    addTest(IntroduceParameterTest.class, "testSimple_A_M").
                    suite();
	}
         
	public void testSimple_A_A(){
		performIntroduvceMethod(currentTest.testSimple_A_A);
	}
        
	public void testSimple_A_B(){
		performIntroduvceMethod(currentTest.testSimple_A_B);
	}
                
	public void testSimple_A_C(){
		performIntroduvceMethod(currentTest.testSimple_A_C);
	}
                        
	public void testSimple_A_D(){
		performIntroduvceMethod(currentTest.testSimple_A_D);
	}
        
	public void testSimple_A_E(){
		performIntroduvceMethod(currentTest.testSimple_A_E);
	}
        
	public void testSimple_A_F(){
		performIntroduvceMethod(currentTest.testSimple_A_F);
	}
                
	public void testSimple_A_G(){
		performIntroduvceMethod(currentTest.testSimple_A_G);
	}
                        
	public void testSimple_A_H(){
		performIntroduvceMethod(currentTest.testSimple_A_H);
	}
        
	public void testSimple_A_I(){
		performIntroduvceMethod(currentTest.testSimple_A_I);
	}
        
	public void testSimple_A_J(){
		performIntroduvceMethod(currentTest.testSimple_A_J);
	}
                
//	public void testSimple_A_K(){
//		performIntroduvceMethod(currentTest.testSimple_A_K);
//	}
                        
	public void testSimple_A_L(){
		performIntroduvceMethod(currentTest.testSimple_A_L);
	}
        
	public void testSimple_A_M(){
		performIntroduvceMethod(currentTest.testSimple_A_M);
	}

        
        private void performIntroduvceMethod(currentTest c){      
//  "ClassA":           
//            
//  testSimple_A_A  - (12,20,24);  default;       Declarefinal=false; ReplaceAllOccurences=false; GenerateJavadocForthisMethod=false; UpdateExistingMethode; 
//  testSimple_A_B  - (12,20,24);  "myParameter"; Declarefinal=true;  ReplaceAllOccurences=false; GenerateJavadocForthisMethod=false; UpdateExistingMethode; 
//  testSimple_A_C  - (12,20,24);  "myParameter"; Declarefinal=false; ReplaceAllOccurences=true;  GenerateJavadocForthisMethod=false; UpdateExistingMethode; 
//  testSimple_A_D  - (12,20,24);  "myParameter"; Declarefinal=false; ReplaceAllOccurences=false; GenerateJavadocForthisMethod=true;  UpdateExistingMethode; 
//
//  testSimple_A_E  - (13,20,24);  default;       Declarefinal=false; ReplaceAllOccurences=false; GenerateJavadocForthisMethod=false; CreateNewMethodAndDelegateFromExistingMethod; 
//  testSimple_A_F  - (13,20,24);  "myParameter"; Declarefinal=true;  ReplaceAllOccurences=false; GenerateJavadocForthisMethod=false; CreateNewMethodAndDelegateFromExistingMethod; 
//  testSimple_A_G  - (13,20,24);  "myParameter"; Declarefinal=false; ReplaceAllOccurences=true;  GenerateJavadocForthisMethod=false; CreateNewMethodAndDelegateFromExistingMethod; 
//  testSimple_A_H  - (13,20,24);  "myParameter"; Declarefinal=false; ReplaceAllOccurences=false; GenerateJavadocForthisMethod=true;  CreateNewMethodAndDelegateFromExistingMethod; 
//
//  testSimple_A_I  - (30,13,22);  default;       Declarefinal=false; ReplaceAllOccurences=false; GenerateJavadocForthisMethod=false; UpdateExistingMethode; 
//  testSimple_A_J  - (30,13,22);  "myParameter"; Declarefinal=true;  ReplaceAllOccurences=false; GenerateJavadocForthisMethod=false; UpdateExistingMethode; 
//  testSimple_A_K  - (30,13,22);  "myParameter"; Declarefinal=false; ReplaceAllOccurences=true;  GenerateJavadocForthisMethod=false; UpdateExistingMethode; 
//  testSimple_A_L  - (30,13,22);  "myParameter"; Declarefinal=false; ReplaceAllOccurences=false; GenerateJavadocForthisMethod=true;  UpdateExistingMethode; 
//
//  testSimple_A_M  - (30,13,22);  default;       Declarefinal=false; ReplaceAllOccurences=false; GenerateJavadocForthisMethod=false; CreateNewMethodAndDelegateFromExistingMethod; 
//
//  ----------------------------------------
//            
//  testSimple_A_K failures because of https://netbeans.org/bugzilla/show_bug.cgi?id=239079     
			
            	IntroduceParameterOperator ipo = null;
                ErrorOperator eo = null;
                String report = "";
                
                boolean debugMode = false;
            
                EditorOperator editor;
                
                // open source file
                String curClass = "";
                 switch(c){
                    case testSimple_A_A: curClass = "Class_A_A"; break;
                    case testSimple_A_B: curClass = "Class_A_B"; break;
                    case testSimple_A_C: curClass = "Class_A_C"; break;
                    case testSimple_A_D: curClass = "Class_A_D"; break;
						
                    case testSimple_A_E: curClass = "Class_A_E"; break;
                    case testSimple_A_F: curClass = "Class_A_F"; break;
                    case testSimple_A_G: curClass = "Class_A_G"; break;
                    case testSimple_A_H: curClass = "Class_A_H"; break;
						
                    case testSimple_A_I: curClass = "Class_A_I"; break;
                    case testSimple_A_J: curClass = "Class_A_J"; break;
                    case testSimple_A_K: curClass = "Class_A_K"; break;
                    case testSimple_A_L: curClass = "Class_A_L"; break;
						
                    case testSimple_A_M: curClass = "Class_A_M"; break;
                }
                
                 // open source file
                switch(c){
                    default:
                        openSourceFile("introduceParameter", curClass);
                        editor = new EditorOperator(curClass + ".java");
                        break;
                }
                
                // delete part of code
                switch(c){
                    case nothing: 
                        editor.setCaretPosition(62, 13);
                        editor.select(62, 13, 20);
                        editor.pushKey(KeyEvent.VK_BACK_SPACE); break;
                }
                
                if(debugMode) new EventTool().waitNoEvent(2000);

                // type some text
                switch(c){
                    case nothing: editor.insert("break"); break;
                }

                // put carret on position
                switch(c){
                    case testSimple_A_A:
                    case testSimple_A_B:
                    case testSimple_A_C:
                    case testSimple_A_D:
                        editor.setCaretPosition(12, 1);
                        break;
                    case testSimple_A_E:
                    case testSimple_A_F:
                    case testSimple_A_G:
                    case testSimple_A_H:
                        editor.setCaretPosition(13, 1);
                        break;
                    case testSimple_A_I:
                    case testSimple_A_J:
                    case testSimple_A_K:
                    case testSimple_A_L:
                        editor.setCaretPosition(30, 1);
                        break;
                    case testSimple_A_M:
                        editor.setCaretPosition(8, 1);
                        break;
                }
                
                // select predefined part of code
                switch(c){
                    case testSimple_A_A:
                    case testSimple_A_B:
                    case testSimple_A_C:
                    case testSimple_A_D:
			editor.select(12, 20, 24);
                        break;
                    case testSimple_A_E:
                    case testSimple_A_F:
                    case testSimple_A_G:
                    case testSimple_A_H:
                        editor.select(13, 20, 24);
                        break;
                    case testSimple_A_I:
                    case testSimple_A_J:
                    case testSimple_A_K:
                    case testSimple_A_L:
                        editor.select(30, 13, 22);
                        break;
                    case testSimple_A_M:
                        editor.select(8, 9, 22);
                        break;
                }
                
                if(debugMode) new EventTool().waitNoEvent(1000);
                
		new EventTool().waitNoEvent(1000); // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
				
                // call Reafctor > Introduce parameter
                switch(c){
                    default:
                        new RefactorIntroduceParameterAction().performPopup(editor);
                        break;
                }
                                               
                // catch Introduce method dialog
                switch(c){
                    case nothing: break;
                    default:
                        ipo = new IntroduceParameterOperator();
                        break;
                }
                
                if(debugMode) new EventTool().waitNoEvent(2000);
				
		new EventTool().waitNoEvent(1000); // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
                
                // type new name
                switch(c){
                    case nothing:
                        ipo.getNewName().pushKey(KeyEvent.VK_BACK_SPACE);
                        ipo.getNewName().pushKey(KeyEvent.VK_BACK_SPACE);
                        ipo.getNewName().pushKey(KeyEvent.VK_BACK_SPACE); break;
                    case testSimple_A_A:
                    case testSimple_A_E:
                    case testSimple_A_I: break;
                    default:
                        ipo.getParName().typeText("myParameter");
                        break;
                }
                
                if(debugMode) new EventTool().waitNoEvent(1500);
                
                 // select/deselect Declare Final
                switch(c){
                    case testSimple_A_B:
                    case testSimple_A_F:
                    case testSimple_A_J:
                         // Declare Final = TRUE
                        if(!ipo.getDeclareFinal().isSelected()) ipo.getDeclareFinal().changeSelection(true);
                        break;
                    default:
                         // Declare Final = FALSE
                        if(ipo.getDeclareFinal().isSelected()) ipo.getDeclareFinal().doClick();
                        break;
                }
                
                if(debugMode) new EventTool().waitNoEvent(1500);
                
                // select/deselect Replace All Occurences
                switch(c){
                    case testSimple_A_C:
                    case testSimple_A_G:
                    case testSimple_A_K:
                         // Replace All Occurences = TRUE
                        if(!ipo.getReplaceAllOccurences().isSelected()) ipo.getReplaceAllOccurences().doClick();
                        break;
                    default:
                         // Replace All Occurences = FALSE
                        if(ipo.getReplaceAllOccurences().isSelected()) ipo.getReplaceAllOccurences().doClick();
                        break;
                }
                
                // select/deselect Generate Javadoc for this Method
                switch(c){
                    case testSimple_A_D:
                    case testSimple_A_H:
                    case testSimple_A_L:
                         // Replace All Occurences = TRUE
                        if(!ipo.getGenerateJavadoc().isSelected()) ipo.getGenerateJavadoc().doClick();
                        break;
                    default:
                         // Replace All Occurences = FALSE
                        if(ipo.getGenerateJavadoc().isSelected()) ipo.getGenerateJavadoc().doClick();
                        break;
                }
                
                // Create new Method and Delegate from Existing Method / Update Existing Methode
                switch(c){
                    case testSimple_A_A:
                    case testSimple_A_B:
                    case testSimple_A_C:
                    case testSimple_A_D:
						
                    case testSimple_A_I:
                    case testSimple_A_J:
                    case testSimple_A_K:
                    case testSimple_A_L:
                        // Update Existing Methode
                        ipo.getUpdateMethods().doClick();
                        break;
                    case testSimple_A_E:
                    case testSimple_A_F:
                    case testSimple_A_G:
                    case testSimple_A_H:
                        // Create new Method and Delegate from Existing Method
                        ipo.getCreatenewMethod().doClick();
                        break;
                }
                
                if(debugMode) new EventTool().waitNoEvent(2000);
                                
                // check whenever Ok btn is disable
                switch(c){                       
                    case nothing:
//                        if (ipo.getBtnRefactor().isEnabled()) report = "// OK BTN IS ENABLED ALTHOUGH REFACTORING IS UNVAILABLE!!!";
                        break;
                }
                 
                // perform refactor by pressing Refactor / Cancel
                switch(c){
                    case nothing:
                        ipo.cancel();
                        break;
                    default:
                        ipo.refactor();
                        break;
                }
                
                if(debugMode) new EventTool().waitNoEvent(3000);
                                
                // catch Error dialog
                switch(c){
                    case nothing:
                        eo = new ErrorOperator();
                        eo.ok();
                        break;
                }
                
                // add report to editor, which causes test is failed
                if(!report.equals("")){
                    editor.setCaretPosition(1, 1);
                    editor.insert(report);
                    editor.pushKey(KeyEvent.VK_ENTER);
                }
                                
                if(debugMode) new EventTool().waitNoEvent(2000);
				
				new EventTool().waitNoEvent(1000); // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!

                // evalue result and discard changes
		ref(editor.getText());
		editor.closeDiscard();
	}

        public void browse(Component cmp) {
            System.out.println(cmp.getClass().getName());
            if(cmp instanceof Container) {
                Component[] components = ((Container)cmp).getComponents();
                for (Component component : components) {
                    browse(component);
                }
            }
        }
}
