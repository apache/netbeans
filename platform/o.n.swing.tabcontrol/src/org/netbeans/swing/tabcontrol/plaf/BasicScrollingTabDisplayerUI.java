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
/*
 * BasicScrollingTabDisplayerUI.java
 *
 * Created on March 19, 2004, 1:08 PM
 */

package org.netbeans.swing.tabcontrol.plaf;

import org.netbeans.swing.tabcontrol.TabDisplayer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import org.netbeans.swing.tabcontrol.TabData;
import org.netbeans.swing.tabcontrol.TabDataModel;
import org.openide.windows.TopComponent;

/**
 * Base class for tab displayers that have scrollable tabs.
 *
 * @author Tim Boudreau
 */
public abstract class BasicScrollingTabDisplayerUI extends BasicTabDisplayerUI {
    private Rectangle scratch = new Rectangle();
    
    private JPanel controlButtons;
    
    private TabControlButton btnScrollLeft;
    private TabControlButton btnScrollRight;
    private TabControlButton btnDropDown;
    private TabControlButton btnMaximizeRestore;

    private final Autoscroller autoscroll = new Autoscroller();

    /**
     * Creates a new instance of BasicScrollingTabDisplayerUI
     */
    public BasicScrollingTabDisplayerUI(TabDisplayer displayer) {
        super(displayer);
    }

    @Override
    public Insets getAutoscrollInsets() {
        return new Insets( 0, 30, 0, 30 );
    }

    @Override
    public void autoscroll( Point location ) {
        if( !displayer.getBounds().contains( location ) ) {
            autoscroll.stop();
            return;
        }
        if( location.x < 30) {
            autoscroll.start( true );
        } else if( location.x > displayer.getWidth() - 30 - getTabAreaInsets().right ) {
            autoscroll.start( false );
        } else {
            autoscroll.stop();
        }
    }

    @Override
    protected TabLayoutModel createLayoutModel() {
        DefaultTabLayoutModel dtlm = new DefaultTabLayoutModel(
                displayer.getModel(),
                displayer);
        return new ScrollingTabLayoutModel(dtlm, selectionModel,
                displayer.getModel());
    }

    @Override
    protected TabState createTabState() {
        return new ScrollingTabState();
    }
    
    @Override
    protected HierarchyListener createHierarchyListener() {
        return new ScrollingHierarchyListener();
    }

    @Override
    public void makeTabVisible (int tab) {
        if( tab < 0 ) //#219681 - nobody has set the selected tab yet
            return;
        if (scroll().makeVisible(tab, getTabsAreaWidth())) {
            getTabsVisibleArea(scratch);
            displayer.repaint(scratch.x, scratch.y, scratch.width, scratch.height);
        }

        if( null == btnMaximizeRestore )
            return;
        TabData td = displayer.getModel().getTab(tab);
        Component c = td.getComponent();
        if( !(c instanceof TopComponent) )
            return;
        boolean maximizeEnabled = displayer.getContainerWinsysInfo().isTopComponentMaximizationEnabled((TopComponent)c);
        btnMaximizeRestore.setEnabled(maximizeEnabled);
    }

    /**
     * Returns the width of the tabs area
     */
    protected final int getTabsAreaWidth() {
        int result = displayer.getWidth();
        Insets ins = getTabAreaInsets();
        return result - (ins.left + ins.right);
    }

    @Override
    public Insets getTabAreaInsets() {
        return new Insets(0, 0, 0, getControlButtons().getPreferredSize().width + 5);
    }

    @Override
    protected final int getLastVisibleTab() {
        if (displayer.getModel().size() == 0) {
            return -1;
        }
        return scroll().getLastVisibleTab(getTabsAreaWidth());
    }

    @Override
    protected final int getFirstVisibleTab() {
        if (displayer.getModel().size() == 0) {
            return -1;
        }
        return scroll().getFirstVisibleTab(getTabsAreaWidth());
    }

    @Override
    protected void install() {
        super.install();
        installControlButtons();
        ((ScrollingTabLayoutModel) layoutModel).setPixelsToAddToSelection (
                defaultRenderer.getPixelsToAddToSelection());
    }

    @Override
    protected void uninstall() {
        super.uninstall();
        displayer.setLayout(null);
        displayer.removeAll();
    }

