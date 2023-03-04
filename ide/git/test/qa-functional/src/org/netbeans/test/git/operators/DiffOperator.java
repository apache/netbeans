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
package org.netbeans.test.git.operators;

import java.awt.Component;
import javax.swing.JComponent;
import org.netbeans.jellytools.TopComponentOperator;
import org.netbeans.jemmy.ComponentChooser;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JComboBoxOperator;
import org.netbeans.jemmy.operators.JToggleButtonOperator;
import org.netbeans.jemmy.operators.Operator;


public class DiffOperator extends TopComponentOperator {
    
    /** Waits for Diff TopComponent with specified file name.
     * @param name name of 'diffed' file
     */
    public DiffOperator(String name) {
        super(waitTopComponent(null, name, 0, new DiffSubchooser()));
    }
    
    /** Waits for first open Diff TopComponent. */
    public DiffOperator() {
        this(null);
    }
    
    private JButtonOperator _btNext;
    private JButtonOperator _btPrevious;
    private JToggleButtonOperator _tbRemoveLocal;
    private JToggleButtonOperator _tbLocal;
    private JToggleButtonOperator _tbRemote;
    private JButtonOperator _btRefresh;
    private JButtonOperator _btUpdate;
    private JButtonOperator _btCommit;
    private JComboBoxOperator _cbFiles;
    
    //******************************
    // Subcomponents definition part
    //******************************
    
    /** Tries to find Go to Next Difference JButton in diff view.
     * @return JButtonOperator
     */
    public JButtonOperator btNext() {
        if (_btNext==null) {
            String tooltip = "Go to next difference";
            _btNext = new JButtonOperator(this, new TooltipChooser(tooltip, getComparator()));
        }
        return _btNext;
    }
    
    /** Tries to find Go to Previous Difference JButton in diff view.
     * @return JButtonOperator
     */
    public JButtonOperator btPrevious() {
        if (_btPrevious==null) {
            String tooltip = "Go to previous difference";
            _btPrevious = new JButtonOperator(this, new TooltipChooser(tooltip, getComparator()));
        }
        return _btPrevious;
    }
    
    /** Tries to find "Remove vs Local" JToggleButton in diff view.
     * @return JToggleButtonOperator
     */
    public JToggleButtonOperator tbRemoteLocal() {
        if (_tbRemoveLocal==null) {
            _tbRemoveLocal = new JToggleButtonOperator(this, "Remote vs Local");
        }
        return _tbRemoveLocal;
    }
    
    /** Tries to find "Local" JToggleButton in diff view.
     * @return JToggleButtonOperator
     */
    public JToggleButtonOperator tbLocal() {
        if (_tbLocal==null) {
            Operator.StringComparator oldComparator = Operator.getDefaultStringComparator();
            Operator.DefaultStringComparator comparator = new Operator.DefaultStringComparator(true, true);
            try {
                this.setComparator(comparator);
                _tbLocal = new JToggleButtonOperator(this, "Local");
            } finally {
                this.setComparator(oldComparator);
            }
        }
        return _tbLocal;
    }
    
    /** Tries to find "Remote" JToggleButton in diff view.
     * @return JToggleButtonOperator
     */
    public JToggleButtonOperator tbRemote() {
        if (_tbRemote==null) {
            Operator.StringComparator oldComparator = Operator.getDefaultStringComparator();
            Operator.DefaultStringComparator comparator = new Operator.DefaultStringComparator(true, true);
            try {
                this.setComparator(comparator);
                _tbRemote = new JToggleButtonOperator(this, "Remote");
            } finally {
                this.setComparator(oldComparator);
            }
        }
        return _tbRemote;
    }
    
    /** Tries to find Refresh Diff JButton in diff view.
     * @return JButtonOperator
     */
    public JButtonOperator btRefresh() {
        if (_btRefresh==null) {
            _btRefresh = new JButtonOperator(this, new TooltipChooser("Refresh Diff", this.getComparator()));
        }
        return _btRefresh;
    }
    
    /** Tries to find Update JButton in diff view.
     * @return JButtonOperator
     */
    public JButtonOperator btUpdate() {
        if (_btUpdate==null) {
            _btUpdate = new JButtonOperator(this, new TooltipChooser("Update \"{0}\"", this.getComparator()));
        }
        return _btUpdate;
    }
    
    /** Tries to find Commit JButton in diff view.
     * @return JButtonOperator
     */
    public JButtonOperator btCommit() {
        if (_btCommit==null) {
            _btCommit = new JButtonOperator(this, new TooltipChooser("Commit \"{0}\"", this.getComparator()));
        }
        return _btCommit;
    }
    
    /** Tries to find files JComboBoxOperator in diff view.
     * @return JComboBoxOperator
     */
    public JComboBoxOperator cbFiles() {
        if (_cbFiles == null) {
            _cbFiles = new JComboBoxOperator(this);
        }
        return _cbFiles;
    }
    //****************************************
    // Low-level functionality definition part
    //****************************************
    
    /** clicks on Go to Next Difference JButton
     */
    public void next() {
        btNext().push();
    }
    
    /** clicks on Go to Previous Difference JButton
     */
    public void previous() {
        btPrevious().push();
    }
    
    /** checks or unchecks given JToggleButton
     * @param state boolean requested state
     */
    public void checkRemoteLocal(boolean state) {
        if (tbRemoteLocal().isSelected()!=state) {
            tbRemoteLocal().push();
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
    
    /** clicks on Refresh Diff JButton
     */
    public void refresh() {
        btRefresh().push();
    }
    
    /** clicks on Update JButton
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
    
    /** Selects specified file in combo box.
     * @param name file name to be selected
     */
    public void selectFile(String name) {
        cbFiles().selectItem(name);
    }
    
    /** Selects index-th file in combo box.
     * @param index index of file to be selected
     */
    public void selectFile(int index) {
        cbFiles().selectItem(index);
    }
    
    //*****************************************
    // High-level functionality definition part
    //*****************************************
    
    /** Performs verification of VersioningOperator by accessing all its components.
     */
    public void verify() {
        btNext();
        btPrevious();
        tbRemoteLocal();
        tbLocal();
        tbRemote();
        btRefresh();
        btUpdate();
        btCommit();
        cbFiles();
    }
    
    /** SubChooser to determine TopComponent is instance of
     * org.netbeans.modules.versioning.system.cvss.ui.actions.diff.DiffExecutor$DiffTopComponent
     * Used in constructor.
     */
    private static final class DiffSubchooser implements ComponentChooser {
        public boolean checkComponent(Component comp) {
            return comp.getClass().getName().endsWith("DiffTopComponent"); // NOI18N
        }
        
        public String getDescription() {
            return "org.netbeans.modules.subversion.ui.diff.DiffTopComponent"; // NOI18N
        }
    }
    
    /** Chooser which can be used to find a component with given tooltip,
     * for example a button.
     */
    private static class TooltipChooser implements ComponentChooser {
        private String buttonTooltip;
        private Operator.StringComparator comparator;
        
        public TooltipChooser(String buttonTooltip, Operator.StringComparator comparator) {
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

