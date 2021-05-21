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

package org.netbeans.api.editor.settings;

import javax.swing.text.AttributeSet;

/**
 * The map of coloring names and their parameters.
 *
 * <p>The term coloring refers to a set attributes that can be used for rendering
 * text in an editor window. These attributes may be but are not limited to a
 * font, foreground and background color, etc.
 * 
 * <p>The coloring names are defined by modules. Each coloring is represented by
 * <code>AttributeSet</code>, which contains keys and values for all the attributes
 * that should be used for rendering text that was colorified by the coloring.
 * The keys that can be used to obtain particular attributes are defined in the
 * {@link javax.swing.text.StyleConstants} and
 * {@link org.netbeans.api.editor.settings.EditorStyleConstants} classes.
 * 
 * <p>Supported keys for FontColorSettings are:
 * <ol>
 *    <li> StyleConstants.FontFamily </li>
 *    <li> StyleConstants.FontSize </li>
 *    <li> StyleConstants.Bold </li>
 *    <li> StyleConstants.Italic </li>
 *    <li> StyleConstants.Foreground </li>
 *    <li> StyleConstants.Background </li>
 *    <li> StyleConstants.Underline </li>
 *    <li> StyleConstants.StrikeThrough </li>
 *    <li> and all attributes defined in {@link org.netbeans.api.editor.settings.EditorStyleConstants} </li>
 * </ol>
 *
 * <p>Instances of this class should be retrieved from <code>MimeLookup</code>.
 * 
 * <p><font color="red">This class must NOT be extended by any API clients.</font>
 *
 * @author Martin Roskanin
 */
public abstract class FontColorSettings {

    /**
     * @deprecated This should have never been made public. Nobody can listen on this property.
     */
    @Deprecated
    public static final String PROP_FONT_COLORS = "fontColors"; //NOI18N
    
    /**
     * Construction prohibited for API clients.
     */
    public FontColorSettings() {
        // Control instantiation of the allowed subclass only
        if (!getClass().getName().startsWith("org.netbeans.modules.editor.settings.storage")) { // NOI18N
            throw new IllegalStateException("Instantiation prohibited. " + getClass().getName()); // NOI18N
        }
    }
    
    /**
     * Gets the font and colors. 
     * 
     * @param settingName font and colors setting name
     *
     * @return AttributeSet describing the font and colors. 
     */
    public abstract AttributeSet getFontColors(String settingName);

    /**
     * Gets the token font and colors. 
     * 
     * @param tokenName token name
     *
     * @return AttributeSet describing the font and colors
     */
    public abstract AttributeSet getTokenFontColors(String tokenName);
    
}
