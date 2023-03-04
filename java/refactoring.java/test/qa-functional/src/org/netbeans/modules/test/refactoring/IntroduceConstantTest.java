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
import org.netbeans.modules.test.refactoring.actions.*;
import org.netbeans.modules.test.refactoring.operators.*;
import org.netbeans.modules.test.refactoring.operators.ErrorOperator;

/**
 @author (stanislav.sazonov@oracle.com)
 */
public class IntroduceConstantTest extends ModifyingRefactoring {


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
                                   testSimple_A_N,
                                   testSimple_A_O,
                                   testSimple_A_P,
                                   
                                   testSimple_A_Q,
                                   testSimple_A_R,
                                   testSimple_A_S,
                                   testSimple_A_T,
                                   
                                   testSimple_A_U,
                                   testSimple_A_V,
                                   testSimple_A_W,
                                   testSimple_A_X,
                                   
                                   testSimple_A_Y,
                                   testSimple_A_Z,
                                   testSimple_B_A,
                                   testSimple_B_B,
                                   
                                   testSimple_B_C,
                                   testSimple_B_D,
                                   testSimple_B_E,
                                   testSimple_B_F,
                                   
                                   testSimple_B_G,
                                   testSimple_B_H,
                                   testSimple_B_I,
                                   testSimple_B_J,
                                   
