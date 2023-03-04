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

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.Border;
import org.netbeans.core.windows.Constants;
import org.netbeans.swing.tabcontrol.SlideBarDataModel;
import org.netbeans.swing.tabcontrol.SlidingButton;
import org.netbeans.swing.tabcontrol.TabData;
import org.netbeans.swing.tabcontrol.TabbedContainer;
import org.netbeans.swing.tabcontrol.customtabs.Tabbed;
import org.netbeans.swing.tabcontrol.customtabs.TabbedComponentFactory;
import org.netbeans.swing.tabcontrol.customtabs.TabbedType;
import org.netbeans.swing.tabcontrol.event.TabActionEvent;
import org.openide.util.Lookup;
import org.openide.util.Utilities;
import org.openide.windows.TopComponent;

/*
 * Helper class to manage slide operations of asociated slide bar.
 * Handles sliding logic that assures
 * just one component is slided at the time.
 * Uses TabbedContainer to represent and display slide component.
 *
 * @author Dafe Simonek
 */
final class CommandManager implements ActionListener {
   
    /** Asociated slide bar */
    private final SlideBar slideBar;
    /** Local tabbed container used to display slided component */
    private Tabbed slidingTabbed;

    /** Data of slide operation in progress */
    private Component curSlidedComp;
    private SlidingButton curSlideButton;
    private int curSlideOrientation;
    private int curSlidedIndex;
    private ResizeGestureRecognizer recog;
    
    
    public CommandManager(SlideBar slideBar) {
        this.slideBar = slideBar;
        recog = new ResizeGestureRecognizer(this);
    }
   
    ResizeGestureRecognizer getResizer() {
        return recog;
    }
    
    public void slideResize(int delta) {
        if (!isCompSlided()) {
            return;
        }
        SlideOperation op = SlideOperationFactory.createSlideResize(getSlidingTabbed().getComponent(), curSlideOrientation);
        Rectangle finish = getSlidingTabbed().getComponent().getBounds(null);
        String side = orientation2Side(curSlideOrientation);
        if (Constants.BOTTOM.equals(side)) {
            finish.height = finish.height - delta;
            finish.y = finish.y + delta;
        }
        if (Constants.RIGHT.equals(side)) {
            finish.width = finish.width - delta;
            finish.x = finish.x + delta;
        }
        if (Constants.LEFT.equals(side)) {
            finish.width = finish.width + delta;
        }
        if (Constants.TOP.equals(side)) {
            finish.height = finish.height + delta;
        }
        op.setFinishBounds(finish);
        postEvent(new SlideBarActionEvent(slideBar, SlideBar.COMMAND_SLIDE_RESIZE, op));
        
    }
    
    public void slideIn(int tabIndex) {
        SlideBarDataModel model = slideBar.getModel();
        if (isCompSlided()) {
            if (curSlidedComp != model.getTab(tabIndex).getComponent()) {
                // another component requests slide in, so slide out current first
                slideOut(false, false);
            }
        }
        
        curSlidedIndex = tabIndex;
        curSlidedComp = model.getTab(tabIndex).getComponent();
        curSlideOrientation = model.getOrientation();
        curSlideButton = slideBar.getButton(tabIndex);
        Tabbed cont = updateSlidedTabContainer(tabIndex);
        SlideOperation operation = SlideOperationFactory.createSlideIn(
            cont.getComponent(), curSlideOrientation, true, true);

        boolean alreadyListening = false;
        for( AWTEventListener el : Toolkit.getDefaultToolkit().getAWTEventListeners() ) {
            if( el == getAWTListener() ) {
                alreadyListening = false;
                break;
            }
        }
        if( !alreadyListening )
            Toolkit.getDefaultToolkit().addAWTEventListener( getAWTListener(), MouseEvent.MOUSE_EVENT_MASK);
        
        curSlideButton.setSelected(true);

        postEvent(new SlideBarActionEvent(slideBar, SlideBar.COMMAND_SLIDE_IN, operation));
        recog.attachResizeRecognizer(orientation2Side(curSlideOrientation), cont.getComponent());
    }
    
