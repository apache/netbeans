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
package org.netbeans.modules.java.lsp.server.ui;

import com.google.gson.JsonObject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import javax.swing.text.Document;
import org.eclipse.lsp4j.ApplyWorkspaceEditParams;
import org.eclipse.lsp4j.ApplyWorkspaceEditResponse;
import org.eclipse.lsp4j.ClientCapabilities;
import org.eclipse.lsp4j.FailureHandlingKind;
import org.eclipse.lsp4j.InitializeParams;
import org.eclipse.lsp4j.MessageParams;
import org.eclipse.lsp4j.WorkspaceClientCapabilities;
import org.eclipse.lsp4j.WorkspaceEditCapabilities;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.netbeans.api.editor.document.LineDocument;
import org.netbeans.api.editor.document.LineDocumentUtils;
import org.netbeans.api.lsp.ResourceOperation;
import org.netbeans.api.lsp.TextDocumentEdit;
import org.netbeans.api.lsp.TextEdit;
import org.netbeans.api.lsp.WorkspaceEdit;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.lsp.server.TestCodeLanguageClient;
import org.netbeans.modules.java.lsp.server.protocol.NbCodeClientCapabilities;
import org.netbeans.modules.java.lsp.server.protocol.SaveDocumentRequestParams;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Union2;
import org.openide.util.test.MockLookup;

/**
 *
 * @author sdedic
 */
public class AbstractApplyEditsImplementationTest extends NbTestCase {

    public AbstractApplyEditsImplementationTest(String name) {
        super(name);
    }
    
    FileObject workdir;
    LC client = new LC();
    NbCodeClientCapabilities nbCaps;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        MockLookup.setLayersAndInstances(client, new AbstractApplyEditsImplementation());
        workdir = FileUtil.toFileObject(getWorkDir());
        clearWorkDir();
        
        InitializeParams init = new InitializeParams();
        ClientCapabilities ccaps = new ClientCapabilities();
        WorkspaceClientCapabilities wcaps = new WorkspaceClientCapabilities();
        WorkspaceEditCapabilities ecaps = new WorkspaceEditCapabilities();
        ecaps.setDocumentChanges(true);
        ecaps.setFailureHandling(FailureHandlingKind.Abort);
        
        wcaps.setWorkspaceEdit(ecaps);
        ccaps.setWorkspace(wcaps);
        init.setCapabilities(ccaps);
        JsonObject ext = new JsonObject();
        ext.add("nbcodeCapabilities", new JsonObject());
        init.setInitializationOptions(ext);
        
