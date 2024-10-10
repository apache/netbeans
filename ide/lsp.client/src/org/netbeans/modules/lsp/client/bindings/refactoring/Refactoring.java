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
package org.netbeans.modules.lsp.client.bindings.refactoring;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.text.BadLocationException;
import javax.swing.text.Position;
import javax.swing.text.StyledDocument;
import org.eclipse.lsp4j.CreateFile;
import org.eclipse.lsp4j.DeleteFile;
import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.ReferenceParams;
import org.eclipse.lsp4j.RenameFile;
import org.eclipse.lsp4j.RenameParams;
import org.eclipse.lsp4j.ResourceOperation;
import org.eclipse.lsp4j.ResourceOperationKind;
import org.eclipse.lsp4j.TextDocumentEdit;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4j.WorkspaceEdit;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.netbeans.api.queries.FileEncodingQuery;
import org.netbeans.modules.lsp.client.LSPBindings;
import org.netbeans.modules.lsp.client.Utils;
import org.netbeans.modules.lsp.client.bindings.refactoring.ModificationResult.Difference;
import org.netbeans.modules.lsp.client.bindings.refactoring.tree.DiffElement;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.api.RenameRefactoring;
import org.netbeans.modules.refactoring.api.WhereUsedQuery;
import org.netbeans.modules.refactoring.spi.BackupFacility;
import org.netbeans.modules.refactoring.spi.RefactoringCommit;
import org.netbeans.modules.refactoring.spi.RefactoringElementsBag;
import org.netbeans.modules.refactoring.spi.RefactoringPlugin;
import org.netbeans.modules.refactoring.spi.RefactoringPluginFactory;
import org.netbeans.modules.refactoring.spi.SimpleRefactoringElementImplementation;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.LineCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.text.CloneableEditorSupport;
import org.openide.text.Line;
import org.openide.text.PositionBounds;
import org.openide.text.PositionRef;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.Pair;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author lahvac
 */
@NbBundle.Messages("TXT_Canceled=Canceled")
public class Refactoring {

    private static final class WhereUsedRefactoringPlugin implements RefactoringPlugin {

        private final WhereUsedQuery query;
        private final LSPBindings bindings;
        private final ReferenceParams params;
        private final AtomicBoolean cancel = new AtomicBoolean();
        private volatile CompletableFuture<List<? extends Location>> runningRequest;

        public WhereUsedRefactoringPlugin(WhereUsedQuery query, LSPBindings bindings, ReferenceParams params) {
            this.query = query;
            this.bindings = bindings;
            this.params = params;
        }

        @Override
        public Problem preCheck() {
            return null;
        }

        @Override
        public Problem checkParameters() {
            return null;
        }

        @Override
        public Problem fastCheckParameters() {
            return null;
        }

        @Override
        public void cancelRequest() {
            cancel.set(true);
            CompletableFuture localRunningRequest = runningRequest;
            if(localRunningRequest != null) {
                localRunningRequest.cancel(true);
            }
        }

        @Override
        public Problem prepare(RefactoringElementsBag refactoringElements) {
            try {
                runningRequest = bindings.getTextDocumentService().references(params);
                for (Location l : runningRequest.get()) {
                    if(cancel.get()) {
                        break;
                    }
                    FileObject file = Utils.fromURI(l.getUri());
                    if (file != null) {
                        PositionBounds bounds;
                        try {
                            CloneableEditorSupport es = Utils.lookupForFile(file, CloneableEditorSupport.class);
                            EditorCookie ec = Utils.lookupForFile(file, EditorCookie.class);
                            StyledDocument doc = ec.openDocument();

                            bounds = new PositionBounds(es.createPositionRef(Utils.getOffset(doc, l.getRange().getStart()), Position.Bias.Forward),
                                                            es.createPositionRef(Utils.getOffset(doc, l.getRange().getEnd()), Position.Bias.Forward));
                        } catch (IOException ex) {
                            Exceptions.printStackTrace(ex);
                            bounds = null;
                        }
                        LineCookie lc = Utils.lookupForFile(file, LineCookie.class);
                        Line startLine = lc.getLineSet().getCurrent(l.getRange().getStart().getLine());
                        String lineText = startLine.getText();
                        int highlightEnd = Math.min(lineText.length(), l.getRange().getEnd().getCharacter());
                        String annotatedLine = lineText.substring(0, l.getRange().getStart().getCharacter()) +
                                               "<strong>" + lineText.substring(l.getRange().getStart().getCharacter(), highlightEnd) + "</strong>" +
                                               lineText.substring(highlightEnd);
                        refactoringElements.add(query, new LSPRefactoringElementImpl(annotatedLine, file, bounds));
                    }
                }
                runningRequest = null;
                return null;
            } catch (CancellationException ex) {
                return new Problem(false, Bundle.TXT_Canceled());
            } catch (InterruptedException | ExecutionException ex) {
                Exceptions.printStackTrace(ex);
                return new Problem(true, ex.getLocalizedMessage());
            }
        }

    }

