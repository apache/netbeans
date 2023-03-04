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
 * TabDisplayerUI.java
 *
 * Created on March 16, 2004, 5:55 PM
 */

package org.netbeans.swing.tabcontrol;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Map;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.SingleSelectionModel;
import javax.swing.UIManager;
import javax.swing.plaf.ComponentUI;
import org.netbeans.swing.tabcontrol.event.TabActionEvent;
import org.netbeans.swing.tabcontrol.plaf.TabControlButton;
import org.netbeans.swing.tabcontrol.plaf.TabControlButtonFactory;
import org.openide.windows.TopComponent;

/**
 * The basic UI of a tab displayer component.  Defines the API of the UI for
 * TabDisplayers, which may be called by TabDisplayer.
 *
 * @author Tim Boudreau
 * @see org.netbeans.swing.tabcontrol.plaf.AbstractTabDisplayerUI
 * @see org.netbeans.swing.tabcontrol.plaf.BasicTabDisplayerUI
 */
public abstract class TabDisplayerUI extends ComponentUI {
    protected SingleSelectionModel selectionModel = null;
    protected final TabDisplayer displayer;

    /**
     * Creates a new instance of TabDisplayerUI
     */
    protected TabDisplayerUI(TabDisplayer displayer) {
        this.displayer = displayer;
    }


    @Override
    public void installUI(JComponent c) {
        assert c == displayer;
        selectionModel = displayer.getSelectionModel();
        
        //Will only be non-null if we are in the middle of an L&F change - don't
        //replace it so listeners are not clobbered
        if (selectionModel == null) {
            selectionModel = createSelectionModel();
        }
        
        installSelectionModel();
    }

    @Override
    public void uninstallUI(JComponent c) {
        assert c == displayer;
    }
    
    protected Font getTxtFont() {
        Font result = UIManager.getFont("TabbedPane.font");
        if (result != null) {
            return result;
        }
        result = UIManager.getFont("controlFont");
        return result;
    }
    
    /**
     * Get a shape representing the exact outline of the numbered tab. The
     * implementations in the package will return instances of
     * <code>EqualPolygon</code> from this method; other implementations may
     * return what they want, but for performance reasons, it is highly
     * desirable that the shape object returned honor <code>equals()</code> and
     * <code>hashCode()</code>, as there are significant optimizations in
     * NetBeans' drag and drop support that depend on this.
     */
    public abstract Polygon getExactTabIndication(int index);

    /**
     * Get a shape representing the area of visual feedback during a drag and
     * drop operation, which represents where a tab will be inserted if a drop
     * operation is performed over the indicated tab. <p>The implementations in
     * the package will return instances of <code>EqualPolygon</code> from this
     * method; other implementations may return what they want, but for
     * performance reasons, it is highly desirable that the shape object
     * returned honor <code>equals()</code> and <code>hashCode()</code>, as
     * there are significant optimizations in NetBeans' drag and drop support
     * that depened on this.
     *
     * @return Shape representing feedback shape
     */
    public abstract Polygon getInsertTabIndication(int index);

    /**
     * Returns the index of the tab at the passed point, or -1 if no tab is at
     * that location. Note that this method may return -1 for coordinates which
     * are within a tab as returned by getTabRect(), but are not within the
     * visible shape of the tab as the UI paints it.
     */
    public abstract int tabForCoordinate(Point p);

    /**
     * Configure the passed rectangle with the shape of the tab at the given
     * index.
     */
    public abstract Rectangle getTabRect(int index,
                                         final Rectangle destination);

    /**
     * Returns an image suitable for use in drag and drop operations,
     * representing the tab at this index.  The default implementation returns null.
     *
     * @param index A tab index
     * @throws IllegalArgumentException if no tab is at the passed index
     */
    public Image createImageOfTab(int index) {
        return null;
    }

    /**
     * Create the selection model which will handle selection for the
     * TabDisplayer.  SPI method located here because TabDisplayer.setSelectionModel
     * is package private.
     */
    protected abstract SingleSelectionModel createSelectionModel();

