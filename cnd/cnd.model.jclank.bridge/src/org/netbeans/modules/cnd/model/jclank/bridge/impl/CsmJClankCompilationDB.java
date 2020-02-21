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
package org.netbeans.modules.cnd.model.jclank.bridge.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;
import org.clang.frontend.InputKind;
import org.clang.frontend.LangStandard;
import org.clang.tools.services.ClankCompilationDataBase;
import org.clang.tools.services.support.DataBaseEntryBuilder;
import org.netbeans.modules.cnd.api.project.NativeFileItem;
import org.netbeans.modules.cnd.api.project.NativeProject;
import org.netbeans.modules.cnd.spi.utils.CndFileSystemProvider;
import org.netbeans.modules.cnd.utils.FSPath;
import org.openide.filesystems.FileObject;

/**
 *
 */
public final class CsmJClankCompilationDB implements ClankCompilationDataBase {

    private final Collection<ClankCompilationDataBase.Entry> compilations;
    private final String name;

    private CsmJClankCompilationDB(Collection<ClankCompilationDataBase.Entry> compilations) {
        this("JClankDB with [" + compilations.size() + "] entries", compilations); // NOI18N
    }

    private CsmJClankCompilationDB(String dbName, Collection<Entry> compilations) {
        this.name = dbName;
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

    public static Set<NativeFileItem> getSources(NativeProject project) {
        // sorted 
        Set<NativeFileItem> srcFiles = new TreeSet<>(new NFIComparator());
        for (NativeFileItem nfi : project.getAllFiles()) {
            if (!nfi.isExcluded()) {
                switch (nfi.getLanguage()) {
                    case C:
                    case CPP:
                        srcFiles.add(nfi);
                        break;
                    default:
                        break;
                }
            }
        }
        if (false/*part of AllFiles*/) srcFiles.addAll(project.getStandardHeadersIndexers());
        return srcFiles;
    }

    public static ClankCompilationDataBase convertNativeFileItems(Collection<NativeFileItem> nfis, String dbName, boolean useURL) {
        Collection<ClankCompilationDataBase.Entry> compilations = new ArrayList<>();
        for (NativeFileItem nfi : nfis) {
            Entry entry = createEntry(nfi, useURL);
            assert entry != null;
            compilations.add(entry);
        }
        return new CsmJClankCompilationDB(dbName, compilations);
    }

    public static ClankCompilationDataBase convertProject(NativeProject prj, boolean useURL) {
        return convertNativeFileItems(getSources(prj), prj.getProjectDisplayName(), useURL);
    }

    public static Collection<ClankCompilationDataBase> convertProjects(Collection<NativeProject> prjs, boolean useURL) {
        Collection<ClankCompilationDataBase> out = new ArrayList<>();
        for (NativeProject prj : prjs) {
            out.add(convertProject(prj, useURL));
        }
        return out;
    }

    public static ClankCompilationDataBase.Entry createEntry(NativeFileItem nfi, boolean useURL) {
        CharSequence mainFile = useURL ? CndFileSystemProvider.toUrl(FSPath.toFSPath(nfi.getFileObject())) : nfi.getAbsolutePath();
        DataBaseEntryBuilder builder = new DataBaseEntryBuilder(mainFile, null);

        builder.setLang(getLang(nfi)).setLangStd(getLangStd(nfi));

        // -I or -F
        for (org.netbeans.modules.cnd.api.project.IncludePath incPath : nfi.getUserIncludePaths()) {
            FileObject fileObject = incPath.getFSPath().getFileObject();
            if (fileObject != null && fileObject.isFolder()) {
                CharSequence path = useURL ? incPath.getFSPath().getURL() : incPath.getFSPath().getPath();
                builder.addUserIncludePath(path, incPath.isFramework(), incPath.ignoreSysRoot());
            }
        }
        // -isystem
        for (org.netbeans.modules.cnd.api.project.IncludePath incPath : nfi.getSystemIncludePaths()) {
            FileObject fileObject = incPath.getFSPath().getFileObject();
            if (fileObject != null && fileObject.isFolder()) {
                CharSequence path = useURL ? incPath.getFSPath().getURL() : incPath.getFSPath().getPath();
                builder.addPredefinedSystemIncludePath(path, incPath.isFramework(), incPath.ignoreSysRoot());
            }
        }

        // system pre-included headers
        for (FSPath fSPath : nfi.getSystemIncludeHeaders()) {
            FileObject fileObject = fSPath.getFileObject();
            if (fileObject != null && fileObject.isData()) {
                String path = useURL ? fSPath.getURL().toString() : fSPath.getPath();
                builder.addIncFile(path);
            }
        }

        // handle -include
        for (FSPath fSPath : nfi.getIncludeFiles()) {
            FileObject fileObject = fSPath.getFileObject();
            if (fileObject != null && fileObject.isData()) {
                String path = useURL ? fSPath.getURL().toString() : fSPath.getPath();
                builder.addIncFile(path);
            }
        }

        // -D
        for (String macro : nfi.getSystemMacroDefinitions()) {
            builder.addPredefinedSystemMacroDef(macro);
        }
        for (String macro : nfi.getUserMacroDefinitions()) {
            builder.addUserMacroDef(macro);
        }

        return builder.createDataBaseEntry();
    }

    private static LangStandard.Kind getLangStd(NativeFileItem startEntry) throws AssertionError {
        LangStandard.Kind lang_std = LangStandard.Kind.lang_unspecified;
        switch (startEntry.getLanguageFlavor()) {
            case DEFAULT:
            case UNKNOWN:
                break;
            case C:
                break;
            case C89:
                lang_std = LangStandard.Kind.lang_gnu89;
                break;
            case C99:
                lang_std = LangStandard.Kind.lang_gnu99;
                break;
            case CPP98:
                // we don't have flavor for C++98 in APT, but C++03 is used in fact
                lang_std = LangStandard.Kind.lang_cxx03;
                break;
            case CPP11:
                lang_std = LangStandard.Kind.lang_gnucxx11;
                break;
            case C11:
                lang_std = LangStandard.Kind.lang_gnu11;
                break;
            case CPP14:
                // FIXME
                lang_std = LangStandard.Kind.lang_gnucxx14;
                break;
            case CPP17:
                // FIXME
                lang_std = LangStandard.Kind.lang_gnucxx1z;
                break;
            case F77:
            case F90:
            case F95:
            default:
                throw new AssertionError(startEntry.getLanguageFlavor().name());
        }
        return lang_std;
    }

    private static InputKind getLang(NativeFileItem startEntry) throws AssertionError {
        InputKind lang = InputKind.IK_None;
        switch (startEntry.getLanguage()) {
            case C:
            case C_HEADER:
                lang = InputKind.IK_C;
                break;
            case CPP:
                lang = InputKind.IK_CXX;
                break;
            case FORTRAN:
            case OTHER:
            default:
                throw new AssertionError(startEntry.getLanguage().name());
        }
        return lang;
    }

    private static final class NFIComparator implements Comparator<NativeFileItem> {

        public NFIComparator() {
        }

        @Override
        public int compare(NativeFileItem o1, NativeFileItem o2) {
            return o1.getAbsolutePath().compareTo(o2.getAbsolutePath());
        }
    }
}