    /** Fires slide out operation. 
     * @param requestsActivation true means restore focus to some other view after
     * slide out, false means no additional reactivation
     */
    public void slideOut(boolean requestsActivation, boolean useEffect) {
        if (!isCompSlided()) {
            return;
        }
        
        SlideOperation operation = SlideOperationFactory.createSlideOut(
            getSlidingTabbed().getComponent(), curSlideOrientation, useEffect, requestsActivation);
        
        Toolkit.getDefaultToolkit().removeAWTEventListener( getAWTListener() );

        curSlideButton.setSelected(false);
        
        recog.detachResizeRecognizer(orientation2Side(curSlideOrientation), getSlidingTabbed().getComponent());
        
        curSlidedComp = null;
        curSlideButton = null;
        curSlideOrientation = -1;
        curSlidedIndex = -1;

        postEvent(new SlideBarActionEvent(slideBar, SlideBar.COMMAND_SLIDE_OUT, operation));
    }
    
    
    public void slideIntoDesktop(int tabIndex, boolean useEffect) {
        SlideOperation operation = null;
        if (isCompSlided()) {
            operation = SlideOperationFactory.createSlideIntoDesktop(
                getSlidingTabbed().getComponent(), curSlideOrientation, useEffect);
        }
        recog.detachResizeRecognizer(orientation2Side(curSlideOrientation), getSlidingTabbed().getComponent());
        postEvent(new SlideBarActionEvent(slideBar, SlideBar.COMMAND_DISABLE_AUTO_HIDE, operation, null, tabIndex));
    }
    
    public void toggleTransparency( int tabIndex ) {
        //do nothing, the TabbedContainer will take care of the transparency
    }
    
    public void showPopup(MouseEvent mouseEvent, int tabIndex) {
        postEvent(new SlideBarActionEvent(slideBar, SlideBar.COMMAND_POPUP_REQUEST, mouseEvent, tabIndex));
    }
    
    protected static String orientation2Side (int orientation) {
        String side = Constants.LEFT; 
        if (orientation == SlideBarDataModel.WEST) {
            side = Constants.LEFT;
        } else if (orientation == SlideBarDataModel.EAST) {
            side = Constants.RIGHT;
        } else if (orientation == SlideBarDataModel.SOUTH) {
            side = Constants.BOTTOM;
        } else if (orientation == SlideBarDataModel.NORTH) {
            side = Constants.TOP;
        }
        return side;
    }
    
    
    /** Activates or deactivates asociated tabbed container used as
     * sliding component.
     */
    public void setActive(boolean active) {
        getSlidingTabbed().setActive(active);
    }
    
    /********* implementation of ActionListener **************/
    
    /** Reacts to actions from currently slided tabbed container, forwards
     * received events to tabbed instance, which ensures that 
     * actions are handled in the same way as usual.
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        if (TabbedContainer.COMMAND_POPUP_REQUEST.equals(e.getActionCommand())) {
            TabActionEvent tae = (TabActionEvent) e;
            if (curSlidedComp instanceof TopComponent) {
                TopComponent tc = (TopComponent)curSlidedComp;
                Action[] actions = slideBar.getTabbed().getPopupActions(tc.getActions(), curSlidedIndex);
                if (actions == null) {
                    actions = tc.getActions();
                }
                if (actions == null || actions.length == 0 )
                    return;
                
                showPopupMenu(
                    Utilities.actionsToPopup(actions, tc.getLookup()), tae.getMouseEvent().getPoint(), tae.getMouseEvent().getComponent());
                
            }
        } else if (TabbedContainer.COMMAND_DISABLE_AUTO_HIDE.equals(e.getActionCommand())) {
            if( curSlidedIndex >= 0 )
                slideIntoDesktop(curSlidedIndex, true);
        } else if (TabbedContainer.COMMAND_ENABLE_AUTO_HIDE.equals(e.getActionCommand())) {
            slideBar.getSelectionModel().setSelectedIndex(-1);
        } else if (TabbedContainer.COMMAND_TOGGLE_TRANSPARENCY.equals(e.getActionCommand())) {
            TabActionEvent tae = (TabActionEvent) e;
            toggleTransparency( tae.getTabIndex() );
        } else if (TabbedContainer.COMMAND_MAXIMIZE.equals(e.getActionCommand())) {
            //inform the window system that the slided window changes its maximized status
            postEvent(new SlideBarActionEvent(slideBar, SlideBar.COMMAND_MAXIMIZE, null, null, curSlidedIndex));
        } else {
            // convert event - fix index, local tabbed container index isn't right in slide bar context
            TabActionEvent tae = (TabActionEvent)e;
            if( TabbedContainer.COMMAND_CLOSE.equals(tae.getActionCommand()) && curSlidedIndex < 0 ) {
                //#242321
                return;
            }
            TabActionEvent newEvt = new TabActionEvent(
                tae.getSource(), tae.getActionCommand(), curSlidedIndex, tae.getMouseEvent());
            
            postEvent(newEvt);
        }
    }
    
    /************************** non-public stuff **********************/

