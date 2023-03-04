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
 * AbstractMenuFactory.java
 *
 * Created on January 24, 2004, 7:23 PM
 */

package org.netbeans.actions.engine.spi;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;
import java.beans.BeanInfo;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import org.netbeans.actions.spi.ActionProvider;
import org.netbeans.actions.spi.ContainerProvider;

/** Basic implementation of a menu factory.
 *
 * @author  Tim Boudreau
 */
public abstract class AbstractMenuFactory implements MenuFactory {
    private AbstractEngine engine;
    protected static final String KEY_CONTAINERCONTEXT = "containerContext"; //NOI18N
    protected static final String KEY_ACTION = "action";
    protected static final String KEY_CREATOR = "creator";
    /** Creates a new instance of AbstractMenuFactory */
    protected AbstractMenuFactory(AbstractEngine engine) {
        if (engine == null) throw new NullPointerException (
            "Engine may not be null."); //NOI18N
        this.engine = engine;
    }
    
    protected AbstractEngine getEngine() {
        return engine;
    }
    
    public JMenu createMenu (String containerContext) {
        System.err.println("Creating a menu: " + containerContext);
        JMenu result = new JMenu();
        result.putClientProperty (KEY_CONTAINERCONTEXT, containerContext);
        result.setName(containerContext);
        result.setText (getEngine().getContainerProvider().getDisplayName(
            ContainerProvider.TYPE_MENU, containerContext));
        populateMenu(containerContext, result); //XXX listener should do this
        attachToMenu(result);
        return result;
    }
    
    private void attachToMenu (JMenu menu) {
        menu.addContainerListener(getMenuListener());
        menu.addComponentListener(getMenuListener());
        menu.addMenuListener(getMenuListener());
    }
    
    public String getContainerContext (JMenu menu) {
        return (String) menu.getClientProperty (KEY_CONTAINERCONTEXT);
    }
    
    private Listener listener = null;
    private Listener getMenuListener() {
        if (listener == null) {
            listener = new Listener();
        }
        return listener;
    }
    
    private JMenuItem getOrCreateMenuItem(int type) {
        JMenuItem result = type == ActionProvider.ACTION_TYPE_ITEM ? new JMenuItem() :
            type == ActionProvider.ACTION_TYPE_SUBCONTEXT ? new JMenu() : null;
        if (type == ActionProvider.ACTION_TYPE_ITEM) {
            result.addActionListener (getItemListener());
        } else if (type == ActionProvider.ACTION_TYPE_SUBCONTEXT){
            attachToMenu ((JMenu) result);
        }
        result.putClientProperty (KEY_CREATOR, this);
        return result;
    }
    
    protected void populateMenu (String containerCtx, JMenu menu) {
        ActionProvider provider = getEngine().getActionProvider();
        String[] names = provider.getActionNames(containerCtx);
        for (int i=0; i < names.length; i++) {
            JMenuItem item = getOrCreateMenuItem(
                provider.getActionType(names[i], containerCtx));
            configureMenuItem (item, containerCtx, names[i], provider, null);
            menu.add(item);
        }
        getEngine().notifyMenuShown(containerCtx, menu); //XXX listener should do this
        addMapping (containerCtx, menu, ContainerProvider.TYPE_MENU); //XXX handle popup
    }
    
    protected void depopulateMenu (String containerCtx, JMenu menu) {
        menu.removeAll();
    }
    
    private void configureMenuItem (JMenuItem item, String containerCtx, String action, ActionProvider provider, Map context) {
//        System.err.println("ConfigureMenuItem: " + containerCtx + "/" + action);
        item.setName(action);
        item.putClientProperty(KEY_ACTION, action);
        item.putClientProperty(KEY_CONTAINERCONTEXT, containerCtx);
        item.putClientProperty(KEY_CREATOR, this);
        item.setText(
            provider.getDisplayName(action, containerCtx));
//        System.err.println("  item text is " + item.getText());
        item.setToolTipText(provider.getDescription(action, containerCtx));
        int state = context == null ? ActionProvider.STATE_ENABLED | ActionProvider.STATE_VISIBLE :
            provider.getState (action, containerCtx, context);
        boolean enabled = (state & ActionProvider.STATE_ENABLED) != 0; 
//        System.err.println("action " + action + (enabled ? " enabled" : " disabled"));
        item.setEnabled(enabled);
        boolean visible = (state & ActionProvider.STATE_VISIBLE) != 0;
//        System.err.println("action " + action + (visible ? " visible" : " invisible"));
        item.setVisible(visible);
        item.setMnemonic(provider.getMnemonic(action, containerCtx));
        item.setDisplayedMnemonicIndex(provider.getMnemonicIndex(action, containerCtx));
        item.setIcon(provider.getIcon(action, containerCtx, BeanInfo.ICON_COLOR_16x16));
    }
    
