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
package org.netbeans.modules.markdown.utils;

import java.awt.Color;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.swing.text.AttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.html.StyleSheet;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.modules.editor.settings.storage.api.EditorSettings;
import org.netbeans.api.editor.settings.FontColorSettings;
import org.netbeans.modules.editor.settings.storage.api.FontColorSettingsFactory;
import org.netbeans.modules.markdown.MarkdownDataObject;
import org.openide.util.NbBundle;

/**
 *
 * @author bhaidu
 */
public class StyleUtils {

    private static final Map<String, String> fontConfig2tagMapping = Map.ofEntries(
        Map.entry("body", "body"), // NOI18N
        Map.entry("code", "code"), // NOI18N
        Map.entry("pre", "pre"), // NOI18N
        Map.entry("blockquote", "blockquote p"), // NOI18N
        Map.entry("heading1", "h1, h1 a"), // NOI18N
        Map.entry("heading2", "h2, h2 a"), // NOI18N
        Map.entry("heading3", "h3, h3 a"), // NOI18N
        Map.entry("heading4", "h4, h4 a"), // NOI18N
        Map.entry("heading5", "h5, h5 a"), // NOI18N
        Map.entry("heading6", "h6, h6 a") // NOI18N
    );

    public static void addNbSyles(StyleSheet ss) {

        //main css
        appendDefaultMarkdownCssRules(ss);

        //coloring & font settings
        appendColoringCssRulesFromFontConfigs(ss);
    }

    public static void addRule(StyleSheet ss, AttributeSet attributeSet, AttributeSet defaultAttributes) {
        String nameAttr = (String) attributeSet.getAttribute(StyleConstants.NameAttribute);

        String htmlTag = fontConfig2tagMapping.get(nameAttr);

        if (htmlTag == null) {
            return;
        }

        StringBuilder cssRule = new StringBuilder();
        cssRule.append(htmlTag);
        cssRule.append(" {");  // NOI18N
        cssRule.append(rgbStyling("color", getThemeColor(attributeSet, defaultAttributes, StyleConstants.ColorConstants.Foreground)));  // NOI18N
        cssRule.append(rgbStyling("background-color", getThemeColor(attributeSet, defaultAttributes, StyleConstants.ColorConstants.Background)));  // NOI18N
        cssRule.append("}");  // NOI18N
        ss.addRule(cssRule.toString());
    }

    public static Color getThemeColor(AttributeSet attributeSet, AttributeSet defaultAttributes, Object constant) {
        Color color = (Color) attributeSet.getAttribute(constant);
        if (color == null && defaultAttributes != null) {
            color = (Color) defaultAttributes.getAttribute(constant);
        }
        return color;
    }

    public static String rgbStyling(String property, Color color) {
        if (color == null) {
            return ""; // NOI18N
        }

        StringBuilder cssRule = new StringBuilder();
        cssRule.append(property);
        cssRule.append(":rgb(");  // NOI18N
        cssRule.append(color.getRed());
        cssRule.append(",");  // NOI18N
        cssRule.append(color.getGreen());
        cssRule.append(",");  // NOI18N
        cssRule.append(color.getBlue());
        cssRule.append(");");  // NOI18N
        return cssRule.toString();
    }

    private static void appendDefaultMarkdownCssRules(StyleSheet ss) {
        String defaultRules = NbBundle.getMessage(StyleUtils.class, "CSS_DEFAULT");  // NOI18N
        ss.addRule(defaultRules);
    }

    private static Iterator<AttributeSet> loadCustomizedMarkdownFontAttributes() {
        String profile = EditorSettings.getDefault().getCurrentFontColorProfile();
        FontColorSettingsFactory fcsf = EditorSettings.getDefault().getFontColorSettings(new String[]{MarkdownDataObject.MIME_TYPE});
        return fcsf.getAllFontColors(profile).iterator();
    }

    private static AttributeSet loadDefaultNbMarkdownFontAttributes() {
        FontColorSettings fcs = (MimeLookup.getLookup(MimePath.get(MarkdownDataObject.MIME_TYPE)).lookup(FontColorSettings.class));
        return fcs.getFontColors("default"); // NOI18N
    }

    private static void appendColoringCssRulesFromFontConfigs(StyleSheet ss) {
        //contains the default / fallback values of fontConfigs
        AttributeSet defaultAttributes = loadDefaultNbMarkdownFontAttributes();

        //contains the full list of fontConfigs but populated only with custom values
        Iterator<AttributeSet> attributesIt = loadCustomizedMarkdownFontAttributes();
        while (attributesIt.hasNext()) {
            AttributeSet attributeSet = attributesIt.next();
            Enumeration<?> en = attributeSet.getAttributeNames();

            while (en.hasMoreElements()) {
                Object key = en.nextElement();

                if (key instanceof StyleConstants) {
                    addRule(ss, attributeSet, defaultAttributes);
                }
            }
        }
    }
}
