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

package org.netbeans.modules.nashorn.execution;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.modules.java.api.common.ui.PlatformFilter;
import org.netbeans.modules.nashorn.execution.options.Settings;
import org.openide.modules.SpecificationVersion;
import org.openide.util.WeakListeners;

/**
 *
 * @author Martin
 */
public class NashornPlatform {

    private static final String NASHORN_PLATFORM_VERSION = "1.8";   // NOI18N
    private static final SpecificationVersion SMALLEST_VERSION = new SpecificationVersion(NASHORN_PLATFORM_VERSION);

    private static NashornPlatform INSTANCE;

    private volatile JavaPlatform nashornPlatform;
    private final PlatformManagerListener platformListener;
    private final Set<ChangeListener> listeners = new HashSet<>();

    private NashornPlatform() {
        JavaPlatformManager jpm = JavaPlatformManager.getDefault();
        platformListener = new PlatformManagerListener();
        jpm.addPropertyChangeListener(WeakListeners.propertyChange(platformListener, jpm));
        Settings.getPreferences().addPreferenceChangeListener(
                WeakListeners.create(PreferenceChangeListener.class, platformListener, Settings.getPreferences()));
        nashornPlatform = getNashornPlatform(jpm);
    }

    public static synchronized NashornPlatform getDefault() {
        if (INSTANCE == null) {
            INSTANCE = new NashornPlatform();
        }
        return INSTANCE;
    }

    /**
     * @return The platform or <code>null</code>.
     */
    public JavaPlatform getPlatform() {
        return nashornPlatform;
    }

    private static SpecificationVersion getPlatformVersion(JavaPlatform p) {
        if (p == null) {
            return new SpecificationVersion("1.8"); // NOI18N
        }
        return p.getSpecification().getVersion();
    }

    public static boolean isNashornSupported(JavaPlatform p) {
        SpecificationVersion version = getPlatformVersion(p);
        if (version.equals(SMALLEST_VERSION)) {
            return true;
        }
        if (version.compareTo(new SpecificationVersion("15")) < 0) { // NOI18N
            return true;
        }
        return false;
    }

    public static boolean isGraalJsSupported(JavaPlatform p) {
        SpecificationVersion version = getPlatformVersion(p);
        if (version.compareTo(SMALLEST_VERSION) < 0) {
            return false;
        }
        return true;
    }

    public static boolean isGraalJSPreferred(JavaPlatform p) {
        return p != null && p.findTool("node") != null; // NOI18N
    }

    public void addChangeListener(ChangeListener chl) {
        synchronized (listeners) {
            listeners.add(chl);
        }
    }

    public void removeChangeListener(ChangeListener chl) {
        synchronized (listeners) {
            listeners.remove(chl);
        }
    }

    private void fireChangeEvent() {
        ChangeEvent che = new ChangeEvent(this);
        List<ChangeListener> listeners_;
        synchronized (listeners) {
            listeners_ = new ArrayList<>(listeners);
        }
        for (ChangeListener chl : listeners_) {
            chl.stateChanged(che);
        }
    }

    private static JavaPlatform getNashornPlatform(JavaPlatformManager platformManager) {
        String nashornPlatformDisplayName = Settings.getPreferences().get(Settings.PREF_NASHORN_PLATFORM_DISPLAY_NAME, null);
        SpecificationVersion smallestVersion = new SpecificationVersion(NASHORN_PLATFORM_VERSION);
        JavaPlatform defaultPlatform = platformManager.getDefaultPlatform();
        SpecificationVersion version = defaultPlatform.getSpecification().getVersion();
        if (version.compareTo(smallestVersion) >= 0) {
            if (nashornPlatformDisplayName == null ||
                nashornPlatformDisplayName.equals(defaultPlatform.getDisplayName())) {
                return defaultPlatform;
            }
        }
        // Check the rest:
        for (JavaPlatform jp : platformManager.getInstalledPlatforms()) {
            if (jp.getSpecification().getVersion().compareTo(smallestVersion) >= 0) {
                if (nashornPlatformDisplayName == null ||
                    nashornPlatformDisplayName.equals(jp.getDisplayName())) {

                    if (!jp.getInstallFolders().isEmpty()) { // Check for broken platforms
                        return jp;
                    }
                }
            }
        }
        if (nashornPlatformDisplayName != null) {
            // Did not find the platform that we have in settings,
            // choose some suitable one instead:
            if (version.compareTo(smallestVersion) >= 0) {
                return defaultPlatform;
            }
            // The default is insufficient, check the rest:
            for (JavaPlatform jp : platformManager.getInstalledPlatforms()) {
                if (jp.getSpecification().getVersion().compareTo(smallestVersion) >= 0) {
                    if (!jp.getInstallFolders().isEmpty()) { // Check for broken platforms
                        return jp;
                    }
                }
            }
        }
        return null;
    }

    public void setPlatform(JavaPlatform selectedPlatform) {
        if (selectedPlatform == null) {
            Settings.getPreferences().remove(Settings.PREF_NASHORN_PLATFORM_DISPLAY_NAME);
        } else {
            if (!isJsJvmPlatform(selectedPlatform)) {
                throw new IllegalArgumentException(selectedPlatform.getDisplayName());
            }
            Settings.getPreferences().put(Settings.PREF_NASHORN_PLATFORM_DISPLAY_NAME,
                                          selectedPlatform.getDisplayName());
        }
        nashornPlatform = selectedPlatform;
    }

    public static boolean isJsJvmPlatform(JavaPlatform platform) {
        return platform.getSpecification().getVersion().compareTo(SMALLEST_VERSION) >= 0;
    }

    public static PlatformFilter getFilter() {
        return new PlatformFilter() {
            @Override
            public boolean accept(JavaPlatform platform) {
                return isJsJvmPlatform(platform);
            }
        };
    }

    private class PlatformManagerListener implements PropertyChangeListener,
                                                     PreferenceChangeListener {

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            nashornPlatform = getNashornPlatform(JavaPlatformManager.getDefault());
            fireChangeEvent();
        }

        @Override
        public void preferenceChange(PreferenceChangeEvent evt) {
            if (Settings.PREF_NASHORN_PLATFORM_DISPLAY_NAME.equals(evt.getKey())) {
                nashornPlatform = getNashornPlatform(JavaPlatformManager.getDefault());
                fireChangeEvent();
            }
        }

    }

}