    protected LayoutManager createLayout() {
        return new WCLayout();
    }

    /**
     * @return A component that holds control buttons (scroll left/right, drop down menu)
     * that are displayed to right of the tab area.
     */
    protected Component getControlButtons() {
        if( null == controlButtons ) {
            JPanel buttonsPanel = new JPanel( null );
            buttonsPanel.setOpaque( false );

            int width = 0;
            int height = 0;
            
            final boolean isGTK = "GTK".equals(UIManager.getLookAndFeel().getID());

            //create scroll-left button
            Action a = scroll().getBackwardAction();
            a.putValue( "control", displayer ); //NO18N
            btnScrollLeft = TabControlButtonFactory.createScrollLeftButton( displayer, a, isGTK );
            buttonsPanel.add( btnScrollLeft );
            Dimension prefDim = btnScrollLeft.getPreferredSize();
            btnScrollLeft.setBounds( width, 0, prefDim.width, prefDim.height );
            width += prefDim.width;
            height = prefDim.height;
            width += getScrollButtonPadding();

            //create scroll-right button
            a = scroll().getForwardAction();
            a.putValue( "control", displayer ); //NO18N
            btnScrollRight = TabControlButtonFactory.createScrollRightButton( displayer, a, isGTK );
            buttonsPanel.add( btnScrollRight );
            prefDim = btnScrollRight.getPreferredSize();
            btnScrollRight.setBounds( width, 0, prefDim.width, prefDim.height );
            width += prefDim.width;
            height = Math.max ( height, prefDim.height );

            //create drop down button
            btnDropDown = TabControlButtonFactory.createDropDownButton( displayer, isGTK );
            buttonsPanel.add( btnDropDown );

            width += 3;
            prefDim = btnDropDown.getPreferredSize();
            btnDropDown.setBounds( width, 0, prefDim.width, prefDim.height );
            width += prefDim.width;
            height = Math.max ( height, prefDim.height );
            
            //maximize / restore button
            if( null != displayer.getContainerWinsysInfo() 
                    && displayer.getContainerWinsysInfo().isTopComponentMaximizationEnabled()) {
                width += 3;
                btnMaximizeRestore = TabControlButtonFactory.createMaximizeRestoreButton( displayer, isGTK );
                buttonsPanel.add( btnMaximizeRestore );
                prefDim = btnMaximizeRestore.getPreferredSize();
                btnMaximizeRestore.setBounds( width, 0, prefDim.width, prefDim.height );
                width += prefDim.width;
                height = Math.max ( height, prefDim.height );
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

    int getScrollButtonPadding() {
        return 0;
    }
    
    @Override
    protected ComponentListener createComponentListener() {
        return new ScrollingDisplayerComponentListener();
    }

    private int lastKnownModelSize = Integer.MAX_VALUE;
    /** Overrides <code>modelChanged()</code> to clear the transient information in the
     * state model, which may now contain tab indices that don't exist, and also
     * to clear cached width/last-visible-tab data in the layout model, and ensure that
     * the selected tab is visible.
     */
    @Override
    protected void modelChanged() {
        scroll().clearCachedData();
        int index = selectionModel.getSelectedIndex();
        
        //If the user has intentionally scrolled the selected tab offscreen, do ensure space is
        //optimally used, but don't volunteer to radically change the scroll point
        if (index >= scroll().getCachedFirstVisibleTab() && index < scroll().getCachedLastVisibleTab()) {
            makeTabVisible(selectionModel.getSelectedIndex());
        }
        
        int modelSize = displayer.getModel().size();
        if (modelSize < lastKnownModelSize) {
            //When closing tabs, make sure we resync the state, so the
            //user doesn't end up with a huge gap due to closed tabs
            scroll().ensureAvailableSpaceUsed(true);
        }
        lastKnownModelSize = modelSize;
        super.modelChanged();
    }

    protected void installControlButtons() {
        displayer.setLayout(createLayout());
        displayer.add(getControlButtons());
    }

    /**
     * Convenience getter for the layout model as an instance of
     * ScrollingTabLayoutModel
     */
    protected final ScrollingTabLayoutModel scroll() {
        return (ScrollingTabLayoutModel) layoutModel;
    }

    /**
     * Overridden to update the offset of the ScrollingTabLayoutModel on mouse
     * wheel events
     */
    @Override
    protected void processMouseWheelEvent(MouseWheelEvent e) {
        int i = e.getWheelRotation();
        //clear the mouse-in-tab index so we don't occasionally have
        //tabs the mouse is not in scrolling away looking as if the mouse
        //is in them
        tabState.clearTransientStates();
        int offset = scroll().getOffset();
        if (i > 0 && (offset < displayer.getModel().size() - 1)) {
            if (scroll().isLastTabClipped()) {
                scroll().setOffset(offset + 1);
            }
        } else if (i < 0) {
            if (offset >= 0) {
                scroll().setOffset(offset - 1);
            }
        } else {
            return;
        }
        

        //tabState.repaintAllTabs();
        //XXX should optimize this - need to make sure the space below the tabs
        //is painted on metal and win classic
        displayer.repaint();
    }


    protected class ScrollingTabState extends BasicTabState {
        @Override
        public int getState(int tabIndex) {
            int result = super.getState(tabIndex);
            int first = getFirstVisibleTab();
            int last = getLastVisibleTab();

            if (tabIndex < first || tabIndex > last) {
                return TabState.NOT_ONSCREEN;
            }
            if (first == last && first == tabIndex
                    && displayer.getModel().size() > 1) {
                //We have a very small area to fit tabs - smaller than even the
                //minimum clip width, probably < 40 pixels.  Definitely don't
                //want to display a close button or much of anything else
                result |= TabState.CLIP_LEFT | TabState.CLIP_RIGHT;

            } else if (getTabsAreaWidth() < scroll()
                    .getMinimumLeftClippedWidth()
                    + scroll().getMinimumRightClippedWidth()
                    && tabIndex == first && last == first - 1 && displayer.getModel()
                    .size()
                    > 1 && scroll().isLastTabClipped()) {
                //when we're displaying two tabs in less than enough room,
                //make sure a truncated tab is never displayed with a close button
                result |= TabState.CLIP_LEFT;
            } else {
                if (tabIndex == first && scroll().getOffset() == first) {
                    result |= TabState.CLIP_LEFT;
                }
                if (tabIndex == last && scroll().isLastTabClipped()) {
                    result |= TabState.CLIP_RIGHT;
                }
            }
            return result;
        }
    }

    protected class ScrollingDisplayerComponentListener extends ComponentAdapter {
        @Override
        public void componentResized(ComponentEvent e) {
            //Notify the layout model that its cached sizes are invalid
            makeTabVisible(selectionModel.getSelectedIndex());
        }
    }
    
    protected class ScrollingHierarchyListener extends DisplayerHierarchyListener {
        @Override
        public void hierarchyChanged(HierarchyEvent e) {
            super.hierarchyChanged (e);
            if ((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0) {
                if (displayer.isShowing()) {
                    //#47850 - for some reason, uninstall can be called on the Ui class, before this gets processed.
                    // check for null values just to be sure.
                    if (tabState != null && selectionModel != null) {
                        tabState.setActive (displayer.isActive());
                        makeTabVisible (selectionModel.getSelectedIndex());
                    }
                }
            }
        }
    }

    /**
     * Provides an offscreen graphics context so that widths based on character
     * size can be calculated correctly before the component is shown. Never returns null.
     *
     * <p>For more accurate text measurements, clients should prefer calling
     * {@link #getOffscreenGraphics(JComponent)}.
     */
    public static Graphics2D getOffscreenGraphics() {
      return getOffscreenGraphics(null);
    }

    /* Just keep a single cached ScratchGraphics object for now. Even if two components are being
    painted on different screens with different incompatible ScratchGraphics instances, the cache
    should still be effective, since painting will likely happen one window at a time. */
    private static volatile ScratchGraphics cachedScratchGraphics = null;

    /**
     * Provides an offscreen graphics context so that widths based on character
     * size can be calculated correctly before the component is shown. Never returns null.
     *
     * @param component may be null without causing fatal errors, but should be set for accurate
     *        text measurement (especially on displays with HiDPI scaling enabled)
     */
    public static Graphics2D getOffscreenGraphics(JComponent component) {
      GraphicsConfiguration gc = (component == null) ? null : component.getGraphicsConfiguration();
      if (gc == null) {
          gc = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
      }

      ScratchGraphics scratchGraphics = cachedScratchGraphics;
      if (scratchGraphics != null && scratchGraphics.isConfigurationCompatible(gc)) {
          return scratchGraphics.getGraphics();
      }

      scratchGraphics = new ScratchGraphics(gc);
      cachedScratchGraphics = scratchGraphics;
      return scratchGraphics.getGraphics();
    }

    /* Same class as used in org.openide.awt.HtmlRendererImpl. This could eventually be pulled out
    into its own utility class, though this would require a new API to be exposed. */
    private static final class ScratchGraphics {
        private final GraphicsConfiguration configuration;
        private Reference<Graphics2D> graphics = new SoftReference<Graphics2D>(null);

        public ScratchGraphics(GraphicsConfiguration configuration) {
            if (configuration == null) {
                throw new NullPointerException();
            }
            this.configuration = configuration;
        }

        public boolean isConfigurationCompatible(GraphicsConfiguration other) {
            return configuration.getColorModel().equals(other.getColorModel())
                && configuration.getDefaultTransform().equals(other.getDefaultTransform());
        }

        public Graphics2D getGraphics() {
            Graphics2D result = graphics.get();
            if (result == null) {
                /* Equivalent to configuration.createCompatibleImage(int, int), just to show that only the
                ColorModel field of the GraphicsConfiguration is really relevant here. */
                ColorModel model = configuration.getColorModel();
                WritableRaster raster = model.createCompatibleWritableRaster(1, 1);
                BufferedImage img = new BufferedImage(model, raster, model.isAlphaPremultiplied(), null);
                result = img.createGraphics();
                this.graphics = new SoftReference<Graphics2D>(result);
            }
            // Restore state on every call, just in case a client modified it for some reason.
            result.setClip(null);
            /* Apply the scaling HiDPI transform. This affects font measurements, via
            FontRenderContext.getTransform(). */
            result.setTransform(configuration.getDefaultTransform());
            return result;
        }
    }

    /**
     * @return Bounds for the control buttons in the tab displayer container.
     */
    protected Rectangle getControlButtonsRectangle( Container parent ) {
        Component c = getControlButtons();
        return new Rectangle( parent.getWidth()-c.getWidth(), 0, c.getWidth(), c.getHeight() );
    }
    
    @Override
    public Dimension getMinimumSize(JComponent c) {
        return new Dimension( 100,  displayer.getPreferredSize().height);
    }
    
    /**
     * Layout manager for the tab displayer to make sure that control buttons
     * are always displayed at the end of the tab list.
     */
    private class WCLayout implements LayoutManager {

        @Override
        public void addLayoutComponent(String name, Component comp) {
        }

        @Override
        public void layoutContainer(java.awt.Container parent) {
            
            Rectangle r = getControlButtonsRectangle( parent );
            Component c = getControlButtons();
            c.setBounds( r );
        }

        @Override
        public Dimension minimumLayoutSize(Container parent) {
            return getMinimumSize((JComponent) parent);
        }

        @Override
        public Dimension preferredLayoutSize(Container parent) {
            return getPreferredSize((JComponent) parent);
        }

        @Override
        public void removeLayoutComponent(java.awt.Component comp) {
        }
    }

    private class Autoscroller implements ActionListener {

        private int direction = 0;
        private Timer timer;

        public void start( boolean scrollLeft ) {
            int newDirection = scrollLeft ? -1 : 1;
            if( null == timer || !timer.isRunning() || direction != newDirection ) {
                if( null == timer ) {
                    timer = new Timer( 300, this );
                    timer.setRepeats( true );
                }
                this.direction = newDirection;
                timer.start();
            }
        }

        public void stop() {
            if( null != timer ) {
                timer.stop();
            }
            direction = 0;
        }

        @Override
        public void actionPerformed( ActionEvent e ) {
            if( direction < 0 ) {
                int offset = scroll().getOffset();
                if( offset >= 0 ) {
                    scroll().setOffset( offset-1 );
                } else {
                    timer.stop();
                }
            } else if( direction > 0 ) {
                int offset = scroll().getOffset();
                if (offset < displayer.getModel().size() - 1 && scroll().isLastTabClipped()) {
                    scroll().setOffset(offset + 1);
                } else {
                    timer.stop();
                }
            }
            displayer.repaint();
        }
    }
}