                                   nothing
        };

	public IntroduceConstantTest(String name){
		super(name);
	}

	public static Test suite(){
            
            // testSimple_O, testSimple_P fails because of not implemented feature,
            // which checks that field with same name is already exists
            // https://netbeans.org/bugzilla/...
            //
            // testSimple_B_G fails because introducing constant is still allowed
            // https://netbeans.org/bugzilla/show_bug.cgi?id=236187
            // 16.10.2013
            
		return JellyTestCase.emptyConfiguration().
				addTest(RenameTest.class, "testSimple_A_A").
				addTest(RenameTest.class, "testSimple_A_B").
				addTest(RenameTest.class, "testSimple_A_C").
				addTest(RenameTest.class, "testSimple_A_D").
                        
                                addTest(RenameTest.class, "testSimple_A_E").
				addTest(RenameTest.class, "testSimple_A_F").
				addTest(RenameTest.class, "testSimple_A_G").
				addTest(RenameTest.class, "testSimple_A_H").
                        
                                addTest(RenameTest.class, "testSimple_A_I").
				addTest(RenameTest.class, "testSimple_A_J").
				addTest(RenameTest.class, "testSimple_A_K").
				addTest(RenameTest.class, "testSimple_A_L").
                        
                                addTest(RenameTest.class, "testSimple_A_M").
				addTest(RenameTest.class, "testSimple_A_N").
//				addTest(RenameTest.class, "testSimple_A_O"). // <--- don't work
//				addTest(RenameTest.class, "testSimple_A_P"). // <--- don't work
                        
                                addTest(RenameTest.class, "testSimple_A_Q").
				addTest(RenameTest.class, "testSimple_A_R").
				addTest(RenameTest.class, "testSimple_A_S").
				addTest(RenameTest.class, "testSimple_A_T").
                        
                                addTest(RenameTest.class, "testSimple_A_U").
				addTest(RenameTest.class, "testSimple_A_V").
				addTest(RenameTest.class, "testSimple_A_W").
				addTest(RenameTest.class, "testSimple_A_X").
                        
                                addTest(RenameTest.class, "testSimple_A_Y").
				addTest(RenameTest.class, "testSimple_A_Z").
				addTest(RenameTest.class, "testSimple_B_A").
				addTest(RenameTest.class, "testSimple_B_B").
                        
                                addTest(RenameTest.class, "testSimple_B_C").
				addTest(RenameTest.class, "testSimple_B_D").
				addTest(RenameTest.class, "testSimple_B_E").
				addTest(RenameTest.class, "testSimple_B_F").
                        
//                                addTest(RenameTest.class, "testSimple_B_G"). // <--- don't work
				addTest(RenameTest.class, "testSimple_B_H").
				addTest(RenameTest.class, "testSimple_B_I").
				addTest(RenameTest.class, "testSimple_B_J").
                        
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
                
        public void testSimple_A_K(){
		performIntroduvceMethod(currentTest.testSimple_A_K);
	}
                        
        public void testSimple_A_L(){
		performIntroduvceMethod(currentTest.testSimple_A_L);
        }
        
        public void testSimple_A_M(){
		performIntroduvceMethod(currentTest.testSimple_A_M);
	}
        
        public void testSimple_A_N(){
		performIntroduvceMethod(currentTest.testSimple_A_N);
	}
                
//        public void testSimple_A_O(){
//		performIntroduvceMethod(currentTest.testSimple_A_O);
//	}
//                        
//        public void testSimple_A_P(){
//		performIntroduvceMethod(currentTest.testSimple_A_P);
//        }
        
        public void testSimple_A_Q(){
		performIntroduvceMethod(currentTest.testSimple_A_Q);
	}
        
        public void testSimple_A_R(){
		performIntroduvceMethod(currentTest.testSimple_A_R);
	}
                
        public void testSimple_A_S(){
		performIntroduvceMethod(currentTest.testSimple_A_S);
	}
                        
        public void testSimple_A_T(){
		performIntroduvceMethod(currentTest.testSimple_A_T);
        }
        
        public void testSimple_A_U(){
		performIntroduvceMethod(currentTest.testSimple_A_U);
	}
        
        public void testSimple_A_V(){
		performIntroduvceMethod(currentTest.testSimple_A_V);
	}
                
        public void testSimple_A_W(){
		performIntroduvceMethod(currentTest.testSimple_A_W);
	}
                        
        public void testSimple_A_X(){
		performIntroduvceMethod(currentTest.testSimple_A_X);
        }
        
        public void testSimple_A_Y(){
		performIntroduvceMethod(currentTest.testSimple_A_Y);
	}
        
        public void testSimple_A_Z(){
		performIntroduvceMethod(currentTest.testSimple_A_Z);
	}
                
        public void testSimple_B_A(){
		performIntroduvceMethod(currentTest.testSimple_B_A);
	}
                        
        public void testSimple_B_B(){
		performIntroduvceMethod(currentTest.testSimple_B_B);
        }
        
        public void testSimple_B_C(){
		performIntroduvceMethod(currentTest.testSimple_B_C);
	}
                        
        public void testSimple_B_D(){
		performIntroduvceMethod(currentTest.testSimple_B_D);
        }
        
        public void testSimple_B_E(){
		performIntroduvceMethod(currentTest.testSimple_B_E);
	}
                        
        public void testSimple_B_F(){
		performIntroduvceMethod(currentTest.testSimple_B_F);
        }
        
//        public void testSimple_B_G(){
//		performIntroduvceMethod(currentTest.testSimple_B_G);
//	}
                        
        public void testSimple_B_H(){
		performIntroduvceMethod(currentTest.testSimple_B_H);
        }
        
        public void testSimple_B_I(){
		performIntroduvceMethod(currentTest.testSimple_B_I);
	}
                        
        public void testSimple_B_J(){
		performIntroduvceMethod(currentTest.testSimple_B_J);
        }
        
        private void performIntroduvceMethod(currentTest c){      
//  "ClassA":           
//            
//                testSimple_A_A  - select(12,24,29); Public;    "myField"; ReplaceAllOccurences=false;
//                testSimple_A_B  - select(12,24,29); Protected; "myField"; ReplaceAllOccurences=false;
//                testSimple_A_C  - select(12,24,29); Default;   "myField"; ReplaceAllOccurences=false;
//                testSimple_A_D  - select(12,24,29); Private;   "myField"; ReplaceAllOccurences=false;
//            
//                testSimple_A_E  - select(9, 16, 21); Public;    "myField"; ReplaceAllOccurences=true;
//                testSimple_A_F  - select(9, 16, 21); Protected; "myField"; ReplaceAllOccurences=true;
//                testSimple_A_G  - select(9, 16, 21); Default;   "myField"; ReplaceAllOccurences=true;
//                testSimple_A_H  - select(9, 16, 21); Private;   "myField"; ReplaceAllOccurences=true;
//            
//                testSimple_A_I  - select(18, 16, 16); Public;    "myField"; ReplaceAllOccurences=true;
//                testSimple_A_J  - select(18, 16, 16); Protected; "myField"; ReplaceAllOccurences=true;
//                testSimple_A_K  - select(18, 16, 16); Default;   "myField"; ReplaceAllOccurences=true;
//                testSimple_A_L  - select(18, 16, 16); Private;   "myField"; ReplaceAllOccurences=true;
//
//  ILLEGAL:
//                testSimple_A_M  - select(18, 16, 16); Public;    "";
//                testSimple_A_N  - select(18, 16, 16); Protected; "true";
//                testSimple_A_O  - select(18, 16, 16); Default;   "s";
//                testSimple_A_P  - select(18, 16, 16); Private;   "l";
//
//  "ClassB":
//            
//                testSimple_A_Q  - select(13, 20, 25); Public;    "myField"; ReplaceAllOccurences=true;
//                testSimple_A_R  - select(13, 20, 25); Protected; "myField"; ReplaceAllOccurences=true;
//                testSimple_A_S  - select(13, 20, 25); Default;   "myField"; ReplaceAllOccurences=true;
//                testSimple_A_T  - select(13, 20, 25); Private;   "myField"; ReplaceAllOccurences=true;
//            
//                testSimple_A_U  - select(18, 22, 27); Public;    "myField"; ReplaceAllOccurences=true;
//                testSimple_A_V  - select(18, 22, 27); Protected; "myField"; ReplaceAllOccurences=true;
//                testSimple_A_W  - select(18, 22, 27); Default;   "myField"; ReplaceAllOccurences=true;
//                testSimple_A_X  - select(18, 22, 27); Default;   "myField"; ReplaceAllOccurences=true;
//            
//                testSimple_A_Y  - select(22, 26, 32); Public;    "myField"; ReplaceAllOccurences=true;
//                testSimple_A_Z  - select(22, 26, 32); Protected; "myField"; ReplaceAllOccurences=true;
//                testSimple_B_A  - select(22, 26, 32); Public;    "myField"; ReplaceAllOccurences=true;
//                testSimple_B_B  - select(22, 26, 32); Protected; "myField"; ReplaceAllOccurences=true;
//            
//                testSimple_B_C  - select(22, 26, 32); Public;    "myField"; ReplaceAllOccurences=false;
//                testSimple_B_D  - select(22, 26, 32); Protected; "myField"; ReplaceAllOccurences=false;
//                testSimple_B_E  - select(22, 26, 32); Public;    "myField"; ReplaceAllOccurences=false;
//                testSimple_B_F  - select(22, 26, 32); Protected; "myField"; ReplaceAllOccurences=false;
//
//  ILLEGAL SELLECTION:
//                testSimple_B_G  - select(8, 25, 40); 
//                testSimple_B_H  - select(18, 16, 27);
//                testSimple_B_I  - select(21, 23);
//                testSimple_B_J  - select(21, 15, 25);    
            
            	IntroduceConstantOperator ico = null;
                ErrorOperator eo = null;
                String report = "";
                
                boolean debugMode = false;
            
                EditorOperator editor;
                
                // open source file
                switch(c){
                    default:
                        openSourceFile("introduceConstant", "ClassA");
                        editor = new EditorOperator("ClassA.java");
                        break;
                    case testSimple_A_Q:
                    case testSimple_A_R:
                    case testSimple_A_S:
                    case testSimple_A_T:
                        
                    case testSimple_A_U:
                    case testSimple_A_V:
                    case testSimple_A_W:
                    case testSimple_A_X:
                        
                    case testSimple_A_Y:
                    case testSimple_A_Z:
                    case testSimple_B_A:
                    case testSimple_B_B:
                        
                    case testSimple_B_C:
                    case testSimple_B_D:
                    case testSimple_B_E:
                    case testSimple_B_F:
                        
                    case testSimple_B_G:
                    case testSimple_B_H:
                    case testSimple_B_I:
                    case testSimple_B_J:
                        openSourceFile("introduceConstant", "ClassB");
                        editor = new EditorOperator("ClassB.java");
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
                    case testSimple_A_D: editor.setCaretPosition(12, 1); break;
                        
                    case testSimple_A_E:
                    case testSimple_A_F:
                    case testSimple_A_G:
                    case testSimple_A_H: editor.setCaretPosition(9, 1); break;
                        
                    case testSimple_A_I:
                    case testSimple_A_J:
                    case testSimple_A_K:
                    case testSimple_A_L:
                        
                    case testSimple_A_M:
                    case testSimple_A_N:
                    case testSimple_A_O:
                    case testSimple_A_P: editor.setCaretPosition(16, 1); break;
                        
                    case testSimple_A_Q:
                    case testSimple_A_R:
                    case testSimple_A_S:
                    case testSimple_A_T: editor.setCaretPosition(13, 1); break;
                        
                    case testSimple_A_U:
                    case testSimple_A_V:
                    case testSimple_A_W:
                    case testSimple_A_X: editor.setCaretPosition(18, 1); break;
                        
                    case testSimple_A_Y:
                    case testSimple_A_Z:
                    case testSimple_B_A:
                    case testSimple_B_B:
                        
                    case testSimple_B_C:
                    case testSimple_B_D:
                    case testSimple_B_E:
                    case testSimple_B_F: editor.setCaretPosition(22, 1); break;
                        
                    case testSimple_B_G: editor.setCaretPosition(8,  1); break;
                    case testSimple_B_H: editor.setCaretPosition(18, 1); break;
                    case testSimple_B_I: editor.setCaretPosition(21, 1); break;
                    case testSimple_B_J: editor.setCaretPosition(21, 1); break;
                }
                
                // select predefined part of code
                switch(c){
                    case testSimple_A_A:
                    case testSimple_A_B:
                    case testSimple_A_C:
                    case testSimple_A_D: editor.select(12, 24, 29); break;
                        
                    case testSimple_A_E:
                    case testSimple_A_F:
                    case testSimple_A_G:
                    case testSimple_A_H: editor.select(9, 16, 21); break;
                        
                    case testSimple_A_I:
                    case testSimple_A_J:
                    case testSimple_A_K:
                    case testSimple_A_L:
                        
                    case testSimple_A_M:
                    case testSimple_A_N:
                    case testSimple_A_O:
                    case testSimple_A_P: editor.select(18, 16, 16); break;
                        
                    case testSimple_A_Q:
                    case testSimple_A_R:
                    case testSimple_A_S:
                    case testSimple_A_T: editor.select(13, 20, 25); break;
                        
                    case testSimple_A_U:
                    case testSimple_A_V:
                    case testSimple_A_W:
                    case testSimple_A_X: editor.select(18, 22, 27); break;
                        
                    case testSimple_A_Y:
                    case testSimple_A_Z:
                    case testSimple_B_A:
                    case testSimple_B_B:
                        
                    case testSimple_B_C:
                    case testSimple_B_D:
                    case testSimple_B_E:
                    case testSimple_B_F: editor.select(22, 26, 32); break;
                        
                    case testSimple_B_G: editor.select(8, 25, 40);  break;
                    case testSimple_B_H: editor.select(18, 16, 27); break;
                    case testSimple_B_I: editor.select(21, 23);     break;
                    case testSimple_B_J: editor.select(21, 15, 25); break;
                }
                
                if(debugMode) new EventTool().waitNoEvent(1000);
                
                new EventTool().waitNoEvent(1000);
                
                // call Reafctor > Introduce method
                switch(c){
                    default: new RefactorIntroduceConstantAction().performPopup(editor); break;
                }
                                               
                // catch Introduce method dialog
                switch(c){
                    case testSimple_B_G:
                    case testSimple_B_H:
                    case testSimple_B_I:
                    case testSimple_B_J: break;
                    default: ico = new IntroduceConstantOperator(); break;
                }
                
                if(debugMode) new EventTool().waitNoEvent(2000);
                
                // type new name
                switch(c){
                    case testSimple_A_M:
                        ico.getNewName().pushKey(KeyEvent.VK_BACK_SPACE);
                        ico.getNewName().pushKey(KeyEvent.VK_BACK_SPACE);
                        ico.getNewName().pushKey(KeyEvent.VK_BACK_SPACE); break;
                    case testSimple_A_N: ico.getNewName().typeText("true"); break;
                    case testSimple_A_O: ico.getNewName().typeText("s"); break;
                    case testSimple_A_P: ico.getNewName().typeText("l"); break;
                        
                    case testSimple_B_G:
                    case testSimple_B_H:
                    case testSimple_B_I:
                    case testSimple_B_J: break;
                        
                    default: ico.getNewName().typeText("myField"); break;
                }
                
                // select access option 
                switch(c){
                    case testSimple_A_A:
                    case testSimple_A_E:
                    case testSimple_A_I:
                    case testSimple_A_M:
                        
                    case testSimple_A_Q:
                    case testSimple_A_U:
                    case testSimple_A_Y:
                    case testSimple_B_C: ico.getRadPublic().setSelected(true);    break;   // Public
                        
                    case testSimple_A_B:
                    case testSimple_A_F:
                    case testSimple_A_J:
                    case testSimple_A_N: 
                        
                    case testSimple_A_R:
                    case testSimple_A_V:
                    case testSimple_A_Z:
                    case testSimple_B_D: ico.getRadProtected().setSelected(true); break;   // Protected
                        
                    case testSimple_A_C:
                    case testSimple_A_G:
                    case testSimple_A_K:
                    case testSimple_A_O: 
                        
                    case testSimple_A_S:
                    case testSimple_A_W:
                    case testSimple_B_A:
                    case testSimple_B_E: ico.getRadDefault().setSelected(true);   break;   // Default
                        
                    case testSimple_A_D:
                    case testSimple_A_H:
                    case testSimple_A_L:
                    case testSimple_A_P: 
                        
                    case testSimple_A_T:
                    case testSimple_A_X:
                    case testSimple_B_B:
                    case testSimple_B_F: ico.getRadPrivate().setSelected(true);   break;   // Private
                }
                
                if(debugMode) new EventTool().waitNoEvent(2000);
                
                // Replace All Occurences
                switch(c){
                    case testSimple_A_A:
                    case testSimple_A_B:
                    case testSimple_A_C:
                    case testSimple_A_D:
                        
                    case testSimple_B_C:
                    case testSimple_B_D:
                    case testSimple_B_E:
                    case testSimple_B_F:
                        // ReplaceAllOccurences = FALSE
                        if(ico.getAlsoReplace().isSelected()) ico.getAlsoReplace().doClick();
//                        ico.getAlsoReplace().setSelected(false);
                        break;
                    case testSimple_A_E:
                    case testSimple_A_F:
                    case testSimple_A_G:
                    case testSimple_A_H:
                        
                    case testSimple_A_I:
                    case testSimple_A_J:
                    case testSimple_A_K:
                    case testSimple_A_L:
                        
                    case testSimple_A_Q:
                    case testSimple_A_R:
                    case testSimple_A_S:
                    case testSimple_A_T:
                        
                    case testSimple_A_U:
                    case testSimple_A_V:
                    case testSimple_A_W:
                    case testSimple_A_X:
                        
                    case testSimple_A_Y:
                    case testSimple_A_Z:
                    case testSimple_B_A:
                    case testSimple_B_B:
                        // ReplaceAllOccurences = TRUE
                        if(!ico.getAlsoReplace().isSelected()) ico.getAlsoReplace().doClick();
                        break;
                }
                
                if(debugMode) new EventTool().waitNoEvent(3000);
                
                // check whenever Ok btn is disable
                switch(c){                       
                    case testSimple_A_M:
                    case testSimple_A_N:
                    case testSimple_A_O:
                    case testSimple_A_P:
                        if (ico.btOK().isEnabled()) report = "// OK BTN IS ENABLED ALTHOUGH REFACTORING IS UNVAILABLE!!!";
                        break;
                }
                
                // perform refactor by pressing Ok / Cancel
                switch(c){
                    case testSimple_A_M:
                    case testSimple_A_N:
                    case testSimple_A_O:
                    case testSimple_A_P: ico.cancel(); break;
                    
                    case testSimple_B_G:
                    case testSimple_B_H:
                    case testSimple_B_I:
                    case testSimple_B_J: break;
                        
                    default: ico.ok(); break;
                }
                
                if(debugMode) new EventTool().waitNoEvent(3000);
                                
                // catch Error dialog
                switch(c){
                    case testSimple_B_G:
                    case testSimple_B_H:
                    case testSimple_B_I:
                    case testSimple_B_J:
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
