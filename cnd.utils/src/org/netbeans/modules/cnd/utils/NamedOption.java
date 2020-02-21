/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
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