    private Rectangle getScreenCompRect(Component comp) { 
        Rectangle result = new Rectangle(comp.getLocationOnScreen(), comp.getSize());
        
        return result;
    }
    
     private static final boolean NO_POPUP_PLACEMENT_HACK = Boolean.getBoolean("netbeans.popup.no_hack"); // NOI18N
// ##########################     
// copied from TabbedHandler, maybe reuse..
//     

    /** Shows given popup on given coordinations and takes care about the
     * situation when menu can exceed screen limits */
    private static void showPopupMenu (JPopupMenu popup, Point p, Component comp) {
        if (NO_POPUP_PLACEMENT_HACK) {
            popup.show(comp, p.x, p.y);
            return;
        }

        SwingUtilities.convertPointToScreen (p, comp);
        Dimension popupSize = popup.getPreferredSize ();
        Rectangle screenBounds = Utilities.getUsableScreenBounds(comp.getGraphicsConfiguration());

        if (p.x + popupSize.width > screenBounds.x + screenBounds.width) {
            p.x = screenBounds.x + screenBounds.width - popupSize.width;
        }
        if (p.y + popupSize.height > screenBounds.y + screenBounds.height) {
            p.y = screenBounds.y + screenBounds.height - popupSize.height;
        }

        SwingUtilities.convertPointFromScreen (p, comp);
        popup.show(comp, p.x, p.y);
    }    

    
    private Tabbed getSlidingTabbed () {
        if (slidingTabbed == null) {
            TabbedComponentFactory factory = Lookup.getDefault().lookup( TabbedComponentFactory.class );
            slidingTabbed = factory.createTabbedComponent( TabbedType.VIEW, slideBar.createWinsysInfo() );
            slidingTabbed.addActionListener(this);
            Border b = null;
            String side = orientation2Side( slideBar.getModel().getOrientation() );
            b = UIManager.getBorder("floatingBorder-"+side); //NOI18N
            if( b == null )
                b = UIManager.getBorder("floatingBorder"); //NOI18N
            if (b != null && slidingTabbed.getComponent() instanceof JComponent ) {
                ((JComponent)slidingTabbed.getComponent()).setBorder (b);
            }

            if( slidingTabbed.getComponent() instanceof JComponent )
            registerEscHandler((JComponent)slidingTabbed.getComponent());
        }
        return slidingTabbed;
    }
    
    private Tabbed updateSlidedTabContainer(int tabIndex) {
        Tabbed container = getSlidingTabbed();
//        TabDataModel containerModel = container.getModel();
        SlideBarDataModel dataModel = slideBar.getModel();
        // creating new TabData instead of just referencing
        // to be able to compare and track changes between models of slide bar and 
        // slided tabbed container
        TabData origTab = dataModel.getTab(tabIndex);
        TopComponent tc = ( TopComponent ) origTab.getComponent();
        container.setTopComponents( new TopComponent[] { tc }, tc );
        return container;
    }
    
