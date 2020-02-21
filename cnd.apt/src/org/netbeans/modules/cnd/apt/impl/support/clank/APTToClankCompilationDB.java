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
package org.netbeans.modules.cnd.apt.impl.support.clank;

import java.util.Collection;
import java.util.Collections;
import org.clang.frontend.InputKind;
import org.clang.frontend.LangStandard;
import org.clang.tools.services.ClankCompilationDataBase;
import org.clang.tools.services.spi.ClankFileSystemProvider;
import org.clang.tools.services.support.DataBaseEntryBuilder;
import static org.clank.support.NativePointer.*;
import org.llvm.adt.StringRef;
import org.llvm.support.sys.path;
import org.netbeans.modules.cnd.apt.support.IncludeDirEntry;
import org.netbeans.modules.cnd.apt.support.api.PreprocHandler;
import org.netbeans.modules.cnd.apt.support.api.StartEntry;
import org.netbeans.modules.cnd.spi.utils.CndFileSystemProvider;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.netbeans.modules.cnd.utils.FSPath;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

/**
 *
 */
public final class APTToClankCompilationDB implements ClankCompilationDataBase {
    private static boolean SKIP_COMPILER_SETTINGS = Boolean.valueOf(System.getProperty("cnd.skip.compiler.builtin", "false")); // NOI18N
    private final Collection<ClankCompilationDataBase.Entry> compilations;
    private final String name;

    private APTToClankCompilationDB(Collection<ClankCompilationDataBase.Entry> compilations) {
        this("APTToClankDB with [" + compilations.size() + "] entries", compilations);// NOI18N
    }

    private APTToClankCompilationDB(CharSequence dbName, Collection<Entry> compilations) {
        this.name = dbName == null ? "" : dbName.toString();
        this.compilations = Collections.unmodifiableCollection(compilations);
    }

    @Override
    public Collection<ClankCompilationDataBase.Entry> getCompilations() {
        return compilations;
    }

    @Override
    public String getName() {
        return name;
    }

    public static ClankCompilationDataBase convertPPHandler(PreprocHandler ppHandler, CharSequence dbName) {
        Entry entry = createEntry(ppHandler);
        assert entry != null;
        return new APTToClankCompilationDB(dbName, Collections.singletonList(entry));
    }

    public static boolean isFortran(PreprocHandler ppHandler) {
        try {
            Language language = Language.valueOf(ppHandler.getLanguage().toString());
            return language == Language.FORTRAN;
        } catch (Throwable e) {
            return false;
        }
    }

    public static ClankCompilationDataBase.Entry createEntry(PreprocHandler ppHandler) {
        ClankIncludeHandlerImpl includeHandler = (ClankIncludeHandlerImpl)ppHandler.getIncludeHandler();
        StartEntry startEntry = includeHandler.getStartEntry();
        String startEntryFilePath = startEntry.getStartFile().toString();
        CharSequence startUrl = CndFileSystemProvider.toUrl(startEntry.getFileSystem(), startEntryFilePath);
        DataBaseEntryBuilder builder = new DataBaseEntryBuilder(startUrl, null);

        builder.setLang(getLang(ppHandler.getLanguage(), startEntryFilePath));
        builder.setLangStd(getLangStd(ppHandler.getLanguageFlavor()));

        // -I or -F
        for (IncludeDirEntry incDir : includeHandler.getUserIncludePaths()) {
            if (incDir.isExistingDirectory()) {
                FSPath fsPath = new FSPath(incDir.getFileSystem(), incDir.getPath());
                FileObject fileObject = fsPath.getFileObject();
                if (fileObject != null && fileObject.isFolder()) {
                    CharSequence incPathUrl = fsPath.getURL();
                    if (CndUtils.isDebugMode()) {
                        CndUtils.assertTrueInConsole(path.is_absolute(new StringRef(create_char$ptr_utf8(incPathUrl))), "why non-absolute path " + incPathUrl + " is used for:" + fsPath + ".PP=", ppHandler);
                    }
                    builder.addUserIncludePath(incPathUrl, incDir.isFramework(), incDir.ignoreSysRoot());
                }
            }
        }
        
        if (!SKIP_COMPILER_SETTINGS) {
            // -isystem
            for (IncludeDirEntry incDir : includeHandler.getSystemIncludePaths()) {
                if (incDir.isExistingDirectory()) {
                    FSPath fsPath = new FSPath(incDir.getFileSystem(), incDir.getPath());
                    FileObject fileObject = fsPath.getFileObject();
                    if (fileObject != null && fileObject.isFolder()) {
                        CharSequence incPathUrl = fsPath.getURL();
                        if (CndUtils.isDebugMode()) {
                            CndUtils.assertTrueInConsole(path.is_absolute(new StringRef(create_char$ptr_utf8(incPathUrl))), "why non-absolute path " + incPathUrl + " is used for:" + fsPath + ".PP=", ppHandler);
                        }
                        builder.addPredefinedSystemIncludePath(incPathUrl, incDir.isFramework(), incDir.ignoreSysRoot());
                    }
                }
            }
        }
        
        // handle -include
        for (IncludeDirEntry incFile : includeHandler.getUserIncludeFilePaths()) {
            // FIXME: relative path can be passed to builder
            
            // TODO: now getUserIncludeFilePaths contains both: user -include and 
            // special -include extracted from compiler built-ins
            // they could be in different file systems; may be they need to be split
            FSPath fsPath = new FSPath(incFile.getFileSystem(), incFile.getPath());
            FileObject fileObject = fsPath.getFileObject();
            if (fileObject != null && fileObject.isData()) {
                CharSequence incPathUrl = fsPath.getURL();
                if (CndUtils.isDebugMode()) {
                    CndUtils.assertTrueInConsole(path.is_absolute(new StringRef(create_char$ptr_utf8(incPathUrl))), "why non-absolute path " + incPathUrl + " is used for:" + fsPath + ".PP=", ppHandler);
                }                
                builder.addIncFile(incPathUrl.toString());
            }
        }

        ClankFileMacroMap macroMap = (ClankFileMacroMap)ppHandler.getMacroMap();
        // -D
        if (!SKIP_COMPILER_SETTINGS) {
            for (String macro : macroMap.getSystemMacroDefinitions()) {
                builder.addPredefinedSystemMacroDef(macro);
            }
        }
        
        for (String macro : macroMap.getUserMacroDefinitions()) {
            builder.addUserMacroDef(macro);
        }

        builder.setFileSystem(ClankFileSystemProvider.getDefault().getFileSystem());
        if (CndFileSystemProvider.isRemote(startEntry.getFileSystem())) {
            CharSequence prefix = CndFileSystemProvider.toUrl(startEntry.getFileSystem(), "/"); //NOI18N
            builder.setAbsPathLookupPrefix(prefix);
        }        
        return builder.createDataBaseEntry();
    }

