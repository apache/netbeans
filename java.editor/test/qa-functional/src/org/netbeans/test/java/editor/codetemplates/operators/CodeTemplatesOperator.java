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

package org.netbeans.test.java.editor.codetemplates.operators;

import java.awt.Component;
import java.awt.Container;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.OptionsOperator;
import org.netbeans.jellytools.TopComponentOperator;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.operators.ContainerOperator;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JCheckBoxOperator;
import org.netbeans.jemmy.operators.JComboBoxOperator;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.jemmy.operators.JEditorPaneOperator;
import org.netbeans.jemmy.operators.JListOperator;
import org.netbeans.jemmy.operators.JTabbedPaneOperator;
import org.netbeans.jemmy.operators.JTableHeaderOperator;
import org.netbeans.jemmy.operators.JTableOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;

/**
 *
 * @author jprox
 */
public class CodeTemplatesOperator extends NbDialogOperator {
    
    private static OptionsOperator optionsOperator = null;
    private ContainerOperator<Container> panel;
    
    private static List<String[]> defaultTemplates;

    public static List<String[]> getDefaultTemplates() {
        return defaultTemplates;
    }
        
    
    
    public static CodeTemplatesOperator invoke(boolean openOptions) {
        if (openOptions) {
            optionsOperator = OptionsOperator.invoke();
        } else {
            optionsOperator = new OptionsOperator();
        }
        CodeTemplatesOperator codeTemplatesOperator = new CodeTemplatesOperator((JDialog) optionsOperator.getSource());
        codeTemplatesOperator.switchToTemplates();
        if(defaultTemplates==null) { // first invocation -> save default values for java
            codeTemplatesOperator.switchLanguage("Java");
            defaultTemplates = codeTemplatesOperator.dumpTemplatesTable();
        }
        return codeTemplatesOperator;
    }

    private void switchToTemplates() {
        optionsOperator.selectEditor();
        Component findComponent = optionsOperator.findSubComponent(new JTabbedPaneOperator.JTabbedPaneFinder());
        JTabbedPaneOperator tabbedPane = new JTabbedPaneOperator((JTabbedPane) findComponent);
        tabbedPane.selectPage("Code Templates");
        panel = new ContainerOperator<>((Container) tabbedPane.getSelectedComponent());
    }

    public OptionsOperator getOptionsOperator() {
        return optionsOperator;
    }

    

    private CodeTemplatesOperator(JDialog dialog) {
        super(dialog);
    }
    
    public CodeTemplatesOperator switchLanguage(String language) {
        JComboBoxOperator c = getLanguageCombo();
        c.selectItem(language);
        return this;
    }
    
    public CodeTemplatesOperator addNewTemplate(String abbrev) {
        getNewBt().pushNoBlock();        
        new EventTool().waitNoEvent(250);
        JDialogOperator newDialog = new JDialogOperator("New Code Template");        
        JTextFieldOperator textField = new JTextFieldOperator(newDialog);
        textField.typeText(abbrev);
        new EventTool().waitNoEvent(250);
        JButtonOperator jbo = new JButtonOperator(newDialog, "OK");
        jbo.pushNoBlock();
        new EventTool().waitNoEvent(500);
        return this;
    }
    
    public CodeTemplatesOperator addNewTemplate(String abbrev,String expandedText) {
        boolean exists = selectTemplate(abbrev);
        if(exists) return this;
        addNewTemplate(abbrev);
        selectTemplate(abbrev);
        setExtendedText(expandedText);
        return this;
    }
    
    public boolean removeTemplate(String abbrev) {
        boolean res = selectTemplate(abbrev);
        new EventTool().waitNoEvent(250);
        getRemoveBt().pushNoBlock();
        return res;
    }

    
    public void removeActualTemplate() {        
        getRemoveBt().pushNoBlock();
        
    }
    
    public boolean selectTemplate(String abbrev) {
        JTableOperator templatesTable1 = getTemplatesTable();
        int row = templatesTable1.findCellRow(abbrev, 0, 0);
        if(row==-1) return false;
        templatesTable1.selectCell(row, 0);
        return true;                
    }
    
    public List<String[]> dumpTemplatesTable() {
        List<String[]> res = new LinkedList<>();
        JTableOperator tableOperator = getTemplatesTable();
        for (int i = 0; i < tableOperator.getRowCount(); i++) {
            String[] items = new String[3];
            for (int j = 0; j < 3; j++) {
                items[j] = (String) tableOperator.getValueAt(i, j);
            }
            res.add(items);
        }
        return res;
    }
    
    public String getSelectedTemplate() {
        JTableOperator tableOperator = getTemplatesTable();
        int selectedRow = tableOperator.getSelectedRow();
        if(selectedRow<0) return null;
        return (String) tableOperator.getValueAt(selectedRow, 0);
    }
    
    public CodeTemplatesOperator setExtendedText(String text) {
        getEditorOnTab("Expanded Text").setText(text);
        return this;
        
    }

    public CodeTemplatesOperator setDescription(String description) {
        getEditorOnTab("Description").setText(description);
        return this;
    }
    
    private JEditorPaneOperator getEditorOnTab(final String tabName) {
        JTabbedPaneOperator tabbedPane = getTabbedPane();
        tabbedPane.selectPage(tabName);
        ContainerOperator<JEditorPane> selectedComponent = new ContainerOperator<>((Container)tabbedPane.getSelectedComponent());
        JEditorPaneOperator jepo = new JEditorPaneOperator(selectedComponent);
        return jepo;
    }
    
