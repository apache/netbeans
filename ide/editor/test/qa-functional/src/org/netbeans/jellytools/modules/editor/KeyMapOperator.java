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
package org.netbeans.jellytools.modules.editor;

import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.util.Vector;
import javax.swing.ComboBoxModel;
import javax.swing.JButton;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.OptionsOperator;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JComboBoxOperator;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.jemmy.operators.JListOperator;
import org.netbeans.jemmy.operators.JPopupMenuOperator;
import org.netbeans.jemmy.operators.JTableOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;
import org.openide.util.Exceptions;

/**
 *
 * @author Jiri Prox Jiri.Prox@Sun.COM
 */
public class KeyMapOperator extends JDialogOperator {

    private static final String DUPLICATE_BUTTON = "Duplicate";
    private static final String RESTORE_BUTTON = "Restore";
    private static final String DELETE_BUTTON = "Delete";
    private JComboBoxOperator profile;
    private JTableOperator actionsTable;
    private JButtonOperator manageProfilesButton;
    private JTextFieldOperator actionSearchByName;
    private JTextFieldOperator actionSearchByShortcut;
    private static OptionsOperator options;
    
    private static boolean firstInvocation = true;

    /** Creates a new instance of KeyMapOperator */
    public KeyMapOperator() {
        super("Options");
        System.out.println("[TEST_DEBUG] KeyMapOperator ");
        System.out.println("[TEST_DEBUG] ===============");
    }

    public static KeyMapOperator invoke() {
        options = OptionsOperator.invoke();
        options.selectKeymap();
        if(firstInvocation) {
          new EventTool().waitNoEvent(10000);
          firstInvocation = false;
        } else {
          new EventTool().waitNoEvent(500);
        }
        return new KeyMapOperator();
    }

    public JComboBoxOperator profile() {
        if (profile == null) {
            profile = new JComboBoxOperator(this);
        }
        return profile;
    }

    public JTableOperator actionsTable() {
        if (actionsTable == null) {
            actionsTable = new JTableOperator(this);
        }
        return actionsTable;
    }

    public JButtonOperator manageProfilesButton() {
        if (manageProfilesButton == null) {
            manageProfilesButton = new JButtonOperator(this, "Manage Profiles...");
        }
        return manageProfilesButton;
    }

    public JTextFieldOperator actionSearchByName() {
        if (actionSearchByName == null) {
            actionSearchByName = new JTextFieldOperator(this, 0);
        }
        return actionSearchByName;
    }

    public JTextFieldOperator actionSearchByShortcut() {
        if (actionSearchByShortcut == null) {
            actionSearchByShortcut = new JTextFieldOperator(this, 1);
        }
        return actionSearchByShortcut;
    }

    public void searchActionName(String actionName) {
        actionSearchByName().setText(actionName);
        sleep(1000);
    }

    public TableModel getActionsTableModel() {
        JTableOperator tab = actionsTable();
        return tab.getModel();
    }

    private int buildKeyModifierMask(boolean ctrl, boolean alt, boolean shift) {
        int _mask = 0;
        if (ctrl) {
            _mask = _mask | KeyEvent.CTRL_DOWN_MASK;
        }
        if (alt) {
            _mask = _mask | KeyEvent.ALT_DOWN_MASK;
        }
        if (shift) {
            _mask = _mask | KeyEvent.SHIFT_DOWN_MASK;
        }
        return _mask;
    }

    private JListOperator clickShortcutEllipsisButton(JTableOperator tab, int row) {
        TableModel tm = tab.getModel();
        TableCellRenderer cellRenderer = tab.getCellRenderer(row, 2);
        Rectangle cellRect = tab.getCellRect(row, 1, true);
        //org.netbeans.modules.options.keymap.ShortcutCellPanel sc = (org.netbeans.modules.options.keymap.ShortcutCellPanel) tm.getValueAt(row, 1);
//        final JButton button = sc.getButton();
//        int x = button.getX() + button.getWidth() / 2;
//        int y = button.getY() + button.getHeight() / 2;
        Rectangle r = tab.getCellRect(row, 1, false);
        tab.clickMouse(r.x + r.width-3, r.y + (r.height/2), 1);
        System.out.println("[TEST_DEBUG]  Pressed [...] button on row " + (row + 1));
        return new JListOperator(new JPopupMenuOperator());
    }

