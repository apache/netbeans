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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Window;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import org.netbeans.core.windows.Constants;
import org.netbeans.core.windows.WindowManagerImpl;
import org.netbeans.core.windows.view.ModeView;
import org.netbeans.core.windows.view.SlidingView;
import org.netbeans.core.windows.view.ViewElement;
import org.netbeans.core.windows.view.dnd.TopComponentDraggable;
import org.netbeans.core.windows.view.dnd.WindowDnDManager;
import org.netbeans.core.windows.view.ui.AbstractModeContainer;
import org.netbeans.core.windows.view.ui.ModeComponent;
import org.netbeans.core.windows.view.dnd.TopComponentDroppable;
import org.netbeans.swing.tabcontrol.customtabs.Tabbed;
import org.openide.windows.TopComponent;


/*
 * SlideBarContainer.java
 *
 * @author Dafe Simonek
 */
public final class SlideBarContainer extends AbstractModeContainer {
    
    /** panel displaying content of this container */
    VisualPanel panel;
    
    /** Creates a new instance of SlideBarContainer */
    public SlideBarContainer(ModeView modeView, WindowDnDManager windowDnDManager) {
        super(modeView, windowDnDManager, Constants.MODE_KIND_SLIDING);
        
        panel = new VisualPanel(this);
        panel.setBorder(computeBorder(getSlidingView().getSide()));
        Component slideBar = this.tabbedHandler.getComponent();
        boolean horizontal = true;
        if( slideBar instanceof SlideBar ) {
            horizontal = ((SlideBar)slideBar).isHorizontal();
        }
        panel.add(slideBar, horizontal ? BorderLayout.WEST : BorderLayout.NORTH );
    }
    
    
    private SlidingView getSlidingView() {
        return (SlidingView)super.getModeView();
    }
    
    @Override
    public void requestAttention (TopComponent tc) {
        tabbedHandler.requestAttention(tc);
    }

    @Override
    public void cancelRequestAttention (TopComponent tc) {
        tabbedHandler.cancelRequestAttention (tc);
    }

    @Override
    public void setAttentionHighlight( TopComponent tc, boolean highlight ) {
        tabbedHandler.setAttentionHighlight (tc, highlight);
    }

    @Override
    public void makeBusy(TopComponent tc, boolean busy) {
        tabbedHandler.makeBusy( tc, busy );
    }
    
    @Override
    public void setTopComponents(TopComponent[] tcs, TopComponent selected) {
        super.setTopComponents(tcs, selected);
    }
    
    public Rectangle getTabBounds(int tabIndex) {
        return tabbedHandler.getTabBounds(tabIndex);
    }

    @Override
    protected Component getModeComponent() {
        return panel;
    }
    
    @Override
    protected Tabbed createTabbed() {
        return new TabbedSlideAdapter(((SlidingView)modeView).getSide());
    }
    
    @Override
    protected boolean isAttachingPossible() {
        return false;
    }

    @Override
    protected TopComponentDroppable getModeDroppable() {
        return panel;
    }    
    
    @Override
    protected void updateActive(boolean active) {
        // #48588 - when in SDI, slidein needs to front the editor frame.
        if(active) {
            Window window = SwingUtilities.getWindowAncestor(panel);
            if(window != null && !window.isActive() && WindowManagerImpl.getInstance().getEditorAreaState() == Constants.EDITOR_AREA_SEPARATED) {
                window.toFront();
            }
        }
    }
    
    @Override
    public boolean isActive() {
        Window window = SwingUtilities.getWindowAncestor(panel);
        // #54791 - just a doublecheck, IMHO should not happen anymore
        // after the winsys reenetrancy fix.
        return window == null ? false : window.isActive();
    }    
    
    @Override
    protected void updateTitle(String title) {
        // XXX - we have no title?
    }

    private static final boolean isAqua = "Aqua".equals(UIManager.getLookAndFeel().getID()); //NOI18N

    private static Border bottomBorder;
    private static Border bottomEmptyBorder;
    private static Border leftEmptyBorder;
    private static Border leftBorder;
    private static Border rightEmptyBorder;
    private static Border rightBorder;
    private static Border topEmptyBorder;
    private static Border topBorder;
    
    /** Builds empty border around slide bar. Computes its correct size
     * based on given orientation
     */
    private static Border computeBorder(String orientation) {
        if( isAqua )
            return BorderFactory.createEmptyBorder();
        int bottom = 0, left = 0, right = 0, top = 0;
        if (Constants.LEFT.equals(orientation)) {
            top = 1; left = 1; bottom = 1; right = 2; 
        }
        if (Constants.BOTTOM.equals(orientation)) {
            top = 2; left = 5; bottom = 1; right = 1;
        }
        if (Constants.TOP.equals(orientation)) {
            top = 1; left = 1; bottom = 2; right = 1; 
        }
        if (Constants.RIGHT.equals(orientation)) {
            top = 1; left = 2; bottom = 1; right = 1; 
        }
        return new EmptyBorder(top, left, bottom, right);
    }
    
    
    /** Component enclosing slide boxes, implements needed interfaces to talk
     * to rest of winsys
     */
    private static class VisualPanel extends JPanel implements ModeComponent, TopComponentDroppable {
    
        private final SlideBarContainer modeContainer;
        private final String side;

