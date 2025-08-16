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
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import org.eclipse.lsp4j.CreateFile;
import org.eclipse.lsp4j.DeleteFile;
import org.eclipse.lsp4j.InitializeParams;
import org.eclipse.lsp4j.InitializeResult;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.RenameFile;
import org.eclipse.lsp4j.RenameOptions;
import org.eclipse.lsp4j.RenameParams;
import org.eclipse.lsp4j.ResourceOperation;
import org.eclipse.lsp4j.ServerCapabilities;
import org.eclipse.lsp4j.TextDocumentEdit;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4j.VersionedTextDocumentIdentifier;
import org.eclipse.lsp4j.WorkspaceEdit;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.services.LanguageServer;
import org.eclipse.lsp4j.services.TextDocumentService;
import org.eclipse.lsp4j.services.WorkspaceService;
import org.junit.Test;
import org.netbeans.junit.MockServices;
import org.netbeans.modules.lsp.client.Utils;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.text.CloneableEditorSupport;
import static org.junit.Assert.*;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.editor.NbEditorKit;
import org.netbeans.modules.lsp.client.LSPBindings;
import org.netbeans.modules.lsp.client.TestUtils.BaseTextDocumentServiceImpl;
import org.netbeans.modules.lsp.client.TestUtils.BaseWorkspaceServiceImpl;
import static org.netbeans.modules.lsp.client.TestUtils.MIME_TYPE;
import org.netbeans.modules.lsp.client.TestUtils.MimeDataProviderImpl;
import org.netbeans.modules.lsp.client.TestUtils.MockLSP;
import org.netbeans.modules.lsp.client.TestUtils.MockMimeResolver;
import org.netbeans.modules.lsp.client.bindings.refactoring.Refactoring.LSPCreateFile;
import org.netbeans.modules.lsp.client.bindings.refactoring.Refactoring.LSPDeleteFile;
import org.netbeans.modules.lsp.client.bindings.refactoring.Refactoring.LSPRenameFile;
import org.netbeans.modules.lsp.client.bindings.refactoring.tree.DiffElement;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.api.RefactoringElement;
import org.netbeans.modules.refactoring.api.RefactoringSession;
import org.netbeans.modules.refactoring.api.RenameRefactoring;
import org.netbeans.modules.refactoring.api.impl.APIAccessor;
import org.netbeans.modules.refactoring.spi.RefactoringElementImplementation;
import org.netbeans.modules.refactoring.spi.impl.UndoableWrapper;
import org.netbeans.spi.editor.mimelookup.MimeDataProvider;
import org.openide.text.PositionRef;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author lahvac
 */
public class RenameRefactoringTest {