    private static final class RenameRefactoringPlugin implements RefactoringPlugin {

        private final RenameRefactoring refactoring;
        private final LSPBindings bindings;
        private final RenameParams params;
        private final AtomicBoolean cancel = new AtomicBoolean();
        private volatile CompletableFuture<WorkspaceEdit> runningRequest;

        public RenameRefactoringPlugin(RenameRefactoring refactoring, LSPBindings bindings, RenameParams params) {
            this.refactoring = refactoring;
            this.bindings = bindings;
            this.params = params;
        }

        @Override
        public Problem preCheck() {
            return null;
        }

        @Override
        public Problem checkParameters() {
            return null;
        }

        @Override
        public Problem fastCheckParameters() {
            return null;
        }

        @Override
        public void cancelRequest() {
            cancel.set(true);
            CompletableFuture localRunningRequest = runningRequest;
            if(localRunningRequest != null) {
                localRunningRequest.cancel(true);
            }
        }

        @Override
        public Problem prepare(RefactoringElementsBag refactoringElements) {
            if (cancel.get()) {
                return new Problem(false, Bundle.TXT_Canceled());
            }
            Problem p = null;
            try {
                runningRequest = bindings.getTextDocumentService().rename(params);
                WorkspaceEdit edit = runningRequest.get();
                List<Either<TextDocumentEdit, ResourceOperation>> documentChanges = edit.getDocumentChanges();
                ModificationResult result = new ModificationResult();
                Map<FileObject, List<Difference>> file2Diffs = new HashMap<>();
                Map<String, String> newURI2Old = new HashMap<>();
                Map<String, String> newFileURI2Content = new HashMap<>();

                if (documentChanges != null) {
                    for (Either<TextDocumentEdit, ResourceOperation> part : documentChanges) {
                        if(cancel.get()) {
                            break;
                        }
                        if (part.isLeft()) {
                            String uri = part.getLeft().getTextDocument().getUri();
                            uri = newURI2Old.getOrDefault(uri, uri);
                            FileObject file = Utils.fromURI(uri);

                            if (file != null) {
                                for (TextEdit te : part.getLeft().getEdits()) {
                                    Difference diff = textEdit2Difference(file, te);
                                    file2Diffs.computeIfAbsent(file, f -> new ArrayList<>())
                                              .add(diff);
                                }
                            } else if (newFileURI2Content.containsKey(uri)) {
                                FileObject temp = FileUtil.createMemoryFileSystem().getRoot().createData("temp.txt");
                                try (OutputStream out = temp.getOutputStream()) {
                                    out.write(newFileURI2Content.get(uri).getBytes()); //TODO: encoding - native, OK?
                                }
                                List<Difference> diffs = new ArrayList<>();
                                for (TextEdit te : part.getLeft().getEdits()) {
                                    diffs.add(textEdit2Difference(temp, te));
                                }
                                ModificationResult tempResult = new ModificationResult();
                                tempResult.addDifferences(temp, diffs);
                                newFileURI2Content.put(uri, tempResult.getResultingSource(temp));
                            } else {
                                //XXX: problem...
                            }
                        } else {
                            switch (part.getRight().getKind()) {
                                case ResourceOperationKind.Rename: {
                                    RenameFile rename = (RenameFile) part.getRight();
                                    FileObject file = Utils.fromURI(rename.getOldUri());
                                    refactoringElements.addFileChange(refactoring, new LSPRenameFile(file, rename.getNewUri()));
                                    newURI2Old.put(rename.getNewUri(), rename.getOldUri());
                                    break;
                                }
                                case ResourceOperationKind.Delete: {
                                    DeleteFile delete = (DeleteFile) part.getRight();
                                    FileObject file = Utils.fromURI(delete.getUri());
                                    refactoringElements.addFileChange(refactoring, new LSPDeleteFile(file));
                                    break;
                                }
                                case ResourceOperationKind.Create: {
                                    CreateFile create = (CreateFile) part.getRight();
                                    String uri = create.getUri();
                                    newFileURI2Content.put(uri, "");
                                    break;
                                }
                                default:
                                    p = chain(new Problem(true, "Unknown file operation: " + part.getRight().getKind()), p);
                                    break;
                            }
                        }
                    }
                } else {
                    for (Entry<String, List<TextEdit>> fileAndChanges : edit.getChanges().entrySet()) {
                        if(cancel.get()) {
                            break;
                        }
                        //TODO: errors:
                        FileObject file = Utils.fromURI(fileAndChanges.getKey());

                        for (TextEdit te : fileAndChanges.getValue()) {
                            Difference diff = textEdit2Difference(file, te);
                            file2Diffs.computeIfAbsent(file, f -> new ArrayList<>())
                                      .add(diff);
                        }
                    }
                }

                if (cancel.get()) {
                    p = chain(new Problem(false, Bundle.TXT_Canceled()), p);
                } else {

                    file2Diffs.entrySet()
                        .forEach(e -> {
                            e.getValue()
                                .forEach(diff -> refactoringElements.add(refactoring, DiffElement.create(diff, e.getKey(), result)));
                            result.addDifferences(e.getKey(), e.getValue());
                        });

                    newFileURI2Content.entrySet()
                        .forEach(e -> {
                            refactoringElements.add(refactoring, new LSPCreateFile(e.getKey(), e.getValue()));
                        });
                    refactoringElements.registerTransaction(new RefactoringCommit(Collections.singletonList(result)));

                    if (cancel.get()) {
                        p = chain(new Problem(false, Bundle.TXT_Canceled()), p);
                    }
                }

                return p;
            } catch (CancellationException ex) {
                return chain(new Problem(false, Bundle.TXT_Canceled()), p);
            } catch (InterruptedException | ExecutionException | IOException ex) {
                return chain(new Problem(true, ex.getLocalizedMessage()), p);
            } finally {
                runningRequest = null;
            }
        }

