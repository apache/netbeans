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
package org.netbeans.modules.db.sql.editor;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import org.netbeans.spi.queries.FileEncodingQueryImplementation;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/** 
 * Detects UTF-16 encoding for SQL files (see #156585).
 *
 * @author Jiri Skrivanek
 */
@org.openide.util.lookup.ServiceProvider(service = org.netbeans.spi.queries.FileEncodingQueryImplementation.class, position = 110)
public class SQLFileEncodingQueryImpl extends FileEncodingQueryImplementation {

    private static final String SQL_MIME_TYPE = "text/x-sql";  //NOI18N

    @Override
    public Charset getEncoding(FileObject file) {
        String mimeType = FileUtil.getMIMEType(file, SQL_MIME_TYPE);
        if (mimeType != null && mimeType.equals(SQL_MIME_TYPE)) {
            byte[] buff = new byte[4];
            InputStream is = null;
            int bytesRead = 0;
            try {
                is = file.getInputStream();
                bytesRead = is.read(buff);
            } catch (Exception ex) {
                // ignore
            } finally {
                if (is != null) {
                    try {
                        is.close();
                    } catch (IOException ex) {
                        // ignore
                    }
                }
            }
            if (bytesRead == 4 && isUTF16(buff)) {
                return StandardCharsets.UTF_16;
            }
        }
        return null;
    }

    /** Returns true if given byte buffer contains UTF-16 encoding signs.
     * @return true for UTF-16 encoding, false otherwise
     */
    private static boolean isUTF16(byte[] buff) {
        switch (buff[0]) {
            // UTF-16 big-endian marked
            case (byte) 0xfe:
                return buff[1] == (byte) 0xff && (buff[2] != 0 || buff[3] != 0);
            // UTF-16 little-endian marked
            case (byte) 0xff:
                return buff[1] == (byte) 0xfe && (buff[2] != 0 || buff[3] != 0);
        }
        return false;
    }
}
