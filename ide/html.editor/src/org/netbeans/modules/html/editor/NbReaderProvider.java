/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.html.editor;

import org.netbeans.modules.html.editor.lib.api.HtmlVersion;
import java.io.*;
import java.net.URL;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.html.editor.lib.api.dtd.ReaderProvider;
import org.openide.filesystems.*;
import org.openide.util.Exceptions;

public class NbReaderProvider implements ReaderProvider {

    private static final String CATALOG_FILE_NAME = "catalog"; // NOI18N

    private Map<String, String> mapping = new HashMap<>();
    private FileObject dtdSetFolder;

    @Deprecated
    public static synchronized void setupReaders() {
        Logger.global.log(Level.INFO, "Please remove the call to NbReaderProvider.setupReaders(), it is not necessary anymore.", new Exception()); //NOI18N
    }

    public NbReaderProvider(FileObject folder) {
        dtdSetFolder = folder;
        initialize();
    }

    @Override
    public Collection<String> getIdentifiers() {
        return mapping.keySet();
    }

    @Override
    public Reader getReaderForIdentifier(String identifier, String filename) {
        FileObject file = getSystemId(identifier);
        if(file == null) {
            return null;
        }
        
        try {
            return new InputStreamReader(file.getInputStream());
        } catch (FileNotFoundException exc) {
            return null;
        }
        
    }

    @Override
    public boolean isXMLContent(String identifier) {
        return HtmlVersion.find(identifier, null).isXhtml();
    }

    @Override
    public FileObject getSystemId(String publicId) {
         String fileName = (String) mapping.get(publicId);
        if (fileName == null) {
            return null;
        }
        if (dtdSetFolder == null) {
            return null;
        }

        FileObject file = dtdSetFolder.getFileObject(fileName);
        return file;
    }

    private void initialize() {
        FileObject catalog = dtdSetFolder.getFileObject(CATALOG_FILE_NAME);
        if (catalog != null) {
            try {
                mapping.putAll(parseCatalog(new InputStreamReader(catalog.getInputStream())));
            } catch (FileNotFoundException exc) {
                Exceptions.printStackTrace(exc);
            }
        }
    }

    private Map<String, String> parseCatalog(Reader catalogReader) {
        HashMap<String, String> hashmap = new HashMap<>();
        LineNumberReader reader = new LineNumberReader(catalogReader);

        for (;;) {
            String line;

            try {
                line = reader.readLine();
            } catch (IOException exc) {
                return null;
            }

            if (line == null) {
                break;
            }

            StringTokenizer st = new StringTokenizer(line);
            if (st.hasMoreTokens() && "PUBLIC".equals(st.nextToken()) && st.hasMoreTokens()) { // NOI18N
                st.nextToken("\""); // NOI18N
                if (!st.hasMoreTokens()) {
                    continue;
                }
                String id = st.nextToken("\""); // NOI18N

                if (!st.hasMoreTokens()) {
                    continue;
                }
                st.nextToken(" \t\n\r\f"); // NOI18N

                if (!st.hasMoreTokens()) {
                    continue;
                }
                String file = st.nextToken();
                hashmap.put(id, file);
            }
        }
        return hashmap;
    }

}
