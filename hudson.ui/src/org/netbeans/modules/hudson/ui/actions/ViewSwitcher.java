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
