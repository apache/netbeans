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

import java.awt.event.KeyEvent;
import junit.framework.Test;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jemmy.EventTool;
import org.netbeans.modules.test.refactoring.actions.RefactorIntroduceFieldAction;
import org.netbeans.modules.test.refactoring.operators.ErrorOperator;
import org.netbeans.modules.test.refactoring.operators.IntroduceFieldOperator;

/**
 @author (stanislav.sazonov@oracle.com)
 */
public class IntroduceFieldTest extends ModifyingRefactoring {


        private enum currentTest { testSimple_A,
                                   testSimple_B,
                                   testSimple_C,
                                   testSimple_D,
                                   testSimple_E,
                                   testSimple_f,
                                   testSimple_G,
                                   testSimple_H,
                                   testSimple_I,
                                   testSimple_J,
                                   testSimple_K,
                                   testSimple_L,
                                   testSimple_M,
                                   testSimple_N,
                                   testSimple_O,
                                   testSimple_P,
                                   testSimple_Q,
                                   testSimple_R,
                                   testSimple_S,
                                   testSimple_T,
                                   nothing
        };

	public IntroduceFieldTest(String name){
		super(name);
	}

	public static Test suite(){
            // testSimple_Q, testSimple_R fails because of not implemented feature,
            // which checks that field with same name is already exists
            // 20.09.2013
            
		return JellyTestCase.emptyConfiguration().
				addTest(RenameTest.class, "testSimple_A").
				addTest(RenameTest.class, "testSimple_B").
				addTest(RenameTest.class, "testSimple_C").
				addTest(RenameTest.class, "testSimple_D").
                                addTest(RenameTest.class, "testSimple_E").
				addTest(RenameTest.class, "testSimple_f").
				addTest(RenameTest.class, "testSimple_G").
				addTest(RenameTest.class, "testSimple_H").
                                addTest(RenameTest.class, "testSimple_I").
				addTest(RenameTest.class, "testSimple_J").
				addTest(RenameTest.class, "testSimple_K").
				addTest(RenameTest.class, "testSimple_L").
                                addTest(RenameTest.class, "testSimple_M").
				addTest(RenameTest.class, "testSimple_N").
				addTest(RenameTest.class, "testSimple_O").
				addTest(RenameTest.class, "testSimple_P").
//                                addTest(RenameTest.class, "testSimple_Q").
//				addTest(RenameTest.class, "testSimple_R").
				addTest(RenameTest.class, "testSimple_S").
				addTest(RenameTest.class, "testSimple_T").
				suite();
	}
         
	public void testSimple_A(){
		performIntroduvceMethod(currentTest.testSimple_A);
	}
        
        public void testSimple_B(){
		performIntroduvceMethod(currentTest.testSimple_B);
	}
                
        public void testSimple_C(){
		performIntroduvceMethod(currentTest.testSimple_C);
	}
                        
        public void testSimple_D(){
		performIntroduvceMethod(currentTest.testSimple_D);
	}
        
        public void testSimple_E(){
		performIntroduvceMethod(currentTest.testSimple_E);
	}
        
        public void testSimple_f(){
		performIntroduvceMethod(currentTest.testSimple_f);
	}
                
        public void testSimple_G(){
		performIntroduvceMethod(currentTest.testSimple_G);
	}
                        
        public void testSimple_H(){
		performIntroduvceMethod(currentTest.testSimple_H);
	}
        
        public void testSimple_I(){
		performIntroduvceMethod(currentTest.testSimple_I);
	}
        
        public void testSimple_J(){
		performIntroduvceMethod(currentTest.testSimple_J);
	}
                
        public void testSimple_K(){
		performIntroduvceMethod(currentTest.testSimple_K);
	}
                        
        public void testSimple_L(){
		performIntroduvceMethod(currentTest.testSimple_L);
        }
        
        public void testSimple_M(){
		performIntroduvceMethod(currentTest.testSimple_M);
	}
        
        public void testSimple_N(){
		performIntroduvceMethod(currentTest.testSimple_N);
	}
                
        public void testSimple_O(){
		performIntroduvceMethod(currentTest.testSimple_O);
	}
                        
        public void testSimple_P(){
		performIntroduvceMethod(currentTest.testSimple_P);
        }
        
//        public void testSimple_Q(){
//		performIntroduvceMethod(currentTest.testSimple_Q);
//	}
//        
//        public void testSimple_R(){
//		performIntroduvceMethod(currentTest.testSimple_R);
//	}
                
        public void testSimple_S(){
		performIntroduvceMethod(currentTest.testSimple_S);
	}
                        
