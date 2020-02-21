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

import org.netbeans.modules.cnd.utils.FileAndFileObjectFilter;
import org.openide.util.NbBundle;

public class MacOSXDynamicLibraryFileFilter extends FileAndFileObjectFilter {

    private static final String suffixes[] = {"dylib"}; // NOI18N
    private static MacOSXDynamicLibraryFileFilter instance = null;

    public MacOSXDynamicLibraryFileFilter() {
	super();
    }

    public static MacOSXDynamicLibraryFileFilter getInstance() {
	if (instance == null) {
            instance = new MacOSXDynamicLibraryFileFilter();
        }
	return instance;
    }

    @Override
    public String getDescription() {
        return NbBundle.getMessage(MacOSXDynamicLibraryFileFilter.class, "MACOSX_DYNAMIC_LIB_FILTER"); // NOI18N
    }

    @Override
    protected String[] getSuffixes() {
        return suffixes;
    }
}
