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
package org.netbeans.modules.markdown;

import java.awt.Color;
import java.util.Collection;
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
import org.openide.util.NbBundle;

/**
 *
 * @author bhaidu
 */
public class StyleUtils {

    private static final Map<String, String> fontConfig2tagMapping = new HashMap<String, String>() {
        {
            put("body", "body");
            put("code", "code");
            put("pre", "pre");
            put("heading1", "h1, h1 a");
            put("heading2", "h2, h2 a");
            put("heading3", "h3, h3 a");
            put("heading4", "h4, h4 a");
            put("heading5", "h5, h5 a");
            put("heading6", "h6, h6 a");
        }
    };

    public static void addNbSyles(StyleSheet ss) {
        String profile = EditorSettings.getDefault().getCurrentFontColorProfile();
        String defaultRules = NbBundle.getMessage(StyleUtils.class, "CSS_DEFAULT");
        ss.addRule(defaultRules); 
        FontColorSettings fcs = (MimeLookup.getLookup(MimePath.get(MarkdownDataObject.MIME_TYPE)).lookup(FontColorSettings.class));
        AttributeSet defaultAttributes = fcs.getFontColors("default"); // NOI18N
        FontColorSettingsFactory fcsf = EditorSettings.getDefault().getFontColorSettings(new String[]{MarkdownDataObject.MIME_TYPE});
        Collection<AttributeSet> attributes = fcsf.getAllFontColors(profile);
        Iterator<AttributeSet> attributesIt = attributes.iterator();
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

    public static void addRule(StyleSheet ss, AttributeSet attributeSet, AttributeSet defaultAttributes) {
        String nameAttr = (String) attributeSet.getAttribute(StyleConstants.NameAttribute);
        StringBuilder cssRule = new StringBuilder();
        String htmlTag = fontConfig2tagMapping.get(nameAttr);
        if (htmlTag == null) {
            return;
        }
        cssRule.append(htmlTag);
        cssRule.append(" {");
        
        cssRule.append(rgbStyling("color", getThemeColor(attributeSet, defaultAttributes, StyleConstants.ColorConstants.Foreground )));  // NOI18N
        cssRule.append(rgbStyling("background-color", getThemeColor(attributeSet, defaultAttributes, StyleConstants.ColorConstants.Background)));  // NOI18N
        cssRule.append("}");
        ss.addRule(cssRule.toString());
    }

    public static Color getThemeColor(AttributeSet attributeSet, AttributeSet defaultAttributes, Object constant ) {
        Color color = (Color) attributeSet.getAttribute(constant);
        if (color == null && defaultAttributes != null) {
            color = (Color) defaultAttributes.getAttribute(constant);
        }
        return color;
    }
    
    public static String rgbStyling(String property, Color color) {
        StringBuilder cssRule = new StringBuilder();
        if (color != null) {
            cssRule.append(property);
            cssRule.append(":rgb(");  // NOI18N
            cssRule.append(color.getRed());
            cssRule.append(",");  // NOI18N
            cssRule.append(color.getGreen());
            cssRule.append(",");  // NOI18N
            cssRule.append(color.getBlue());
            cssRule.append(");");  // NOI18N
        }
        
        return cssRule.toString();
    }
}
