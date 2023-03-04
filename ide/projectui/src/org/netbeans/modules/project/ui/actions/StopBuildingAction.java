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

import java.util.List;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.spi.project.ui.support.BuildExecutionSupport;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.awt.DynamicMenuContent;
import org.openide.awt.Mnemonics;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.WeakListeners;
import org.openide.util.actions.CallableSystemAction;
import org.openide.util.actions.SystemAction;

/**
 * Action which stops the currently running Ant process.
 * If there is more than one, a dialog appears asking you to select one.
 * @author Jesse Glick
 * @see "issue #43143"
 */
@ActionID(id = "org.netbeans.modules.project.ui.actions.StopBuildingAction", category = "Project")
@ActionRegistration(displayName = "#LBL_stop_building", lazy=false)
@ActionReference(path = "Menu/BuildProject", position = 1100)
public final class StopBuildingAction extends CallableSystemAction implements ChangeListener {
    
   public StopBuildingAction()  {
       super();
       BuildExecutionSupportImpl.getInstance().addChangeListener(WeakListeners.change(this, BuildExecutionSupportImpl.getInstance()));
   }

    @Override
    public void performAction() {
        List<BuildExecutionSupport.Item> toStop = BuildExecutionSupportImpl.getInstance().getRunningItems();

        if (toStop.size() > 1) {
            // More than one, need to select one.
            toStop = StopBuildingAlert.selectProcessToKill(toStop);
        }
        for (BuildExecutionSupport.Item t : toStop) {
            if (t != null) {
                t.stopRunning();
            }
        }
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(StopBuildingAction.class, "LBL_stop_building");
    }

    @Override
    public HelpCtx getHelpCtx() {
        return null;
    }

    @Override
    protected void initialize() {
        super.initialize();
        setEnabled(false); // no processes initially
    }

    @Override
    protected boolean asynchronous() {
        return false;
    }
    
    @Override
    public JMenuItem getMenuPresenter() {
        class SpecialMenuItem extends JMenuItem implements DynamicMenuContent {
            public SpecialMenuItem() {
                super(StopBuildingAction.this);
            }
            public @Override JComponent[] getMenuPresenters() {
                String label = NbBundle.getMessage(StopBuildingAction.class, "LBL_stop_building");
                List<BuildExecutionSupport.Item> items = BuildExecutionSupportImpl.getInstance().getRunningItems();
                switch (items.size()) {
                    case 0:
                        label = NbBundle.getMessage(StopBuildingAction.class, "LBL_stop_building");
                        break;
                    case 1:
                        label = NbBundle.getMessage(StopBuildingAction.class, "LBL_stop_building_one",
                                items.iterator().next().getDisplayName());
                        break;
                    default:
                        label = NbBundle.getMessage(StopBuildingAction.class, "LBL_stop_building_many");
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

    public @Override void stateChanged(ChangeEvent e) {
        final List<BuildExecutionSupport.Item> items = BuildExecutionSupportImpl.getInstance().getRunningItems();

        SwingUtilities.invokeLater(new Runnable() {
            public @Override void run() {
                SystemAction.get(StopBuildingAction.class).setEnabled(items.size() > 0);
            }
        });
    }
    
}
