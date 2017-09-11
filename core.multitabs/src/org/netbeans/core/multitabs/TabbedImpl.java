/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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

    @Override
    public void setActive( boolean active ) {
        //TODO implement
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
