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
package org.netbeans.jellytools;

import java.awt.Container;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Iterator;
import javax.swing.SwingUtilities;
import org.netbeans.jemmy.JemmyException;
import org.netbeans.jemmy.QueueTool;
import org.netbeans.jemmy.operators.ContainerOperator;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JListOperator;
import org.netbeans.jemmy.operators.JTableOperator;
import org.openide.windows.Mode;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 * Handle documents area of NetBeans IDE. It holds editor top components
 * which can be tested by {@link EditorOperator}. This operator only enables
 * to switch between editors and to manipulate control buttons.
 * <p>
 * Usage:<br>
 * <pre>
        EditorWindowOperator ewo = new EditorWindowOperator();
        String filename = "MyClass";
        EditorOperator eo1 = ewo.selectPage(filename);
        // gets currently selected editor
        EditorOperator eo2 = ewo.getEditor();
        // switches to requested editor and gets EditorOperator instance
        EditorOperator eo3 = ewo.getEditor(filename);
        // manipulate control buttons
        ewo.moveTabsLeft();
        ewo.moveTabsRight();
        ewo.jumpLeft();
        // selects document from the list
        ewo.selectDocument(filename);
 * </pre>
 * @author Jiri.Skrivanek@sun.com
 */
public class EditorWindowOperator {
    
    /** Components operators. */
    private static JButtonOperator _btLeft;
    private static JButtonOperator _btRight;
    private static JButtonOperator _btDown;
    
    /** Creates new instance of EditorWindowOperator.
     * @deprecated Use static methods instead.
     */
    @Deprecated
    public EditorWindowOperator() {
        // useless now because all methods are static
    }
    
    /** Returns operator of left arrow button in top right corner intended to 
     * move tabs to be visible left ones.
     * @return JButtonOperator instance
     */
    public static JButtonOperator btLeft() {
        if(_btLeft == null) {
            _btLeft = new JButtonOperator(
                        new ContainerOperator(getEditor().findTabDisplayer()), 0);
        }
        return _btLeft;
    }

    /** Returns operator of right arrow button in top right corner intended to 
     * move tabs to be visible right ones.
     * @return JButtonOperator instance
     */
    public static JButtonOperator btRight() {
        if(_btRight == null) {
            _btRight = new JButtonOperator(
                        new ContainerOperator(getEditor().findTabDisplayer()), 1);
        }
        return _btRight;
    }
    
    /** Returns operator of down arrow button in top right corner intended to 
     * show list of opened documents and selects a document in the list.
     * @return JButtonOperator instance
     */
    public static JButtonOperator btDown() {
        if(_btDown == null) {
            _btDown = new JButtonOperator(
                        new ContainerOperator(getEditor().findTabDisplayer()), 2);
        }
        return _btDown;
    }

    /** Close all opened documents and discard all changes.
     * It works also if no file is modified, so it is a safe way how to close
     * documents and no block further execution.
     */
    public static void closeDiscard() {
        closeDiscard(findEditorMode());
    }
    /** Close all opened documents and discard all changes.
     * It works also if no file is modified, so it is a safe way how to close
     * documents and no block further execution.
     */
    public static void closeDiscard(final Mode mode) {
        try {
            SwingUtilities.invokeAndWait(new Runnable() {

                public void run() {
                    Iterator iter = Arrays.asList(mode.getTopComponents()).iterator();
                    while (iter.hasNext()) {
                        EditorOperator.close((TopComponent) iter.next(), false);
                    }
                }
            });
        } catch (InterruptedException ex) {
            throw new JemmyException("Interrupted!", ex); //NOI18N
        } catch (InvocationTargetException ex) {
            throw new JemmyException("Exception while closing.", ex); //NOI18N
        }
    }
    
    /** Selects page by its label. If only one file is open in Editor, it does
     * nothing.
     * @param label label of page to switch to
     * @return instance of selected EditorOperator
     */
    public static EditorOperator selectPage(String label) {
        return new EditorOperator(label);
    }
    
