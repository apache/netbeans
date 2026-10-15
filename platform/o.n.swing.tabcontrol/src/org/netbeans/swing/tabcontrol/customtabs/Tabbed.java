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


package org.netbeans.swing.tabcontrol.customtabs;


import java.awt.*;
import java.awt.event.ActionListener;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.event.ChangeListener;
import org.openide.windows.TopComponent;


/**
 * Abstraction of a container similar to JTabbedPane. The container holds several
 * TopComponents and user is switching the active (showing) TopComponent by clicking
 * on a tab with TopComponent title (and icon). The look and feel of the container
 * may differ depending whether it is showing document or non-document TopComponents.
 *
 * @see TabbedComponentFactory
 *
 * @since 1.33
 *
 * @author  Peter Zavadsky
 * @author S. Aubrecht
 */
public abstract class Tabbed {

    public abstract void requestAttention(TopComponent tc);

    public abstract void cancelRequestAttention(TopComponent tc);

    /**
     * Turn tab highlight on/off
     * @param tc
     * @since 1.38
     */
    public void setAttentionHighlight(TopComponent tc, boolean highlight) {
    }
    
    public abstract void addTopComponent(String name, Icon icon, TopComponent tc, String toolTip);

    public abstract void insertComponent(String name, Icon icon, Component comp, String toolTip, int position);
    
    public abstract void setTopComponents(TopComponent[] tcs, TopComponent selected);
    
    public abstract int getTabCount();
    
    public abstract TopComponent[] getTopComponents();
    
    public abstract TopComponent getTopComponentAt(int index);
    
    public abstract int indexOf(Component tc);
    
    public abstract void removeComponent(Component comp);
    
    public abstract void setTitleAt(int index, String title);
    
    public abstract void setIconAt(int index, Icon icon);
    
    public abstract void setToolTipTextAt(int index, String toolTip);
    
    public abstract void setSelectedComponent(Component comp);
    
    public abstract TopComponent getSelectedTopComponent();

    public abstract void addChangeListener(ChangeListener listener);
    
    public abstract void removeChangeListener(ChangeListener listener);

    public abstract void addActionListener (ActionListener al);

    public abstract void removeActionListener (ActionListener al);

    public abstract void setActive(boolean active);
    
    public abstract int tabForCoordinate(Point p);
   
    public abstract Shape getIndicationForLocation(Point location, TopComponent startingTransfer,
            Point startingPoint, boolean attachingPossible);
    
    public abstract Object getConstraintForLocation(Point location, boolean attachingPossible);
    
    public abstract Image createImageOfTab (int tabIndex);
    
    /**
     * Accessor for visual component holding components
     * @see Accessor
     */
    public abstract Component getComponent();
    
    /** Allows tabbed implementors to speficy content of popup menu on tab
     * with given index. Incoming actions are default set by winsys
     */
    public abstract Action[] getPopupActions(Action[] defaultActions, int tabIndex);
    
    /** Returns bounds of tab with given index */
    public abstract Rectangle getTabBounds(int tabIndex);
    
    /**
     * @return Bounds of the area which displays the tab headers.
     * @since 2.32
     */
    public abstract Rectangle getTabsArea();
    
    public abstract boolean isTransparent();
    
    public abstract void setTransparent( boolean transparent );

    /**
     * Notify user that given TopComponent is 'busy' (some lengthy process is
     * running in it).
     * @param tc
     * @param busy True to make the TopComponent busy, false to cancel the notification.
     * @since 1.34
     */
    public void makeBusy( TopComponent tc, boolean busy ) {
    }

    /**
     * Check if given TopComponent is busy
     * @param tc
     * @return True if the TopComponent is busy and its header should be painted
     * in a special way, false otherwise.
     * @since 1.34
     */
    public boolean isBusy( TopComponent tc ) {
        return false;
    }
    
    /**
     * Visual containers that hold the tabbed components must implement this interface
     * otherwise window drag and drop and some popup menu may not work correctly.
     * 
     * @see Tabbed#getComponent()
     */
    public interface Accessor {

        public Tabbed getTabbed ();

    } // end of Accessor
}

