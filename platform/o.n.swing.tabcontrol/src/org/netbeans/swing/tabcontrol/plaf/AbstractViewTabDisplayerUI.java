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

package org.netbeans.swing.tabcontrol.plaf;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.KeyboardFocusManager;
import java.awt.LayoutManager;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JPanel;
import javax.swing.event.ListDataEvent;
import org.netbeans.swing.tabcontrol.TabData;
import org.netbeans.swing.tabcontrol.TabDataModel;
import org.netbeans.swing.tabcontrol.TabDisplayer;
import org.netbeans.swing.tabcontrol.TabDisplayerUI;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.KeyStroke;
import javax.swing.SingleSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.UIManager;
import org.netbeans.swing.tabcontrol.TabbedContainer;
import org.netbeans.swing.tabcontrol.WinsysInfoForTabbedContainer;
import org.netbeans.swing.tabcontrol.event.ComplexListDataEvent;
import org.netbeans.swing.tabcontrol.event.ComplexListDataListener;
import org.openide.windows.TopComponent;

/**
 * Basic UI class for view tabs - non scrollable tabbed displayer, which shows all
 * tabs equally sized, proportionally. This class is independent on specific
 * L&F, acts as base class for specific L&F descendants.
 * <p>
 * XXX eventually this class should be deleted and a subclass of BasicTabDisplayer can be used;
 * currently this is simply a port of the original code to the new API. Do not introduce any new
 * subclasses of this.
 *
 * @author Dafe Simonek
 *
 */
public abstract class AbstractViewTabDisplayerUI extends TabDisplayerUI {

    private TabDataModel dataModel;

    private TabLayoutModel layoutModel;

    private FontMetrics fm;

    private Font txtFont;
    
    private Component controlButtons;

    protected Controller controller;
    
    private TabControlButton btnClose;
    private TabControlButton btnAutoHidePin;
    private TabControlButton btnMinimizeMode;
    
    /** Pin action */
    private final Action pinAction = new PinAction();
    private static final String PIN_ACTION = "pinAction";
    //toggle transparency action
    private static final String TRANSPARENCY_ACTION = "transparencyAction";
    
    public AbstractViewTabDisplayerUI (TabDisplayer displayer) {
        super (displayer);
        displayer.setLayout(null);
    }

