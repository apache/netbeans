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


package org.netbeans.core.windows.view.ui;


import org.netbeans.swing.tabcontrol.customtabs.Tabbed;
import org.netbeans.core.windows.Constants;
import org.netbeans.core.windows.WindowManagerImpl;
import org.netbeans.core.windows.view.ModeContainer;
import org.netbeans.core.windows.view.ModeView;
import org.netbeans.core.windows.view.dnd.TopComponentDroppable;
import org.netbeans.core.windows.view.dnd.WindowDnDManager;
import org.openide.windows.TopComponent;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;
import org.netbeans.core.windows.view.dnd.TopComponentDraggable;


/** 
 * Abstract helper implementation of <code>ModeContainer</code>.
 * PENDING: It provides also support for TopComponentDroppable.
 *
 * @author  Peter Zavadsky
 */
public abstract class AbstractModeContainer implements ModeContainer {
    
    /** Associated mode view. */
    protected final ModeView modeView;

    protected final TabbedHandler tabbedHandler;
    
    // PENDING
    protected final WindowDnDManager windowDnDManager;
    
    // kind of mode, editor or view
    private final int kind;
    

    public AbstractModeContainer(ModeView modeView, WindowDnDManager windowDnDManager, int kind) {
        this.modeView = modeView;
        this.windowDnDManager = windowDnDManager;
        this.kind = kind;
        this.tabbedHandler = new TabbedHandler(modeView, kind, createTabbed());
    }


    public ModeView getModeView() {
        return modeView;
    }
    
    /** */
    public Component getComponent() {
        return getModeComponent();
    }
    
    protected abstract Component getModeComponent();
    
    protected abstract Tabbed createTabbed();

    public void addTopComponent(TopComponent tc) {
        tabbedHandler.addTopComponent(tc, kind);
    }

    public void removeTopComponent(TopComponent tc) {
        tabbedHandler.removeTopComponent(tc);

        TopComponent selected = tabbedHandler.getSelectedTopComponent();
        updateTitle(selected == null
            ? "" : WindowManagerImpl.getInstance().getTopComponentDisplayName(selected)); // NOI18N
    }
    
    public void setSelectedTopComponent(TopComponent tc) {
        tabbedHandler.setSelectedTopComponent(tc);
        
        updateTitle(WindowManagerImpl.getInstance().getTopComponentDisplayName(tc));
    }
    
    public void setTopComponents(TopComponent[] tcs, TopComponent selected) {
        //Cheaper to do the equality test here than later
        if (!Arrays.equals(tcs, getTopComponents())) {
            tabbedHandler.setTopComponents(tcs, selected);
            updateTitle(WindowManagerImpl.getInstance().getTopComponentDisplayName(selected));
        } else {
            //[dafe] It is also used as selection modifier only, for example when
            // clearing selection to null on sliding modes
            setSelectedTopComponent(selected);
        }
    }
    
    protected abstract void updateTitle(String title);
    
    protected abstract void updateActive(boolean active);
    
    
    public TopComponent getSelectedTopComponent() {
        return tabbedHandler.getSelectedTopComponent();
    }
    
    public void setActive(boolean active) {
        updateActive(active);

        TopComponent selected = tabbedHandler.getSelectedTopComponent();
        updateTitle(selected == null
            ? "" : WindowManagerImpl.getInstance().getTopComponentDisplayName(selected)); // NOI18N
        
        tabbedHandler.setActive(active);
    }
    
    @Override
    public void focusSelectedTopComponent() {
        // PENDING focus gets main window sometimes, investgate and refine (jdk1.4.1?).
        final TopComponent selectedTopComponent = tabbedHandler.getSelectedTopComponent();

        if (selectedTopComponent == null) {
            return;
        }

        Window oldFocusedW = FocusManager.getCurrentManager().getFocusedWindow();
        Window newFocusedW = SwingUtilities.getWindowAncestor(selectedTopComponent);
        //#177550: Call requestFocus on selected TC only if TC is in AWT hierarchy
        if (newFocusedW != null) {
            if (newFocusedW.equals(oldFocusedW) || null == oldFocusedW) {
                // focus transfer inside one window or system is not active in OS at all
                // so requestFocusInWindow call is right and enough
                selectedTopComponent.requestFocusInWindow();
            } else {
                // focus transfer between different windows
                newFocusedW.toFront();
                selectedTopComponent.requestFocus();
            }
        }
    }
    
    public TopComponent[] getTopComponents() {
        return tabbedHandler.getTopComponents();
    }

    public void updateName(TopComponent tc) {
        TopComponent selected = getSelectedTopComponent();
        if(tc == selected) {
            updateTitle(tc == null 
                ? "" : WindowManagerImpl.getInstance().getTopComponentDisplayName(tc)); // NOI18N
        }
        
        tabbedHandler.topComponentNameChanged(tc, kind);
    }
    
    public void updateToolTip(TopComponent tc) {
        tabbedHandler.topComponentToolTipChanged(tc);
    }
    
    public void updateIcon(TopComponent tc) {
        tabbedHandler.topComponentIconChanged(tc);
    }

    // XXX
    protected int getKind() {
        return kind;
    }
    
    // Support for TopComponentDroppable
    protected Shape getIndicationForLocation(Point location) {
        return tabbedHandler.getIndicationForLocation(location,
            windowDnDManager.getStartingTransfer().getTopComponent(),
            windowDnDManager.getStartingPoint(),
            isAttachingPossible());
    }
    
    protected Object getConstraintForLocation(Point location) {
        return tabbedHandler.getConstraintForLocation(location, isAttachingPossible());
    }
    
    protected abstract boolean isAttachingPossible();
    
    protected ModeView getDropModeView() {
        return modeView;
    }
    
    protected Component getDropComponent() {
        return tabbedHandler.getComponent();
    }
    
    protected abstract TopComponentDroppable getModeDroppable();
    
    protected boolean canDrop(TopComponentDraggable transfer) {
        if(transfer.isAllowedToMoveAnywhere()) {
            return true;
        }
        
        boolean isNonEditor = transfer.getKind() == Constants.MODE_KIND_VIEW || transfer.getKind() == Constants.MODE_KIND_SLIDING;
        boolean thisIsNonEditor = this.kind == Constants.MODE_KIND_VIEW || this.kind == Constants.MODE_KIND_SLIDING;
        return isNonEditor == thisIsNonEditor;
    }
    // Support for TopComponentDroppable
}

