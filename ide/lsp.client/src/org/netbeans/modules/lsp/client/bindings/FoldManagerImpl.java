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
package org.netbeans.modules.lsp.client.bindings;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.eclipse.lsp4j.FoldingRange;
import org.eclipse.lsp4j.FoldingRangeRequestParams;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.netbeans.api.editor.fold.Fold;
import org.netbeans.api.editor.fold.FoldType;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.modules.lsp.client.LSPBindings;
import org.netbeans.modules.lsp.client.LSPBindings.BackgroundTask;
import org.netbeans.modules.lsp.client.Utils;
import org.netbeans.spi.editor.fold.FoldHierarchyTransaction;
import org.netbeans.spi.editor.fold.FoldInfo;
import org.netbeans.spi.editor.fold.FoldManager;
import org.netbeans.spi.editor.fold.FoldManagerFactory;
import org.netbeans.spi.editor.fold.FoldOperation;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;

/**
 *
 * @author lahvac
 */
public class FoldManagerImpl implements FoldManager, BackgroundTask {

    private FoldOperation operation;
    private FileObject file;

    @Override
    public void init(FoldOperation operation) {
        this.operation = operation;
    }

    @Override
    public void initFolds(FoldHierarchyTransaction transaction) {
        Document doc = operation.getHierarchy().getComponent().getDocument();
        file = NbEditorUtilities.getFileObject(doc);

        if (file == null) {
            return ;
        }

        LSPBindings.addBackgroundTask(file, this);
    }

    @Override
    public void insertUpdate(DocumentEvent evt, FoldHierarchyTransaction transaction) {
    }

    @Override
    public void removeUpdate(DocumentEvent evt, FoldHierarchyTransaction transaction) {
    }

    @Override
    public void changedUpdate(DocumentEvent evt, FoldHierarchyTransaction transaction) {
    }

    @Override
    public void removeEmptyNotify(Fold epmtyFold) {
    }

    @Override
    public void removeDamagedNotify(Fold damagedFold) {
    }

    @Override
    public void expandNotify(Fold expandedFold) {
    }

    @Override
    public void release() {
        if (file != null) {
            LSPBindings.removeBackgroundTask(file, this);
            file = null;
        }
    }

    @Override
    public void run(LSPBindings bindings, FileObject file) {
        EditorCookie ec = Utils.lookupForFile(file, EditorCookie.class);
        Document doc = ec != null ? ec.getDocument() : null;
        if (doc == null) {
            return;
        }

        List<FoldingRange> ranges = computeRanges(bindings, file);
        List<FoldInfo> infos = computeInfos(doc, ranges);
        SwingUtilities.invokeLater(() -> {
            doc.render(() -> {
                operation.getHierarchy().render(() -> {
                    try {
                        operation.update(infos, null, null);
                    } catch (BadLocationException ex) {
                        LOG.log(Level.FINE, null, ex);
                    }
                });
            });
        });
    }
    
    static List<FoldInfo> computeInfos(Document doc, List<FoldingRange> ranges) {
        Set<FoldingRangeInfo2> foldingRangesSeen = new HashSet<>();
        List<FoldInfo> infos = new ArrayList<>();
        if (ranges != null) {
            for (FoldingRange r : ranges) {
                int start;
                if(r.getStartCharacter() == null) {
                    int endCharacter = Utils.getEndCharacter(doc, r.getStartLine());
                    start = Utils.getOffset(doc, new Position(r.getStartLine(), endCharacter));
                } else {
                    start = Utils.getOffset(doc, new Position(r.getStartLine(), r.getStartCharacter()));
                }
                int end;
                if (r.getEndCharacter() == null) {
                    int endCharacter = Utils.getEndCharacter(doc, r.getEndLine());
                    end = Utils.getOffset(doc, new Position(r.getEndLine(), endCharacter));
                } else {
                    end = Utils.getOffset(doc, new Position(r.getEndLine(), r.getEndCharacter()));
                }
                // Map the fold range type to netbeans as far as possible
                FoldType foldType;
                if ("comment".equals(r.getKind())) {
                    foldType = FoldType.COMMENT;
                } else if ("imports".equals(r.getKind())) {
                    foldType = FoldType.IMPORT;
                } else {
                    foldType = FoldType.CODE_BLOCK;
                }
                FoldingRangeInfo2 fri2 = new FoldingRangeInfo2(start, end, foldType);
                if (!foldingRangesSeen.contains(fri2)) {
                    infos.add(FoldInfo.range(start, end, foldType));
                    foldingRangesSeen.add(fri2);
                }
            }
        }
        return infos;
    }
   
    static List<FoldingRange> computeRanges(LSPBindings bindings, FileObject file) {
        if (bindings.getInitResult() != null &&
            bindings.getInitResult().getCapabilities() != null &&
            bindings.getInitResult().getCapabilities().getFoldingRangeProvider() != null) { //XXX
            try {
                return bindings.getTextDocumentService().foldingRange(new FoldingRangeRequestParams(new TextDocumentIdentifier(Utils.toURI(file)))).get();
            } catch (InterruptedException | ExecutionException ex) {
                LOG.log(Level.FINE, null, ex);
            }
        }
        return Collections.emptyList();
    }

    private static final Logger LOG = Logger.getLogger(FoldManagerImpl.class.getName());

    @MimeRegistration(mimeType="", service=FoldManagerFactory.class, position = 1950)
    public static final class FactoryImpl implements FoldManagerFactory {

        @Override
        public FoldManager createFoldManager() {
            return new FoldManagerImpl();
        }

    }

    static class FoldingRangeInfo2 {
        private int start;
        private int end;
        private FoldType type;

        public FoldingRangeInfo2(int start, int end, FoldType type) {
            this.start = start;
            this.end = end;
            this.type = type;
        }

        public int getStart() {
            return start;
        }

        public int getEnd() {
            return end;
        }

        public FoldType getType() {
            return type;
        }

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 23 * hash + this.start;
            hash = 23 * hash + this.end;
            hash = 23 * hash + Objects.hashCode(this.type);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final FoldingRangeInfo2 other = (FoldingRangeInfo2) obj;
            if (this.start != other.start) {
                return false;
            }
            if (this.end != other.end) {
                return false;
            }
            if (!Objects.equals(this.type, other.type)) {
                return false;
            }
            return true;
        }
    }
}
