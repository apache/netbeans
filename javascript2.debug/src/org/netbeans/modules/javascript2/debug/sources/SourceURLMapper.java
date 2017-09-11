/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */

package org.netbeans.modules.javascript2.debug.sources;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.URLMapper;
import org.openide.util.URLStreamHandlerRegistration;
import org.openide.util.lookup.ServiceProvider;

/**
 * Support for URLs of the form js_sources://fs&lt;ID&gt;/folder/file
 * 
 * @author Martin
 */
@ServiceProvider(service=URLMapper.class)
public final class SourceURLMapper extends URLMapper {
    
    private static final Map<Long,Reference<FileSystem>> filesystems = new HashMap<>(); // i.e. a sparse array by id
    
    public @Override URL getURL(FileObject fo, int type) {
        if (type != URLMapper.INTERNAL) {
            return null;
        }
        try {
            FileSystem fs = fo.getFileSystem();
            if (fs instanceof SourceFS) {
                String path = fo.getPath();
                if (fo.isFolder() && !fo.isRoot()) {
                    path += '/';
                }
                return url((SourceFS) fs, path);
            }
        } catch (FileStateInvalidException x) {
            // ignore
        }
        return null;
    }
    
    // keep as separate method to avoid linking Handler until needed
    private static synchronized URL url(SourceFS fs, String path) {
        synchronized (filesystems) {
            Reference<FileSystem> r = filesystems.get(fs.getID());
            if (r == null || r.get() == null) {
                r = new WeakReference<FileSystem>(fs);
                filesystems.put(fs.getID(), r);
            }
        }
        try {
            //return new URL(null, PROTOCOL + "://fs" + fs.getID() + "/" + path, new SourceURLHandler());
            return new URL(SourceFilesCache.URL_PROTOCOL, "fs" + fs.getID(), -1, "/" + percentEncode(path), new SourceURLHandler());
        } catch (MalformedURLException x) {
            throw new AssertionError(x);
        }
    }
    
    private static final Pattern HOST = Pattern.compile("fs(\\d+)"); // NOI18N
    static FileObject find(URL url) {
        if (!SourceFilesCache.URL_PROTOCOL.equals(url.getProtocol())) {
            return null;
        }
        String host = url.getHost();
        if (host == null) {
            return null;
        }
        Matcher m = HOST.matcher(host);
        if (!m.matches()) {
            return null;
        }
        Reference<FileSystem> r;
        synchronized (filesystems) {
            r = filesystems.get(Long.parseLong(m.group(1)));
        }
        if (r == null) {
            return null;
        }
        FileSystem fs = r.get();
        if (fs == null) {
            return null;
        }
        String path = url.getPath().substring(1);
        path = percentDecode(path);
        return fs.findResource(path);
    }
    
    public @Override FileObject[] getFileObjects(URL url) {
        FileObject f = find(url);
        return f != null ? new FileObject[] {f} : null;
    }
    
    public static String percentEncode(String text) {
        StringBuilder encoded = null;
        int li = 0;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            int v = c;
            if (47 <= v && v <= 57 || // slash and 0-9
                'A' <= v && v <= 'Z' ||
                'a' <= v && v <= 'z' ||
                v == '.') {
                
                continue;
            }
            String e = encode(c);
            if (encoded == null) {
                encoded = new StringBuilder();
            }
            encoded.append(text.substring(li, i));
            encoded.append(e);
            li = i + 1;
        }
        if (encoded != null) {
            if (li < text.length()) {
                encoded.append(text.substring(li));
            }
            return encoded.toString();
        } else {
            return text;
        }
    }
    
    private static String encode(char c) {
        String s = new String(new char[] { c });
        byte[] bytes;
        try {
            bytes = s.getBytes("utf-8");
        } catch (UnsupportedEncodingException ex) {
            bytes = s.getBytes();
        }
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append('%');
            String hs = Integer.toHexString(b & 0xFF);
            if (hs.length() == 1) {
                sb.append('0');
            }
            sb.append(hs);
        }
        return sb.toString();
    }
    
    public static String percentDecode(String text) {
        int i = text.indexOf('%');
        if (i < 0) {
            return text;
        }
        StringBuilder decoded = new StringBuilder();
        int li = 0;
        while (i >= 0) {
            decoded.append(text.substring(li, i));
            List<Byte> bytes = new ArrayList<>();
            while (text.length() > (i + 2) && text.charAt(i) == '%') {
                int v = Integer.parseInt(text.substring(i+1, i+3), 16);
                bytes.add(new Byte((byte) (v & 0xFF)));
                i += 3;
            }
            byte[] byteArray = new byte[bytes.size()];
            for (int bi = 0; bi < byteArray.length; bi++) {
                byteArray[bi] = bytes.get(bi);
            }
            String s;
            try {
                s = new String(byteArray, "utf-8");
            } catch (UnsupportedEncodingException ex) {
                s = new String(byteArray);
            }
            decoded.append(s);
            if (i < text.length() && text.charAt(i) == '%') {
                // an extra %
                decoded.append('%');
                i++;
            }
            li = i;
            i = text.indexOf('%', li);
        }
        if (li < text.length()) {
            decoded.append(text.substring(li));
        }
        return decoded.toString();
    }
    

    @URLStreamHandlerRegistration(protocol=SourceFilesCache.URL_PROTOCOL)
    public static final class SourceURLHandler extends URLStreamHandler {
        
        protected @Override URLConnection openConnection(URL u) throws IOException {
            
            return new SourceConnection(u);
        }
    }
}
