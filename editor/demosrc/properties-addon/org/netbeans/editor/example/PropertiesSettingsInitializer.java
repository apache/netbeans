/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
