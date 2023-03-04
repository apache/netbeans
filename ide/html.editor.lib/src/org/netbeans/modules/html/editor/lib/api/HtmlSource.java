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
package org.netbeans.modules.html.editor.lib.api;

import java.io.IOException;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.modules.parsing.api.Snapshot;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.util.Exceptions;

/**
 *
 * @author marekfukala
 */
public final class HtmlSource {

    private CharSequence sourceCode;
    private Snapshot snapshot;
    private FileObject sourceFileObject;

    public HtmlSource(CharSequence sourceCode) {
        this.sourceCode = sourceCode;
    }

    public HtmlSource(FileObject sourceFileObject) {
        this.sourceFileObject = sourceFileObject;
    }

    public HtmlSource(Snapshot snapshot) {
        this.snapshot = snapshot;
    }
    
    public HtmlSource(CharSequence sourceCode, Snapshot snapshot, FileObject sourceFileObject) {
        this.sourceCode = sourceCode;
        this.snapshot = snapshot;
        this.sourceFileObject = sourceFileObject;
    }

    public synchronized CharSequence getSourceCode() {
        if(sourceCode == null) {
            if(snapshot != null) {
                loadContentFromSnapshot();
            } else if(sourceFileObject != null) {
                loadContentFromFileObject();
            }
        }
        return sourceCode;
    }

    public Snapshot getSnapshot() {
        return snapshot;
    }

    public synchronized FileObject getSourceFileObject() {
        if (sourceFileObject == null && snapshot != null) {
            //try to obtain the FileObject from the snapshot if not explicitly provided
            sourceFileObject = snapshot.getSource().getFileObject();
        }
        return sourceFileObject;
    }

    private void loadContentFromFileObject() {
        try {
            DataObject dobj = DataObject.find(sourceFileObject);
            if (dobj != null) {
                EditorCookie cake = dobj.getCookie(EditorCookie.class);
                final Document doc = cake.openDocument();
                doc.render(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            sourceCode = doc.getText(0, doc.getLength());
                        } catch (BadLocationException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                    }
                });
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    private void loadContentFromSnapshot() {
        sourceCode = snapshot.getText(); //immutable CharSequence
    }
}
