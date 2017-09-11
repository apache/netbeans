/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.versioning.util.common;

import java.awt.Toolkit;
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
        
        boolean hasClipboardText = Toolkit.getDefaultToolkit().getSystemClipboard()
            .isDataFlavorAvailable(DataFlavor.stringFlavor);
        
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
