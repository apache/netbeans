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
