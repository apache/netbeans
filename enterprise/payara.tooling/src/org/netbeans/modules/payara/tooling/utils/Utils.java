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
package org.netbeans.modules.payara.tooling.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Common utilities.
 * <p/>
 * @author Vince Kraemer, Tomas Kraus, Peter Benedikovic
 */
public class Utils {

    ////////////////////////////////////////////////////////////////////////////
    // Class attributes                                                       //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Get system default line separator.
     * <p/>
     * @return System default line separator.
     */
    public static String lineSeparator() {
        String lineSeparator = System.getProperty("line.separator");
        if (lineSeparator == null) {
            lineSeparator = "\n";
        }
        return lineSeparator;
    }

    /**
     * Sanitize module name for use as Payara query parameter.
     * <p/>
     * @param name Payara module name.
     * @return Sanitized Payara module name.
     */
    public static String sanitizeName(String name) {
        if (null == name || name.matches("[\\p{L}\\p{N}_][\\p{L}\\p{N}\\-_./;#:]*")) {
            return name;
        }
        // the string is bad...
        return "_" + name.replaceAll("[^\\p{L}\\p{N}\\-_./;#:]", "_");
    }

    /**
     * Add quotes to string if and only if it contains space characters.
     *<p/>
     * Note: does not handle generalized white space (tabs, localized white
     * space, etc.)
     *<p/>
     * @param path File path in string form.
     * @return Quoted path if it contains any space characters, otherwise same.
     */
    public static String quote(String path) {
        return path.indexOf(' ') == -1 ? path : "\"" + path + "\"";
    }
    
    /**
     * Convert classpath fragment using standard separator to a list of
     * normalized files (nonexistent jars will be removed).
     *
     * @param cp classpath string
     * @param root root folder for expanding relative path names
     * @return list of existing jars, normalized
     */
    public static List<File> classPathToFileList(String cp, File root) {
        List<File> result = new ArrayList<File>();
        if(cp != null && cp.length() > 0) {
            String [] jars = cp.split(File.pathSeparator);
            for(String jar: jars) {
                File jarFile = new File(jar);
                if(!jarFile.isAbsolute() && root != null) {
                    jarFile = new File(root, jar);
                }
                if(jarFile.exists()) {
                    result.add(jarFile);
                }
            }
        }
        return result;
    }
    
    /**
     * Pattern that matches strings like ${com.sun.aas.instanceRoot}
     */
    private static Pattern pattern = Pattern.compile("\\$\\{([^}]+)\\}"); // NOI18N
    
    /**
     * Utility method that finds all occurrences of variable references and
     * replaces them with their values.
     * Values are taken from <code>varMap</code> and escaped. If they are
     * not present there, system properties are queried. If not found there
     * the variable reference is replaced with the same string with special
     * characters escaped.
     * 
     * @param value String value where the variables have to be replaced with values
     * @param varMap mapping of variable names to their values
     * @return String where the all the replacement was done
     */
    public static String doSub(String value, Map<String, String> varMap) {
        try {
            Matcher matcher = pattern.matcher(value);
            boolean result = matcher.find();
            if (result) {
                StringBuffer sb = new StringBuffer(value.length() * 2);
                do {
                    String key = matcher.group(1);
                    String replacement = varMap.get(key);
                    if (replacement == null) {
                        replacement = System.getProperty(key);
                        if (replacement != null) {
                            replacement = escapePath(replacement);
                        } else {
                            replacement = "\\$\\{" + key + "\\}"; // NOI18N
                        }
                    } else {
                        replacement = escapePath(replacement);
                    }
                    matcher.appendReplacement(sb, replacement);
                    result = matcher.find();
                } while (result);
                matcher.appendTail(sb);
                value = sb.toString();
            }
        } catch (Exception ex) {
            Logger.getLogger("payara").log(Level.INFO, ex.getLocalizedMessage(), ex); // NOI18N
        }
        return value;
    }
    
    /**
     * Add escape characters for backslash and dollar sign characters in
     * path field.
     *
     * @param path file path in string form.
     * @return adjusted path with backslashes and dollar signs escaped with
     *   backslash character.
     */
    public static String escapePath(String path) {
        return path.replace("\\", "\\\\").replace("$", "\\$"); // NOI18N
    }
    
    public static String[] splitOptionsString(String optionString) {
        return optionString.trim().split("\\s+(?=-)");
    }

    /**
     * Concatenate elements of {@link String} array as
     * a single <code>String</code> containing all elements separated
     * by <code>,</code>.
     * <p/>
     * @param array {2see String} array containing elements
     *              to be concatenated.
     * @return {2see String} containing all elements concatenated and separated
     *         by <code>,</code> or <code>null</code> when <code>array</code>
     *         is <code>null</code>.
     */
    public static String concatenate(final String[] array) {
        if (array != null) {
            boolean first = true;
            StringBuilder sb = new StringBuilder();
            for (String str : array) {
                if (first) {
                    first = false;
                } else {
                    sb.append(", ");
                }
                sb.append(str);
            }
            return sb.toString();
        }
        return null;
    }

}
