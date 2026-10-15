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
package org.netbeans.core.multitabs.prefs;

import java.awt.BorderLayout;
import javax.swing.JPanel;
import org.netbeans.core.windows.options.TabsPanel;

/**
 * 
 * @author S. Aubrecht
 */
//@OptionsPanelController.Keywords(keywords={"#KW_WindowOptions"}, location=OptionsDisplayer.ADVANCED, tabTitle="#AdvancedOption_DisplayName_WinSys")
final class MultiTabsPanel extends TabsPanel {

    private InnerTabsPanel tabsPanel;

    MultiTabsPanel( final MultiTabsOptionsPanelController controller ) {
        super( controller );
    }

    @Override
    protected void initTabsPanel( JPanel panel ) {
        if( null == tabsPanel )
            tabsPanel = new InnerTabsPanel( ( MultiTabsOptionsPanelController ) controller);
        panel.removeAll();
        panel.setLayout( new BorderLayout(0, 0) );
        panel.add( tabsPanel, BorderLayout.CENTER );
    }

    @Override
    protected void load() {
        super.load();
        tabsPanel.load();
    }

    @Override
    protected boolean store() {
        if( null == tabsPanel )
            return false;
        boolean changed = super.store();
        changed |= tabsPanel.store();
        return changed;
    }
}