    @Test
    public void testSimpleRename() throws Exception {
        MockLSP.createServer = () -> new TestLanguageServer();
        MockServices.setServices(MimeDataProviderImpl.class, MockMimeResolver.class, RootMimeDataProviderImpl.class);

        FileObject folder = FileUtil.createMemoryFileSystem().getRoot().createFolder("myfolder");
        FileObject file1 = createFile(folder, "data1.mock-txt");

        try (OutputStream out = file1.getOutputStream()) {
            out.write(("  test  other\n" +
                       "  other test\n").getBytes(StandardCharsets.UTF_8));
        }

        FileObject file2 = createFile(folder, "data2.mock-txt");

        try (OutputStream out = file2.getOutputStream()) {
            out.write(("  2test  other\n" +
                       "  2other test\n").getBytes(StandardCharsets.UTF_8));
        }

        String uri = Utils.toURI(file1);

        List<LSPBindings> bindings = LSPBindings.getBindings(file1);
        RenameParams renameParams = new RenameParams(new TextDocumentIdentifier(uri), new Position(1, 8), "newName");
        List<Function<RenameParams, WorkspaceEdit>> renameFunctions = Arrays.asList(
            params -> {
                assertEquals(uri, params.getTextDocument().getUri());
                assertEquals("newName", params.getNewName());

                WorkspaceEdit result = new WorkspaceEdit();
                String file1URI = params.getTextDocument().getUri();
                TextDocumentEdit file1Edits = new TextDocumentEdit(new VersionedTextDocumentIdentifier(file1URI, -1),
                                                                   Arrays.asList(new TextEdit(new Range(new Position(0, 2), new Position(0,  6)), "newName"),
                                                                                 new TextEdit(new Range(new Position(1, 8), new Position(1, 12)), "newName")));
                String file2URI = file1URI.replace("data1", "data2");
                TextDocumentEdit file2Edits = new TextDocumentEdit(new VersionedTextDocumentIdentifier(file2URI, -1),
                                                                   Arrays.asList(new TextEdit(new Range(new Position(0, 3), new Position(0,  7)), "newName"),
                                                                                 new TextEdit(new Range(new Position(1, 9), new Position(1, 13)), "newName")));
                result.setDocumentChanges(Arrays.asList(Either.forLeft(file1Edits), Either.forLeft(file2Edits)));
                return result;
            },
            params -> {
                assertEquals(uri, params.getTextDocument().getUri());
                assertEquals("newName", params.getNewName());

                WorkspaceEdit result = new WorkspaceEdit();
                Map<String, List<TextEdit>> file2Edits = new HashMap<>();
                String file1URI = params.getTextDocument().getUri();
                file2Edits.put(file1URI, Arrays.asList(new TextEdit(new Range(new Position(0, 2), new Position(0,  6)), "newName"),
                                                       new TextEdit(new Range(new Position(1, 8), new Position(1, 12)), "newName")));
                String file2URI = file1URI.replace("data1", "data2");
                file2Edits.put(file2URI, Arrays.asList(new TextEdit(new Range(new Position(0, 3), new Position(0,  7)), "newName"),
                                                       new TextEdit(new Range(new Position(1, 9), new Position(1, 13)), "newName")));

                result.setChanges(file2Edits);
                return result;
            }
        );
        for (Function<RenameParams, WorkspaceEdit> renameFunc : renameFunctions) {
            renameFunction = renameFunc;

            RenameRefactoring refactoring = new RenameRefactoring(Lookups.fixed(new LSPBindingsCollection(bindings), renameParams));

            RefactoringSession session = RefactoringSession.create("test rename");
            assertNull(refactoring.checkParameters());
            assertNull(refactoring.preCheck());
            assertNull(refactoring.prepare(session));

            Set<String> elements = new HashSet<>();

            for (RefactoringElement re : session.getRefactoringElements()) {
                RefactoringElementImplementation impl =
                        APIAccessor.DEFAULT.getRefactoringElementImplementation(re);
                Method getNewFileContent = impl.getClass().getDeclaredMethod("getNewFileContent");

                getNewFileContent.setAccessible(true);

                String newFileContent = (String) getNewFileContent.invoke(impl);
                String element = positionToString(re.getPosition().getBegin()) + "-" +
                                 positionToString(re.getPosition().getEnd()) + ":" +
                                 newFileContent;

                elements.add(element);
            }

            Set<String> expectedElements = new HashSet<>(Arrays.asList(
                    "1:9-1:13:  2newName  other\n" +
                    "  2other newName\n",
                    "0:3-0:7:  2newName  other\n" +
                    "  2other newName\n",
                    "1:8-1:12:  newName  other\n" +
                    "  other newName\n",
                    "0:2-0:6:  newName  other\n" +
                    "  other newName\n"
            ));

            assertEquals(expectedElements, elements);

            session.doRefactoring(true);

            assertFile(file1, "  newName  other\n" +
                              "  other newName\n");
            assertFile(file2, "  2newName  other\n" +
                              "  2other newName\n");

            session.undoRefactoring(true);

            assertFile(file1, "  test  other\n" +
                              "  other test\n");
            assertFile(file2, "  2test  other\n" +
                              "  2other test\n");
        }
    }

