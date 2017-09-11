/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
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
