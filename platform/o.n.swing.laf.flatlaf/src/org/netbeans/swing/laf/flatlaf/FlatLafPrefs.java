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
package org.netbeans.swing.laf.flatlaf;

import java.util.prefs.Preferences;
import org.openide.util.NbPreferences;

/**
 *
 * @author Karl Tauber
 */
class FlatLafPrefs {

    private static final String USE_WINDOW_DECORATIONS = "useWindowDecorations";
    private static final String UNIFIED_TITLE_BAR = "unifiedTitleBar";
    private static final String MENU_BAR_EMBEDDED = "menuBarEmbedded";
    private static final String UNDERLINE_MENU_SELECTION = "underlineMenuSelection";
    private static final String ALWAYS_SHOW_MNEMONICS = "alwaysShowMnemonics";

    private static final Preferences prefs = NbPreferences.forModule(FlatLafPrefs.class);

    static boolean isUseWindowDecorations() {
        return prefs.getBoolean(USE_WINDOW_DECORATIONS, true);
    }

    static void setUseWindowDecorations(boolean value) {
        putBoolean(USE_WINDOW_DECORATIONS, value, true);
    }

    static boolean isUnifiedTitleBar() {
        return prefs.getBoolean(UNIFIED_TITLE_BAR, true);
    }

    static void setUnifiedTitleBar(boolean value) {
        putBoolean(UNIFIED_TITLE_BAR, value, true);
    }

    static boolean isMenuBarEmbedded() {
        return prefs.getBoolean(MENU_BAR_EMBEDDED, true);
    }

    static void setMenuBarEmbedded(boolean value) {
        putBoolean(MENU_BAR_EMBEDDED, value, true);
    }

    static boolean isUnderlineMenuSelection() {
        return prefs.getBoolean(UNDERLINE_MENU_SELECTION, false);
    }

    static void setUnderlineMenuSelection(boolean value) {
        putBoolean(UNDERLINE_MENU_SELECTION, value, false);
    }

    static boolean isAlwaysShowMnemonics() {
        return prefs.getBoolean(ALWAYS_SHOW_MNEMONICS, false);
    }

    static void setAlwaysShowMnemonics(boolean value) {
        putBoolean(ALWAYS_SHOW_MNEMONICS, value, false);
    }

    private static void putBoolean(String key, boolean value, boolean def) {
        if (value != def) {
            prefs.putBoolean(key, value);
        } else {
            prefs.remove(key);
        }
    }
}