        static {
            if( isAqua ) {
                bottomBorder = BorderFactory.createMatteBorder(1, 0, 0, 0, UIManager.getColor("NbBrushedMetal.darkShadow")); //NOI18N

                bottomEmptyBorder = BorderFactory.createMatteBorder(3, 0, 0, 0, UIManager.getColor("NbSplitPane.background")); //NOI18N
                
                topBorder = BorderFactory.createMatteBorder(0, 0, 1, 0, UIManager.getColor("NbBrushedMetal.darkShadow")); //NOI18N

                topEmptyBorder = BorderFactory.createMatteBorder(0, 0, 3, 0, UIManager.getColor("NbSplitPane.background")); //NOI18N

                leftEmptyBorder = BorderFactory.createMatteBorder(0, 0, 0, 3, UIManager.getColor("NbSplitPane.background")); //NOI18N

                leftBorder = BorderFactory.createMatteBorder( 0,0,0,1, UIManager.getColor("NbSplitPane.background"));
 
                rightEmptyBorder = BorderFactory.createMatteBorder(0, 3, 0, 0, UIManager.getColor("NbSplitPane.background")); //NOI18N

                rightBorder = BorderFactory.createMatteBorder(0, 1, 0, 0, UIManager.getColor("NbBrushedMetal.darkShadow")); //NOI18N
            }
        }
        
        public VisualPanel (SlideBarContainer modeContainer) {
            super(new BorderLayout());
            this.modeContainer = modeContainer;
            // To be able to activate on mouse click.
            enableEvents(java.awt.AWTEvent.MOUSE_EVENT_MASK);
            side = modeContainer.getSlidingView().getSide();
            if( isAqua ) {
                setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
                setOpaque(true);
            }
            if( UIManager.getBoolean( "NbMainWindow.showCustomBackground" ) ) //NOI18N
                setOpaque( false);
        }

        @Override
        public ModeView getModeView() {
            return modeContainer.getModeView();
        }
        
        @Override
        public int getKind() {
            return modeContainer.getKind();
        }
        
        // TopComponentDroppable>>
        @Override
        public Shape getIndicationForLocation(Point location) {
            return modeContainer.getIndicationForLocation(location);
        }
        
        @Override
        public Object getConstraintForLocation(Point location) {
            return modeContainer.getConstraintForLocation(location);
        }
        
        @Override
        public Component getDropComponent() {
            return modeContainer.getDropComponent();
        }
        
        @Override
        public ViewElement getDropViewElement() {
            return modeContainer.getDropModeView();
        }
        
        @Override
        public boolean canDrop(TopComponentDraggable transfer, Point location) {
            return modeContainer.canDrop(transfer) && !transfer.isModeTransfer();
        }
        
        @Override
        public boolean supportsKind(TopComponentDraggable transfer) {
            if(transfer.isModeTransfer())
                return false;
            
            if(transfer.isAllowedToMoveAnywhere()) {
                 return true;
            }
            boolean isNonEditor = transfer.getKind() == Constants.MODE_KIND_VIEW || transfer.getKind() == Constants.MODE_KIND_SLIDING;
            boolean thisIsNonEditor = getKind() == Constants.MODE_KIND_VIEW || getKind() == Constants.MODE_KIND_SLIDING;

            return (isNonEditor == thisIsNonEditor);
        }
        // TopComponentDroppable<<

        @Override
        public Dimension getMinimumSize() {
            if (!hasVisibleComponents()) {
                // have minimum size, to avoid gridbag layout to place the empty component at [0,0] location.
                // clashes with the dnd
                Border b = getBorder();
                if( null != b ) {
                    Insets insets = b.getBorderInsets( this );
                    return new Dimension( Math.max(1, insets.left+insets.right), Math.max(1, insets.top+insets.bottom) );
                }
                return new Dimension(1,1);
            }
            return super.getMinimumSize();
        }
        
        @Override
        public Dimension getPreferredSize() {
            if( isAqua && !hasVisibleComponents()) {
                return getMinimumSize();
            }
            return super.getPreferredSize();
        }
        
        private boolean hasVisibleComponents() {
            for( Component c : getComponents() ) {
                if( c instanceof SlideBar )
                    continue;
                if( null != c && c.isVisible() )
                    return true;
            }
            return modeContainer.getTopComponents().length > 0;
        }

        @Override
        public Border getBorder() {
            if( !isAqua || null == modeContainer )
                return super.getBorder();

            Border result;
            if( Constants.BOTTOM.equals(side) ) {
                if( !hasVisibleComponents() )
                    result = bottomEmptyBorder;
                else
                    result = bottomBorder;
            } else if( Constants.TOP.equals(side) ) {
                if( !hasVisibleComponents() )
                    result = topEmptyBorder;
                else
                    result = topBorder;
            } else if( Constants.RIGHT.equals(side) ) {
                if( !hasVisibleComponents() )
                    result = rightEmptyBorder;
                else
                    result = rightBorder;
            } else if( Constants.LEFT.equals(side) ) {
                if( !hasVisibleComponents() )
                    result = leftEmptyBorder;
                else
                    result = leftBorder;
            } else {
                result = BorderFactory.createEmptyBorder();
            }

            return result;
        }
    } // End of VisualPanel
    
}
