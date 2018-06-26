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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */
package org.netbeans.modules.web.jsf.editor.index;

import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import org.netbeans.modules.parsing.spi.indexing.Context;
import org.netbeans.modules.parsing.spi.indexing.support.IndexDocument;
import org.netbeans.modules.parsing.spi.indexing.support.IndexingSupport;
import org.openide.filesystems.FileObject;

/**
 *
 * @author marekfukala
 */
public class JsfIndexSupport {

    static final String TLD_LIB_SUFFIX = ".tld"; //NOI18N
    static final String FACELETS_LIB_SUFFIX = ".taglib.xml"; //NOI18N
    static final String TIMESTAMP_KEY = "timestamp"; //NOI18N
    static final String TLD_LIBRARY_MARK_KEY = "tagLibraryDescriptor"; //NOI18N
    static final String FACELETS_LIBRARY_MARK_KEY = "faceletsLibraryDescriptor"; //NOI18N
    static final String LIBRARY_NAMESPACE_KEY = "namespace"; //NOI18N
    static final String FILE_CONTENT_CHECKSUM = "fileContentChecksum"; //NOI18N

    static boolean isFaceletsLibraryDescriptor(FileObject file) {
        return file.getNameExt().endsWith(FACELETS_LIB_SUFFIX);
    }

    static boolean isTagLibraryDescriptor(FileObject file) {
        return file.getNameExt().endsWith(TLD_LIB_SUFFIX);
    }

    static void indexTagLibraryDescriptor(Context context, FileObject file, String namespace) throws IOException {
        IndexingSupport sup = IndexingSupport.getInstance(context);
        IndexDocument doc = sup.createDocument(file);
        doc.addPair(JsfIndexSupport.TIMESTAMP_KEY, Long.toString(System.currentTimeMillis()), false, true);
        doc.addPair(LIBRARY_NAMESPACE_KEY, namespace, true, true);
        doc.addPair(TLD_LIBRARY_MARK_KEY, Boolean.TRUE.toString(), true, true);
        doc.addPair(FILE_CONTENT_CHECKSUM, getMD5Checksum(file.getInputStream()), false, true);
        sup.addDocument(doc);
    }

    static void indexFaceletsLibraryDescriptor(Context context, FileObject file, String namespace) throws IOException {
        IndexingSupport sup = IndexingSupport.getInstance(context);
        IndexDocument doc = sup.createDocument(file);
        doc.addPair(JsfIndexSupport.TIMESTAMP_KEY, Long.toString(System.currentTimeMillis()), false, true);
        doc.addPair(LIBRARY_NAMESPACE_KEY, namespace, true, true);
        doc.addPair(FACELETS_LIBRARY_MARK_KEY, Boolean.TRUE.toString(), true, true);
        doc.addPair(FILE_CONTENT_CHECKSUM, getMD5Checksum(file.getInputStream()), false, true);
        sup.addDocument(doc);
    }

    /*test*/ static String getMD5Checksum(InputStream is) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5"); //NOI18N

            byte[] buffer = new byte[1024];
            int numRead;
            do {
                numRead = is.read(buffer);
                if (numRead > 0) {
                    md.update(buffer, 0, numRead);
                }
            } while (numRead != -1);
            is.close();

            byte[] b = md.digest();
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < b.length; i++) {
                sb.append(Integer.toString((b[i] & 0xff) + 0x100, 16).substring(1));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException | IOException e) {
            //no-op
            return "n/a"; //NOI18N
        }
    }
}
