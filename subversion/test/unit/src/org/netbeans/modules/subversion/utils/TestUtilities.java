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

package org.netbeans.modules.subversion.utils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.tigris.subversion.svnclientadapter.SVNUrl;

/**
 *
 * @author ondra.vrabec
 * @author Marian Petras
 */
public final class TestUtilities {

    private static final String hexadecimalChars = "0123456789abcdef";  //NOI18N

    private TestUtilities () {}

    /**
     * Formats file's location into SVNUrl format
     * @param file
     * @return file's location in a SVNUrl format
     */
    public static String formatFileURL (File file) {
        String path = file.getAbsolutePath();
        String url;
        url = ("file:///" + encodeToUrlFormat(path.replace('\\', '/')))
              .replace("file:////", "file:///");
        return url;
    }

    public static String formatPathURL(String path) {
        return encodeToUrlFormat(path);
    }

    public static String encodeToUrlFormat(String path) {
        StringBuilder buf = new StringBuilder(path.length() + 4);
        encodeToUrlFormat(path, buf);
        return buf.toString();
    }

    public static void encodeToUrlFormat(String path, StringBuilder buf) {
        final int length = path.length();

        for (int i = 0; i < length; i++) {
            char c = path.charAt(i);
            if (isLegalPathChar(c)) {
                buf.append(c);
            } else {
                appendEncoded(c, buf);
            }
        }
    }

    private static void appendEncoded(int c, StringBuilder buf) {

        /*
         * Encode by UTF-8 encoding to one, two, three or four bytes, then
         * encode each byte using the common URI syntax (e.g. "%20"):
         */

        if (c < 0x0080) {                                      // 1 byte (ASCII)
            appendEncodedByte(         c & 0x00007f,          buf);

        } else if (c < 0x0800) {                               // 2 bytes
            appendEncodedByte(0xc0 | ((c & 0x0007c0) >>>  6), buf);
            appendEncodedByte(0x80 |  (c & 0x00003f)        , buf);

        } else if (c < 0x10000) {                              // 3 bytes
            appendEncodedByte(0xe0 | ((c & 0x00f000) >>> 12), buf);
            appendEncodedByte(0x80 | ((c & 0x000fc0) >>>  6), buf);
            appendEncodedByte(0x80 |  (c & 0x00003f)        , buf);

        } else {                                               // 4 bytes
            appendEncodedByte(0xf0 | ((c & 0x1c0000) >>> 18), buf);
            appendEncodedByte(0x80 | ((c & 0x03f000) >>> 12), buf);
            appendEncodedByte(0x80 | ((c & 0x000fc0) >>>  6), buf);
            appendEncodedByte(0x80 |  (c & 0x00003f)        , buf);

        }
    }

    private static void appendEncodedByte(int c, StringBuilder buf) {
        assert c < 0x100;

        buf.append('%');
        buf.append(hexadecimalChars.charAt((c & 0xf0) >>> 4));
        buf.append(hexadecimalChars.charAt( c & 0x0f)       );
    }

    private static boolean isEncodedByte(char c, String s, int i) {
        return c == '%' && i + 2 < s.length() && isHexDigit(s.charAt(i + 1)) && isHexDigit(s.charAt(i + 2));
    }

    private static boolean isHexDigit(char c) {
        return c >= '0' && c <= '9' || c >= 'A' && c <= 'F' || c >= 'a' && c <= 'f';
    }

    /**
     * Decodes svn URI by decoding %XX escape sequences.
     *
     * @param url url to decode
     * @return decoded url
     */
    public static SVNUrl decode(SVNUrl url) {
        if (url == null) return null;
        String s = url.toString();
        StringBuffer sb = new StringBuffer(s.length());

        boolean inQuery = false;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '?') {
                inQuery = true;
            } else if (c == '+' && inQuery) {
                sb.append(' ');
            } else if (isEncodedByte(c, s, i)) {
                List<Byte> byteList = new ArrayList<Byte>();
                do  {
                    byteList.add((byte) Integer.parseInt(s.substring(i + 1, i + 3), 16));
                    i += 3;
                    if (i >= s.length()) break;
                    c = s.charAt(i);
                } while(isEncodedByte(c, s, i));

                if(byteList.size() > 0) {
                    byte[] bytes = new byte[byteList.size()];
                    for(int ib = 0; ib < byteList.size(); ib++) {
                        bytes[ib] = byteList.get(ib);
                    }
                    try {
                        sb.append(new String(bytes, "UTF8"));
                    } catch (Exception e) {
                        
                    }
                    i--;
                }
            } else {
                sb.append(c);
            }
        }
        try {
            return new SVNUrl(sb.toString());
        } catch (java.net.MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    private static boolean isLegalPathChar(char c) {
        return isAlnumChar(c) || ("/-_.!~*'():@&=+$,".indexOf(c) != -1);//NOI18N
    }

    private static boolean isAlnumChar(char c) {
        return ((c >= 'a') && (c <= 'z'))
               || ((c >= 'A') && (c <= 'Z'))
               || ((c >= '0') && (c <= '9'));
    }

    public static void deleteRecursively(File file) throws IOException {
        FileObject fo = FileUtil.toFileObject(file);
        if (fo == null) return;
        fo.delete();
    }    
}
