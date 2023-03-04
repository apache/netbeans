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

package org.netbeans.modules.hudson.ui.actions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;
import org.netbeans.modules.hudson.api.HudsonInstance;
import org.netbeans.modules.hudson.api.HudsonView;
import static org.netbeans.modules.hudson.ui.actions.Bundle.*;
import org.netbeans.modules.hudson.ui.nodes.HudsonInstanceNode;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.awt.DynamicMenuContent;
import org.openide.util.ContextAwareAction;
import org.openide.util.Lookup;
import org.openide.util.NbBundle.Messages;
import org.openide.util.actions.Presenter;

@ActionID(category="Team", id="org.netbeans.modules.hudson.ui.actions.ViewSwitcher")
@ActionRegistration(displayName="#ViewSwitcher.label", lazy=false)
@ActionReference(path=HudsonInstance.ACTION_PATH, position=100)
@Messages({
    "# \"View\" is used as noun in a menu for selection of a defined view.",
    "ViewSwitcher.label=View"})
public class ViewSwitcher extends AbstractAction implements ContextAwareAction, Presenter.Popup {

    private final HudsonInstance instance;

    public ViewSwitcher() {
        this(null);
    }

    public @Override Action createContextAwareInstance(Lookup actionContext) {
        Collection<? extends HudsonInstance> instances = actionContext.lookupAll(HudsonInstance.class);
        return new ViewSwitcher(instances.size() == 1 ? instances.iterator().next() : null);
    }

    private ViewSwitcher(HudsonInstance instance) {
        super(ViewSwitcher_label());
        this.instance = instance;
    }

    public @Override void actionPerformed(ActionEvent e) {
        assert false;
    }

    public @Override JMenuItem getPopupPresenter() {
        return new Menu();
    }

    private class Menu extends JMenu implements DynamicMenuContent {

        Menu() {
            setText(ViewSwitcher_label());
        }

        public @Override JComponent[] getMenuPresenters() {
            if (instance == null || instance.getViews().size() < 2) {
                return new JComponent[0];
            }
            removeAll();
            String selectedView = instance.prefs().get(HudsonInstanceNode.SELECTED_VIEW, null);
            String primaryViewName = instance.getPrimaryView().getName();
            JRadioButtonMenuItem item = new JRadioButtonMenuItem(primaryViewName);
            item.setSelected(selectedView == null || selectedView.equals(primaryViewName));
            item.addActionListener(new ActionListener() {
                public @Override void actionPerformed(ActionEvent e) {
                    instance.prefs().remove(HudsonInstanceNode.SELECTED_VIEW);
                }
            });
            add(item);
            addSeparator();
            for (final HudsonView view : instance.getViews()) {
                final String name = view.getName();
                if (name.equals(primaryViewName)) {
                    continue;
                }
                item = new JRadioButtonMenuItem(name);
                item.setSelected(name.equals(selectedView));
                item.addActionListener(new ActionListener() {
                    public @Override void actionPerformed(ActionEvent e) {
                        instance.prefs().put(HudsonInstanceNode.SELECTED_VIEW, name);
                    }
                });
                add(item);
            }
            return new JComponent[] {this};
        }

        public @Override JComponent[] synchMenuPresenters(JComponent[] items) {
            return getMenuPresenters();
        }

    }

}