        public void testSimple_T(){
		performIntroduvceMethod(currentTest.testSimple_T);
        }
        
        private void performIntroduvceMethod(currentTest c){      
            
//                testSimple_A  - select(21,17,21); Public;    "myField"; InField=true; ReplaceAllOccurences=true; DeclareFinal=true
//                testSimple_B  - select(21,17,21); Protected; "myField"; InField=true; ReplaceAllOccurences=true; DeclareFinal=true
//                testSimple_C  - select(21,17,21); Default;   "myField"; InField=true; ReplaceAllOccurences=true; DeclareFinal=true
//                testSimple_D  - select(21,17,21); Private;   "myField"; InField=true; ReplaceAllOccurences=true; DeclareFinal=true
            
//                testSimple_E  - select(22,17,21); Public;    "myField"; InCurrentMethod=true; ReplaceAllOccurences=false;
//                testSimple_F  - select(22,17,21); Protected; "myField"; InCurrentMethod=true; ReplaceAllOccurences=false;
//                testSimple_G  - select(22,17,21); Default;   "myField"; InCurrentMethod=true; ReplaceAllOccurences=false;
//                testSimple_H  - select(22,17,21); Private;   "myField"; InCurrentMethod=true; ReplaceAllOccurences=false;
            
//                testSimple_I  - select(33,20,25); Public;    "myField"; InField=true; ReplaceAllOccurences=disbled; DeclareFinal=false;
//                testSimple_J  - select(33,20,25); Protected; "myField"; InField=true; ReplaceAllOccurences=disbled; DeclareFinal=false;
//                testSimple_K  - select(33,20,25); Default;   "myField"; InField=true; ReplaceAllOccurences=disbled; DeclareFinal=false;
//                testSimple_L  - select(33,20,25); Private;   "myField"; InField=true; ReplaceAllOccurences=disbled; DeclareFinal=false;
            
//                testSimple_M  - select(27,17,21); Public;    "myField"; InConstructor=true; ReplaceAllOccurences=disbled; DeclareFinal=false;
//                testSimple_N  - select(27,17,21); Protected; "myField"; InConstructor=true; ReplaceAllOccurences=disbled; DeclareFinal=false;
//                testSimple_O  - select(27,17,21); Default;   "myField"; InConstructor=true; ReplaceAllOccurences=disbled; DeclareFinal=false;
//                testSimple_P  - select(27,17,21); Private;   "myField"; InConstructor=true; ReplaceAllOccurences=disbled; DeclareFinal=false;
            
//                ILLEGAL            
//                testSimple_Q  - select(33,20,25); Public;    "konst";
//                testSimple_R  - select(33,20,25); Protected; "constExpr";
//                testSimple_S  - select(33,20,25); Default;   "null";
//                testSimple_T  - select(33,20,25); Private;   "";
            
            	IntroduceFieldOperator ifo = null;
                ErrorOperator eo = null;
                String report = "";
                
                boolean debugMode = false;
            
                // open source file
		openSourceFile("introduceField", "ClassA");
		EditorOperator editor = new EditorOperator("ClassA.java");
                
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
                    case testSimple_A:
                    case testSimple_B:
                    case testSimple_C:
                    case testSimple_D: editor.setCaretPosition(21, 1); break;
                    case testSimple_E:
                    case testSimple_f:
                    case testSimple_G:
                    case testSimple_H: editor.setCaretPosition(22, 1); break;
                    case testSimple_I:
                    case testSimple_J:
                    case testSimple_K:
                    case testSimple_L: editor.setCaretPosition(33, 1); break;
                    case testSimple_M:
                    case testSimple_N:
                    case testSimple_O:
                    case testSimple_P: editor.setCaretPosition(27, 1); break;
                    case testSimple_Q:
                    case testSimple_R:
                    case testSimple_S:
                    case testSimple_T: editor.setCaretPosition(33, 1); break;
                }
                
                // select predefined part of code
                switch(c){
                    case testSimple_A:
                    case testSimple_B:
                    case testSimple_C:
                    case testSimple_D: editor.select(21, 17, 21); break;
                    case testSimple_E:
                    case testSimple_f:
                    case testSimple_G:
                    case testSimple_H: editor.select(22, 17, 21); break;
                    case testSimple_I:
                    case testSimple_J:
                    case testSimple_K:
                    case testSimple_L: editor.select(33, 20, 25); break;
                    case testSimple_M:
                    case testSimple_N:
                    case testSimple_O:
                    case testSimple_P: editor.select(27, 17, 21); break;
                    case testSimple_Q:
                    case testSimple_R:
                    case testSimple_S:
                    case testSimple_T: editor.select(33, 20, 25); break;
                }
                
