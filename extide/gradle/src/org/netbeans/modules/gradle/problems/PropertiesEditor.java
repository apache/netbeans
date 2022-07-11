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
package org.netbeans.modules.gradle.problems;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.api.actions.Savable;
import org.netbeans.api.editor.document.AtomicLockDocument;
import org.netbeans.api.editor.document.LineDocumentUtils;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.EditableProperties;

/**
 *
 * @author sdedic
 */
public class PropertiesEditor {
    private static final Logger LOG = Logger.getLogger(PropertiesEditor.class.getName());
    
    private FileObject file;
    private final Path filePath;
    private Document openedDocument;
    private EditableProperties properties;

    public PropertiesEditor(FileObject file) {
        this.file = file;
        File f = FileUtil.toFile(file);
        this.filePath = f != null ? f.toPath() : null;
    }
    
    public PropertiesEditor(Path path) {
        this.filePath = path;
    }

    public Path getFilePath() {
        return filePath;
    }
    
    public EditableProperties open() throws IOException {
        if (properties != null) {
            return properties;
        }
        if (file == null) {
            return properties = new EditableProperties(false);
        }
        EditorCookie cake = file.getLookup().lookup(EditorCookie.class);
        if (cake != null) {
            openedDocument = cake.getDocument();
        }
        
        EditableProperties p;
        if (openedDocument == null) {
            try (InputStream istm = file.getInputStream()) {
                p = new EditableProperties(false);
                p.load(istm);
            }
            this.properties = p;
            return p;
        }
        IOException[] err = new IOException[1];
        AtomicReference<ByteArrayInputStream> ref = new AtomicReference<>();
        openedDocument.render(() -> {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try (PrintWriter pw = new PrintWriter(baos)) {
                pw.print(
                        openedDocument.getText(0, openedDocument.getLength())
                );
                ref.set(new ByteArrayInputStream(baos.toByteArray()));
            } catch (BadLocationException ex) {
                err[0] = new IOException(ex);
            }
        });
        p = new EditableProperties(false);
        p.load(ref.get());
        this.properties = p;
        return p;
    }
    
    public void save() throws IOException {
        if (file != null) {
            EditorCookie cake = file.getLookup().lookup(EditorCookie.class);
            if (cake != null) {
                openedDocument = cake.getDocument();
            }

            if (openedDocument != null) {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                properties.store(baos);
                String str = new String(baos.toByteArray(), StandardCharsets.ISO_8859_1);
                AtomicLockDocument ald = LineDocumentUtils.asRequired(openedDocument, AtomicLockDocument.class);
                BadLocationException[] err = new BadLocationException[1];
                Runnable edit = () -> {
                    int curLen = openedDocument.getLength();
                    try {
                        openedDocument.insertString(0, str, null);
                        openedDocument.remove(str.length(), curLen);
                    } catch (BadLocationException ex) {
                        err[0] = ex;
                    }
                };
                ald.runAtomicAsUser(edit);
                file.getLookup().lookup(Savable.class).save();
                return;
            }
        }
        try (OutputStream fos = Files.newOutputStream(filePath, StandardOpenOption.CREATE_NEW, StandardOpenOption.TRUNCATE_EXISTING)) {
            properties.store(fos);
        }
    }
}