        private Problem chain(Problem current, Problem existing) {
            if (existing != null) {
                current.setNext(existing);
            }
            return current;
        }

        private Difference textEdit2Difference(FileObject file, TextEdit edit) {
            if (file != null) {
                try {
                    EditorCookie ec = Utils.lookupForFile(file, EditorCookie.class);
                    StyledDocument doc = ec.openDocument();
                    CloneableEditorSupport es = Utils.lookupForFile(file, CloneableEditorSupport.class);

                    PositionRef start = es.createPositionRef(Utils.getOffset(doc, edit.getRange().getStart()), Position.Bias.Forward);
                    PositionRef end   = es.createPositionRef(Utils.getOffset(doc, edit.getRange().getEnd()), Position.Bias.Forward);
                    PositionBounds bounds = new PositionBounds(start, end);
                    return new Difference(Difference.Kind.CHANGE, start, end, bounds.getText(), edit.getNewText());
                } catch (IOException | BadLocationException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }

            return null;
        }
    }

    private static String uri2SimpleName(String uri) {
        int dot = uri.lastIndexOf('/');

        return uri.substring(dot + 1);
    }

    public static class LSPRefactoringElementImpl extends SimpleRefactoringElementImplementation {

        private final String annotatedLine;
        private final FileObject file;
        private final PositionBounds bounds;

