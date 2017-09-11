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

package org.apache.tools.ant.module.run;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.text.Collator;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import org.apache.tools.ant.module.AntModule;
import org.apache.tools.ant.module.api.AntProjectCookie;
import org.apache.tools.ant.module.api.support.TargetLister;
import org.apache.tools.ant.module.api.support.TargetLister.Target;
import org.apache.tools.ant.module.loader.AntProjectDataObject;
import org.openide.ErrorManager;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.awt.Actions;
import org.openide.util.ContextAwareAction;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.actions.Presenter;
import org.openide.util.actions.SystemAction;

/**
 * Submenu which permits the user to run various targets from the project.
 * Distinction made between the main target, other documented targets, and other
 * undocumented targets.
 */
@ActionID(id = "org.apache.tools.ant.module.nodes.RunTargetsAction", category = "Build")
@ActionRegistration(displayName = "#LBL_run_targets_action", lazy=false)
@ActionReferences(value = {
    @ActionReference(position = 900, path = "Editors/text/x-ant+xml/Popup"),
    @ActionReference(path="org-apache-tools-ant-module/target-actions", position=300),
    @ActionReference(position = 200, path = AntProjectDataObject.ACTIONS)})
public final class RunTargetsAction extends SystemAction implements ContextAwareAction {

    @Override
    public String getName () {
        return NbBundle.getMessage (RunTargetsAction.class, "LBL_run_targets_action");
    }

    @Override
    public HelpCtx getHelpCtx () {
        return HelpCtx.DEFAULT_HELP;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        //well, since someone can assign a shortcut ti the action, the invokation is unvaiodable, make it noop        
        //assert false : "Action should never be called without a context";
    }

    @Override
    public Action createContextAwareInstance(final Lookup actionContext) {
        if (actionContext.lookup(TargetLister.Target.class) != null) { //#220590
            final Target target = actionContext.lookup(TargetLister.Target.class);
            AbstractAction a = new AbstractAction(getName()) {
                              @Override
                              public void actionPerformed(ActionEvent e) {
                                  try {
                                      new TargetExecutor(target.getOriginatingScript(), new String[]{target.getName()}).execute();
                                  } catch (IOException ioe) {
                                      AntModule.err.notify(ioe);
                                  }
                              }
                          };
            
            a.putValue(ACCELERATOR_KEY, this.getValue(ACCELERATOR_KEY));            
            return a;
        } else {
            return new ContextAction(actionContext);
        }
    }
    
    /**
     * The particular instance of this action for a given project.
     */
    private static final class ContextAction extends AbstractAction implements Presenter.Popup {
        
        private final AntProjectCookie project;
        
        public ContextAction(Lookup lkp) {
            super(SystemAction.get(RunTargetsAction.class).getName());
            Collection<? extends AntProjectCookie> apcs = lkp.lookupAll(AntProjectCookie.class);
            AntProjectCookie _project = null;
            if (apcs.size() == 1) {
                _project = apcs.iterator().next();
                if (_project.getParseException() != null) {
                    _project = null;
                }
            }
            project = _project;
            super.setEnabled(project != null);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
        //well, since someone can assign a shortcut ti the action, the invokation is unvaiodable, make it noop        
        //assert false : "Action should never be called without a context";
        }

        @Override
        public JMenuItem getPopupPresenter() {
            if (project != null) {
                return createMenu(project);
            } else {
                return new Actions.MenuItem(this, false);
            }
        }

        @Override
        public void setEnabled(boolean b) {
            assert false : "No modifications to enablement status permitted";
        }
        
    }

    /**
     * Create the submenu.
     */
    private static JMenu createMenu(AntProjectCookie project) {
        return new LazyMenu(project);
    }
    
    private static final class LazyMenu extends JMenu {
        
        private final AntProjectCookie project;
        private boolean initialized = false;
        
        public LazyMenu(AntProjectCookie project) {
            super(SystemAction.get(RunTargetsAction.class).getName());
            this.project = project;
        }
        
