/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.test.html5;

import java.awt.event.KeyEvent;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.table.TableModel;
import org.netbeans.jellytools.TopComponentOperator;
import org.netbeans.modules.css.visual.RuleEditorNode;
import org.netbeans.jemmy.operators.*;

/**
 * Operator for CSS Styles
 *
 * @author Vladimir Riha
 * @version 1.0
 */
public class CSSStylesOperator extends TopComponentOperator {

    public CSSStylesOperator(String filename) {
        super(filename + " - CSS Styles");
    }
    private JSplitPaneOperator _sppPseudoClasses;
    private JToggleButtonOperator _btSelectionView;
    private JToggleButtonOperator _btDocumentView;
    private JSplitPaneOperator _sppProperties;
    private JLabelOperator _lblSelectedElement;
    private JListOperator _lstAppliedRules;
    private JToggleButtonOperator _tbActive;
    private JToggleButtonOperator _tbHover;
    private JToggleButtonOperator _tbFocus;
    private JToggleButtonOperator _tbVisited;
    private JLabelOperator _lblSelectedRule;

    //******************************
    // Subcomponents definition part
    //******************************
    /**
     * Tries to find null JSplitPaneOperator in this dialog.
     *
     * @return JSplitPaneOperator
     */
    private JSplitPaneOperator sppPseudoClasses() {
        if (_sppPseudoClasses == null) {
            _sppPseudoClasses = new JSplitPaneOperator(this);
        }
        return _sppPseudoClasses;
    }

    /**
     * Tries to find "Selection" JToggleButton in this dialog.
     *
     * @return JToggleButtonOperator
     */
    private JToggleButtonOperator btSelectionView() {
        if (_btSelectionView == null) {
            _btSelectionView = new JToggleButtonOperator(sppPseudoClasses(), "Selection");
        }
        return _btSelectionView;
    }

    /**
     * Tries to find "Document" JToggleButton in this dialog.
     *
     * @return JToggleButtonOperator
     */
    private JToggleButtonOperator btDocumentView() {
        if (_btDocumentView == null) {
            _btDocumentView = new JToggleButtonOperator(sppPseudoClasses(), "Document");
        }
        return _btDocumentView;
    }

    private JSplitPaneOperator sppProperties() {
        if (_sppProperties == null) {
            _sppProperties = new JSplitPaneOperator(sppPseudoClasses());
        }
        return _sppProperties;
    }

    /**
     * Tries to find label with currently selected element in CSS
     * Styles|Selection part of window (bottom part). Sample output
     * <code>body #main.body</code>
     *
     * @return
     */
    public String getSelectedHTMLElementName() {
        if (_lblSelectedElement == null) {
            _lblSelectedElement = new JLabelOperator(sppProperties());
        }
        String label = _lblSelectedElement.getText();
        label = label.substring(label.indexOf("<b>") + 3, label.indexOf(" -") - 3);
        return label.replace("</b>", " ");
    }

    /**
     * Returns name of selected CSS rule in Rule Editor part of window
     *
     * @return
     */
    public String getSelectedCSSRuleName() {
        if (_lblSelectedRule == null) {
            _lblSelectedRule = new JLabelOperator(sppPseudoClasses(), 2);
        }
        String label = _lblSelectedRule.getText();
        return label.substring(0, label.indexOf("properties"));
    }

    /**
     * Tries to find JListOperator in this dialog (list of applied rules)
     *
     * @return JListOperator
     */
    private JListOperator lstAppliedRules() {
        if (_lstAppliedRules == null) {
            _lstAppliedRules = new JListOperator(sppProperties());
        }
        return _lstAppliedRules;
    }

    /**
     * Tries to find active checkbox in this dialog.
     *
     * @return JToggleButtonOperator
     */
    private JToggleButtonOperator tbActive() {
        if (_tbActive == null) {
            _tbActive = new JToggleButtonOperator(sppProperties(), 1);
        }
        return _tbActive;
    }

    /**
     * Tries to find hover checkbox in this dialog.
     *
     * @return JToggleButtonOperator
     */
    private JToggleButtonOperator tbHover() {
        if (_tbHover == null) {
            _tbHover = new JToggleButtonOperator(sppPseudoClasses(), 4);
        }
        return _tbHover;
    }

    /**
     * Tries to find focus checkbox in this dialog.
     *
     * @return JToggleButtonOperator
     */
    private JToggleButtonOperator tbFocus() {
        if (_tbFocus == null) {
            _tbFocus = new JToggleButtonOperator(sppPseudoClasses(), 5);
        }
        return _tbFocus;
    }

