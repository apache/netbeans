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
package org.netbeans.api.diff;

import java.awt.Component;
import org.netbeans.spi.diff.DiffControllerImpl;
import org.netbeans.spi.diff.DiffControllerProvider;
import org.openide.util.Lookup;

import java.beans.PropertyChangeListener;
import java.io.IOException;
import javax.swing.JComponent;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;

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

    private DiffController(DiffControllerImpl impl) {
        this.impl = impl;
    }

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

    /**
     * Creates a Diff Controller for supplied left and right sources capable of creating enhanced UI.
     * 
     * Same as {@link #createEnhanced(org.netbeans.api.diff.StreamSource, org.netbeans.api.diff.StreamSource) } but
     * will try to copy UI state like diff mode or divider location from the provided controller.
     *
     * @param other Controller to copy UI state like diff modes or divider location from, may be null.
     * @param base defines content of the Base Diff pane
     * @param modified defines content of the Modified (possibly editable) Diff pane
     * @return DiffController implementation of the DiffController class
     * @throws java.io.IOException when the reading from input streams fails.
     * @since 1.81
     */
    public static DiffController createEnhanced(DiffController other, StreamSource base, StreamSource modified) throws IOException {
        DiffController newController = createEnhanced(base, modified);
        if (other != null) {
            copyUIState(other, newController);
        }
        return newController;
    }
    
    private static void copyUIState(DiffController oldView, DiffController newView) {
        JTabbedPane oldTP = findComponent(oldView.getJComponent(), JTabbedPane.class, "diff-view-mode-switcher"); // NOI18N
        JTabbedPane newTP = findComponent(newView.getJComponent(), JTabbedPane.class, "diff-view-mode-switcher"); // NOI18N
        if (newTP != null && oldTP != null) {
            newTP.setSelectedIndex(oldTP.getSelectedIndex());
        }
        JSplitPane oldSP = findComponent(oldView.getJComponent(), JSplitPane.class, "diff-view-mode-splitter"); // NOI18N
        JSplitPane newSP = findComponent(newView.getJComponent(), JSplitPane.class, "diff-view-mode-splitter"); // NOI18N
        if (newSP != null && oldSP != null) {
            newSP.setDividerLocation(oldSP.getDividerLocation());
        }
    }

    @SuppressWarnings("unchecked")
    private static <T extends JComponent> T findComponent(JComponent parent, Class<T> ofType, String withProp) {
        if (ofType.isInstance(parent) && Boolean.TRUE.equals(parent.getClientProperty(withProp))) {
            return (T) parent;
        } else {
            for (Component child : parent.getComponents()) {
                if (child instanceof JComponent jc) {
                    T comp = findComponent(jc, ofType, withProp);
                    if (comp != null) {
                        return comp;
                    }
                }
            }
        }
        return null;
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

        @Override
        public void setLocation(DiffController.DiffPane pane, DiffController.LocationType type, int location) {
            if (type == DiffController.LocationType.DifferenceIndex) {
                view.setCurrentDifference(location);
            }
        }

        @Override
        public JComponent getJComponent() {
            return (JComponent) view.getComponent();
        }

        @Override
        public int getDifferenceCount() {
            return view.getDifferenceCount();
        }
    }
}
