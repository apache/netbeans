/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2004, 2016 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 */
/*
 * AbstractToolbarFactory.java
 *
 * Created on January 25, 2004, 11:59 AM
 */

package org.netbeans.actions.engine.spi;

import java.awt.Component;
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
import java.util.Map;
import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import org.netbeans.actions.engine.spi.ToolbarFactory;
import org.netbeans.actions.spi.ActionProvider;

/**
 *
 * @author  Tim Boudreau
 */
public class AbstractToolbarFactory extends ToolbarFactory {
    private AbstractEngine engine;
    protected static final String KEY_CONTAINERCTX = "containerContext";
    protected static final String KEY_ACTION = "action";  //NOI18N
    protected static final String KEY_CREATOR = "creator"; //NOI18N
    /** Creates a new instance of AbstractToolbarFactory */
    public AbstractToolbarFactory(AbstractEngine engine) {
        if (engine == null) {
            throw new NullPointerException ("Engine may not be null"); //NOI18N
        }
        this.engine = engine;
    }
    
    protected final AbstractEngine getEngine() {
        return engine;
    }
    
    private String munge(String containerCtx) {
        return "toolbar" + "." + containerCtx; //NOI18N
    }
    
    private Map mappings = new HashMap();
    private void addMapping(String containerCtx, JComponent jc) {
        mappings.put (munge(containerCtx), jc);
    }
    
    private void removeMapping (String containerCtx) {
        mappings.remove (munge(containerCtx));
    }
    
    private JToolBar getToolbarForContext (String containerCtx) {
        return (JToolBar) mappings.get(munge(containerCtx));
    }
    
    public JToolBar createToolbar(String containerCtx) {
        JToolBar result = new JToolBar();
        result.setName(containerCtx);
        result.putClientProperty (KEY_CONTAINERCTX, containerCtx);
        result.putClientProperty (KEY_CREATOR, this);
        attachToToolbar(containerCtx, result);
        populateToolbar(containerCtx, result); //XXX listener should do this
        return result;
    }
    
    private void attachToToolbar (String containerCtx, JToolBar toolbar) {
        toolbar.addComponentListener (getToolbarListener());
        toolbar.addContainerListener (getToolbarListener());
    }
    
    private void detachFromToolbar (String containerCtx, JToolBar toolbar) {
        toolbar.removeComponentListener (getToolbarListener());
        toolbar.removeContainerListener (getToolbarListener());
        mappings.remove (containerCtx);
    }
    
    private Listener toolbarListener = null;
    private Listener getToolbarListener() {
        if (toolbarListener == null) {
            toolbarListener = new Listener();
        }
        return toolbarListener;
    }

    protected AbstractButton getOrCreateToolbarButton(int type) {
        AbstractButton result;
        if (type == ActionProvider.ACTION_TYPE_TOGGLE) {
            result = new JToggleButton();
        } else if (type == ActionProvider.ACTION_TYPE_ITEM) {
            result = new JButton();
        } else if (type == ActionProvider.ACTION_TYPE_SUBCONTEXT) {
            result = new JMenu();
        } else {
            result = null;
        }
        if (result != null) {
            result.addActionListener(getToolbarButtonListener());
        }
        //Otherwise presumably a wrapper or subclass will produce the result
        return result;
    }
    
    public void update (String containerCtx) {
//        System.err.println("ToolbarFactory update " + containerCtx);
        JToolBar tb = (JToolBar) mappings.get(munge(containerCtx));
        synchronized (tb.getTreeLock()) {
    //        System.err.println("Toolbar to update: " + tb);
            ActionProvider provider = getEngine().getActionProvider();
            if (tb != null) {
                Component[] c = tb.getComponents();
                for (int i=0; i < c.length; i++) {
                    if (c[i] instanceof AbstractButton) {
                        AbstractButton b = (AbstractButton) c[i];
                        String action = (String) b.getClientProperty (KEY_ACTION);
                        configureToolbarButton (b, containerCtx, action, provider,
                            getEngine().getContextProvider().getContext());
                    }
                }

            } else {
                System.err.println("Asked to update non existent toolbar " + containerCtx);
            }
        }
    }
    
