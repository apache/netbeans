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

package org.netbeans.modules.options.keymap;

import java.awt.Component;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;
import org.netbeans.core.options.keymap.api.ShortcutAction;
import org.openide.ErrorManager;

/**
 *
 * @author Jan Jancura
 */
public class KeymapListRenderer extends DefaultTreeCellRenderer {

    private KeymapViewModel keymapViewModel;
    private static ErrorManager log = ErrorManager.getDefault ().getInstance
        (KeymapListRenderer.class.getName ());

    
    public KeymapListRenderer (KeymapViewModel keymapViewModel) {
        if (keymapViewModel == null) throw new NullPointerException ();
        this.keymapViewModel = keymapViewModel;
        //Image i = Utilities.loadImage ("org/openide/resources/actions/empty.gif");
        setLeafIcon (null);
    }

    @Override
    public Component getTreeCellRendererComponent (
        JTree tree, 
        Object value,
        boolean sel,
        boolean expanded,
        boolean leaf, 
        int row,
        boolean hasFocus
    ) {
        super.getTreeCellRendererComponent (tree, value, sel, expanded, leaf, row, hasFocus);
	
	// There needs to be a way to specify disabled icons.
        if (leaf) {
            String displayName = ((ShortcutAction) value).getDisplayName ();
            StringBuffer text = new StringBuffer (displayName);
            if (log.isLoggable (1)) {
                text.append (" <");
                text.append (((ShortcutAction) value).getId ());
                text.append ("> ");
            }
            String[] shortcuts = keymapViewModel.getMutableModel().getShortcuts ((ShortcutAction) value);
            if (shortcuts.length == 1)
                text.append ("  [").append (shortcuts [0]).append ("]");
            else 
            if (shortcuts.length > 1) {
                int i, k = shortcuts.length;
                text.append ("  [").append (shortcuts [0]);
                for (i = 1; i < k; i++)
                    text.append (",").append (shortcuts [i]);
                text.append ("]");
            }
//            if (value instanceof Action) {
//                text += " " + ((Action) value).getValue (Action.NAME);
//            }
            setText (text.toString ());
//            Icon icon = getLeafIcon (); //Utils.getIcon (value);
//            if (icon != null) {
//                if (tree.isEnabled ())
//                    setIcon (icon);
//                else
//                    setDisabledIcon (icon);
//            }
        }
	return this;
    }           
}