    /**
     * Tries to find visited checkbox in this dialog.
     *
     * @return JToggleButtonOperator
     */
    private JToggleButtonOperator tbVisited() {
        if (_tbVisited == null) {
            _tbVisited = new JToggleButtonOperator(sppPseudoClasses(), 6);
        }
        return _tbVisited;
    }

    //****************************************
    // Low-level functionality definition part
    //****************************************
    /**
     * checks or unchecks Selection view button
     *
     * @param state boolean requested state
     */
    public void checkSelection(boolean state) {
        if (btSelectionView().isSelected() != state) {
            btSelectionView().push();
        }
    }

    /**
     * checks or unchecks Document view button
     *
     * @param state boolean requested state
     */
    public void checkDocument(boolean state) {
        if (btDocumentView().isSelected() != state) {
            btDocumentView().push();
        }
    }

    /**
     * checks or unchecks active pseudoclass button
     *
     * @param state boolean requested state
     */
    public void checkPseudoClassActive(boolean state) {
        if (tbActive().isSelected() != state) {
            tbActive().push();
        }
    }

    /**
     * checks or unchecks hover pseudoclass button
     *
     * @param state boolean requested state
     */
    public void checkPseudoClassHover(boolean state) {
        if (tbHover().isSelected() != state) {
            tbHover().push();
        }
    }

    /**
     * checks or unchecks focus pseudoclass button
     *
     * @param state boolean requested state
     */
    public void checkPseudoClassFocus(boolean state) {
        if (tbFocus().isSelected() != state) {
            tbFocus().push();
        }
    }

    /**
     * checks or unchecks visited pseudoclass button
     *
     * @param state boolean requested state
     */
    public void checkPseudoClassVisited(boolean state) {
        if (tbVisited().isSelected() != state) {
            tbVisited().push();
        }
    }

    /**
     * Returns HashMap&lt;String, String&gt; of element's CSS properties (top
     * part of window)
     *
     * @return
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     */
    public HashMap<String, String> getCSSProperties() throws IllegalAccessException, InvocationTargetException {
        JTableOperator _tabTreeTable = new JTableOperator(sppProperties());
        TableModel tm = _tabTreeTable.getModel();
        HashMap<String, String> properties = new HashMap<String, String>();
        org.openide.nodes.Node.Property t;
        for (int i = 0; i < tm.getRowCount(); i++) {
            t = (org.openide.nodes.Node.Property) tm.getValueAt(i, 1);
            properties.put(tm.getValueAt(i, 0).toString(), t.getValue().toString());
        }

        return properties;
    }

    /**
     * Returns HashMap&lt;String, String&gt; of all properties from currently
     * focused rule (Rule editor part)
     *
     * @return
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     */
    public HashMap<String, String> getRuleEditorProperties() throws IllegalAccessException, InvocationTargetException {
        JTableOperator _tabTreeTable = new JTableOperator(sppPseudoClasses(), 1);
        TableModel tm = _tabTreeTable.getModel();
        HashMap<String, String> properties = new HashMap<String, String>();
        RuleEditorNode.DeclarationProperty t;
        for (int i = 0; i < tm.getRowCount(); i++) {
            if (tm.getValueAt(i, 0) instanceof RuleEditorNode.DeclarationProperty) {
                t = (RuleEditorNode.DeclarationProperty) tm.getValueAt(i, 0);
                properties.put(t.getDisplayName(), t.getValue().toString());
            }
        }

        return properties;
    }

    /**
     * Clicks (focuses) element's property in CSS Styles|Selection view (top
     * part of window)
     *
     * @param propertyName CSS property (e.g. width)
     * @throws NullPointerException in case property is not found
     */
    public void focusProperty(String propertyName) throws NullPointerException {
        JTableOperator _tabTreeTable = new JTableOperator(sppProperties());
        TableModel tm = _tabTreeTable.getModel();
        for (int i = 0; i < tm.getRowCount(); i++) {
            if (tm.getValueAt(i, 0).toString().equalsIgnoreCase(propertyName)) {
                _tabTreeTable.clickOnCell(i, 0);
                return;
            }
        }
        throw new NullPointerException("Property \"" + propertyName + "\" not found");
    }

    /**
     * Returns String[] containing property name and property value of currently
     * selected row in Rule Editor in CSS Styles
     *
     * @return
     */
    public String[] getFocusedProperty() {
        JTableOperator _tabTreeTable = new JTableOperator(sppPseudoClasses(), 1);
        TableModel tm = _tabTreeTable.getModel();
        RuleEditorNode.DeclarationProperty t = (RuleEditorNode.DeclarationProperty) tm.getValueAt(_tabTreeTable.getSelectedRow(), 0);
        return new String[]{t.getDisplayName(), t.getValue().toString()};
    }

