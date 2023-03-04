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
package org.netbeans.modules.versioning.util;

import org.openide.util.ImageUtilities;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;
import org.openide.util.NbBundle;
import org.openide.util.HelpCtx;
import org.openide.ErrorManager;
import org.openide.awt.TabbedPaneFactory;

import javax.swing.*;
import java.io.*;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.util.*;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;

/**
 * Top component of the Versioning Output view.
 * 
 * @author Maros Sandor
 */
public class VersioningOutputTopComponent extends TopComponent implements Externalizable, PropertyChangeListener {
   
    private static final long serialVersionUID = 1L;    
    
    private static VersioningOutputTopComponent instance;

    private Set<JComponent> components = new HashSet<JComponent>(1);
    
    private JTabbedPane tabbedPane = TabbedPaneFactory.createCloseButtonTabbedPane();
    
    private Action prevTabAction = new AbstractAction() {
        public void actionPerformed(ActionEvent e) {
            tabbedPane.setSelectedIndex(getPreviousSubTabIndex(tabbedPane, tabbedPane.getSelectedIndex()));
        }
    };

    private Action nextTabAction = new AbstractAction() {
        public void actionPerformed(ActionEvent e) {
            tabbedPane.setSelectedIndex(getNextSubTabIndex(tabbedPane, tabbedPane.getSelectedIndex()));
        }
    };
    
    public VersioningOutputTopComponent() {
        // XXX - please rewrite to regular API when available - see issue #55955
        putClientProperty("SlidingName", NbBundle.getMessage(VersioningOutputTopComponent.class, "CTL_VersioningOutput_Title")); //NOI18N 
        setIcon(ImageUtilities.loadImage("org/netbeans/modules/versioning/util/resources/window-versioning.png"));  // NOI18N
        setLayout(new BorderLayout());
        getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(VersioningOutputTopComponent.class, "CTL_VersioningOutput_Title"));  // NOI18N
        updateName();
        tabbedPane.addPropertyChangeListener(this);
        getActionMap().put("PreviousViewAction", prevTabAction);
        getActionMap().put("NextViewAction", nextTabAction);
    }

    public void propertyChange(PropertyChangeEvent evt) {
        if (TabbedPaneFactory.PROP_CLOSE.equals(evt.getPropertyName())) {
            JComponent c = (JComponent) evt.getNewValue();
            removeComponent(c);
        }
    }

    public HelpCtx getHelpCtx() {
        return new HelpCtx(getClass());
    }

    public static synchronized VersioningOutputTopComponent getInstance() {
        if (VersioningOutputTopComponent.instance == null) {
            VersioningOutputTopComponent.instance = (VersioningOutputTopComponent) WindowManager.getDefault().findTopComponent("versioning_output"); // NOI18N
            if (VersioningOutputTopComponent.instance == null) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, new IllegalStateException(
                    "Can not find Versioning Output component")); // NOI18N
                VersioningOutputTopComponent.instance = new VersioningOutputTopComponent();
            }
        }
    
        return VersioningOutputTopComponent.instance;
    }

    protected String preferredID() {
        return "versioning_output";    // NOI18N       
    }
    
    public int getPersistenceType() {
        return TopComponent.PERSISTENCE_ALWAYS;
    }
    
    public void writeExternal(ObjectOutput out) throws IOException {
        super.writeExternal(out);
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        super.readExternal(in);
        components = new HashSet<JComponent>(1);
        tabbedPane = TabbedPaneFactory.createCloseButtonTabbedPane();
        tabbedPane.addPropertyChangeListener(this);
        updateName();
    }
    
    public Object readResolve() {
        return getInstance();
    }    

    private void updateName() {
        if (components.size() == 0) {
            setName(NbBundle.getMessage(VersioningOutputTopComponent.class, "CTL_VersioningOutput_Title")); // NOI18N            
        } else if (components.size() == 1) {
            JComponent c = components.iterator().next();
            setName(NbBundle.getMessage(VersioningOutputTopComponent.class, "CTL_VersioningOutput_TitleOne", c.getName())); // NOI18N            
        } else {
            setName(NbBundle.getMessage(VersioningOutputTopComponent.class, "CTL_VersioningOutput_Title")); // NOI18N            
        }
    }

    private void removeComponent(JComponent c) {
        assert SwingUtilities.isEventDispatchThread();
        assert components.remove(c);
        if (components.size() == 0) {
            removeAll();
        } else if (components.size() == 1) {
            tabbedPane.removeAll();
            removeAll();
            add(components.iterator().next());
        } else {
            tabbedPane.remove(c);
        }
        updateName();
        revalidate();
    }

    void addComponent(String key, JComponent c) {
        assert SwingUtilities.isEventDispatchThread();
        for (JComponent existing : components) {
            if (existing.getClientProperty(VersioningOutputTopComponent.class).equals(key)) {
                removeComponent(existing);
                break;
            }
        }
        if (!components.add(c)) return;
        c.putClientProperty(VersioningOutputTopComponent.class, key);
        if (components.size() == 1) {
            assert getComponentCount() == 0;
            add(c);
        } else if (components.size() == 2) {
            assert getComponentCount() == 1;
            removeAll();
            Iterator<JComponent> it = components.iterator();
            tabbedPane.add(it.next());
            tabbedPane.add(it.next());
            tabbedPane.setSelectedComponent(c);
            add(tabbedPane);
        } else {
            assert getComponentCount() == 1;
            tabbedPane.add(c);
            tabbedPane.setSelectedComponent(c);
        }
        updateName();
        revalidate();
    }

    private int getPreviousSubTabIndex(JTabbedPane tabs, int tabIndex) {
        int previousTabIndex = tabIndex;
        for (int i = 0; i < tabs.getComponentCount(); i++) {
            previousTabIndex--;
            if (previousTabIndex < 0) {
                previousTabIndex = tabs.getComponentCount() - 1;
            }
            if (tabs.isEnabledAt(previousTabIndex)) {
                break;
            }
        }
        return previousTabIndex;
    }

    private int getNextSubTabIndex(JTabbedPane tabs, int tabIndex) {
       int nextTabIndex = tabIndex;
       for (int i = 0; i < tabs.getComponentCount(); i++) {
            nextTabIndex++;
            if (nextTabIndex == tabs.getComponentCount()) {
                nextTabIndex = 0;
            }
            if (tabs.isEnabledAt(nextTabIndex)) {
                break;
            }
        }
        return nextTabIndex;
    }
}
