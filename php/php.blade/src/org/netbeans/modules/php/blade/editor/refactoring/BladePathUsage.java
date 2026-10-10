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
package org.netbeans.modules.php.blade.editor.refactoring;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.text.BadLocationException;
import javax.swing.text.Position;
import javax.swing.text.StyledDocument;
import org.netbeans.api.editor.document.LineDocument;
import org.netbeans.api.editor.document.LineDocumentUtils;
import org.netbeans.modules.php.blade.editor.BladeLanguage;
import org.netbeans.modules.php.blade.editor.indexing.BladeIndex.IndexedOffsetReference;
import org.netbeans.modules.php.blade.editor.indexing.QueryUtils;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.api.WhereUsedQuery;
import org.netbeans.modules.refactoring.spi.RefactoringElementsBag;
import org.netbeans.modules.refactoring.spi.RefactoringPlugin;
import org.netbeans.modules.refactoring.spi.RefactoringPluginFactory;
import org.netbeans.modules.refactoring.spi.SimpleRefactoringElementImplementation;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.text.CloneableEditorSupport;
import org.openide.text.PositionBounds;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author bogdan
 */
public class BladePathUsage {

    private static final class WhereUsedRefactoringPlugin implements RefactoringPlugin {

        private final WhereUsedQuery query;
        private final BladePathInfo bladeFileReference;
        private final AtomicBoolean cancel = new AtomicBoolean();

        public WhereUsedRefactoringPlugin(WhereUsedQuery query, BladePathInfo bladeFileReference) {
            this.query = query;
            this.bladeFileReference = bladeFileReference;
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
        }

        @Override
        public Problem prepare(RefactoringElementsBag refactoringElements) {
            try {
                FileObject sourceFO = this.bladeFileReference.getSourceFile();

                if (!BladeLanguage.MIME_TYPE.equals(sourceFO.getMIMEType())) {
                    return null;
                }

                List<IndexedOffsetReference> references = QueryUtils.getIncludePathReferences(this.bladeFileReference.getBladePath(), sourceFO);

                for (IndexedOffsetReference reference : references) {
                    if (cancel.get()) {
                        throw new CancellationException();
                    }

                    PositionBounds bounds;
                    FileObject fo = reference.getOriginFile();
                    int start = reference.getStart();
                    int end = start + reference.getReference().length();
                    try {
                        CloneableEditorSupport es = fo.getLookup().lookup(CloneableEditorSupport.class);
                        EditorCookie ec = fo.getLookup().lookup(EditorCookie.class);
                        StyledDocument doc = ec.openDocument();
                        LineDocument ldoc = (LineDocument) doc;

                        int rowStart = LineDocumentUtils.getLineStart(ldoc, start);
                        int rowEnd = LineDocumentUtils.getLineEnd(ldoc, end);

                        bounds = new PositionBounds(
                                es.createPositionRef(start, Position.Bias.Forward),
                                es.createPositionRef(end, Position.Bias.Forward)
                        );

                        String lineText = doc.getText(rowStart, rowEnd - rowStart);
                        //offset quote symbols
                        String annotatedLine
                                = lineText.substring(0, start - rowStart + 1)
                                + "<strong>" //NOI18N
                                + lineText.substring(start - rowStart + 1, end - rowStart + 2)
                                + "</strong>" //NOI18N
                                + lineText.substring(end - rowStart + 2);
                        refactoringElements.add(query, new BladeRefactoringElementImpl(annotatedLine, fo, bounds));
                    } catch (BadLocationException | IOException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
                
                return null;
            } catch (CancellationException ex) {
                return new Problem(false, "Cancelled");
            }
        }

    }

    @ServiceProvider(service = RefactoringPluginFactory.class)
    public static class FactoryImpl implements RefactoringPluginFactory {

        @Override
        public RefactoringPlugin createInstance(AbstractRefactoring refactoring) {
            if (refactoring instanceof WhereUsedQuery) {
                WhereUsedQuery q = (WhereUsedQuery) refactoring;
                BladePathInfo symbolInformation = q.getRefactoringSource().lookup(BladePathInfo.class);
                if (symbolInformation != null) {
                    return new WhereUsedRefactoringPlugin(q, symbolInformation);
                }
            }
            return null;
        }

    }

    public static class BladeRefactoringElementImpl extends SimpleRefactoringElementImplementation {

        private final String annotatedLine;
        private final FileObject file;
        private final PositionBounds bounds;

        public BladeRefactoringElementImpl(String annotatedLine, FileObject file, PositionBounds bounds) {
            this.annotatedLine = annotatedLine;
            this.file = file;
            this.bounds = bounds;
        }

        @Override
        public String getText() {
            return "Element usage"; //NOI18N
        }

        @Override
        public String getDisplayText() {
            return annotatedLine;
        }

        @Override
        public void performChange() {
            // Currently the BladeRefactoringElementImpl is only used for the
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
}
