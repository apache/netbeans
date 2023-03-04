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
package org.netbeans.core.multitabs.prefs;

import org.netbeans.core.windows.options.TabsOptionsPanelController;
import org.netbeans.core.windows.options.TabsPanel;

/**
 * 
 * @author S. Aubrecht
 */
public final class MultiTabsOptionsPanelController extends TabsOptionsPanelController {

    private MultiTabsPanel panel;

    @Override
    protected void changed(Object isChanged, Object isChangedInnerTabsPanel) {
        super.changed(isChanged, isChangedInnerTabsPanel);
    }

    @Override
    protected TabsPanel getPanel() {
        if (panel == null) {
            panel = new MultiTabsPanel(this);
        }
        return panel;
    }
}
