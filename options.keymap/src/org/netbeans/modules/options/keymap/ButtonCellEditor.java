/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.options.keymap;

import java.awt.Color;
import java.awt.Component;
import java.awt.Window;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Arrays;
import java.util.Collection;
import java.util.EventObject;
import java.util.Set;
import javax.swing.DefaultCellEditor;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.plaf.ColorUIResource;

import org.netbeans.core.options.keymap.api.ShortcutAction;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.NbBundle;


/**
 * Cell Editor for shortcuts column
 * @author Max Sauer
 */
class ButtonCellEditor extends DefaultCellEditor {

    private Object              action;
    private KeymapViewModel     model;
    private String              orig;
    
    private KeyAdapter escapeAdapter = new KeyAdapter() {

        @Override
        public void keyPressed(KeyEvent e) {
            if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                JTable table = (JTable) cell.getParent();
                table.getCellEditor().cancelCellEditing();
                model.update();
            }
        }
    };
    
    private static final ShortcutCellPanel cell = new ShortcutCellPanel();


    public ButtonCellEditor (KeymapViewModel model) {
        super(new ShortcutTextField());
        this.model = model;
    }

    @Override
    public boolean shouldSelectCell(EventObject anEvent) {
        return true;
    }

    private void removeConflictingShortcut(ShortcutAction action, String shortcutPrefix) {
        if (shortcutPrefix.contains(" ")) {//multi-key shortcuts conflict
            shortcutPrefix = shortcutPrefix.substring(0, shortcutPrefix.indexOf(' '));
        }
        String[] shortcuts = model.getMutableModel().getShortcuts(action);
        for (int i = 0; i < shortcuts.length; i++) {
            if (shortcuts[i].startsWith(shortcutPrefix)) {
                model.getMutableModel().removeShortcut(action, shortcuts[i]);
            }
        }
    }
    
    @Override
    public boolean stopCellEditing() {
        String s = cell.toString();
        Window ancestorWindow = (Window)SwingUtilities.getRoot(cell);
        // #236458: options are now saved asynchronously. If the dialog was to 
        if (ancestorWindow == null) {
            return true;
        }
        // HACK: if this Editor creates a dialog, it will lose the focus and Swing
        // will remove the editor, calling JTable.cancelEditing. Any re-selections performed
        // by the JTable will occur BEFORE the dialog is finished, so we need to
        // reestablish the column selection later from here.
        // This binds the BCEditor to the KeymapTable layout / internals.
        JTable parent = (JTable)cell.getParent();
        
        ShortcutAction sca = (ShortcutAction) action;
        Set<ShortcutAction> conflictingAction = model.getMutableModel().findActionForShortcutPrefix(s);
        conflictingAction.remove(sca); //remove the original action
        
        Collection<ShortcutAction> sameScopeActions = model.getMutableModel().filterSameScope(conflictingAction, sca);
        
        if (!conflictingAction.isEmpty()) {
             if (!SwingUtilities.isEventDispatchThread()) {
                 // #236458: options are now saved asynchronously, off EDT. If we display dialog, the IDE will lock up.
                cell.getTextField().setText(orig);
                fireEditingCanceled();
                return true;
             }
            //there is a conflicting action, show err dialog
            Object overrride = overrride(conflictingAction, sameScopeActions);
            
            // bring the focus back
            ancestorWindow.toFront();
            parent.requestFocus();
            if (overrride.equals(DialogDescriptor.YES_OPTION)) {
                for (ShortcutAction sa : conflictingAction) {
                    removeConflictingShortcut(sa, s); //remove all conflicting shortcuts
                }
                //proceed with override
            } else if (overrride == DialogDescriptor.CANCEL_OPTION) {
                cell.getTextField().setText(orig);
                fireEditingCanceled();
                setBorderEmpty();
                return true;
            }
            // NO_OPTION fallls through and adds additional shortcut.
        }
        cell.getTextField().removeActionListener(delegate);
        cell.getTextField().removeKeyListener(escapeAdapter);
        model.getMutableModel().removeShortcut((ShortcutAction) action, orig);
        if (!(s.length() == 0)) // do not add empty shortcuts
            model.getMutableModel().addShortcut((ShortcutAction) action, s);
        fireEditingStopped();
        setBorderEmpty();
        model.update();
        return true;
    }

    @Override
    public void cancelCellEditing() {
        cell.getTextField().setText(orig);
        fireEditingCanceled();
        setBorderEmpty();
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value,
            boolean isSelected,
            int row, int column) {
        JComponent c = (JComponent)super.getTableCellEditorComponent(table, value, isSelected, row, column);
        cell.setText((String) value);
        this.orig = cell.getTextField().getText();
        this.action = ((ActionHolder) table.getValueAt(row, 0)).getAction();
        final JTextField textField = cell.getTextField();
        textField.addActionListener(delegate);
        textField.setBorder(new LineBorder(Color.BLACK));
        if(!Arrays.asList(textField.getKeyListeners()).contains(escapeAdapter)) {
            textField.addKeyListener(escapeAdapter);
        }
        // allow the UI delegate to replace the background with more sensible color
        cell.setBgColor(c.getBackground());
        cell.setFgCOlor(c.getForeground(), false);
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                textField.requestFocus();
            }
        });
        return cell;
    }

    @Override
    public Object getCellEditorValue() {
        return cell.getTextField().getText();
    }

    @Override
    public Component getComponent() {
        return cell.getTextField();
    }

    /**
     * Shows dialog where user chooses whether SC of conflicting action should be overridden
     * @param displayName name of conflicting action
     * @return dialog result
     */
    private Object overrride(Set<ShortcutAction> conflictingActions, Collection<ShortcutAction> sameScope) {
        StringBuffer conflictingActionList = new StringBuffer();

        for (ShortcutAction sa : conflictingActions) {
            conflictingActionList.append("<li>'").append (sa.getDisplayName()).append ("'</li>"); //NOI18N
        }
        
        JPanel innerPane = new JPanel();
        
        innerPane.add(new JLabel(NbBundle.getMessage(ButtonCellEditor.class, 
                sameScope.isEmpty() ? "Override_Shortcut2" : "Override_Shortcut", 
                conflictingActionList))); //NOI18N
        DialogDescriptor descriptor = new DialogDescriptor(
                innerPane,
                NbBundle.getMessage(ButtonCellEditor.class, "Conflicting_Shortcut_Dialog"),
                true,
                sameScope.isEmpty() ? 
                    DialogDescriptor.YES_NO_CANCEL_OPTION :
                    DialogDescriptor.YES_NO_OPTION,
                null,
                null); //NOI18N
        
        DialogDisplayer.getDefault().notify(descriptor);
        Object o = descriptor.getValue();
        if (!sameScope.isEmpty() && o == DialogDescriptor.NO_OPTION) {
            return DialogDescriptor.CANCEL_OPTION;
        } else {
            return o;
        }
    }

    private void setBorderEmpty() {
        ((JComponent) getComponent()).setBorder(new EmptyBorder(0, 0, 0, 0));
    }
}
