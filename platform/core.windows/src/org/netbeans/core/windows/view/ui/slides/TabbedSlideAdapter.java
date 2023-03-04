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

package org.netbeans.core.windows.view.ui.slides;

import java.awt.Component;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.Action;
import javax.swing.DefaultSingleSelectionModel;
import javax.swing.Icon;
import javax.swing.SingleSelectionModel;
import javax.swing.event.ChangeListener;
import org.netbeans.core.windows.Constants;
import org.netbeans.core.windows.ModeImpl;
import org.netbeans.core.windows.WindowManagerImpl;
import org.netbeans.core.windows.actions.ActionUtils;
import org.netbeans.swing.tabcontrol.DefaultTabDataModel;
import org.netbeans.core.windows.view.dnd.DragAndDropFeedbackVisualizer;
import org.netbeans.swing.tabcontrol.SlideBarDataModel;
import org.netbeans.swing.tabcontrol.TabData;
import org.netbeans.swing.tabcontrol.TabDataModel;
import org.netbeans.swing.tabcontrol.TabbedContainer;
import org.netbeans.swing.tabcontrol.customtabs.Tabbed;
import org.openide.util.ChangeSupport;
import org.openide.util.ImageUtilities;
import org.openide.windows.TopComponent;

/*
 * Adapts SlideBar to match Tabbed interface, which is used by TabbedHandler
 * for talking to component containers. SlideBar is driven indirectly,
 * through modifications of its model.
 *
 * @author Dafe Simonek
 */
public final class TabbedSlideAdapter extends Tabbed {
    
    /** data model of informations about top components in container */
    private TabDataModel dataModel;
    /** selection model which contains selection info in container */
    private SingleSelectionModel selModel;
    /** Visual component for displaying box for sliding windows */
    private SlideBar slideBar;
    /** List of action listeners */
    private List<ActionListener> actionListeners;
    private final ChangeSupport cs = new ChangeSupport(this);
    
    private final ModeImpl slidingMode;
    
    /** Creates a new instance of SlideBarTabs */
    public TabbedSlideAdapter(String side) {
        dataModel = new SlideBarDataModel.Impl();
        setSide(side);
        selModel = new DefaultSingleSelectionModel();
        slideBar = new SlideBar(this, (SlideBarDataModel)dataModel, selModel);
        slidingMode = findSlidingMode();
    }
    
    @Override
    public void requestAttention (TopComponent tc) {
        slideBar.setBlinking(tc, true);
    }
    
    @Override
    public void cancelRequestAttention (TopComponent tc) {
        slideBar.setBlinking(tc, false);
    }

    @Override
    public void makeBusy( TopComponent tc, boolean busy ) {
        slideBar.makeBusy( tc, busy );
    }

    @Override
    public boolean isBusy( TopComponent tc ) {
        return WindowManagerImpl.getInstance().isTopComponentBusy( tc );
    }
    
    private void setSide (String side) {
        int orientation = SlideBarDataModel.WEST;
        if (Constants.LEFT.equals(side)) {
            orientation = SlideBarDataModel.WEST;
        } else if (Constants.RIGHT.equals(side)) {
            orientation = SlideBarDataModel.EAST;
        } else if (Constants.BOTTOM.equals(side)) {
            orientation = SlideBarDataModel.SOUTH;
        } else if (Constants.TOP.equals(side)) {
            orientation = SlideBarDataModel.NORTH;
        }
        ((SlideBarDataModel)dataModel).setOrientation(orientation);
    }

    @Override
    public final synchronized void addActionListener(ActionListener listener) {
        if (actionListeners == null) {
            actionListeners = new ArrayList<ActionListener>();
        }
        actionListeners.add(listener);
    }

    /**
     * Remove an action listener.
     *
     * @param listener The listener to remove.
     */
    @Override
    public final synchronized void removeActionListener(ActionListener listener) {
        if (actionListeners != null) {
            actionListeners.remove(listener);
            if (actionListeners.isEmpty()) {
                actionListeners = null;
            }
        }
    }

    final void postActionEvent(ActionEvent event) {
        List<ActionListener> list;
        synchronized (this) {
            if (actionListeners == null)
                return;
            list = Collections.unmodifiableList(actionListeners);
        }
        for (int i = 0; i < list.size(); i++) {
            list.get(i).actionPerformed(event);
        }
    }
    
    @Override
    public void addChangeListener(ChangeListener listener) {
        cs.addChangeListener(listener);
    }    
    
    @Override
    public void removeChangeListener(ChangeListener listener) {
        cs.removeChangeListener(listener);
    }
    
    final void postSelectionEvent() {
        cs.fireChange();
    }
    
    public void addPropertyChangeListener(String name, PropertyChangeListener listener) {
        slideBar.addPropertyChangeListener(name, listener);
    }
    
