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
package org.netbeans.core.windows.view.ui.tabcontrol;

import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.geom.Area;
import javax.swing.Icon;
import javax.swing.JTabbedPane;
import javax.swing.SingleSelectionModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.core.windows.view.ui.slides.SlideController;
import org.netbeans.core.windows.view.ui.tabcontrol.tabbedpane.CloseableTabComponent;
import org.netbeans.core.windows.view.ui.tabcontrol.tabbedpane.NBTabbedPane;
import org.netbeans.core.windows.view.ui.tabcontrol.tabbedpane.NBTabbedPaneController;
import org.netbeans.swing.tabcontrol.ComponentConverter;
import org.netbeans.swing.tabcontrol.TabDataModel;
import org.netbeans.swing.tabcontrol.TabbedContainer;
import org.netbeans.swing.tabcontrol.WinsysInfoForTabbedContainer;
import org.netbeans.swing.tabcontrol.customtabs.Tabbed;
import org.netbeans.swing.tabcontrol.customtabs.TabbedType;
import org.netbeans.swing.tabcontrol.event.TabActionEvent;
import org.openide.windows.TopComponent;

/**
 * Tab control based on plain JTabbedPane.
 *
 * @author S. Aubrecht
 */
public class JTabbedPaneAdapter extends NBTabbedPane implements Tabbed.Accessor, SlideController {

    private NBTabbedPaneController controller;

    public JTabbedPaneAdapter( TabbedType type, WinsysInfoForTabbedContainer info ) {
        super( null, type, info );
        controller = new NBTabbedPaneController( this );
        controller.attachModelAndSelectionListeners();

        getModel().addChangeListener( new ChangeListener() {

            @Override
            public void stateChanged( ChangeEvent ce ) {

                int idx = getModel().getSelectedIndex();
                if( idx != -1 ) {
                    tabbedImpl.fireStateChanged();
                }
            }
        } );
    }
    private final AbstractTabbedImpl tabbedImpl = new AbstractTabbedImpl() {


        @Override
        public int getTabCount() {
            return JTabbedPaneAdapter.this.getTabCount();
        }

        @Override
        public int indexOf( Component tc ) {
            return JTabbedPaneAdapter.this.indexOf( tc );
        }

        @Override
        public void setTitleAt( int index, String title ) {
            CloseableTabComponent ctc = ( CloseableTabComponent ) getTabComponentAt( index );
            ctc.setTitle( title );
            getTabModel().setText( index, title );
        }

        @Override
        public void setIconAt( int index, Icon icon ) {
            CloseableTabComponent ctc = ( CloseableTabComponent ) getTabComponentAt( index );
            ctc.setIcon( icon );
        }

        @Override
        public void setToolTipTextAt( int index, String toolTip ) {
            CloseableTabComponent ctc = ( CloseableTabComponent ) getTabComponentAt( index );
            ctc.setTooltip( toolTip );
        }

        @Override
        public void addActionListener( ActionListener al ) {
            JTabbedPaneAdapter.this.addActionListener( al );
        }

        @Override
        public void removeActionListener( ActionListener al ) {
            JTabbedPaneAdapter.this.removeActionListener( al );
        }

        @Override
        public void setActive( boolean active ) {
            JTabbedPaneAdapter.this.setActive( active );
        }

        @Override
        public int tabForCoordinate( Point p ) {
            return JTabbedPaneAdapter.this.tabForCoordinate( p );
        }

        @Override
        public Image createImageOfTab( int tabIndex ) {
            return JTabbedPaneAdapter.this.createImageOfTab( tabIndex );
        }

        @Override
        public Component getComponent() {
            return JTabbedPaneAdapter.this;
        }

        @Override
        public Rectangle getTabBounds( int tabIndex ) {
            return JTabbedPaneAdapter.this.getBoundsAt( tabIndex );
        }

        @Override
        public Rectangle getTabsArea() {
            return getBounds();
        }

        @Override
        public boolean isTransparent() {
            return false;
        }

        @Override
        public void setTransparent( boolean transparent ) {
        }

        @Override
        protected TabDataModel getTabModel() {
            return JTabbedPaneAdapter.this.getDataModel();
        }

        @Override
        protected SingleSelectionModel getSelectionModel() {
            return JTabbedPaneAdapter.this.getModel();
        }

        @Override
        protected void requestAttention( int tabIndex ) {
            JTabbedPaneAdapter.this.requestAttention( tabIndex );
        }

        @Override
        protected void cancelRequestAttention( int tabIndex ) {
            JTabbedPaneAdapter.this.cancelRequestAttention( tabIndex );
        }

        @Override
        protected void setAttentionHighlight( int tabIndex, boolean highlight ) {
            JTabbedPaneAdapter.this.setAttentionHighlight( tabIndex, highlight );
        }

        @Override
        protected int dropIndexOfPoint( Point location ) {
            return JTabbedPaneAdapter.this.dropIndexOfPoint( location );
        }

        @Override
        protected ComponentConverter getComponentConverter() {
            return JTabbedPaneAdapter.this.getComponentConverter();
        }

        @Override
        protected Shape getDropIndication( TopComponent draggedTC, Point location ) {
            return JTabbedPaneAdapter.this.getDropIndication( draggedTC, location );
        }
    };