    @Override
    public void installUI(JComponent c) {
        super.installUI(c);
        ToolTipManager.sharedInstance().registerComponent(displayer);
        controller = createController();
        dataModel = displayer.getModel();
        dataModel.addChangeListener (controller);
        dataModel.addComplexListDataListener(controller);
        layoutModel = createLayoutModel();
        if( !Boolean.getBoolean("winsys.non_stretching_view_tabs") && !isUseStretchingTabs() ) {
            btnMinimizeMode = TabControlButtonFactory.createSlideGroupButton( displayer );
            c.setLayout( new PinButtonLayout() );
        }
        displayer.addPropertyChangeListener (controller);
        selectionModel.addChangeListener (controller);
        displayer.addMouseListener(controller);
        displayer.addMouseMotionListener(controller);
        installControlButtons();
        dataModel.addChangeListener( new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                showHideControlButtons();
                if( null != dataModel )
                    dataModel.removeChangeListener( this );
            }
        });
        showHideControlButtons();
    }
    
    /**
     * Creates model to layout the tabs out within the displayer.
     * 
     * @return New instance of a model to calculate tab sizes.
     * @since 1.27
     */
    protected TabLayoutModel createLayoutModel() {
        if (Boolean.getBoolean("winsys.non_stretching_view_tabs")) {
            ViewTabLayoutModel2.PaddingInfo padding = new ViewTabLayoutModel2.PaddingInfo();
            padding.iconsXPad = 5;
            padding.txtIconsXPad = 10;
            padding.txtPad = new Dimension(5, 2);
            return new ViewTabLayoutModel2(displayer, padding);
        } else if( isUseStretchingTabs() ) {
            return new ViewTabLayoutModel(dataModel, displayer);
        } else {
            btnMinimizeMode = TabControlButtonFactory.createSlideGroupButton( displayer );
            displayer.setLayout( new PinButtonLayout() );
            return new NonStretchingViewTabLayoutModel(dataModel, displayer);
        }
    }
    
    void showHideControlButtons() {
        Component tabComponent = null;
        boolean tcSlidingEnabled = true;
        boolean tcClosingEnabled = true;
        int selIndex = Math.max( 0, displayer.getSelectionModel().getSelectedIndex() );
        if( selIndex >= 0 && selIndex < displayer.getModel().size() ) {
            TabData tab = displayer.getModel().getTab( selIndex );
            tabComponent = tab.getComponent();
            if( tabComponent instanceof TopComponent ) {
                tcSlidingEnabled = displayer.getContainerWinsysInfo().isTopComponentSlidingEnabled( (TopComponent)tabComponent );
                tcClosingEnabled = displayer.getContainerWinsysInfo().isTopComponentClosingEnabled( (TopComponent)tabComponent );
            }
        }
        btnAutoHidePin.setVisible( tabComponent != null 
                && !TabDisplayer.ORIENTATION_INVISIBLE.equals( displayer.getContainerWinsysInfo().getOrientation( tabComponent ) )
                && displayer.getContainerWinsysInfo().isTopComponentSlidingEnabled()
                && tcSlidingEnabled );

        if( null != btnClose ) {
            btnClose.setVisible(tabComponent != null && tcClosingEnabled);
        }
    }
    
    protected void installControlButtons() {
        if( null != getControlButtons() )
            displayer.add( getControlButtons() );
        if( null != btnMinimizeMode )
            displayer.add( btnMinimizeMode );
    }
    
    private static final int ICON_X_PAD = 4;

    /**
     * @return A component that holds all control buttons (maximize/restor, 
     * slide/pin, close) that are displayed in the active tab or null if
     * control buttons are not supported.
     */
    protected Component getControlButtons() {
        if( null == controlButtons ) {
            JPanel buttonsPanel = new JPanel( null );
            buttonsPanel.setOpaque( false );

            int width = 0;
            int height = 0;

            boolean showPin = null != displayer.getContainerWinsysInfo() && isUseStretchingTabs();
            //create autohide/pin button
            btnAutoHidePin = TabControlButtonFactory.createSlidePinButton( displayer );
            if( showPin ) {
                buttonsPanel.add( btnAutoHidePin );

                Icon icon = btnAutoHidePin.getIcon();
                height = icon.getIconHeight();
            }

            if( null == displayer.getContainerWinsysInfo() 
                    || displayer.getContainerWinsysInfo().isTopComponentClosingEnabled() ) {
                //create close button
                btnClose = TabControlButtonFactory.createCloseButton( displayer );
                buttonsPanel.add( btnClose );
                
                Icon icon = btnClose.getIcon();
                height = Math.max( height, icon.getIconHeight() );
            }

            if( null != btnClose ) {
                Icon icon = btnClose.getIcon();
                btnClose.setBounds( width, (height - icon.getIconHeight()) / 2, icon.getIconWidth(), icon.getIconHeight() );
                width += icon.getIconWidth();
            }

            if( showPin ) {
                if( 0 != width )
                    width += ICON_X_PAD;
                Icon icon = btnAutoHidePin.getIcon();
                btnAutoHidePin.setBounds( width, (height - icon.getIconHeight()) / 2, icon.getIconWidth(), icon.getIconHeight() );
                width += icon.getIconWidth();
                width += ICON_X_PAD;
            }
                
            Dimension size = new Dimension( width, height );
            buttonsPanel.setMinimumSize( size );
            buttonsPanel.setSize( size );
            buttonsPanel.setPreferredSize( size );
            buttonsPanel.setMaximumSize( size );
            
            controlButtons = buttonsPanel;
        }
        return controlButtons;
    }

    @Override
    public void uninstallUI(JComponent c) {
        super.uninstallUI(c);
        ToolTipManager.sharedInstance().unregisterComponent(displayer);
        displayer.removePropertyChangeListener (controller);
        dataModel.removeChangeListener(controller);
        dataModel.removeComplexListDataListener(controller);
        selectionModel.removeChangeListener(controller);
        displayer.removeMouseListener(controller);
        displayer.removeMouseMotionListener(controller);
        if (controlButtons != null) {
            displayer.remove(controlButtons);
            controlButtons = null;
        }
        layoutModel = null;
        selectionModel = null;
        dataModel = null;
        controller = null;
    }

    protected Controller createController() {
        return new Controller();
    }

    @Override
    public void paint(Graphics g, JComponent c) {

        ColorUtil.setupAntialiasing(g);

        TabData tabData;
        int x, y, width, height;
        String text;
        
        paintDisplayerBackground( g, c );

        for (int i = 0; i < dataModel.size(); i++) {
            // gather data
            tabData = dataModel.getTab(i);
            x = layoutModel.getX(i);
            y = layoutModel.getY(i);
            width = layoutModel.getW(i);
            height = layoutModel.getH(i);
            text = tabData.getText();
            // perform paint
            if (g.hitClip(x, y, width, height)) {
                paintTabBackground(g, i, x, y, width, height);
                paintTabContent(g, i, text, x, y, width, height);
                paintTabBorder(g, i, x, y, width, height);
            }
        }
    }

    protected final TabDataModel getDataModel() {
        return dataModel;
    }

    public final TabLayoutModel getLayoutModel() {
        return layoutModel;
    }

    protected final TabDisplayer getDisplayer() {
        return displayer;
    }

    protected final SingleSelectionModel getSelectionModel() {
        return selectionModel;
    }

    public Controller getController() {
        return controller;
    }

    protected final boolean isSelected(int index) {
        if( index < 0 )
            return false;
        return selectionModel.getSelectedIndex() == index;
    }

    protected final boolean isActive() {
        return displayer.isActive();
    }

    protected final boolean isFocused(int index) {
        if( index < 0 )
            return false;
        return isSelected(index) && isActive();
    }

    @Override
    protected final SingleSelectionModel createSelectionModel() {
        return new DefaultTabSelectionModel (displayer.getModel());
    }

    @Override
    public int dropIndexOfPoint(Point p) {
        int result = 0;
        for (int i=0; i < displayer.getModel().size(); i++) {
            int x = getLayoutModel().getX(i);
            int w = getLayoutModel().getW(i);
            if (p.x >= x && p.x <= x + w) {
                if (i == displayer.getModel().size() - 1) {
                    if (p.x > x + (w / 2)) {
                        result = displayer.getModel().size();
                        break;
                    } else {
                        result = i;
                        break;
                    }
                } else {
                    result = i;
                    break;
                }
            }
        }
        return result;
    }

    /**
     * Specifies font to use for text and font metrics. Subclasses may override
     * to specify their own text font
     */
    @Override
    protected Font getTxtFont() {
        if (txtFont == null) {
            txtFont = (Font) UIManager.get("windowTitleFont");
            if (txtFont == null) {
                txtFont = new Font("Dialog", Font.PLAIN, 11);
            } else if (txtFont.isBold()) {
                // don't use deriveFont() - see #49973 for details
                txtFont = new Font(txtFont.getName(), Font.PLAIN, txtFont.getSize());
            }
        }
        return txtFont;
    }

    protected final FontMetrics getTxtFontMetrics() {
        if (fm == null) {
            JComponent control = getDisplayer();
            fm = control.getFontMetrics(getTxtFont());
        }
        return fm;
    }

    protected abstract void paintTabContent(Graphics g, int index, String text,
                                            int x, int y, int width,
                                            int height);

    protected abstract void paintTabBorder(Graphics g, int index, int x, int y,
                                           int width, int height);

    protected abstract void paintTabBackground(Graphics g, int index, int x,
                                               int y, int width, int height);
    
    /**
     * Paint the background when using non-stretching tabs.
     * @param g
     * @param c 
     * @since 1.27
     */
    protected void paintDisplayerBackground( Graphics g, JComponent c ) {
        int x;
        int width;
        if( !isUseStretchingTabs() ) {
            x = 0;
            width = c.getWidth();
            if( dataModel.size() > 0 ) {
                x += layoutModel.getX( dataModel.size()-1 );
                x += layoutModel.getW( dataModel.size()-1 )-1;
                width -= x;
            }
            paintTabBackground( g, -1, x, 0, width, c.getHeight());
            paintTabBorder( g, -1, x, 0, width, c.getHeight());
        }
    }



    /** Registers shortcut for enable/ disable auto-hide functionality */
    @Override
    public void unregisterShortcuts(JComponent comp) {
        comp.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).
            remove(KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE,
                                InputEvent.CTRL_DOWN_MASK));
        comp.getActionMap().remove(PIN_ACTION);
        
        comp.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).
            remove(KeyStroke.getKeyStroke(KeyEvent.VK_NUMPAD0,
                                InputEvent.CTRL_DOWN_MASK));
        comp.getActionMap().remove(TRANSPARENCY_ACTION);
    }

    /** Registers shortcut for enable/ disable auto-hide functionality */
    @Override
    public void registerShortcuts(JComponent comp) {
        comp.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).
            put(KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE,
                                InputEvent.CTRL_DOWN_MASK), PIN_ACTION);
        comp.getActionMap().put(PIN_ACTION, pinAction);

        //TODO make shortcut configurable
        comp.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).
            put(KeyStroke.getKeyStroke(KeyEvent.VK_NUMPAD0,
                                InputEvent.CTRL_DOWN_MASK), TRANSPARENCY_ACTION);
        comp.getActionMap().put(TRANSPARENCY_ACTION, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                shouldPerformAction(TabbedContainer.COMMAND_TOGGLE_TRANSPARENCY, getSelectionModel().getSelectedIndex(), null);
            }
        });
    }
    
    @Override
    public Polygon getExactTabIndication(int index) {
        // TBD - the same code is copied in ScrollableTabsUI, should be shared
        // if will not differ
//        GeneralPath indication = new GeneralPath();
        JComponent control = getDisplayer();
        int height = control.getHeight();

        TabLayoutModel tlm = getLayoutModel();

        int tabXStart = tlm.getX(index);

        int tabXEnd = tabXStart + tlm.getW(index);

        int[] xpoints = new int[4];
        int[] ypoints = new int[4];
        xpoints[0] = tabXStart;
        ypoints[0] = 0;
        xpoints[1] = tabXEnd;
        ypoints[1] = 0;
        xpoints[2] = tabXEnd;
        ypoints[2] = height - 1;
        xpoints[3] = tabXStart;
        ypoints[3] = height - 1;

        return new EqualPolygon(xpoints, ypoints);
    }

    @Override
    public Polygon getInsertTabIndication(int index) {
        EqualPolygon indication = new EqualPolygon();
        JComponent control = getDisplayer();
        int height = control.getHeight();
        int width = control.getWidth();
        TabLayoutModel tlm = getLayoutModel();

        int tabXStart;
        int tabXEnd;
        if (index == 0) {
            tabXStart = 0;
            tabXEnd = tlm.getW(0) / 2;
        } else if (index >= getDataModel().size()) {
            tabXStart = tlm.getX(index - 1) + tlm.getW(index - 1) / 2;
            tabXEnd = tabXStart + tlm.getW(index - 1);
            if (tabXEnd > width) {
                tabXEnd = width;
            }
        } else {
            tabXStart = tlm.getX(index - 1) + tlm.getW(index - 1) / 2;
            tabXEnd = tlm.getX(index) + tlm.getW(index) / 2;
        }

        indication.moveTo(tabXStart, 0);
        indication.lineTo(tabXEnd, 0);
        indication.lineTo(tabXEnd, height - 1);
        indication.lineTo(tabXStart, height - 1);
        return indication;
    }

    /** Paints the rectangle occupied by a tab into an image and returns the result */
    @Override
    public Image createImageOfTab(int index) {
        TabData td = displayer.getModel().getTab(index);
        
        JLabel lbl = new JLabel(td.getText());
        int width = lbl.getFontMetrics(lbl.getFont()).stringWidth(td.getText());
        int height = lbl.getFontMetrics(lbl.getFont()).getHeight();
        width = width + td.getIcon().getIconWidth() + 6;
        height = Math.max(height, td.getIcon().getIconHeight()) + 5;
        
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        g.setColor(lbl.getForeground());
        g.setFont(lbl.getFont());
        td.getIcon().paintIcon(lbl, g, 0, 0);
        g.drawString(td.getText(), 18, height / 2);
        
        
        return image;
    }

    @Override
    public Rectangle getTabRect(int index, Rectangle destination) {
        if (destination == null) {
            destination = new Rectangle();
        }
        if (index < 0 || index > displayer.getModel().size()) {
            destination.setBounds (0,0,0,0);
            return destination;
        }
        destination.x = layoutModel.getX(index);
        destination.width = layoutModel.getW(index);
        destination.height = layoutModel.getH(index);
        destination.y = Math.min (0, displayer.getHeight() - destination.height);
        return destination;
    }
    
    @Override
    public int tabForCoordinate(Point p) {
        int max = displayer.getModel().size();
        if (max == 0 || p.y > displayer.getHeight() || p.y < 0 || p.x < 0 || 
            p.x > displayer.getWidth()) {
                
            return -1;
        }
        
        for (int i=0; i < max; i++) {
            int left = layoutModel.getX(i);
            int right = left + layoutModel.getW(i);
            if (p.x > left && p.x < right) {
                return i;
            }
        }
        
        return -1;
    }

    @Override
    public Dimension getMinimumSize(JComponent c) {
        return new Dimension( 100, c.getPreferredSize().height);
    }
    
    protected int createRepaintPolicy () {
        return TabState.REPAINT_SELECTION_ON_ACTIVATION_CHANGE
                | TabState.REPAINT_ON_SELECTION_CHANGE
                | TabState.REPAINT_ON_MOUSE_ENTER_TAB
                | TabState.REPAINT_ON_MOUSE_ENTER_CLOSE_BUTTON
                | TabState.REPAINT_ON_MOUSE_PRESSED;
    }
    
    protected final TabState tabState = new ViewTabState();
    
    private class ViewTabState extends TabState {
	public ViewTabState () {}
	
        @Override
        public int getRepaintPolicy(int tab) {
            return createRepaintPolicy();
        }
        
        @Override
        public void repaintAllTabs() {
            displayer.repaint();
        }
        
        @Override
        public void repaintTab (int tab) {
            if (tab < 0 || tab >= displayer.getModel().size()) {
                //This can happen because we can be notified
                //of a change on a tab that has just been removed
                //from the model
                return;
            }
            Rectangle r = getTabRect(tab, null);
            displayer.repaint(r);
        }
        
        @Override
        boolean isDisplayable() {
            return displayer.isDisplayable();
        }
    }
    
    /**
     * Determine if the tab should be flashing
     */
    protected boolean isAttention (int tab) {
        if( tab < 0 )
            return false;
        return (tabState.getState(tab) & TabState.ATTENTION) != 0
                || (tabState.getState(tab) & TabState.HIGHLIGHT) != 0;
    }

    /**
     * 
     * @param tab
     * @return True to highlight the given tab, false otherwise.
     * @since 1.38
     */
    protected boolean isHighlight (int tab) {
        if( tab < 0 )
            return false;
        return (tabState.getState(tab) & TabState.HIGHLIGHT) != 0;
    }
    

    @Override
    protected void requestAttention (int tab) {
        tabState.addAlarmTab(tab);
    }    

    @Override
    protected void cancelRequestAttention (int tab) {
        tabState.removeAlarmTab(tab);
    }

    @Override
    protected void setAttentionHighlight (int tab, boolean highlight) {
        if( highlight ) {
            tabState.addHighlightTab(tab);
        } else {
            tabState.removeHighlightTab(tab);
        }
    }
    
    final boolean isUseStretchingTabs() {
        if( Boolean.getBoolean("winsys.stretching_view_tabs") ) //NOI18N
            return true;
        if( displayer.getType() != TabDisplayer.TYPE_VIEW )
            return true;
        WinsysInfoForTabbedContainer winsysInfo = displayer.getContainerWinsysInfo();
        if( null != winsysInfo && winsysInfo.isSlidedOutContainer() )
            return true;
        return null == winsysInfo || !winsysInfo.isModeSlidingEnabled();
    }
    
    int getModeButtonVerticalOffset() {
        return 0;
    }

    /**
     * Listen to mouse events and handles selection behaviour and close icon
     * button behaviour.
     */
    protected class Controller extends MouseAdapter
            implements MouseMotionListener, ChangeListener, PropertyChangeListener, ComplexListDataListener {

        /**
         * true when selection is changed as a result of mouse press
         */
        private boolean selectionChanged;

        protected boolean shouldReact(MouseEvent e) {
            boolean isLeft = SwingUtilities.isLeftMouseButton(e);
            return isLeft;
        }

        @Override
        public void stateChanged (ChangeEvent ce) {
            displayer.repaint();
            showHideControlButtons();
        }

        @Override
        public void propertyChange (PropertyChangeEvent pce) {
            if (TabDisplayer.PROP_ACTIVE.equals (pce.getPropertyName())) {
                displayer.repaint();
            }
        }
        
        /**
         * @param p Mouse point location
         * @return True if the point is in the control buttons panel.
         */
        public boolean inControlButtonsRect( Point p ) {
            boolean res = false;
            if( null != controlButtons ) {
                Point p2 = SwingUtilities.convertPoint(displayer, p, controlButtons);
                res |= controlButtons.contains(p2);
            }
            if( null != btnMinimizeMode ) {
                Point p2 = SwingUtilities.convertPoint(displayer, p, btnMinimizeMode);
                res |= btnMinimizeMode.contains(p2);
            }
            return res;
        }

        @Override
        public void mousePressed(MouseEvent e) {
            Point p = e.getPoint();
            int i = getLayoutModel().indexOfPoint(p.x, p.y);
            tabState.setPressed(i);
            SingleSelectionModel sel = getSelectionModel();
            selectionChanged = i != sel.getSelectedIndex();
            // invoke possible selection change
            if ((i != -1) || !selectionChanged) {
                boolean change = shouldPerformAction(TabDisplayer.COMMAND_SELECT,
                    i, e);
                if (change) {
                    getSelectionModel().setSelectedIndex(i);
                    tabState.setSelected(i);
                    Component tc = i >= 0 ? getDataModel().getTab(i).getComponent() : null;
                    if(tc instanceof TopComponent
                       && !((TopComponent) tc).isAncestorOf(KeyboardFocusManager.getCurrentKeyboardFocusManager().getPermanentFocusOwner())) {
                        ((TopComponent)tc).requestActive();
                    }
                }
            } 
            if (e.isPopupTrigger()) {
                //Post a popup menu show request
                shouldPerformAction(TabDisplayer.COMMAND_POPUP_REQUEST, i, e);
            }
        }

        @Override
        public void mouseClicked (MouseEvent e) {
            if (e.getClickCount() >= 2 && !e.isPopupTrigger()) {
                Point p = e.getPoint();
                int i = getLayoutModel().indexOfPoint(p.x, p.y);
                SingleSelectionModel sel = getSelectionModel();
                selectionChanged = i != sel.getSelectedIndex();
                // invoke possible selection change
                if ((i != -1) || !selectionChanged) {
                boolean change = shouldPerformAction(TabDisplayer.COMMAND_SELECT,
                    i, e);
                    if (change) {
                        getSelectionModel().setSelectedIndex(i);
                    }
                }
                if (i != -1 && e.getButton() == MouseEvent.BUTTON1 ) {
                    //Post a maximize request
                    shouldPerformAction(TabDisplayer.COMMAND_MAXIMIZE, i, e);
                }
            } else if( e.getClickCount() == 1 && !e.isPopupTrigger() && e.getButton() == MouseEvent.BUTTON2 ) {
                //middle-button clicked
                Point p = e.getPoint();
                int i = getLayoutModel().indexOfPoint(p.x, p.y);
                if( i >= 0 ) {
                    //Post a close request
                    shouldPerformAction(TabDisplayer.COMMAND_CLOSE, i, e);
                }
            }
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            // close button must not be active when selection change was
            // triggered by mouse press
            tabState.setPressed(-1);
            Point p = e.getPoint();
            int i = getLayoutModel().indexOfPoint(p.x, p.y);
            if (e.isPopupTrigger()) {
                //Post a popup menu show request
                shouldPerformAction(TabDisplayer.COMMAND_POPUP_REQUEST, i, e);
            }
        }

        @Override
        public void indicesAdded(ComplexListDataEvent e) {
            tabState.indicesAdded(e);
        }

        /**
         * Elements have been removed at the indices specified by the event's
         * getIndices() value
         *
         * @param e The event
         */
        @Override
        public void indicesRemoved(ComplexListDataEvent e) {
            tabState.indicesRemoved(e);
        }

        /**
         * Elements have been changed at the indices specified by the event's
         * getIndices() value.  If the changed data can affect display width (such
         * as a text change or a change in icon size), the event's
         * <code>isTextChanged()</code> method will return true.
         *
         * @param e The event
         */
        @Override
        public void indicesChanged(ComplexListDataEvent e) {
            tabState.indicesChanged(e);
        }
        
        @Override
        public void intervalAdded (ListDataEvent evt) {
            tabState.intervalAdded(evt);
        }
        
        @Override
        public void intervalRemoved (ListDataEvent evt) {
            tabState.intervalRemoved(evt);
        }
        
        @Override
        public void contentsChanged(ListDataEvent evt) {
            tabState.contentsChanged(evt);
        }

        @Override
        public void mouseDragged(MouseEvent e) {
        }

        @Override
        public void mouseMoved(MouseEvent e) {
        }
    } // end of Controller
    
    private class PinAction extends AbstractAction {
        @Override
        public void actionPerformed(ActionEvent e) {
            if( null != btnAutoHidePin ) {
                btnAutoHidePin.performAction( null );
            }
        }
    }
    
    private final class PinButtonLayout implements LayoutManager {

        @Override
        public void addLayoutComponent( String name, Component comp ) {
        }

        @Override
        public void removeLayoutComponent( Component comp ) {
        }

        @Override
        public Dimension preferredLayoutSize( Container parent ) {
            return parent.getPreferredSize();
        }

        @Override
        public Dimension minimumLayoutSize( Container parent ) {
            return parent.getPreferredSize();
        }

        @Override
        public void layoutContainer( Container parent ) {
            btnMinimizeMode.setBounds( 0, 0, btnMinimizeMode.getIcon().getIconWidth(), btnMinimizeMode.getIcon().getIconHeight());
            btnMinimizeMode.setLocation( displayer.getWidth()-btnMinimizeMode.getWidth()-5, (displayer.getHeight()-btnMinimizeMode.getHeight())/2 + getModeButtonVerticalOffset());
        }
    }
}