    /**
     * Allows ActionListeners attached to the container to determine if the
     * event should be acted on. Delegates to <code>displayer.postActionEvent()</code>.
     * This method will create a TabActionEvent with the passed string as an 
     * action command, and cause the displayer to fire this event.  It will
     * return true if no listener on the displayer consumed the TabActionEvent;
     * consuming the event is the way a listener can veto a change, or provide
     * special handling for it.
     *
     * @param command The action command - this should be TabDisplayer.COMMAND_SELECT
     *                or TabDisplayer.COMMAND_CLOSE, but private contracts
     *                between custom UIs and components are also an option.
     * @param tab     The index of the tab upon which the action should act, or
     *                -1 if non-applicable
     * @param event   A mouse event which initiated the action, or null
     * @return true if the event posted was not consumed by any listener
     */
    protected final boolean shouldPerformAction(String command, int tab,
                                                MouseEvent event) {
        TabActionEvent evt = new TabActionEvent(displayer, command, tab, event);
        displayer.postActionEvent(evt);
        return !evt.isConsumed();
    }
    
    /**
     * Allows ActionListeners attached to the container to determine if the
     * event should be acted on. Delegates to <code>displayer.postActionEvent()</code>.
     * This method will create a TabActionEvent with the passed string as an 
     * action command, and cause the displayer to fire this event.  It will
     * return true if no listener on the displayer consumed the TabActionEvent;
     * consuming the event is the way a listener can veto a change, or provide
     * special handling for it.
     *
     * @param e The original tab action event.
     * @return true if the event posted was not consumed by any listener
     * @since 1.27
     */
    protected final boolean shouldPerformAction(TabActionEvent e) {
        TabActionEvent evt = new TabActionEvent(displayer, e.getActionCommand(), e.getTabIndex(), e.getMouseEvent());
        evt.setGroupName( e.getGroupName() );
        displayer.postActionEvent(evt);
        return !evt.isConsumed();
    }

    /**
     * Instruct the UI to ensure that the tab at the given index is visible.
     * Some UIs allow scrolling or otherwise hiding tabs.  The default
     * implementation is a no-op.
     *
     * @param index The index of the tab that should be made visible, which
     *              should be within the range of 0 to the count of tabs in the
     *              model
     */
    public void makeTabVisible(int index) {
        //do nothing
    }

    /**
     * Check if the given tab is busy and should be painted in a special way.
     * @param tabIndex
     * @return True if given tab is 'busy', false otherwise.
     * @since 1.34
     */
    public final boolean isTabBusy( int tabIndex ) {
        WinsysInfoForTabbedContainer winsysInfo = displayer.getContainerWinsysInfo();
        if( null == winsysInfo )
            return false;
        TabDataModel model = displayer.getModel();
        if( tabIndex < 0 || tabIndex >= model.size() )
            return false;
        TabData td = model.getTab( tabIndex );
        if( td.getComponent() instanceof TopComponent ) {
            return winsysInfo.isTopComponentBusy( (TopComponent)td.getComponent() );
        }
        return false;
    }

    /**
     * Installs the selection model into the tab control via a package private
     * method.
     */
    private void installSelectionModel() {
        displayer.setSelectionModel(selectionModel);
    }

    /**
     *  The index a tab would acquire if dropped at a given point
     *
     * @param p A point
     * @return An index which may be equal to the size of the data model
     */
    public abstract int dropIndexOfPoint (Point p);
    
    public abstract void registerShortcuts (JComponent comp);
        
    public abstract void unregisterShortcuts (JComponent comp);
    
    
    protected abstract void requestAttention (int tab);
    
    protected abstract void cancelRequestAttention (int tab);

    /**
     * Turn tab highlight on/off
     * @param tab
     * @since 1.38
     */
    protected void setAttentionHighlight (int tab, boolean highlight) {
    }

    /**
     * @since 1.9
     * @return An icon for various buttons displayed in tab control (close/pin/scroll left/right etc), see TabControlButton class.
     */
    public Icon getButtonIcon( int buttonId, int buttonState ) {
        Icon res = null;
        initIcons();
        String[] paths = buttonIconPaths.get( buttonId );
        if( null != paths && buttonState >=0 && buttonState < paths.length ) {
            res = TabControlButtonFactory.getIcon( paths[buttonState] );
        }
        return res;
    }
    
    public void postTabAction( TabActionEvent e ) {
        if( shouldPerformAction( e ) ) {
            
            //TODO do something here??
        }
    }

    /**
     * @return Insets denoting active autoscroll areas.
     * @since 1.48
     */
    public Insets getAutoscrollInsets() {
        return new Insets(0,0,0,0);
    }

