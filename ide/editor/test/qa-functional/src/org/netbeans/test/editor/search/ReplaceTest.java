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

package org.netbeans.test.editor.search;

import java.awt.Component;
import java.awt.Window;
import java.awt.event.KeyEvent;
import junit.framework.Test;
import junit.textui.TestRunner;
import org.netbeans.test.editor.lib.EditorTestCase;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.MainWindowOperator;
import org.netbeans.jellytools.modules.editor.Replace;
import org.netbeans.jellytools.modules.editor.ReplaceBarOperator;
import org.netbeans.jellytools.modules.editor.SearchBarOperator;
import org.netbeans.jemmy.ComponentChooser;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.TestOut;
import org.netbeans.jemmy.operators.WindowOperator;
import org.netbeans.junit.NbModuleSuite;

/**
 *
 * @author Roman Strobl
 */
public class ReplaceTest extends EditorTestCase {
    
    private static int REPLACE_TIMEOUT = 100;
    
    /** Creates a new instance of ReplaceTest */
    public ReplaceTest(String testMethodName) {
        super(testMethodName);
    }
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();        
        System.out.println("#############");
        System.out.println("# Starting "+this.getName());
        System.out.println("#############");
    }

    @Override
    protected void tearDown() throws Exception {
        System.out.println("#############");
        System.out.println("# Finished "+this.getName());
        System.out.println("#############");
        super.tearDown();
    }
    
    
                
    /**
     * TC1 - open and close replace dialog
     */
    public void testReplaceDialogOpenClose() {
        openDefaultProject();
        openDefaultSampleFile();
        try {
            EditorOperator editor = getDefaultSampleEditorOperator();
            ReplaceBarOperator bar = ReplaceBarOperator.invoke(editor);            
            new EventTool().waitNoEvent(100);
            assertTrue(bar.isVisible());            
            bar.getContainerOperator().pushKey(KeyEvent.VK_ESCAPE);
            new EventTool().waitNoEvent(200);
            assertFalse(bar.isVisible());                                    
        } finally {            
            closeFileWithDiscard();
        }
    }
    
    /**
     * TC2 - Replace Dialog Open - Selection
     */
    public void testReplaceSelectionRepeated() {
        openDefaultProject();
        openDefaultSampleFile();
        try {
            EditorOperator editor = getDefaultSampleEditorOperator();           
            // choose the "testReplaceSelectionRepeated" word            
            editor.setCaretPosition(1,1);            
            new EventTool().waitNoEvent(REPLACE_TIMEOUT);
            ReplaceBarOperator bar = ReplaceBarOperator.invoke(editor);            
            // check only selected checkboxes
            bar.uncheckAll();            
            new EventTool().waitNoEvent(REPLACE_TIMEOUT);
            bar.getSearchBar().findCombo().typeText("testReplaceSelectionRepeated");
            bar.replaceCombo().clearText();
            bar.replaceCombo().typeText("testReplaceSelectionRepeated2");            
            new EventTool().waitNoEvent(REPLACE_TIMEOUT);            
            bar.replaceButton().doClick();            
            new EventTool().waitNoEvent(REPLACE_TIMEOUT);            
            editor.setCaretPosition(52,1);                        
            bar.getSearchBar().findCombo().clearText();
            bar.getSearchBar().findCombo().typeText("testReplaceSelectionRepeated");            
            bar.replaceCombo().clearText();
            bar.replaceCombo().typeText("testReplaceSelectionRepeated2");               
            new EventTool().waitNoEvent(REPLACE_TIMEOUT);
            bar.replaceButton().doClick();            
            bar.replaceButton().doClick();
            new EventTool().waitNoEvent(REPLACE_TIMEOUT);                       
            bar.closeButton().doClick();
            ref(editor.getText());
            compareReferenceFiles();            
        } finally {            
            closeFileWithDiscard();
        }
    }
    
    /**
     * TC3 - Replace Dialog Combo Box
     */
