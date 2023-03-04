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
package org.netbeans.lib.profiler.ui.swing;

import java.awt.event.ActionEvent;
import javax.swing.Action;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;

/**
 *
 * @author Jiri Sedlacek
 */
public class ActionPopupButton extends PopupButton {
    
    private Action action;
    private final Action[] actions;
    
//    private Dimension minSize;
//    private Dimension prefSize;
    
    public ActionPopupButton(Action... _actions) {
        this(0, _actions);
    }
    
    public ActionPopupButton(int initial, Action... _actions) {
        actions = _actions;
        selectAction(initial);
    }
    
    public final Action[] getActions() {
        return actions;
    }
    
    public final void selectAction(Action _action) {
        action = _action;
        setText(action == null ? "" : action.getValue(Action.NAME).toString()); // NOI18N
    }
    
    public final void selectAction(int index) {
        selectAction(actions[index]);
    }
    
    public final Action getSelectedAction() {
        return action;
    }
    
    public final int getSelectedIndex() {
        for (int i = 0; i < actions.length; i++)
            if (actions[i] == action) return i;
        return -1;
    }
    
    protected void populatePopup(JPopupMenu popup) {
        for (final Action _action : actions) {
            if (_action != null) {
                popup.add(new JRadioButtonMenuItem(_action.getValue(Action.NAME).toString(), _action == action) {
                    protected void fireActionPerformed(ActionEvent e) {
                        selectAction(_action);
                        _action.actionPerformed(e);
                    }
                });
            } else {
                popup.addSeparator();
            }
        }
    }
    
//    public Dimension getMinimumSize() {
//        if (minSize == null) {
//            Action orig = action;
//            for (Action _action : actions) if (_action != null) {
//                selectAction(_action);
//                Dimension min = super.getMinimumSize();
//                if (minSize == null) minSize = min;
//                minSize.width = Math.max(minSize.width, min.width);
//                minSize.height = Math.max(minSize.height, min.height);
//            }
//            selectAction(orig);
//        }
//        return minSize;
//    }
//    
//    public Dimension getPreferredSize() {
//        if (prefSize == null) {
//            Action orig = action;
//            for (Action _action : actions) if (_action != null) {
//                selectAction(_action);
//                Dimension pref = super.getPreferredSize();
//                if (prefSize == null) prefSize = pref;
//                prefSize.width = Math.max(prefSize.width, pref.width);
//                prefSize.height = Math.max(prefSize.height, pref.height);
//            }
//            selectAction(orig);
//        }
//        return prefSize;
//    }
    
}
