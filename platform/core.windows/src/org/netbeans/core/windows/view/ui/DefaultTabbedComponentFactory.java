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
package org.netbeans.core.windows.view.ui;

import javax.swing.JTabbedPane;
import javax.swing.UIManager;
import org.netbeans.core.windows.Switches;
import org.netbeans.core.windows.options.WinSysPrefs;
import org.netbeans.core.windows.view.ui.tabcontrol.JTabbedPaneAdapter;
import org.netbeans.core.windows.view.ui.tabcontrol.TabbedAdapter;
import org.netbeans.swing.tabcontrol.WinsysInfoForTabbedContainer;
import org.netbeans.swing.tabcontrol.customtabs.Tabbed;
import org.netbeans.swing.tabcontrol.customtabs.TabbedComponentFactory;
import org.netbeans.swing.tabcontrol.customtabs.TabbedType;
import org.openide.util.lookup.ServiceProvider;

/**
 * Factory to create default tab containers.
 *
 * @since 2.43
 *
 * @author S. Aubrecht
 */
@ServiceProvider(service=TabbedComponentFactory.class,position=1000)
public class DefaultTabbedComponentFactory implements TabbedComponentFactory {

    private final boolean isAquaLaF = "Aqua".equals(UIManager.getLookAndFeel().getID()); //NOI18N

    @Override
    public Tabbed createTabbedComponent( TabbedType type, WinsysInfoForTabbedContainer info ) {
        if( Switches.isUseSimpleTabs() ) {
            boolean multiRow = Switches.isSimpleTabsMultiRow();
            int placement = Switches.getSimpleTabsPlacement();
            JTabbedPaneAdapter tabPane = new JTabbedPaneAdapter( type, info );
            tabPane.setTabPlacement( placement );
            tabPane.setTabLayoutPolicy( multiRow ? JTabbedPane.WRAP_TAB_LAYOUT : JTabbedPane.SCROLL_TAB_LAYOUT );
            return tabPane.getTabbed();
        }
        else if( type == TabbedType.EDITOR ) {
            boolean multiRow = WinSysPrefs.HANDLER.getBoolean( WinSysPrefs.DOCUMENT_TABS_MULTIROW, false );
            int placement = WinSysPrefs.HANDLER.getInt( WinSysPrefs.DOCUMENT_TABS_PLACEMENT, JTabbedPane.TOP );
            if( isAquaLaF ) {
                multiRow = false;
                if( placement == JTabbedPane.LEFT || placement == JTabbedPane.RIGHT ) {
                    placement = JTabbedPane.TOP;
                }
            }
            if( multiRow || placement != JTabbedPane.TOP ) {
                JTabbedPaneAdapter tabPane = new JTabbedPaneAdapter( type, info );
                tabPane.setTabPlacement( placement );
                tabPane.setTabLayoutPolicy( multiRow ? JTabbedPane.WRAP_TAB_LAYOUT : JTabbedPane.SCROLL_TAB_LAYOUT );
                return tabPane.getTabbed();
            }
        }
        return new TabbedAdapter( type.toInt(), info ).getTabbed();
    }
}