    protected void populateToolbar (String containerCtx, JToolBar toolbar) {
//        System.err.println("AbstractToolbarFactory.populateToolbar");
        ActionProvider provider = getEngine().getActionProvider();
        String[] names = provider.getActionNames(containerCtx);
///        System.err.println("Names are " + Arrays.asList(names));
        for (int i=0; i < names.length; i++) {
            int type = provider.getActionType(names[i], containerCtx);
            AbstractButton item = getOrCreateToolbarButton(type);
            configureToolbarButton (item, containerCtx, names[i], provider, null);
            toolbar.add(item);
        }
        addMapping (containerCtx, toolbar);
        getEngine().notifyToolbarShown(containerCtx, toolbar); //XXX listener should do this
    }
    
    private void configureToolbarButton (AbstractButton item, String containerCtx, String action, ActionProvider provider, Map ctx) {
        item.setFocusable(false);
        item.setName(action);
        item.putClientProperty (KEY_ACTION, action);
        item.setToolTipText(
            provider.getDisplayName(action, containerCtx));
//        item.setToolTipText(provider.getDescription(action, containerCtx));
        int state = ctx == null ? ActionProvider.STATE_VISIBLE :
            provider.getState (action, containerCtx, ctx);
        boolean enabled = (state & ActionProvider.STATE_ENABLED) != 0;
        item.setEnabled(enabled);
        boolean visible = (state & ActionProvider.STATE_VISIBLE) != 0;
        item.setVisible (visible);
        boolean toggled = (state & ActionProvider.STATE_SELECTED) != 0;
        item.setSelected(toggled);
//        item.setMnemonic(provider.getMnemonic(action, containerCtx));
//        item.setDisplayedMnemonicIndex(provider.getMnemonicIndex(action, containerCtx));
        item.setIcon(provider.getIcon(action, containerCtx, BeanInfo.ICON_COLOR_16x16));
    }
    
    protected void depopulateToolbar (String containerCtx, JToolBar toolbar) {
        toolbar.removeAll();
        detachFromToolbar(containerCtx, toolbar);
    }
    
    private ButtonListener blistener = null;
    private ButtonListener getToolbarButtonListener() {
        if (blistener == null) {
            blistener = new ButtonListener();
        }
        return blistener;
    }
    
    private class ButtonListener implements ActionListener {
        public void actionPerformed (ActionEvent ae) {
            JComponent item = (JComponent) ae.getSource();
            String actionCommand = (String) item.getClientProperty(KEY_ACTION);
            String context = (String) item.getClientProperty(KEY_CONTAINERCTX);
            
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
    
    private class Listener extends ComponentAdapter implements ContainerListener {
        
        public void componentAdded(ContainerEvent e) {
            JComponent jc = (JComponent) e.getChild();
            JToolBar tb = (JToolBar) e.getContainer();
            String ctx = (String) tb.getClientProperty(KEY_CONTAINERCTX);
            jc.putClientProperty (KEY_CONTAINERCTX, ctx);
        }
        
        public void componentHidden(ComponentEvent e) {
            JToolBar jtb = (JToolBar) e.getComponent();
            String ctx = (String) jtb.getClientProperty(KEY_CONTAINERCTX);
            depopulateToolbar (ctx, jtb);
        }
        
        public void componentRemoved(ContainerEvent e) {
            JComponent jc = (JComponent) e.getChild();
            jc.putClientProperty (KEY_CONTAINERCTX, null);
        }
        
        public void componentShown(ComponentEvent e) {
            JToolBar jtb = (JToolBar) e.getComponent();
            String ctx = (String) jtb.getClientProperty(KEY_CONTAINERCTX);
            populateToolbar (ctx, jtb);
            getEngine().notifyToolbarShown(ctx, jtb);
        }
        
    }
    
}
