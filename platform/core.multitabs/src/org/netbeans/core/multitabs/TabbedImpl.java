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
package org.netbeans.core.multitabs;

import java.awt.Component;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.event.ActionListener;
import java.awt.geom.Path2D;
import javax.swing.Icon;
import javax.swing.SingleSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.core.windows.view.ui.tabcontrol.AbstractTabbedImpl;
import org.netbeans.swing.tabcontrol.ComponentConverter;
import org.netbeans.swing.tabcontrol.DefaultTabDataModel;
import org.netbeans.swing.tabcontrol.TabData;
import org.netbeans.swing.tabcontrol.TabDataModel;
import org.netbeans.swing.tabcontrol.WinsysInfoForTabbedContainer;
import org.netbeans.swing.tabcontrol.customtabs.Tabbed;
import org.netbeans.swing.tabcontrol.plaf.BusyTabsSupport;
import org.openide.windows.TopComponent;

/**
 * The default Tabbed implementation. The actual visual tab container is TabContainer
 * class and most of its logic is wired in Controller class.
 *
 * @see org.netbeans.swing.tabcontrol.customtabs.Tabbed
 *
 * @author S. Aubrecht
 */
public final class TabbedImpl extends AbstractTabbedImpl {

    private final TabContainer container;
    private final Controller controller;
    private final DefaultTabDataModel tabModel;
    private ComponentConverter componentConverter = ComponentConverter.DEFAULT;
    private boolean active;

    public TabbedImpl( WinsysInfoForTabbedContainer winsysInfo, int orientation ) {
        tabModel = new DefaultTabDataModel();
        TabDisplayer displayer = TabDisplayerFactory.getDefault().createTabDisplayer( tabModel, orientation );
        controller = new Controller( displayer );

        container = new TabContainer(this, displayer, orientation );
        
        getSelectionModel().addChangeListener( new ChangeListener() {

            @Override
            public void stateChanged( ChangeEvent e ) {
                fireStateChanged();
            }
        });
    }


    @Override
    protected final TabDataModel getTabModel() {
        return tabModel;
    }

    @Override
    protected final SingleSelectionModel getSelectionModel() {
        return controller.getSelectionModel();
    }

    @Override
    protected void requestAttention( int tabIndex ) {
        //TODO implement
    }

    @Override
    protected void cancelRequestAttention( int tabIndex ) {
        //TODO implement
    }

    @Override
    protected void setAttentionHighlight( int tabIndex, boolean highlight ) {
        //TODO implement
    }

    @Override
    protected int dropIndexOfPoint( Point location ) {
        location = SwingUtilities.convertPoint( getComponent(), location, getTabDisplayer() );
        return getTabDisplayer().dropIndexOfPoint( location );
    }

    @Override
    protected ComponentConverter getComponentConverter() {
        return componentConverter;
    }

    @Override
    protected Shape getDropIndication( TopComponent draggedTC, Point location ) {
        location = SwingUtilities.convertPoint( getComponent(), location, getTabDisplayer() );
        Path2D res = new Path2D.Double();
        Rectangle tabRect = getTabDisplayer().dropIndication( draggedTC, location );
        if( null != tabRect ) {
            tabRect = SwingUtilities.convertRectangle( getTabDisplayer(), tabRect, container );
            res.append( tabRect, false );
        }
        res.append( container.getContentArea(), false );
        return res;
    }

    @Override
    public void addActionListener( ActionListener al ) {
        controller.addActionListener( al );
    }

    @Override
    public void removeActionListener( ActionListener al ) {
        controller.removeActionListener( al );
    }

    @Override
    public int getTabCount() {
        return getTabModel().size();
    }

    @Override
    public int indexOf( Component comp ) {
        int max = getTabModel().size();
        TabDataModel mdl = getTabModel();
        for (int i=0; i < max; i++) {
            if (getComponentConverter().getComponent(mdl.getTab(i)) == comp) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public void setTitleAt( int index, String title ) {
        getTabModel().setText( index, title );
    }

    @Override
    public void setIconAt( int index, Icon icon ) {
        getTabModel().setIcon( index, icon );
    }

    @Override
    public void setToolTipTextAt( int index, String toolTip ) {
        tabModel.setToolTipTextAt(index, toolTip);
    }


    public static boolean isActive( Component c ) {
        Tabbed.Accessor acc = (Tabbed.Accessor)SwingUtilities.getAncestorOfClass(Tabbed.Accessor.class, c);
        return acc != null
                && acc.getTabbed() instanceof TabbedImpl
                && ((TabbedImpl) acc.getTabbed()).active;
    }

    @Override
    public void setActive( boolean active ) {
        this.active = active;
    }

    @Override
    public int tabForCoordinate( Point p ) {
        p = SwingUtilities.convertPoint( getComponent(), p, getTabDisplayer() );
        TabData td = getTabDisplayer().getTabAt( p );
        if( null == td )
            return -1;
        return tabModel.indexOf( td );
    }

    @Override
    public Image createImageOfTab( int tabIndex ) {
        return null;
    }

    @Override
    public Component getComponent() {
        return container;
    }

    @Override
    public Rectangle getTabBounds( int tabIndex ) {
        Rectangle res = getTabDisplayer().getTabBounds( tabIndex );
        if( null != res )
            res = SwingUtilities.convertRectangle( getTabDisplayer(), res, container );
        return res;
    }

    @Override
    public Rectangle getTabsArea() {
        Rectangle res = container.getTabDisplayer().getTabsArea();
        res = SwingUtilities.convertRectangle( getTabDisplayer(), res, container );
        return res;
    }

    @Override
    public boolean isTransparent() {
        return false;
    }

    @Override
    public void setTransparent( boolean transparent ) {
        //NOOP (or make non-opaque?)
    }

    @Override
    public void makeBusy( TopComponent tc, boolean busy ) {
        int tabIndex = indexOf( tc );
        BusyTabsSupport.getDefault().makeTabBusy( this, tabIndex, busy );
    }

    private TabDisplayer getTabDisplayer() {
        return container.getTabDisplayer();
    }
}
