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

package org.netbeans.core.windows.view.ui.tabcontrol;

import org.netbeans.core.windows.Constants;
import org.netbeans.core.windows.WindowManagerImpl;
import org.netbeans.swing.tabcontrol.customtabs.Tabbed;
import org.netbeans.swing.tabcontrol.ComponentConverter;
import org.netbeans.swing.tabcontrol.TabbedContainer;
import org.openide.windows.TopComponent;
import org.netbeans.core.windows.view.ui.slides.SlideController;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionListener;
import org.netbeans.core.windows.ModeImpl;
import org.netbeans.core.windows.Switches;
import org.netbeans.swing.tabcontrol.*;
import org.netbeans.swing.tabcontrol.event.TabActionEvent;
import org.netbeans.swing.tabcontrol.plaf.BusyTabsSupport;

/** Adapter class that implements a pseudo JTabbedPane API on top
 * of the new tab control.  This class should eventually be eliminated
 * and the TabbedContainer's model-driven API should be used directly.
 *
 * @author  Tim Boudreau
 */
public class TabbedAdapter extends TabbedContainer implements Tabbed.Accessor, SlideController {
    
    /** Creates a new instance of TabbedAdapter */
    public TabbedAdapter (int type, WinsysInfoForTabbedContainer winsysInfo) {
        super (null, type, winsysInfo);
        getSelectionModel().addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged (ChangeEvent ce) {
                int idx = getSelectionModel().getSelectedIndex();
                if (idx != -1) {
                    tabbedImpl.fireStateChanged();
                }
            }
        });
    }
    
    /** Finds out in what state is window system mode containing given component.
     * 
     * @return true if given component is inside mode which is in maximized state,
     * false otherwise 
     */
    public static boolean isInMaximizedMode (Component comp) {
        ModeImpl maxMode = WindowManagerImpl.getInstance().getCurrentMaximizedMode();
        if (maxMode == null) {
            return false;
        }
        return maxMode.containsTopComponent((TopComponent)comp);
    }
    
    /********** implementation of SlideController *****************/
    
    @Override
    public void userToggledAutoHide(int tabIndex, boolean enabled) {
        postActionEvent(new TabActionEvent(this, TabbedContainer.COMMAND_ENABLE_AUTO_HIDE, tabIndex));
    }    

    @Override
    public void userToggledTransparency(int tabIndex) {
        postActionEvent(new TabActionEvent(this, TabbedContainer.COMMAND_TOGGLE_TRANSPARENCY, tabIndex));
    }    
    
    /********* implementation of Tabbed.Accessor **************/
    
    @Override
    public Tabbed getTabbed() {
        return tabbedImpl;
    }

    /********* implementation of WinsysInfoForTabbed ********/
    
    public static class WinsysInfo extends WinsysInfoForTabbedContainer {
        private int containerType;
        public WinsysInfo( int containerType ) {
            this.containerType = containerType;
        }
        
        @Override
        public Object getOrientation (Component comp) {
            WindowManagerImpl wmi = WindowManagerImpl.getInstance();
            // don't show pin button in separate views
            if (!wmi.isDocked((TopComponent)comp)) {
                return TabDisplayer.ORIENTATION_INVISIBLE;
            }

            String side = wmi.guessSlideSide((TopComponent)comp);
            Object result = null;
            if (side.equals(Constants.LEFT)) {
                result = TabDisplayer.ORIENTATION_WEST;
            } else if (side.equals(Constants.RIGHT)) {
                result = TabDisplayer.ORIENTATION_EAST;
            } else if (side.equals(Constants.BOTTOM)) {
                result = TabDisplayer.ORIENTATION_SOUTH;
            } else if (side.equals(Constants.TOP)) {
                result = TabDisplayer.ORIENTATION_NORTH;
            } else {
                result = TabDisplayer.ORIENTATION_CENTER;
            }
            return result;   
        }

        @Override
        public boolean inMaximizedMode (Component comp) {
            return isInMaximizedMode(comp);
        }    
        
        @Override
        public boolean isTopComponentSlidingEnabled() {
            return Switches.isTopComponentSlidingEnabled();
        }
        
        @Override
        public boolean isTopComponentClosingEnabled() {
            if( containerType == Constants.MODE_KIND_EDITOR )
                return Switches.isEditorTopComponentClosingEnabled();
            else
                return Switches.isViewTopComponentClosingEnabled();
        }
        
        @Override
        public boolean isTopComponentMaximizationEnabled() {
            return Switches.isTopComponentMaximizationEnabled();
        }

        @Override
        public boolean isTopComponentClosingEnabled(TopComponent tc) {
            return !Boolean.TRUE.equals(tc.getClientProperty(TopComponent.PROP_CLOSING_DISABLED))
                    && isTopComponentClosingEnabled();
        }

        @Override
        public boolean isTopComponentMaximizationEnabled(TopComponent tc) {
            return !Boolean.TRUE.equals(tc.getClientProperty(TopComponent.PROP_MAXIMIZATION_DISABLED))
                    && isTopComponentMaximizationEnabled();
        }

        @Override
        public boolean isTopComponentSlidingEnabled(TopComponent tc) {
            return !Boolean.TRUE.equals(tc.getClientProperty(TopComponent.PROP_SLIDING_DISABLED))
                    && isTopComponentSlidingEnabled();
        }

        @Override
        public boolean isModeSlidingEnabled() {
            return Switches.isModeSlidingEnabled();
        }

        @Override
        public boolean isTopComponentBusy( TopComponent tc ) {
            return WindowManagerImpl.getInstance().isTopComponentBusy( tc );
        }
    } // end of LocInfo
    
    private final AbstractTabbedImpl tabbedImpl = new AbstractTabbedImpl() {


        @Override
        public Rectangle getTabBounds(int tabIndex) {
            return getTabRect(tabIndex, new Rectangle());
        }

        @Override
        public Rectangle getTabsArea() {
            return getUI().getTabsArea();
        }

        @Override
        public Component getComponent() {
            return TabbedAdapter.this;
        }

        @Override
        public int getTabCount() {
            return TabbedAdapter.this.getTabCount();
        }

        @Override
        public int indexOf( Component tc ) {
            return TabbedAdapter.this.indexOf( tc );
        }

        @Override
        public void setTitleAt( int index, String title ) {
            TabbedAdapter.this.setTitleAt( index, title );
        }

        @Override
        public void setIconAt( int index, Icon icon ) {
            TabbedAdapter.this.setIconAt( index, icon );
        }

        @Override
        public void setToolTipTextAt( int index, String toolTip ) {
            TabbedAdapter.this.setToolTipTextAt( index, toolTip );
        }

        @Override
        public void addActionListener( ActionListener al ) {
            TabbedAdapter.this.addActionListener( al );
        }

        @Override
        public void removeActionListener( ActionListener al ) {
            TabbedAdapter.this.removeActionListener( al );
        }

        @Override
        public void setActive( boolean active ) {
            TabbedAdapter.this.setActive( active );
        }

        @Override
        public int tabForCoordinate( Point p ) {
            return TabbedAdapter.this.tabForCoordinate( p );
        }

        @Override
        public Image createImageOfTab( int tabIndex ) {
            return TabbedAdapter.this.createImageOfTab( tabIndex );
        }

        @Override
        public boolean isTransparent() {
            return TabbedAdapter.this.isTransparent();
        }

        @Override
        public void setTransparent( boolean transparent ) {
            TabbedAdapter.this.setTransparent( transparent );
        }

        @Override
        protected TabDataModel getTabModel() {
            return TabbedAdapter.this.getModel();
        }

        @Override
        protected SingleSelectionModel getSelectionModel() {
            return TabbedAdapter.this.getSelectionModel();
        }

        @Override
        protected void requestAttention( int tabIndex ) {
            TabbedAdapter.this.requestAttention( tabIndex );
        }

        @Override
        protected void cancelRequestAttention( int tabIndex ) {
            TabbedAdapter.this.cancelRequestAttention( tabIndex );
        }

        @Override
        protected void setAttentionHighlight( int tabIndex, boolean highlight ) {
            TabbedAdapter.this.setAttentionHighlight( tabIndex, highlight );
        }

        @Override
        protected int dropIndexOfPoint( Point location ) {
            return TabbedAdapter.this.dropIndexOfPoint( location );
        }

        @Override
        protected ComponentConverter getComponentConverter() {
            return TabbedAdapter.this.getComponentConverter();
        }

        @Override
        protected Shape getDropIndication( TopComponent draggedTC, Point location ) {
            return TabbedAdapter.this.getDropIndication( draggedTC, location );
        }

        @Override
        public void makeBusy( TopComponent tc, boolean busy ) {
            int tabIndex = indexOf( tc );
            BusyTabsSupport.getDefault().makeTabBusy( this, tabIndex, busy );
        }
    };

    @Override
    public void addNotify() {
        super.addNotify();
        BusyTabsSupport.getDefault().install( getTabbed(), getModel() );
    }

    @Override
    public void removeNotify() {
        super.removeNotify();
        BusyTabsSupport.getDefault().uninstall( getTabbed(), getModel() );
    }
}
