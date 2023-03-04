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
package org.netbeans.modules.languages.dataobject;

import java.io.BufferedReader;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.MultiDataObject;
import org.openide.nodes.CookieSet;
import org.openide.nodes.Node;
import org.openide.text.DataEditorSupport;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.MIMEResolver;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.util.Lookup;

@MIMEResolver.ExtensionRegistration(
    mimeType="text/x-nbs",
    position=210,
    displayName="#NBSResolver",
    extension={ "nbs" }
)
public class LanguagesDataObject extends MultiDataObject {

    public LanguagesDataObject(FileObject pf, LanguagesDataLoader loader) throws DataObjectExistsException, IOException {
        super(pf, loader);
        CookieSet cookies = getCookieSet();
        cookies.add((Node.Cookie) DataEditorSupport.create(this, getPrimaryEntry(), cookies));
    }

    @Override
    protected Node createNodeDelegate() {
        return new LanguagesDataNode(this);
    }

    @Override
    public Lookup getLookup() {
        return getCookieSet().getLookup();
    }
    
    @Override
    protected DataObject handleCreateFromTemplate(final DataFolder df, final String name) throws IOException {
        DataObject createdClass = super.handleCreateFromTemplate(df, name);
        FileObject createdClassFO = createdClass.getPrimaryFile();
        Map<String, String> tokens = new HashMap<String, String>();
        tokens.put("__CLASS_NAME__", name);
        tokens.put("__PACKAGE_NAME__", getPackageName(createdClassFO));
        createFileWithSubstitutions(this.getPrimaryFile(), createdClassFO, tokens);
        return createdClass;
    }

    static String getPackageName(final FileObject createdClass) {
        FileObject parent = createdClass.getParent();
        // XXX bad. Acquire source directory though 'src.dir' property or some Scala's ClassPath
        while (parent != null && !"src".equals(parent.getNameExt())) {
            parent = parent.getParent();
        }
        return (parent == null) ? "test" // fallback
                : createdClass.getParent().getPath().substring(parent.getPath().length() + 1).replace('/', '.');
    }

    static void createFileWithSubstitutions(
            final FileObject sourceFO,
            final FileObject targetFO,
            final Map<String,String> tokens) throws IOException {
        FileLock lock = targetFO.lock();
        try {
            copyAndSubstituteTokens(sourceFO.getURL(), lock, targetFO, tokens);
        } finally {
            lock.releaseLock();
        }
    }
     
    private static void copyAndSubstituteTokens(final URL content,
            final FileLock lock, final FileObject targetFO, final Map<String,String> tokens) throws IOException {
        OutputStream os = targetFO.getOutputStream(lock);
        try {
            PrintWriter pw = new PrintWriter(os);
            try {
                InputStream is = content.openStream();
                try {
                    Reader r = new InputStreamReader(is);
                    BufferedReader br = new BufferedReader(r);
                    String line;
                    while ((line = br.readLine()) != null) {
                        pw.println(tokens == null ? line : replaceTokens(tokens, line));
                    }
                } finally {
                    is.close();
                }
            } finally {
                pw.close();
            }
        } finally {
            os.close();
        }
    }
    
    private static String replaceTokens(final Map<String,String> tokens, String line) {
        for (Map.Entry<String, String> entry : tokens.entrySet()) {
            line = line.replaceAll(entry.getKey(), entry.getValue());
        }
        return line;
    }
  
}

