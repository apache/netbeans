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

package org.netbeans.test.java.editor.actions;

import java.awt.event.KeyEvent;
import javax.swing.KeyStroke;
import junit.framework.Test;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.operators.JEditorPaneOperator;
import org.netbeans.junit.NbModuleSuite;

/**
 * Basic Navigation Actions Test class.
 * The base navigation actions can be found at:
 * http://editor.netbeans.org/doc/UserView/apdx_a_nshortcuts.html
 *
 * Test covers following actions:
 *
 * StandardNavigationActions:
 * -------------------------
 * caret-forward [RIGHT]
 * caret-backward [LEFT]
 * caret-down [DOWN]
 * caret-up [UP]
 * selection-forward [SHIFT-RIGHT]
 * selection-backward [SHIFT-LEFT]
 * selection-down [SHIFT-DOWN]
 * selection-up [SHIFT-UP]
 * caret-next-word [CTRL-RIGHT]
 * caret-previous-word [CTRL-LEFT]
 * selection-next-word [CTRL-SHIFT-RIGHT]
 * selection-previous-word [CTRL-SHIFT-LEFT]
 * page-down [PAGE_DOWN]
 * page-up [PAGE_UP]
 * selection-page-down [SHIFT-PAGE_DOWN]
 * selection-page-up [SHIFT-PAGE_UP]
 * caret-begin-line [HOME]
 * caret-end-line [END]
 * selection-begin-line [SHIFT-HOME]
 * selection-end-line [SHIFT-END]
 * caret-begin [CTRL-HOME]
 * caret-end [CTRL-END]
 * selection-begin [CTRL-SHIFT-HOME]
 * selection-end [CTRL-SHIFT-END]
 * caret-end-word [ALT-U E]
 * 
 * @author Martin Roskanin
 */
  public class JavaNavigationActionsTest extends JavaEditorActionsTestCase {

    private JEditorPaneOperator txtOper;
    private EditorOperator editor;
      
    /** Creates a new instance of Main */
    public JavaNavigationActionsTest(String testMethodName) {
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
        editor.setCaretPosition(caretPosToSet);
        txtOper.pushKey(key,mod);
        waitMaxMilisForValue(1500, getResolver(txtOper, etalon), Boolean.TRUE);
        int newCaretOffset = txtOper.getCaretPosition();
        if (checkSelection){
            int selectionStart = txtOper.getSelectionStart();
            int selectionEnd = txtOper.getSelectionEnd(); 
            if (selectionStart != Math.min(caretPosToSet, etalon) ||
                    selectionEnd != Math.max(caretPosToSet, etalon)){
                String keyString = KeyStroke.getKeyStroke(key, mod).toString();
//                System.out.println(keyString+": Action failed: [etalon/newCaretOffset/selectionStart/selectionEnd]: ["+etalon+"/"+
//                        newCaretOffset+"/"+selectionStart+"/"+selectionEnd+"]");
                fail(keyString+": Action failed: [etalon/newCaretOffset/selectionStart/selectionEnd]: ["+etalon+"/"+
                        newCaretOffset+"/"+selectionStart+"/"+selectionEnd+"]");
            }
        }else{
            if (etalon != newCaretOffset){
                String keyString = KeyStroke.getKeyStroke(key, mod).toString();
//                System.out.println(keyString+": Action failed: [etalon/newCaretOffset]: ["+etalon+"/"+
//                        newCaretOffset+"]");
                fail(keyString+": Action failed: [etalon/newCaretOffset]: ["+etalon+"/"+
                        newCaretOffset+"]");
            }
        }
    }    
    
    public void testStandardNavigationActions(){
        openDefaultProject();
        openDefaultSampleFile();
        try {
        
            editor = getDefaultSampleEditorOperator();
            txtOper = editor.txtEditorPane();            
            new EventTool().waitNoEvent(2000);
            // -------- test  RIGHT action ---
            checkActionByKeyStroke(KeyEvent.VK_RIGHT, 0, 92, 93, false);

            // -------- test  LEFT action ---
            checkActionByKeyStroke(KeyEvent.VK_LEFT, 0, 93, 92, false);
            

            // -------- test DOWN action ---
            // set caret at 10,14
            checkActionByKeyStroke(KeyEvent.VK_DOWN, 0, 272, 277, false);

            // -------- test UP action ---
            // set caret at 10,14
            checkActionByKeyStroke(KeyEvent.VK_UP, 0, 272, 266, false);

            // -------- test  select RIGHT action ---
            checkActionByKeyStroke(KeyEvent.VK_RIGHT, KeyEvent.SHIFT_DOWN_MASK, 92, 93, true);

            // -------- test  select LEFT action ---
            checkActionByKeyStroke(KeyEvent.VK_LEFT, KeyEvent.SHIFT_DOWN_MASK, 93, 92, true);

            // -------- test select DOWN action ---
            // set caret at 10,14
            checkActionByKeyStroke(KeyEvent.VK_DOWN, KeyEvent.SHIFT_DOWN_MASK, 272, 277, true);

            // -------- test select UP action ---
            // set caret at 10,14
            checkActionByKeyStroke(KeyEvent.VK_UP, KeyEvent.SHIFT_DOWN_MASK, 272, 266, true);

            // -------- test caret-next-word action ---
            // set caret at 1,12
            checkActionByKeyStroke(KeyEvent.VK_RIGHT, KeyEvent.CTRL_DOWN_MASK, 11, 12, false);

            // -------- test caret-previous-word action -----
            checkActionByKeyStroke(KeyEvent.VK_LEFT, KeyEvent.CTRL_DOWN_MASK, 34, 31, false);

            // -------- test selection-next-word action ---
            // set caret at 1,12
            checkActionByKeyStroke(KeyEvent.VK_RIGHT, KeyEvent.CTRL_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK, 11, 12, true);

            // -------- test selection-previous-word action -----
            checkActionByKeyStroke(KeyEvent.VK_LEFT, KeyEvent.CTRL_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK, 34, 31, true);

            // -------- test page-down action -------
            editor.setCaretPosition(5,1);
            int caretDown = txtOper.getCaretPosition();
            editor.setCaretPosition(1,1);
            int pageDownStart = txtOper.getCaretPosition();
            txtOper.pushKey(KeyEvent.VK_PAGE_DOWN);
            int pageDownEnd = txtOper.getCaretPosition();
            if (pageDownEnd < caretDown){
                fail("PAGE_DOWN failed");
            }


            // -------- test page-up action -------
            editor.setCaretPosition(32,1);
            int caretUp = txtOper.getCaretPosition();
            editor.setCaretPosition(38,1);
            int pageUpStart = txtOper.getCaretPosition();
            txtOper.pushKey(KeyEvent.VK_PAGE_UP);
            int pageUpEnd = txtOper.getCaretPosition();
            if (pageUpEnd > caretUp){
                fail("PAGE_UP failed");
            }

            // -------- test page-down action -------
            checkActionByKeyStroke(KeyEvent.VK_PAGE_DOWN, KeyEvent.SHIFT_DOWN_MASK, pageDownStart, pageDownEnd, true);

            // -------- test page-up action -------
            checkActionByKeyStroke(KeyEvent.VK_PAGE_UP, KeyEvent.SHIFT_DOWN_MASK, pageUpStart, pageUpEnd, true);

            // -------- test caret-begin-line action -------
            checkActionByKeyStroke(KeyEvent.VK_HOME, 0, 18, 0, false);

            // -------- test caret-end-line action -------
            checkActionByKeyStroke(KeyEvent.VK_END, 0, 18, 72, false);

            // -------- test selection-begin-line action -------
            checkActionByKeyStroke(KeyEvent.VK_HOME, KeyEvent.SHIFT_DOWN_MASK, 18, 0, true);

            // -------- test selection-end-line action -------
            checkActionByKeyStroke(KeyEvent.VK_END, KeyEvent.SHIFT_DOWN_MASK, 18, 72, true);

            // -------- test caret-begin action -------
            checkActionByKeyStroke(KeyEvent.VK_HOME, KeyEvent.CTRL_DOWN_MASK, 18, 0, false);

            // -------- test caret-end action -------
            checkActionByKeyStroke(KeyEvent.VK_END, KeyEvent.CTRL_DOWN_MASK, 18, txtOper.getDocument().getLength(), false);

            // -------- test selection-begin action -------
            checkActionByKeyStroke(KeyEvent.VK_HOME, KeyEvent.CTRL_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK, 18, 0, true);

            // -------- test selection-end action -------
            checkActionByKeyStroke(KeyEvent.VK_END, KeyEvent.CTRL_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK, 18, txtOper.getDocument().getLength(), true);
            
        } finally {
            closeFileWithDiscard();
        }
            
    }
    
     public static Test suite() {
         return NbModuleSuite.create(
                 NbModuleSuite.createConfiguration(JavaNavigationActionsTest.class).addTest(JavaNavigationActionsTest.class,"testStandardNavigationActions").enableModules(".*").clusters(".*"));
     }
    
    
}