    public void removePropertyChangeListener(String name, PropertyChangeListener listener) {
        slideBar.removePropertyChangeListener(name, listener);
    }
    
    @Override
    public void addTopComponent(String name, Icon icon, TopComponent tc, String toolTip) {
        dataModel.addTab(dataModel.size(), new TabData(tc, icon, name, toolTip));
    }
    
    @Override
    public TopComponent getSelectedTopComponent() {
        int index = selModel.getSelectedIndex();
        return index < 0 ? null : (TopComponent)dataModel.getTab(index).getComponent();
    }
    
    @Override
    public TopComponent getTopComponentAt(int index) {
        return (TopComponent)dataModel.getTab(index).getComponent();
    }
    
    @Override
    public TopComponent[] getTopComponents() {
        int size = dataModel.size();
        TopComponent[] result = new TopComponent[size];
        for (int i=0; i < size; i++) {
            result[i] = (TopComponent) dataModel.getTab(i).getComponent();
        }
        return result;
    }
    
    @Override
    public void setActive(boolean active) {
        slideBar.setActive(active);
    }
    
    @Override
    public void setIconAt(int index, Icon icon) {
        dataModel.setIcon(index, icon);
    }
    
    @Override
    public void setTitleAt(int index, String title) {
        dataModel.setText(index, title);
    }
    
    @Override
    public void setToolTipTextAt(int index, String toolTip) {
        // XXX - not supported yet
    }
    
    @Override
    public void setTopComponents(TopComponent[] tcs, TopComponent selected) {
        TabData[] data = new TabData[tcs.length];
        int toSelect=-1;
        for(int i = 0; i < tcs.length; i++) {
            TopComponent tc = tcs[i];
            Image icon = tc.getIcon();
            String displayName = WindowManagerImpl.getInstance().getTopComponentDisplayName(tc);
            data[i] = new TabData(
                tc,
                icon == null ? null : ImageUtilities.image2Icon(icon),
                displayName == null ? "" : displayName, // NOI18N
                tc.getToolTipText());
            if (selected == tcs[i]) {
                toSelect = i;
            }
        }

        dataModel.setTabs(data);
        setSelectedComponent(selected);
    }
    
    @Override
    public int getTabCount() {
        return dataModel.size();
    }    
    
    @Override
    public int indexOf(Component tc) {
        int size = dataModel.size();
        for (int i=0; i < size; i++) {
            if (tc == dataModel.getTab(i).getComponent()) return i;
        }
        return -1;
    }
    
    @Override
    public void insertComponent(String name, Icon icon, Component comp, String toolTip, int position) {
        dataModel.addTab(position, new TabData(comp, icon, name, toolTip));
    }
    
    @Override
    public void removeComponent(Component comp) {
        int i = indexOf(comp);
        dataModel.removeTab(i);
    }
    
    @Override
    public void setSelectedComponent(Component comp) {
        int newIndex = indexOf(comp);
        if (selModel.getSelectedIndex() != newIndex) {
            selModel.setSelectedIndex(newIndex);
        }
        if (comp instanceof TopComponent) {
            //Inelegant to do this here, but it guarantees blinking stops
            TopComponent tc = (TopComponent) comp;
            tc.cancelRequestAttention();
        }
    }
    
    @Override
    public int tabForCoordinate(Point p) {
        return slideBar.tabForCoordinate(p.x, p.y);
    }
    
    @Override
    public Component getComponent() {
        return slideBar;
    }

/*************** No DnD support yet **************/
    
    @Override
    public Object getConstraintForLocation(Point location, boolean attachingPossible) {
        int tab = slideBar.nextTabForCoordinate(location.x, location.y);
        return Integer.valueOf(tab);
    }
    
    @Override
    public Shape getIndicationForLocation(Point location, TopComponent startingTransfer, 
                            Point startingPoint, boolean attachingPossible) {
        
//        int tab = tabForCoordinate(location);
        int nextTab = slideBar.nextTabForCoordinate(location.x, location.y);
        SlideBarDataModel sbdm = (SlideBarDataModel)dataModel;
        if (getTabCount() != 0) {
            if (nextTab == 0) {
                Rectangle rect = getTabBounds(0);
                if (isHorizontal()) {
                    rect.x = 0;
                    rect.width = rect.width / 2;
                } else {
                    rect.y = 0;
                    rect.height = rect.height / 2;
                }
                return rect;
            } else if (nextTab < getTabCount()) {
                Rectangle rect1 = getTabBounds(nextTab - 1);
                Rectangle rect2 = getTabBounds(nextTab);
                Rectangle result = new Rectangle();
                if (isHorizontal()) {
                    result.y = rect1.y;
                    result.height = rect1.height;
                    result.x = rect1.x + (rect1.width / 2);
                    result.width = rect2.x + (rect2.width / 2) - result.x;
                } else {
                    result.x = rect1.x;
                    result.width = rect1.width;
                    result.y = rect1.y + (rect1.height / 2);
                    result.height = rect2.y + (rect2.height / 2) - result.y;
                }
                return result;
            } else if (nextTab == getTabCount()) {
                Rectangle rect = getTabBounds(getTabCount() - 1);
                if (isHorizontal()) {
                    rect.x = rect.x + rect.width;
                } else {
                    rect.y = rect.y + rect.height;
                }
                return rect;
            }
        } 
        if (isHorizontal()) {
            return new Rectangle(10, 0, 50, 20);
        }
        return new Rectangle(0, 10, 20, 50);
    }
    
