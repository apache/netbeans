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

package org.netbeans.modules.java.project.ui;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.prefs.Preferences;
import org.openide.util.NbPreferences;

/**
 * Preferences for the module.
 * @author Tomas Zezula, Jesse Glick
 */
public class JavaProjectSettings {

    private JavaProjectSettings() {}

    private static final PropertyChangeSupport pcs = new PropertyChangeSupport(JavaProjectSettings.class);

    public enum PackageViewType {
        /**
         * The package view should be displayed as a list of packages.
         */
        PACKAGES,
        /**
         * The package view should be displayed as a tree of folders.
         */
        TREE,
        /**
         * #53192: the package view should be displayed as a tree of folders with unique subcomponents collapsed.
         */
        REDUCED_TREE;
    }

    public static final String PROP_PACKAGE_VIEW_TYPE = "packageViewType"; //NOI18N
    private static final String PROP_SHOW_AGAIN_BROKEN_REF_ALERT = "showAgainBrokenRefAlert"; //NOI18N

    private static Preferences prefs() {
        return NbPreferences.forModule(JavaProjectSettings.class);
    }

    /**
     * Returns how the package view should be displayed.
     */
    public static PackageViewType getPackageViewType() {
        int type = prefs().getInt(PROP_PACKAGE_VIEW_TYPE, -1);
        PackageViewType[] types = PackageViewType.values();
        return type >= 0 && type < types.length ? types[type] : PackageViewType.PACKAGES;
    }

    /**
     * Sets how the package view should be displayed.
     */
    public static void setPackageViewType(PackageViewType type) {
        PackageViewType currentType = getPackageViewType();
        if (currentType != type) {
            prefs().putInt(PROP_PACKAGE_VIEW_TYPE, type.ordinal());
            pcs.firePropertyChange(PROP_PACKAGE_VIEW_TYPE, currentType, type);
        }
    }

    public static boolean isShowAgainBrokenRefAlert() {
        return prefs().getBoolean(PROP_SHOW_AGAIN_BROKEN_REF_ALERT, true);
    }

    public static void setShowAgainBrokenRefAlert(boolean again) {
        prefs().putBoolean(PROP_SHOW_AGAIN_BROKEN_REF_ALERT, again);
    }

    public static void addPropertyChangeListener(PropertyChangeListener l) {
        pcs.addPropertyChangeListener(l);
    }

    public static void removePropertyChangeListener(PropertyChangeListener l) {
        pcs.removePropertyChangeListener(l);
    }

}
