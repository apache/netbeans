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
package org.netbeans.modules.project.dependency.impl;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.api.actions.Savable;
import org.netbeans.api.editor.document.AtomicLockDocument;
import org.netbeans.api.editor.document.LineDocument;
import org.netbeans.api.editor.document.LineDocumentUtils;
import org.netbeans.api.lsp.TextDocumentEdit;
import org.netbeans.api.lsp.TextEdit;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

/**
 * Class that processes TextDocumentEdit for a document
 * @author sdedic
 */
public class TextDocumentEditProcessor {
    private final TextDocumentEdit  edits;
    private boolean saveAfterEdit;
    private boolean forkDocument;
    
    private FileObject targetFile;
    private EditorCookie editor;
    private LineDocument document;
    
    public TextDocumentEditProcessor(TextDocumentEdit edits) {
        this.edits = edits;
    }

    public boolean isForkDocument() {
        return forkDocument;
    }

    TextDocumentEditProcessor setForkDocument(boolean forkDocument) {
        this.forkDocument = forkDocument;
        return this;
    }

    public boolean isSaveAfterEdit() {
        return saveAfterEdit;
    }

    public TextDocumentEditProcessor setSaveAfterEdit(boolean saveAfterEdit) {
        this.saveAfterEdit = saveAfterEdit;
        return this;
    }

    public FileObject getTargetFile() {
        return targetFile;
    }
    
    @NbBundle.Messages({
        "# {0} - filename",
        "ERR_FileNotEditable=File {0} is not editable",
        "# {0} - filename",
        "ERR_FailedToEditDocument=Failed to edit document {0}",
    })
    private void open() throws IOException {
        FileObject fo = ProjectModificationResultImpl.fromString(edits.getDocument());
        if (fo == null || !fo.isValid()) {
            throw new FileNotFoundException(edits.getDocument());
        }
        
        editor = fo.getLookup().lookup(EditorCookie.class);
        if (editor == null  || !fo.canWrite()) {
            throw new IOException(Bundle.ERR_FileNotEditable(edits.getDocument()));
        }
        targetFile = fo;
        Document doc = editor.openDocument();
        if (isForkDocument()) {
            String mime = (String)doc.getProperty("mimeType"); //NOI18N
            Document forked = LineDocumentUtils.createDocument(mime != null ? mime : "text/plain");
            BadLocationException[] err = new BadLocationException[1];
            
            Document fDoc = doc;
            fDoc.render(() -> {
                try {
                    forked.insertString(0, fDoc.getText(0, fDoc.getLength()), null);
                } catch (BadLocationException ex) {
                    err[0] = ex;
                }
            });
            if (err[0] != null) {
                throw new IOException(Bundle.ERR_FailedToEditDocument(edits.getDocument()), err[0]);
            }
            doc = forked;
        }
        document = LineDocumentUtils.asRequired(doc, LineDocument.class);
    }
    
    public String getText() throws IOException {
        String[] text = new String[1];
        BadLocationException[] err  = new BadLocationException[1];
        
        document.render(() -> {
            try {
                text[0] = document.getText(0, document.getLength());
            } catch (BadLocationException ex) {
                err[0] = ex;
            }
        });
        if (err[0] != null) {
            throw new IOException(err[0]);
        } else {
            return text[0];
        }
    }

    public TextDocumentEditProcessor execute() throws IOException {
        open();
        
        BadLocationException err[] = new BadLocationException[1];
        LineDocumentUtils.asRequired(document, AtomicLockDocument.class).runAtomicAsUser(
                () -> {
                    try {
                        performEdits();
                    } catch (BadLocationException ex) {
                        err[0] = ex;
                    }
                }
        );
        if (err[0] != null) {
            throw new IOException(Bundle.ERR_FailedToEditDocument(edits.getDocument()));
        }
        
        if (isSaveAfterEdit() && !isForkDocument()) {
            Savable ss = targetFile.getLookup().lookup(Savable.class);
            ss.save();
        }
        return this;
    }
    
    public Document getDocument() {
        return document;
    }
    
    public void performEdits() throws BadLocationException {
        List<TextEdit> newEdits = new ArrayList<>(edits.getEdits());
        newEdits.sort(ProjectModificationResultImpl.textEditComparator(edits.getEdits()).reversed());
        
        for (TextEdit te : edits.getEdits()) {
            int s = te.getStartOffset();
            int e = te.getEndOffset();
            // let positions that point at the start of the buffer remain in its place: first 
            // insert the text AFTER the deleted part, then delete.
            if (te.getNewText() != null && !te.getNewText().isEmpty()) {
                document.insertString(e, te.getNewText(), null);
            }
            if (e > s) {
                document.remove(s, e - s);
            }
        }
    }
}
