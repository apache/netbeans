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
package org.netbeans.core.multitabs;

import org.netbeans.core.multitabs.prefs.SettingsImpl;

/**
 * Read-only access to multi-tab settings.
 *
 * @author S. Aubrecht
 */
public final class Settings {

    private final SettingsImpl impl = new SettingsImpl();
    private static Settings theInstance;

    private Settings() {
    }

    /**
     * @return The one and only instance.
     */
    public static Settings getDefault() {
        synchronized( Settings.class ) {
            if( null == theInstance ) {
                theInstance = new Settings();
            }
        }
        return theInstance;
    }

    /**
     * @return True if multi-tabs are enabled, false otherwise.
     */
    public boolean isEnabled() {
        return impl.isEnabled();
    }

    /**
     * @return One of JTabbedPane.TOP BOTTOM LEFT or RIGHT constants.
     * @see javax.swing.JTabbedPane#getTabPlacement() 
     */
    public int getTabsLocation() {
        return impl.getTabsLocation();
    }

    /**
     * @return True to show full file path of currently selected document.
     */
    public boolean isShowFullPath() {
        return impl.isShowFullPath();
    }

    /**
     * @return True to use same color for all tabs from the same project.
     */
    public boolean isSameProjectSameColor() {
        return impl.isSameProjectSameColor();
    }

    /**
     * @return True to sort drop-down document list by project.
     */
    public boolean isSortDocumentListByProject() {
        return impl.isSortDocumentListByProject();
    }

    /**
     * @return True to show one row of tabs per project.
     */
    public boolean isTabRowPerProject() {
        return impl.isTabRowPerProject();
    }

    public boolean isShowFolderName() {
        return impl.isShowFolderName();
    }

    /**
     * @return Maximum tab row count.
     */
    public int getRowCount() {
        return impl.getRowCount();
    }
}