    /** Selects page by its index. If only one file is open in Editor, it does
     * nothing.
     * @param index index of page to be selected
     * @return instance of selected EditorOperator
     * @see EditorOperator
     */
    public static EditorOperator selectPage(int index) {
        try {
            // finds and selects index-th editor
            new TopComponentOperator((TopComponent)getEditor().findTabbedAdapter().getModel().getTab(index).getComponent());
        } catch (IndexOutOfBoundsException e) {
            throw new JemmyException("Index "+index+" out of bounds.", e); //NOI18N
        }
        return getEditor();
    }
    
    /** Returns EditorOperator instance of currently selected document.
     * @return EditorOperator instance of the selected document
     * @see EditorOperator
     */
    public static EditorOperator getEditor() {
        final Mode mode = findEditorMode();
        // run in dispatch thread
        String name = (String)new QueueTool().invokeSmoothly(new QueueTool.QueueAction("getSelectedTopComponent().getName()") {    // NOI18N
            public Object launch() {
                return mode.getSelectedTopComponent().getName();
            }
        });
        return new EditorOperator(name);
    }
    
    /** Selects page with given label and returns EditorOperator instance of
     * that page.
     * @param label label of page to be selected
     * @return EditorOperator instance of requested page
     * @see EditorOperator
     */
    public static EditorOperator getEditor(String label) {
        return selectPage(label);
    }
    
    /** Selects page with given index and returns EditorOperator instance of
     * that page.
     * @param index index of page to be selected
     * @return EditorOperator instance of requested page
     * @see EditorOperator
     */
    public static EditorOperator getEditor(int index) {
        return selectPage(index);
    }

    /********************** Control buttons ********************************/
    
    /** If the leftmost visible tab is partially hidden, it clicks on it. 
     * Otherwise it does nothing.
     * @return true if tabs were moved, false otherwise
     */
    public static boolean jumpLeft() {
        if(btLeft().isEnabled()) {
            Container cont = getEditor().findTabDisplayer();
            // click left corner
            new ContainerOperator(cont).clickMouse(cont.getX()+1, cont.getY()+cont.getHeight()/2, 1);
            return true;
        }
        return false;
    }
    
    /** Pushes rigth arrow control button in top right corner intended to 
     * move tabs to be visible right ones. If the button is not enabled, 
     * it does nothing.
     */
    public static void moveTabsRight() {
        btRight().push();
    }

    /** Pushes left arrow control button in top right corner intended to 
     * move tabs to be visible left ones. If the button is not enabled, 
     * it does nothing.
     */
    public static void moveTabsLeft() {
        btLeft().push();
    }

    /** Pushes down arrow control button in top right corner intended to 
     * show list of opened documents and selects index-th documents in the list.
     */
    public static void selectDocument(int index) {
        btDown().push();
        JTableOperator tableOper = new JTableOperator(MainWindowOperator.getDefault());
        tableOper.selectCell(index, 0);
    }

    /** Pushes down arrow control button in top right corner intended to 
     * show list of opened documents and selects document with given name
     * in the list.
     */
    public static void selectDocument(String name) {
        btDown().push();
        JTableOperator tableOper = new JTableOperator(MainWindowOperator.getDefault());
        int row = tableOper.findCellRow(name);
        if (row > -1) {
            tableOper.selectCell(row, 0);
        } else {
            throw new JemmyException("Cannot select document \"" + name + "\".");
        }
    }
    
    /** Performs verification by accessing all sub-components */    
    public static void verify() {
        getEditor().verify();
    }
    
    /** Finds editor mode within IDE window system.
     * @return editor mode instance
     */
    private static Mode findEditorMode() {
        // run in dispatch thread
        return (Mode)new QueueTool().invokeSmoothly(new QueueTool.QueueAction("findMode") {    // NOI18N
            public Object launch() {
                return WindowManager.getDefault().findMode("editor"); //NOI18N
            }
        });
    }
}
