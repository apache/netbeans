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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
