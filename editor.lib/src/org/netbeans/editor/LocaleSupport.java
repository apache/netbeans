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

package org.netbeans.editor;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.MissingResourceException;

/**
* All the strings that should be localized will go through this place.
* Multiple custom localizers can be registered.
*
* @deprecated this class is deprecated as the number of bundle queries is proportional
 *  to the number of registered localizers which is not optimal from the performance
 *  point of view. The queries through this class should be replaced
 *  by queries to individual bundles.
*
* @author Miloslav Metelka
* @version 1.00
*/

public class LocaleSupport {
    
    private static final boolean debug
            = Boolean.getBoolean("netbeans.debug.editor.localesupport");
    
// Fix of #36754 - localizers memory consumption
//    private static final String NULL_STRING = new String();

    /** Cache for the retrieved strings */
    // Fix of #36754 - localizers memory consumption
//    private static final HashMap cache = new HashMap(503);

    private static Localizer[] localizers = new Localizer[0];

    /** Add a new localizer to the localizer array. The array of localizers
    * is tracked from the lastly added localizer to the firstly added one
    * until the translation for the given key is found.
    * @param localizer localizer to add to the localizer array.
    */
    public static void addLocalizer(Localizer localizer) {
        ArrayList ll = new ArrayList(Arrays.asList(localizers));
        ll.add(localizer);
        Localizer[] la = new Localizer[ll.size()];
        ll.toArray(la);
        localizers = la;

//        cache.clear();
    }

    /** Remove the existing localizer from the localizer array.
    * @param localizer localizer to remove.
    */
    public static void removeLocalizer(Localizer localizer) {
        ArrayList ll = new ArrayList(Arrays.asList(localizers));
        ll.remove(localizer);
        Localizer[] la = new Localizer[ll.size()];
        ll.toArray(la);
        localizers = la;

//        cache.clear();
    }

    /** Get the localized string for the given key using the registered
    * localizers.
    * @param key key to translate to localized string.
    * @return localized string or null if there's no localization.
    */
    public static synchronized String getString(String key) {
        // Fix of #36754 - localizers memory consumption
        String ret = null;
        // String ret = (String)cache.get(key);
        //if (ret == null) {
            int i;
            for (i = localizers.length - 1; i >= 0; i--) {

                // Try to find a return value
                try {
                    ret = localizers[i].getString(key);
                } catch (MissingResourceException e) { // localizers are often bundles
                    ret = null;
                }

                if (ret != null) {
                    break;
                }
            }

//            if (ret == null) {
//                ret = NULL_STRING;
//            }
//            cache.put(key, ret);
        //}

//        return (ret != NULL_STRING) ? ret : null;
        if (debug) {
            String inLocalizerString = (i >= 0)
                ? " found in localizer=" + localizers[i] // NOI18N
                : ""; // NOI18N
            /*DEBUG*/System.err.println("LocaleSupport.getString(): key=\"" + key + // NOI18N
                    "\", value=\"" + ret + "\"" + inLocalizerString);
            /*DEBUG*/Thread.dumpStack();
        }
        return ret;
    }

    /** Get the localized string or the default value if no translation
    * for the given key exists.
    * @param key key to translate to localized string.
    * @param defaultValue default value to be returned in case no localized
    *   string is found for the given key.
    */
    public static String getString(String key, String defaultValue) {
        String ret = getString(key);
        return (ret != null) ? ret : defaultValue;
    }
    
    /** Get the localized character or the default value if no translation
     * for the given key exists. This method is mainly usable for getting
     * localized mnemonics.
     * @param key key to translate to localized character.
     * @param defaultValue default value to be returned in case no localized
     *        character is found for the given key.
     */
    public static char getChar( String key, char defaultValue ) {
        String value = getString( key );
        if( value == null || value.length() < 1 ) return defaultValue;
        return value.charAt( 0 );
    }

    /** Translates the keys to the localized strings. There can be multiple localizers
    * registered in the locale support.
    */
    public interface Localizer {

        /** Translate the key to the localized string.
        * @param key key to translate to the localized string.
        */
        public String getString(String key);

    }

}
