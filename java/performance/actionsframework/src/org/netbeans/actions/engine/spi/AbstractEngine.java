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
/*
 * AbstractEngine.java
 *
 * Created on January 24, 2004, 1:36 PM
 */

package org.netbeans.actions.engine.spi;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JToolBar;
import org.netbeans.actions.api.ContextProvider;
import org.netbeans.actions.api.Engine;
import org.netbeans.actions.engine.spi.MenuFactory;
import org.netbeans.actions.engine.spi.ToolbarFactory;
import org.netbeans.actions.spi.ActionProvider;
import org.netbeans.actions.spi.ContainerProvider;

/** Convenience basic impl of Engine. Mostly it connects individual
 * infrastructure pieces with each other.
 *
 * @author  Tim Boudreau
 */
public abstract class AbstractEngine extends Engine {
    private ActionProvider actionProvider;
    private ContainerProvider containerProvider;
    private MenuFactory menuFactory = null;
    private ToolbarFactory toolbarFactory = null;
    private ActionFactory actionFactory = null;
    
    /** Creates a new instance of AbstractEngine */
    protected AbstractEngine(ActionProvider actionProvider, ContainerProvider containerProvider) {
        this.actionProvider = actionProvider;
        this.containerProvider = containerProvider;
    }
    
    private ContextProvider contextProvider;
    public void setContextProvider (ContextProvider ctx) {
        this.contextProvider = ctx;
    }
    
    public ContextProvider getContextProvider() {
        return contextProvider;
    }
    
    protected final ActionProvider getActionProvider() {
        return actionProvider;
    }
    
    protected final ContainerProvider getContainerProvider() {
        return containerProvider;
    }
    
    protected Action getAction (String containerCtx, String action) {
        return getActionFactory().getAction(action, containerCtx, getContextProvider().getContext());
    }
    
    protected abstract MenuFactory createMenuFactory();
    
    protected abstract ToolbarFactory createToolbarFactory();
    
    protected abstract ActionFactory createActionFactory();
    
    protected abstract ContextMenuFactory createContextMenuFactory();
    
    void notifyWillPerform (String action, String containerCtx) {
        
    }
    
    void notifyPerformed (String action, String containerCtx) {
        update();
    }
    
    private ContextMenuFactory contextMenuFactory = null;
    public final ContextMenuFactory getContextMenuFactory() {
        if (contextMenuFactory == null) {
            contextMenuFactory = createContextMenuFactory();
        }
        return contextMenuFactory;
    }
    
    public final MenuFactory getMenuFactory() {
        if (menuFactory == null) {
            menuFactory = createMenuFactory();
        }
        return menuFactory;
    }
    
    public final ToolbarFactory getToolbarFactory() {
        if (toolbarFactory == null) {
            toolbarFactory = createToolbarFactory();
        }
        return toolbarFactory;
    }
    
    public final ActionFactory getActionFactory() {
        if (actionFactory == null) {
            actionFactory = createActionFactory();
        }
        return actionFactory;
    }
    

    void notifyOpened (String containerCtx, Object type) {
        
    }
    
    void notifyClosed (String containerCtx, Object type) {
        
    }
    
    boolean isOpen(String containerCtx, Object type) {
        if (type == ContainerProvider.TYPE_MENU) {
            return showingMenus.contains(containerCtx);
        } else if (type == ContainerProvider.TYPE_TOOLBAR){
            return showingToolbars.contains(containerCtx);
        } else {
            return false;
        }
    }

    
    /** Indicates a menu has been displayed onscreen, not that it has been opened.
     */
    protected void notifyMenuShown (String containerCtx, JMenu menu) {
        showingMenus.add(containerCtx);
    }
    
    protected void notifyMenuHidden (String containerCtx, JMenu menu) {
        showingMenus.remove (containerCtx);
    }
    
    protected void notifyToolbarShown (String containerCtx, JToolBar toolbar) {
        showingToolbars.add (containerCtx);
    }
    
    protected void notifyToolbarHidden (String containerCtx, JToolBar toolbar) {
        showingToolbars.remove (containerCtx);
    }
    
    public void update() {
        updateToolbars();
        updateMenus();
    }
    
    protected void updateToolbars() {
        int count = showingToolbars.size();
        String[] toolbars = new String[count];
        toolbars = (String[]) showingToolbars.toArray(toolbars);
        for (int i=0; i < count; i++) {
            getToolbarFactory().update (toolbars[i]);
        }
    }
    
    protected void updateMenus() {
        int count = showingMenus.size();
        String[] menus = new String[count];
        menus = (String[]) showingMenus.toArray(menus);
        for (int i=0; i < count; i++) {
            getMenuFactory().update (menus[i]);
        }
    }
    
    public final JMenuBar createMenuBar() {
        JMenuBar result = new JMenuBar();
        ContainerProvider cp = getContainerProvider();
        String[] menus = cp.getMenuContainerContexts();
        for (int i=0; i < menus.length; i++) {
            JMenu menu = getMenuFactory().createMenu(menus[i]);
            result.add (menu);
        }
        return result;
    }
    
    public final JToolBar[] createToolbars() {
        ContainerProvider cp = getContainerProvider();
        String[] toolbars = cp.getToolbarContainerContexts();
        JToolBar[] result = new JToolBar[toolbars.length];
        for (int i=0; i < toolbars.length; i++) {
            result[i] = getToolbarFactory().createToolbar(toolbars[i]);
        }
        return result;
    }
    
    public JPopupMenu createPopupMenu() {
        return getContextMenuFactory().createMenu(getContextProvider().getContext());
    }
    
    private Set showingMenus = new HashSet();
    private Set showingToolbars = new HashSet();
    
}
