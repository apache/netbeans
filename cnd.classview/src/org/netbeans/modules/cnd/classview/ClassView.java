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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.cnd.classview;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.beans.PropertyVetoException;
import javax.accessibility.Accessible;
import javax.accessibility.AccessibleContext;
import javax.accessibility.AccessibleRole;
import org.netbeans.modules.cnd.api.model.*;
import org.netbeans.modules.cnd.api.model.util.CsmTracer;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collections;
import javax.swing.*;
import javax.swing.JComponent.AccessibleJComponent;
import javax.swing.text.*;
import org.netbeans.modules.cnd.api.project.NativeProjectRegistry;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.ExplorerUtils;
import org.openide.explorer.view.BeanTreeView;
import org.openide.nodes.*;
import org.netbeans.modules.cnd.classview.resources.I18n;
import org.netbeans.modules.cnd.modelutil.AbstractCsmNode;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;


/**
 * View as such
 */
public class ClassView extends JComponent implements ExplorerManager.Provider, Accessible, PropertyChangeListener, Lookup.Provider {
    
    /** composited view */
    protected BeanTreeView view;
    private ClassViewModel model;// = new ClassViewModel();
    private ViewMouseListener mouseListener = new ViewMouseListener();
    private final ExplorerManager manager = new ExplorerManager();
    private final InstanceContent selectedNodes = new InstanceContent();
    private final Lookup lookup = new AbstractLookup(selectedNodes);

    private static final boolean TRACE_MODEL_CHANGE_EVENTS = Boolean.getBoolean("cnd.classview.trace.events"); // NOI18N
    
    public ClassView() {
        setLayout(new BorderLayout());
        //init();
        manager.addPropertyChangeListener(this);
        ActionMap map = this.getActionMap();
        map.put(DefaultEditorKit.copyAction, ExplorerUtils.actionCopy(manager));
        setupRootContext(createEmptyRoot());
    }

    public void propertyChange(PropertyChangeEvent evt) {
        if (ExplorerManager.PROP_SELECTED_NODES.equals(evt.getPropertyName())) {
            selectedNodes.setPairs(Collections.<AbstractLookup.Pair>emptyList());
            for (Node n : (Node[]) evt.getNewValue()) {
                selectedNodes.add(n);
                if (n instanceof AbstractCsmNode) {
                    CsmObject csmObject = ((AbstractCsmNode) n).getCsmObject();
                    if (csmObject != null) {
                        selectedNodes.add(csmObject);
                    }
                }
            }
        }
    }

