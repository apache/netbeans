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

package org.netbeans.modules.versioning.util;

import java.awt.Color;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Bridge between options panel and versioning systems.
 * Versioning systems should implement this if they want to display colors in Options > Fonts and Colors and handle their changes.
 * 
 * @author ondra
 */
public abstract class OptionsPanelColorProvider {

    private static final String colorNamePrefix = "annotationFormat."; //NOI18N
    private final HashMap<String, AnnotationFormat> colors;

    protected OptionsPanelColorProvider () {
        colors = new HashMap<String, AnnotationFormat>();
    }

    /**
     * Returns the name of the versioning system displayed in the list in the options panel.
     * @return the name of the versioning system
     */
    public abstract String getName ();

    /**
     * Returns colors for the versioning system.
     * @return colors for the versioning system
     */
    public final Map<String, Color[]> getColors() {
        synchronized (colors) {
            Map<String, Color[]> outputColors = new HashMap<String, Color[]>(colors.size());
            for (Map.Entry<String, AnnotationFormat> e : colors.entrySet()) {
                AnnotationFormat af = e.getValue();
                outputColors.put(af.getDisplayName(), new Color[] {af.getActualColor(), af.getDefaultColor()});
            }
            return outputColors;
        }
    }

    /**
     * Handles modifications made to colors in the options panel.
     * Implement this to handle these changes and persist the new color values.
     * @param newColors modified colors
     */
    public void colorsChanged(Map<String, Color> newColors) {
        synchronized (colors) {
            for (Map.Entry<String, Color> e : newColors.entrySet()) {
                AnnotationFormat af = colors.get(e.getKey());
                if (af != null) {
                    af.setActualColor(e.getValue());
                }
            }
            saveColors(colors.values());
        }
    }

    /**
     * Returns hexa representation of the given value in two hexa-digits
     * @param val value to be coded in hexa
     * @return hexa representation of the given value
     */
    protected static final String to2Hex (int val) {
        String hexStr = Integer.toHexString(val & 0xff);
        if (hexStr.length() == 1) {
            hexStr = "0" + hexStr; //NOI18N
        }
        return hexStr;
    }

    /**
     * Full key used mainly in preferences
     * @param annotationKey
     * @return
     */
    protected static String getColorKey (String annotationKey) {
        return colorNamePrefix + annotationKey;
    }

    /**
     * Creates new annotation
     * @param formatKey key used as a part of the final key in preferences
     * @param displayName diaplsy name of the annotation, e.g. 'Locally modified', will be displayed in the option dialog
     * @param defaultColor default color, can be null
     * @param isTooltip node label or TT
     * @return
     */
    protected final AnnotationFormat createAnnotationFormat (String formatKey, String displayName, Color defaultColor, boolean isTooltip) {
        return new AnnotationFormat(formatKey, displayName, defaultColor, defaultColor == null ? null : getSavedColor(getColorKey(formatKey), defaultColor), isTooltip);
    }

    /**
     * Puts the given annotation amongst configurable annotations which will be displayed in the option dialog
     * @param af
     */
    protected void putColor (AnnotationFormat af) {
        colors.put(af.getDisplayName(), af);
    }

    /**
     * Returns persisted color
     * @param key
     * @param defaultColor
     * @return
     */
    protected abstract Color getSavedColor (String key, Color defaultColor);

    /**
     * Creates annotation format for annotators
     * @param color
     * @param isTooltip
     * @return
     */
    protected abstract MessageFormat createFormat (Color color, boolean isTooltip);

    /**
     * Persists colors
     * @param colors
     */
    protected abstract void saveColors (Collection<AnnotationFormat> colors);

    public class AnnotationFormat {

        private final String displayName;
        private final boolean tooltip;
        private final String key;
        private final Color defaultColor;
        private Color actualColor;
        private MessageFormat format;

        private AnnotationFormat (String key, String displayName, Color defaultColor, Color actualColor, boolean tooltip) {
            this.key = key;
            this.displayName = displayName;
            this.defaultColor = defaultColor;
            this.tooltip = tooltip;
            setActualColor(actualColor);
        }

        /**
         * TT or node label
         * @return
         */
        public boolean isTooltip() {
            return tooltip;
        }

        /**
         * Display name of the annotation format (e.g. Modified)
         * @return
         */
        public String getDisplayName() {
            return displayName;
        }

        /**
         * Default color for the annotation
         * @return
         */
        public Color getDefaultColor() {
            return defaultColor;
        }

        /**
         * Key for the annotation, used mainly as a key in preferences
         * @return
         */
        public String getKey() {
            return key;
        }

        /**
         * Returns the format of the node label or TT for annotators
         * @return
         */
        public synchronized MessageFormat getFormat() {
            if (format == null) {
                Color color = getActualColor();
                format = createFormat(color, isTooltip());
            }
            return format;
        }

        /**
         * Current color used in the annotation
         * @return
         */
        public Color getActualColor() {
            return actualColor;
        }

        private synchronized void setActualColor(Color color) {
            this.actualColor = color == null ? defaultColor : color;
            format = null;
        }
    }
}