        public LSPRefactoringElementImpl(String annotatedLine, FileObject file, PositionBounds bounds) {
            this.annotatedLine = annotatedLine;
            this.file = file;
            this.bounds = bounds;
        }

        @Override
        public String getText() {
            return "TODO: getText";
        }

        @Override
        public String getDisplayText() {
            return annotatedLine;
        }

        @Override
        public void performChange() {
            // Currently the LSPRefactoringElementImpl is only used for the
            // WhereUsedRefactoring, which is not doing changes
            throw new UnsupportedOperationException();
        }

        @Override
        public Lookup getLookup() {
            return Lookup.EMPTY;
        }

        @Override
        public FileObject getParentFile() {
            return file;
        }

        @Override
        public PositionBounds getPosition() {
            return bounds;
        }
    }

    public static class LSPRenameFile extends SimpleRefactoringElementImplementation {

        private final FileObject fo;
        private final String newUri;
        public LSPRenameFile(FileObject fo, String newUri) {
            this.fo = fo;
            this.oldUri = fo.toURI().toString();
            this.newUri = newUri;
        }

        @Override
        @NbBundle.Messages({
            "# {0} - current name of the file",
            "TXT_RenameFile=Rename file {0}",
            "# {0} - current name of the folders",
            "TXT_RenameFolder=Rename folder {0}"
        })
        public String getText() {
            return fo.isFolder()? Bundle.TXT_RenameFolder(fo.getNameExt()) :
                                  Bundle.TXT_RenameFile(fo.getNameExt());
        }

        @Override
        public String getDisplayText() {
            return getText();
        }

        private String oldUri;
        
        @Override
        public void performChange() {
            oldUri = fo.getName();
            doRename(newUri);
        }
        
        @Override
        public void undoChange(){
//            if (!fo.isValid()) {
//                throw new CannotUndoRefactoring(Collections.singleton(fo.getPath()));
//            }
            doRename(oldUri);
        }

        private void doRename(String uri) {
            try {
                //XXX: different path, not only the name:
                String newName = uri.substring(uri.lastIndexOf('/') + 1);
                DataObject.find(fo).rename(newName);
            } catch (DataObjectNotFoundException ex) {
                throw new IllegalStateException(ex);
            } catch (IOException ex) {
                throw new IllegalStateException(ex);
            }
        }

        @Override
        public Lookup getLookup() {
            return Lookup.EMPTY;
        }

        @Override
        public FileObject getParentFile() {
            return fo;
        }

        @Override
        public PositionBounds getPosition() {
            return null;
        }

        @Override
        public String toString() {
            return uri2SimpleName(fo.toURI().toString()) + "=>" + uri2SimpleName(newUri);
        }

    }

    public static class LSPDeleteFile extends SimpleRefactoringElementImplementation {
        private final URL res;
        private final String filename;

        private BackupFacility.Handle id;

        /**
         *
         * @param fo
         * @param session
         */
        public LSPDeleteFile(FileObject fo) {
            this.res = fo.toURL();
            this.filename = fo.getNameExt();
        }

        @Override
        @NbBundle.Messages({
            "# {0} - name of the file to be deleted",
            "TXT_DeleteFile=Delete file {0}"
        })
        public String getText() {
            return Bundle.TXT_DeleteFile(filename);
        }

        @Override
        public String getDisplayText() {
            return getText();
        }

