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
package org.netbeans.modules.debugger.jpda.visual.ui;

import java.awt.BorderLayout;
import java.lang.ref.WeakReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComponent;
import javax.swing.JPanel;
import org.netbeans.modules.debugger.jpda.visual.spi.ComponentInfo;
import org.netbeans.spi.navigator.NavigatorPanel;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.ExplorerUtils;
import org.openide.explorer.view.BeanTreeView;
import org.openide.explorer.view.TreeView;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 * The Navigator content of component hierarchy.
 * 
 * @author Martin Entlicher
 */
public class ComponentHierarchy extends JPanel implements NavigatorPanel, ExplorerManager.Provider {
    
    private static final Logger logger = Logger.getLogger(ComponentHierarchy.class.getName());
    
    private static ComponentHierarchy CH;
    
    private TreeView treeView;
    private Lookup lookup;
    private ExplorerManager explorerManager;
    
    public ComponentHierarchy() {
        createComponents();
    }
    
    private void createComponents() {
        explorerManager = new ExplorerManager();
        lookup = ExplorerUtils.createLookup(explorerManager, getActionMap());
        setLayout(new java.awt.BorderLayout());
        treeView = new BeanTreeView();
        add(treeView, BorderLayout.CENTER);
    }
    
    public static synchronized ComponentHierarchy getInstance() {
        if (CH == null) {
            CH = new ComponentHierarchy();
        }
        return CH;
    }

    @Override
    public String getDisplayName() {
        return NbBundle.getMessage(ComponentHierarchy.class, "CTL_ComponentHierarchy");
    }

    @Override
    public String getDisplayHint() {
        return NbBundle.getMessage(ComponentHierarchy.class, "HINT_ComponentHierarchy");
    }

    @Override
    public JComponent getComponent() {
        return this;
    }

    @Override
    public void panelActivated(Lookup context) {
        ComponentInfo ci = context.lookup(ComponentInfo.class);
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("panelActivated("+context+") ci = "+ci+", tc = "+context.lookup(ScreenshotComponent.class));
            if (ci != null) {
                logger.fine("  ci name = "+ci.getDisplayName());
            }
        }
        ExplorerUtils.activateActions(explorerManager, true);
    }

    @Override
    public void panelDeactivated() {
        ExplorerUtils.activateActions(explorerManager, false);
    }

    @Override
    public Lookup getLookup() {
        return lookup;
    }

    @Override
    public ExplorerManager getExplorerManager() {
        return explorerManager;
    }
    
}
