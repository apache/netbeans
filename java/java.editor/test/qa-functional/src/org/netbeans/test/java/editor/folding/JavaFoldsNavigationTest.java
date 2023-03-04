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

package org.netbeans.test.java.editor.folding;

import java.awt.event.KeyEvent;
import javax.swing.KeyStroke;
import javax.swing.text.JTextComponent;
import junit.framework.Test;
import junit.textui.TestRunner;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.operators.JEditorPaneOperator;
import org.netbeans.jemmy.operators.JTextComponentOperator;
import org.netbeans.junit.NbModuleSuite;

/**
 * Test behavior of navigation through java code folds.
 *
 * Test covers following actions:
 * caret-forward [RIGHT]
 * caret-backward [LEFT]
 * caret-down [DOWN]
 * caret-up [UP]
 * selection-forward [SHIFT-RIGHT]
 * selection-backward [SHIFT-LEFT]
 * selection-down [SHIFT-DOWN]
 * selection-up [SHIFT-UP]
 * caret-begin-line [HOME]
 * caret-end-line [END]
 * selection-begin-line [SHIFT-HOME]
 * selection-end-line [SHIFT-END]
 *
 * Actions:
 * caret-next-word [CTRL-RIGHT]
 * caret-previous-word [CTRL-LEFT]
 * selection-next-word [CTRL-SHIFT-RIGHT]
 * selection-previous-word [CTRL-SHIFT-LEFT]
 * should be added to testcase after issue #47454 will be fixed
 *
 * @author Martin Roskanin
 */
  public class JavaFoldsNavigationTest extends JavaCodeFoldingTestCase {

    private JEditorPaneOperator txtOper;
    private EditorOperator editor;
     
    /** Creates a new instance of Main */
    public JavaFoldsNavigationTest(String testMethodName) {
        super(testMethodName);
    }
    
    private ValueResolver getResolver(final JEditorPaneOperator txtOper, final int etalon){
        ValueResolver resolver = new ValueResolver(){
            public Object getValue(){
                int newCaretPos = txtOper.getCaretPosition();
                return (newCaretPos == etalon) ? Boolean.TRUE : Boolean.FALSE;
            }
        };
        
        return resolver;
    }
    
    private void checkActionByKeyStroke(int key, int mod, int caretPosToSet, int etalon, boolean checkSelection){
        if (caretPosToSet == -1){
            caretPosToSet = txtOper.getCaretPosition();
        }else{
            editor.setCaretPosition(caretPosToSet);
            txtOper.getCaret().setMagicCaretPosition(null);
        }        
        txtOper.pushKey(key,mod);    
        waitMaxMilisForValue(3500, getResolver(txtOper, etalon), Boolean.TRUE);
        int newCaretOffset = txtOper.getCaretPosition();
        if (checkSelection){
            int selectionStart = txtOper.getSelectionStart();
            int selectionEnd = txtOper.getSelectionEnd(); 
            if (selectionStart != Math.min(caretPosToSet, etalon) ||
                    selectionEnd != Math.max(caretPosToSet, etalon)){
                String keyString = KeyStroke.getKeyStroke(key, mod).toString();
                fail(keyString+": Action failed: [etalon/newCaretOffset/selectionStart/selectionEnd]: ["+etalon+"/"+
                        newCaretOffset+"/"+selectionStart+"/"+selectionEnd+"]");
//                System.out.println(keyString+": Action failed: [etalon/newCaretOffset/selectionStart/selectionEnd]: ["+etalon+"/"+newCaretOffset+"/"+selectionStart+"/"+selectionEnd+"]");
            }
        }else{
            if (etalon != newCaretOffset){
                String keyString = KeyStroke.getKeyStroke(key, mod).toString();
                fail(keyString+": Action failed: [etalon/newCaretOffset]: ["+etalon+"/"+
                        newCaretOffset+"]");
//                System.out.println(keyString+": Action failed: [etalon/newCaretOffset]: ["+etalon+"/"+ newCaretOffset+"]");
            }
        }
    }
    
    public void testJavaFoldsNavigation(){
        openDefaultProject();
        openFile("Source Packages|java_code_folding.JavaFoldsNavigationTest", "testJavaFoldsNavigation");
        //openDefaultSampleFile();
        try {            
            editor = getDefaultSampleEditorOperator();
            JTextComponentOperator txtCompOper = new JTextComponentOperator(editor);
            JTextComponent target = (JTextComponent)txtCompOper.getSource();
            txtOper = editor.txtEditorPane();

            // wait max. 6 second for code folding initialization
            waitForFolding(target, 6000);

            //01 collapse initial comment fold. [ */|]
            // check caret left action
            collapseFoldAtCaretPosition(editor, 4, 4); // 4,4 -caret offset 70
            
            // check left
            checkActionByKeyStroke(KeyEvent.VK_LEFT, 0, 70, 0, false);
            
            //check selectin
            checkActionByKeyStroke(KeyEvent.VK_LEFT, KeyEvent.SHIFT_DOWN_MASK, 70, 0, true);
            
            // check caret right action
            checkActionByKeyStroke(KeyEvent.VK_RIGHT, 0, 0, 70, false);
            
            // check caret right action, selection
            checkActionByKeyStroke(KeyEvent.VK_RIGHT, KeyEvent.SHIFT_DOWN_MASK, 0, 70, true);
            
            // check home action
            checkActionByKeyStroke(KeyEvent.VK_HOME, 0, 70, 0, false);
            
            // check home action, selection
            checkActionByKeyStroke(KeyEvent.VK_HOME, KeyEvent.SHIFT_DOWN_MASK, 70, 0, true);

            // check end action
            checkActionByKeyStroke(KeyEvent.VK_END, 0, 0, 70, false);
            
            // check end action, selection
            checkActionByKeyStroke(KeyEvent.VK_END, KeyEvent.SHIFT_DOWN_MASK, 0, 70, true);
            
            // check up action
            checkActionByKeyStroke(KeyEvent.VK_UP, 0, 71, 0, false);
            
            // check up action, selection
            checkActionByKeyStroke(KeyEvent.VK_UP, KeyEvent.SHIFT_DOWN_MASK, 71, 0, true);
            
            // check down action
            checkActionByKeyStroke(KeyEvent.VK_DOWN, 0, 0, 71, false);
            
            // check down action, selection
            checkActionByKeyStroke(KeyEvent.VK_DOWN, KeyEvent.SHIFT_DOWN_MASK, 0, 71, true);

            // checking end of fold
            // check up action
            checkActionByKeyStroke(KeyEvent.VK_UP, 0, 84, 70, false);
            
            // check up action, selection
            checkActionByKeyStroke(KeyEvent.VK_UP, KeyEvent.SHIFT_DOWN_MASK, 84, 70, true);
            
            // check down action
            checkActionByKeyStroke(KeyEvent.VK_DOWN, 0, 70, 83, false);
            
            // check down action, selection
            checkActionByKeyStroke(KeyEvent.VK_DOWN, KeyEvent.SHIFT_DOWN_MASK, 70, 83, true);
            
            // check magic position
            checkActionByKeyStroke(KeyEvent.VK_UP, 0, 83, 70, false);
            checkActionByKeyStroke(KeyEvent.VK_DOWN, 0, -1, 83, false);
                                                
            
            // ------------------------------------------------------------------------
            
            
            // check actions on one-line fold
            collapseFoldAtCaretPosition(editor, 25, 13); // 25,13 - caret offset 422

            
            // check left
            checkActionByKeyStroke(KeyEvent.VK_LEFT, 0, 454, 414, false);
            
            //check selectin
            checkActionByKeyStroke(KeyEvent.VK_LEFT, KeyEvent.SHIFT_DOWN_MASK, 454, 414, true);
            
            // check caret right action
            checkActionByKeyStroke(KeyEvent.VK_RIGHT, 0, 414, 454, false);
            
            // check caret right action, selection
            checkActionByKeyStroke(KeyEvent.VK_RIGHT, KeyEvent.SHIFT_DOWN_MASK, 414, 454, true);
            
            // check home action
            checkActionByKeyStroke(KeyEvent.VK_HOME, 0, 454, 414, false);
            
            // check home action, selection
            checkActionByKeyStroke(KeyEvent.VK_HOME, KeyEvent.SHIFT_DOWN_MASK, 454, 414, true);

            // check end action
            checkActionByKeyStroke(KeyEvent.VK_END, 0, 414, 454, false);
            
            // check end action, selection
            checkActionByKeyStroke(KeyEvent.VK_END, KeyEvent.SHIFT_DOWN_MASK, 414, 454, true);
            
            // check up action
            checkActionByKeyStroke(KeyEvent.VK_UP, 0, 459, 414, false);
            
            // check up action, selection
            checkActionByKeyStroke(KeyEvent.VK_UP, KeyEvent.SHIFT_DOWN_MASK, 459, 414, true);
            
            // check down action
            checkActionByKeyStroke(KeyEvent.VK_DOWN, 0, 414, 459, false);
            
            // check down action, selection
            checkActionByKeyStroke(KeyEvent.VK_DOWN, KeyEvent.SHIFT_DOWN_MASK, 414, 459, true);

            // checking end of fold
            // check up action
            new EventTool().waitNoEvent(2000);
            checkActionByKeyStroke(KeyEvent.VK_UP, 0, 505, 454, false);
            new EventTool().waitNoEvent(2000);
            // check up action, selection
            checkActionByKeyStroke(KeyEvent.VK_UP, KeyEvent.SHIFT_DOWN_MASK, 505, 454, true);
                        
            //----------------------------------------------------------------
            //check multi fold on line
                       
            collapseFoldAtCaretPosition(editor, 36, 86); // 36,84 -caret offset 920
                                   
            // check left
            checkActionByKeyStroke(KeyEvent.VK_LEFT, 0, 920, 917, false);
                       
            //check selectin
            checkActionByKeyStroke(KeyEvent.VK_LEFT, KeyEvent.SHIFT_DOWN_MASK, 920, 917, true);
            
            // check caret right action
            checkActionByKeyStroke(KeyEvent.VK_RIGHT, 0, 917, 920, false);
            
            // check caret right action, selection
            checkActionByKeyStroke(KeyEvent.VK_RIGHT, KeyEvent.SHIFT_DOWN_MASK, 917, 920, true);
            
            // check home action
            checkActionByKeyStroke(KeyEvent.VK_HOME, 0, 920, 839, false);
            
            // check home action, selection
            checkActionByKeyStroke(KeyEvent.VK_HOME, KeyEvent.SHIFT_DOWN_MASK, 920, 839, true);

            // check end action
            checkActionByKeyStroke(KeyEvent.VK_END, 0, 917, 949, false);
            
            // check end action, selection
            checkActionByKeyStroke(KeyEvent.VK_END, KeyEvent.SHIFT_DOWN_MASK, 917, 949, true);
                                    
            // check up action
            checkActionByKeyStroke(KeyEvent.VK_UP, 0, 1032, 917, false);
            
            // check up action, selection
            checkActionByKeyStroke(KeyEvent.VK_UP, KeyEvent.SHIFT_DOWN_MASK, 1032, 917, true);
            
            // check down action
            checkActionByKeyStroke(KeyEvent.VK_DOWN, 0, 917, 1032, false);
            
            // check down action, selection
            checkActionByKeyStroke(KeyEvent.VK_DOWN, KeyEvent.SHIFT_DOWN_MASK, 917, 1032, true);
             
            // checking end of fold
            // check up action
            checkActionByKeyStroke(KeyEvent.VK_UP, 0, 1038, 920, false);
            
            // check up action, selection
            checkActionByKeyStroke(KeyEvent.VK_UP, KeyEvent.SHIFT_DOWN_MASK, 1038, 920, true);
            
            // check down action
            checkActionByKeyStroke(KeyEvent.VK_DOWN, 0, 920, 1038, false);
            
            // check down action, selection
            checkActionByKeyStroke(KeyEvent.VK_DOWN, KeyEvent.SHIFT_DOWN_MASK, 920, 1038, true);
            
            // check magic position
            checkActionByKeyStroke(KeyEvent.VK_UP, 0, 1033, 917, false);
            checkActionByKeyStroke(KeyEvent.VK_DOWN, 0, -1, 1033, false);
            
            
        } finally{
            closeFileWithDiscard();    
        }
    }
    
    public static void main(String[] args) {
        TestRunner.run(JavaFoldsNavigationTest.class);
    }
    
      public static Test suite() {
          return NbModuleSuite.create(
                  NbModuleSuite.createConfiguration(JavaFoldsNavigationTest.class).enableModules(".*").clusters(".*"));
      }

    
}
