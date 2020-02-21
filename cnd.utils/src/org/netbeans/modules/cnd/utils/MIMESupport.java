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
import org.netbeans.modules.cnd.utils.cache.CndFileUtils;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Parameters;

/**
 * Support methods for CND specific mime types
 */
public final class MIMESupport {

    private static final String[] SOURCE_MIME_TYPES = new String[] {
        MIMENames.CPLUSPLUS_MIME_TYPE, MIMENames.C_MIME_TYPE, MIMENames.HEADER_MIME_TYPE,
        MIMENames.FORTRAN_MIME_TYPE, MIMENames.ASM_MIME_TYPE,};

    private static final String[] BINARY_MIME_TYPES = new String[] {
        MIMENames.EXE_MIME_TYPE, MIMENames.ELF_EXE_MIME_TYPE,
        MIMENames.ELF_CORE_MIME_TYPE, MIMENames.ELF_SHOBJ_MIME_TYPE,
        MIMENames.ELF_STOBJ_MIME_TYPE, MIMENames.ELF_GENERIC_MIME_TYPE,
        MIMENames.ELF_OBJECT_MIME_TYPE};

    private MIMESupport() {
        // no instantiation of utility class
    }

    /**
     * tries to detect mime type of file checking cnd known types first
     * @param file file object to check
     * @return one of mime types or "content/unknown"
     */
    public static String getSourceFileMIMEType(FileObject fo) {
        Parameters.notNull("file object", fo); // NOI18N
        // try fast check
        String mime = FileUtil.getMIMEType(fo, SOURCE_MIME_TYPES);
        if (mime == null || mime.equals("content/unknown")) { // NOI18N
            // now full check
            mime = FileUtil.getMIMEType(fo);
        }
        return mime;
    }

    /**
     * tries to detect mime type of file checking cnd known types first
     * @param file file to check
     * @return one of mime types or "content/unknown"
     */
    public static String getSourceFileMIMEType(File file) {
        FileObject fo = CndFileUtils.toFileObject(CndFileUtils.normalizeFile(file));
        String mime;
        if (fo != null && fo.isValid()) {
            // try fast check
            mime = getSourceFileMIMEType(fo);
        } else {
            mime = getKnownSourceFileMIMETypeByExtension(file.getPath());
        }
        return mime != null ? mime : "content/unknown"; // NOI18N
    }

    /**
     * tries to detect mime type by file path extension only
     * more precise (but possibly slower) method is @see getSourceMIMEType(File file).
     * This method can not detect header files without extensions, while
     * @see getSourceMIMEType(File file) can
     * @param filePathOrName path or name to check
     * @return one of cnd source mime types (@see MIMENames.SOURCE_MIME_TYPES) or null
     */
    public static String getKnownSourceFileMIMETypeByExtension(String filePathOrName) {
        String fileName = CndPathUtilities.getBaseName(filePathOrName);
        // check by known file extension
        String ext = FileUtil.getExtension(fileName);
        for (String mimeType : SOURCE_MIME_TYPES) {
            if (MIMEExtensions.isRegistered(mimeType, ext)) {
                return mimeType;
            }
        }
        return null;
    }


    /**
     * tries to detect mime type of file checking cnd known types first
     * @param file file object to check
     * @return one of mime types or "content/unknown"
     */
    public static String getBinaryFileMIMEType(FileObject fo) {
        Parameters.notNull("file object", fo); // NOI18N
        // try fast check
        String mime = FileUtil.getMIMEType(fo, BINARY_MIME_TYPES);
        if (mime == null) {
            // now full check
            mime = FileUtil.getMIMEType(fo);
        }
        return mime;
    }

    /**
     * tries to detect mime type of file checking cnd known types first
     * @param file file to check
     * @return one of mime types or "content/unknown"
     */
    public static String getBinaryFileMIMEType(File file) {
        FileObject fo = CndFileUtils.toFileObject(CndFileUtils.normalizeFile(file));
        String mime;
        if (fo != null && fo.isValid()) {
            // try fast check
            mime = getBinaryFileMIMEType(fo);
        } else {
            mime = getKnownBinaryFileMIMETypeByExtension(file.getPath());
        }
        return mime != null ? mime : "content/unknown"; // NOI18N
    }

    /**
     * tries to detect mime type by file path extension only
     * more precise (but possibly slower) method is @see getSourceMIMEType(File file).
     * This method can not detect header files without extensions, while
     * @see getSourceMIMEType(File file) can
     * @param filePathOrName path or name to check
     * @return one of cnd source mime types (@see MIMENames.SOURCE_MIME_TYPES) or null
     */
    public static String getKnownBinaryFileMIMETypeByExtension(String filePathOrName) {
        String fileName = CndPathUtilities.getBaseName(filePathOrName);
        // check by known file extension
        String ext = FileUtil.getExtension(fileName);
        for (String mimeType : BINARY_MIME_TYPES) {
            if (MIMEExtensions.isRegistered(mimeType, ext)) {
                return mimeType;
            }
        }
        return null;
    }

}
