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

package org.netbeans.modules.python.project.ui;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.prefs.Preferences;
import org.openide.util.NbPreferences;

/**
 * Preferences for the module.
 * <p>
 * <b>This is copied from the corresponding Java action in java.projects (JavaProjectSettings)</b>
 * </p>
 *
 */
public class PythonProjectSettings {

    private PythonProjectSettings() {}

    private static final PropertyChangeSupport pcs = new PropertyChangeSupport(PythonProjectSettings.class);

    /**
     * The package view should be displayed as a list of packages.
     */
    public static final int TYPE_PACKAGE_VIEW = 0;

    /**
     * The package view should be displayed as a tree of folders.
     */
    public static final int TYPE_TREE = 1;

    public static final String PROP_PACKAGE_VIEW_TYPE = "packageViewType"; //NOI18N
//    private static final String PROP_SHOW_AGAIN_BROKEN_REF_ALERT = "showAgainBrokenRefAlert"; //NOI18N

    private static Preferences prefs() {
        return NbPreferences.forModule(PythonProjectSettings.class);
    }

    /**
     * Returns how the package view should be displayed.
     * @return {@link #TYPE_PACKAGE_VIEW} or {@link #TYPE_TREE}
     */
    public static int getPackageViewType() {
        return prefs().getInt(PROP_PACKAGE_VIEW_TYPE, TYPE_PACKAGE_VIEW);
    }

    /**
     * Sets how the package view should be displayed.
     * @param type either {@link #TYPE_PACKAGE_VIEW} or {@link #TYPE_TREE}
     */
    public static void setPackageViewType(int type) {
        int currentType = getPackageViewType();
        if (currentType != type) {
            prefs().putInt(PROP_PACKAGE_VIEW_TYPE, type);
            pcs.firePropertyChange(PROP_PACKAGE_VIEW_TYPE, currentType, type);
        }
    }

//    public static boolean isShowAgainBrokenRefAlert() {
//        return prefs().getBoolean(PROP_SHOW_AGAIN_BROKEN_REF_ALERT, true);
//    }
//
//    public static void setShowAgainBrokenRefAlert(boolean again) {
//        prefs().putBoolean(PROP_SHOW_AGAIN_BROKEN_REF_ALERT, again);
//    }

    public static void addPropertyChangeListener(PropertyChangeListener l) {
        pcs.addPropertyChangeListener(l);
    }

    public static void removePropertyChangeListener(PropertyChangeListener l) {
        pcs.removePropertyChangeListener(l);
    }

}
