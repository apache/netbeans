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
package org.netbeans.modules.html.angular;

import java.awt.Color;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.parsing.api.Snapshot;

/**
 *
 * @author marekfukala
 */
public class Utils {
    
    private static final int URL_CONNECTION_TIMEOUT = 1000; //ms
    private static final int URL_READ_TIMEOUT = URL_CONNECTION_TIMEOUT * 3; //ms
     
    /**
     * Gets document range for the given from and to embedded offsets. 
     * 
     * Returns null if the converted document offsets are invalid.
     */
    public static OffsetRange getValidDocumentOffsetRange(int efrom, int eto, Snapshot snapshot) {
        if(efrom == -1 || eto == -1) {
            throw new IllegalArgumentException(String.format("bad range: %s - %s", efrom, eto));
        }
        int dfrom = snapshot.getOriginalOffset(efrom);
        int dto = snapshot.getOriginalOffset(eto);
        if(dfrom == -1 || dto == -1) {
            return null;
        }
        if(dfrom > dto) {
            return null;
        }
        
        return new OffsetRange(dfrom, dto);
    }
    
    public static String hexColorCode(Color c) {
        return Integer.toHexString(c.getRGB()).substring(2);
    }

    public static String getContentAsString(URL url, Charset charset) throws IOException {
        StringWriter writer = new StringWriter();
        loadURL(url, writer, charset);
        return writer.getBuffer().toString();
       
    }
    
    public static void loadURL(URL url, Writer writer, Charset charset) throws IOException {
        if (charset == null) {
            charset = Charset.defaultCharset();
        }
        URLConnection con = url.openConnection();
        con.setConnectTimeout(URL_CONNECTION_TIMEOUT); 
        con.setReadTimeout(URL_READ_TIMEOUT); 
        con.connect();
        Reader r = new InputStreamReader(new BufferedInputStream(con.getInputStream()), charset);
        char[] buf = new char[2048];
        int read;
        while ((read = r.read(buf)) != -1) {
            writer.write(buf, 0, read);
        }
        r.close();
    }
    
    public static String getFileContent(File file) throws IOException {
        Reader r = new InputStreamReader(new FileInputStream(file), "UTF-8"); // NOI18N
        StringBuilder sb = new StringBuilder();
        try {
            char[] buf = new char[2048];
            int read;
            while ((read = r.read(buf)) != -1) {
                sb.append(buf, 0, read);
            }
        } finally {
            r.close();
        }
        return sb.toString();
    }
 
    public static String cutQueryFromTemplateUrl(String url) {
        int index = url.indexOf('?');
        return index > 0 ? url.substring(0, index) : url;
    }
}
