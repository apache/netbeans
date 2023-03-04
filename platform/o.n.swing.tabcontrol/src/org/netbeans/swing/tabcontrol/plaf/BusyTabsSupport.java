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
