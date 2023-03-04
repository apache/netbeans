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
