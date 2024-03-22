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

/**
 * A widget for display on the dashboard. A widget does not control its own
 * rendering, but provides a title and list of elements that it wants to be
 * rendered by the {@link DashboardDisplayer}. This allows for uniform design,
 * different rendering in different contexts, and rendering that does not rely
 * on Swing.
 * <p>
 * All widget instances should be registered in {@code Dashboard/Widgets} and
 * linked from the dashboard categories required, such as
 * {@code Dashboard/Main}. Only one widget instance should exist, but it might
 * be shown in multiple places, on one dashboard or many. The
 * {@link DashboardDisplayer.Panel} passed in to methods allows the widget to
 * provide contextual information where necessary. The panel may also be used to
 * mark the widget as invalid, where the title and elements need to be
 * re-requested and re-rendered.
 * <p>
 * Various optional hooks are provided so that a widget can hook into its
 * display. The
 * {@link #attach(org.netbeans.spi.dashboard.DashboardDisplayer.Panel)} hook
 * will be called when a widget is first being prepared for display on a
 * particular panel. It might be used to allocate expensive resources. The
 * {@link #showing(org.netbeans.spi.dashboard.DashboardDisplayer.Panel)} and
 * {@link #hidden(org.netbeans.spi.dashboard.DashboardDisplayer.Panel)} hooks
 * will be called when a particular panel is opened or closed. If using these
 * for attaching listeners, remember that a widget may be showing in more than
 * one place - store the panel in a set and use set emptiness to control. The
 * {@link #removed(java.lang.String)} hook is called when a widget reference is
 * removed by the user. Configuration data attached to the ID might be cleared
 * up. A panel is not provided as one might not be in existence.
 */
public interface DashboardWidget {

    /**
     * The widget title. Empty text may be returned where no title is required,
     * which a displayer might render differently.
     *
     * @param panel display panel link
     * @return widget title
     */
    public String title(DashboardDisplayer.Panel panel);

    /**
     * List of elements to be rendered for this widget.
     *
     * @param panel display panel link
     * @return list of elements
     */
    public List<WidgetElement> elements(DashboardDisplayer.Panel panel);

    /**
     * Optional hook called when the widget is first being attached to a
     * particular panel in any run of the application. This method is guaranteed
     * to be called before the title or elements are requested.
     *
     * @param panel display panel link
     */
    public default void attach(DashboardDisplayer.Panel panel) {
    }

    /**
     * Optional hook called when the widget is made visible on a particular
     * panel.
     *
     * @param panel display panel link
     */
    public default void showing(DashboardDisplayer.Panel panel) {
    }

    /**
     * Optional hook called when the widget is hidden on a particular panel.
     *
     * @param panel display panel link
     */
    public default void hidden(DashboardDisplayer.Panel panel) {
    }

    /**
     * Optional hook called when a widget reference is unregistered by the user.
     * The ID will match that which would be provided by the panel - the panel
     * might not exist. Any configuration data stored linked to the panel ID
     * should be disposed of.
     *
     * @param id widget registration / panel ID
     */
    public default void removed(String id) {
    }

}
