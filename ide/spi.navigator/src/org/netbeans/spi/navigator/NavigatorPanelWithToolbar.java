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
package org.netbeans.spi.navigator;

import javax.swing.JComponent;

/** Description of navigation view with a toolbar component on top of basic
 * {@link NavigatorPanel} features.
 *
 * Clients will implement this interface when they need a toolbar for their Navigator view/panel.
 *
 * Implementors of this interface will be plugged into Navigator UI.
 * @see NavigatorPanel.Registration
 *
 * @since 1.25
 *
 * @author jpeska
 */
public interface NavigatorPanelWithToolbar extends NavigatorPanel {

    /** Returns a JComponent which represents panel toolbar.
     *
     * It allows clients to display a toolbar on top of the Navigator window, next to the panel chooser (ComboBox).
     *
     * @return Instance of JComponent.
     */
    public JComponent getToolbarComponent();
}
