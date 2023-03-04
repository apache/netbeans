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

package org.netbeans.editor.example;

import java.awt.Font;
import java.awt.Color;
import java.awt.SystemColor;
import java.util.*;

import org.netbeans.editor.Settings;
import org.netbeans.editor.SettingsNames;
import org.netbeans.editor.SettingsUtil;
import org.netbeans.editor.Coloring;
import org.netbeans.editor.SettingsDefaults;
import org.netbeans.editor.Syntax;
import org.netbeans.editor.BaseKit;
import org.netbeans.editor.TokenContext;
import org.netbeans.editor.TokenContextPath;
import org.netbeans.editor.TokenCategory;
import org.netbeans.modules.properties.syntax.*;


public class PropertiesSettingsInitializer extends Settings.AbstractInitializer {

    /** Name assigned to initializer */
    public static final String NAME = "properties-settings-initializer"; // NOI18N
    private Class propertiesClass;

    public PropertiesSettingsInitializer( Class propertiesClass ) {
        super(NAME);
        this.propertiesClass = propertiesClass;
    }

    public void updateSettingsMap (Class kitClass, Map settingsMap) {
        
        // Properties colorings
        if (kitClass == BaseKit.class) {
            new PropertiesTokenColoringInitializer().updateSettingsMap(kitClass, settingsMap);

        }

        if (kitClass == propertiesClass) {
            settingsMap.put (org.netbeans.editor.SettingsNames.ABBREV_MAP, getPropertiesAbbrevMap());

            SettingsUtil.updateListSetting(settingsMap, SettingsNames.TOKEN_CONTEXT_LIST,
                new TokenContext[] {
                    PropertiesTokenContext.context
                }
            );

        }

    }


    Map getPropertiesAbbrevMap() {
        Map propertiesAbbrevMap = new TreeMap ();
        return propertiesAbbrevMap;
    }

    static class PropertiesTokenColoringInitializer
    extends SettingsUtil.TokenColoringInitializer {

        Font boldFont = SettingsDefaults.defaultFont.deriveFont(Font.BOLD);
        Font italicFont = SettingsDefaults.defaultFont.deriveFont(Font.ITALIC);

        Coloring emptyColoring = new Coloring(null, null, null);
        Coloring commentColoring = new Coloring(italicFont, Coloring.FONT_MODE_APPLY_STYLE,
                            Color.gray, null);

        Coloring numbersColoring = new Coloring(null, Color.red, null);

        public PropertiesTokenColoringInitializer() {
            super(PropertiesTokenContext.context);
        }

        public Object getTokenColoring(TokenContextPath tokenContextPath,
        TokenCategory tokenIDOrCategory, boolean printingSet) {

            if (!printingSet) {
                switch (tokenIDOrCategory.getNumericID()) {
                    case PropertiesTokenContext.KEY_ID:
                        return new Coloring(boldFont, Coloring.FONT_MODE_APPLY_STYLE,
                                Color.blue, null);

                    case PropertiesTokenContext.EQ_ID:
                    case PropertiesTokenContext.TEXT_ID:
                        return emptyColoring;

                    case PropertiesTokenContext.LINE_COMMENT_ID:
                        return new Coloring(italicFont, Coloring.FONT_MODE_APPLY_STYLE,
                                Color.gray, null);

                    case PropertiesTokenContext.VALUE_ID:
                        return new Coloring(null, Color.magenta, null);
                }



            } else { // printing set
                switch (tokenIDOrCategory.getNumericID()) {

                    default:
                         return SettingsUtil.defaultPrintColoringEvaluator;
                }

            }

            return null;

        }

    }

}