    private String munge (String containerCtx, Object type) {
        //XXX create a munge for popup menus
        return "menu." + "mainMenu." + containerCtx;
    }
    
    private Map mappings = new HashMap();
    private void addMapping (String containerCtx, JComponent comp, Object type) {
        mappings.put (munge (containerCtx, type), comp);
    }
    
    public void update (String containerCtx) {
        JMenu menu = (JMenu) mappings.get(munge(containerCtx, ContainerProvider.TYPE_MENU));
        if (menu != null) {
            updateMenu (menu);
        } else {
            System.err.println("Tried to update unknown menu context:" + containerCtx);
        }
    }
    
    private void updateMenu (JMenu menu) {
        ActionProvider provider = getEngine().getActionProvider();
        Map context = getEngine().getContextProvider().getContext();
        String containerCtx = (String) menu.getClientProperty(KEY_CONTAINERCONTEXT);
        boolean isDynamic = getEngine().getContainerProvider().isDynamicContext(
            ContainerProvider.TYPE_MENU, containerCtx);
        
        String[] actions = provider.getActionNames(containerCtx);
//        System.err.println("Updating menu " + containerCtx + "actions: " + Arrays.asList(actions));
        
        int count = menu.getItemCount();
//        System.err.println("Item count = " + count);
        //XXX for dynamic menus, we'll need to compare the contents of the
        //menu with the list of strings, and add/prune
        
        for (int i=0; i < count; i++) {
            JMenuItem item = menu.getItem(i);
            if (item != null) {
                String action = (String) item.getClientProperty (KEY_ACTION);
                configureMenuItem (item, containerCtx, action, provider, context);
            }
        }
    }
    
    private MenuItemListener itemListener = null;
    private MenuItemListener getItemListener() {
        if (itemListener == null) {
            itemListener = new MenuItemListener();
        }
        return itemListener;
    }
    
    private class MenuItemListener implements ActionListener {
        public void actionPerformed(java.awt.event.ActionEvent e) {
            JMenuItem item = (JMenuItem) e.getSource();
            String actionCommand = (String) item.getClientProperty(KEY_ACTION);
            String context = (String) item.getClientProperty(KEY_CONTAINERCONTEXT);
            
            getEngine().notifyWillPerform (actionCommand, context);
            
            Action action = getEngine().getAction(context, actionCommand);
            
            if (action.isEnabled()) {
                ActionEvent event = new ActionEvent (item, 
                ActionEvent.ACTION_PERFORMED, actionCommand);
                action.actionPerformed(event);
            }
            
            getEngine().notifyPerformed (actionCommand, context);
        }
    }
    
    /** Listener which listens to all components to determine their state and
     * add context information to child components */
    private class Listener extends ComponentAdapter implements ContainerListener, MenuListener {
        public void componentAdded(ContainerEvent e) {
            JMenu menu = (JMenu) e.getContainer();
            JComponent item = (JComponent) e.getChild();
            //Mark the child as belonging to the parent container context
            String containerContext = getContainerContext(menu);
            
            item.putClientProperty (KEY_CONTAINERCONTEXT, containerContext);
        }
        
        public void componentRemoved(ContainerEvent e) {
            JComponent menu = (JComponent) e.getContainer();
            JComponent item = (JComponent) e.getChild();
            item.putClientProperty (KEY_CONTAINERCONTEXT, null);
        }
        
        public void componentHidden(ComponentEvent e) {
            JMenu menu = (JMenu) e.getComponent();
            String containerContext = getContainerContext(menu);
            getEngine().notifyMenuHidden (containerContext, menu);
            depopulateMenu (containerContext, menu);
        }
        
        public void componentShown(ComponentEvent e) {
            JMenu menu = (JMenu) e.getComponent();
            String containerCtx = getContainerContext(menu);
            System.err.println("ComponentShown: Menu" + containerCtx + " - " + menu);
            populateMenu(containerCtx, menu);
            getEngine().notifyMenuShown (containerCtx, menu);
        }
        
        public void menuCanceled(MenuEvent e) {
        }
        
        public void menuDeselected(MenuEvent e) {
        }
        
        public void menuSelected(MenuEvent e) {
        }
        
    }
}