        @Override
        public void performChange() {
            try {
                FileObject fo = URLMapper.findFileObject(res);
                if (fo == null) {
                    throw new IOException(res.toString());
                }
                id = BackupFacility.getDefault().backup(fo);
                DataObject.find(fo).delete();
            } catch (DataObjectNotFoundException ex) {
                Exceptions.printStackTrace(ex);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }

        @Override
        public void undoChange() {
            try {
                FileObject f = URLMapper.findFileObject(res);
                if (f != null) {
//                        throw new CannotUndoRefactoring(Collections.singleton(f.getPath()));
                }
                id.restore();
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }

        @Override
        public Lookup getLookup() {
            return Lookup.EMPTY;
        }

        @Override
        public FileObject getParentFile() {
            return URLMapper.findFileObject(res);
        }

        @Override
        public PositionBounds getPosition() {
            return null;
        }

        @Override
        public String toString() {
            return uri2SimpleName(res.toString()) + "=>";
        }

    }

    public static class LSPCreateFile extends SimpleRefactoringElementImplementation {
        private final String uri;
        private final String content;

        /**
         * 
         * @param fo
         * @param session
         */
        public LSPCreateFile(String uri, String content) {
            this.uri = uri;
            this.content = content;
        }

        @Override
        @NbBundle.Messages({
            "# {0} - name of the newly created file",
            "TXT_CreateFile=Create file {0}"
        })
        public String getText() {
            return Bundle.TXT_CreateFile(uri2SimpleName(uri));
        }

        @Override
        public String getDisplayText() {
            return getText();
        }

        private FileObject target;

        @Override
        public void performChange() {
            try {
                Pair<FileObject, String> p = fileAndRemainingPath(uri);
                target = FileUtil.createData(p.first(), p.second());
                try (Writer w = new OutputStreamWriter(target.getOutputStream(), FileEncodingQuery.getEncoding(target))) {
                    w.write(content);
                }
            } catch (IOException | URISyntaxException ex) {
                Exceptions.printStackTrace(ex);
            }
        }

        @Override
        public void undoChange() {
            try {
                target.delete();
                target = null;
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }

        @Override
        public Lookup getLookup() {
            return Lookup.EMPTY;
        }

        @Override
        public FileObject getParentFile() {
            try {
                return fileAndRemainingPath(uri).first();
            } catch (URISyntaxException | MalformedURLException ex) {
                Exceptions.printStackTrace(ex);
                return null;
            }
        }

        @SuppressWarnings({"NestedAssignment", "AssignmentToMethodParameter"})
        private static Pair<FileObject, String> fileAndRemainingPath(String uri) throws URISyntaxException, MalformedURLException {
            StringBuilder path = new StringBuilder();
            FileObject existing;
            while ((existing = URLMapper.findFileObject(new URI(uri).toURL())) == null) {
                int slash = uri.lastIndexOf('/');
                if (path.length() > 0) {
                    path.insert(0, '/');
                }
                path.insert(0, uri.substring(slash + 1));
                uri = uri.substring(0, slash);
            }
            return Pair.of(existing, path.toString());
        }

        @Override
        public PositionBounds getPosition() {
            return null;
        }

        @Override
        public String toString() {
            return "=>" + uri2SimpleName(uri) + "(" + content + ")";
        }

    }

    @ServiceProvider(service=RefactoringPluginFactory.class)
    public static class FactoryImpl implements RefactoringPluginFactory {

        @Override
        public RefactoringPlugin createInstance(AbstractRefactoring refactoring) {
            if (refactoring instanceof WhereUsedQuery) {
                WhereUsedQuery q = (WhereUsedQuery) refactoring;
                LSPBindings bindings = q.getRefactoringSource().lookup(LSPBindings.class);
                ReferenceParams params = q.getRefactoringSource().lookup(ReferenceParams.class);
                if (bindings != null && params != null) {
                    return new WhereUsedRefactoringPlugin(q, bindings, params);
                }
            } else if (refactoring instanceof RenameRefactoring) {
                RenameRefactoring r = (RenameRefactoring) refactoring;
                LSPBindings bindings = r.getRefactoringSource().lookup(LSPBindings.class);
                RenameParams params = r.getRefactoringSource().lookup(RenameParams.class);
                if (bindings != null && params != null) {
                    return new RenameRefactoringPlugin(r, bindings, params);
                }
            }
            return null;
        }

    }

}