//    public void testReplaceDialogComboBox() {
//        openDefaultProject();
//        openDefaultSampleFile();
//        try {
//            EditorOperator editor = getDefaultSampleEditorOperator();
//            editor.setCaretPosition(1,1);
//            ReplaceBarOperator bar = ReplaceBarOperator.invoke(editor);
//            bar.uncheckAll();
//            SearchBarOperator search = bar.getSearchBar();            
//            //search.wrapAroundCheckBox().setSelected(true);            
//            search.findCombo().clearText();
//            search.findCombo().typeText("package");
//            bar.replaceCombo().clearText();
//            bar.replaceCombo().typeText("pakaz");
//            bar.replaceButton().doClick();
//            new EventTool().waitNoEvent(REPLACE_TIMEOUT);           
//            bar.replaceButton().doClick();
//            // check status bar
// //            waitForLabel("'package' not found");
//		
//            editor.setCaretPosition(1,1);
//            new EventTool().waitNoEvent(REPLACE_TIMEOUT);           
//            search.findCombo().clearText();
//            search.findCombo().typeText("class");
//            bar.replaceCombo().clearText();
//            bar.replaceCombo().typeText("klasa");
//            bar.replaceButton().doClick();
//            new EventTool().waitNoEvent(REPLACE_TIMEOUT);           
//            bar.replaceButton().doClick();
//            // check status bar
// //            waitForLabel("'class' not found");
//            
//            editor.setCaretPosition(1,1);
//            new EventTool().waitNoEvent(REPLACE_TIMEOUT);           
//            search.findCombo().clearText();
//            search.findCombo().typeText("testReplaceDialogComboBox");
//            bar.replaceCombo().clearText();
//            bar.replaceCombo().typeText("testReplaceDialogComboBox2");
//            bar.replaceButton().doClick();
//            // check status bar
// //            waitForLabel("'testReplaceDialogComboBox' found at 13:35");
//            
//		new EventTool().waitNoEvent(REPLACE_TIMEOUT);           
//            boolean[] found = new boolean[3];
//            String[] etalon = {"testReplaceDialogComboBox","class","package"};
//            for (int i = 0; i<search.findCombo().getItemCount(); i++) {
//		    System.out.println(search.findCombo().getItemAt(i));
//		    if(i<found.length) 
//			  found[i] = etalon[i].equals((String)search.findCombo().getItemAt(i));
//            }
//            for (boolean b : found) {
//                assertTrue(b);
//            }                        
//            
//            String[] etalonReplace = {"testReplaceDialogComboBox2","klasa","pakaz"};
//            
//            for (int i = 0; i<bar.replaceCombo().getItemCount(); i++) {
//		    System.out.println(bar.replaceCombo().getItemAt(i));
//		    if(i<found.length)
//			  found[i] = etalonReplace[i].equals((String)bar.replaceCombo().getItemAt(i));                
//            }
//            for (boolean b : found) {
//                assertTrue(b);
//            }                                                
//            new EventTool().waitNoEvent(REPLACE_TIMEOUT);
//            bar.closeButton().doClick();            
//            ref(editor.getText());
//            compareReferenceFiles();            
//        } finally {
//            closeFileWithDiscard();
//        }
//    }
    
    /**
     * TC4 - Replace Match Case
     */
    public void testReplaceMatchCase() {
        openDefaultProject();
        openDefaultSampleFile();
        try {
            EditorOperator editor = getDefaultSampleEditorOperator();
            ReplaceBarOperator bar = ReplaceBarOperator.invoke(editor);
            editor.setCaretPosition(1,1);
            bar.uncheckAll();
            final SearchBarOperator searchBar = bar.getSearchBar();
            searchBar.matchCaseCheckBox().setSelected(true);
            searchBar.findCombo().clearText();
            searchBar.findCombo().typeText("testCase");            
            bar.replaceCombo().clearText();
            bar.replaceCombo().typeText("xxxxXxxx");
            bar.replaceButton().doClick();                                    
            ref(editor.getText());
            compareReferenceFiles();
        } finally {
            closeFileWithDiscard();
        }
    }
    
    /**
     * TC5 - Replace All
     */
    public void testReplaceAll() {
        openDefaultProject();
        openDefaultSampleFile();
        try {
            EditorOperator editor = getDefaultSampleEditorOperator();           
            editor.setCaretPosition(1,1);
            ReplaceBarOperator bar = ReplaceBarOperator.invoke(editor);
            bar.uncheckAll();
            final SearchBarOperator searchBar = bar.getSearchBar();
            searchBar.matchCaseCheckBox().setSelected(true);
            searchBar.findCombo().clearText();
            searchBar.findCombo().typeText("testWord");            
            bar.replaceCombo().clearText();
            bar.replaceCombo().typeText("xxxxXxxx");
            
            bar.replaceAll().doClick();
//            waitForLabel("14 of 14 items replaced");
            ref(editor.getText());
            compareReferenceFiles();
        } finally {
            closeFileWithDiscard();
        }
    }
    
    /**
     * TC6 - Replace in Selection Only
     */
    public void testReplaceInSelectionOnly() {
        openDefaultProject();
        openDefaultSampleFile();
        try {
            EditorOperator editor = getDefaultSampleEditorOperator();
            editor.select(58, 62);
            ReplaceBarOperator bar = ReplaceBarOperator.invoke(editor);
            bar.uncheckAll();
            final SearchBarOperator searchBar = bar.getSearchBar();
            searchBar.findCombo().clearText();
            searchBar.findCombo().typeText("testWord");
            bar.replaceCombo().clearText();
            bar.replaceCombo().typeText("xxxxXxxx");            
            new EventTool().waitNoEvent(REPLACE_TIMEOUT);           
            bar.replaceAll().doClick();
            // check status bar
//            waitForLabel("5 of 5 items replaced");                                    
            ref(editor.getText());
            compareReferenceFiles();
            
        } finally {
            closeFileWithDiscard();
        }
    }
           
    /**
     * Waits for label to appear on Status Bar, checks it 10 times before
     * failing.
     * @param label label which should be displayed on status bar
     */
    public void waitForLabel(final String label) {   
        String statusText = null;
        for (int i = 0; i<30; i++) {
            statusText = MainWindowOperator.getDefault().getStatusText();
            if (label.equals(statusText)) {
                break;
            }
            new EventTool().waitNoEvent(100);
        }        
        System.out.println(statusText);        
        assertEquals(label, MainWindowOperator.getDefault().getStatusText());
    }
            
    public static void main(String[] args) {
        TestRunner.run(ReplaceTest.class);                
    }
}
