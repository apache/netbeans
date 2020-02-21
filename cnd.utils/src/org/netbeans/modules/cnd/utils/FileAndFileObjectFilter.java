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

import java.io.File;
import org.netbeans.modules.cnd.utils.FileFilterFactory.AbstractFileAndFileObjectFilter;
import org.netbeans.modules.cnd.utils.cache.CndFileUtils;
import org.openide.filesystems.FileObject;

/**
 *
 */
/* A combination of FileFilter and FileObjectFilter */
public abstract class FileAndFileObjectFilter extends AbstractFileAndFileObjectFilter {

    @Override
    public final boolean accept(File f) {
        if (f != null) {
            if (f.isDirectory()) {
                return true;
            }
            if (accept(f.getName())) {
                return true;
            }
            return mimeAccept(f);
        }
        return false;
    }

    protected boolean mimeAccept(File f) {
        return false;
    }

    @Override
    public final boolean accept(FileObject f) {
        if (f != null) {
            if (f.isFolder()) {
                return true;
            }
            if (accept(f.getNameExt())) {
                return true;
            }
            return mimeAccept(f);
        }
        return false;
    }

    protected boolean mimeAccept(FileObject f) {
        return false;
    }

    protected final boolean accept(String fileName) {
        int index = fileName.lastIndexOf('.');
        if (index >= 0) {
            // Match suffix
            String suffix = fileName.substring(index + 1);
            if (amongSuffixes(suffix, getSuffixes())) {
                return true;
            }
        } else {
            // Match entire name
            if (amongSuffixes(fileName, getSuffixes())) {
                return true;
            }
        }
        return false;
    }

    protected abstract String[] getSuffixes();

    @Override
    public String getSuffixesAsString() {
        String[] suffixes = getSuffixes();
        StringBuilder ret = new StringBuilder();
        for (int i = 0; i < suffixes.length; i++) {
            if (0 < i) {
                ret.append(' '); // NOI18N
            }
            ret.append('.').append(suffixes[i]); // NOI18N
        }
        return ret.toString();
    }

    private boolean amongSuffixes(String suffix, String[] suffixes) {
        for (int i = 0; i < suffixes.length; i++) {
            if (areFilenamesEqual(suffixes[i], suffix)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return getDescription();
    }

    protected boolean areFilenamesEqual(String firstFile, String secondFile) {
        return CndFileUtils.isSystemCaseSensitive() ? firstFile.equals(secondFile) : firstFile.equalsIgnoreCase(secondFile);
    }
}
