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

package org.netbeans.modules.debugger.ui.actions;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.WeakHashMap;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.Properties;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.spi.debugger.ui.AttachType;
import org.netbeans.spi.debugger.ui.Controller;
import org.netbeans.spi.debugger.ui.PersistentController;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.ui.support.BuildExecutionSupport;
import org.netbeans.spi.project.ui.support.MainProjectSensitiveActions;
import org.openide.awt.Actions;
import org.openide.awt.DropDownButtonFactory;
import org.openide.awt.StatusDisplayer;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.WeakListeners;
import org.openide.util.actions.Presenter;

/**
 *
 * @author Martin Entlicher
 */
public class DebugMainProjectAction implements Action, Presenter.Toolbar, PopupMenuListener {

    private static Set<AttachHistorySupport> ahs = null;
    
    private final Action delegate;
    private final DebugHistorySupport debugHistorySupport;
    private final AttachHistorySupport attachHistorySupport;
    private boolean menuInitialized;
    
    /** Creates a new instance of DebugMainProjectAction */
    public DebugMainProjectAction() {
        delegate = MainProjectSensitiveActions.mainProjectCommandAction(
                ActionProvider.COMMAND_DEBUG,
                NbBundle.getMessage(DebugMainProjectAction.class, "LBL_DebugMainProjectAction_Name" ),ImageUtilities.loadImageIcon("org/netbeans/modules/debugger/resources/debugProject.png", false)); // NOI18N
        delegate.putValue("iconBase","org/netbeans/modules/debugger/resources/debugProject.png"); //NOI18N
        debugHistorySupport = new DebugHistorySupport();
        attachHistorySupport = new AttachHistorySupport();
    }
    
    @Override public Object getValue(String arg0) {
        return delegate.getValue(arg0);
    }

    @Override public void putValue(String arg0, Object arg1) {
        delegate.putValue(arg0, arg1);
    }

    @Override public void setEnabled(boolean arg0) {
        delegate.setEnabled(arg0);
    }

    @Override public boolean isEnabled() {
        return delegate.isEnabled();
    }

    @Override public void addPropertyChangeListener(PropertyChangeListener arg0) {
        delegate.addPropertyChangeListener(arg0);
    }

    @Override public void removePropertyChangeListener(PropertyChangeListener arg0) {
        delegate.removePropertyChangeListener(arg0);
    }

    @Override public void actionPerformed(ActionEvent arg0) {
        Project p = OpenProjects.getDefault().getMainProject();
        GestureSubmitter.logDebugProject(p);
        delegate.actionPerformed(arg0);
    }

    @Override public Component getToolbarPresenter() {
        JPopupMenu menu = new JPopupMenu();
        JButton button = DropDownButtonFactory.createDropDownButton(
                new ImageIcon(new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB)), menu);
        final JMenuItem item = new JMenuItem(Actions.cutAmpersand((String) delegate.getValue("menuText")));
        item.setEnabled(delegate.isEnabled());

        delegate.addPropertyChangeListener(new PropertyChangeListener() {
            @Override public void propertyChange(PropertyChangeEvent evt) {
                String propName = evt.getPropertyName();
                if ("enabled".equals(propName)) {
                    item.setEnabled((Boolean)evt.getNewValue());
                } else if ("menuText".equals(propName)) {
                    item.setText(Actions.cutAmpersand((String) evt.getNewValue()));
                } else if ("selectedProjects".equals(propName)) {
                    Project[] projects = (Project[]) evt.getNewValue();
                    if (projects.length == 1) {
                        debugHistorySupport.setSelectedProject(projects[0].getProjectDirectory());
                    } else {
                        debugHistorySupport.setSelectedProject(null);
                    }
                }
            }
        });

