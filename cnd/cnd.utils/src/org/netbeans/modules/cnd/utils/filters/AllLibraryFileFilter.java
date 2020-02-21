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

package org.netbeans.modules.cnd.utils.filters;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.cnd.utils.FileFilterFactory.AbstractFileAndFileObjectFilter;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

public class AllLibraryFileFilter extends AbstractFileAndFileObjectFilter {

    private static AllLibraryFileFilter instance = null;

    private final List<AbstractFileAndFileObjectFilter> filters = new ArrayList<AbstractFileAndFileObjectFilter>();

    public static AllLibraryFileFilter getInstance() {
        if (instance == null) {
            instance = new AllLibraryFileFilter();
        }
        return instance;
    }

    private AllLibraryFileFilter() {
        filters.add(ElfStaticLibraryFileFilter.getInstance());
        if (Utilities.isWindows()) {
            filters.add(PeDynamicLibraryFileFilter.getInstance());
            filters.add(PeStaticLibraryFileFilter.getInstance());
        } else if (Utilities.getOperatingSystem() == Utilities.OS_MAC) {
            filters.add(MacOSXDynamicLibraryFileFilter.getInstance());
        } else {
            filters.add(ElfDynamicLibraryFileFilter.getInstance());
        }
    }

    @Override
    public String getDescription() {
        return NbBundle.getMessage(AllLibraryFileFilter.class, "ALL_LIB_FILTER"); // NOI18N
    }

    @Override
    public boolean accept(File f) {
        for(AbstractFileAndFileObjectFilter filter: filters) {
            if (filter.accept(f)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean accept(FileObject fileObject) {
        for(AbstractFileAndFileObjectFilter filter: filters) {
            if (filter.accept(fileObject)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String getSuffixesAsString() {
        return "";
    }
}
