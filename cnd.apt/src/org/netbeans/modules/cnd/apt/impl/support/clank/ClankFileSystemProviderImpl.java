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

import org.clang.basic.vfs.FileSystem;
import org.clang.tools.services.spi.ClankFileSystemProvider;
import org.llvm.adt.IntrusiveRefCntPtr;
import org.netbeans.modules.cnd.apt.debug.APTTraceFlags;
import org.netbeans.modules.cnd.utils.cache.CharSequenceUtils;
import org.openide.util.Utilities;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 */
@ServiceProvider(service = ClankFileSystemProvider.class, position = 100)
public class ClankFileSystemProviderImpl extends  ClankFileSystemProvider{
    
    public static final String RFS_PREFIX = "rfs:"; //NOI18N

    
    public ClankFileSystemProviderImpl() {
        
    }    

    @Override
    public IntrusiveRefCntPtr<FileSystem> getFileSystem() {
        boolean useFS;
        if (APTTraceFlags.ALWAYS_USE_NB_FS || Utilities.isWindows()) {
            useFS = true;
        } else {
            useFS = false;
        }
        return useFS ? new IntrusiveRefCntPtr<FileSystem>(ClankFileObjectBasedFileSystem.getInstance()) : null;
    }

    public static String getPathFromUrl(String path) {
        if (CharSequenceUtils.startsWith(path, RFS_PREFIX)) {
            // examples:
            // rfs://user@host:22/usr/include
            // rfs:user@host:22/usr/include
            // rfs://user@host:22
            // rfs:user@host:22
            int pos = CharSequenceUtils.indexOf(path, ":", RFS_PREFIX.length()); //NOI18N
            if (pos > 0) {
                pos++;
                while (pos < path.length() && Character.isDigit(path.charAt(pos))) {
                    pos++;
                }
                return path.substring(pos, path.length());
            } else {
                throw new IllegalArgumentException("The path " + path + " starts with " + RFS_PREFIX + //NOI18N
                        " but does not contain a colon after it"); //NOI18N                
            }
        } else {
            return path;
        }
    }
}
