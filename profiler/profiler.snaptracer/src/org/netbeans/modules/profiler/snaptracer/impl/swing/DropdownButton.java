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

package org.netbeans.modules.profiler.snaptracer.impl.swing;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JToggleButton;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

/**
 *
 * @author Jiri Sedlacek
 */
public final class DropdownButton extends JToggleButton {

    public static final String KEY_CLASS = "KEY_CLASS"; // NOI18N
    public static final String KEY_BOOLVALUE = "KEY_BOOLVALUE"; // NOI18N

    private final List<Action> actions = new ArrayList<>();

    
    public DropdownButton(Icon icon) {
        super(icon);
    }


    public void addAction(Action action) {
        actions.add(action);
    }

    public void addSeparator() {
        actions.add(null);
    }


    protected void fireActionPerformed(ActionEvent event) {
        JPopupMenu popup = new JPopupMenu();
        for (Action action : actions) addAction(popup, action);
        popup.addPopupMenuListener(new PopupMenuListener() {
            public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
                setSelected(true);
            }
            public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
                setSelected(false);
            }
            public void popupMenuCanceled(PopupMenuEvent e) {}
        });
        popup.show(this, getWidth() - popup.getPreferredSize().width, getHeight());
    }

    private void addAction(JPopupMenu popup, Action action) {
        if (action == null) {
            popup.addSeparator();
        } else {
            Class cls = (Class)action.getValue(KEY_CLASS);
            if (Boolean.class.equals(cls)) {
                Boolean boolvalue = (Boolean)action.getValue(KEY_BOOLVALUE);
                JCheckBoxMenuItem item = new JCheckBoxMenuItem(action);
                item.setSelected(boolvalue);
                popup.add(item);
            } else {
                popup.add(action);
            }
        }
    }

}