    /**
     * Edits property value in Rule Editor by selecting given cell and typing
     * the value
     *
     * @param propertyName
     * @param propertyValue
     * @throws NullPointerException in case property is not found
     */
    public void editProperty(String propertyName, String propertyValue) throws NullPointerException {
        JTableOperator _tabTreeTable = new JTableOperator(sppPseudoClasses(), 1);
        TableModel tm = _tabTreeTable.getModel();
        RuleEditorNode.DeclarationProperty t;

        for (int i = 0; i < tm.getRowCount(); i++) {
            if (tm.getValueAt(i, 0) instanceof RuleEditorNode.DeclarationProperty) {
                t = (RuleEditorNode.DeclarationProperty) tm.getValueAt(i, 0);
                if (t.getDisplayName().equalsIgnoreCase(propertyName)) {

                    _tabTreeTable.clickOnCell(i, 1);
                    _tabTreeTable.pressKey(KeyEvent.VK_BACK_SPACE);

                    for (int k = 0; k < propertyValue.length(); k++) {
                        _tabTreeTable.typeKey(propertyValue.charAt(k));
                    }

                    _tabTreeTable.pressKey(KeyEvent.VK_ENTER);
                    return;
                }
            }
        }

        throw new NullPointerException("Property \"" + propertyName + "\" not found");
    }

    /**
     * Removes property from Rule Editor part of window using context menu
     *
     * @param propertyName
     * @throws NullPointerException in case property is not found
     */
    public void removeProperty(String propertyName) throws NullPointerException {
        JTableOperator _tabTreeTable = new JTableOperator(sppPseudoClasses(), 1);
        TableModel tm = _tabTreeTable.getModel();
        RuleEditorNode.DeclarationProperty t;

        for (int i = 0; i < tm.getRowCount(); i++) {
            if (tm.getValueAt(i, 0) instanceof RuleEditorNode.DeclarationProperty) {
                t = (RuleEditorNode.DeclarationProperty) tm.getValueAt(i, 0);
                if (t.getDisplayName().equalsIgnoreCase(propertyName)) {
                    (new JPopupMenuOperator(_tabTreeTable.callPopupOnCell(i, 0))).pushMenu("Remove Property");
                    return;
                }
            }
        }

        throw new NullPointerException("Property \"" + propertyName + "\" not found");
    }

    /**
     * Adds a new property via "Add property" row in Rule Editor in currently
     * used rule
     *
     * @param propertyName CSS property (e.g. width)
     * @param propertyValue value of property
     */
    public void addProperty(String propertyName, String propertyValue) {
        JTableOperator _tabTreeTable = new JTableOperator(sppPseudoClasses(), 1);
        TableModel tm = _tabTreeTable.getModel();

        _tabTreeTable.clickOnCell(tm.getRowCount() - 1, 0);
        for (int j = 0; j < propertyName.length(); j++) {
            _tabTreeTable.typeKey(propertyName.charAt(j));
        }
        _tabTreeTable.pressKey(KeyEvent.VK_ENTER);
        _tabTreeTable.clickForEdit(tm.getRowCount() - 1, 1);

        for (int k = 0; k < propertyValue.length(); k++) {
            _tabTreeTable.typeKey(propertyValue.charAt(k));
        }
        _tabTreeTable.pressKey(KeyEvent.VK_ENTER);
    }

    /**
     * Returns array of applied rules (middle part of CSS Styles window)
     *
     * @return
     */
    public AppliedRule[] getAppliedRules() {
        JListOperator op = lstAppliedRules();
        int size = op.getModel().getSize();
        AppliedRule[] rules = new AppliedRule[size];
        AppliedRule rule;
        String tmp;
        JPanel p;
        JPanel panel;

        for (int i = 0; i < size; i++) {

            panel = (JPanel) op.getRenderedComponent(i);
            rule = new AppliedRule();
            rule.atRule = ((JLabel) panel.getComponent(3)).getText();// hardcoded order...

            tmp = ((JLabel) panel.getComponent(0)).getText();
            if (tmp.contains(">")) {
                rule.selector = tmp.substring(tmp.indexOf(">") + 1);
            } else {
                rule.selector = tmp;
            }

            tmp = ((JLabel) panel.getComponent(1)).getText();
            if (tmp.contains(">")) {
                rule.path = tmp.substring(tmp.indexOf(">") + 1);
            } else {
                rule.path = tmp;
            }

            p = (JPanel) panel.getComponent(2);
            tmp = ((JLabel) p.getComponent(0)).getText();
            if (tmp.contains("u>")) {
                rule.source = tmp.substring(tmp.indexOf("u>") + 2);
            } else {
                rule.source = tmp;
            }
            rules[i] = rule;
        }
        return rules;
    }

