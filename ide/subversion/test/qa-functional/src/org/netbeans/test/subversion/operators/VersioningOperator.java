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
package org.netbeans.test.subversion.operators;

import java.awt.Component;
import javax.swing.JComponent;
import org.netbeans.jellytools.TopComponentOperator;
import org.netbeans.jellytools.actions.Action;
import org.netbeans.jemmy.ComponentChooser;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JPopupMenuOperator;
import org.netbeans.jemmy.operators.JTableOperator;
import org.netbeans.jemmy.operators.JToggleButtonOperator;

/** Class implementing all necessary methods for handling "Versioning" view.
 * <br>
 * Usage:<br>
 * <pre>
 *      VersioningOperator vo = VersioningOperator.invoke();
 *      vo.checkLocal(true);
 *      vo.checkRemote(true);
 *      vo.checkAll(true);
 *      vo.refresh();
 *      vo.diff();
 *      vo.update();
 *      vo.performPopup("MyFile", "Exclude from Commit");
 *      CommitOperator co = vo.commit();
 *      co.setCommitMessage("Commit message.");
 *      co.commit();
 * </pre>
 *
 * @see CommitOperator
 *
 */
public class VersioningOperator extends TopComponentOperator {
    
    /** "Versioning" */
    static final String VERSIONING_TITLE = "Versioning";
    static final String SUBVERSION_TITLE = "Subversion";
    
    /** Waits for Versioning TopComponent within whole IDE. */
    public VersioningOperator() {
        super(waitTopComponent(null, SUBVERSION_TITLE, 0, new VersioningSubchooser()));
    }
    
    /** Invokes Window|Versioning main menu item and returns new instance of
     * VersioningOperator.
     * @return new instance of VersioningOperator */
    public static VersioningOperator invoke() {
        //new Action("Window|" + VERSIONING_TITLE + "|" + SUBVERSION_TITLE, null).perform();
        new Action("Team|Show Changes", null).perform();
        return new VersioningOperator();
    }
    
    private JToggleButtonOperator _tbAll;
    private JToggleButtonOperator _tbLocal;
    private JToggleButtonOperator _tbRemote;
    private JButtonOperator _btRefresh;
    private JButtonOperator _btDiff;
    private JButtonOperator _btUpdate;
    private JButtonOperator _btCommit;
    private JTableOperator _tabFiles;
    
    
    //******************************
    // Subcomponents definition part
    //******************************
    
    /** Tries to find "All" JToggleButton in this dialog.
     * @return JToggleButtonOperator
     */
    public JToggleButtonOperator tbAll() {
        if (_tbAll==null) {
            _tbAll = new JToggleButtonOperator(this, "All");
        }
        return _tbAll;
    }
    
    /** Tries to find "Local" JToggleButton in this dialog.
     * @return JToggleButtonOperator
     */
    public JToggleButtonOperator tbLocal() {
        if (_tbLocal==null) {
            _tbLocal = new JToggleButtonOperator(this, "Local");
        }
        return _tbLocal;
    }
    
    /** Tries to find "Remote" JToggleButton in this dialog.
     * @return JToggleButtonOperator
     */
    public JToggleButtonOperator tbRemote() {
        if (_tbRemote==null) {
            _tbRemote = new JToggleButtonOperator(this, "Remote");
        }
        return _tbRemote;
    }
    
    /** Tries to find Refresh Status JButton in this dialog.
     * @return JButtonOperator
     */
    public JButtonOperator btRefresh() {
        if (_btRefresh==null) {
            _btRefresh = new JButtonOperator(this, new TooltipChooser(
                    "Refresh Status",
                    this.getComparator()));
        }
        return _btRefresh;
    }
    
    /** Tries to find Diff All JButton in this dialog.
     * @return JButtonOperator
     */
    public JButtonOperator btDiff() {
        if (_btDiff==null) {
            _btDiff = new JButtonOperator(this, new TooltipChooser(
                    "Diff All",
                    this.getComparator()));
        }
        return _btDiff;
    }
    
    /** Tries to find Update All JButton in this dialog.
     * @return JButtonOperator
     */
    public JButtonOperator btUpdate() {
        if (_btUpdate==null) {
            _btUpdate = new JButtonOperator(this, new TooltipChooser(
                    "Update All",
                    this.getComparator()));
        }
        return _btUpdate;
    }
    