    /**
     * Scroll the tabs when something is dragged over the TabDisplayer.
     * @param location Mouse location in TabDisplayer's coordinates.
     * @since 1.48
     */
    public void autoscroll( Point location ) {
    }
    
    private static Map<Integer, String[]> buttonIconPaths;
    private static void initIcons() {
        if( null == buttonIconPaths ) {
            buttonIconPaths = new HashMap<Integer, String[]>(10);
            
            //close button
            String[] iconPaths = new String[4];
            iconPaths[TabControlButton.STATE_DEFAULT] = "org/openide/awt/resources/metal_bigclose_enabled.png"; // NOI18N
            iconPaths[TabControlButton.STATE_PRESSED] = "org/openide/awt/resources/metal_bigclose_pressed.png"; // NOI18N
            iconPaths[TabControlButton.STATE_DISABLED] = iconPaths[TabControlButton.STATE_DEFAULT];
            iconPaths[TabControlButton.STATE_ROLLOVER] = "org/openide/awt/resources/metal_bigclose_rollover.png"; // NOI18N
            buttonIconPaths.put( TabControlButton.ID_CLOSE_BUTTON, iconPaths );
            
            //slide/pin button
            iconPaths = new String[4];
            iconPaths[TabControlButton.STATE_DEFAULT] = "org/netbeans/swing/tabcontrol/resources/metal_slideright_enabled.png"; // NOI18N
            iconPaths[TabControlButton.STATE_PRESSED] = "org/netbeans/swing/tabcontrol/resources/metal_slideright_pressed.png"; // NOI18N
            iconPaths[TabControlButton.STATE_DISABLED] = iconPaths[TabControlButton.STATE_DEFAULT];
            iconPaths[TabControlButton.STATE_ROLLOVER] = "org/netbeans/swing/tabcontrol/resources/metal_slideright_rollover.png"; // NOI18N
            buttonIconPaths.put( TabControlButton.ID_SLIDE_RIGHT_BUTTON, iconPaths );
            
            iconPaths = new String[4];
            iconPaths[TabControlButton.STATE_DEFAULT] = "org/netbeans/swing/tabcontrol/resources/metal_slideleft_enabled.png"; // NOI18N
            iconPaths[TabControlButton.STATE_PRESSED] = "org/netbeans/swing/tabcontrol/resources/metal_slideleft_pressed.png"; // NOI18N
            iconPaths[TabControlButton.STATE_DISABLED] = iconPaths[TabControlButton.STATE_DEFAULT];
            iconPaths[TabControlButton.STATE_ROLLOVER] = "org/netbeans/swing/tabcontrol/resources/metal_slideleft_rollover.png"; // NOI18N
            buttonIconPaths.put( TabControlButton.ID_SLIDE_LEFT_BUTTON, iconPaths );
            
            iconPaths = new String[4];
            iconPaths[TabControlButton.STATE_DEFAULT] = "org/netbeans/swing/tabcontrol/resources/metal_slidebottom_enabled.png"; // NOI18N
            iconPaths[TabControlButton.STATE_PRESSED] = "org/netbeans/swing/tabcontrol/resources/metal_slidebottom_pressed.png"; // NOI18N
            iconPaths[TabControlButton.STATE_DISABLED] = iconPaths[TabControlButton.STATE_DEFAULT];
            iconPaths[TabControlButton.STATE_ROLLOVER] = "org/netbeans/swing/tabcontrol/resources/metal_slidebottom_rollover.png"; // NOI18N
            buttonIconPaths.put( TabControlButton.ID_SLIDE_DOWN_BUTTON, iconPaths );
            
            iconPaths = new String[4];
            iconPaths[TabControlButton.STATE_DEFAULT] = "org/netbeans/swing/tabcontrol/resources/metal_pin_enabled.png"; // NOI18N
            iconPaths[TabControlButton.STATE_PRESSED] = "org/netbeans/swing/tabcontrol/resources/metal_pin_pressed.png"; // NOI18N
            iconPaths[TabControlButton.STATE_DISABLED] = iconPaths[TabControlButton.STATE_DEFAULT];
            iconPaths[TabControlButton.STATE_ROLLOVER] = "org/netbeans/swing/tabcontrol/resources/metal_pin_rollover.png"; // NOI18N
            buttonIconPaths.put( TabControlButton.ID_PIN_BUTTON, iconPaths );
            
            //left button
            iconPaths = new String[4];
            iconPaths[TabControlButton.STATE_DEFAULT] = "org/netbeans/swing/tabcontrol/resources/metal_scrollleft_enabled.png"; // NOI18N
            iconPaths[TabControlButton.STATE_DISABLED] = "org/netbeans/swing/tabcontrol/resources/metal_scrollleft_disabled.png"; // NOI18N
            iconPaths[TabControlButton.STATE_ROLLOVER] = "org/netbeans/swing/tabcontrol/resources/metal_scrollleft_rollover.png"; // NOI18N
            iconPaths[TabControlButton.STATE_PRESSED] = "org/netbeans/swing/tabcontrol/resources/metal_scrollleft_pressed.png"; // NOI18N
            buttonIconPaths.put( TabControlButton.ID_SCROLL_LEFT_BUTTON, iconPaths );
            
            //right button
            iconPaths = new String[4];
            iconPaths[TabControlButton.STATE_DEFAULT] = "org/netbeans/swing/tabcontrol/resources/metal_scrollright_enabled.png"; // NOI18N
            iconPaths[TabControlButton.STATE_DISABLED] = "org/netbeans/swing/tabcontrol/resources/metal_scrollright_disabled.png"; // NOI18N
            iconPaths[TabControlButton.STATE_ROLLOVER] = "org/netbeans/swing/tabcontrol/resources/metal_scrollright_rollover.png"; // NOI18N
            iconPaths[TabControlButton.STATE_PRESSED] = "org/netbeans/swing/tabcontrol/resources/metal_scrollright_pressed.png"; // NOI18N
            buttonIconPaths.put( TabControlButton.ID_SCROLL_RIGHT_BUTTON, iconPaths );
            
            //drop down button
            iconPaths = new String[4];
            iconPaths[TabControlButton.STATE_DEFAULT] = "org/netbeans/swing/tabcontrol/resources/metal_popup_enabled.png"; // NOI18N
            iconPaths[TabControlButton.STATE_DISABLED] = "org/netbeans/swing/tabcontrol/resources/metal_popup_disabled.png"; // NOI18N
            iconPaths[TabControlButton.STATE_ROLLOVER] = "org/netbeans/swing/tabcontrol/resources/metal_popup_rollover.png"; // NOI18N
            iconPaths[TabControlButton.STATE_PRESSED] = "org/netbeans/swing/tabcontrol/resources/metal_popup_pressed.png"; // NOI18N
            buttonIconPaths.put( TabControlButton.ID_DROP_DOWN_BUTTON, iconPaths );
            
            //maximize/restore button
            iconPaths = new String[4];
            iconPaths[TabControlButton.STATE_DEFAULT] = "org/netbeans/swing/tabcontrol/resources/metal_maximize_enabled.png"; // NOI18N
            iconPaths[TabControlButton.STATE_DISABLED] = "org/netbeans/swing/tabcontrol/resources/metal_maximize_disabled.png"; // NOI18N
            iconPaths[TabControlButton.STATE_ROLLOVER] = "org/netbeans/swing/tabcontrol/resources/metal_maximize_rollover.png"; // NOI18N
            iconPaths[TabControlButton.STATE_PRESSED] = "org/netbeans/swing/tabcontrol/resources/metal_maximize_pressed.png"; // NOI18N
            buttonIconPaths.put( TabControlButton.ID_MAXIMIZE_BUTTON, iconPaths );
            
            iconPaths = new String[4];
            iconPaths[TabControlButton.STATE_DEFAULT] = "org/netbeans/swing/tabcontrol/resources/metal_restore_enabled.png"; // NOI18N
            iconPaths[TabControlButton.STATE_DISABLED] = "org/netbeans/swing/tabcontrol/resources/metal_restore_disabled.png"; // NOI18N
            iconPaths[TabControlButton.STATE_ROLLOVER] = "org/netbeans/swing/tabcontrol/resources/metal_restore_rollover.png"; // NOI18N
            iconPaths[TabControlButton.STATE_PRESSED] = "org/netbeans/swing/tabcontrol/resources/metal_restore_pressed.png"; // NOI18N
            buttonIconPaths.put( TabControlButton.ID_RESTORE_BUTTON, iconPaths );
        }
    }
    
}
