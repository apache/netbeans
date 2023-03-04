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

package org.netbeans.modules.debugger.ui.models;

import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JToggleButton;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import org.netbeans.api.debugger.ActiveBreakpoints;
import org.netbeans.api.debugger.Breakpoint;
import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.DebuggerManagerAdapter;
import org.netbeans.api.debugger.Properties;
import org.netbeans.api.debugger.Session;
import org.netbeans.api.project.Project;
import org.netbeans.modules.debugger.ui.actions.AddBreakpointAction;
import org.netbeans.modules.debugger.ui.models.BreakpointGroup.Group;
import org.netbeans.modules.debugger.ui.views.VariablesViewButtons;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Martin Entlicher
 */
public class BreakpointsViewButtons {

    public static final String PREFERENCES_NAME = "variables_view"; // NOI18N
    public static final String SHOW_VALUE_AS_STRING = "show_value_as_string"; // NOI18N
    private static final String DEACTIVATED_LINE_BREAKPOINT =
        "org/netbeans/modules/debugger/resources/editor/Breakpoint_stroke.png";                 // NOI18N

    public static JButton createNewBreakpointActionButton() {
        JButton button = VariablesViewButtons.createButton(
                "org/netbeans/modules/debugger/resources/breakpointsView/NewBreakpoint.png",
                NbBundle.getMessage (BreakpointsViewButtons.class, "Hint_New_Breakpoint")
            );
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                new AddBreakpointAction().actionPerformed(e);
            }
        });
        return button;
    }
    
    @NbBundle.Messages({"CTL_DeactivateAllBreakpoints=Deactivate all breakpoints in current session",
                        "CTL_ActivateAllBreakpoints=Activate all breakpoints in current session",
                        "CTL_NoDeactivation=The current session does not allow to deactivate breakpoints",
                        "CTL_NoSession=No debugger session"})
    public static AbstractButton createActivateBreakpointsActionButton() {
        ImageIcon icon = ImageUtilities.loadImageIcon(DEACTIVATED_LINE_BREAKPOINT, false);
        final JToggleButton button = new JToggleButton(icon);
        // ensure small size, just for the icon
        Dimension size = new Dimension(icon.getIconWidth() + 8, icon.getIconHeight() + 8);
        button.setPreferredSize(size);
        button.setMargin(new Insets(1, 1, 1, 1));
        button.setBorder(new EmptyBorder(button.getBorder().getBorderInsets(button)));
        button.setToolTipText(Bundle.CTL_DeactivateAllBreakpoints());
        button.setFocusable(false);
        final BreakpointsActivator ba = new BreakpointsActivator(button);
        button.addActionListener(ba);
        DebuggerManager.getDebuggerManager().addDebuggerListener(DebuggerManager.PROP_CURRENT_ENGINE, new DebuggerManagerAdapter() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                DebuggerEngine de = (DebuggerEngine) evt.getNewValue();
                ba.setCurrentEngine(de);
            }
        });
        ba.setCurrentEngine(DebuggerManager.getDebuggerManager().getCurrentEngine());
        return button;
    }
    
    private static class BreakpointsActivator implements ActionListener {
        
        private final Reference<JToggleButton> buttonRef;
        //private DebuggerEngine currentEngine;
        private volatile ActiveBreakpoints ab;
        //private String name;
        
        public BreakpointsActivator(JToggleButton button) {
            this.buttonRef = new WeakReference<JToggleButton>(button);
        }
        
        public void setCurrentEngine(DebuggerEngine currentEngine) {
            //this.currentEngine = currentEngine;
            //Session session = currentEngine.lookupFirst(null, Session.class);
            //this.name = session.getName();
            final JToggleButton button = buttonRef.get();
            if (button == null) {
                return ;
            }
            ActiveBreakpoints ab;
            final boolean active;
            final boolean canDeactivate;
            if (currentEngine == null) {
                ab = null;
                active = canDeactivate = false;
            } else {
                ab = ActiveBreakpoints.get(currentEngine);
                active = ab.areBreakpointsActive();
                canDeactivate = ab.canDeactivateBreakpoints();
            }
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    button.setSelected(!active);
                    button.setEnabled(canDeactivate);
                    setTooltip(button, active, canDeactivate);
                }
            });
            this.ab = ab;
        }
        
        @Override
        public void actionPerformed(ActionEvent e) {
            ActiveBreakpoints ab = this.ab;
            if (ab == null || !ab.canDeactivateBreakpoints()) {
                return ;
            }
            JToggleButton button = (JToggleButton) e.getSource();
            final boolean active = !button.isSelected();
            ab.setBreakpointsActive(active);
            setTooltip(button, active, true);
        }
        
        private static void setTooltip(JToggleButton button, boolean active, boolean canDeactivate) {
            if (!canDeactivate) {
                if (active) {
                    button.setToolTipText(Bundle.CTL_NoDeactivation());
                } else {
                    button.setToolTipText(Bundle.CTL_NoSession());
                }
            } else {
                if (active) {
                    button.setToolTipText(Bundle.CTL_DeactivateAllBreakpoints());
                } else {
                    button.setToolTipText(Bundle.CTL_ActivateAllBreakpoints());
                }
            }
        }
    }

    public static synchronized JButton createGroupSelectionButton() {
        final JButton button = VariablesViewButtons.createButton(
                "org/netbeans/modules/debugger/resources/breakpointsView/BreakpointGroups_options_16.png",
                NbBundle.getMessage (BreakpointsViewButtons.class, "Hint_Select_bp_groups")
            );
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                final Properties props = Properties.getDefault().getProperties("Breakpoints");
                String[] groupNames = (String[]) props.getArray("Grouping", new String[] { Group.CUSTOM.name() });
                String brkpGroup;
                if (groupNames.length == 0) {
                    brkpGroup = Group.NO.name();
                } else if (groupNames.length > 1) {
                    brkpGroup = Group.NESTED.name();
                } else {
                    brkpGroup = groupNames[0];
                }
                JPopupMenu menu = new JPopupMenu(NbBundle.getMessage (BreakpointsViewButtons.class, "Lbl_bp_groups"));
                for (Group group : Group.values()) {
                    menu.add(createJRadioButtonMenuItem(group, brkpGroup));
                }
                menu.addSeparator();
                menu.add(createCheckBoxMenuItem("LBL_BreakpointsFromOpenProjectsOnly", BreakpointGroup.PROP_FROM_OPEN_PROJECTS, props));
                if (currentSessionHaveProjects()) {
                    menu.add(createCheckBoxMenuItem("LBL_BreakpointsFromCurrentDebugSessionOnly", BreakpointGroup.PROP_FROM_CURRENT_SESSION_PROJECTS, props));
                }
                menu.show(button, 16, 0);

            }
        });
        button.setVisible(false);
        RequestProcessor.getDefault().post(new Runnable() {
            @Override
            public void run() {
                boolean groupableBreakpoints = false;
                Breakpoint[] brkps = DebuggerManager.getDebuggerManager().getBreakpoints();
                for (Breakpoint b : brkps) {
                    if (b.getGroupProperties() != null) {
                        groupableBreakpoints = true;
                        break;
                    }
                }
                if (groupableBreakpoints) {
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            button.setVisible(true);
                        }
                    });
                } else {
                    final boolean[] gb = new boolean[] { groupableBreakpoints };
                    DebuggerManager.getDebuggerManager().addDebuggerListener(new DebuggerManagerAdapter() {
                        @Override
                        public void breakpointAdded(Breakpoint breakpoint) {
                            if (!gb[0] && breakpoint.getGroupProperties() != null) {
                                gb[0] = true;
                                SwingUtilities.invokeLater(new Runnable() {
                                    @Override
                                    public void run() {
                                        button.setVisible(true);
                                    }
                                });
                            }
                        }
                    });
                }
            }
        });
        return button;
    }

    private static JRadioButtonMenuItem createJRadioButtonMenuItem(Group group, String brkpGroup) {
        JRadioButtonMenuItem gb = new JRadioButtonMenuItem(new GroupChangeAction(group));
        gb.setSelected(brkpGroup.equals(group.name()));
        return gb;
    }

    private static JCheckBoxMenuItem createCheckBoxMenuItem(String text, final String propName, final Properties props) {
        boolean selected = props.getBoolean(propName, true);
        text = NbBundle.getMessage(BreakpointsViewButtons.class, text);
        final JCheckBoxMenuItem chb = new JCheckBoxMenuItem(text, selected);
        chb.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                boolean selected = chb.isSelected();
                props.setBoolean(propName, selected);
            }
        });
        return chb;
    }

    private static boolean currentSessionHaveProjects() {
        // TODO: Perhaps the session could provide it's breakpoints directly somehow.
        Session currentSession = DebuggerManager.getDebuggerManager().getCurrentSession();
        if (currentSession == null) {
            return false;
        }
        List<? extends Project> sessionProjects = currentSession.lookup(null, Project.class);
        return sessionProjects.size() > 0;
    }

    // **************************************************************************

    private static class GroupChangeAction extends AbstractAction {

        private Group group;

        public GroupChangeAction(Group group) {
            this.group = group;
            String name = "LBL_"+group.name()+"Group"; // NOI18N
            name = NbBundle.getMessage (BreakpointsViewButtons.class, name);
            putValue(Action.NAME, name);
        }


        public void actionPerformed(ActionEvent e) {
            if (group == Group.NESTED) {
                BreakpointNestedGroupsDialog bngd = new BreakpointNestedGroupsDialog();
                bngd.setDisplayedGroups((String[]) Properties.getDefault().
                        getProperties("Breakpoints").getArray("Grouping", new String[] { Group.CUSTOM.name() }));
                String title = NbBundle.getMessage(BreakpointNestedGroupsDialog.class, "BreakpointNestedGroupsDialog_title");
                Object res = DialogDisplayer.getDefault().notify(new DialogDescriptor(bngd, title, true, null));
                if (NotifyDescriptor.OK_OPTION.equals(res)) {
                    Properties.getDefault().getProperties("Breakpoints").
                            setArray("Grouping", bngd.getDisplayedGroups());
                }
            } else {
                Properties.getDefault().getProperties("Breakpoints").setArray("Grouping", new String[] { group.name() });
            }
        }

    }

}