    public CodeTemplatesOperator setContext(Set<String> set) {
        JTabbedPaneOperator tabbedPane = getTabbedPane();
        tabbedPane.selectPage("Contexts");
        ContainerOperator<JEditorPane> selectedComponent = new ContainerOperator<>((Container)tabbedPane.getSelectedComponent());
        JListOperator list = new JListOperator(selectedComponent);
        for (int i = 0; i < list.getModel().getSize(); i++) {
            JCheckBox checkBox = (JCheckBox) list.getRenderedComponent(i);
            String contextName = checkBox.getText();
            list.scrollToItem(i);
            if(!checkBox.isSelected() && set.contains(contextName)) {
                list.selectItem(i);
            } else if(checkBox.isSelected() && !set.contains(contextName)) {
                list.selectItem(i);
            }            
        }   
        return this;
        
    }
    
    public String getExtendedText() {
        return getEditorOnTab("Expanded Text").getText();
    }
    
    public String getDescription() {
        return getEditorOnTab("Description").getText();
    }
        
    public Set<String> getContexts() {
        Set<String> result = new HashSet<>();
        JTabbedPaneOperator tabbedPane = getTabbedPane();
        tabbedPane.selectPage("Contexts");
        ContainerOperator<JEditorPane> selectedComponent = new ContainerOperator<>((Container)tabbedPane.getSelectedComponent());
        JListOperator list = new JListOperator(selectedComponent);
        for (int i = 0; i < list.getModel().getSize(); i++) {
            JCheckBox checkBox = (JCheckBox) list.getRenderedComponent(i);            
            if(checkBox.isSelected())  {
                result.add(checkBox.getText());                
            }                         
        }        
        return result;
    }
            
    public static enum ExpandTemplateOn {
        TAB("Tab",KeyEvent.VK_TAB,0),
        SPACE("Space",KeyEvent.VK_SPACE,0),
        SHIFTSPACE("Shift+Space",KeyEvent.VK_SPACE,KeyEvent.SHIFT_DOWN_MASK),
        ENTER("Enter",KeyEvent.VK_ENTER,0);
        
        private final String displayName;        
        private final int keyCode;
        private final int keyModifier;

        public int getKeyCode() {
            return keyCode;
        }

        public int getKeyModifier() {
            return keyModifier;
        }                

        private ExpandTemplateOn(String displayName, int keyCode, int keyModifier) {
            this.displayName = displayName;
            this.keyCode = keyCode;
            this.keyModifier = keyModifier;
        }        

        public String getDisplayName() {
            return displayName;
        }
    }
    
    public CodeTemplatesOperator setExpandTemplateOn(ExpandTemplateOn on) {
        getExpandOnCombo().selectItem(on.getDisplayName());
        return this;
    }
    
    public static enum OnExpansion {
        REFORMAT("Reformat Text"),REINDENT("Reindent Text"),NOTHING("Do Nothing");
        
        private final String displayName;

        private OnExpansion(String displayName) {
            this.displayName = displayName;
        }        

        public String getDisplayName() {
            return displayName;
        }                
    }
    
    public CodeTemplatesOperator setOnExpansion(OnExpansion action) {
        getOnExpansionCombo().selectItem(action.getDisplayName());
        return this;
    }
    
    public CodeTemplatesOperator changeOrder(int columnIndex) {
        JTableHeaderOperator headerOperator = getTemplatesTable().getHeaderOperator();
        Point pointToClick = headerOperator.getPointToClick(columnIndex);
        headerOperator.clickMouse(pointToClick.x, pointToClick.y, 1);
        return this;
    }
    
    public CodeTemplatesOperator selectLine(int i) {
        getTemplatesTable().selectCell(i, 0);
        return this;
    }
    
    
    
    private JComboBoxOperator languageCombo;
    private JComboBoxOperator expandOnCombo;
    private JComboBoxOperator onExpansionCombo;
    
    private JButtonOperator newBt;
    private JButtonOperator removeBt;
    
    
    private JTabbedPaneOperator tabbedPane;
    private JTableOperator templatesTable;

    
    public JComboBoxOperator getLanguageCombo() {
        if(languageCombo==null) {
            languageCombo = new JComboBoxOperator(panel, 0);
        }
        return languageCombo;
    }

    public JComboBoxOperator getExpandOnCombo() {
        if(expandOnCombo==null) {
            expandOnCombo = new JComboBoxOperator(panel, 1);
        }
        return expandOnCombo;
    }

    public JComboBoxOperator getOnExpansionCombo() {
        if(onExpansionCombo==null) {
            onExpansionCombo = new JComboBoxOperator(panel, 2);
        }
        return onExpansionCombo;
    }

    public JButtonOperator getNewBt() {
        if(newBt==null) {
            newBt = new JButtonOperator(panel,"New");
        }
        return newBt;
    }

    public JButtonOperator getRemoveBt() {
        if(removeBt==null) {
            removeBt = new JButtonOperator(panel,"Remove");
        }
        return removeBt;
    }

    public JTabbedPaneOperator getTabbedPane() {
        if(tabbedPane==null) {
            tabbedPane = new JTabbedPaneOperator(panel);
        }
        return tabbedPane;
    }

    public JTableOperator getTemplatesTable() {
        if(templatesTable==null) {
            templatesTable = new JTableOperator(panel);
        }
        return templatesTable;
    }                
}
