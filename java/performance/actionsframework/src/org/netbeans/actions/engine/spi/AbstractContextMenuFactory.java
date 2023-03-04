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
 * AbstractContextMenuFactory.java
 *
 * Created on January 31, 2004, 9:22 PM
 */

package org.netbeans.actions.engine.spi;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.BeanInfo;
import java.util.Map;
import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import org.netbeans.actions.spi.ActionProvider;
import org.netbeans.actions.spi.ContainerProvider;

/**
 *
 * @author  tim
 */
public abstract class AbstractContextMenuFactory implements ContextMenuFactory {
    private static final String KEY_CREATOR="creator";
    private static final String KEY_ACTION="action";
    private static final String KEY_CONTAINERCONTEXT="containercontext";
    private AbstractEngine engine;
    /** Creates a new instance of AbstractContextMenuFactory */
    protected AbstractContextMenuFactory(AbstractEngine engine) {
        this.engine = engine;
    }

    protected final AbstractEngine getEngine() {
        return engine;
    }
    
    public JPopupMenu createMenu(Map context) {
        JPopupMenu result = new JPopupMenu();
        populateMenu (result, context);
        return result;
    }
    
    private JMenuItem getOrCreateMenuItem(int type) {
        JMenuItem result = type == ActionProvider.ACTION_TYPE_ITEM ? new JMenuItem() :
            type == ActionProvider.ACTION_TYPE_SUBCONTEXT ? new JMenu() : null;
        if (type == ActionProvider.ACTION_TYPE_ITEM) {
            result.addActionListener (getItemListener());
        } else if (type == ActionProvider.ACTION_TYPE_SUBCONTEXT){
            //attachToMenu ((JMenu) result);
        }
        result.putClientProperty (KEY_CREATOR, this);
        return result;
    }    
    
    protected void populateMenu (JPopupMenu menu, Map context) {
        ActionProvider provider = getEngine().getActionProvider();
        String[] names = provider.getActionNames(ContainerProvider.CONTEXTMENU_CONTEXT);
        for (int i=0; i < names.length; i++) {
            JMenuItem item = getOrCreateMenuItem(
                provider.getActionType(names[i], ContainerProvider.CONTEXTMENU_CONTEXT));
            configureMenuItem (item, ContainerProvider.CONTEXTMENU_CONTEXT, names[i], provider, context);
            menu.add(item);
        }
//        getEngine().notifyMenuShown(ContainerProvider.CONTEXTMENU_CONTEXT, menu); //XXX listener should do this
//        addMapping (ContainerProvider.CONTEXTMENU_CONTEXT, menu, ContainerProvider.TYPE_MENU); //XXX handle popup
    }    
    
    private void configureMenuItem (JMenuItem item, String containerCtx, String action, ActionProvider provider, Map context) {
//        System.err.println("ConfigureMenuItem: " + containerCtx + "/" + action);
        item.setName(action);
        item.putClientProperty(KEY_ACTION, action);
        item.putClientProperty(KEY_CONTAINERCONTEXT, containerCtx);
        item.putClientProperty(KEY_CREATOR, this);
        item.setText(
            provider.getDisplayName(action, containerCtx));
        item.setToolTipText(provider.getDescription(action, containerCtx));
        int state = context == null ? ActionProvider.STATE_ENABLED | ActionProvider.STATE_VISIBLE :
            provider.getState (action, containerCtx, context);
        boolean enabled = (state & ActionProvider.STATE_ENABLED) != 0; 
        item.setEnabled(enabled);
        boolean visible = (state & ActionProvider.STATE_VISIBLE) != 0;
        //Intentionally use enabled property
        item.setVisible(enabled);
        item.setMnemonic(provider.getMnemonic(action, containerCtx));
        item.setDisplayedMnemonicIndex(provider.getMnemonicIndex(action, containerCtx));
        item.setIcon(provider.getIcon(action, containerCtx, BeanInfo.ICON_COLOR_16x16));
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
    
}
