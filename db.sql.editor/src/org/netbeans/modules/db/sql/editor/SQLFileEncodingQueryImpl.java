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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */
package org.netbeans.modules.db.sql.editor;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
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
                return Charset.forName("UTF-16");  //NOI18N
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
