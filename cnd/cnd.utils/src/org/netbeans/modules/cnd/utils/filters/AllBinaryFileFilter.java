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
import org.netbeans.modules.cnd.utils.FileAndFileObjectFilter;
import org.netbeans.modules.cnd.utils.MIMEExtensions;
import org.netbeans.modules.cnd.utils.MIMENames;
import org.netbeans.modules.cnd.utils.MIMESupport;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

public class AllBinaryFileFilter extends FileAndFileObjectFilter {

    private static AllBinaryFileFilter instance = null;
    private static String[] suffixes = null;

    public static AllBinaryFileFilter getInstance() {
        if (instance == null) {
            instance = new AllBinaryFileFilter();
        }
        return instance;
    }

    @Override
    public String getDescription() {
        return NbBundle.getMessage(AllBinaryFileFilter.class, "FILECHOOSER_All_BINARIES_FILEFILTER"); // NOI18N
    }

    @Override
    protected boolean mimeAccept(File f) {
        //if (FileUtil.getExtension(f.getName()).length() == 0) {
            return MIMENames.isBinary(MIMESupport.getBinaryFileMIMEType(f));
        //}
        //return super.mimeAccept(f);
    }

    @Override
    protected boolean mimeAccept(FileObject f) {
        //if (FileUtil.getExtension(f.getName()).length() == 0) {
            return MIMENames.isBinary(MIMESupport.getBinaryFileMIMEType(f));
        //}
        //return super.mimeAccept(f);
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
        allSuffixes.addAll(MIMEExtensions.get(MIMENames.EXE_MIME_TYPE).getValues());
        allSuffixes.addAll(MIMEExtensions.get(MIMENames.ELF_EXE_MIME_TYPE).getValues());
        allSuffixes.addAll(MIMEExtensions.get(MIMENames.ELF_CORE_MIME_TYPE).getValues());
        allSuffixes.addAll(MIMEExtensions.get(MIMENames.ELF_SHOBJ_MIME_TYPE).getValues());
        allSuffixes.addAll(MIMEExtensions.get(MIMENames.ELF_STOBJ_MIME_TYPE).getValues());
        allSuffixes.addAll(MIMEExtensions.get(MIMENames.ELF_GENERIC_MIME_TYPE).getValues());
        allSuffixes.addAll(MIMEExtensions.get(MIMENames.ELF_OBJECT_MIME_TYPE).getValues());
        return allSuffixes.toArray(new String[allSuffixes.size()]);
    }
}
