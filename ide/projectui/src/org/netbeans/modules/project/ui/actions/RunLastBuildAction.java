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