    @Override
    public Image createImageOfTab(int tabIndex) {
        TabData dt = slideBar.getModel().getTab(tabIndex);
        if (dt.getComponent() instanceof TopComponent) {
            DefaultTabDataModel tempModel = new DefaultTabDataModel( new TabData[] { dt } );
            TabbedContainer temp = new TabbedContainer( tempModel, TabbedContainer.TYPE_VIEW );
            temp.setSize( 300,300 );
            
            return temp.createImageOfTab(0);
        }
        
        return null;
    }
    
    public DragAndDropFeedbackVisualizer getDragAndDropFeedbackVisualizer( int tabIndex ) {
        slideBar.getSelectionModel().setSelectedIndex(tabIndex);
        return new DragAndDropFeedbackVisualizer( this, tabIndex );
//        TabData dt = slideBar.getModel().getTab(tabIndex);
//        if (dt.getComponent() instanceof TopComponent) {
//            DefaultTabDataModel tempModel = new DefaultTabDataModel( new TabData[] { dt } );
//            TabbedContainer temp = new TabbedContainer( tempModel, TabbedContainer.TYPE_VIEW );
//JWindow w = new JWindow();
//w.setBounds(-2000, -2000, 300, 300);
//w.getContentPane().add( temp );     
//w.setVisible(true);       
////temp.setSize( 300,300 );
////temp.setLocation( -10000,-10000);
////temp.setVisible(true);
////temp.invalidate();
////temp.revalidate();
////temp.repaint();
////            temp.updateUI();
////temp.getSelectionModel().setSelectedIndex(0);
//            
//    Window drag = temp.createDragWindow(0);
////    w.dispose();
//return drag;
//        }
//        
//        return null;
    }
    
    /** Add action for disabling slide */
    @Override
    public Action[] getPopupActions(Action[] defaultActions, int tabIndex) {
        boolean isMDI = WindowManagerImpl.getInstance().getEditorAreaState() == Constants.EDITOR_AREA_JOINED;
        Action[] result = new Action[defaultActions.length + (isMDI ? 1 : 0)];
        System.arraycopy(defaultActions, 0, result, 0, defaultActions.length);
        if (isMDI) {
            result[defaultActions.length] =
                new ActionUtils.ToggleWindowTransparencyAction(slideBar,
                    tabIndex,
                    slideBar.isSlidedTabTransparent()
                        && tabIndex == slideBar.getSelectionModel().getSelectedIndex());
        }
        return result;
    }
    
    @Override
    public Rectangle getTabBounds(int tabIndex) {
        return slideBar.getTabBounds(tabIndex);
    }

    @Override
    public boolean isTransparent() {
        return false;
    }

    @Override
    public void setTransparent(boolean transparent) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Rectangle getTabsArea() {
        Rectangle res = slideBar.getBounds();
        res.setLocation( 0, 0 );
        return res;
    }
    
    final ModeImpl getSlidingMode() {
        return slidingMode;
    }
    
    private ModeImpl findSlidingMode() {
        String modeName;
        switch( ((SlideBarDataModel)dataModel).getOrientation() ) {
            case SlideBarDataModel.EAST:
                modeName = "rightSlidingSide"; //NOI18N
                break;
            case SlideBarDataModel.SOUTH:
                modeName = "bottomSlidingSide"; //NOI18N
                break;
            case SlideBarDataModel.NORTH:
                modeName = "topSlidingSide"; //NOI18N
                break;
            case SlideBarDataModel.WEST:
            default:
                modeName = "leftSlidingSide"; //NOI18N
        }
        return ( ModeImpl ) WindowManagerImpl.getInstance().findMode( modeName );
    }

    
    final boolean isHorizontal() {
        return ((SlideBarDataModel)dataModel).getOrientation() == SlideBarDataModel.SOUTH 
                || ((SlideBarDataModel)dataModel).getOrientation() == SlideBarDataModel.NORTH;
    }
}