    private void registerEscHandler (JComponent comp) {
        comp.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "slideOut");
        comp.getActionMap().put("slideOut", escapeAction);
    }

    private AWTEventListener awtListener = null;
    private AWTEventListener getAWTListener() {
        if( null == awtListener ) {
            awtListener = new AWTEventListener() {
                @Override
                public void eventDispatched(AWTEvent event) {
                    if( event.getID() == MouseEvent.MOUSE_CLICKED && event.getSource() instanceof Component
                            && !(SwingUtilities.isDescendingFrom((Component)event.getSource(), getSlidingTabbed().getComponent())
                            || SwingUtilities.isDescendingFrom((Component)event.getSource(), slideBar)) ) {
                        //#159356 - make sure window slides out when clicked outside that window
                        TopComponent tc = slideBar.getTabbed().getSelectedTopComponent();
                        if( null != tc && TopComponent.getRegistry().getActivated() != tc )
                            slideBar.getSelectionModel().setSelectedIndex( -1 );
                    }
                }
            };
        }
        return awtListener;
    }
    

/***** dumping info about all registered Esc handlers, could be usable for
 * debugging
    
    private void dumpEscHandlers (JComponent comp) {
        InputMap map = null;
        JComponent curChild = null;
        Component[] children = comp.getComponents();
        for (int i = 0; i < children.length; i++) {
            if (children[i] instanceof JComponent) {
                curChild =(JComponent)children[i]; 
                dumpItem(curChild);
                dumpEscHandlers(curChild);
            }
        }
    }
    
    private void dumpItem(JComponent comp) {
        dumpInnerItem(comp.getInputMap(JComponent.WHEN_FOCUSED), comp.getActionMap(), comp);
        dumpInnerItem(comp.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT), comp.getActionMap(), comp);
        dumpInnerItem(comp.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW), comp.getActionMap(), comp);
    }
    
    private void dumpInnerItem(InputMap map, ActionMap actionMap, JComponent comp) {
        Object cmdKey = map.get(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0));
        if (cmdKey != null) {
            Action action = actionMap.get(cmdKey);
            if (action.isEnabled()) {
                System.out.println("Enabled command found:");
                System.out.println("component: " + comp);
                System.out.println("command key: " + cmdKey);
                System.out.println("action: " + action);
            } else {
                System.out.println("disabled command " + cmdKey);
            }
        }
    }
 
 **********/
    
    /** @return true if some component is currently slided, it means visible
     * over another components in desktop, false otherwise
     */
    boolean isCompSlided() {
        return curSlidedComp != null;
    }
    
    /* #return Component that is slided into desktop or null if no component is
     * slided currently.
     */
    Component getSlidedComp() {
        if (!isCompSlided()) {
            return null;
        }
        return slidingTabbed.getComponent();
    }

    /** Synchronizes its state with current state of data model. 
     * Removes currently slided component if it is no longer present in the model,
     * also keeps text up to date.
     */
    void syncWithModel() {
        if (curSlidedComp == null) {
            return; 
        }
        
        if (!slideBar.containsComp(curSlidedComp)) {
            // TBD - here should be closeSlide operation, which means
            // just remove from desktop
            slideOut(false, false);
        } else {
            // keep title text up to date
            SlideBarDataModel model = slideBar.getModel();
            // #46319 - during close, curSlidedIndex may become out of sync,
            // in which case do nothing
            if (curSlidedIndex < model.size()) {
                String freshText = model.getTab(curSlidedIndex).getText();
                getSlidingTabbed().setTitleAt( 0, freshText );
                slideBar.repaint();
                curSlideButton = slideBar.getButton(curSlidedIndex);
                curSlideButton.setSelected(true);
            }
        }
    }

    /** Actually performs sliding related event by sending it to the 
     * winsys through Tabbed instance
     */
    private void postEvent(ActionEvent evt) {
        ((TabbedSlideAdapter)slideBar.getTabbed()).postActionEvent(evt);
    }
    
    private final Action escapeAction = new EscapeAction();
    
    private final class EscapeAction extends javax.swing.AbstractAction {
        @Override
        public void actionPerformed(ActionEvent e) {
            slideBar.getSelectionModel().setSelectedIndex(-1);
        }
    } // end of EscapeAction
   
    
}
