/**
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
package org.netbeans.modules.lsp.client;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.StyledDocument;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.Command;
import org.eclipse.lsp4j.ExecuteCommandParams;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4j.WorkspaceEdit;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.netbeans.api.editor.document.LineDocument;
import org.netbeans.api.editor.document.LineDocumentUtils;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;
import org.openide.text.NbDocument;
import org.openide.util.Exceptions;

/**
 *
 * @author lahvac
 */
public class Utils {

    public static String toURI(FileObject file) {
        return file.toURI().toString().replace("file:/", "file:///");
    }

    public static Position createPosition(Document doc, int offset) throws BadLocationException {
         return new Position(LineDocumentUtils.getLineIndex((LineDocument) doc, offset),
                             offset - LineDocumentUtils.getLineStart((LineDocument) doc, offset));
    }

    public static int getOffset(Document doc, Position pos) {
        return LineDocumentUtils.getLineStartFromIndex((LineDocument) doc, pos.getLine()) + pos.getCharacter();
    }

    public static void applyWorkspaceEditor(WorkspaceEdit edit) {
        for (Map.Entry<String, List<TextEdit>> e : edit.getChanges().entrySet()) {
            try {
                FileObject file = URLMapper.findFileObject(new URI(e.getKey()).toURL());
                EditorCookie ec = file.getLookup().lookup(EditorCookie.class);
                Document doc = ec != null ? ec.openDocument() : null;
                if (doc == null) {
                    continue;
                }
                NbDocument.runAtomic((StyledDocument) doc, () -> {
                    e.getValue()
                     .stream()
                     .sorted((te1, te2) -> te1.getRange().getEnd().getLine() == te2.getRange().getEnd().getLine() ? te1.getRange().getEnd().getCharacter() - te2.getRange().getEnd().getCharacter() : te1.getRange().getEnd().getLine() - te2.getRange().getEnd().getLine())
                     .forEach(te -> {
                        try {
                            int start = Utils.getOffset(doc, te.getRange().getStart());
                            int end = Utils.getOffset(doc, te.getRange().getEnd());
                            doc.remove(start, end - start);
                            doc.insertString(start, te.getNewText(), null);
                        } catch (BadLocationException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                     });
                });
            } catch (URISyntaxException | IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }

    public static void applyCodeAction(LSPBindings server, Either<Command, CodeAction> cmd) {
        try {
            Command command;

            if (cmd.isLeft()) {
                command = cmd.getLeft();
            } else {
                Utils.applyWorkspaceEditor(cmd.getRight().getEdit());
                command = cmd.getRight().getCommand();
            }
            if (command != null) {
                server.getWorkspaceService().executeCommand(new ExecuteCommandParams(command.getCommand(), command.getArguments())).get();
            }
        } catch (InterruptedException | ExecutionException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
}
