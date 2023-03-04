/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.versioning.util;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * Should be implemented by a particular VCS system and returned on a {@link FileObject#getAttribute(java.lang.String)}
 * call with the {@link #PROVIDED_EXTENSIONS_SEARCH_HISTORY} argument.
 * 
 * @author Tomas Stupka
 */
public abstract class SearchHistorySupport {

    public static final String PROVIDED_EXTENSIONS_SEARCH_HISTORY = "ProvidedExtensions.SearchHistorySupport";

    private static final Logger LOG = Logger.getLogger(SearchHistorySupport.class.getName());

    private final File file;

    protected SearchHistorySupport(File file) {
        this.file = file;
    }

    public static SearchHistorySupport getInstance(File file) {
        FileObject fo = FileUtil.toFileObject(file);
        if(fo == null) {
            return null;
        }
        SearchHistorySupport support = (SearchHistorySupport) fo.getAttribute(PROVIDED_EXTENSIONS_SEARCH_HISTORY);
        return support;
    }

    protected File getFile() {
        return file;
    }

    /**
     * @see org.netbeans.modules.bugtracking.spi.VCSAccessor#searchHistory(File, int)
     */
    public boolean searchHistory(int line) throws IOException {
        assert !SwingUtilities.isEventDispatchThread() : "Accessing remote repository. Do not call in  awt!";
        if (!file.exists()) {
            LOG.log(Level.WARNING, "Trying to show history for non-existent file {0}", file.getAbsolutePath());
            return false;
        }
        if (!file.isFile()) {
            LOG.log(Level.WARNING, "Trying to show history for a folder {0}", file.getAbsolutePath());
            return false;
        }
        return searchHistoryImpl(line);
    }

    protected abstract boolean searchHistoryImpl(int line) throws IOException;

}
