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
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.BadLocationException;
import javax.swing.text.StyledDocument;
import org.eclipse.lsp4j.DocumentFormattingParams;
import org.eclipse.lsp4j.DocumentRangeFormattingParams;
import org.eclipse.lsp4j.FormattingOptions;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.eclipse.lsp4j.TextEdit;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.modules.editor.indent.api.IndentUtils;
import org.netbeans.modules.editor.indent.spi.Context;
import org.netbeans.modules.editor.indent.spi.ExtraLock;
import org.netbeans.modules.editor.indent.spi.ReformatTask;
import org.netbeans.modules.lsp.client.LSPBindings;
import org.netbeans.modules.lsp.client.Utils;
import org.openide.filesystems.FileObject;
import org.openide.text.NbDocument;

public class Formatter implements ReformatTask {

    private static final Logger LOG = Logger.getLogger(Formatter.class.getName());

    public static class Factory implements ReformatTask.Factory {

        @Override
        public ReformatTask createTask(Context context) {
            FileObject file = NbEditorUtilities.getFileObject(context.document());
            if (file != null) {
                List<LSPBindings> servers = LSPBindings.getBindings(file);
                for (LSPBindings server : servers) {
                    boolean rangeFormatting = Utils.isEnabled(server.getInitResult().getCapabilities().getDocumentRangeFormattingProvider());
                    if (rangeFormatting) {
                        return new Formatter(context, server, ReformatKind.RANGE);
                    }
                    boolean documentFormatting = Utils.isEnabled(server.getInitResult().getCapabilities().getDocumentFormattingProvider());
                    if (documentFormatting) {
                        return new Formatter(context, server, ReformatKind.DOCUMENT);
                    }
                }
            }
            return null;
        }

    }

    private final Context ctx;
    private final LSPBindings server;
    private final ReformatKind kind;

    public Formatter(Context ctx, LSPBindings server, ReformatKind kind) {
        this.ctx = ctx;
        this.server = server;
        this.kind = kind;
    }

    @Override
    public void reformat() throws BadLocationException {
        FileObject file = NbEditorUtilities.getFileObject(ctx.document());
        if (file != null) {
            switch (kind) {
                case RANGE -> rangeFormat(file, server);
                case DOCUMENT -> documentFormat(file, server);
                default -> throw new IllegalStateException("Unknown kind: " + kind);
            }
        }
    }

    private void rangeFormat(FileObject fo, LSPBindings bindings) throws BadLocationException {
        DocumentRangeFormattingParams drfp = new DocumentRangeFormattingParams();
        drfp.setTextDocument(new TextDocumentIdentifier(Utils.toURI(fo)));
        drfp.setOptions(new FormattingOptions(
            IndentUtils.indentLevelSize(ctx.document()),
            IndentUtils.isExpandTabs(ctx.document())));
        drfp.setRange(new Range(
            Utils.createPosition(ctx.document(), ctx.startOffset()),
            Utils.createPosition(ctx.document(), ctx.endOffset())));
        List<TextEdit> edits = new ArrayList<>();
        try {
            edits = new ArrayList<>(bindings.getTextDocumentService().rangeFormatting(drfp).get());
        } catch (InterruptedException | ExecutionException ex) {
            LOG.log(Level.INFO,
                String.format("LSP document rangeFormat failed for {0}", fo),
                ex);
        }

        applyTextEdits(edits);
    }

    private void documentFormat(FileObject fo, LSPBindings bindings) throws BadLocationException {
        DocumentFormattingParams dfp = new DocumentFormattingParams();
        dfp.setTextDocument(new TextDocumentIdentifier(Utils.toURI(fo)));
        dfp.setOptions(new FormattingOptions(
            IndentUtils.indentLevelSize(ctx.document()),
            IndentUtils.isExpandTabs(ctx.document())));
        List<TextEdit> edits = new ArrayList<>();
        try {
            edits.addAll(bindings.getTextDocumentService().formatting(dfp).get());
        } catch (InterruptedException | ExecutionException ex) {
            LOG.log(Level.INFO,
                String.format("LSP document format failed for {0}", fo),
                ex);
        }

        applyTextEdits(edits);
    }

    private void applyTextEdits(List<TextEdit> edits) {
        if (ctx.document() instanceof StyledDocument) {
            NbDocument.runAtomic((StyledDocument) ctx.document(), () -> {
                Utils.applyEditsNoLock(ctx.document(), edits, ctx.startOffset(), ctx.endOffset());
            });
        } else {
            Utils.applyEditsNoLock(ctx.document(), edits, ctx.startOffset(), ctx.endOffset());
        }
    }

    @Override
    public ExtraLock reformatLock() {
        return null;
    }

    private enum ReformatKind {
        RANGE,
        DOCUMENT;
    }
}
