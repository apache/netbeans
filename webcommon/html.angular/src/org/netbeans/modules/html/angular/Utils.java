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
import java.nio.charset.StandardCharsets;
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
        Reader r = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8);
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
