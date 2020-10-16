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
import java.util.concurrent.ExecutionException;
import javax.swing.text.Position;
import javax.swing.text.StyledDocument;
import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.ReferenceParams;
import org.netbeans.modules.lsp.client.LSPBindings;
import org.netbeans.modules.lsp.client.Utils;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.api.WhereUsedQuery;
import org.netbeans.modules.refactoring.spi.RefactoringElementsBag;
import org.netbeans.modules.refactoring.spi.RefactoringPlugin;
import org.netbeans.modules.refactoring.spi.RefactoringPluginFactory;
import org.netbeans.modules.refactoring.spi.SimpleRefactoringElementImplementation;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.LineCookie;
import org.openide.filesystems.FileObject;
import org.openide.text.CloneableEditorSupport;
import org.openide.text.Line;
import org.openide.text.PositionBounds;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author lahvac
 */
public class Refactoring {

    private static final class WhereUsedRefactoringPlugin implements RefactoringPlugin {

        private final WhereUsedQuery query;
        private final LSPBindings bindings;
        private final ReferenceParams params;

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
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Problem prepare(RefactoringElementsBag refactoringElements) {
            try {
                for (Location l : bindings.getTextDocumentService().references(params).get()) {
                    FileObject file = Utils.fromURI(l.getUri());
                    PositionBounds boundsTemp = null;
                    if (file != null) {
                        try {
                            CloneableEditorSupport es = file.getLookup().lookup(CloneableEditorSupport.class);
                            EditorCookie ec = file.getLookup().lookup(EditorCookie.class);
                            StyledDocument doc = ec.openDocument();

                            boundsTemp = new PositionBounds(es.createPositionRef(Utils.getOffset(doc, l.getRange().getStart()), Position.Bias.Forward),
                                                            es.createPositionRef(Utils.getOffset(doc, l.getRange().getEnd()), Position.Bias.Forward));
                        } catch (IOException ex) {
                            Exceptions.printStackTrace(ex);
                            boundsTemp = null;
                        }
                        PositionBounds bounds = boundsTemp;
                        LineCookie lc = file.getLookup().lookup(LineCookie.class);
                        Line startLine = lc.getLineSet().getCurrent(l.getRange().getStart().getLine());
                        String lineText = startLine.getText();
                        int highlightEnd = Math.min(lineText.length(), l.getRange().getEnd().getCharacter());
                        String annotatedLine = lineText.substring(0, l.getRange().getStart().getCharacter()) +
                                               "<strong>" + lineText.substring(l.getRange().getStart().getCharacter(), highlightEnd) + "</strong>" +
                                               lineText.substring(highlightEnd);
                        refactoringElements.add(query, new LSPRefactoringElementImpl(annotatedLine, file, bounds));
                    }
                }
                return null;
            } catch (InterruptedException | ExecutionException ex) {
                return new Problem(true, ex.getLocalizedMessage());
            }
        }

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
            }
            return null;
        }

    }

}
