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
package org.netbeans.test.localhistory.operators;

import java.awt.Component;
import java.awt.Point;
import javax.swing.JComponent;
import javax.swing.JPopupMenu;
import org.netbeans.jellytools.TopComponentOperator;
import org.netbeans.jellytools.TreeTableOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.ComponentChooser;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JListOperator;
import org.netbeans.jemmy.operators.JPopupMenuOperator;
import org.netbeans.test.localhistory.actions.ShowLocalHistoryAction;
import org.openide.explorer.view.OutlineView;

/** Class implementing all necessary methods for handling "Local History" view.
 */
public class ShowLocalHistoryOperator extends TopComponentOperator {
    
    /** "Local History" */
    static final String LOCAL_HISTORY_TITLE = "Local History";
    
    /** Waits for Local History TopComponent within whole IDE. */
    public ShowLocalHistoryOperator() {
        super(waitTopComponent(null, LOCAL_HISTORY_TITLE, 0, new LocalHistorySubchooser()));
    }
    
    /** Selects nodes and call Local History action on them.
     * @param nodes an array of nodes
     * @return SearchHistoryOperator instance
     */
    public static ShowLocalHistoryOperator invoke(Node[] nodes) {
        new ShowLocalHistoryAction().perform(nodes);
        return new ShowLocalHistoryOperator();
    }
    
    /** Selects node and call Local History action on it.
     * @param node node to be selected
     * @return SearchHistoryOperator instance
     */
    public static ShowLocalHistoryOperator invoke(Node node) {
        return invoke(new Node[] {node});
    }
    
    private JButtonOperator _btNext;
    private JButtonOperator _btPrevious;
    private OutlineViewOperator _treeTable;

    //******************************
    // Subcomponents definition part
    //******************************
    
    
    
    /** Tries to find Go to Next Difference JButton in this dialog.
     * @return JButtonOperator
     */
    public JButtonOperator btNext() {
        if (_btNext==null) {
            String tooltip = "Go to next difference";
            _btNext = new JButtonOperator(this, new TooltipChooser(tooltip, getComparator()));
        }
        return _btNext;
    }
    
    /** Tries to find Go to Previous Difference JButton in this dialog.
     * @return JButtonOperator
     */
    public JButtonOperator btPrevious() {
        if (_btPrevious==null) {
            String tooltip = "Go to previous difference";
            _btPrevious = new JButtonOperator(this, new TooltipChooser(tooltip, getComparator()));
        }
        return _btPrevious;
    }
    
    /** Tries to find History JList in this dialog.
     * @return JListOperator
     */
    public OutlineViewOperator treeTableHistory() {
        if (_treeTable == null) {
            _treeTable = new OutlineViewOperator(this);
        }
        return _treeTable;
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
    
    /** Selects a folder denoted by path.
     * @param path path to folder without root (e.g. "folder|subfolder")
     */
    public void selectFolder(String path) {
        new Node(treeTableHistory().tree(), path).select();
    }
    
    public void performPopupAction(int rowIndex, String path) {
        JPopupMenu popup = treeTableHistory().callPopupOnCell(rowIndex, 0);
        JPopupMenuOperator popupOperator = new JPopupMenuOperator(popup);
        popupOperator.pushMenu(path);
    }
    
    public int getVersionCount(){
//        for needs of automated tests only!
//        Assuming there is only [today] node and the rest are versions
        treeTableHistory().selectCell(1, 0);      
        return treeTableHistory().getRowCount()-1;
    }
          
    
    //*****************************************
    // High-level functionality definition part
    //*****************************************
    
    /** Performs verification of SearchHistoryOperator by accessing all its components.
     */
    public void verify() {
        btNext();
        btPrevious();
        treeTableHistory();
    }
    
    /** SubChooser to determine TopComponent is instance of
     *  org.netbeans.modules.versioning.system.cvss.ui.history.SearchHistoryTopComponent
     * Used in constructor.
     */
    private static final class LocalHistorySubchooser implements ComponentChooser {
        public boolean checkComponent(Component comp) {
            return comp.getClass().getName().endsWith("LocalHistoryTopComponent");
        }
        
        public String getDescription() {
            return "org.netbeans.modules.localhistory.ui.view.LocalHistoryTopComponent";
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
