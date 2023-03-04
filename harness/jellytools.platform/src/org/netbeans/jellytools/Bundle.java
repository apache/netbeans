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
 *        Bundle.getString("org.netbeans.core.Bundle", "CTL_FMT_LocalProperties", new Object[] {Integer.valueOf(1), "AnObject"});
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
