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

package org.netbeans.api.java.source;

import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.lib.editor.util.CharSequenceUtilities;

/**
 *
 * @author Ralph Benjamin Ruijs &lt;ralphbenjamin@netbeans.org&gt;
 * @since 0.123
 */
public final class CodeStyleUtils {

    private CodeStyleUtils() {
    }
    
    /**
     * Add a prefix and suffix to a name. If the prefix or suffix are null, then
     * the name without prefixes or suffixes will be returned. When you add a
     * prefix value ending with an alphabetic character, the name will be
     * capitalized. For example, if the prefix for a name is defined as s, and
     * the name is "i", then the suggested name will be "sI".
     *
     *
     * @param name the name to add the Prefix and Suffix to.
     * @param prefix the prefix to add, or null if no prefix should be added.
     * @param suffix the suffix to add, or null if no suffix should be added.
     * @return the name with added prefix and suffix
     * @see #getCapitalizedName(java.lang.CharSequence)
     */
    @NonNull
    public static String addPrefixSuffix(@NullAllowed CharSequence name,
                                         @NullAllowed String prefix,
                                         @NullAllowed String suffix) {
        StringBuilder sb = new StringBuilder();
        boolean capitalize = false;
        if (prefix != null && prefix.length() > 0) {
            if (Character.isAlphabetic(prefix.charAt(prefix.length() - 1))) {
                capitalize = true;
            }
            sb.insert(0, prefix);
        }
        if (name != null) {
            sb.append(capitalize ? getCapitalizedName(name) : name);
        }
        if (suffix != null) {
            sb.append(suffix);
        }
        return sb.toString();
    }

    /**
     * Removes a prefix and suffix from a name. When you remove a prefix value
     * ending with an alphabetic character, the name will be decapitalized. If
     * the name originally started with _ , they will not be added.
     *
     * @param name the name to remove the Prefix and Suffix from.
     * @param prefix the prefix to remove, or null if no prefix should be
     * removed.
     * @param suffix the suffix to remove, or null if no suffix should be
     * removed.
     * @return the name without the prefix and suffix
     * @see #addPrefixSuffix(java.lang.CharSequence, java.lang.String,
     * java.lang.String)
     * @see #getDecapitalizedName(java.lang.CharSequence) 
     */
    @NonNull
    public static String removePrefixSuffix(@NonNull CharSequence name,
                                            @NullAllowed String prefix,
                                            @NullAllowed String suffix) {
        StringBuilder sb = new StringBuilder(name);
        int start = 0;
        int end = name.length();
        boolean decapitalize = false;
        if (prefix != null && prefix.length() > 0 && CharSequenceUtilities.startsWith(name, prefix)) {
            start = prefix.length();
            if (Character.isAlphabetic(prefix.charAt(prefix.length() - 1))) {
                decapitalize = true;
            }
        }
        if (suffix != null && suffix.length() > 0 && CharSequenceUtilities.endsWith(name, suffix)) {
            end = end - suffix.length();
        }
        // in the case that prefix + suffix overlap in the name, do not strip anything (the computer
        // could not decide whether prefix is desirable to retain or suffix is). The same for empty 
        // basename.
        String result = start <= end ? sb.substring(start, end) : name.toString();
        if(decapitalize) {
            return getDecapitalizedName(result);
        } else {
            return result;
        }
    }

    /**
     * Capitalize a name following the javabeans specification. This normally
     * means converting the first character from lower case to upper case, but
     * in the (unusual) special case when there is more than one character and
     * the second character is upper case, we leave it alone. Thus "fooBah"
     * becomes "FooBah" and "x" becomes "X", but "eMail" stays as "eMail".
     * <p>To stay backwards compatible, if the name starts with _ they will be
     * removed.
     *
     * @param name the name to capitalize
     * @return the capizalized name
     */
    @NonNull
    public static String getCapitalizedName(CharSequence name) {
        StringBuilder sb = new StringBuilder(name);
        while (sb.length() > 1 && sb.charAt(0) == '_') { //NOI18N
            sb.deleteCharAt(0);
        }

        //Beans naming convention, #165241
        if (sb.length() > 1 && Character.isUpperCase(sb.charAt(1))) {
            return sb.toString();
        }

        if (sb.length() > 0) {
            sb.setCharAt(0, Character.toUpperCase(sb.charAt(0)));
        }
        return sb.toString();
    }
    
    /**
     * Decapitalize a name to its normal java form. This normally
     * means converting the first character from upper case to lower case, but
     * in the (unusual) special case when there is more than one character and
     * the second character is upper case, we leave it alone. Thus "FooBah"
     * becomes "fooBah" and "X" becomes "x", but "URL" stays as "URL".
     *
     * @param name the name to decapitalize
     * @return the decapizalized name
     */
    @NonNull
    public static String getDecapitalizedName(@NonNull CharSequence name) {
        //Beans naming convention, #165241
        if (name.length() > 1 && (Character.isUpperCase(name.charAt(1)) ||
                                  Character.isLowerCase(name.charAt(0)))) {
            return name.toString();
        }
        
        StringBuilder sb = new StringBuilder(name);
        if (sb.length() > 0) {
            sb.setCharAt(0, Character.toLowerCase(sb.charAt(0)));
        }
        return sb.toString();
    }

    /**
     * Computes the method name that should be used to read the property value.
     * 
     * @param fieldName the name of the property
     * @param isBoolean true if the property is of boolean type
     * @param isStatic true if the property is static
     * @param cs the CodeSyle to use
     * @return the getter name
     */
    @NonNull
    public static String computeGetterName(CharSequence fieldName, boolean isBoolean, boolean isStatic, CodeStyle cs) {
        StringBuilder sb = new StringBuilder(getCapitalizedName(removeFieldPrefixSuffix(fieldName, isStatic, cs)));
        sb.insert(0, isBoolean ? "is" : "get"); //NOI18N
        String getterName = sb.toString();
        return getterName;
    }

    /**
     * Computes the method name that should be used to write the property value.
     * 
     * @param fieldName the name of the property
     * @param isStatic true if the property is static
     * @param cs the CodeSyle to use
     * @return the setter name
     */
    @NonNull
    public static String computeSetterName(CharSequence fieldName, boolean isStatic, CodeStyle cs) {
        StringBuilder name = new StringBuilder(getCapitalizedName(removeFieldPrefixSuffix(fieldName, isStatic, cs)));
        name.insert(0, "set"); //NOI18N
        return name.toString();
    }
    
    private static String removeFieldPrefixSuffix(CharSequence fieldName, boolean isStatic, CodeStyle cs) {
        return removePrefixSuffix(fieldName,
                                  isStatic ? cs.getStaticFieldNamePrefix() : cs.getFieldNamePrefix(),
                                  isStatic ? cs.getStaticFieldNameSuffix() : cs.getFieldNameSuffix());
    }
}
