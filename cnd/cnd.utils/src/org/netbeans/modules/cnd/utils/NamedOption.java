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
package org.netbeans.modules.cnd.utils;

import java.util.Arrays;
import java.util.prefs.Preferences;
import org.openide.util.Lookup;
import org.openide.util.NbPreferences;
import org.openide.util.lookup.Lookups;

/**
 *
 */
public abstract class NamedOption {
    public static final String HIGHLIGTING_CATEGORY = "CND/options/highlighting"; //NOI18N
    public static final String MAKE_PROJECT_CATEGORY = "CND/options/makeProject"; //NOI18N
    public static final String HINTS_CATEGORY = "CND/options/hints"; //NOI18N
    public static final String OTHER_CATEGORY = "CND/options/other"; //NOI18N
    public static final String EXTRA_OPTIONS_FLAG = "cnd.options.project.extra"; //NOI18N
    
    private static final Accessor accessor = new Accessor();
    
    public enum OptionKind {
        Boolean,
        Integer,
        String
    }

    public abstract String getName();
    
    public abstract String getDisplayName();

    public abstract String getDescription();

    public abstract OptionKind getKind();

    public abstract Object getDefaultValue();
    
    public boolean isVisible() {
        return true;
    }
    
    public static Accessor getAccessor() {
        return accessor;
    }
    
    public static final class Accessor {
        private final Preferences preferences = NbPreferences.forModule(NamedOption.class);
        
        public final boolean getBoolean(String name) {
            NamedOption option = findOption(name);
            if (option.getKind() == OptionKind.Boolean) {
                return preferences.getBoolean(option.getName(), (Boolean)option.getDefaultValue());
            }
            throw new IllegalArgumentException("Option "+option.getName()+" is not boolean"); //NOI18N
        }

        
        public final void setBoolean(String name, boolean value) {
            NamedOption option = findOption(name);
            if (option.getKind() == OptionKind.Boolean) {
                preferences.putBoolean(option.getName(), value);
                return;
            }
            throw new IllegalArgumentException("Option "+option.getName()+" is not boolean"); //NOI18N
        }

        public final int getInteger(String name) {
            NamedOption option = findOption(name);
            if (option.getKind() == OptionKind.Integer) {
                return preferences.getInt(option.getName(), (Integer)option.getDefaultValue());
            }
            throw new IllegalArgumentException("Option "+option.getName()+" is not integer"); //NOI18N
        }

        public final void setInteger(String name, int value) {
            NamedOption option = findOption(name);
            if (option.getKind() == OptionKind.Integer) {
                preferences.putInt(option.getName(), value);
                return;
            }
            throw new IllegalArgumentException("Option "+option.getName()+" is not integer"); //NOI18N
        }
        
        public final String getString(String name) {
            NamedOption option = findOption(name);
            if (option.getKind() == OptionKind.String) {
                return preferences.get(option.getName(), (String)option.getDefaultValue());
            }
            throw new IllegalArgumentException("Option "+option.getName()+" is not string"); //NOI18N
        }

        public final void setString(String name, String value) {
            NamedOption option = findOption(name);
            if (option.getKind() == OptionKind.String) {
                preferences.get(option.getName(), value);
                return;
            }
            throw new IllegalArgumentException("Option "+option.getName()+" is not string"); //NOI18N
        }

        private NamedOption findOption(String name) {
            for (String category: Arrays.asList(HIGHLIGTING_CATEGORY, HINTS_CATEGORY, MAKE_PROJECT_CATEGORY, OTHER_CATEGORY)) {
                for (NamedOption option : Lookups.forPath(category).lookupAll(NamedOption.class)) {
                    if (name.equals(option.getName())) {
                        return option;
                    }
                }
            }
            for (NamedOption option : Lookup.getDefault().lookupAll(NamedOption.class)) {
                if (name.equals(option.getName())) {
                    return option;
                }
            }
            throw new IllegalArgumentException("Not found option " + name); //NOI18N
        }
    }
}
