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
package org.netbeans.modules.cnd.makeproject.api.configurations;

import java.util.StringTokenizer;
import org.openide.util.Lookup;

public final class ConfigurationSupport {

    private ConfigurationSupport() {
    }

    public static String appendConfName(String oldConfs, Configuration newConf) {
        return oldConfs + "," + newConf.getDisplayName(); // NOI18N
    }

    public static String removeConfName(String oldConfs, Configuration oldConf) {
        StringBuilder newConfs = new StringBuilder();
        StringTokenizer st = new StringTokenizer(oldConfs, ","); // NOI18N
        while (st.hasMoreTokens()) {
            // Strip "'s
            String token = st.nextToken();
            String displayName = token; //token.substring(1, token.length()-1);
            if (displayName.equals(oldConf.getDisplayName())) {
                continue;
            }
            if (newConfs.length() > 0) {
                newConfs.append(","); // NOI18N
            }
            newConfs.append(displayName);
        }
        return newConfs.toString();
    }

    public static String renameConfName(String oldConfs, String oldDisplayName, String newDisplayName) {
        int i = oldConfs.indexOf(oldDisplayName);
        if (i < 0) {
            // Error FIXUP; should be there!
        }
        String newConfs = oldConfs.substring(0, i) + newDisplayName + oldConfs.substring(i + oldDisplayName.length());
        return newConfs;

    }

    public static String makeNameLegal(String displayName) {
        StringBuilder tmp = new StringBuilder();
        int len = displayName.length();
        for (int i = 0; i < displayName.length(); i++) {
            char ch = displayName.charAt(i);
            if (i == 0 && (isLetterOrDigit(ch)
                    || ch == '_')) {
                tmp.append(ch);
            } else if (i == len - 1
                    && ch == '.') {
                tmp.append('_'); // NOI18N
            } else if (i != 0
                    && (isLetterOrDigit(ch)
                    || ch == '_'
                    || ch == '-'
                    || ch == '.')) {
                tmp.append(ch);
            } else {
                tmp.append("_"); // NOI18N
            }
        }
        if (tmp.length() == 0) {
            return "Configuration"; // NOI18N
        } else {
            return tmp.toString();
        }
    }

    private static boolean isLetterOrDigit(char ch) {
        if (ch < '0' || ch > 'z') {
            return false;
        } else {
            return Character.isLetterOrDigit(ch);
        }
    }

    public static String getNameFromDisplayName(String displayName) {
        /*
        StringBuffer tmp = new StringBuffer();
        for (int i = 0; i < displayName.length(); i++) {
        if (!Character.isWhitespace(displayName.charAt(i)) &&
        displayName.charAt(i) != '(' &&
        displayName.charAt(i) != ')' &&
        displayName.charAt(i) != ',') {
        tmp.append(displayName.charAt(i));
        }
        }
        return tmp.toString();
         */
        return displayName; // FIXUP: are thay always identical????
    }

    // Unique names
    public static String getUniqueName(Configuration[] cs, String baseName) {
        int number = 1;
        String newDisplayName;
        while (true) {
            if (number == 1) {
                newDisplayName = baseName;
            } else {
                newDisplayName = baseName + "-" + number; // NOI18N
            }
            if (isNameUnique(cs, newDisplayName)) {
                break;
            }
            number++;
        }
        return newDisplayName;
    }

    public static String getUniqueNewName(Configuration[] cs) {
        return getUniqueName(cs, "NewConfiguration"); // NOI18N
    }

    public static String getUniqueCopyName(Configuration[] cs, Configuration copy) {
        int number = 1;
        String newBaseName = "Copy"; // NOI18N
        String newName;
        while (true) {
            if (number == 1) {
                newName = newBaseName + "_of_" + copy.getName(); // NOI18N
            } else {
                newName = newBaseName + "-" + number + "_of_" + copy.getName(); // NOI18N
            }
            if (isNameUnique(cs, newName)) {
                break;
            }
            number++;
        }
        return newName;
    }

    public static boolean isNameUnique(Configuration[] cs, String displayName) {
        boolean unique = true;
        String name = getNameFromDisplayName(displayName);
        for (int i = 0; i < cs.length; i++) {
            if (cs[i].getName().equals(name)) {
                unique = false;
                break;
            }
        }
        return unique;
    }

    // property values
    public static String getConfsPropertyValue(Configuration[] cs) {
        StringBuilder configurationProperty = new StringBuilder();
        for (int i = 0; i < cs.length; i++) {
            if (configurationProperty.length() > 0) {
                configurationProperty.append(","); // NOI18N
            }
            configurationProperty.append(cs[i].getDisplayName());
        }
        return configurationProperty.toString();
    }

    public static String getDefaultConfPropertyValue(Configuration conf) {
        return conf.getDisplayName();
    }

    public static MakeConfiguration getProjectActiveConfiguration(Lookup.Provider project) {
        if (project != null) {
            ConfigurationDescriptorProvider pdp = project.getLookup().lookup(ConfigurationDescriptorProvider.class);
            if (pdp != null) {
                MakeConfigurationDescriptor cd = pdp.getConfigurationDescriptor();
                if (cd != null) {
                    return cd.getActiveConfiguration();
                }
            }
        }
        return null;
    }
}
