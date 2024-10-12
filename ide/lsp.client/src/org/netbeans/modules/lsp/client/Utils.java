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
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import javax.swing.SwingUtilities;
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
import org.openide.cookies.LineCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.text.Line;
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

    public static String uriReplaceFilename(String inputUriString, String filename) {
        try {
            URI inputUri = URI.create(inputUriString);
            URI newUri = inputUri.resolve(new URI(null, null, filename, null));
            if ("file".equals(newUri.getScheme())) {
                // By default the java URI routines drop empty hostnames from
                // file URIs. Normalize this to the empty hostname. See also
                // #toURI
                return new URI("file", "", newUri.getPath(), null).toString();
            } else {
                return newUri.toString();
            }
        } catch (URISyntaxException ex) {
            throw new IllegalArgumentException(ex);
        }
    }

    public static Position createPosition(Document doc, int offset) throws BadLocationException {
         return new Position(LineDocumentUtils.getLineIndex((LineDocument) doc, offset),
                             offset - LineDocumentUtils.getLineStart((LineDocument) doc, offset));
    }

    public static int getOffset(Document doc, Position pos) {
        return LineDocumentUtils.getLineStartFromIndex((LineDocument) doc, pos.getLine()) + pos.getCharacter();
    }
    
    public static int getEndCharacter(Document doc, int line) {
        int start = LineDocumentUtils.getLineStartFromIndex((LineDocument) doc, line);
        try {
            return LineDocumentUtils.getLineEnd((LineDocument) doc, start) - start;
        } catch (BadLocationException ex) {
            Exceptions.printStackTrace(ex);
        }
        return 0;
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
            EditorCookie ec = lookupForFile(file, EditorCookie.class);
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
        applyEditsNoLock(doc, edits, null, null);
    }

    /**
     * Apply edits to the document. The edits can be filtered to only cover
     * parts of the documents.
     *
     * @param doc        {@link Document} the edits shall be applied to
     * @param edits      list {@link TextEdit} to apply
     * @param startLimit if not {@code null} only edits with a {@code start}
     *                   larger than or equals to this offset are considered.
     *                   The offset is expected to be apply to the original
     *                   state of the document.
     * @param endLimit   if not {@code null} only edits with an {@code end}
     *                   lower than this offset are considered. The offset is
     *                   expected to be apply to the original state of the
     *                   document.
     */
    public static void applyEditsNoLock(Document doc, List<? extends TextEdit> edits, Integer startLimit, Integer endLimit) {
        edits
         .stream()
         .sorted(rangeReverseSort)
         .forEach(te -> {
            try {
                int start = Utils.getOffset(doc, te.getRange().getStart());
                int end = Utils.getOffset(doc, te.getRange().getEnd());
                if ((startLimit == null || start >= startLimit)
                    && (endLimit == null || end < endLimit)) {
                    doc.remove(start, end - start);
                    doc.insertString(start, te.getNewText(), null);
                }
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
                if(cmd.getRight().getEdit() != null) {
                    Utils.applyWorkspaceEdit(cmd.getRight().getEdit());
                }
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

    public static FileObject fromURI(String targetUri) {
        try {
            URI target = URI.create(targetUri);
            return URLMapper.findFileObject(target.toURL());
        } catch (MalformedURLException ex) {
            Exceptions.printStackTrace(ex);
            return null;
        }
    }

    public static void open(String targetUri, Range targetRange) {
        FileObject targetFile = fromURI(targetUri);

        if (targetFile != null) {
            LineCookie lc = lookupForFile(targetFile, LineCookie.class);

            //TODO: expecting lc != null!

            Line line = lc.getLineSet().getCurrent(targetRange.getStart().getLine());

            SwingUtilities.invokeLater(() ->
                line.show(Line.ShowOpenType.OPEN, Line.ShowVisibilityType.FOCUS, targetRange.getStart().getCharacter())
            );
        } else {
            //TODO: beep
        }
    }

    public static <T> T lookupForFile(FileObject targetFile, Class<T> clazz) {
        if(targetFile == null) {
            return null;
        }
        T lc = null;
        try {
            if (lc == null) {
                DataObject dataObject = DataObject.find(targetFile);
                lc = dataObject.getLookup().lookup(clazz);
            }
        } catch (DataObjectNotFoundException ex) {
            // Ignore
        }
        if (lc == null) {
            lc = targetFile.getLookup().lookup(clazz);
        }
        return lc;
    }

    private static final Comparator<TextEdit> rangeReverseSort = (s1, s2) -> {
        int l1 = s1.getRange().getEnd().getLine();
        int l2 = s2.getRange().getEnd().getLine();
        int c1 = s1.getRange().getEnd().getCharacter();
        int c2 = s2.getRange().getEnd().getCharacter();
        if (l1 != l2) {
            return l2 - l1;
        } else {
            return c2 - c1;
        }
    };

    public static boolean isTrue(Boolean b) {
        return b != null && b;
    }
    public static boolean isEnabled(Either<Boolean, ?> settings) {
        return settings != null && (settings.isLeft() ? isTrue(settings.getLeft())
                                                       : settings.getRight() != null);
    }
}
