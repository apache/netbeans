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

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.StyledDocument;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.Command;
import org.eclipse.lsp4j.CreateFile;
import org.eclipse.lsp4j.DeleteFile;
import org.eclipse.lsp4j.ExecuteCommandParams;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.RenameFile;
import org.eclipse.lsp4j.ResourceOperation;
import org.eclipse.lsp4j.ResourceOperationKind;
import org.eclipse.lsp4j.TextDocumentEdit;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4j.WorkspaceEdit;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.netbeans.api.editor.document.LineDocument;
import org.netbeans.api.editor.document.LineDocumentUtils;
import org.netbeans.lib.editor.util.swing.DocumentUtilities;
import org.netbeans.modules.editor.indent.api.IndentUtils;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
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

    public static void applyWorkspaceEdit(WorkspaceEdit edit) {
        if (edit.getDocumentChanges() != null) {
            for (Either<TextDocumentEdit, ResourceOperation> change : edit.getDocumentChanges()) {
                if (change.isLeft()) {
                    applyEdits(change.getLeft().getTextDocument().getUri(), change.getLeft().getEdits());
                } else {
                    switch (change.getRight().getKind()) {
                        case ResourceOperationKind.Create:
                            try {
                                FileUtil.createData(new File(new URI(((CreateFile) change.getRight()).getUri())));
                            } catch (IOException | URISyntaxException ex) {
                                Exceptions.printStackTrace(ex);
                            }
                            break;
                        case ResourceOperationKind.Delete:
                            try {
                                URLMapper.findFileObject(new URI(((DeleteFile) change.getRight()).getUri()).toURL()).delete();
                            } catch (IOException | URISyntaxException ex) {
                                Exceptions.printStackTrace(ex);
                            }
                            break;
                        case ResourceOperationKind.Rename:
                            try {
                                File target = new File(new URI(((RenameFile) change.getRight()).getNewUri()));
                                FileObject targetFolder = FileUtil.createFolder(target.getParentFile());
                                FileObject source = URLMapper.findFileObject(new URI(((RenameFile) change.getRight()).getOldUri()).toURL());
                                DataObject od = DataObject.find(source);
                                //XXX: should move and rename in one go!
                                od.move(DataFolder.findFolder(targetFolder));
                                od.rename(target.getName());
                            } catch (IOException | URISyntaxException ex) {
                                Exceptions.printStackTrace(ex);
                            }
                            break;
                    }
                }
            }
        } else {
            for (Map.Entry<String, List<TextEdit>> e : edit.getChanges().entrySet()) {
                applyEdits(e.getKey(), e.getValue());
            }
        }
    }

    private static void applyEdits(String uri, List<TextEdit> edits) {
        try {
            FileObject file = URLMapper.findFileObject(new URI(uri).toURL());
            EditorCookie ec = file.getLookup().lookup(EditorCookie.class);
            Document doc = ec != null ? ec.openDocument() : null;
            if (doc == null) {
                return ;
            }
            NbDocument.runAtomic((StyledDocument) doc, () -> {
                applyEditsNoLock(doc, edits);
            });
        } catch (URISyntaxException | IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    public static void applyEditsNoLock(Document doc, List<? extends TextEdit> edits) {
        edits
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
    }

    public static void applyCodeAction(LSPBindings server, Either<Command, CodeAction> cmd) {
        try {
            Command command;

            if (cmd.isLeft()) {
                command = cmd.getLeft();
            } else {
                Utils.applyWorkspaceEdit(cmd.getRight().getEdit());
                command = cmd.getRight().getCommand();
            }
            if (command != null) {
                server.getWorkspaceService().executeCommand(new ExecuteCommandParams(command.getCommand(), command.getArguments())).get();
            }
        } catch (InterruptedException | ExecutionException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    public static Position computeEndPositionForRemovedText(Position startPos, String removedText) {
        int endLine = startPos.getLine();
        int endChar = startPos.getCharacter();
        for (char c : removedText.toCharArray()) {
            if (c == '\n') {
                endLine++;
                endChar = 0;
            } else {
                endChar++;
            }
        }
        return new Position(endLine, endChar);
    }

    public static List<TextEdit> computeDefaultOnTypeIndent(Document doc, int changeStart, Position startPos, String newText) {
        List<TextEdit> edits = new ArrayList<>();
        try {
            int indentLevel = IndentUtils.indentLevelSize(doc);
            int lineStart = IndentUtils.lineStartOffset(doc, changeStart);
            int indent = IndentUtils.lineIndent(doc, lineStart);
            if (newText.equals("}") && indent == changeStart - lineStart) {
                CharSequence cs = DocumentUtilities.getText(doc);
                int balance = 1;
                int idx = changeStart - 1;
                while (idx >= 0 && balance > 0) {
                    switch (cs.charAt(idx)) {
                        case '{': balance--; break;
                        case '}': balance++; break;
                    }
                    idx--;
                }
                int newIndent;
                if (balance == 0) {
                    newIndent = IndentUtils.lineIndent(doc, IndentUtils.lineStartOffset(doc, idx));
                } else {
                    newIndent = 0;
                }
                edits.add(new TextEdit(new Range(new Position(startPos.getLine(), 0), new Position(startPos.getLine(), indent)), IndentUtils.createIndentString(doc, newIndent)));
            } else if (newText.equals("\n")) {
                Position insertPos = new Position(startPos.getLine() + 1, 0);
                int newIndent = indent;
                if (changeStart > 0 && DocumentUtilities.getText(doc, changeStart - 1, 1).charAt(0) == '{') {
                    newIndent += indentLevel;
                }
                edits.add(new TextEdit(new Range(insertPos, insertPos), IndentUtils.createIndentString(doc, newIndent)));
            }
        } catch (BadLocationException ex) {
            Exceptions.printStackTrace(ex);
        }
        return edits;
    }
}