    /**
     * Get a shape appropriate for drawing on the window's glass pane to
     * indicate where a component should appear in the tab order if it is
     * dropped here.
     *
     * @param dragged An object being dragged, or null. The object may be an
     * instance of
     * <code>TabData</code> or
     * <code>Component</code>, in which case a check will be done of whether the
     * dragged object is already in the data model, so that attempts to drop the
     * object over the place it already is in the model will always return the
     * exact indication of that tab's position.
     *
     * @param location A point
     * @return Drop indication drawing
     */
    public Shape getDropIndication( Object dragged, Point location ) {
        int over = dropIndexOfPoint( location );
        Rectangle component = getSelectedComponent().getBounds();
        Area selectedComponent = new Area( component );

        Rectangle firstTab = null, secondTab = null;
        if( over > 0 && over < getTabCount() )
            firstTab = getBoundsAt( over-1 );
        if( over < getTabCount() )
            secondTab = getBoundsAt( over );
        if( over >= getTabCount() ) {
            firstTab = getBoundsAt( getTabCount()-1 );
            secondTab = null;
        }
        Rectangle joined = joinTabAreas( firstTab, secondTab );
        Area t = new Area( joined );
        selectedComponent.add( t );
        return selectedComponent;
    }

    private Rectangle joinTabAreas( Rectangle firstTab, Rectangle secondTab ) {
        assert null != firstTab || null != secondTab;
        Rectangle res = new Rectangle();
        switch( getTabPlacement() ) {
            case JTabbedPane.TOP:
            case JTabbedPane.BOTTOM:
                if( null != firstTab && null != secondTab && firstTab.y != secondTab.y ) {
                    //the neighboring tabs are on different tab rows
                    firstTab = null;
                }
                if( null == firstTab ) {
                    res.height = secondTab.height;
                    res.y = secondTab.y;
                    res.x = secondTab.x;
                    res.width = secondTab.width/2;
                } else if( null == secondTab ) {
                    res.height = firstTab.height;
                    res.y = firstTab.y;
                    res.x = firstTab.x + firstTab.width/2;
                    res.width = firstTab.width/2;
                } else {
                    res.height = firstTab.height;
                    res.y = firstTab.y;
                    res.x = firstTab.x + firstTab.width/2;
                    res.width = firstTab.width/2 + secondTab.width/2;
                }
                break;
                
            case JTabbedPane.LEFT:
            case JTabbedPane.RIGHT:
                if( null != firstTab && null != secondTab && firstTab.x != secondTab.x ) {
                    //the neighboring tabs are on different tab rows
                    firstTab = null;
                }
                if( null == firstTab ) {
                    res.width = secondTab.width;
                    res.y = secondTab.y;
                    res.x = secondTab.x;
                    res.height = secondTab.height/2;
                } else if( null == secondTab ) {
                    res.width = firstTab.width;
                    res.x = firstTab.x;
                    res.y = firstTab.y + firstTab.height/2;
                    res.height = firstTab.height/2;
                } else {
                    res.width = firstTab.width;
                    res.x = firstTab.x;
                    res.y = firstTab.y + firstTab.height/2;
                    res.height = firstTab.height/2 + secondTab.height/2;
                }
                break;
        }
        return res;
    }

    /**
     * ******* implementation of Tabbed.Accessor *************
     */
    @Override
    public Tabbed getTabbed() {
        return tabbedImpl;
    }

    @Override
    public void userToggledAutoHide( int tabIndex, boolean enabled ) {
        postActionEvent(new TabActionEvent(this, TabbedContainer.COMMAND_ENABLE_AUTO_HIDE, tabIndex));
    }

    @Override
    public void userToggledTransparency( int tabIndex ) {
        postActionEvent(new TabActionEvent(this, TabbedContainer.COMMAND_TOGGLE_TRANSPARENCY, tabIndex));
    }

    @Override
    public Dimension getMinimumSize() {
        return new Dimension(10,10);
    }


}