        @Override
        public JPopupMenu getPopupMenu() {
            if (!initialized || project == null) {
                initialized = true;
                Set<TargetLister.Target> allTargets;
                try {
                    allTargets = TargetLister.getTargets(project);
                } catch (IOException e) {
                    // XXX how to notify properly?
                    AntModule.err.notify(ErrorManager.INFORMATIONAL, e);
                    allTargets = Collections.emptySet();
                }
                String defaultTarget = null;
                SortedSet<String> describedTargets = new TreeSet<String>(Collator.getInstance());
                SortedSet<String> otherTargets = new TreeSet<String>(Collator.getInstance());
                for (TargetLister.Target t : allTargets) {
                    if (t.isOverridden()) {
                        // Cannot be called.
                        continue;
                    }
                    if (t.isInternal()) {
                        // Don't present in GUI.
                        continue;
                    }
                    String name = t.getName();
                    if (t.isDefault()) {
                        defaultTarget = name;
                    } else if (t.isDescribed()) {
                        describedTargets.add(name);
                    } else {
                        otherTargets.add(name);
                    }
                }
                boolean needsep = false;
                VerticalGridLayout vgl = new VerticalGridLayout();
                getPopupMenu().setLayout(vgl);
                if (defaultTarget != null) {
                    needsep = true;
                    JMenuItem menuitem = new JMenuItem(defaultTarget);
                    menuitem.addActionListener(new TargetMenuItemHandler(project, defaultTarget));
                    add(menuitem);
                }
                if (needsep) {
                    needsep = false;
                    addSeparator();
                }
                if (!describedTargets.isEmpty()) {
                    needsep = true;
                    for (String target : describedTargets) {
                        JMenuItem menuitem = new JMenuItem(target);
                        menuitem.addActionListener(new TargetMenuItemHandler(project, target));
                        add(menuitem);
                    }
                }
                if (needsep) {
                    needsep = false;
                    addSeparator();
                }
                if (!otherTargets.isEmpty()) {
                    needsep = true;
                    JMenu submenu = new JMenu(NbBundle.getMessage(RunTargetsAction.class, "LBL_run_other_targets"));
                    VerticalGridLayout submenuVgl = new VerticalGridLayout();
                    submenu.getPopupMenu().setLayout(submenuVgl);
                    for (String target : otherTargets) {
                        JMenuItem menuitem = new JMenuItem(target);
                        menuitem.addActionListener(new TargetMenuItemHandler(project, target));
                        submenu.add(menuitem);
                    }
                    add(submenu);
                }
                if (needsep) {
                    needsep = false;
                    addSeparator();
                }
                add(new AdvancedAction(project, allTargets));
            }
            return super.getPopupMenu();
        }
        
    }

    /**
     * Action handler for a menu item representing one target.
     */
    private static final class TargetMenuItemHandler implements ActionListener, Runnable {
        
        private final AntProjectCookie project;
        private final String target;
        
        public TargetMenuItemHandler(AntProjectCookie project, String target) {
            this.project = project;
            this.target = target;
        }
        
        @Override
        public void actionPerformed(ActionEvent ev) {
            // #16720 part 2: don't do this in the event thread...
            RequestProcessor.getDefault().post(this);
        }
        
        @Override
        public void run() {
            try {
                TargetExecutor te = new TargetExecutor(project, new String[] {target});
                te.execute();
            } catch (IOException ioe) {
                AntModule.err.notify(ioe);
            }
        }
        
    }
    
    /**
     * Menu item to let the user select a random target(s), and set properties and verbosity.
     */
    private static final class AdvancedAction extends AbstractAction {
        
        private final AntProjectCookie project;
        private final Set<TargetLister.Target> allTargets;
        
        public AdvancedAction(AntProjectCookie project, Set<TargetLister.Target> allTargets) {
            super(NbBundle.getMessage(RunTargetsAction.class, "LBL_run_advanced"));
            this.project = project;
            this.allTargets = allTargets;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            new AdvancedActionPanel(project, allTargets).display();
        }
        
    }
    
}
