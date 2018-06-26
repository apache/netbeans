/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2015, 2016 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 */package org.netbeans.modules.glassfish.tooling.utils;

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
     * Sanitize module name for use as Glassfish query parameter.
     * <p/>
     * @param name Glassfish module name.
     * @return Sanitized Glassfish module name.
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
            Logger.getLogger("glassfish").log(Level.INFO, ex.getLocalizedMessage(), ex); // NOI18N
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
