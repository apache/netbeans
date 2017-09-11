/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */

package org.netbeans.modules.test.refactoring;


import java.awt.Component;
import java.awt.event.KeyEvent;
import javax.swing.JPopupMenu;
import junit.framework.Test;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jemmy.ComponentChooser;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.operators.JPopupMenuOperator;
import org.netbeans.modules.test.refactoring.actions.RefactorIntroduceMethodAction;
import org.netbeans.modules.test.refactoring.operators.ErrorOperator;
import org.netbeans.modules.test.refactoring.operators.IntroduceMethodOperator;


/**
 @author (stanislav.sazonov@oracle.com)
 */
public class IntroduceMethodTest extends ModifyingRefactoring {


        private enum currentTest { testSimple_A,
                                   testSimple_B,
                                   testSimple_C,
                                   testSimple_D,
                                   testCancel,
                                   testReturn_A,
                                   testReturn_B,
                                   testEndingWithBreak,
                                   testEndingWithReturn,
                                   testEndingWithContinue,
                                   testIllegal_A,
                                   testIllegal_B,
                                   testIllegal_C,
                                   testIllegal_D,
                                   testIllegal_E,
                                   testIllegal_F,
                                   testGlobalVar,
                                   testStaticMethod
        };

	public IntroduceMethodTest(String name){
		super(name);
	}

