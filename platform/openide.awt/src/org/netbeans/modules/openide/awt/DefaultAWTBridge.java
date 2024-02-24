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

package org.netbeans.modules.openide.awt;

import java.awt.Component;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.JToggleButton;
import org.openide.awt.Actions;
import org.openide.awt.DynamicMenuContent;
import org.openide.util.actions.BooleanStateAction;
import org.openide.util.actions.SystemAction;
import org.openide.util.lookup.ServiceProvider;
import org.openide.util.actions.ActionPresenterProvider;
import org.openide.util.actions.Presenter;

/** Default implementation of presenters for various action types.
 */
@ServiceProvider(service=ActionPresenterProvider.class)
public final class DefaultAWTBridge extends ActionPresenterProvider {
    public JMenuItem createMenuPresenter (Action action) {
        if (action instanceof BooleanStateAction) {
            BooleanStateAction b = (BooleanStateAction)action;
            return new Actions.CheckboxMenuItem (b, true);
        }
        if (action.getValue(Actions.ACTION_VALUE_TOGGLE) != null) {
            return new Actions.CheckboxMenuItem(action, true);
        }
        if (action instanceof SystemAction) {
            SystemAction s = (SystemAction)action;
            return new Actions.MenuItem (s, true);
        }
            
        return new Actions.MenuItem (action, true);
    }
    
    public @Override JMenuItem createPopupPresenter(Action action) {
        JMenuItem item;
        if (action instanceof BooleanStateAction) {
            BooleanStateAction b = (BooleanStateAction)action;
            item = new Actions.CheckboxMenuItem (b, false);
        } else if (action instanceof SystemAction) {
            SystemAction s = (SystemAction)action;
            item = new Actions.MenuItem (s, false);
        } else {
            item = new Actions.MenuItem (action, false);
        }
        return item;
    }
    
    @Override
    public Component createToolbarPresenter(Action action) {
        AbstractButton btn;
        if ((action instanceof BooleanStateAction) || (action.getValue(Actions.ACTION_VALUE_TOGGLE) != null)) {
            btn = new JToggleButton();
            Actions.connect(btn, action);
        } else {
            btn = new JButton();
            Actions.connect(btn, action);
        }
        return btn;
    }
    
    public JPopupMenu createEmptyPopup() {
        return new JPopupMenu();
    }  
    
    public @Override Component[] convertComponents(Component comp) {
        if (comp instanceof JMenuItem) {
            JMenuItem item = (JMenuItem) comp;
            if (Boolean.TRUE.equals(item.getClientProperty(DynamicMenuContent.HIDE_WHEN_DISABLED)) && !item.isEnabled()) {
                return new Component[0];
            }
        }
         if (comp instanceof DynamicMenuContent) {
            Component[] toRet = ((DynamicMenuContent)comp).getMenuPresenters();
            boolean atLeastOne = false;
            Collection<Component> col = new ArrayList<Component>();
            for (int i = 0; i < toRet.length; i++) {
                if (toRet[i] instanceof DynamicMenuContent && toRet[i] != comp) {
                    col.addAll(Arrays.asList(convertComponents(toRet[i])));
                    atLeastOne = true;
                } else {
                    if (toRet[i] == null) {
                        toRet[i] = new JSeparator();
                    }
                    col.add(toRet[i]);
                }
            }
            if (atLeastOne) {
                return col.toArray(new Component[0]);
            } else {
                return toRet;
            }
         }
         return new Component[] {comp};
    }
    
}