        nbCaps = NbCodeClientCapabilities.get(init);
    }
    
    interface TextEditAcceptor {
        public boolean accept(org.eclipse.lsp4j.WorkspaceEdit wk, org.eclipse.lsp4j.TextDocumentEdit te, org.eclipse.lsp4j.TextEdit edit);
    }
    interface ResourceAcceptor {
        public boolean accept(org.eclipse.lsp4j.WorkspaceEdit wk, org.eclipse.lsp4j.ResourceOperation op);
    }
    
    class LC extends TestCodeLanguageClient {
        List<String> saveErrorDocuments = new ArrayList<>();
        List<String> savedDocuments = new ArrayList<>();
        List<ApplyWorkspaceEditParams> appliedEdits = new ArrayList<>();
        TextEditAcceptor textAcceptor;
        ResourceAcceptor resourceAcceptor;

        @Override
        public NbCodeClientCapabilities getNbCodeCapabilities() {
            return nbCaps;
        }

        @Override
        public void logMessage(MessageParams params) {
            switch (params.getType()) {
                case Error:
                case Warning:
                    fail(params.getMessage());
                    break;
                case Info:
                case Log:
                    System.out.println(params.getMessage());
                default:
                    throw new AssertionError(params.getType().name());
            }
        }

        @Override
        public CompletableFuture<Boolean> requestDocumentSave(SaveDocumentRequestParams documentUris) {
            Set<String> docs = new HashSet<>(documentUris.getDocuments());
            docs.removeAll(saveErrorDocuments);
            savedDocuments.addAll(docs);
            if (docs.size() != documentUris.getDocuments().size()) {
                CompletableFuture cf = new CompletableFuture();
                cf.completeExceptionally(new IOException("I/O error"));
                return cf;
            } else {
                return CompletableFuture.completedFuture(true);
            }
        }

        @Override
        public CompletableFuture<ApplyWorkspaceEditResponse> applyEdit(ApplyWorkspaceEditParams params) {
            int index = 0;
            for (Either<org.eclipse.lsp4j.TextDocumentEdit, org.eclipse.lsp4j.ResourceOperation> op : params.getEdit().getDocumentChanges()) {
                if (op.isLeft()) {
                    org.eclipse.lsp4j.TextDocumentEdit e = op.getLeft();
                    for (org.eclipse.lsp4j.TextEdit te : e.getEdits()) {
                        if (textAcceptor != null && !textAcceptor.accept(params.getEdit(), e, te)) {
                            ApplyWorkspaceEditResponse resp = new ApplyWorkspaceEditResponse(false);
                            resp.setFailedChange(index);
                            resp.setFailureReason("Rejected");
                            return CompletableFuture.completedFuture(resp);
                        }
                    }
                } else {
                    org.eclipse.lsp4j.ResourceOperation rop = op.getRight();
                    if (resourceAcceptor != null && !resourceAcceptor.accept(params.getEdit(), rop)) {
                        ApplyWorkspaceEditResponse resp = new ApplyWorkspaceEditResponse(false);
                        resp.setFailedChange(index);
                        resp.setFailureReason("Rejected");
                        return CompletableFuture.completedFuture(resp);
                    }
                }
                index++;
            }
            appliedEdits.add(params);
            return CompletableFuture.completedFuture(new ApplyWorkspaceEditResponse(true));
        }
        
    }
    
    public void testSimpleEdit() throws Exception {
        FileObject src = FileUtil.toFileObject(getDataDir()).getFileObject("ResourceTestingData.java");
        FileObject dst = src.copy(workdir, "Document", "java");
        String text = dst.asText();
        int index = text.indexOf("*/") + 2;
        TextEdit textEdit = new TextEdit(0, index, "");
        TextDocumentEdit edit = new TextDocumentEdit(dst.toURI().toString(), Collections.singletonList(
            textEdit
        ));
        
        WorkspaceEdit we = new WorkspaceEdit(Collections.singletonList(
            Union2.createFirst(edit)
        ));
        
        Document d = dst.getLookup().lookup(EditorCookie.class).openDocument();
        LineDocument ld = LineDocumentUtils.as(d, LineDocument.class);
        int row = LineDocumentUtils.getLineIndex(ld, index);
        int col = index - LineDocumentUtils.getLineStart(ld, index);
        
        List<String> uris = WorkspaceEdit.applyEdits(Collections.singletonList(we), true).get();
        assertEquals(1, uris.size());
        assertEquals(edit.getDocument(), uris.get(0));
        
        org.eclipse.lsp4j.TextEdit clientEdit;
        
        clientEdit = client.appliedEdits.get(0).getEdit().getDocumentChanges().get(0).getLeft().getEdits().get(0);
        assertEquals(0, clientEdit.getRange().getStart().getLine());
        assertEquals(0, clientEdit.getRange().getStart().getCharacter());

        assertEquals(row, clientEdit.getRange().getEnd().getLine());
        assertEquals(col, clientEdit.getRange().getEnd().getCharacter());
    }
    
    public void testTextChangesAndSave() throws Exception {
        FileObject src = FileUtil.toFileObject(getDataDir()).getFileObject("ResourceTestingData.java");
        FileObject dst = src.copy(workdir, "Document", "java");
        FileObject dst2 = src.copy(workdir, "Document2", "java");

        String text = dst.asText();
        int index = text.indexOf("*/") + 3; // include the newline after end-comment.
        TextEdit textEdit = new TextEdit(0, index, "");
        String newResourceURI = workdir.toURI().resolve("NewDocument.txt").toString();
        
        Document d = dst.getLookup().lookup(EditorCookie.class).openDocument();
        LineDocument ld = LineDocumentUtils.as(d, LineDocument.class);
        int row = LineDocumentUtils.getLineIndex(ld, index);
        int col = index - LineDocumentUtils.getLineStart(ld, index);

        String delText = "@SuppressWarnings(\"unchecked\")";
        String replaceText = "/* Suppression replaced by comment */";
        int deleteIndex = text.indexOf(delText);

        TextDocumentEdit edit = new TextDocumentEdit(dst.toURI().toString(), Arrays.asList(
            textEdit
        ));
        TextDocumentEdit edit2 = new TextDocumentEdit(dst2.toURI().toString(), Arrays.asList(
            new TextEdit(deleteIndex, deleteIndex + delText.length(), replaceText)
        ));

        int row2 = LineDocumentUtils.getLineIndex(ld, deleteIndex);
        int col2 = deleteIndex - LineDocumentUtils.getLineStart(ld, deleteIndex);
        // adjust for the deleted comment lines.
        
        WorkspaceEdit we = new WorkspaceEdit(Arrays.asList(
            Union2.createFirst(edit),
            Union2.createFirst(edit2)
        ));
        
        WorkspaceEdit.applyEdits(Collections.singletonList(we), true);
        List<String> uris = WorkspaceEdit.applyEdits(Collections.singletonList(we), false).get();
        assertEquals(2, uris.size());
        assertEquals(2, client.savedDocuments.size());
    }
    
    public void testTextAndResourceChanges() throws Exception {
        FileObject src = FileUtil.toFileObject(getDataDir()).getFileObject("ResourceTestingData.java");
        FileObject dst = src.copy(workdir, "Document", "java");
        String text = dst.asText();
        int index = text.indexOf("*/") + 3; // include the newline after end-comment.
        TextEdit textEdit = new TextEdit(0, index, "");
        String newResourceURI = workdir.toURI().resolve("NewDocument.txt").toString();
        ResourceOperation.CreateFile cf = new ResourceOperation.CreateFile(newResourceURI);
        TextDocumentEdit edit2 = new TextDocumentEdit(newResourceURI, Collections.singletonList(
            new TextEdit(0, 0, "Hello, world!")
        ));
        
        Document d = dst.getLookup().lookup(EditorCookie.class).openDocument();
        LineDocument ld = LineDocumentUtils.as(d, LineDocument.class);
        int row = LineDocumentUtils.getLineIndex(ld, index);
        int col = index - LineDocumentUtils.getLineStart(ld, index);

        String delText = "@SuppressWarnings(\"unchecked\")";
        String replaceText = "/* Suppression replaced by comment */";
        int deleteIndex = text.indexOf(delText);

        TextDocumentEdit edit = new TextDocumentEdit(dst.toURI().toString(), Arrays.asList(
            textEdit,
            new TextEdit(deleteIndex, deleteIndex + delText.length(), replaceText)
        ));

        int row2 = LineDocumentUtils.getLineIndex(ld, deleteIndex);
        int col2 = deleteIndex - LineDocumentUtils.getLineStart(ld, deleteIndex);
        // adjust for the deleted comment lines.
        
        WorkspaceEdit we = new WorkspaceEdit(Arrays.asList(
            Union2.createFirst(edit),
            Union2.createSecond(cf),
            Union2.createFirst(edit2)
        ));
        
        List<String> uris = WorkspaceEdit.applyEdits(Collections.singletonList(we), false).get();
        assertEquals(2, uris.size());
        assertEquals(0, client.savedDocuments.size());
        assertTrue(uris.contains(edit.getDocument()));
        assertTrue(uris.contains(cf.getNewFile()));

        org.eclipse.lsp4j.TextEdit clientEdit;
        clientEdit = client.appliedEdits.get(0).getEdit().getDocumentChanges().get(0).getLeft().getEdits().get(0);
        assertEquals(0, clientEdit.getRange().getStart().getLine());
        assertEquals(0, clientEdit.getRange().getStart().getCharacter());

        assertEquals(row, clientEdit.getRange().getEnd().getLine());
        assertEquals(col, clientEdit.getRange().getEnd().getCharacter());

        clientEdit = client.appliedEdits.get(0).getEdit().getDocumentChanges().get(0).getLeft().getEdits().get(1);
        assertEquals(row2, clientEdit.getRange().getStart().getLine());
        assertEquals(col2, clientEdit.getRange().getStart().getCharacter());

        assertEquals(row2, clientEdit.getRange().getEnd().getLine());
        assertEquals(col2 + delText.length(), clientEdit.getRange().getEnd().getCharacter());
        assertEquals(replaceText, clientEdit.getNewText());
        
        clientEdit = client.appliedEdits.get(0).getEdit().getDocumentChanges().get(2).getLeft().getEdits().get(0);
        assertEquals(0, clientEdit.getRange().getStart().getLine());
        assertEquals(0, clientEdit.getRange().getStart().getCharacter());
        assertEquals(0, clientEdit.getRange().getEnd().getLine());
        assertEquals(0, clientEdit.getRange().getEnd().getCharacter());
        assertEquals(0, clientEdit.getRange().getEnd().getLine());
        assertEquals("Hello, world!", clientEdit.getNewText());
    }
}
