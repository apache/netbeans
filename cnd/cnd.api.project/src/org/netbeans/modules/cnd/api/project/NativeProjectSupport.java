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
package org.netbeans.modules.cnd.api.project;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import org.netbeans.modules.cnd.spi.project.NativeFileSearchProvider;
import org.netbeans.modules.cnd.spi.project.NativeProjectExecutionProvider;
import org.netbeans.modules.cnd.utils.CndLanguageStandards;
import org.netbeans.modules.cnd.utils.CndLanguageStandards.CndLanguageStandard;
import org.netbeans.modules.cnd.utils.FSPath;
import org.netbeans.modules.cnd.utils.MIMEExtensions;
import org.netbeans.modules.cnd.utils.MIMENames;
import org.openide.util.lookup.Lookups;

/**
 *
 */
public final class NativeProjectSupport {
    private NativeProjectSupport() {}

    /**
     * Execute a command from user's PATH in the context of the native project
     *
     * @param project Native project
     * @param executable Executable name (not path)
     * @param env Additional environment variables
     * @param args Arguments
     * @return NativeExitStatus
     */
    public static NativeExitStatus execute(NativeProject project, final String executable, final String[] env, final String... args) throws IOException {
        for (NativeProjectExecutionProvider provider : Lookups.forPath(NativeProjectExecutionProvider.PATH).lookupAll(NativeProjectExecutionProvider.class)) {
            NativeExitStatus result = provider.execute(project, executable, env, args);
            if (result != null) {
                return result;
            }            
        }
        return null;
    }

    /**
     * Return the name of the development platform (Solaris-x86, Solaris-sparc,
     * MacOSX, Windows, Linux-x86)
     *
     * @param project Native project
     * @return development platform name
     */
    public static String getPlatformName(NativeProject project) {
        for (NativeProjectExecutionProvider provider : Lookups.forPath(NativeProjectExecutionProvider.PATH).lookupAll(NativeProjectExecutionProvider.class)) {
            String result = provider.getPlatformName(project);
            if (result != null) {
                return result;
            }
        }
        return null;
    }

    /**
     * *
     * Searcher for find project file by name
     *
     * @param project Native project
     * @return searcher
     */
    public static NativeFileSearch getNativeFileSearch(final NativeProject project) {
        final List<NativeFileSearch> delegates = new ArrayList<NativeFileSearch>();
        for (NativeFileSearchProvider provider : Lookups.forPath(NativeFileSearchProvider.PATH).lookupAll(NativeFileSearchProvider.class)) {
            NativeFileSearch result = provider.getNativeFileSearch(project);
            if (result != null) {
                delegates.add(result);
            }
        }
        return new NativeFileSearch() {
            @Override
            public Collection<FSPath> searchFile(NativeProject project, String fileName) {
                Collection<FSPath> out = new LinkedHashSet<FSPath>();
                for (NativeFileSearch searcher : delegates) {
                    out.addAll(searcher.searchFile(project, fileName));
                }
                return out;
            }
        };
    }
    
    public static NativeFileItem.LanguageFlavor cndStandardToItemFlavor(CndLanguageStandard defaultStandard) {
        NativeFileItem.LanguageFlavor flavor = NativeFileItem.LanguageFlavor.UNKNOWN;
        switch (defaultStandard) {
            case C89:
                flavor =NativeFileItem.LanguageFlavor.C89;
                break;
            case C99:
                flavor =NativeFileItem.LanguageFlavor.C99;
                break;
            case C11:
                flavor =NativeFileItem.LanguageFlavor.C11;
                break;
            case C17:
                flavor =NativeFileItem.LanguageFlavor.C17;
                break;
            case C23:
                flavor =NativeFileItem.LanguageFlavor.C23;
                break;
            case CPP98:
                flavor =NativeFileItem.LanguageFlavor.CPP98;
                break;
            case CPP11:
                flavor =NativeFileItem.LanguageFlavor.CPP11;
                break;
            case CPP14:
                flavor =NativeFileItem.LanguageFlavor.CPP14;
                break;
            case CPP17:
                flavor =NativeFileItem.LanguageFlavor.CPP17;
                break;
            case CPP20:
                flavor =NativeFileItem.LanguageFlavor.CPP20;
                break;
            case CPP23:
                flavor =NativeFileItem.LanguageFlavor.CPP23;
                break;
        }
        return flavor;
    }

