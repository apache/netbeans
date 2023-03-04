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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.*;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListDataEvent;
import org.netbeans.core.windows.Constants;
import org.netbeans.core.windows.EditorOnlyDisplayer;
import org.netbeans.core.windows.ModeImpl;
import org.netbeans.core.windows.Switches;
import org.netbeans.core.windows.WindowManagerImpl;
import org.netbeans.core.windows.view.ui.tabcontrol.TabbedAdapter;
import org.netbeans.swing.tabcontrol.*;
import org.netbeans.swing.tabcontrol.customtabs.Tabbed;
import org.netbeans.swing.tabcontrol.event.ComplexListDataEvent;
import org.netbeans.swing.tabcontrol.event.ComplexListDataListener;
import org.netbeans.swing.tabcontrol.event.TabActionEvent;
import org.netbeans.swing.tabcontrol.plaf.BusyTabsSupport;
import org.netbeans.swing.tabcontrol.plaf.TabControlButton;
import org.netbeans.swing.tabcontrol.plaf.TabControlButtonFactory;
import org.openide.windows.Mode;
import org.openide.windows.TopComponent;

/*
 * Swing component of slide bar. 
 * Holds and shows set of toggle slide buttons and synchronizes them with 
 * data model.
 *
 * All data manipulation are done indirectly through ascoiated models,
 * Swing AWT hierarchy is just synchronized.
 *
 * @author Dafe Simonek
 */
