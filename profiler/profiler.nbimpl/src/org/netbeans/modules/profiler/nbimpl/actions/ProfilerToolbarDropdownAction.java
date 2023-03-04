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

package org.netbeans.modules.profiler.nbimpl.actions;

import org.openide.awt.Actions;
import org.openide.awt.DropDownButtonFactory;
import org.openide.util.actions.Presenter;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import org.netbeans.modules.profiler.actions.AttachAction;


/**
 *
 * @author Jiri Sedlacek
 */
public class ProfilerToolbarDropdownAction implements Action, Presenter.Toolbar {
    //~ Instance fields ----------------------------------------------------------------------------------------------------------

    private final Action defaultAction;
    private Component toolbarPresenter;

    //~ Constructors -------------------------------------------------------------------------------------------------------------

    public ProfilerToolbarDropdownAction() {
        defaultAction = Actions.forID("Profile", "org.netbeans.modules.profiler.actions.ProfileMainProject"); // NOI18N
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    public void setEnabled(boolean b) {
        defaultAction.setEnabled(b);
    }

    public boolean isEnabled() {
        return defaultAction.isEnabled();
    }

    // --- Presenter.Toolbar implementation --------------------------------------
    public Component getToolbarPresenter() {
        if (toolbarPresenter == null) {
            // gets the real action registered in the menu from layer
            Action a = Actions.forID("Profile", "org.netbeans.modules.profiler.actions.AttachMainProject"); // NOI18N
            final Action attachProjectAction = a != null ? a : /* XXX should be impossible */AttachAction.getInstance();
            
            // gets the real action registered in the menu from layer
            a = Actions.forID("Profile", "org.netbeans.modules.profiler.actions.AttachAction"); // NOI18N
            final Action attachProcessAction = a != null ? a : /* XXX should be impossible */AttachAction.getInstance();

            JPopupMenu dropdownPopup = new JPopupMenu();
            dropdownPopup.add(createDropdownItem(defaultAction));
            dropdownPopup.add(createDropdownItem(attachProjectAction));
            dropdownPopup.addSeparator();
            dropdownPopup.add(createDropdownItem(attachProcessAction));

            JButton button = DropDownButtonFactory.createDropDownButton(new ImageIcon(
                    new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB)), dropdownPopup);
            Actions.connect(button, defaultAction);

            toolbarPresenter = button;
        }

        return toolbarPresenter;
    }

    // --- Action implementation -------------------------------------------------
    public Object getValue(String key) {
        return defaultAction.getValue(key);
    }

    public void actionPerformed(ActionEvent e) {
        defaultAction.actionPerformed(e);
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        defaultAction.addPropertyChangeListener(listener);
    }

    public void putValue(String key, Object value) {
        defaultAction.putValue(key, value);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        defaultAction.removePropertyChangeListener(listener);
    }

    // --- Private implementation ------------------------------------------------
    private static JMenuItem createDropdownItem(final Action action) {
        String name = (String)action.getValue("menuText"); // NOI18N
        if (name == null || name.trim().isEmpty()) name = (String)action.getValue(NAME);
        final JMenuItem item = new JMenuItem(Actions.cutAmpersand(name)) {
            public void fireActionPerformed(ActionEvent e) {
                action.actionPerformed(e);
            }
        };
        item.setEnabled(action.isEnabled());
        
        // #231371
        action.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                String propName = evt.getPropertyName();
                if ("enabled".equals(propName)) { // NOI18N
                    item.setEnabled((Boolean)evt.getNewValue());
                } else if ("menuText".equals(propName)) { // NOI18N
                    item.setText(Actions.cutAmpersand((String) evt.getNewValue()));
                }
            }
        });

        return item;
    }
}
