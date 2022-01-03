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

package org.netbeans.modules.cnd.apt.utils;

import java.util.Iterator;
import org.netbeans.modules.cnd.apt.debug.APTTraceFlags;
import org.netbeans.modules.cnd.apt.impl.support.SupportAPIAccessor;
import org.netbeans.modules.cnd.apt.support.IncludeDirEntry;
import org.netbeans.modules.cnd.apt.support.ResolvedPath;
import org.netbeans.modules.cnd.utils.CndPathUtilities;
import org.netbeans.modules.cnd.utils.cache.CharSequenceUtils;
import org.netbeans.modules.cnd.utils.cache.CndFileUtils;
import org.netbeans.modules.cnd.utils.cache.FilePathCache;
import org.openide.filesystems.FileSystem;

/**
 *
 */
public class APTIncludeUtils {
    
    private APTIncludeUtils() {
    }
    
    /** 
     * finds file relatively to the baseFile 
     * caller must check that resolved path is not the same as base file
     * to prevent recursive inclusions 
     */
    public static ResolvedPath resolveFilePath(FileSystem fs, String inclString, CharSequence baseFile) {
        if (baseFile != null) {
            String folder = CndPathUtilities.getDirName(baseFile.toString());
            String absolutePath = folder + CndFileUtils.getFileSeparatorChar(fs) + inclString;
            if (isExistingFile(fs, absolutePath)) {
                absolutePath = normalize(fs, absolutePath);
                folder = normalize(fs, folder);
                return new ResolvedPath(fs, FilePathCache.getManager().getString(folder), absolutePath, true, 0);
            }
        }
        return null;
    }
    
    public static ResolvedPath resolveAbsFilePath(FileSystem fs, String absFile) {
        if (APTTraceFlags.APT_ABSOLUTE_INCLUDES) {
            if (CndPathUtilities.isPathAbsolute(absFile) && isExistingFile(fs, absFile) ) {
                absFile = normalize(fs, absFile);
                String parent = CndPathUtilities.getDirName(absFile);
                return new ResolvedPath(fs, FilePathCache.getManager().getString(parent), absFile, false, 0);
            }
        }   
        return null;
    }    
    
    public static ResolvedPath resolveFilePath(Iterator<IncludeDirEntry> searchPaths, String anIncludedFile, int dirOffset) {        
        SupportAPIAccessor accessor = SupportAPIAccessor.get();
        while( searchPaths.hasNext() ) {
            IncludeDirEntry dirPrefix = searchPaths.next();
            if (accessor.isExistingDirectory(dirPrefix)) {
                FileSystem fs = dirPrefix.getFileSystem();
                char fileSeparatorChar = CndFileUtils.getFileSeparatorChar(fs);
                String includedFile = anIncludedFile.replace('/', fileSeparatorChar);
                CharSequence prefix = dirPrefix.getAsSharedCharSequence();
                int len = prefix.length();
                String absolutePath;
                if (len > 0 && prefix.charAt(len - 1) == fileSeparatorChar) {
                    absolutePath = prefix + includedFile;
                } else {
                    absolutePath = CharSequenceUtils.toString(prefix, fileSeparatorChar, includedFile);
                }
                if (isExistingFile(fs, absolutePath)) {
                    return new ResolvedPath(fs, prefix, normalize(fs, absolutePath), false, dirOffset);
                } else {
                    if (dirPrefix.isFramework()) {
                        int i = includedFile.indexOf('/'); // NOI18N
                        if (i > 0) {
                            // possible it is framework include (see IZ#160043)
                            // #include <GLUT/glut.h>
                            // header is located in the /System/Library/Frameworks/GLUT.framework/Headers
                            // system path is /System/Library/Frameworks
                            // So convert framework path
                            absolutePath = dirPrefix.getPath()+"/"+includedFile.substring(0,i)+".framework/Headers"+includedFile.substring(i); // NOI18N
                            if (isExistingFile(fs, absolutePath)) {
                                return new ResolvedPath(fs, dirPrefix.getAsSharedCharSequence(), normalize(fs, absolutePath), false, dirOffset);
                            }
                        }
                    }
                }
            }
            dirOffset++;
        }
        return null;
    }

    private static String normalize(FileSystem fs, String path) {
        return CndFileUtils.normalizeAbsolutePath(fs, path);
    }

    private static boolean isExistingFile(FileSystem fs, String filePath) {
        return CndFileUtils.isExistingFile(fs, filePath);
    }
}
