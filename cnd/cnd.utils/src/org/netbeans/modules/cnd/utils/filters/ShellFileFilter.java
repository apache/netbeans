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
import org.netbeans.modules.cnd.utils.MIMEExtensions;
import org.netbeans.modules.cnd.utils.MIMENames;
import org.netbeans.modules.cnd.utils.cache.CndFileUtils;
import org.netbeans.modules.cnd.utils.FileAndFileObjectFilter;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;

public class ShellFileFilter extends FileAndFileObjectFilter {

    private static ShellFileFilter instance = null;

    public ShellFileFilter() {
        super();
    }

    public static ShellFileFilter getInstance() {
        if (instance == null) {
            instance = new ShellFileFilter();
        }
        return instance;
    }

    @Override
    public String getDescription() {
        return NbBundle.getMessage(ShellFileFilter.class, "FILECHOOSER_SHELL_FILEFILTER"); // NOI18N
    }

    @Override
    protected boolean mimeAccept(File f) {
        return mimeAccept(CndFileUtils.toFileObject(f));
    }

    @Override
    protected boolean mimeAccept(FileObject fo) {
        if (fo != null) {
            if (fo.isValid()) {
                return MIMENames.SHELL_MIME_TYPE.equals(FileUtil.getMIMEType(fo, MIMENames.SHELL_MIME_TYPE));
            } else {
                return MIMEExtensions.isRegistered(MIMENames.SHELL_MIME_TYPE, FileUtil.getExtension(fo.getNameExt()));
            }
        }
        return false;
    }

    @Override
    protected String[] getSuffixes() {
        return new String[]{};
    }
}
