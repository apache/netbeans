/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.cnd.apt.impl.support;

import java.util.List;
import org.netbeans.modules.cnd.apt.debug.APTTraceFlags;
import org.netbeans.modules.cnd.apt.structure.APTInclude;
import org.netbeans.modules.cnd.apt.structure.APTIncludeNext;
import org.netbeans.modules.cnd.apt.support.APTFileSearch;
import org.netbeans.modules.cnd.apt.support.APTIncludeResolver;
import org.netbeans.modules.cnd.apt.support.APTMacroCallback;
import org.netbeans.modules.cnd.apt.support.IncludeDirEntry;
import org.netbeans.modules.cnd.apt.utils.APTIncludeUtils;
import org.netbeans.modules.cnd.apt.support.ResolvedPath;
import org.netbeans.modules.cnd.utils.CndPathUtilities;
import org.netbeans.modules.cnd.utils.FSPath;
import org.netbeans.modules.cnd.utils.cache.FilePathCache;
import org.openide.filesystems.FileSystem;

/**
 * implementation of include resolver
 */
public class APTIncludeResolverImpl implements APTIncludeResolver {
    private final int baseFileIncludeDirIndex;
    private final CharSequence baseFile;
    private final List<IncludeDirEntry> systemIncludePaths;
    private final List<IncludeDirEntry> userIncludePaths;
    private final APTFileSearch fileSearch;
    private final FileSystem fileSystem;
    //private final int hashCode;
//    private static final boolean TRACE = Boolean.getBoolean("apt.trace.resolver");

    public APTIncludeResolverImpl(FileSystem fs, CharSequence path, int baseFileIncludeDirIndex,
                                    List<IncludeDirEntry> systemIncludePaths,
                                    List<IncludeDirEntry> userIncludePaths, APTFileSearch fileSearch) {
        this.fileSystem = fs;
        this.baseFile = FilePathCache.getManager().getString(path);
        this.systemIncludePaths = systemIncludePaths;
        this.userIncludePaths = userIncludePaths;
        //int aHashCode = 0;
        //if (APTTraceFlags.USE_INCLIDE_RESOLVER_CACHE) {
        //    for(IncludeDirEntry entry: systemIncludePaths) {
        //        aHashCode+=entry.hashCode()*31;
        //    }
        //    for(IncludeDirEntry entry: userIncludePaths) {
        //        aHashCode+=entry.hashCode()*19;
        //    }
        //}
        //hashCode = aHashCode;
        this.baseFileIncludeDirIndex = baseFileIncludeDirIndex;
        this.fileSearch = fileSearch;
//        if (TRACE) {
//            System.err.printf("APTIncludeResolverImpl.ctor %s %s systemIncludePaths: %s\n", fileSystem, path, systemIncludePaths); // NOI18N
//        }
    }


    @Override
    public ResolvedPath resolveInclude(APTInclude apt, APTMacroCallback callback) {
        ResolvedPath result = resolveFilePath(apt.getFileName(callback), apt.isSystem(callback), false);
//        if (TRACE) {
//            System.err.printf("APTIncludeResolverImpl.resolveInclude %s in %s -> %s\n", apt.getFileName(callback), baseFile, result);
//            if (result == null) {
//                result = resolveFilePath(apt.getFileName(callback), apt.isSystem(callback), false);
//            }
//        }
        return result;
    }

    @Override
    public ResolvedPath resolveIncludeNext(APTIncludeNext apt, APTMacroCallback callback) {
        ResolvedPath result = resolveFilePath(apt.getFileName(callback), apt.isSystem(callback), true);
//        if (TRACE) {
//            System.err.printf("APTIncludeResolverImpl.resolveIncludeNext %s in %s -> %s\n", apt.getFileName(callback), baseFile, result);
//        }
        return result;
    }

    public CharSequence getBasePath() {
        return baseFile;
    }

    //@Override
    //public int hashCode() {
    //    return hashCode;
    //}

    //@Override
    //public boolean equals(Object obj) {
    //    if (obj == null) {
    //        return false;
    //    }
    //    if (getClass() != obj.getClass()) {
    //        return false;
    //    }
    //    final APTIncludeResolverImpl other = (APTIncludeResolverImpl) obj;
    //    if (this.systemIncludePaths != other.systemIncludePaths && (this.systemIncludePaths == null || !this.systemIncludePaths.equals(other.systemIncludePaths))) {
    //        return false;
    //    }
    //    if (this.userIncludePaths != other.userIncludePaths && (this.userIncludePaths == null || !this.userIncludePaths.equals(other.userIncludePaths))) {
    //        return false;
    //    }
    //    return true;
    //}

    ////////////////////////////////////////////////////////////////////////////
    // implementation details
    //private static int count = 0;
    //private static int hit = 0;

    private ResolvedPath resolveFilePath(String includedFile, boolean system, boolean includeNext) {
        ResolvedPath result = null;
        if (includedFile != null && (includedFile.length() > 0)) {
            result = APTIncludeUtils.resolveAbsFilePath(fileSystem, includedFile);
            if (result == null && !system && !includeNext) {
                // for <system> "current dir" has lowest priority
                // for #include_next should start from another dir
                result = APTIncludeUtils.resolveFilePath(fileSystem, includedFile, baseFile);
            }
            if ( result == null) {
                if (includeNext) {
                    PathsCollectionIterator paths =  new PathsCollectionIterator(userIncludePaths, systemIncludePaths, baseFileIncludeDirIndex+1);
                    result = APTIncludeUtils.resolveFilePath(paths, includedFile, baseFileIncludeDirIndex+1);
                } else {
                    //if (APTTraceFlags.USE_INCLIDE_RESOLVER_CACHE) {
                    //    //count++;
                    //    result = ResolverResultsCache.getResolvedPath(includedFile, this);
                    //}
                    //if (result == null) {
                        PathsCollectionIterator paths = new PathsCollectionIterator(userIncludePaths, systemIncludePaths, 0);
                        result = APTIncludeUtils.resolveFilePath(paths, includedFile, 0);
                        //if (APTTraceFlags.USE_INCLIDE_RESOLVER_CACHE) {
                        //    if (result != null) {
                        //        ResolverResultsCache.putResolvedPath(includedFile, this, result);
                        //    }
                        //}
                    //} else {
                        //hit++;
                        //if (hit%10000 == 0) {
                        //    System.err.println("Count = "+count+" hit = "+hit);
                        //}
                    //}
                }
            }
            if ( result == null && system && !includeNext) {
                // <system> was skipped above, check now, but not for #include_next
                result = APTIncludeUtils.resolveFilePath(fileSystem, includedFile, baseFile);
            }
        }
        if (result == null && fileSearch != null) {
            if (APTTraceFlags.FIX_NOT_FOUND_INCLUDES) {
                FSPath path = fileSearch.searchInclude(includedFile, baseFile);
                if (path != null) {
                    result = APTIncludeUtils.resolveFilePath(path.getFileSystem(), CndPathUtilities.getBaseName(path.getPath()), path.getPath());
                }
            }
        }
        return result;
    }

    @Override
    public String toString() {
        return "APTIncludeResolverImpl{\n" + "baseFileIncludeDirIndex=" + baseFileIncludeDirIndex + ",\nbaseFile=" + baseFile + ",\nfileSystem=" + fileSystem.getDisplayName() +  // NOI18N
                ",\nsystemIncludePaths=" + systemIncludePaths + ",\nuserIncludePaths=" + userIncludePaths + ",\nfileSearch=" + fileSearch + "\n}"; // NOI18N
    }


}
