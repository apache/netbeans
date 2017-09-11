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

package org.netbeans.jellytools;

import java.util.MissingResourceException;
import java.util.ResourceBundle;
import org.netbeans.jemmy.JemmyException;
import org.openide.util.NbBundle;

/** Helper class to get strings from NetBeans Bundle.properties files.
 * <br>
 * Everytime someone wants to identify a component by its title, label, caption or whatever,
 * he should not use hard coded string in his test case but he should use
 * <code>Bundle.getString(bundleName, key)</code> to obtain string from bundle.
 * Then test cases can be executed on different than English locale because
 * <code>getString()</code> methods returns string according to current locale.
 * <br><br>
 * Usage:
 * <br><pre>
 *        // "OK"
 *        Bundle.getString("org.netbeans.core.windows.services.Bundle", "OK_OPTION_CAPTION");
 *        // "Properties of AnObject"
 *        Bundle.getString("org.netbeans.core.Bundle", "CTL_FMT_LocalProperties", new Object[] {new Integer(1), "AnObject"});
 *        // "View"
 *        Bundle.getStringTrimmed("org.netbeans.core.Bundle", "Menu/View");
 * </pre>
 */
public class Bundle {
    
    /** Placeholder to disallow creating of instances. */
    private Bundle() {
        throw new Error("Bundle is just a container for static methods");
    }
    
    /** Returns ResourceBundle from specified path.
     * @param bundle path to bundle (e.g. "org.netbeans.core.Bundle")
     * @return ResourceBundle instance
     */
    public static ResourceBundle getBundle(String bundle) {
        try {
            return NbBundle.getBundle(bundle);
        } catch (NullPointerException e) {
            throw new JemmyException("\"" + bundle + "\" bundle was not found", e);
        } catch (MissingResourceException e) {
            throw new JemmyException("\"" + bundle + "\" bundle was not found", e);
        }
    }
    
    /** Gets string from specified ResourceBundle.
     * @param bundle instance of ResourceBundle
     * @param key key of requested string
     * @return string from bundle in current locale
     */
    public static String getString(ResourceBundle bundle, String key) {
        try {
            return bundle.getString(key);
        } catch (MissingResourceException e) {
            throw new JemmyException("\"" + key + "\" key was not found", e);
        } catch (NullPointerException npe) {
            throw new JemmyException("Cannot accept null parameter.", npe);
        }
    }
    
    /** Gets string from bundle specified by path to bundle and format it.
     * @param bundle path to bundle (e.g. "org.netbeans.core.Bundle")
     * @param key key of requested string
     * @param params parameters to be formatted
     * @return string from bundle in current locale with formatted parameters
     */
    public static String getString(ResourceBundle bundle, String key, Object[] params) {
        return java.text.MessageFormat.format(getString(bundle, key), params);
    }
    
    /** Gets string from bundle specified by path to bundle.
     * @param bundle path to bundle (e.g. "org.netbeans.core.Bundle")
     * @param key key of requested string
     * @return string from bundle in current locale
     */
    public static String getString(String bundle, String key) {
        return getString(getBundle(bundle), key);
    }
    
    /** Gets string from bundle, removes mnemonic (i.e. '&' or '(&X)') from it
     * and cuts parameters like {0} from the end.
     * @param bundle path to bundle (e.g. "org.netbeans.core.Bundle")
     * @param key key of requested string
     * @return string from bundle in current locale. Mnemonic (i.e. '&' or '(&X)')
     * is removed and parameter patterns are also removed starting by first '{'.
     */
    public static String getStringTrimmed(String bundle, String key) {
        return trim(getString(getBundle(bundle), key));
    }
    
    /** Gets string from bundle specified by path to bundle and format it.
     * @param bundle path to bundle (e.g. "org.netbeans.core.Bundle")
     * @param key key of requested string
     * @param params parameter to be formatted
     * @return string from bundle in current locale with formatted parameters
     */
    public static String getString(String bundle, String key, Object[] params) {
        return java.text.MessageFormat.format(getString(bundle, key), params);
    }
    
    /** Gets string from bundle and formats it. It removes mnemonic (i.e. '&' or '(&X)') 
     * from it and cuts parameters like {0} from the end if any.
     * @param bundle path to bundle (e.g. "org.netbeans.core.Bundle")
     * @param key key of requested string
     * @param params parameter to be formatted
     * @return string from bundle in current locale. Mnemonic and parameters 
     * like {0} removed from the end.
     */
    public static String getStringTrimmed(String bundle, String key, Object[] params) {
        return trim(getString(getBundle(bundle), key, params));
    }
    
    /** Removes mnemonic (i.e. '&' or '(&X)') and cut parameters like {0} from the end.
     * @param value string to modify
     * @return string with removed mnemonic and parameters like {0} from the end.
     */
    private static String trim(String value) {
        // remove mnemonic, i.e. '&' or '(&X)'
        value = cutAmpersand(value);
        // cut parameters like {0} from string
        if(value.indexOf('{')!=-1) {
            value = value.substring(0, value.indexOf('{'));
        }
        return value;
    }

    /**
     * Removes an ampersand from a text string; commonly used to strip out unneeded mnemonics.
     * Replaces the first occurence of <samp>&amp;?</samp> by <samp>?</samp> or <samp>(&amp;??</samp> by the empty string 
     * where <samp>?</samp> is a wildcard for any character.
     * <samp>&amp;?</samp> is a shortcut in English locale.
     * <samp>(&amp;?)</samp> is a shortcut in Japanese locale.
     * Used to remove shortcuts from workspace names (or similar) when shortcuts are not supported.
     * <p>The current implementation behaves in the same way regardless of locale.
     * In case of a conflict it would be necessary to change the
     * behavior based on the current locale.
     * @param text a localized label that may have mnemonic information in it
     * @return string without first <samp>&amp;</samp> if there was any
     */
    private static String cutAmpersand(String text) {
        // modified code of org.openide.awt.Actions.cutAmpersand
        // see also org.openide.awt.Mnemonics
        int i;
        String result = text;
        /* First check of occurence of '(&'. If not found check 
          * for '&' itself.
          * If '(&' is found then remove '(&??' and rest of line.
          */
        i = text.indexOf("(&"); // NOI18N
        if (i >= 0 && i + 3 < text.length() && /* #31093 */text.charAt(i + 3) == ')') { // NOI18N
            result = text.substring(0, i);
        } else {
            //Sequence '(&?)' not found look for '&' itself
            i = text.indexOf('&');
            if (i < 0) {
                //No ampersand
                result = text;
            } else if (i == (text.length() - 1)) {
                //Ampersand is last character, wrong shortcut but we remove it anyway
                result = text.substring(0, i);
            } else {
                //Remove ampersand from middle of string
                result = text.substring(0, i) + text.substring(i + 1);
            }
        }
        return result;
    }
}
