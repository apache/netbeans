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
package org.netbeans.test.subversion.operators;

import java.awt.Component;
import java.awt.Point;
import javax.swing.JComponent;
import javax.swing.JTextField;
import org.netbeans.jellytools.TopComponentOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.ComponentChooser;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JLabelOperator;
import org.netbeans.jemmy.operators.JListOperator;
import org.netbeans.jemmy.operators.JPopupMenuOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;
import org.netbeans.jemmy.operators.JToggleButtonOperator;
import org.netbeans.test.subversion.operators.actions.SearchHistoryAction;

/** Class implementing all necessary methods for handling "Search History" view.
 */
public class SearchHistoryOperator extends TopComponentOperator {
    
    /** "Search History" */
    static final String SEARCH_HISTORY_TITLE = "Search History";
    
    /** Waits for Search History TopComponent within whole IDE. */
    public SearchHistoryOperator() {
        super(waitTopComponent(null, SEARCH_HISTORY_TITLE, 0, new SearchHistorySubchooser()));
    }
    
    /** Selects nodes and call search history action on them.
     * @param nodes an array of nodes
     * @return SearchHistoryOperator instance
     */
    public static SearchHistoryOperator invoke(Node[] nodes) {
        new SearchHistoryAction().perform(nodes);
        new EventTool().waitNoEvent(3000);
        return new SearchHistoryOperator();
    }
    
    /** Selects node and call search history action on it.
     * @param node node to be selected
     * @return SearchHistoryOperator instance
     */
    public static SearchHistoryOperator invoke(Node node) {
        return invoke(new Node[] {node});
    }
    
    private JLabelOperator _lblMessage;
    private JTextFieldOperator _txtMessage;
    private JLabelOperator _lblUsername;
    private JTextFieldOperator _txtUsername;
    private JLabelOperator _lblFrom;
    private JTextFieldOperator _txtFrom;
    private JButtonOperator _btBrowseFrom;
    private JLabelOperator _lblTo;
    private JTextFieldOperator _txtTo;
    private JButtonOperator _btBrowseTo;
    private JButtonOperator _btSearch;
    private JToggleButtonOperator _tbSummary;
    private JToggleButtonOperator _tbDiff;
    private JButtonOperator _btNext;
    private JButtonOperator _btPrevious;
    private JListOperator _lstHistory;
    
    //******************************
    // Subcomponents definition part
    //******************************
    
    /** Tries to find "Message:" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblMessage() {
        if (_lblMessage==null) {
            _lblMessage = new JLabelOperator(this, "Message:");
        }
        return _lblMessage;
    }
    
    /** Tries to find null JTextField in this dialog.
     * @return JTextFieldOperator
     */
    public JTextFieldOperator txtMessage() {
        if (_txtMessage==null) {
            _txtMessage = new JTextFieldOperator(
                    (JTextField)lblMessage().getLabelFor());
        }
        return _txtMessage;
    }
    
    /** Tries to find "Username:" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblUsername() {
        if (_lblUsername==null) {
            _lblUsername = new JLabelOperator(this, "Username:");
        }
        return _lblUsername;
    }
    
    /** Tries to find null JTextField in this dialog.
     * @return JTextFieldOperator
     */
    public JTextFieldOperator txtUsername() {
        if (_txtUsername==null) {
            _txtUsername = new JTextFieldOperator(
                    (JTextField)lblUsername().getLabelFor());
        }
        return _txtUsername;
    }
    
    /** Tries to find "From:" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblFrom() {
        if (_lblFrom==null) {
            _lblFrom = new JLabelOperator(this, "From:");
        }
        return _lblFrom;
    }
    
    /** Tries to find null JTextField in this dialog.
     * @return JTextFieldOperator
     */
    public JTextFieldOperator txtFrom() {
        if (_txtFrom==null) {
            _txtFrom = new JTextFieldOperator(
                    (JTextField)lblFrom().getLabelFor());
        }
        return _txtFrom;
    }
    
    /** Tries to find "Browse..." JButton in this dialog.
     * @return JButtonOperator
     */
    public JButtonOperator btBrowseFrom() {
        if (_btBrowseFrom==null) {
            _btBrowseFrom = new JButtonOperator(this, "Browse");
        }
        return _btBrowseFrom;
    }
    
    /** Tries to find "To:" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblTo() {
        if (_lblTo==null) {
            _lblTo = new JLabelOperator(this, "To:");
        }
        return _lblTo;
    }
    
    /** Tries to find null JTextField in this dialog.
     * @return JTextFieldOperator
     */
    public JTextFieldOperator txtTo() {
        if (_txtTo==null) {
            _txtTo = new JTextFieldOperator(
                    (JTextField)lblTo().getLabelFor());
        }
        return _txtTo;
    }
    
