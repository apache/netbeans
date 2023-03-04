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
import org.openide.awt.TabbedPaneFactory;
import org.openide.util.lookup.ServiceProvider;

/**
 * Factory to create tabbed panes with a small 'close' button in each tab.
 *
 * @author S. Aubrecht
 * @since 2.52
 */
@ServiceProvider(service=TabbedPaneFactory.class)
public class NbTabbedPaneFactory extends TabbedPaneFactory {

    @Override
    public JTabbedPane createTabbedPane() {
        return new CloseButtonTabbedPane();
    }
}