    /*package local*/ void selectInClasses(final CsmOffsetableDeclaration decl) {
        CsmModelAccessor.getModel().enqueue(new Runnable() {

            public void run() {
                ClassViewModel currentModel = getModel();
                if (currentModel != null) {
                    Node node = currentModel.findDeclaration(decl);
                    if (node != null) {
                        try {
                            setUserActivity();
                            getExplorerManager().setSelectedNodes(new Node[]{node});
                        } catch (PropertyVetoException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                    }
                }
            }
        }, "Class View: select in classes"); // NOI18N
    }

    public Lookup getLookup() {
        return lookup;
    }

    private void init(){
        view = new BeanTreeView();
        view.setRootVisible(false);
        view.setDragSource(true);
        add(view, BorderLayout.CENTER);
        setToolTipText(I18n.getMessage("ClassViewTitle")); // NOI18N
        setName(I18n.getMessage("ClassViewTooltip")); // NOI18N
    }
    
    /* Read accessible context
     * @return - accessible context
     */
    @Override
    public AccessibleContext getAccessibleContext() {
        if (accessibleContext == null) {
            accessibleContext = new AccessibleJComponent() {
                @Override
                public AccessibleRole getAccessibleRole() {
                    return AccessibleRole.PANEL;
                }
                
                @Override
                public String getAccessibleName() {
                    if (accessibleName != null) {
                        return accessibleName;
                    }
                    
                    return getName();
                }
                
                /* Fix for 19344: Null accessible decription of all TopComponents on JDK1.4 */
                @Override
                public String getToolTipText() {
                    return ClassView.this.getToolTipText();
                }
            };
        }
        
        return accessibleContext;
    }
    
    @Override
    public boolean requestFocusInWindow() {
        super.requestFocusInWindow();
        return view.requestFocusInWindow();
    }
    
    // In the SDI, requestFocus is called rather than requestFocusInWindow:
    @Override
    public void requestFocus() {
        super.requestFocus();
        view.requestFocus();
    }
    
    private void addRemoveViewListeners(boolean add){
        Component[] scroll = view.getComponents();
        if (scroll != null){
            for(int i = 0; i < scroll.length; i++){
                Component comp = scroll[i];
                if (comp instanceof JScrollBar) {
                    if (add) {
                        comp.addMouseListener(mouseListener);
                        comp.addMouseMotionListener(mouseListener);
                    } else {
                        comp.removeMouseListener(mouseListener);
                        comp.removeMouseMotionListener(mouseListener);
                    }
                }
            }
        }
        JViewport port = view.getViewport();
        Component[] comp = port.getComponents();
        if (comp != null && comp.length>0) {
            if (add) {
                comp[0].addMouseListener(mouseListener);
                comp[0].addMouseMotionListener(mouseListener);
            } else {
                comp[0].removeMouseListener(mouseListener);
                comp[0].removeMouseMotionListener(mouseListener);
            }
        }
    }
    
    private Timer userActivity = null;
    /**
     * delay on user activity.
     */
    private static final int USER_MOUSE_ACTIVITY_DELAY = 2000;
    
    private void setUserActivity(){
        if (userActivity == null) {
            userActivity = new Timer(USER_MOUSE_ACTIVITY_DELAY, new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    stopViewModify();
                }
            });
        }
        userActivity.restart();
        ClassViewModel currentModel = getModel();
        if (currentModel != null && !currentModel.isUserActivity()) {
            currentModel.setUserActivity(true);
            //System.out.println("Start user activity");
        }
    }
    
    private void stopViewModify(){
        ClassViewModel currentModel = getModel();
        if (currentModel != null) {
            currentModel.setUserActivity(false);
        }
        //System.out.println("Stop user activity");
        if (userActivity != null) {
            userActivity.stop();
        }
    }
    
    /*package local*/ void startup() {
        if( Diagnostic.DEBUG ) {Diagnostic.trace("ClassesV: startup()");} // NOI18N
        ClassViewModel currentModel = getModel();
        setModel(new ClassViewModel());
        if( currentModel != null ) {
            currentModel.dispose();
        }
        init();
        addRemoveViewListeners(true);
        if( NativeProjectRegistry.getDefault().getOpenProjects().isEmpty() ) {
            setupRootContext(createEmptyRoot());
        } else {
            setupRootContext(getModel().getRoot());
        }
    }
    
    /*package local*/ void shutdown() {
        if( Diagnostic.DEBUG ) {Diagnostic.trace("ClassesV: shutdown()");} // NOI18N
        addRemoveViewListeners(false);
        ClassViewModel currentModel = getModel();
        if( currentModel != null ) {
            currentModel.dispose();
            setModel(null);
        }
        stopViewModify();
        remove(view);
        view = null;
        userActivity =null;
        mouseListener = null;
        setupRootContext(createEmptyRoot());
    }
    
    public ExplorerManager getExplorerManager() {
        return manager;
    }
    
    /*package local*/ void projectOpened(final CsmProject project) {
        if( Diagnostic.DEBUG ) {Diagnostic.trace("ClassesV: projectOpened() "+project);} // NOI18N
        ClassViewModel currentModel = getModel();
		if (currentModel != null) {
		    currentModel.openProject(project);
		    setupRootContext(currentModel.getRoot());
		}
    }
    
    /*package local*/ void projectClosed(CsmProject project) {
        if( Diagnostic.DEBUG ) {Diagnostic.trace("ClassesV: projectClosed() " + project);} // NOI18N
        ClassViewModel currentModel = getModel();
        if (currentModel != null && !getExplorerManager().getRootContext().isLeaf()) {
            currentModel.closeProject(project);
            RootNode root = currentModel.getRoot();
            Children children = root.getChildren();
            if ((children instanceof ProjectsKeyArray) && ((ProjectsKeyArray) children).isEmpty()){
                setupRootContext(createEmptyRoot());
            } else {
                setupRootContext(root);
            }
        }
    }
    
    /*package local*/ void modelChanged(CsmChangeEvent e) {
        if( TRACE_MODEL_CHANGE_EVENTS ) {
            new CsmTracer().dumpModelChangeEvent(e);
        }
        ClassViewModel currentModel = getModel();
        if (currentModel != null) {
            currentModel.scheduleUpdate(e);
        }
    }
    
    private Node createEmptyRoot() {
        return Node.EMPTY;
    }
    
    // VK: code is copied from org.netbeans.modules.favorites.Tab class
    /** Exchanges deserialized root context to projects root context
     * to keep the uniquennes. */
    private void setupRootContext(final Node rc) {
        if (getExplorerManager().getRootContext() != rc){
            try {
                getExplorerManager().setSelectedNodes(new Node[0]);
            } catch (PropertyVetoException ex) {
                ex.printStackTrace();
            }
            if (Diagnostic.DEBUG) {Diagnostic.trace("ClassesV: setupRootContext() " + rc);} // NOI18N
            getExplorerManager().setRootContext(rc);
        }
    }

    private ClassViewModel getModel() {
        return model;
    }

    private void setModel(ClassViewModel model) {
        this.model = model;
    }
    
    private class ViewMouseListener implements MouseListener, MouseMotionListener{
        
        public void mouseClicked(MouseEvent e) {
            setUserActivity();
        }
        
        public void mouseEntered(MouseEvent e) {
            setUserActivity();
        }
        
        public void mouseExited(MouseEvent e) {
            setUserActivity();
        }
        
        public void mousePressed(MouseEvent e) {
            setUserActivity();
        }
        
        public void mouseReleased(MouseEvent e) {
            setUserActivity();
        }
        
        public void mouseDragged(MouseEvent e) {
            setUserActivity();
        }
        
        public void mouseMoved(MouseEvent e) {
            setUserActivity();
        }
    }
}