    private void injectKeyBinding(JTableOperator tab, int Key, int mask) {
        tab.pushKey(Key, mask);
        tab.pushKey(KeyEvent.VK_ENTER);
        System.out.println("[TEST_DEBUG]  -> pressing shortcut: [key,mask] = [" + Key + "," + mask + "]");
    }

    public Vector<String> getAllShortcutsForAction(String actionName) {
        System.out.println("[TEST_DEBUG]");
        System.out.println("[TEST_DEBUG] ### Examining all shortcuts for action: " + actionName);
        Vector<String> lstr = new Vector<String>();
        String tmpStr = actionSearchByName().getText();
        searchActionName(actionName);
        TableModel tm = getActionsTableModel();
        String _str;
        String _scStr;
        for (int i = 0; i < tm.getRowCount(); i++) {
            _str = tm.getValueAt(i, 0).toString();
            if (_str.equalsIgnoreCase(actionName) || _str.equalsIgnoreCase(actionName + " (alternative shortcut)")) {
                _scStr = tm.getValueAt(i, 1).toString().toLowerCase();
                lstr.add(_scStr);
                System.out.println("[TEST_DEBUG]  -> found action \"" + _str + "\" with shortcut " + _scStr);
            }
        }
        searchActionName(tmpStr);
        System.out.println("[TEST_DEBUG] ### Examining all shortcuts done");
        return lstr;
    }

    public boolean assignShortcutToAction(String actionName, boolean ctrl, boolean shift, boolean alt, int Key) {
        return assignShortcutToAction(actionName, ctrl, shift, alt, Key, false, false);
    }

    public boolean assignShortcutToAction(String actionName, boolean ctrl, boolean shift, boolean alt, int Key, boolean expectedAlreadyAssigned, boolean reassign) {
        System.out.println("[TEST_DEBUG]");
        System.out.println("[TEST_DEBUG] ### Reassigning shortcut for " + actionName + " - Started");
        int mask = buildKeyModifierMask(ctrl, alt, shift);
        String tmpStr = actionSearchByName().getText();
        searchActionName(actionName);
        JTableOperator tab = actionsTable();
        TableModel tm = tab.getModel();
        String _str;
        System.out.println("[TEST_DEBUG]  Found " + tab.getRowCount() + " actions matching action pattern: " + actionName);
        for (int i = 0; i < tab.getRowCount(); i++) {
            _str = tm.getValueAt(i, 0).toString();
            System.out.println("[TEST_DEBUG]  Examining action \"" + _str + "\", which is no. " + (i + 1) + " in the table...");
            if (_str.equalsIgnoreCase(actionName)) {
                System.out.println("[TEST_DEBUG]  -> action \"" + _str + "\" (" + actionName + ") was found");
                sleep(100);
                tab.clickForEdit(i, 1);
                sleep(100);
                injectKeyBinding(tab, Key, mask);
                if (expectedAlreadyAssigned) {
                    if (reassign) {
                        new NbDialogOperator("Conflicting Shortcut Dialog").yes();
                    } else {
                        new NbDialogOperator("Conflicting Shortcut Dialog").cancel();
                    }
                }
                sleep(100);
                break;
            }
        }
        searchActionName(tmpStr);
        System.out.println("[TEST_DEBUG] ### Reassigning shortcut for " + actionName + " - OK");
        return true;
    }

    public boolean assignAlternativeShortcutToAction(String actionName, boolean ctrl, boolean shift, boolean alt, int Key) {
        return assignAlternativeShortcutToAction(actionName, ctrl, shift, alt, Key, false, false);
    }

