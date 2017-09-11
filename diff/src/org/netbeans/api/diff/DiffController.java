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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package org.netbeans.api.diff;

import org.netbeans.spi.diff.DiffControllerImpl;
import org.netbeans.spi.diff.DiffControllerProvider;
import org.openide.util.Lookup;

import javax.swing.*;
import java.beans.PropertyChangeListener;
import java.io.IOException;

/**
 * Encapsulates a single Diff window that displays differences between two files (sources).
 * 
 * @author Maros Sandor
 * @since 1.18
 */
public final class DiffController {

    /**
     * Property change that indicates that set of differences OR the current difference changed. Current difference
     * changes as the user navigates in the view and set of differences may change if the view is editable or a source
     * changes programatically. 
     * Clients should update their state that depends on the current difference index or total number of differences. 
     */
    public static final String PROP_DIFFERENCES = "(void) differencesChanged"; // NOI18N

    /**
     * Enumerates Base (left) and Modified (right) panes of a Diff view for setLocation() method
     */
    public enum DiffPane { Base, Modified };

    /**
     * Enumerates types of location for setLocation() method. 
     */
    public enum LocationType { LineNumber, DifferenceIndex };

    private final DiffControllerImpl impl;

    /**
     * Creates a Diff Controller for supplied left and right sources.
     * 
     * @param base defines content of the Base Diff pane
     * @param modified defines content of the Modified (possibly editable) Diff pane
     * @return DiffController implementation of the DiffController class
     * @throws java.io.IOException when the reading from input streams fails.
     */
    public static DiffController create(StreamSource base, StreamSource modified) throws IOException {
        DiffControllerProvider provider = Lookup.getDefault().lookup(DiffControllerProvider.class);
        if (provider != null) {
            return new DiffController(provider.createDiffController(base, modified));
        } else {
            DiffView view = Diff.getDefault().createDiff(base, modified);
            return new DiffController(new DiffControllerViewBridge(view));
        }
    }

    /**
     * Creates a Diff Controller for supplied left and right sources capable of creating enhanced UI.
     *
     * @param base defines content of the Base Diff pane
     * @param modified defines content of the Modified (possibly editable) Diff pane
     * @return DiffController implementation of the DiffController class
     * @throws java.io.IOException when the reading from input streams fails.
     * @since 1.27
     */
    public static DiffController createEnhanced (StreamSource base, StreamSource modified) throws IOException {
        DiffControllerProvider provider = Lookup.getDefault().lookup(DiffControllerProvider.class);
        if (provider != null) {
            return new DiffController(provider.createEnhancedDiffController(base, modified));
        } else {
            DiffView view = Diff.getDefault().createDiff(base, modified);
            return new DiffController(new DiffControllerViewBridge(view));
        }
    }
        
    private DiffController(DiffControllerImpl impl) {
        this.impl = impl;
    }
    
    /**
     * Ensure the requested location in the Diff view is visible on screen. Diff view can be requested to jump to
     * a given line in either source or to a given Difference.
     * Diff controller may ignore the request if it does not support this functionality.
     * This method must be called from AWT thread.
     * 
     * @param pane defines which pane the location parameter refers to
     * @param type defines the location parameter, see below
     * @param location depending on the type parameter this defines either a line number or a Difference index, both 0-based
     * @throws IllegalArgumentException if location parameter is out of range for the given pane and location type
     */
    public void setLocation(DiffPane pane, LocationType type, int location) {
        impl.setLocation(pane, type, location);
    }

    /**
     * Intializes the Controller and creates visual presenter of the Diff.
     * 
     * @return JComponent component to be embedded into client UI
     */
    public JComponent getJComponent() {
        return impl.getJComponent();
    }

    /**
     * Gets total number of Differences between sources currently displayed in the Diff view.
     * 
     * @return total number of Differences in sources, an integer >= 0
     */
    public int getDifferenceCount() {
        return impl.getDifferenceCount();
    }

    /**
     * Gets the current (highlighted) difference in the Diff view.
     * 
     * @return current difference index or -1 of there is no Current difference
     */
    public int getDifferenceIndex() {
        return impl.getDifferenceIndex();
    }

    /**
     * Adds a property change listener.
     * 
     * @param listener property change listener
     */
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        impl.addPropertyChangeListener(listener);
    }
    
    /**
     * Removes a property change listener.
     * 
     * @param listener property change listener
     */
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        impl.removePropertyChangeListener(listener);
    }

    /**
     * If there is no registered DiffControllerProvider, this provides a bridge from DiffView to DiffControllerProvider.
     */
    private static class DiffControllerViewBridge extends DiffControllerImpl {
        
        private final DiffView view;

        DiffControllerViewBridge(DiffView view) {
            this.view = view;
        }

        public void setLocation(DiffController.DiffPane pane, DiffController.LocationType type, int location) {
            if (type == DiffController.LocationType.DifferenceIndex) {
                view.setCurrentDifference(location);
            }
        }

        public JComponent getJComponent() {
            return (JComponent) view.getComponent();
        }

        public int getDifferenceCount() {
            return view.getDifferenceCount();
        }
    }
}
