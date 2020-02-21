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
import org.netbeans.modules.cnd.utils.FileAndFileObjectFilter;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

public class MakefileFileFilter extends FileAndFileObjectFilter {

    private static final String suffixes[] = {"mk", "Makefile", "makefile"}; // NOI18N
    private static MakefileFileFilter instance = null;

    public MakefileFileFilter() {
	super();
    }

    public static MakefileFileFilter getInstance() {
	if (instance == null) {
            instance = new MakefileFileFilter();
        }
	return instance;
    }
    
    @Override
    public String getDescription() {
        return NbBundle.getMessage(MakefileFileFilter.class, "FILECHOOSER_MAKEFILE_FILEFILTER"); // NOI18N
    }
    
    @Override
    protected boolean mimeAccept(File f) {
        return checkMakefileName(f.getName());
    }

    @Override
    protected boolean mimeAccept(FileObject f) {
        return checkMakefileName(f.getNameExt());
    }

    private boolean checkMakefileName(String name) {
        if (name.indexOf("Makefile") >= 0 || // NOI18N
            name.indexOf("makefile") >= 0) { // NOI18N
            return true;
        }
        return false;
    }

    @Override
    protected String[] getSuffixes() {
        return suffixes;
    }
}