    public boolean assignAlternativeShortcutToAction(String actionName, boolean ctrl, boolean shift, boolean alt, int Key, boolean expectedAlreadyAssigned, boolean reassign) {
        boolean retval = false;
        System.out.println("[TEST_DEBUG]");
        System.out.println("[TEST_DEBUG] ### Assigning alternative shortcut for " + actionName + " - Started");
        int mask = buildKeyModifierMask(ctrl, alt, shift);
        String tmpStr = actionSearchByName().getText();
        searchActionName(actionName);
        JTableOperator tab = actionsTable();
        TableModel tm = tab.getModel();
        String _str;
        System.out.println("[TEST_DEBUG]  Found " + tab.getRowCount() + " actions matching action pattern: " + actionName);
        for (int i = 0; i < tab.getRowCount(); i++) {
            _str = tm.getValueAt(i, 0).toString();
            System.out.println("[TEST_DEBUG]  Examining action " + _str + ", which is no. " + (i + 1) + "in the table...");
            if (_str.equalsIgnoreCase(actionName)) {
                System.out.println("[TEST_DEBUG]  Action " + actionName + "was found");
                JListOperator jli = clickShortcutEllipsisButton(tab, i);
                retval = true;
                try {
                    jli.clickOnItem("Add Alternative");
                } catch (Exception e) {
                    retval = false;
                }
                sleep(100);
                injectKeyBinding(tab, Key, mask);
                if (expectedAlreadyAssigned) {
                    if (reassign) {
                        new NbDialogOperator("Conflicting Shortcut Dialog").yes();
                    } else {
                        new NbDialogOperator("Conflicting Shortcut Dialog").cancel();
                    }
                }
                sleep(100);
                System.out.println("[TEST_DEBUG] ### Assigning alternative shortcut for " + actionName + " - OK");
                break;
            }
        }
        searchActionName(tmpStr);
        return retval;
    }

    public boolean unassignAlternativeShortcutToAction(String actionName, String shortcutStr) {
        System.out.println("[TEST_DEBUG]");
        System.out.println("[TEST_DEBUG] ### Unassigning alternative shortcut for " + actionName + " - Started");
        String tmpStr = actionSearchByName().getText();
        searchActionName(actionName);
        JTableOperator tab = actionsTable();
        TableModel tm = tab.getModel();
        String _str;
        System.out.println("[TEST_DEBUG]  Found " + tab.getRowCount() + " actions matching action pattern: " + actionName);
        for (int i = 0; i < tab.getRowCount(); i++) {
            _str = tm.getValueAt(i, 0).toString();
            System.out.println("[TEST_DEBUG]  Examining action " + _str + ", which is no. " + (i + 1) + "in the table...");
            if (_str.toLowerCase().startsWith(actionName.toLowerCase()) && tm.getValueAt(i, 1).toString().equalsIgnoreCase(shortcutStr)) {
                System.out.println("[TEST_DEBUG]  Action " + actionName + "was found");
                JListOperator jli = clickShortcutEllipsisButton(tab, i);
                jli.clickOnItem("Clear");
                sleep(100);
                System.out.println("[TEST_DEBUG] ### Unassigning alternative shortcut for " + actionName + " - OK");
                break;
            }
        }
        searchActionName(tmpStr);
        return true;
    }

    public void restoreProfile(String profileName) {
        manageProfilesButton().push();
        ManageProfilesDialogOperator mpdo = new ManageProfilesDialogOperator();
        mpdo.Restore(profileName);
        mpdo.close();
    }

    public void duplicateProfile(String profileNameOrig, String profileNameNew) {
        manageProfilesButton().push();
        ManageProfilesDialogOperator mpdo = new ManageProfilesDialogOperator();
        mpdo.Duplicate(profileNameOrig, profileNameNew);
        mpdo.close();
    }

    public void checkProfilesPresent(String... profileNames) {
        manageProfilesButton().push();
        ManageProfilesDialogOperator mpdo = new ManageProfilesDialogOperator();
        mpdo.checkProfileListContent(profileNames);
        mpdo.close();
    }

    public JButtonOperator ok() {
        return options.btOK();
    }

    public JButtonOperator cancel() {
        return options.btCancel();
    }

    public JButtonOperator help() {
        return options.btHelp();
    }

    public void selectProfile(String profile) {
        JComboBoxOperator combo = profile();
        if (combo.getSelectedItem().toString().equals(profile)) {
            return; //no need to switch profile
        }
        ComboBoxModel model = combo.getModel();
        for (int i = 0; i < model.getSize(); i++) {
            Object item = model.getElementAt(i);
            if (item.toString().equals(profile)) {
                combo.setSelectedIndex(i);
                return;
            }
        }
        throw new IllegalArgumentException("Profile " + profile + " not found");
    }

    public void verify() {
        profile();
        actionsTable();
        actionSearchByName();
        actionSearchByShortcut();
        manageProfilesButton();
        ok();
        cancel();
        help();
    }

    private void sleep(int miliseconds) {
        try {
            Thread.sleep(miliseconds);
        } catch (InterruptedException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
}
