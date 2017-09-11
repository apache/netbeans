/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */

package org.netbeans.test.java.editor.remove;

import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.Enumeration;
import javax.swing.ListModel;
import javax.swing.text.AttributeSet;
import junit.framework.Test;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.MainWindowOperator;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.jemmy.operators.JListOperator;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.modules.java.editor.codegen.CodeDeleter;
import org.netbeans.spi.editor.highlighting.HighlightsSequence;
import org.netbeans.test.java.editor.lib.JavaEditorTestCase;

/**
 *
 * @author jprox
 */
public class RemoveSurroundingTest extends JavaEditorTestCase {

    private EditorOperator oper = null;
    
    public RemoveSurroundingTest(String testMethodName) {
        super(testMethodName);
    }
    private static final String TEST_FILE = "RemoveSurrounding";
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        openProject("java_editor_test");                
    }
    
    @Override
    protected void tearDown() throws Exception {                
        super.tearDown();        
    }
      
    private void setAndApply(int row, int col, int select) {
        try {
            openSourceFile("org.netbeans.test.java.editor.remove", TEST_FILE);
            oper = new EditorOperator(TEST_FILE);
            dumpAndSelect(row,col,select);                        
            compareGoldenFile();
        } catch (IOException ioe) {
            fail(ioe);
        } finally {
            if(oper!=null) oper.closeDiscard();
        }
    }
    
        
    private void dumpAndSelect(int row, int col, int select) {
        oper.setCaretPosition(row, col);
        oper.pressKey(KeyEvent.VK_BACK_SPACE, KeyEvent.ALT_DOWN_MASK);
        JDialogOperator jdo = new JDialogOperator(MainWindowOperator.getDefault());
        JListOperator jlo = new JListOperator(jdo);
        ListModel model = jlo.getModel();
        int i;
        for (i = 0; i < model.getSize(); i++) {
            Object item = model.getElementAt(i);
            if(item instanceof CodeDeleter) {
                CodeDeleter codeDeleter = (CodeDeleter) item;
                ref(codeDeleter.getDisplayName());                
                HighlightsSequence highlights = codeDeleter.getHighlight().getHighlights(0, oper.getText().length());
                while(highlights.moveNext()) {
                    ref(highlights.getStartOffset()+" "+highlights.getEndOffset());
                    AttributeSet attributes = highlights.getAttributes();
                    Enumeration<?> attributeNames = attributes.getAttributeNames();
                    while(attributeNames.hasMoreElements()) {
                        Object nextElement = attributeNames.nextElement();
                        ref(nextElement+" "+attributes.getAttribute(nextElement));
                    }
                }
            }            
        }
        if(select>-1) {
            jlo.selectItem(select);
            ref(oper.getText());
        }        
    }
    
    public void testRemoveFor() {
        setAndApply(12,1,0);                        
    }
    
    public void testRemoveIf() {
        setAndApply(16,1,0);
    }
    
    public void testRemoveElse() {
        setAndApply(19,1,0);
    }
    
    public void testRemoveWhile() {
        setAndApply(24,1,0);
    }
    
    public void testRemoveTry() {
        setAndApply(29,1,0);
    }
    
    public void testRemoveCatch() {
        setAndApply(31,1,0);
    }
    
    public void testRemoveResources() {
        setAndApply(34,15,0);
    }
    
    public void testRemoveSynchronized() {
        setAndApply(41,1,0);
    }
    
    public static Test suite() {
        return NbModuleSuite.create(
                NbModuleSuite.createConfiguration(RemoveSurroundingTest.class)
                .addTest("testRemoveFor")     
                .addTest("testRemoveIf")     
                .addTest("testRemoveElse")     
                .addTest("testRemoveWhile")     
                .addTest("testRemoveTry")     
                .addTest("testRemoveCatch")     
                .addTest("testRemoveResources")     
                .addTest("testRemoveSynchronized")     
                .enableModules(".*")
                .clusters(".*"));
    }
    
}
