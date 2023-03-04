/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
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