    public static CndLanguageStandard itemFlavorToCndStandard(NativeFileItem.LanguageFlavor flavor) {
        if (flavor == null) {
            return CndLanguageStandard.UNKNOWN;
        }
        switch (flavor) {
            case UNKNOWN:
            case DEFAULT:
            case C:
                return CndLanguageStandard.UNKNOWN;
            case C89:
                return CndLanguageStandard.C89;
            case C99:
                return CndLanguageStandard.C99;
            case C11:
                return CndLanguageStandard.C11;
            case C17:
                return CndLanguageStandard.C17;
            case C23:
                return CndLanguageStandard.C23;
            case CPP98:
                return CndLanguageStandard.CPP98;
            case CPP11:
                return CndLanguageStandard.CPP11;
            case CPP14:
                return CndLanguageStandard.CPP14;
            case CPP17:
                return CndLanguageStandard.CPP17;
            case CPP20:
                return CndLanguageStandard.CPP20;
            case CPP23:
                return CndLanguageStandard.CPP23;
            case F77:
            case F90:
            case F95:
                return CndLanguageStandard.UNKNOWN;
            default:
                throw new AssertionError(flavor.name());
        }
    }
    
    /**
     *  Default standard for language.
     *  See Tools->Options->C++/Other->Default Standard.
     * 
     * @param lang language
     * @return default language flavor
     */
    public static NativeFileItem.LanguageFlavor getDefaultLanguageFlavor(NativeFileItem.Language lang) {
        NativeFileItem.LanguageFlavor bestFlavor = NativeFileItem.LanguageFlavor.UNKNOWN;
        switch(lang) {
            case C:
                bestFlavor = getDefaultCStandard();
                break;
            case CPP:
                bestFlavor = getDefaultCppStandard();
                break;
            case C_HEADER:
                bestFlavor = getDefaultHeaderStandard();
                break;
        }
        return bestFlavor;
    }
    
    /**
     *  Default standard for C++ language.
     *  See Tools->Options->C++/Other->Default Standard.
     * 
     * @return language flavor
     */
    public static NativeFileItem.LanguageFlavor getDefaultCppStandard() {
        MIMEExtensions me = MIMEExtensions.get(MIMENames.CPLUSPLUS_MIME_TYPE);
        CndLanguageStandards.CndLanguageStandard defaultStandard = me.getDefaultStandard();
        if (defaultStandard != null) {
            switch(defaultStandard) {
                case CPP98: return NativeFileItem.LanguageFlavor.CPP98;
                case CPP11: return NativeFileItem.LanguageFlavor.CPP11;
                case CPP14: return NativeFileItem.LanguageFlavor.CPP14;
                case CPP17: return NativeFileItem.LanguageFlavor.CPP17;
                case CPP20: return NativeFileItem.LanguageFlavor.CPP20;
                case CPP23: return NativeFileItem.LanguageFlavor.CPP23;
            }
        }
        return NativeFileItem.LanguageFlavor.UNKNOWN;
    }

    /**
     *  Default standard for headers.
     *  See Tools->Options->C++/Other->Default Standard.
     * 
     * @return language flavor
     */
    public static NativeFileItem.LanguageFlavor getDefaultHeaderStandard() {
        MIMEExtensions me = MIMEExtensions.get(MIMENames.HEADER_MIME_TYPE);
        CndLanguageStandards.CndLanguageStandard defaultStandard = me.getDefaultStandard();
        if (defaultStandard != null) {
            switch(defaultStandard) {
                case CPP98: return NativeFileItem.LanguageFlavor.CPP98;
                case CPP11: return NativeFileItem.LanguageFlavor.CPP11;
                case CPP14: return NativeFileItem.LanguageFlavor.CPP14;
                case CPP17: return NativeFileItem.LanguageFlavor.CPP17;
                case CPP20: return NativeFileItem.LanguageFlavor.CPP20;
                case CPP23: return NativeFileItem.LanguageFlavor.CPP23;
            }
        }
        return NativeFileItem.LanguageFlavor.UNKNOWN;
    }
    
    /**
     *  Default standard for C language.
     *  See Tools->Options->C++/Other->Default Standard.
     * 
     * @return language flavor
     */
    public static NativeFileItem.LanguageFlavor getDefaultCStandard() {
        MIMEExtensions me = MIMEExtensions.get(MIMENames.C_MIME_TYPE);
        CndLanguageStandards.CndLanguageStandard defaultStandard = me.getDefaultStandard();
        if (defaultStandard != null) {
            switch(defaultStandard) {
                case C89: return NativeFileItem.LanguageFlavor.C89;
                case C99: return NativeFileItem.LanguageFlavor.C99;
                case C11: return NativeFileItem.LanguageFlavor.C11;
                case C17: return NativeFileItem.LanguageFlavor.C17;
                case C23: return NativeFileItem.LanguageFlavor.C23;
            }
        }
        return NativeFileItem.LanguageFlavor.UNKNOWN;
    }
    
    public static final class NativeExitStatus {

        public final int exitCode;
        public final String error;
        public final String output;

        public NativeExitStatus(int exitCode, String output, String error) {
            this.exitCode = exitCode;
            this.error = error;
            this.output = output;
        }

        public boolean isOK() {
            return exitCode == 0;
        }
    }
}