	public static Test suite(){
		return JellyTestCase.emptyConfiguration().
				addTest(RenameTest.class, "testSimple_A").
				addTest(RenameTest.class, "testSimple_B").
				addTest(RenameTest.class, "testSimple_C").
				addTest(RenameTest.class, "testSimple_D").
				addTest(RenameTest.class, "testCancel").
				addTest(RenameTest.class, "testReturn_A").
                                addTest(RenameTest.class, "testReturn_B").
                                addTest(RenameTest.class, "testEndingWithBreak").
                                addTest(RenameTest.class, "testEndingWithReturn").
                                addTest(RenameTest.class, "testEndingWithContinue").
                                addTest(RenameTest.class, "testIllegal_A").
                                addTest(RenameTest.class, "testIllegal_B").
                                addTest(RenameTest.class, "testIllegal_C").
//                                addTest(RenameTest.class, "testIllegal_D").
                                addTest(RenameTest.class, "testIllegal_E").
                                addTest(RenameTest.class, "testIllegal_R").
                                addTest(RenameTest.class, "testGlobalVar").
                                addTest(RenameTest.class, "testStaticMethod").
				suite();
                
                /*
                    30.10.2013 (testIllegal_D)
                    Still don't work
                */
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
	
	public void testCancel(){
		performIntroduvceMethod(currentTest.testCancel);
	}
	
	public void testReturn_A(){
		performIntroduvceMethod(currentTest.testReturn_A);
	}
        
	public void testReturn_B(){
		performIntroduvceMethod(currentTest.testReturn_B);
	}
        
	public void testEndingWithBreak(){
		performIntroduvceMethod(currentTest.testEndingWithBreak);
	}
        
	public void testEndingWithReturn(){
		performIntroduvceMethod(currentTest.testEndingWithReturn);
	}
                
	public void testEndingWithContinue(){
		performIntroduvceMethod(currentTest.testEndingWithContinue);
	}
        
	public void testIllegal_A(){
		performIntroduvceMethod(currentTest.testIllegal_A);
	}
        
	public void testIllegal_B(){
		performIntroduvceMethod(currentTest.testIllegal_B);
	}
        
	public void testIllegal_C(){
		performIntroduvceMethod(currentTest.testIllegal_C);
	}
        
//        public void testIllegal_D(){
//		performIntroduvceMethod(currentTest.testIllegal_D);
//	}
        
	public void testIllegal_E(){
		performIntroduvceMethod(currentTest.testIllegal_E);
	}
                
	public void testIllegal_F(){
		performIntroduvceMethod(currentTest.testIllegal_F);
	}
        
	public void testGlobalVar(){
		performIntroduvceMethod(currentTest.testGlobalVar);
	}
        
	public void testStaticMethod(){
		performIntroduvceMethod(currentTest.testStaticMethod);
	}
        
        private void performIntroduvceMethod(currentTest c){
                boolean debug = false;
            
            	IntroduceMethodOperator imo = null;
                ErrorOperator eo = null;
                String report = "";
            
                // open source file
		openSourceFile("introduceMethod", "ClassA");
		EditorOperator editor = new EditorOperator("ClassA.java");
                
                if(debug) new EventTool().waitNoEvent(3000);
                
                // delete part of code
                switch(c){
                    case testEndingWithBreak:
                    case testEndingWithReturn:
                    case testEndingWithContinue: 
                        editor.setCaretPosition(62, 13);
                        editor.select(62, 13, 20);
                        editor.pushKey(KeyEvent.VK_BACK_SPACE); break;
                    case testIllegal_B: 
                    case testIllegal_C: 
                        editor.setCaretPosition(58, 17);
                        editor.select(58, 17, 24);
                        editor.pushKey(KeyEvent.VK_BACK_SPACE); break;
                }
                
                if(debug) new EventTool().waitNoEvent(3000);

                // type some text
                switch(c){
                    case testEndingWithBreak:
                    case testIllegal_B:          editor.insert("break"); break;
                    case testEndingWithReturn:   editor.insert("return"); break;
                    case testEndingWithContinue:
                    case testIllegal_C:          editor.insert("continue"); break;
                }
                
                // put carret on position
                switch(c){
                    case testSimple_A:
                    case testSimple_B:
                    case testSimple_C:
                    case testSimple_D:
                    case testCancel:    editor.setCaretPosition(37, 1); break;
                    case testReturn_A:  editor.setCaretPosition(45, 1); break;
                    case testReturn_B:  editor.setCaretPosition(47, 1); break;
                    case testIllegal_A: editor.setCaretPosition(42, 1); break;
                    case testIllegal_D:
                    case testIllegal_E:
                    case testIllegal_F: editor.setCaretPosition(47, 1); break;
                    case testGlobalVar: editor.setCaretPosition(73, 1); break;
                    case testStaticMethod: editor.setCaretPosition(83, 1); break;
                }
                
                // select predefined part of code
                switch(c){
                    case testSimple_A:
                    case testSimple_B:
                    case testSimple_C:
                    case testSimple_D:
                    case testCancel:             editor.select(37, 9, 32); break;
                    case testReturn_A:           editor.select(45, 9, 18); break;
                    case testReturn_B:           editor.select(47, 9, 23); break;
                    case testEndingWithBreak:
                    case testEndingWithReturn:
                    case testEndingWithContinue: editor.select(61, 62); break;
                    case testIllegal_A: editor.select(34, 35); break;
                    case testIllegal_B:
                    case testIllegal_C: editor.select(56, 59); break;
                    case testIllegal_D:
                    case testIllegal_E:
                    case testIllegal_F: editor.select(47, 9, 23); break;
                    case testGlobalVar: editor.select(73); break;
                    case testStaticMethod: editor.select(83); break;
                }
                
                if(debug) new EventTool().waitNoEvent(1000);
                
                // call Reafctor > Introduce method
                switch(c){
                    default: new RefactorIntroduceMethodAction().performPopup(editor); break;
                }
                
                if(debug) new EventTool().waitNoEvent(2000);
                               
                // catch Introduce method dialog
                switch(c){
                    case testIllegal_A:
                    case testIllegal_B:
                    case testIllegal_C: break;
                    default: imo = new IntroduceMethodOperator(); break;
                }
                
                // type new name
                switch(c){
                    case testIllegal_A:
                    case testIllegal_B:
                    case testIllegal_C: break;
                    case testIllegal_D: imo.getNewName().typeText("existing"); break;
                    case testIllegal_E: imo.getNewName().typeText(""); break;
                    case testIllegal_F: imo.getNewName().typeText("int"); break;
                    default: imo.getNewName().typeText("myMethod"); break;
                }
                
                // select access option 
                switch(c){
                    case testIllegal_A:
                    case testIllegal_B:
                    case testIllegal_C: break;
                    case testSimple_A:  imo.getRadPublic().setSelected(true);    break;
                    case testSimple_B:  imo.getRadProtected().setSelected(true); break;
                    case testSimple_C:  imo.getRadDefault().setSelected(true);   break;
                    case testSimple_D:  imo.getRadPrivate().setSelected(true);   break;
                    case testCancel:    imo.getRadPublic().setSelected(true);    break;
                    case testReturn_A:  imo.getRadPublic().setSelected(true);    break;
                    default:            imo.getRadDefault().setSelected(true);   break;
                }

                // select Replace All Occurences
                switch(c){
                    case testIllegal_A:
                    case testIllegal_B:
                    case testIllegal_C: break;
                    default: imo.getAlsoReplace().setSelected(false); break;
                }
                
                if(debug) new EventTool().waitNoEvent(1000);
                
                // check whenever Ok btn is disable
                switch(c){
                    case testIllegal_D:
                    case testIllegal_E:
                    case testIllegal_F:
                        if (imo.btOK().isEnabled()) report = "// OK BTN IS ENABLED ALTHOUGH REFACTORING IS UNVAILABLE!!!";
                        break;
                }
                
                // perform refactor by pressing Ok / Cancel
                switch(c){
                    case testIllegal_A:
                    case testIllegal_B:
                    case testIllegal_C: break;
                    case testIllegal_D:
                    case testIllegal_E:
                    case testIllegal_F:
                    case testCancel:    imo.cancel(); break;
                    default:            imo.ok();     break;
                }
                                
                // catch Error dialog
                switch(c){
                    case testIllegal_A:
                    case testIllegal_B:
                    case testIllegal_C:
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
                                
                if(debug) new EventTool().waitNoEvent(3000);

                // evalue result and discard changes
		ref(editor.getText());
		editor.closeDiscard();
	}
}
