/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
