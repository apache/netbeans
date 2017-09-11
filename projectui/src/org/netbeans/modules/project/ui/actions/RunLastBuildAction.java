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
 *
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

package org.netbeans.modules.project.ui.actions;

import java.awt.Component;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.spi.project.ui.support.BuildExecutionSupport;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.awt.Actions;
import org.openide.awt.DynamicMenuContent;
import org.openide.awt.Mnemonics;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.WeakListeners;
import org.openide.util.actions.Presenter;

/**
 * An action to run the last build execution.
 * @see "#47925"
 */
@ActionID(id = "org.netbeans.modules.project.ui.Rerun", category = "Project")
@ActionRegistration(displayName = "#LBL_RunLastBuildAction_general", lazy=false)
@ActionReferences({
    @ActionReference(path="Menu/BuildProject", position=1000),
    @ActionReference(path="Shortcuts", name="D-F11")
})
public final class RunLastBuildAction extends AbstractAction implements ChangeListener, Presenter.Menu, Presenter.Toolbar {
    
    public RunLastBuildAction() {
        super(NbBundle.getMessage(RunLastBuildAction.class, "LBL_RunLastBuildAction_general"));
        BuildExecutionSupportImpl.getInstance().addChangeListener(WeakListeners.change(this, BuildExecutionSupportImpl.getInstance()));
    }
    
    @Override
    public boolean isEnabled() {
        return BuildExecutionSupportImpl.getInstance().getLastItem() != null;
    }
    
    @Override
    public Object getValue(String key) {
        if (key.equals(Action.SHORT_DESCRIPTION)) {
            BuildExecutionSupport.Item item = BuildExecutionSupportImpl.getInstance().getLastItem();
            if (item != null) {
                String display = item.getDisplayName();
                return NbBundle.getMessage(RunLastBuildAction.class, "TIP_RunLastBuildAction_specific", display);
            } else {
                return null;
            }
        } else {
            return super.getValue(key);
        }
    }
    
    public @Override void actionPerformed(ActionEvent e) {
        RequestProcessor.getDefault().post(new Runnable() {
            public @Override void run() {
                BuildExecutionSupport.Item item = BuildExecutionSupportImpl.getInstance().getLastItem();
                if (item != null) {
                    item.repeatExecution();
                }
            }
        });
    }

    public @Override void stateChanged(ChangeEvent e) {
        firePropertyChange("enabled", null, Boolean.valueOf(isEnabled())); // NOI18N
        firePropertyChange(Action.SHORT_DESCRIPTION, null, null);
    }

    public @Override JMenuItem getMenuPresenter() {
        class SpecialMenuItem extends JMenuItem implements DynamicMenuContent {
            public SpecialMenuItem() {
                super(RunLastBuildAction.this);
            }
            public @Override JComponent[] getMenuPresenters() {
                String label;
                BuildExecutionSupport.Item item = BuildExecutionSupportImpl.getInstance().getLastItem();
                if (item != null) {
                    String display = item.getDisplayName();
                    label = NbBundle.getMessage(RunLastBuildAction.class, "LBL_RunLastBuildAction_specific", display);
                } else {
                    label = (String) getValue(Action.NAME);
                }
                Mnemonics.setLocalizedText(this, label);
                return new JComponent[] {this};
            }
            public @Override JComponent[] synchMenuPresenters(JComponent[] items) {
                return getMenuPresenters();
            }
        }
        return new SpecialMenuItem();
    }

    public @Override Component getToolbarPresenter() {
        JButton button = new JButton();
        Actions.connect(button, this);
        return button;
    }

}