    /** Tries to find "Browse..." JButton in this dialog.
     * @return JButtonOperator
     */
    public JButtonOperator btBrowseTo() {
        if (_btBrowseTo==null) {
            _btBrowseTo = new JButtonOperator(this, "Browse", 1);
        }
        return _btBrowseTo;
    }
    
    /** Tries to find "Search" JButton in this dialog.
     * @return JButtonOperator
     */
    public JButtonOperator btSearch() {
        if (_btSearch==null) {
            _btSearch = new JButtonOperator(this, "Search");
        }
        return _btSearch;
    }
    
    /** Tries to find "Summary" JToggleButton in this dialog.
     * @return JToggleButtonOperator
     */
    public JToggleButtonOperator tbSummary() {
        if (_tbSummary==null) {
            _tbSummary = new JToggleButtonOperator(this, "Summary");
        }
        return _tbSummary;
    }
    
    /** Tries to find "Diff" JToggleButton in this dialog.
     * @return JToggleButtonOperator
     */
    public JToggleButtonOperator tbDiff() {
        if (_tbDiff==null) {
            _tbDiff = new JToggleButtonOperator(this, "Diff");
        }
        return _tbDiff;
    }
    
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
    public JListOperator lstHistory() {
        if (_lstHistory==null) {
            _lstHistory = new JListOperator(this);
        }
        return _lstHistory;
    }
    
    
    //****************************************
    // Low-level functionality definition part
    //****************************************
    
    /** gets text for txtMessage
     * @return String text
     */
    public String getMessage() {
        return txtMessage().getText();
    }
    
    /** sets text for txtMessage
     * @param text String text
     */
    public void setMessage(String text) {
        txtMessage().clearText();
        txtMessage().typeText(text);
    }
    
    /** gets text for txtUsername
     * @return String text
     */
    public String getUsername() {
        return txtUsername().getText();
    }
    
    /** sets text for txtUsername
     * @param text String text
     */
    public void setUsername(String text) {
        txtUsername().clearText();
        txtUsername().typeText(text);
    }
    
    /** gets text for txtFrom
     * @return String text
     */
    public String getFrom() {
        return txtFrom().getText();
    }
    
    /** sets text for txtFrom
     * @param text String text
     */
    public void setFrom(String text) {
        txtFrom().clearText();
        txtFrom().typeText(text);
    }
    
    /** gets text for txtTo
     * @return String text
     */
    public String getTo() {
        return txtTo().getText();
    }
    
    /** sets text for txtTo
     * @param text String text
     */
    public void setTo(String text) {
        txtTo().clearText();
        txtTo().typeText(text);
    }
    
    /** clicks on "Search" JButton
     */
    public void search() {
        btSearch().push();
    }
    
    /** checks or unchecks Summary JToggleButton
     * @param state boolean requested state
     */
    public void checkSummary(boolean state) {
        if (tbSummary().isSelected()!=state) {
            tbSummary().push();
        }
    }
    
    /** checks or unchecks Diff JToggleButton
     * @param state boolean requested state
     */
    public void checkDiff(boolean state) {
        if (tbDiff().isSelected()!=state) {
            tbDiff().push();
        }
    }
    
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
    
    public RepositoryBrowserOperator getRevisionFrom() {
        btBrowseFrom().pushNoBlock();
        return new RepositoryBrowserOperator();
    }
    
    public RepositoryBrowserOperator getRevisionTo() {
        btBrowseTo().pushNoBlock();
        return new RepositoryBrowserOperator();
    }
    
    /** Performs popup menu on specified row in history list.
     * @param rowIndex index of row (starts at 0)
     * @param popupPath popup menu path
     */
    public void performPopup(int rowIndex, String popupPath) {
        Point point = lstHistory().getClickPoint(rowIndex);
        lstHistory().clickForPopup(point.x, point.y);
        JPopupMenuOperator popup = new JPopupMenuOperator();
        popup.pushMenu(popupPath);
    }
    
    //*****************************************
    // High-level functionality definition part
    //*****************************************
    
    /** Performs verification of SearchHistoryOperator by accessing all its components.
     */
    public void verify() {
        lblMessage();
        txtMessage();
        lblUsername();
        txtUsername();
        lblFrom();
        txtFrom();
        btBrowseFrom();
        lblTo();
        txtTo();
        btBrowseTo();
        btSearch();
        tbSummary();
        tbDiff();
        btNext();
        btPrevious();
        lstHistory();
    }
    
    /** SubChooser to determine TopComponent is instance of
     *  org.netbeans.modules.versioning.system.cvss.ui.history.SearchHistoryTopComponent
     * Used in constructor.
     */
    private static final class SearchHistorySubchooser implements ComponentChooser {
        public boolean checkComponent(Component comp) {
            return comp.getClass().getName().endsWith("SearchHistoryTopComponent");
        }
        
        public String getDescription() {
            return "org.netbeans.modules.subversion.ui.history.SearchHistoryTopComponent";
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