    /** Tries to find Commit All JButton in this dialog.
     * @return JButtonOperator
     */
    public JButtonOperator btCommit() {
        if (_btCommit==null) {
            _btCommit = new JButtonOperator(this, new TooltipChooser(
                    "Commit All",
                    this.getComparator()));
        }
        return _btCommit;
    }
    
    /** Tries to find files JTable in this dialog.
     * @return JTableOperator
     */
    public JTableOperator tabFiles() {
        if (_tabFiles==null) {
            _tabFiles = new JTableOperator(this);
        }
        return _tabFiles;
    }
    
    
    //****************************************
    // Low-level functionality definition part
    //****************************************
    
    /** checks or unchecks given JToggleButton
     * @param state boolean requested state
     */
    public void checkAll(boolean state) {
        if (tbAll().isSelected()!=state) {
            tbAll().push();
        }
    }
    
    /** checks or unchecks given JToggleButton
     * @param state boolean requested state
     */
    public void checkLocal(boolean state) {
        if (tbLocal().isSelected()!=state) {
            tbLocal().push();
        }
    }
    
    /** checks or unchecks given JToggleButton
     * @param state boolean requested state
     */
    public void checkRemote(boolean state) {
        if (tbRemote().isSelected()!=state) {
            tbRemote().push();
        }
    }
    
    /** clicks on Refresh Status JButton
     */
    public void refresh() {
        btRefresh().push();
    }
    
    /** clicks on Diff All JButton
     */
    public void diff() {
        btDiff().push();
    }
    
    /** clicks on Update All JButton
     */
    public void update() {
        btUpdate().push();
    }
    
    /** clicks on Commit JButton and returns CommitOperator.
     * @return CommitOperator instance
     */
    public CommitOperator commit() {
        btCommit().pushNoBlock();
        return new CommitOperator();
    }
    
    /** Performs popup menu on specified row.
     * @param row row number to be selected (starts from 0)
     * @param popupPath popup menu path
     */
    public void performPopup(int row, String popupPath) {
        tabFiles().selectCell(row, 0);
        JPopupMenuOperator popup = new JPopupMenuOperator(tabFiles().callPopupOnCell(row, 0));
        popup.pushMenu(popupPath);
    }
    
    /** Performs popup menu on specified file.
     * @param filename name of file to be selected
     * @param popupPath popup menu path
     */
    public void performPopup(String filename, String popupPath) {
        performPopup(tabFiles().findCellRow(filename), popupPath);
    }

    /** Performs popup menu on specified row and no block further execution.
     * @param row row number to be selected (starts from 0)
     * @param popupPath popup menu path
     */
    public void performPopupNoBlock(int row, String popupPath) {
        tabFiles().selectCell(row, 0);
        JPopupMenuOperator popup = new JPopupMenuOperator(tabFiles().callPopupOnCell(row, 0));
        popup.pushMenuNoBlock(popupPath);
    }
    
    /** Performs popup menu on specified file and no block further execution.
     * @param filename name of file to be selected
     * @param popupPath popup menu path
     */
    public void performPopupNoBlock(String filename, String popupPath) {
        performPopupNoBlock(tabFiles().findCellRow(filename), popupPath);
    }

    //*****************************************
    // High-level functionality definition part
    //*****************************************
    
    /** Performs verification of VersioningOperator by accessing all its components.
     */
    public void verify() {
        tbAll();
        tbLocal();
        tbRemote();
        btRefresh();
        btDiff();
        btUpdate();
        btCommit();
        tabFiles();
    }
    
    /** SubChooser to determine TopComponent is instance of
     * org.netbeans.modules.subversion.ui.status.SvnVersioningTopComponent
     * Used in constructor.
     */
    private static final class VersioningSubchooser implements ComponentChooser {
        public boolean checkComponent(Component comp) {
            return comp.getClass().getName().endsWith("SvnVersioningTopComponent");
        }
        
        public String getDescription() {
            return "org.netbeans.modules.subversion.ui.status.SvnVersioningTopComponent";
        }
    }
    
    /** Chooser which can be used to find a component with given tooltip,
     * for example a button.
     */
    private static class TooltipChooser implements ComponentChooser {
        private String buttonTooltip;
        private StringComparator comparator;
        
        public TooltipChooser(String buttonTooltip, StringComparator comparator) {
            this.buttonTooltip = buttonTooltip;
            this.comparator = comparator;
        }
        
        public boolean checkComponent(Component comp) {
            return comparator.equals(((JComponent)comp).getToolTipText(), buttonTooltip);
        }
        
        public String getDescription() {
            return "Button with tooltip \""+buttonTooltip+"\".";
        }
    }
}

