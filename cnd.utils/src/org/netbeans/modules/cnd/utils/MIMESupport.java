/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
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
