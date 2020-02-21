/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2015 Sun Microsystems, Inc.
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
