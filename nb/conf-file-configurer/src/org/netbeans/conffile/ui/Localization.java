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
package org.netbeans.conffile.ui;

import org.netbeans.conffile.ui.comp.MarkableAAButton;
import org.netbeans.conffile.ui.comp.AACheckbox;
import org.netbeans.conffile.ui.comp.AALabel;
import org.netbeans.conffile.ui.comp.AARadioButton;
import org.netbeans.conffile.ui.comp.AATextArea;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.ResourceBundle.Control;
import java.util.function.Consumer;
import javax.swing.AbstractButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JRadioButton;
import javax.swing.JTextArea;

/**
 * Quick and dirty localization. See Bundle_en.properties in this package. Each
 * bundle entry gets an enum constant; the enum constant can be used as a
 * factory for components, or can be called to (re)configure them. Fails over to
 * the english bundle, rather than exploding, if a key is not present.
 *
 * @author Tim Boudreau
 */
public enum Localization {

    NEXT,
    BACK,
    CANCEL,
    FINISH,
    DIALOG_TITLE,
    // Config file panel
    LOCATE_CONFIG_FILE,
    CONFIGURATION_FILE,
    CONFIGURATION_FILES,
    SELECT,
    ERR_NO_FILE_SELECTED,
    ERR_NO_SUCH_FILE,
    ERR_IS_DIRECTORY,
    BROWSE,
    // AA mode panel
    SELECT_ANTIALIAS_MODE_TITLE,
    POINT_SIZE,
    SELECT_STYLE,
    // Font size panel
    FONT_SIZE_TITLE,
    // Choose fonts panel
    FONTS_TITLE,
    INCREASE_FONT_SIZE,
    DECREASE_FONT_SIZE,
    FONT_BUTTON_TEXT,
    SAMPLE_TEXT,
    // Memory panel
    MEMORY_PANEL_TITLE,
    MEMORY_INSTRUCTIONS,
    TOTAL_MEMORY,
    MAX_MEMORY_SIZE,
    MIN_MEMORY_SIZE,
    USE_THESE_SETTINGS,
    ERR_NO_MEMORY_SIZE,
    MEGABYTES, GIGABYTES, TERABYTES, KILOBYTES, BYTES,
    // Tweaks
    TWEAKS_TITLE,
    TWEAK_CONSOLE_LOGGER,
    TWEAK_OPENGL,
    TWEAK_STATUS_LINE_IN_MENU_BAR,
    YOUR_NAME,
    WHERE_NAME_IS_USED,
    USE_UTF_8,
    // Finish panel
    SAVE_CONFIGURATION_FILE,
    LINE_SWITCHES_INFO,
    REMOVED_SWITCHES_INFO,
    // Slider labels for memory
    MB_64,
    MB_128,
    MB_256,
    MB_512,
    MB_768,
    GB_1,
    GB_125,
    GB_15,
    GB_175,
    GB_2,
    GB_225,
    GB_25,
    GB_275,
    GB_3,
    // Cancel dialog
    CANCEL_DIALOG_TITLE,
    CANCEL_DIALOG_MESSAGE,
    YES, NO,
    // Choose monitor
    CHOOSE_MONITOR_TITLE,
    CHOOSE_MONITOR_TEXT,
    DONE,
    // Monitor type panel
    MONITOR_SIZE_TITLE,
    MONITOR_SIZE_QUESTION,
    LARGE_DESKTOP,
    SMALL_DESKTOP,
    LAPTOP_SCREEN,
    // LCD Panel
    MONITOR_TYPE_TITLE,
    MONITOR_TYPE_QUESTION,
    CURRENT_IS_MAX_RESOLUTION_CHECKBOX,

    // Hints
    OFF,
    HBGR,
    HRGB,
    VBGR,
    VRGB,
    GASP,
    DEFAULT,
    ON,
    LCD

    ;

    public String format(Object... args) {
        return MessageFormat.format(toString(), args);
    }

    @Override
    public String toString() {
        return findString(textKey());
    }

    public String tip() {
        return findString(tipKey());
    }

    public String desc() {
        return findString(descKey());
    }

    public String nm() {
        return findString(nameKey());
    }

    String textKey() {
        return name().toLowerCase();
    }

    String tipKey() {
        return name().toLowerCase() + "_tip";
    }

    String descKey() {
        return name().toLowerCase() + "_desc";
    }

    String nameKey() {
        return name().toLowerCase() + "_name";
    }

    <T extends JComponent> T applyTo(T comp, Consumer<String> c, Object... args) {
        String txt = findString(textKey());
        String tip = findString(tipKey());
        String desc = findString(descKey());
        String name = findString(nameKey());
        if (txt == null) {
            txt = "<no text for " + name() + ">";
        }
        if (args.length > 0) {
            txt = MessageFormat.format(txt, args);
        }
        c.accept(txt);
        if (desc != null) {
            comp.getAccessibleContext().setAccessibleDescription(desc);
        }
        if (name != null) {
            comp.getAccessibleContext().setAccessibleName(name);
        }
        if (tip != null) {
            comp.setToolTipText(tip);
        }
        return comp;
    }

    public <T extends JLabel> T set(T on, Object... args) {
        return applyTo(on, on::setText, args);
    }

    public <T extends AbstractButton> T set(T on, Object... args) {
        return applyTo(on, on::setText, args);
    }

    public <T extends JTextArea> T set(T on, Object... args) {
        return applyTo(on, on::setText, args);
    }

    public <T extends JCheckBox> T set(T on, Object... args) {
        return applyTo(on, on::setText, args);
    }

    public JLabel label(Object... args) {
        return set(new AALabel(), args);
    }

    public MarkableAAButton button(Object... args) {
        return set(new MarkableAAButton(), args);
    }

    public JCheckBox checkbox(Object... args) {
        return set(new AACheckbox(), args);
    }

    public JRadioButton radioButton(Object... args) {
        return set(new AARadioButton(), args);
    }

    public JTextArea textArea(Object... args) {
        return set(new AATextArea(), args);
    }

    String findString(String key) {
        ResourceBundle first = null;
        try {
            first = bundle();
        } catch (MissingResourceException mre) {

        }
        if (first != null && first.containsKey(key)) {
            return first.getString(key);
        }
        ResourceBundle second = null;
        try {
            second = fallback();
        } catch (MissingResourceException mre) {

        }
        if (second != null && second.containsKey(key)) {
            return second.getString(key);
        }
        return null;
    }

    static ResourceBundle fallback;
    static ResourceBundle local;
    static boolean noFallback;

    static ResourceBundle fallback() {
        if (fallback != null) {
            return fallback;
        }
        ResourceBundle result
                = ResourceBundle.getBundle(Localization.class.getName(), Locale.US, new Ctl());
        if (result == null) {
            noFallback = true;
        }
        return fallback = result;
    }

    static ResourceBundle bundle() {
        if (local != null) {
            return local;
        }
        return local = ResourceBundle.getBundle(Localization.class.getName(), Locale.getDefault(), new Ctl());
    }

    static class Ctl extends Control {

        @Override
        public String toBundleName(String baseName, Locale locale) {
            return Localization.class.getPackage().getName() + ".Bundle_" + locale;
        }
    }
}
