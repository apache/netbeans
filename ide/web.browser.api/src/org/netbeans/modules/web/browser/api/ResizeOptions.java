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
package org.netbeans.modules.web.browser.api;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;

/**
 * Manager for value classes describing a single button to resize web browser window.
 * @see ResizeOption
 */
public final class ResizeOptions {

    private static final Logger LOGGER = Logger.getLogger(ResizeOptions.class.getName());

    private static final ResizeOptions INSTANCE = new ResizeOptions();


    private ResizeOptions() {
    }

    public static ResizeOptions getDefault() {
        return INSTANCE;
    }

    /**
     * Loads all instances from persistent storage.
     * @return list of {@link ResizeOption}s
     */
    public synchronized List<ResizeOption> loadAll() {
        Preferences prefs = getPreferences();
        int count = prefs.getInt("count", 0); // NOI18N
        List<ResizeOption> res = new ArrayList<ResizeOption>(count);
        if (count == 0) {
            res.add(ResizeOption.create(ResizeOption.Type.DESKTOP, NbBundle.getMessage(ResizeOption.class, "Lbl_DESKTOP"),
                    1280, 1024, true, true));
            res.add(ResizeOption.create(ResizeOption.Type.TABLET_LANDSCAPE, NbBundle.getMessage(ResizeOption.class, "Lbl_TABLET_LANDSCAPE"),
                    1024, 768, true, true));
            res.add(ResizeOption.create(ResizeOption.Type.TABLET_PORTRAIT, NbBundle.getMessage(ResizeOption.class, "Lbl_TABLET_PORTRAIT"),
                    768, 1024, true, true));
            res.add(ResizeOption.create(ResizeOption.Type.SMARTPHONE_LANDSCAPE, NbBundle.getMessage(ResizeOption.class, "Lbl_SMARTPHONE_LANDSCAPE"),
                    480, 320, true, true));
            res.add(ResizeOption.create(ResizeOption.Type.SMARTPHONE_PORTRAIT, NbBundle.getMessage(ResizeOption.class, "Lbl_SMARTPHONE_PORTRAIT"),
                    320, 480, true, true));
            res.add(ResizeOption.create(ResizeOption.Type.WIDESCREEN, NbBundle.getMessage(ResizeOption.class, "Lbl_WIDESCREEN"),
                    1680, 1050, false, true));
            res.add(ResizeOption.create(ResizeOption.Type.NETBOOK, NbBundle.getMessage(ResizeOption.class, "Lbl_NETBOOK"),
                    1024, 600, false, true));
        } else {
            for (int i = 0; i < count; i++) {
                Preferences node = prefs.node("option" + i); // NOI18N
                ResizeOption option = load(node);
                if (option != null) {
                    res.add(option);
                }
            }
        }
        return res;
    }

    /**
     * Persists the given options.
     * @param options list of {@link ResizeOption}s
     */
    public synchronized void saveAll(List<ResizeOption> options) {
        Preferences prefs = getPreferences();
        int count = prefs.getInt("count", 0); // NOI18N
        try {
            for (int i = 0; i < count; i++) {
                prefs.node("option" + i).removeNode(); // NOI18N
            }
        } catch (BackingStoreException e) {
            LOGGER.log(Level.FINE, null, e);
        }
        prefs.putInt("count", options.size()); // NOI18N
        for (int i = 0; i < options.size(); i++) {
            Preferences node = prefs.node("option" + i); // NOI18N
            save(node, options.get(i));
        }
    }

    private void save(Preferences prefs, ResizeOption option) {
        prefs.put("displayName", option.getDisplayName()); // NOI18N
        prefs.putInt("width", option.getWidth()); // NOI18N
        prefs.putInt("height", option.getHeight()); // NOI18N
        prefs.putBoolean("toolbar", option.isShowInToolbar()); // NOI18N
        prefs.putBoolean("default", option.isDefault()); // NOI18N
        prefs.put("type", option.getType().name()); // NOI18N
    }

    private ResizeOption load(Preferences prefs) {
        String name = prefs.get("displayName", null); // NOI18N
        int width = prefs.getInt("width", -1); // NOI18N
        int height = prefs.getInt("height", -1); // NOI18N
        boolean toolbar = prefs.getBoolean("toolbar", false); // NOI18N
        boolean isDefault = prefs.getBoolean("default", false); // NOI18N
        ResizeOption.Type type = ResizeOption.Type.valueOf(prefs.get("type", null)); // NOI18N
        try {
            return ResizeOption.create(type, name, width, height, toolbar, isDefault);
        } catch (IllegalArgumentException e) {
            LOGGER.log(Level.WARNING, "Error while loading resize options.", e); // NOI18N
        }
        return null;
    }

    private Preferences getPreferences() {
        return NbPreferences.forModule(ResizeOptions.class).node("resize_options"); // NOI18N
    }

}