    @Test
    public void testFileOperations() throws Exception {
        MockLSP.createServer = () -> new TestLanguageServer();
        MockServices.setServices(MimeDataProviderImpl.class, MockMimeResolver.class, RootMimeDataProviderImpl.class);

        FileObject folder = FileUtil.createMemoryFileSystem().getRoot().createFolder("myfolder");
        FileObject file1 = createFile(folder, "data1.mock-txt");

        try (OutputStream out = file1.getOutputStream()) {
            out.write(("  test  other\n" +
                       "  other test\n").getBytes(StandardCharsets.UTF_8));
        }

        FileObject file2 = createFile(folder, "data2.mock-txt");

        try (OutputStream out = file2.getOutputStream()) {
            out.write(("  2test  other\n" +
                       "  2other test\n").getBytes(StandardCharsets.UTF_8));
        }

        String uri = Utils.toURI(file1);

        List<LSPBindings> bindings = LSPBindings.getBindings(file1);
        RenameParams renameParams = new RenameParams(new TextDocumentIdentifier(uri), new Position(1, 8), "newName");
        renameFunction = params -> {
            assertEquals(uri, params.getTextDocument().getUri());
            assertEquals("newName", params.getNewName());

            WorkspaceEdit result = new WorkspaceEdit();
            String file1URI = params.getTextDocument().getUri();
            TextDocumentEdit file1Edits = new TextDocumentEdit(new VersionedTextDocumentIdentifier(file1URI, -1),
                                                               Arrays.asList(new TextEdit(new Range(new Position(1, 8), new Position(1, 12)), "newName")));
            String file1aURI = file1URI.replace("data1", "data1a");
            ResourceOperation file1Operation = new RenameFile(file1URI, file1aURI);
            TextDocumentEdit file1aEdits = new TextDocumentEdit(new VersionedTextDocumentIdentifier(file1aURI, -1),
                                                                Arrays.asList(new TextEdit(new Range(new Position(0, 2), new Position(0,  6)), "newName")));
            String file2URI = file1URI.replace("data1", "data2");
            ResourceOperation file2Operation = new DeleteFile(file2URI);
            String file3URI = file1URI.replace("data1", "data3");
            ResourceOperation file3Operation = new CreateFile(file3URI);
            TextDocumentEdit file3Edits = new TextDocumentEdit(new VersionedTextDocumentIdentifier(file3URI, -1),
                                                               Arrays.asList(new TextEdit(new Range(new Position(0, 0), new Position(0,  0)), "newName content\n")));
            result.setDocumentChanges(Arrays.asList(Either.forLeft(file1Edits),
                                                    Either.forRight(file1Operation),
                                                    Either.forLeft(file1aEdits),
                                                    Either.forRight(file2Operation),
                                                    Either.forRight(file3Operation),
                                                    Either.forLeft(file3Edits))
            );
            return result;
        };

        RenameRefactoring refactoring = new RenameRefactoring(Lookups.fixed(new LSPBindingsCollection(bindings), renameParams));

        RefactoringSession session = RefactoringSession.create("test rename");
        assertNull(refactoring.checkParameters());
        assertNull(refactoring.preCheck());
        Problem problem = refactoring.prepare(session);
        assertNull(problem2String(problem), problem);

        Set<String> elements = new HashSet<>();

        for (RefactoringElement re : session.getRefactoringElements()) {
            RefactoringElementImplementation impl =
                    APIAccessor.DEFAULT.getRefactoringElementImplementation(re);
            if (impl instanceof DiffElement) {
                Method getNewFileContent = impl.getClass().getDeclaredMethod("getNewFileContent");

                getNewFileContent.setAccessible(true);

                String newFileContent = (String) getNewFileContent.invoke(impl);
                String element = positionToString(re.getPosition().getBegin()) + "-" +
                                 positionToString(re.getPosition().getEnd()) + ":" +
                                 newFileContent;

                elements.add(element);
            } else if (impl instanceof LSPRenameFile ||
                       impl instanceof LSPDeleteFile ||
                       impl instanceof LSPCreateFile) {
                elements.add(impl.toString());
            } else {
                fail("Unknown element class: " + impl.getClass());
            }
        }

        Set<String> expectedElements = new HashSet<>(Arrays.asList(
                "data2.mock-txt=>",
                "data1.mock-txt=>data1a.mock-txt",
                "1:8-1:12:  newName  other\n" +
                "  other newName\n",
                "0:2-0:6:  newName  other\n" +
                "  other newName\n",
                "=>data3.mock-txt(newName content\n)"
        ));

        assertEquals(expectedElements, elements);

        session.doRefactoring(true);

        assertEquals("data1a.mock-txt", file1.getNameExt());
        assertFile(file1, "  newName  other\n" +
                          "  other newName\n");
        assertFalse(file2.isValid());

        //Backup facility does not handle non file:// URLs:
//        session.undoRefactoring(true);
//
//        assertFile(file1, "  test  other\n" +
//                          "  other test\n");
//        assertFile(file2, "  2test  other\n" +
//                          "  2other test\n");
    }

    private FileObject createFile(FileObject folder, String name) throws Exception {
        FileObject file = folder.createData(name);
        EditorCookie ec = file.getLookup().lookup(EditorCookie.class);
        ((CloneableEditorSupport) ec).setMIMEType(MIME_TYPE);

        return file;
    }

    private String positionToString(PositionRef p) throws IOException {
        return "" + p.getLine() + ":" + p.getColumn();
    }

    private void assertFile(FileObject file, String expectedContent) throws IOException {
        assertEquals(expectedContent, file.asText());
    }

    private String problem2String(Problem p) {
        if (p == null){
            return null;
        }
        return p.getMessage() + ":" + p.isFatal() + (p.getNext() != null ? "[" + problem2String(p.getNext()) + "]" : "");
    }

    private static Function<RenameParams, WorkspaceEdit> renameFunction;

    private static final class TestLanguageServer implements LanguageServer {

        @Override
        public CompletableFuture<InitializeResult> initialize(InitializeParams params) {
            ServerCapabilities caps = new ServerCapabilities();
            RenameOptions renameOptions = new RenameOptions();
            caps.setRenameProvider(renameOptions);
            InitializeResult initResult = new InitializeResult(caps);
            return CompletableFuture.completedFuture(initResult);
        }

        @Override
        public CompletableFuture<Object> shutdown() {
            return CompletableFuture.completedFuture(null);
        }

        @Override
        public void exit() {
        }

        @Override
        public TextDocumentService getTextDocumentService() {
            return new BaseTextDocumentServiceImpl() {
                @Override
                public CompletableFuture<WorkspaceEdit> rename(RenameParams params) {
                    WorkspaceEdit result = renameFunction.apply(params);
                    return CompletableFuture.completedFuture(result);
                }
            };
        }

        @Override
        public WorkspaceService getWorkspaceService() {
            return new BaseWorkspaceServiceImpl();
        }

    }

    public static final class RootMimeDataProviderImpl implements MimeDataProvider {
        @Override
        public Lookup getLookup(MimePath mp) {
            if ("".equals(mp.getPath())) {
                return Lookups.fixed(new UndoableWrapper());
            }
            return Lookup.EMPTY;
        }
    }
}