        menu.add(item);
        item.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                DebugMainProjectAction.this.actionPerformed(e);
            }
        });
        try {
            Action ca = Actions.forID("Debug", "org.netbeans.modules.debugger.ui.actions.ConnectAction");
            JMenuItem item2 = new JMenuItem(Actions.cutAmpersand((String) ca.getValue(NAME)));
            Actions.connect(item2, ca);
            menu.add(item2);
        } catch (Exception nsee) {
            Exceptions.printStackTrace(nsee);
        }

        menu.addPopupMenuListener(this);

        Actions.connect(button, this);
        return button;
    }

    static synchronized void attachHistoryChanged() {
        if (ahs == null) { return; }
        for (AttachHistorySupport support : ahs) {
            support.computeItems();
        }
    }

    private static synchronized void addAttachHistorySupport(AttachHistorySupport support) {
        if (ahs == null) {
            ahs = Collections.newSetFromMap(new WeakHashMap<>());
        }
        ahs.add(support);
    }

    // PopupMenuListener ........................................................

    @Override public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
        if (!menuInitialized) {
            JPopupMenu menu = (JPopupMenu)e.getSource();
            debugHistorySupport.init(menu);
            attachHistorySupport.init(menu);
            menuInitialized = true;
        } else {
            debugHistorySupport.refreshItems();
        }
    }

    @Override public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
    }

    @Override public void popupMenuCanceled(PopupMenuEvent e) {
    }
    
    private static class DebugHistorySupport implements ActionListener, ChangeListener {

        private JPopupMenu menu;
        private JMenuItem[] items = new JMenuItem[0];
        private final JSeparator separator1 = new JPopupMenu.Separator();
        private final JSeparator separator2 = new JPopupMenu.Separator();
        private final BuildExecutionSupportChangeSupport besc;
        private final OpenProjectsListener opl;
        private final LinkedList<DebugActionItem> debugItems = new LinkedList<>();
        private volatile FileObject selectedProjectRoot;
        
        private static final int MAX_ITEMS_COUNT = 7;
        private static final String DEBUG_ACTION_ITEM_PROP_NAME = "debug action item";
        private static final RequestProcessor RP = new RequestProcessor(DebugHistorySupport.class.getName());
        
        public DebugHistorySupport() {
            besc = new BuildExecutionSupportChangeSupport();
            besc.addChangeListener(WeakListeners.change(this, besc));
            opl = new OpenProjectsListener();
            OpenProjects.getDefault().addPropertyChangeListener(
                    WeakListeners.propertyChange(opl, OpenProjects.getDefault()));
        }
        
        void init(JPopupMenu menu) {
            this.menu = menu;
            computeItems();
        }
        
        private void computeItems() {
            if (menu == null) {
                return ;
            }
            boolean wasSeparator = items.length > 0;
            for (int i = 0; i < items.length; i++) {
                menu.remove(items[i]);
            }
            synchronized (debugItems) {
                if (debugItems.isEmpty()) {
                    items = new JMenuItem[0];
                } else {
                    int n = debugItems.size();
                    items = new JMenuItem[n];
                    int i, j;
                    for (i = j = 0; i < n; i++) {
                        DebugActionItem dai = debugItems.get(i);
                        String dispName = dai.getDisplayName();
                        if (Objects.equals(selectedProjectRoot, dai.getRoot())) {
                            continue;
                        }
                        items[j] = new JMenuItem(dispName);
                        items[j].putClientProperty(DEBUG_ACTION_ITEM_PROP_NAME, dai.getActionItem());
                        items[j].addActionListener(this);
                        j++;
                    }
                    if (j < items.length) {
                        items = Arrays.copyOf(items, j);
                    }
                }
            }
            if (items.length == 0) {
                if (wasSeparator) {
                    menu.remove(separator1);
                    menu.remove(separator2);
                }
            } else {
                if (!wasSeparator) {
                    menu.insert(separator1, 1);
                }
                int i;
                for (i = 0; i < items.length; i++) {
                    menu.insert(items[i], i + 2);
                }
                menu.insert(separator2, i + 2);
            }
        }
        
        private void refreshItems() {
            computeItems();
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            JMenuItem item = (JMenuItem)e.getSource();
            final BuildExecutionSupport.ActionItem ai =
                    (BuildExecutionSupport.ActionItem) item.getClientProperty(DEBUG_ACTION_ITEM_PROP_NAME);
            RP.post(new Runnable() {
                @Override
                public void run() {
                    ai.repeatExecution();
                }
            });
        }

        @Override
        public void stateChanged(ChangeEvent e) {
            BuildExecutionSupport.Item lastItem = BuildExecutionSupport.getLastFinishedItem();
            if (lastItem instanceof BuildExecutionSupport.ActionItem) {
                BuildExecutionSupport.ActionItem ai = (BuildExecutionSupport.ActionItem) lastItem;
                String action = ai.getAction();
                if (ActionProvider.COMMAND_DEBUG.equals(action)) { // Track debug items only
                    boolean changed = false;
                    synchronized (debugItems) {
                        if (debugItems.isEmpty() || ai != debugItems.getFirst().getActionItem()) {
                            DebugActionItem dai = new DebugActionItem(ai);
                            debugItems.remove(dai); // Remove it if it's there
                            debugItems.addFirst(dai);
                            if (debugItems.size() > MAX_ITEMS_COUNT) {
                                debugItems.removeLast();
                            }
                            changed = true;
                        }
                    }
                    if (changed) {
                        // computeItems(); - not necessary, UI items are refreshed when to be displayed
                    }
                }
            }
        }

        private void setSelectedProject(FileObject projectDirectory) {
            selectedProjectRoot = projectDirectory;
        }
        
        private static final class DebugActionItem {
            
            private final BuildExecutionSupport.ActionItem ai;
            private final FileObject prjRoot;
            
            DebugActionItem(BuildExecutionSupport.ActionItem ai) {
                this.ai = ai;
                prjRoot = ai.getProjectDirectory();
            }

            FileObject getRoot() {
                return prjRoot;
            }
            
            String getDisplayName() {
                return ai.getDisplayName();
            }
            
            BuildExecutionSupport.ActionItem getActionItem() {
                return ai;
            }
            
            @Override
            public boolean equals(Object obj) {
                if (!(obj instanceof DebugActionItem)) {
                    return false;
                }
                DebugActionItem dai = (DebugActionItem) obj;
                return Objects.equals(prjRoot, dai.prjRoot) &&
                       Objects.equals(getDisplayName(), dai.getDisplayName());
            }

            @Override
            public int hashCode() {
                return Objects.hash(prjRoot);
            }
            
        }
        
        private final class OpenProjectsListener implements PropertyChangeListener {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if (evt.getPropertyName().equals(OpenProjects.PROPERTY_OPEN_PROJECTS)) {
                    Project[] opened = (Project[]) evt.getNewValue();
                    Set<FileObject> openRoots = new HashSet<>();
                    for (Project p : opened) {
                        if (p != null) {
                            openRoots.add(p.getProjectDirectory());
                        }
                    }
                    synchronized (debugItems) {
                        int n = debugItems.size();
                        for (int i = 0; i < n; i++) {
                            FileObject root = debugItems.get(i).getRoot();
                            if (root != null && !openRoots.contains(root)) {
                                // The project was closed
                                debugItems.remove(i--);
                                n--;
                            }
                        }
                    }
                }
            }
            
        }
    }
    
    private static class BuildExecutionSupportChangeSupport {
        
        public void addChangeListener(ChangeListener listener) {
            BuildExecutionSupport.addChangeListener(listener);
        }
        
        public void removeChangeListener(ChangeListener listener) {
            BuildExecutionSupport.removeChangeListener(listener);
        }
    }

    // AttachHistorySupport .....................................................

    static class AttachHistorySupport implements ActionListener {

        private JPopupMenu menu;
        private JMenuItem[] items = new JMenuItem[0];
        private JSeparator separator = new JPopupMenu.Separator();
        private static final RequestProcessor RP = new RequestProcessor(AttachHistorySupport.class.getName());

        public void init(JPopupMenu menu) {
            this.menu = menu;
            addAttachHistorySupport(this);
            computeItems();
        }

        public void computeItems() {
            menu.remove(separator);
            for (int x = 0; x < items.length; x++) {
                menu.remove(items[x]);
            } // for
            Properties props = Properties.getDefault().getProperties("debugger").getProperties("last_attaches");
            Integer[] usedSlots = (Integer[]) props.getArray("used_slots", new Integer[0]);
            if (usedSlots.length > 0) {
                menu.add(separator);
            }
            items = new JMenuItem[usedSlots.length];
            for (int x = 0; x < usedSlots.length; x++) {
                String dispName = props.getProperties("slot_" + usedSlots[x]).getString("display_name", "<???>"); // NOI18N
                items[x] = new JMenuItem(dispName);
                items[x].addActionListener(this);
                menu.add(items[x]);
            } // for
        }

        @Override public void actionPerformed(ActionEvent e) {
            JMenuItem item = (JMenuItem)e.getSource();
            int index = -1;
            for (int x = 0; x < items.length; x++) {
                if (items[x] == item) {
                    index = x;
                    break;
                }
            }
            if (index == -1) { return; } // should not occure
            final int findex = index;
            RP.post(new Runnable() {
                @Override
                public void run() {
                    perform(findex);
                }
            });
        }
        
        private void perform(int index) {
            Properties props = Properties.getDefault().getProperties("debugger").getProperties("last_attaches");
            Integer[] usedSlots = (Integer[]) props.getArray("used_slots", new Integer[0]);
            String attachTypeName = props.getProperties("slot_" + usedSlots[index]).getString("attach_type", "???");
            List types = DebuggerManager.getDebuggerManager().lookup (null, AttachType.class);
            AttachType att = null;
            for (Object t : types) {
                AttachType at = (AttachType)t;
                if (attachTypeName.equals(at.getTypeDisplayName())) {
                    att = at;
                    break;
                }
            } // for
            if (att != null) {
                final AttachType attachType = att;
                final PersistentController[] controllerPtr = new PersistentController[] { null };
                try {
                    SwingUtilities.invokeAndWait(new Runnable() {
                        @Override
                        public void run() {
                            JComponent customizer = attachType.getCustomizer ();
                            Controller controller = attachType.getController();
                            if (controller == null && (customizer instanceof Controller)) {
                                Exceptions.printStackTrace(new IllegalStateException("FIXME: JComponent "+customizer+" must not implement Controller interface!"));
                                controller = (Controller) customizer;
                            }
                            if (controller instanceof PersistentController) {
                                controllerPtr[0] = (PersistentController) controller;
                            }
                        }
                    });
                } catch (InterruptedException ex) {
                    Exceptions.printStackTrace(ex);
                    return ;
                } catch (InvocationTargetException ex) {
                    Exceptions.printStackTrace(ex);
                    return ;
                }
                final PersistentController controller = controllerPtr[0];
                if (controller == null) {
                    return ;
                }
                boolean result = controller.load(props.getProperties("slot_" + usedSlots[index]).getProperties("values"));
                if (!result) {
                    return; // [TODO] not loaded, cannot be used to attach
                }
                final boolean[] passedPtr = new boolean[] { false };
                try {
                    SwingUtilities.invokeAndWait(new Runnable() {
                        @Override
                        public void run() {
                            passedPtr[0] = controller.ok();
                        }
                    });
                } catch (InterruptedException ex) {
                    Exceptions.printStackTrace(ex);
                    return ;
                } catch (InvocationTargetException ex) {
                    Exceptions.printStackTrace(ex);
                    return ;
                }
                if (passedPtr[0]) {
                    makeFirst(index);
                    GestureSubmitter.logAttach(attachTypeName);
                }
                //return;
            } else {
                // report failure - attach type not found
                StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(DebugMainProjectAction.class, "CTL_Attach_Type_Not_Found"));
            }
        }

        private void makeFirst(int index) {
            if (index == 0) {
                return;  // nothing to do
            }
            Properties props = Properties.getDefault().getProperties("debugger").getProperties("last_attaches");
            Integer[] usedSlots = (Integer[]) props.getArray("used_slots", new Integer[0]);
            int temp = usedSlots[index];
            for (int x = index; x > 0; x--) {
                usedSlots[x] = usedSlots[x - 1];
            }
            usedSlots[0] = temp;
            props.setArray("used_slots", usedSlots);
            attachHistoryChanged();
        }

    } // AttachHistorySupport


}
