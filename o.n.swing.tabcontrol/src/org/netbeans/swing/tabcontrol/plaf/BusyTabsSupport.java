/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.swing.tabcontrol.plaf;

import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Set;
import javax.swing.Icon;
import javax.swing.Timer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.swing.tabcontrol.TabDataModel;
import org.netbeans.swing.tabcontrol.customtabs.Tabbed;
import org.openide.util.Lookup;
import org.openide.util.WeakSet;
import org.openide.util.lookup.ServiceProvider;
import org.openide.windows.TopComponent;

/**
 * Support class to implement animation in tab headers to indicate some sort of
 * 'busy' state.<br
 * The default implementation tracks changes in registered tab containers
 * (<code>install(Tabbed, TabDataModel)</code>) and if any tab is marked as 'busy'
 * (<code>TopComponent.makeBusy(boolean)</code>) it forces repeated repaints of
 * that tab to allow animation effects. The UI of the tab container then can use
 * an icon this class provides (<code>getBusyIcon(boolean)</code>)to indicate
 * the busy state. The default implementation of this class ensures that the
 * icon is properly animated in each repaint.
 *
 * @see TopComponent#makeBusy(boolean)
 *
 * @author S. Aubrecht
 */
public abstract class BusyTabsSupport {

    private Timer animationTimer;

    private final ChangeListener modelListener = new ChangeListener() {

        @Override
        public void stateChanged( ChangeEvent e ) {
            checkBusyTabs();
        }
    };

    private final Set<Tabbed> containers = new WeakSet<Tabbed>(10);
    private final Set<Tabbed> busyContainers = new WeakSet<Tabbed>(10);

    /**
     * @return The default implementation registered in global Lookup.
     * @see DefaultBusyTabsSupport
     */
    public static BusyTabsSupport getDefault() {
        return Lookup.getDefault().lookup( BusyTabsSupport.class );
    }

    /**
     * Track changes in given tab container to have busy tab headers repainted.
     * @param tabContainer Tab container. This method should be typically called
     * from container's <code>addNotify()</code>.
     * @param tabModel Container data model.
     */
    public final void install( Tabbed tabContainer, TabDataModel tabModel ) {
        if( containers.contains( tabContainer ) )
            return;
        tabModel.addChangeListener( modelListener );
        containers.add( tabContainer );
        checkBusyTabs();
    }

    /**
     * Unregister the given tab container. This method should be typically called
     * from container's <code>removeNotify()</code>.
     * 
     * @param tabContainer
     * @param tabModel 
     */
    public final void uninstall( Tabbed tabContainer, TabDataModel tabModel ) {
        if( busyContainers.remove( tabContainer ) )
            repaintAll( tabContainer );
        tabModel.removeChangeListener( modelListener );
        containers.remove( tabContainer );
        checkBusyTabs();
    }

    /**
     * An icon that can be shown in busy tab's header to indicate some lengthy
     * process is being run in it. The default implementation ensures the icon
     * is properly animated.
     * @param isTabSelected True to get icon for a selected tab, false to get
     * icon for regular tab state.
     * @return Busy-like icon.
     */
    public abstract Icon getBusyIcon( boolean isTabSelected );

    /**
     * Notification that busy state has changed for a given tab.
     * @param tabContainer Tab container.
     * @param tabIndex Index of the tab.
     * @param busy 
     */
    public final void makeTabBusy( Tabbed tabContainer, int tabIndex, boolean busy ) {
        if( !busy ) {
            Rectangle tabRect = tabContainer.getTabBounds( tabIndex );
            if( null != tabRect )
                tabContainer.getComponent().repaint( tabRect.x, tabRect.y, tabRect.width, tabRect.height );
        }
        checkBusyTabs();
    }

    /**
     * 
     * @return The time in milliseconds between repaints of busy tabs. Value of
     * zero means there are no repeated repaints.
     */
    protected abstract int getRepaintTimerIntervalMillis();

    /**
     * Invoked between each repaint events. The default implementation animates
     * the busy icon in this method.
     */
    protected abstract void tick();
    
    private void startAnimationTimer() {
        if( null != animationTimer ) {
            return;
        }
        int interval = getRepaintTimerIntervalMillis();
        if( interval <= 0 )
            return;
        animationTimer = new Timer( interval, new ActionListener() {
            @Override
            public void actionPerformed( ActionEvent e ) {
                checkBusyTabs();
                repaintBusyTabs();
                tick();
            }
        });
        animationTimer.setRepeats( true );
        animationTimer.start();
    }

    private void stopAnimationTimer() {
        if( null == animationTimer ) {
            return;
        }
        animationTimer.stop();
        animationTimer = null;
        repaintBusyTabs();
    }

    private void checkBusyTabs() {
        busyContainers.clear();

        for( Tabbed tc : containers ) {
            if( hasBusyTabs( tc ) )
                busyContainers.add( tc );
        }

        if( busyContainers.isEmpty() ) {
            stopAnimationTimer();
        } else {
            startAnimationTimer();
        }
    }

    private void repaintBusyTabs() {
        for( Tabbed tc : busyContainers ) {
            repaintBusy( tc );
        }
    }

    private void repaintBusy( Tabbed tabbed ) {
        for( int i=0; i<tabbed.getTabCount(); i++ ) {
            TopComponent tc = tabbed.getTopComponentAt( i );
            if( tabbed.isBusy( tc ) ) {
                Rectangle rect = tabbed.getTabBounds( i );
                tabbed.getComponent().repaint( rect.x, rect.y, rect.width, rect.height );
            }
        }
    }

    private void repaintAll( Tabbed tc ) {
        Rectangle rect = tc.getTabsArea();
        tc.getComponent().repaint( rect.x, rect.y, rect.width, rect.height );
    }

    private boolean hasBusyTabs( Tabbed tabbedContainer ) {
        boolean res = false;
        for( int tabIndex = 0; tabIndex<tabbedContainer.getTabCount(); tabIndex++ ) {
            if( tabbedContainer.isBusy( tabbedContainer.getTopComponentAt( tabIndex ) ) ) {
                res = true;
                break;
            }
        }
        return res;
    }

    /**
     * The default implementation of busy tabs.
     * @see BusyIcon
     */
    @ServiceProvider(service=BusyTabsSupport.class)
    public static final class DefaultBusyTabsSupport extends BusyTabsSupport {

        private BusyIcon busyIconSelected;
        private BusyIcon busyIconDefault;

        @Override
        public Icon getBusyIcon( boolean isTabSelected ) {
            if( isTabSelected ) {
                if( null == busyIconSelected ) {
                    busyIconSelected = BusyIcon.create( isTabSelected );
                }
                return busyIconSelected;
            } else {
                if( null == busyIconDefault ) {
                    busyIconDefault = BusyIcon.create( isTabSelected );
                }
                return busyIconDefault;
            }
        }

        @Override
        protected void tick() {
            if( null != busyIconSelected )
                busyIconSelected.tick();

            if( null != busyIconDefault )
                busyIconDefault.tick();
        }

        @Override
        protected int getRepaintTimerIntervalMillis() {
            return 150;
        }
    }
}
