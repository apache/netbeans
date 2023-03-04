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
public class EncapsulateFieldTest extends ModifyingRefactoring {


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
                                   nothing
        };

	public EncapsulateFieldTest(String name){
		super(name);
	}

	public static Test suite(){

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
				addTest(RenameTest.class, "testSimple_A_O").
				addTest(RenameTest.class, "testSimple_A_P").
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
                        
	public void testSimple_A_O(){
		performIntroduvceMethod(currentTest.testSimple_A_O);
	}
                        
	public void testSimple_A_P(){
		performIntroduvceMethod(currentTest.testSimple_A_P);
	}
                        
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
                
    private void performIntroduvceMethod(currentTest c){               
            
            EncapsulateFieldOperator efo = null;
            ErrorOperator eo = null;
            String report = "";

            boolean debugMode = false;

            EditorOperator editor;

            // open source file
            String curClass = "";
             switch(c){
                case testSimple_A_A: curClass = "Class_A_A";  break;
                case testSimple_A_B: curClass = "Class_A_B";  break;
                case testSimple_A_C: curClass = "Class_A_C";  break;
                case testSimple_A_D: curClass = "Class_A_D";  break;
                case testSimple_A_E: curClass = "Class_A_E";  break;
                case testSimple_A_F: curClass = "Class_A_F";  break;
                case testSimple_A_G: curClass = "Class_A_G";  break;
                case testSimple_A_H: curClass = "Class_A_H";  break;
                case testSimple_A_I: curClass = "Class_A_I";  break;
                case testSimple_A_J: curClass = "Class_A_J";  break;
                case testSimple_A_K: curClass = "Class_A_K";  break;
                case testSimple_A_L: curClass = "Class_A_L";  break;
                case testSimple_A_M: curClass = "Class_A_M";  break;
                case testSimple_A_N: curClass = "Class_A_N";  break;
                case testSimple_A_O: curClass = "Class_A_O";  break;
                case testSimple_A_P: curClass = "Class_A_P";  break;
                case testSimple_A_Q: curClass = "Class_A_Q";  break;
                case testSimple_A_R: curClass = "Class_A_R";  break;
                case testSimple_A_S: curClass = "Class_A_S";  break;
                case testSimple_A_T: curClass = "Class_A_T";  break;
                case testSimple_A_U: curClass = "Class_A_U";  break;
                case testSimple_A_V: curClass = "Class_A_V";  break;
                case testSimple_A_W: curClass = "Class_A_W";  break;
                case testSimple_A_X: curClass = "Class_A_X";  break;
                case testSimple_A_Y: curClass = "Class_A_Y";  break;
                case testSimple_A_Z: curClass = "Class_A_Z";  break;
                case testSimple_B_A: curClass = "Class_B_A";  break;
            }

             // open source file
            switch(c){
                default:
                    openSourceFile("encapsulateField", curClass);
                    editor = new EditorOperator(curClass + ".java");
                    break;
            }

            if(debugMode) new EventTool().waitNoEvent(2000);

            // put carret on position
            switch(c){
                case testSimple_A_A:
                case testSimple_A_B:
                case testSimple_A_C:
                case testSimple_A_D:
                case testSimple_A_E:
                case testSimple_A_F:
                    editor.setCaretPosition(6, 1);
                    editor.select(6, 21, 26);
                    break;
                case testSimple_A_G:
                case testSimple_A_H:
                case testSimple_A_I:
                    editor.setCaretPosition(52, 1);
                    editor.select(52, 73, 78);
                    break;
                case testSimple_A_J:
                case testSimple_A_K:
                case testSimple_A_L:
                    editor.setCaretPosition(57, 1);
                    editor.select(57, 73, 78);
                    break;
                case testSimple_A_M:
                case testSimple_A_N:
                case testSimple_A_O:
                case testSimple_A_P:
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
                    editor.setCaretPosition(52, 1);
                    editor.select(52, 73, 78);
                    break;
            }

            if(debugMode) new EventTool().waitNoEvent(1000);

            // call Reafctor > Introduce parameter
            switch(c){
                default:
                    new EncapsulateFieldAction().performPopup(editor);
                    break;
            }

            // catch Introduce method dialog
            switch(c){
                case nothing: break;
                default:
                    efo = new EncapsulateFieldOperator();
                    break;
            }

            new EventTool().waitNoEvent(500);
            
            // Insert Point
            switch(c){
                case testSimple_A_G:
                case testSimple_A_H:
                case testSimple_A_I:
                    efo.setValueAt(0, 1, true);
                    efo.setValueAt(0, 3, true);
                    break;
                case testSimple_A_J:
                case testSimple_A_K:
                case testSimple_A_L:
                    efo.setValueAt(0, 1, true);
                    efo.setValueAt(0, 3, true);
//                    efo.setValueAt(0, 2, "setF1");
//                    efo.setValueAt(0, 3, "getF1");
                    efo.setValueAt(2, 1, true);
                    efo.setValueAt(2, 3, true);
//                    efo.setValueAt(2, 2, "setF3");
//                    efo.setValueAt(2, 3, "getF3");
                    efo.setValueAt(3, 1, true);
                    efo.setValueAt(3, 3, true);
//                    efo.setValueAt(0, 2, "setF4");
//                    efo.setValueAt(0, 3, "getF4");
                    break;
                case testSimple_A_M:
                case testSimple_A_N:
                case testSimple_A_O:
                case testSimple_A_P:
                case testSimple_A_Q:
                case testSimple_A_R:
                case testSimple_A_S:
                case testSimple_A_T:
                    break;
            }
            
            new EventTool().waitNoEvent(500);
            
            switch(c){
                case testSimple_A_A: efo.setInsertPoint("Default"); break;
                case testSimple_A_B: efo.setInsertPoint("First Method"); break;
                case testSimple_A_C: efo.setInsertPoint("Last Method"); break;
                case testSimple_A_D: efo.setInsertPoint("After m1(int field1) : void"); break;
                case testSimple_A_E: efo.setInsertPoint("After Class_A_E(int f)"); break;
                case testSimple_A_F: efo.setInsertPoint("After m2() : void"); break;
                default:
                    efo.setInsertPoint("Default");
                    break;
            }
            
            new EventTool().waitNoEvent(500);
            
            // Sort By
            switch(c){
                case testSimple_A_G: efo.setSortBy("Getter/Setter pairs"); break;
                case testSimple_A_H: efo.setSortBy("Getters then Setters"); break;
                case testSimple_A_I: efo.setSortBy("Method names"); break;
                default:
                    efo.setSortBy("Getter/Setter pairs");
                    break;
            }
            
            new EventTool().waitNoEvent(500);
            
            // Javadoc
            switch(c){
                case testSimple_A_J: efo.setJavadoc("Copy from field"); break;
                case testSimple_A_K: efo.setJavadoc("Create default comments"); break;
                case testSimple_A_L: efo.setJavadoc("None"); break;
                default:
                    efo.setJavadoc("None");
                    break;
            }
            
            new EventTool().waitNoEvent(500);
            
            // Accessors fields visibility
            switch(c){
                case testSimple_A_M: efo.setFieldsVisibility("<default>"); break;
                case testSimple_A_N: efo.setFieldsVisibility("private"); break;
                case testSimple_A_O: efo.setFieldsVisibility("protected"); break;
                case testSimple_A_P: efo.setFieldsVisibility("public"); break;
                default:
                    efo.setFieldsVisibility("private");
                    break;
            }
            
            // Accessors visibility
            switch(c){
                case testSimple_A_Q: efo.setAccessorsVisibility("<default>"); break;
                case testSimple_A_R: efo.setAccessorsVisibility("private"); break;
                case testSimple_A_S: efo.setAccessorsVisibility("protected"); break;
                case testSimple_A_T: efo.setAccessorsVisibility("public"); break;
                default:
                    efo.setAccessorsVisibility("public");
                    break;
            }
            
            // Use Accessors Even When Field Is Accessible
            switch(c){
                case testSimple_A_U:
                    efo.setUseAccessorsEvenWhenFieldIsAccessible(false);
                    break;
                default:
                    efo.setUseAccessorsEvenWhenFieldIsAccessible(true);
                    break;
            }
            
            // Generate Property ChangeS upport
            switch(c){
                case testSimple_A_V:
                case testSimple_A_W:
                    efo.setGeneratePropertyChangeSupport(true);
                    new EventTool().waitNoEvent(500);
                    break;
                default:
                    efo.setGeneratePropertyChangeSupport(false);
                    break;
            }
            
            
            // Select {All, None, Getters, Setters}
            switch(c){
                case testSimple_A_Y:
                    efo.selectNone();
                    new EventTool().waitNoEvent(500);
                    efo.selectGetters();
                    break;
                case testSimple_A_Z:
                    efo.selectNone();
                    new EventTool().waitNoEvent(500);
                    efo.selectSetters();
                    break;
                case testSimple_B_A:
                    efo.selectNone();
                    new EventTool().waitNoEvent(500);
                    efo.selectAll();
                    break;
                default:
                    break;
            }
            
            // Generate Vetoable Change Support
            switch(c){
                case testSimple_A_W:
                    efo.setGenerateVetoableChangeSupport(true);
                    break;
                case nothing:
                    efo.setGenerateVetoableChangeSupport(false);
                    break;
            }
            
            if(debugMode) new EventTool().waitNoEvent(2000);
            
            switch(c){
                case testSimple_A_X:
                    efo.cancel();
                    break;
                default:
                    efo.refactor();
                    break;
            }

            if(debugMode) new EventTool().waitNoEvent(3000);

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
