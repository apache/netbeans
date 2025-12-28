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
package org.netbeans.api.dashboard;

import java.awt.EventQueue;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import org.netbeans.modules.dashboard.DefaultDashboardDisplayer;
import org.netbeans.spi.dashboard.DashboardDisplayer;
import org.netbeans.spi.dashboard.DashboardWidget;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * Provides API access to control the dashboard.
 * <p>
 * Currently only provides support to load and display the main dashboard panel
 * with widgets registered in {@code Dashboard/Main}.
 */
public final class DashboardManager {

    private static final DashboardManager INSTANCE = new DashboardManager();

    private List<DashboardDisplayer.WidgetReference> mainWidgets;
    private DashboardDisplayer mainDisplayer;

    /**
     * Show the main widget dashboard.
     */
    public void show() {
        if (!EventQueue.isDispatchThread()) {
            EventQueue.invokeLater(this::show);
            return;
        }
        if (mainDisplayer == null) {
            mainDisplayer = findMainDisplayer();
        }
        if (mainWidgets == null) {
            mainWidgets = findMainWidgets();
        }
        mainDisplayer.show("Main", mainWidgets);
    }

    private static DashboardDisplayer findMainDisplayer() {
        return DefaultDashboardDisplayer.findOrDefault();
    }

    private static List<DashboardDisplayer.WidgetReference> findMainWidgets() {
        FileObject folder = FileUtil.getConfigFile("Dashboard/Main");
        if (folder == null) {
            return List.of();
        }

        Collection<FileObject> files = FileUtil.getOrder(Arrays.asList(folder.getChildren()), true);
        List<DashboardDisplayer.WidgetReference> widgetRefs = new ArrayList<>();

        for (FileObject file : files) {
            String id = "Main/" + file.getName();
            DashboardWidget widget = FileUtil.getConfigObject(file.getPath(), DashboardWidget.class);
            if (widget != null) {
                widgetRefs.add(new DashboardDisplayer.WidgetReference(id, widget, file));
            }
        }

        return List.copyOf(widgetRefs);
    }

    /**
     * Acquire the default dashboard manager.
     *
     * @return default dashboard manager
     */
    public static DashboardManager getDefault() {
        return INSTANCE;
    }

}