                if(debugMode) new EventTool().waitNoEvent(1000);
                
                // call Reafctor > Introduce method
                switch(c){
                    default: new RefactorIntroduceFieldAction().performPopup(editor); break;
                }
                                               
                // catch Introduce method dialog
                switch(c){
                    default: ifo = new IntroduceFieldOperator(); break;
                }
                
                if(debugMode) new EventTool().waitNoEvent(2000);
                
                // type new name
                switch(c){
                    case testSimple_Q: ifo.getNewName().typeText("konst"); break;
                    case testSimple_R: ifo.getNewName().typeText("constExpr"); break;
                    case testSimple_S: ifo.getNewName().typeText("null"); break;
                    case testSimple_T: ifo.getNewName().pushKey(KeyEvent.VK_BACK_SPACE); break;
                    default: ifo.getNewName().typeText("myField"); break;
                }
                
                // select access option 
                switch(c){
                    case testSimple_A: 
                    case testSimple_E: 
                    case testSimple_I: 
                    case testSimple_M: ifo.getRadPublic().setSelected(true); break;    // Public
                    case testSimple_B: 
                    case testSimple_f: 
                    case testSimple_J: 
                    case testSimple_N: ifo.getRadProtected().setSelected(true); break; // Protected
                    case testSimple_C: 
                    case testSimple_G: 
                    case testSimple_K: 
                    case testSimple_O: ifo.getRadDefault().setSelected(true); break;   // Default
                    case testSimple_D: 
                    case testSimple_H: 
                    case testSimple_L: 
                    case testSimple_P: ifo.getRadPrivate().setSelected(true); break;   // Private
                }
                
                if(debugMode) new EventTool().waitNoEvent(2000);
                
                // - {In Current Method, In Field, In Constructor}
                // - Replace All Occurences
                // - Declare Final
                switch(c){
                    case testSimple_A:
                    case testSimple_B:
                    case testSimple_C:
                    case testSimple_D:
                        // InField = TRUE, ReplaceAllOccurences = TRUE, DeclareFinal = TRUE
                        ifo.getInField().doClick();
                        ifo.getInField().doClick();
                        if(!ifo.getAlsoReplace().isSelected())  ifo.getAlsoReplace().doClick();
                        if(!ifo.getDeclareFinal().isSelected()) ifo.getDeclareFinal().doClick();
                        break;
                    case testSimple_E:
                    case testSimple_f:
                    case testSimple_G:
                    case testSimple_H:
                        // InCurrentMethod = TRUE, ReplaceAllOccurences = FALSE, DeclareFinal = FALSE
                        if(ifo.getAlsoReplace().isSelected())  ifo.getAlsoReplace().doClick();
                        ifo.getInCurrentMethod().doClick();
                        ifo.getInCurrentMethod().doClick();
                        break;
                    case testSimple_I:
                    case testSimple_J:
                    case testSimple_K:
                    case testSimple_L:
                        // InField = TRUE, DeclareFinal = FALSE
                        ifo.getInField().doClick();
                        ifo.getInField().doClick();
                        if(ifo.getDeclareFinal().isSelected()) ifo.getDeclareFinal().doClick();
                        break;
                    case testSimple_M:
                    case testSimple_N:
                    case testSimple_O:
                    case testSimple_P:
                        // InConstructor = true, DeclareFinal = FALSE, ReplaceAllOccurences = TRUE
                        ifo.getInConstructor().doClick();
                        ifo.getInConstructor().doClick();
                        if(!ifo.getAlsoReplace().isSelected())  ifo.getAlsoReplace().doClick();
                        if(ifo.getDeclareFinal().isSelected()) ifo.getDeclareFinal().doClick();
                        break;
                }
                
                if(debugMode) new EventTool().waitNoEvent(3000);
                
                // check whenever Ok btn is disable
                switch(c){
                    case testSimple_Q:
                    case testSimple_R:
                    case testSimple_S:
                    case testSimple_T:
                        if (ifo.btOK().isEnabled()) report = "// OK BTN IS ENABLED ALTHOUGH REFACTORING IS UNVAILABLE!!!";
                        break;
                }
                
                // perform refactor by pressing Ok / Cancel
                switch(c){
                    case testSimple_Q:
                    case testSimple_R:
                    case testSimple_S:
                    case testSimple_T: ifo.cancel(); break;
                    default: ifo.ok(); break;
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

                // evalue result and discard changes
		ref(editor.getText());
		editor.closeDiscard();
	}
}
