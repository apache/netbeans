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
package org.netbeans.spi.dashboard;

import java.util.List;
import java.util.Objects;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

/**
 * A DashboardDisplayer handles rendering and display of
 * {@link DashboardWidget}s registered in a particular category. This module
 * contains a default displayer to handle widgets registered in
 * {@code Dashboard/Main}. Alternative displayer implementations may be
 * registered in the default {@link Lookup} to override the default.
 */
public interface DashboardDisplayer extends Lookup.Provider {

    /**
     * Show the widgets from the provided category. The category is provided for
     * information purposes - the dashboard displayer is not responsible for
     * loading widget references.
     *
     * @param category category being displayed
     * @param widgets list of widget references
     */
    public void show(String category, List<WidgetReference> widgets);

    /**
     * A panel implementation provides the link between a dashboard displayer
     * and each display of a widget. Any widget may be shown multiple times on a
     * dashboard, and potentially on multiple dashboards at the same time. The
     * ID here is unique to each display instance and might be used to store
     * configuration data across executions. A widget may use the panel to
     * trigger a refresh if the rendered elements have changed.
     * <p>
     * The lookup provided by the panel will usually be the same as, or an
     * extension of, the dashboard displayer lookup. It might be used to provide
     * contextual dashboards for a particular context. If the widget needs
     * access to the context in which the widget was registered (eg. the shadow
     * file in {@code Dashboard/Main}) then it should use the lookup from the
     * {@link #widgetReference()}.
     */
    public static interface Panel extends Lookup.Provider {

        /**
         * Access the widget reference for this panel. The lookup of the widget
         * reference may contain the context of how this display of the widget
         * is registered (eg. the {@link FileObject}).
         *
         * @return widget reference
         */
        public WidgetReference widgetReference();

        /**
         * The unique id reference for this display of this widget. Will usually
         * reference the dashboard category and reference file name (eg.
         * {@code Main/Widget1}.
         *
         * @return
         */
        public String id();

        /**
         * Mark this widget rendering as invalid. The displayer will re-request
         * the title and element list from the widget. This method returns
         * immediately - the displayer will re-request and re-render the widget
         * asynchronously.
         */
        public void refresh();

    }

    /**
     * A reference linking a widget to a particular display registration. The
     * lookup from this reference may contain the {@link FileObject} from which
     * the reference was created. The ID should be unique per display
     * registration and suitable for directly returning from {@link Panel#id()}.
     */
    public static final class WidgetReference implements Lookup.Provider {

        private final String id;
        private final DashboardWidget widget;
        private final Lookup lookup;

        /**
         * Construct a WidgetReference.
         *
         * @param id widget registration ID
         * @param widget widget instance
         */
        public WidgetReference(String id, DashboardWidget widget) {
            this(id, widget, null);
        }

        /**
         * Construct a WidgetReference with the provided registration file. The
         * {@link FileObject} will be available from the lookup.
         *
         * @param id
         * @param widget
         * @param registration
         */
        public WidgetReference(String id, DashboardWidget widget, FileObject registration) {
            this.id = Objects.requireNonNull(id);
            this.widget = Objects.requireNonNull(widget);
            this.lookup = registration != null ? Lookups.singleton(registration) : Lookup.EMPTY;
        }

        /**
         * Reference ID, usually derived from widget category and filename (eg.
         * {@code Main/Widget1}) and suitable for use with {@link Panel#id()}.
         *
         * @return reference ID
         */
        public String id() {
            return id;
        }

        /**
         * The widget instance.
         *
         * @return widget
         */
        public DashboardWidget widget() {
            return widget;
        }

        @Override
        public Lookup getLookup() {
            return lookup;
        }

    }

}
