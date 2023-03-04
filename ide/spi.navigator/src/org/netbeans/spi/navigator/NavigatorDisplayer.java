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

import java.beans.PropertyChangeListener;
import java.util.List;
import org.openide.util.lookup.ServiceProvider;
import org.openide.windows.TopComponent;

/**
 * An abstraction for displaying the Navigator UI. Implement only if you need to
 * display the navigator differently from the standard Navigator window.
 * <p>
 * By default the standard Navigator TopComponent is provided which manages the
 * navigator panels. An alternative visualization/placement can be specified by
 * implementing this interface and registering it is a service. The
 * methods of the interface get call by the navigator module based on what it
 * needs at the moment.
 * <p>If an implementation is provided then the navigator needs to be explicitly
 * initialized by calling {@link NavigatorHandler#activateNavigator} when the UI is shown.
 * @since 1.19
 *
 * @author Tomas Pavek
 * @see ServiceProvider
 */
public interface NavigatorDisplayer {
    /**
     * Property name to be fired when the selected panel changes in the UI.
     */
    String PROP_PANEL_SELECTION = "navigatorPanelSelection"; // NOI18N

    /**
     * Called with the display name of currently selected objects (nodes).
     * It should appear e.g. in the title of the enclosing window.
     * @param name The name of selected object(s)
     */
    void setDisplayName(String name);

    /**
     * Called to set all the panels to display, plus which one should be selected.
     * @param panels The panels to display
     * @param select The panel to select (can be null)
     */
    void setPanels(List<? extends NavigatorPanel> panels, NavigatorPanel select);

    /**
     * Called to select given panel in the UI.
     * @param panel The panel to select
     */
    void setSelectedPanel(NavigatorPanel panel);

    /**
     * @return The currently selected panel in the UI (can be null)
     */
    NavigatorPanel getSelectedPanel();

    /**
     * Tells the navigator whether it can postpone reaction to context changes
     * (possibly coalesce frequent changes) when computing the panels. In other
     * words, whether it can be behind the global context sometimes. (It may
     * cause problems in some situations.)
     * @return false if navigator should react synchronously
     */
    boolean allowAsyncUpdate();

    /**
     * @return The enclosing TopComponent of the navigator UI
     */
    TopComponent getTopComponent();

    /**
     * Add a listener for PROP_PANEL_SELECTION property change.
     * @param l the listener
     */
    void addPropertyChangeListener(PropertyChangeListener l);

    /**
     * Remove a listener for PROP_PANEL_SELECTION property change.
     * @param l the listener
     */
    void removePropertyChangeListener(PropertyChangeListener l);
}
