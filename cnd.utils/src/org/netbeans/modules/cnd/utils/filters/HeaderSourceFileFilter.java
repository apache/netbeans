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
import org.netbeans.modules.cnd.utils.MIMESupport;
import org.netbeans.modules.cnd.utils.FileAndFileObjectFilter;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;

public class HeaderSourceFileFilter extends FileAndFileObjectFilter {

    private static HeaderSourceFileFilter instance = null;
    private String[] suffixList = null;

    public static HeaderSourceFileFilter getInstance() {
        if (instance == null) {
            instance = new HeaderSourceFileFilter();
        }
        return instance;
    }

    @Override
    protected boolean mimeAccept(File f) {
        if (FileUtil.getExtension(f.getName()).length() == 0) {
            return MIMENames.HEADER_MIME_TYPE.equals(MIMESupport.getSourceFileMIMEType(f));
        }
        return super.mimeAccept(f);
    }

    @Override
    protected boolean mimeAccept(FileObject f) {
        if (f.getExt().isEmpty()) {
            MIMENames.HEADER_MIME_TYPE.equals(MIMESupport.getSourceFileMIMEType(f));
        }
        return super.mimeAccept(f);
    }

    @Override
    public String getDescription() {
        return NbBundle.getMessage(HeaderSourceFileFilter.class, "FILECHOOSER_HEADER_SOURCES_FILEFILTER", getSuffixesAsString()); // NOI18N
    }

    @Override
    public String[] getSuffixes() {
        if (suffixList == null) {
            suffixList = MIMEExtensions.get(MIMENames.HEADER_MIME_TYPE).getValues().toArray(new String[]{});
        }
        return suffixList;
    }
}
