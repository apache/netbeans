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
package org.netbeans.modules.versioning.util.common;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.event.KeyEvent;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.KeyStroke;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.JTextComponent;
import org.openide.awt.Mnemonics;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 * Builder for the popup menu.
 * 
 * @author Mario Schroeder
 */
class CommitPopupBuilder {
    
    private JPopupMenu popupPresenter;
    
    private Action cutAction;
    
    private Action copyAction;
    
    private Action pasteAction;
    
    private Action selectAllAction;
        
    /**
     * Returns the popup menu.
     * @param component the text component which calls the method
     * @return a poup menu
     */
    JPopupMenu getPopup(JTextComponent component) {
        
        if(popupPresenter == null){
            build(component.getActionMap());
        }
        
        boolean textSelected = component.getSelectedText() != null;       
        
        cutAction.setEnabled(textSelected);
        copyAction.setEnabled(textSelected);

        Clipboard clipboard = Lookup.getDefault().lookup(Clipboard.class);
        if (clipboard == null) {
            clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        }

        boolean hasClipboardText = clipboard.isDataFlavorAvailable(DataFlavor.stringFlavor);
        
        pasteAction.setEnabled(hasClipboardText);
        
        return popupPresenter;
    }

    /**
     * This method builds the popup menu.
     */
    private void build(ActionMap actionMap) {
        
        popupPresenter = new JPopupMenu();
        
        cutAction = actionMap.get(DefaultEditorKit.cutAction);
        copyAction = actionMap.get(DefaultEditorKit.copyAction);
        pasteAction = actionMap.get(DefaultEditorKit.pasteAction);
        selectAllAction = actionMap.get(DefaultEditorKit.selectAllAction);
        
        popupPresenter.add(createMenuItem("CTL_MenuItem_Cut", KeyEvent.VK_X, cutAction));
        popupPresenter.add(createMenuItem("CTL_MenuItem_Copy", KeyEvent.VK_C, copyAction));
        popupPresenter.add(createMenuItem("CTL_MenuItem_Paste", KeyEvent.VK_V, pasteAction));
        popupPresenter.addSeparator();
        popupPresenter.add(createMenuItem("CTL_MenuItem_SelectAll", KeyEvent.VK_A, selectAllAction));
    }
    
    /**
     * Creates a new menu item.
     * @param msgKey key for the message within resource bundle.
     * @param key key for the accelerator
     * @param action the underlying action for the menuitem
     * @return a new menu item
     */
    private JMenuItem createMenuItem(String msgKey, int key, Action action) {
        
        String msg = NbBundle.getMessage(getClass(), msgKey);
        JMenuItem item = new JMenuItem(action);        
        Mnemonics.setLocalizedText(item, msg);
        int keyMask = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
        item.setAccelerator(KeyStroke.getKeyStroke(key, keyMask, false));
        return item;
    }

}