    public void focusRule(String ruleName) throws NullPointerException {
        focusRule(ruleName, 0);
    }

    /**
     * Clicks on rule in Applied Rules section (middle part of CSS Styles
     * window)
     *
     * @param ruleName rule name
     * @param index in case one rule is multiple times in css file, you can
     * specify which occurrence of the rule should be focused (index of first is
     * 0)
     * @throws NullPointerException in case rule is not found
     */
    public void focusRule(String ruleName, int index) throws NullPointerException {
        JListOperator op = lstAppliedRules();
        int size = op.getModel().getSize();
        String tmp;
        JPanel p;
        JPanel panel;
        int ruleFound = 0;
        for (int i = 0; i < size; i++) {

            panel = (JPanel) op.getRenderedComponent(i);
            tmp = ((JLabel) panel.getComponent(0)).getText();
            if (tmp.contains(">")) {
                tmp = tmp.substring(tmp.indexOf(">") + 1);
            }

            if (tmp.equalsIgnoreCase(ruleName)) {
                if (ruleFound == index) {
                    op.clickOnItem(i, 1);
                    return;
                } else {
                    ruleFound++;
                }
            }
        }

        throw new NullPointerException("Rule \"" + ruleName + "\" not found");
    }

    /**
     * Changes value of given CSS property using arrow keys. The property must
     * be in Rule Editor
     *
     * @param propertyName name of CSS property to be changed
     * @param buttonPresses how many times should the arrow key be pressed
     * @param up if false, then down arrow key will be pressed, if true then up
     * @param clickToEdit if true, double click on cell is done to switch to
     * edit mode
     * @param confirmValue if true, Enter is pressed at the end so cell is not
     * editable arrow key
     * @throws NullPointerException in case property is not found
     */
    public void editNumberedProperty(String propertyName, int buttonPresses, boolean up, boolean clickToEdit, boolean confirmValue) throws NullPointerException {

        JTableOperator _tabTreeTable = new JTableOperator(sppPseudoClasses(), 1);
        TableModel tm = _tabTreeTable.getModel();
        RuleEditorNode.DeclarationProperty t;

        for (int i = 0; i < tm.getRowCount(); i++) {
            if (tm.getValueAt(i, 0) instanceof RuleEditorNode.DeclarationProperty) {
                t = (RuleEditorNode.DeclarationProperty) tm.getValueAt(i, 0);
                if (t.getDisplayName().equalsIgnoreCase(propertyName)) {
                    if (clickToEdit) {
                        _tabTreeTable.clickOnCell(i, 1);
                    }
                    if (!up) {
                        for (int j = 0; j < buttonPresses; j++) {
                            _tabTreeTable.pressKey(KeyEvent.VK_DOWN);
                        }
                    } else {
                        for (int j = 0; j < buttonPresses; j++) {
                            _tabTreeTable.pressKey(KeyEvent.VK_UP);
                        }
                    }
                    if (confirmValue) {
                        _tabTreeTable.pressKey(KeyEvent.VK_ENTER);
                    }
                    return;
                }
            }
        }

        throw new NullPointerException("Property \"" + propertyName + "\" not found");
    }
    
    /**
     * Simply presses up/down buttons given times
     *
     * @param buttonPresses how many times up/down button should be pressed
     * @param up if true, up button is pressed, otherwise down button
     * @param confirm if true, Enter is press at the end
     */
    public void pressUpDownButtons(int buttonPresses, boolean up, boolean confirm) {
        JTableOperator _tabTreeTable = new JTableOperator(sppPseudoClasses(), 1);
        if (!up) {
            for (int j = 0; j < buttonPresses; j++) {
                _tabTreeTable.pressKey(KeyEvent.VK_DOWN);
            }
        } else {
            for (int j = 0; j < buttonPresses; j++) {
                _tabTreeTable.pressKey(KeyEvent.VK_UP);
            }
        }
        if (confirm) {
            _tabTreeTable.pressKey(KeyEvent.VK_ENTER);
        }
    }
}

class AppliedRule {

    /**
     * Left column in Applied Rules (e.g. #main)
     */
    public String selector;
    /**
     * Target css file and line number, e.g. styles.css:2
     */
    public String source;
    public String atRule;
    /**
     * Right column in Applied Rules
     */
    public String path;
}