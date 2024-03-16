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
package org.netbeans.swing.laf.flatlaf;

import com.formdev.flatlaf.util.SystemInfo;
import java.awt.Color;
import java.util.prefs.Preferences;
import org.openide.util.NbPreferences;

/**
 *
 * @author Karl Tauber
 */
class FlatLafPrefs {

    private static final String ACCENT_COLOR = "accentColor";
    private static final String USE_WINDOW_DECORATIONS = "useWindowDecorations";
    private static final String UNIFIED_TITLE_BAR = "unifiedTitleBar";
    private static final String MENU_BAR_EMBEDDED = "menuBarEmbedded";
    private static final String UNDERLINE_MENU_SELECTION = "underlineMenuSelection";
    private static final String ALWAYS_SHOW_MNEMONICS = "alwaysShowMnemonics";
    private static final String SHOW_FILECHOOSER_FAVORITES = "showFileChooserFavorites";

    private static final Preferences prefs = NbPreferences.forModule(FlatLafPrefs.class);

    private static final boolean DEF_USE_WINDOW_DECORATIONS = SystemInfo.isWindows_10_orLater;

    static Color getAccentColor() {
        return parseColor(prefs.get(ACCENT_COLOR, null));
    }

    static Color parseColor(String s) {
        try {
            return (s != null && s.startsWith("#"))
                    ? new Color(Integer.parseInt(s.substring(1), 16))
                    : null;
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    static void setAccentColor(Color accentColor) {
        if (accentColor != null) {
            prefs.put(ACCENT_COLOR, String.format("#%06x", accentColor.getRGB() & 0xffffff));
        } else {
            prefs.remove(ACCENT_COLOR);
        }
    }

    static boolean isUseWindowDecorations() {
        return prefs.getBoolean(USE_WINDOW_DECORATIONS, DEF_USE_WINDOW_DECORATIONS);
    }

    static void setUseWindowDecorations(boolean value) {
        putBoolean(USE_WINDOW_DECORATIONS, value, DEF_USE_WINDOW_DECORATIONS);
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

    static boolean isShowFileChooserFavorites() {
        return prefs.getBoolean(SHOW_FILECHOOSER_FAVORITES, false);
    }

    static void setShowFileChooserFavorites(boolean value) {
        putBoolean(SHOW_FILECHOOSER_FAVORITES, value, false);
    }

    private static void putBoolean(String key, boolean value, boolean def) {
        if (value != def) {
            prefs.putBoolean(key, value);
        } else {
            prefs.remove(key);
        }
    }
}
