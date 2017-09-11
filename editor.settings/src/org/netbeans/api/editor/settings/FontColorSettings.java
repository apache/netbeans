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