public final class SlideBar extends JPanel implements ComplexListDataListener,
    SlideBarController, Tabbed.Accessor, ChangeListener, ActionListener {
    
    /** Command indicating request for slide in (appear) of sliding component */
    public static final String COMMAND_SLIDE_IN = "slideIn"; //NOI18N
    
    /** Command indicating request for slide out (hide) of sliding component */
    public static final String COMMAND_SLIDE_OUT = "slideOut"; //NOI18N

    public static final String COMMAND_SLIDE_RESIZE = "slideResize"; //NOI18N

    /** Action command indicating that a popup menu should be shown */
    public static final String COMMAND_POPUP_REQUEST = "popup"; //NOI18N

    /** Action command indicating that component is going from auto-hide state to regular */
    public static final String COMMAND_DISABLE_AUTO_HIDE = "disableAutoHide"; //NOI18N

    /** Action command indicating that component is going from regular to maximized size and vice versa */
    public static final String COMMAND_MAXIMIZE = "slideMaximize"; //NOI18N

    private static final boolean isAqua = "Aqua".equals(UIManager.getLookAndFeel().getID());

    /** Asociation with Tabbed implementation */
    private final TabbedSlideAdapter tabbed;
    /** Holds all data of slide bar */
    private final SlideBarDataModel dataModel;
    /** Selection info */
    private final SingleSelectionModel selModel;
    /** listener for mouse actions and moves, which trigger slide operations */
    private SlideGestureRecognizer gestureRecognizer;
    /** list of sliding buttons */
    private List<SlidingButton> buttons;
    /** operation handler */
    private CommandManager commandMgr;
    /** true when this slide bar is active in winsys, false otherwise */
    private boolean active = false;
    
    private final TabDisplayer dummyDisplayer = new TabDisplayer();
    
    private final int separatorOrientation;
    
    private int row = 0;
    private int col = 0;
    
    /** Creates a new instance of SlideBarContainer with specified orientation.
     * See SlideBarDataModel for possible orientation values.
     */
    public SlideBar(TabbedSlideAdapter tabbed, SlideBarDataModel dataModel, SingleSelectionModel selModel) {
        super(new GridBagLayout() );
        this.tabbed = tabbed;                
        this.dataModel = dataModel;
        this.selModel = selModel;
        commandMgr = new CommandManager(this);
        gestureRecognizer = new SlideGestureRecognizer(this, commandMgr.getResizer());
        buttons = new ArrayList<SlidingButton>(5);
        
        separatorOrientation = tabbed.isHorizontal() ? JSeparator.VERTICAL : JSeparator.HORIZONTAL;
        dummyDisplayer.addActionListener( this );
                
        syncWithModel();
        
        dataModel.addComplexListDataListener(this);
        selModel.addChangeListener(this);

        if( isAqua ) {
            Color bkColor = UIManager.getColor("NbSplitPane.background"); //NOI18N
            if( null == bkColor )
                bkColor = getBackground().darker();
            if( dataModel.getOrientation() == SlideBarDataModel.SOUTH ) {
                setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, bkColor));
            } else if( dataModel.getOrientation() == SlideBarDataModel.NORTH ) {
                setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, bkColor));
            } else if( dataModel.getOrientation() == SlideBarDataModel.WEST ) {
                setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, bkColor));
            } else if( dataModel.getOrientation() == SlideBarDataModel.EAST ) {
                setBorder(BorderFactory.createMatteBorder(0, 1, 0, 0, bkColor));
            }
        }
        if( UIManager.getBoolean( "NbMainWindow.showCustomBackground" ) ) //NOI18N
            setOpaque( false );
    }
    
    public SlideBarDataModel getModel() {
        return dataModel;
    }
    
    public SingleSelectionModel getSelectionModel () {
        return selModel;
    }
    
    /***** reactions to changes in data model, synchronizes AWT hierarchy and display ***/
    
    @Override
    public void intervalAdded(ListDataEvent e) {
        syncWithModel();
    }
    
    @Override
    public void intervalRemoved(ListDataEvent e) {
        syncWithModel();
    }
    
    @Override
    public void contentsChanged(ListDataEvent e) {
        syncWithModel();
    }
    
    @Override
    public void indicesAdded(ComplexListDataEvent e) {
        syncWithModel();
    }
    
    @Override
    public void indicesChanged(ComplexListDataEvent e) {
        syncWithModel();
    }
    
    @Override
    public void indicesRemoved(ComplexListDataEvent e) {
        syncWithModel();
    }

    /** Finds button which contains given point and returns button's index
     * valid in asociated dataModel. Or returns -1 if no button contains
     * given point
     */  
    public int tabForCoordinate(int x, int y) {
        Rectangle curBounds = new Rectangle();
        int index = 0;
        for (Iterator iter = buttons.iterator(); iter.hasNext(); index++) {
            ((Component)iter.next()).getBounds(curBounds);
            if (curBounds.contains(x, y)) {
                return index;
            }
        }
        return -1;
    }
    
    int nextTabForCoordinate(int x, int y) {
        Rectangle curBounds = new Rectangle();
        int index = 0;
        Iterator iter = buttons.iterator();
        while (iter.hasNext()) {
            Component comp = (Component)iter.next();
            comp.getBounds(curBounds);
            if (tabbed.isHorizontal()) {
                if (curBounds.x  + (curBounds.width/2) < x) {
                    index = index + 1;
                    continue;
                }
            } else {
                if (curBounds.y  + (curBounds.height/2) < y) {
                    index = index + 1;
                    continue;
                }
            }
            return index;
        }
        return index;
    }
    
    
    /** Implementation of ChangeListener, reacts to selection changes
     * and assures that currently selected component is slided in
     */
    @Override
    public void stateChanged(ChangeEvent e) {
        int selIndex = selModel.getSelectedIndex();
        
        if( selIndex >= 0 ) {
            EditorOnlyDisplayer.getInstance().cancel(false);
        }
        // notify winsys about selection change
        tabbed.postSelectionEvent();
        // a check to prevent NPE as described in #43605, dafe - is this correct or rather a hack? mkleint
        if (isDisplayable() && isVisible()) {
            // slide in or out
            if (selIndex != -1) {
                commandMgr.slideIn(selIndex);
            } else {
                commandMgr.slideOut(true, true);
            }
        }
    }


    /********** implementation of SlideBarController *****************/
    
    @Override
    public void userToggledAutoHide(int tabIndex, boolean enabled) {
        commandMgr.slideIntoDesktop(tabIndex, true);
    }
    
    @Override
    public void userToggledTransparency(int tabIndex) {
        if( tabIndex != getSelectionModel().getSelectedIndex() )
            getSelectionModel().setSelectedIndex( tabIndex );
        commandMgr.toggleTransparency( tabIndex );
    }
    
    @Override
    public void userTriggeredPopup(MouseEvent mouseEvent, Component clickedButton) {
        int index = getButtonIndex(clickedButton);
        commandMgr.showPopup(mouseEvent, index);
    }
    
    private SlidingButton buttonFor (TopComponent tc) {
        int idx = 0;
        for (Iterator i=dataModel.getTabs().iterator(); i.hasNext();) {
            TabData td = (TabData) i.next();
            if (td.getComponent() == tc) {
                break;
            }
            if (!i.hasNext()) {
                idx = -1;
            } else {
                idx++;
            }
        }
        if (idx >= 0 && idx < dataModel.size()) {
            return getButton(idx);
        } else {
            return null;
        }
    }
    
    public void setBlinking (TopComponent tc, boolean val) {
        SlidingButton button = buttonFor (tc);
        if (button != null) {
            button.setBlinking(val);
        }
    }

    /** Triggers slide operation by changing selected index */
    @Override
    public void userClickedSlidingButton(Component clickedButton) {
        int index = getButtonIndex(clickedButton);
        SlidingButton button = (SlidingButton) buttons.get(index);
        button.setBlinking(false);
        
        if (index != selModel.getSelectedIndex() || !isActive()) {
            TopComponent tc = (TopComponent)dataModel.getTab(index).getComponent();
            if (tc != null) {
                tc.requestActive();
            }
            button.setSelected( true );
        } else {
            selModel.setSelectedIndex(-1);
        }
    }
    
    @Override
    public void userMiddleClickedSlidingButton(Component clickedButton) {
        int index = getButtonIndex(clickedButton);
        SlidingButton button = (SlidingButton) buttons.get(index);
        button.setBlinking(false);
        
        if (index >= 0 && index < dataModel.size() ) {
            TopComponent tc = (TopComponent)dataModel.getTab(index).getComponent();
            tc.close();
        }
    }

    /** Request for automatic slide in from gesture recognizer */
    @Override
    public boolean userTriggeredAutoSlideIn(Component sourceButton) {
        int index = getButtonIndex(sourceButton);
        if (index < 0) {
            return false;
        }
        SlidingButton button = (SlidingButton) buttons.get(index);
        button.setBlinking(false);
        TopComponent tc = (TopComponent)dataModel.getTab(index).getComponent();
        if (tc == null) {
            return false;
        }
        tc.requestVisible();
        return true;
    }    
    
    /** Request for automatic slide out from gesture recognizer */
    @Override
    public void userTriggeredAutoSlideOut() {
        selModel.setSelectedIndex(-1);
    }
    
    public Rectangle getTabBounds(int tabIndex) {
        Component button = getButton(tabIndex);
        if (button == null) {
            return null;
        }
        Insets insets = getInsets();
        Point leftTop = new Point(insets.left, insets.top);
        
        if (tabbed.isHorizontal()) {
            // horizontal layout
            if( tabIndex < dataModel.size() ) {
                leftTop.x = getButton(tabIndex).getLocation().x;
            }
        } else {
            // vertical layout
            if( tabIndex < dataModel.size() ) {
                leftTop.y = getButton(tabIndex).getLocation().y;
            }
        }
        return new Rectangle(leftTop, button.getPreferredSize());
    }
    
    /********* implementation of Tabbed.Accessor **************/
    
    @Override
    public Tabbed getTabbed () {
        return tabbed;
    }

    /********* implementation of WinsysInfoForTabbedContainer **************/
    
    public WinsysInfoForTabbedContainer createWinsysInfo() {
        return new SlidedWinsysInfoForTabbedContainer();
    }

    @Override
    public void actionPerformed( ActionEvent e ) {
        if( e instanceof TabActionEvent ) {
            TabActionEvent tae = ( TabActionEvent ) e;
            if( TabbedContainer.COMMAND_RESTORE_GROUP.equals( tae.getActionCommand() ) ) {
                String nameOfModeToRestore = tae.getGroupName();
                WindowManagerImpl wm = WindowManagerImpl.getInstance();
                ModeImpl modeToRestore = ( ModeImpl ) wm.findMode( nameOfModeToRestore );
                if( null != modeToRestore ) {
                    wm.userRestoredMode( tabbed.getSlidingMode(), modeToRestore );
                }
            }
        }
    }
    
    private GridBagConstraints createConstraints() {
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = row;
        c.gridy = col;
        c.fill = GridBagConstraints.BOTH;
        c.anchor = GridBagConstraints.CENTER;

        if( tabbed.isHorizontal() )
            row++;
        else
            col++;
        
        return c;
    }
    

    private void addSeparator() {
        int separatorSize = UIManager.getInt( "NbSlideBar.GroupSeparator.Size" ); //NOI18N
        if( separatorSize > 0 ) {
            addStrut( separatorSize );
        } else {
            JSeparator separator = new JSeparator(separatorOrientation);
            int gap = UIManager.getInt( "NbSlideBar.GroupSeparator.Gap.Before" ); //NOI18N
            if( gap == 0 )
                gap = 15;
            addStrut(gap);
            GridBagConstraints c = createConstraints();
            c.insets = new Insets( 1,1,1,1 );
            if( separatorOrientation == JSeparator.VERTICAL ) {
                c.fill = GridBagConstraints.VERTICAL;
            } else {
                c.fill = GridBagConstraints.HORIZONTAL;
            }
            add( separator, c );
            gap = UIManager.getInt( "NbSlideBar.GroupSeparator.Gap.After" ); //NOI18N
            if( gap == 0 )
                gap = 5;
            addStrut(gap);
        }
    }
    
    private void addRestoreButton( String modeName ) {
        if( null == modeName )
            return;
        
        TabControlButton restoreButton = TabControlButtonFactory.createRestoreGroupButton( dummyDisplayer, modeName );
        add( restoreButton, createConstraints() );
        restoreButton.putClientProperty( "NbSlideBar.RestoreButton.Orientation", getModel().getOrientation() ); //NOI18N
        int gap = UIManager.getInt( "NbSlideBar.RestoreButton.Gap" ); //NOI18N
        if( gap == 0 )
            gap = 10;
        addStrut(gap);
    }
    
    private void addButton( SlidingButton sb ) {
        JComponent btn = isAqua ? new AquaButtonPanel(sb) : sb;
        add( btn, createConstraints() );
        int gap = UIManager.getInt( "NbSlideBar.SlideButton.Gap" ); //NOI18N
        if( gap > 0 )
            addStrut(gap);
    }

    void makeBusy( TopComponent tc, boolean busy ) {
        BusyTabsSupport.getDefault().makeTabBusy( tabbed, 0, busy );
        if( !busy ) {
            Component slide = getSlidedComp();
            if( null != slide )
                slide.repaint();
        }
        syncWithModel();
    }

    @Override
    public void addNotify() {
        super.addNotify();
        BusyTabsSupport.getDefault().install( getTabbed(), dataModel );
    }

    @Override
    public void removeNotify() {
        super.removeNotify();
        BusyTabsSupport.getDefault().uninstall( getTabbed(), dataModel );
    }

    boolean isHorizontal() {
        return tabbed.isHorizontal();
    }

    private class SlidedWinsysInfoForTabbedContainer extends WinsysInfoForTabbedContainer {
        @Override
        public Object getOrientation(Component comp) {
            if (WindowManagerImpl.getInstance().getEditorAreaState() != Constants.EDITOR_AREA_JOINED) {
                return TabDisplayer.ORIENTATION_INVISIBLE;
            }
            return TabDisplayer.ORIENTATION_CENTER;
        }

        @Override
        public boolean inMaximizedMode(Component comp) {
            return TabbedAdapter.isInMaximizedMode(comp);
        }

        @Override
        public boolean isTopComponentSlidingEnabled() {
            return Switches.isTopComponentSlidingEnabled();
        }

        @Override
        public boolean isTopComponentClosingEnabled() {
            return Switches.isViewTopComponentClosingEnabled();
        }

        @Override
        public boolean isTopComponentMaximizationEnabled() {
            return Switches.isTopComponentMaximizationEnabled();
        }

        @Override
        public boolean isTopComponentClosingEnabled(TopComponent tc) {
            return !Boolean.TRUE.equals(tc.getClientProperty(TopComponent.PROP_CLOSING_DISABLED));
        }

        @Override
        public boolean isTopComponentMaximizationEnabled(TopComponent tc) {
            return !Boolean.TRUE.equals(tc.getClientProperty(TopComponent.PROP_MAXIMIZATION_DISABLED));
        }

        @Override
        public boolean isTopComponentSlidingEnabled(TopComponent tc) {
            return !Boolean.TRUE.equals(tc.getClientProperty(TopComponent.PROP_SLIDING_DISABLED));
        }

        @Override
        public boolean isModeSlidingEnabled() {
            return Switches.isModeSlidingEnabled();
        }

        @Override
        public boolean isSlidedOutContainer() {
            return true;
        }

        @Override
        public boolean isTopComponentBusy( TopComponent tc ) {
            return WindowManagerImpl.getInstance().isTopComponentBusy( tc );
        }
    }
    
    /*************** non public stuff **************************/
    
    /* #return Component that is slided into desktop or null if no component is
     * slided currently.
     */
    Component getSlidedComp() {
        return commandMgr.getSlidedComp();
    }
    
    void setActive(boolean active) {
        this.active = active;
        commandMgr.setActive(active);
    }
    
    boolean isActive() {
        return active;
    }
    
    boolean isHoveringAllowed() {
        return !isActive() || !commandMgr.isCompSlided();
    }
    
    int getButtonIndex(Component button) {
        return buttons.indexOf(button);
        }
    
    SlidingButton getButton(int index) {
        return (SlidingButton)buttons.get(index);
    }
    
    /** @return true if slide bar contains given component, false otherwise */
    boolean containsComp(Component comp) {
        List tabs = getModel().getTabs();
        TabData curTab = null;
        for (Iterator iter = tabs.iterator(); iter.hasNext(); ) {
            curTab = (TabData)iter.next();
            if (comp.equals(curTab.getComponent())) {
                return true;
            }
        }
        return false;
    }
    
    private void addStrut( int size ) {
        JLabel lbl = new JLabel();
        Dimension dim = new Dimension( size, size );
        lbl.setMinimumSize( dim );
        lbl.setPreferredSize( dim );
        lbl.setMaximumSize( dim );
        GridBagConstraints c = createConstraints();
        c.fill = GridBagConstraints.NONE;
        add( lbl, c );
    }
    
    private void syncWithModel () {
        assert SwingUtilities.isEventDispatchThread();
        Set<TabData> blinks = null;
        for (SlidingButton curr: buttons) {
            if (curr.isBlinking()) {
                if (blinks == null) {
                    blinks = new HashSet<TabData>();
                }
                blinks.add (curr.getData());
            }
            gestureRecognizer.detachButton(curr);
        }
        removeAll();
        buttons.clear();
        
        List<TabData> dataList = dataModel.getTabs();
        SlidingButton curButton = null;
        String currentMode = null;
        boolean first = true;
        
        row = 0;
        col = 0;
        
        for( TabData td : dataList ) {
            curButton = new SlidingButton(td, dataModel.getOrientation());
            if (blinks != null && blinks.contains(td)) {
                curButton.setBlinking(true);
            }
            TopComponent tc = ( TopComponent ) td.getComponent();
            if( tabbed.isBusy( tc ) ) {
                curButton.setIcon( BusyTabsSupport.getDefault().getBusyIcon(false) );
            }
            String modeName = getRestoreModeNameForTab( td );
            gestureRecognizer.attachButton(curButton);
            buttons.add(curButton);
        
            if( Switches.isModeSlidingEnabled() ) {
                if( isAqua && first )
                    addStrut(4);
                if( null == currentMode || !currentMode.equals( modeName ) ) {
                    if( !first ) {
                        addSeparator();
                    }
                    addRestoreButton( modeName );
                    currentMode = modeName;
                    first = false;
                }
                addButton( curButton );
            } else {
                addButton( curButton );
            }
        }

        GridBagConstraints c = createConstraints();
        if( tabbed.isHorizontal() )
            c.weightx = 1.0;
        else
            c.weighty = 1.0;
        c.fill = GridBagConstraints.NONE;
        add( new JLabel(), c );
        commandMgr.syncWithModel();
        if( !UIManager.getBoolean( "NbMainWindow.showCustomBackground" ) ) //NOI18N
            setOpaque( !buttons.isEmpty() );
        // #46488 - add(...) is sometimes not enough for proper repaint, god knows why
        revalidate();
        //#47227 - repaint the bar when removing component from bar.
        //#48318 - repaint when changing name -> can change the width of buttons.
        repaint();
    }

    private String getRestoreModeNameForTab( TabData tab ) {
        Component c = tab.getComponent();
        if( c instanceof TopComponent ) {
            WindowManagerImpl wm = WindowManagerImpl.getInstance();
            String tcId = wm.findTopComponentID( (TopComponent)c );
            if( null != tcId ) {
                Mode prevMode = wm.getPreviousModeForTopComponent( tcId, tabbed.getSlidingMode() );
                if( null != prevMode )
                    return prevMode.getName();
            }
        }
        return null;
    }
    
    boolean isSlidedTabTransparent() {
        boolean res = false;
        Component c = getSlidedComp();
        if( c instanceof TabbedContainer ) {
            res = ((TabbedContainer)c).isTransparent();
        } else {
            res = tabbed.isTransparent();
        }
        return res;
    }

    /**
     * Container for sliding buttons when running under Mac Aqua l&f.
     * It used to paint correct borders and background.
     */
    private class AquaButtonPanel extends JPanel {
        private final JToggleButton slidingButton;
        private final Border pressedBorder;

        public AquaButtonPanel( JToggleButton button ) {
            super( new GridBagLayout() );
            this.slidingButton = button;
            if( tabbed.isHorizontal() ) {
                pressedBorder = new BottomBorder();
            } else {
                pressedBorder = new VerticalBorder();
            }
            add( button, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(2,0,2,0), 0, 0) );
            button.getModel().addChangeListener(new ChangeListener() {
                @Override
                public void stateChanged(ChangeEvent e) {
                    AquaButtonPanel.this.repaint();
                }
            });
        }

        private boolean isPressed() {
            if( null == slidingButton )
                return false;
            ButtonModel model = slidingButton.getModel();
            return model.isArmed() || model.isPressed() || model.isSelected();
        }

        private boolean isRollover() {
            if( null == slidingButton )
                return false;
            ButtonModel model = slidingButton.getModel();
            return model.isRollover();
        }

        @Override
        public boolean isOpaque() {
            return isPressed() || isRollover();
        }

        @Override
        public Color getBackground() {
            if( isRollover() )
                return UIManager.getColor("NbSlideBar.rollover"); //NOI18N
            else if( isPressed() )
                return UIManager.getColor("NbSplitPane.background"); //NOI18N
            return super.getBackground();
        }

        @Override
        public Border getBorder() {
            if( isPressed() ) {
                return pressedBorder;
            }
            return BorderFactory.createEmptyBorder();
        }

        @Override
        public Dimension getPreferredSize() {
            return null == slidingButton ? super.getPreferredSize() : slidingButton.getPreferredSize();
        }

        @Override
        public Dimension getMinimumSize() {
            return null == slidingButton ? super.getMinimumSize() : slidingButton.getMinimumSize();
        }

        @Override
        public Dimension getMaximumSize() {
            return null == slidingButton ? super.getMaximumSize() : slidingButton.getMaximumSize();
        }
    }

    private static final class BottomBorder implements Border {

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            g.setColor(UIManager.getColor("NbBrushedMetal.darkShadow")); //NOI18N
            g.drawLine(x, y, x, y+height);
            g.setColor(UIManager.getColor("NbBrushedMetal.lightShadow")); //NOI18N
            g.drawLine(x+width-1, y, x+width-1, y+height);
        }

        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(3,1,3,1);
        }

        @Override
        public boolean isBorderOpaque() {
            return false;
        }
    }

    private static final class VerticalBorder implements Border {

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            g.setColor(UIManager.getColor("NbBrushedMetal.darkShadow")); //NOI18N
            g.drawLine(x, y, x+width-1, y);
            g.setColor(UIManager.getColor("NbBrushedMetal.lightShadow")); //NOI18N
            g.drawLine(x, y+height-1, x+width-1, y+height-1);
        }

        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(1,3,1,3);
        }

        @Override
        public boolean isBorderOpaque() {
            return false;
        }
    }
}
