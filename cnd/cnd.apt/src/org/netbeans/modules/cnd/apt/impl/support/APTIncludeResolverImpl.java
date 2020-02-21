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