    private enum LanguageFlavor {
        UNKNOWN(0),
        C(1), C89(2), C99(3),
        CPP98(4), CPP11(8),
        F77(5), F90(6), F95(7),
        DEFAULT(9),
        C11(10), CPP14(11), CPP17(12);
        private final int flavor;

        private LanguageFlavor(int flavor) {
            this.flavor = flavor;
        }

        public int toExternal() {
            return flavor;
        }

        public static LanguageFlavor fromExternal(int i) {
            switch (i) {
                case 0:
                    return UNKNOWN;
                case 1:
                    return C;
                case 2:
                    return C89;
                case 3:
                    return C99;
                case 4:
                    return CPP98;
                case 5:
                    return F77;
                case 6:
                    return F90;
                case 7:
                    return F95;
                case 8:
                    return CPP11;
                case 9:
                    return DEFAULT;
                case 10:
                    return C11;
                case 11:
                    return CPP14;
                case 12:
                    return CPP17;
                default:
                    return UNKNOWN;
            }
        }
    }

    private static LangStandard.Kind getLangStd(CharSequence langFlavor) throws AssertionError {
        LangStandard.Kind out_lang_std = LangStandard.Kind.lang_unspecified;
        String strFlavor = langFlavor.toString();
        LanguageFlavor flavor = LanguageFlavor.UNKNOWN;
        if (strFlavor != null && !strFlavor.isEmpty()) {
            try {
                flavor = LanguageFlavor.valueOf(strFlavor);
            } catch (IllegalArgumentException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        switch (flavor) {
            case DEFAULT:
            case UNKNOWN:
                break;
            case C:
                break;
            case C89:
                out_lang_std = LangStandard.Kind.lang_gnu89;
                break;
            case C99:
                out_lang_std = LangStandard.Kind.lang_gnu99;
                break;
            case CPP98:
                // we don't have flavor for C++98 in APT, but C++03 is used in fact
                out_lang_std = LangStandard.Kind.lang_cxx03;
                break;
            case CPP11:
                out_lang_std = LangStandard.Kind.lang_gnucxx11;
                break;
            case C11:
                out_lang_std = LangStandard.Kind.lang_gnu11;
                break;
            case CPP14:
                // FIXME
                out_lang_std = LangStandard.Kind.lang_gnucxx14;
                break;
            case CPP17:
                // FIXME
                out_lang_std = LangStandard.Kind.lang_gnucxx1z;
                break;
            case F77:
            case F90:
            case F95:
                // FIXME
                out_lang_std = LangStandard.Kind.lang_cxx03;
                break;
            default:
                throw new AssertionError(flavor.name() + " from " + langFlavor);
        }
        return out_lang_std;
    }

    private enum Language {
        C, CPP, FORTRAN, C_HEADER, OTHER
    }

    private static InputKind getLang(CharSequence langStr, String filePath) throws AssertionError {
        InputKind out = InputKind.IK_None;
        Language language = Language.CPP;
        String strLang = langStr.toString();
        if (strLang != null && ! strLang.isEmpty()) {
            try {
                language = Language.valueOf(strLang);
            } catch (IllegalArgumentException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        switch (language) {
            case C:
                out = InputKind.IK_C;
                break;
            case C_HEADER:
                // for headers use C++
                out = InputKind.IK_CXX;
                break;
            case CPP:
                out = InputKind.IK_CXX;
                break;
            case FORTRAN:
                // FIXME
                out = InputKind.IK_CXX;
                break;
            case OTHER:
                // REVIEW:
                out = InputKind.IK_CXX;
                if (filePath.endsWith(".c")) {// NOI18N
                    out = InputKind.IK_C;
                }
                break;
            default:
                throw new AssertionError(language + " from " + langStr);
        }
        return out;
    }
}
