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
import java.util.HashSet;
import java.util.Set;
import org.netbeans.modules.cnd.utils.MIMEExtensions;
import org.netbeans.modules.cnd.utils.MIMENames;
import org.netbeans.modules.cnd.utils.MIMESupport;
import org.netbeans.modules.cnd.utils.FileAndFileObjectFilter;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;

public class AllSourceFileFilter extends FileAndFileObjectFilter {

    private static AllSourceFileFilter instance = null;
    private static String[] suffixes = null;

    public static AllSourceFileFilter getInstance() {
        if (instance == null) {
            instance = new AllSourceFileFilter();
        }
        return instance;
    }

    @Override
    public String getDescription() {
        return NbBundle.getMessage(AllSourceFileFilter.class, "FILECHOOSER_All_SOURCES_FILEFILTER"); // NOI18N
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
    public String[] getSuffixes() {
        if (suffixes == null) {
            suffixes = getAllSuffixes();
        }
        return suffixes;
    }
    
    private String[] getAllSuffixes() {
        Set<String> allSuffixes = new HashSet<String>();
        allSuffixes.addAll(MIMEExtensions.get(MIMENames.CPLUSPLUS_MIME_TYPE).getValues());
        allSuffixes.addAll(MIMEExtensions.get(MIMENames.C_MIME_TYPE).getValues());
        allSuffixes.addAll(MIMEExtensions.get(MIMENames.HEADER_MIME_TYPE).getValues());
        allSuffixes.addAll(MIMEExtensions.get(MIMENames.FORTRAN_MIME_TYPE).getValues());
        allSuffixes.addAll(MIMEExtensions.get(MIMENames.ASM_MIME_TYPE).getValues());
        return allSuffixes.toArray(new String[allSuffixes.size()]);
    }
}
