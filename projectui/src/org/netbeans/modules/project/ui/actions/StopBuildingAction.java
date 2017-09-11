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
