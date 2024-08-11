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
import java.util.Collection;
import java.util.List;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;
import org.netbeans.api.lsp.Diagnostic;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.ErrorDescriptionFactory;
import org.netbeans.spi.editor.hints.HintsController;
import org.netbeans.spi.editor.hints.Severity;
import org.netbeans.spi.lsp.ErrorProvider;
import org.openide.filesystems.FileObject;
import org.openide.util.RequestProcessor;
import org.openide.util.WeakListeners;

class ErrorProviderBridge implements Runnable, DocumentListener {

    private final FileObject file;
    private final RequestProcessor.Task task;
    private final Collection<? extends ErrorProvider> errorProviders;
    private final DocumentListener listener;

    ErrorProviderBridge(Document doc, FileObject file, Collection<? extends ErrorProvider> errorProviders, RequestProcessor rp) {
        this.file = file;
        this.errorProviders = errorProviders;
        this.task = rp.create(this);
        this.listener = WeakListeners.create(DocumentListener.class, this, doc);
        doc.addDocumentListener(listener);
    }

    final void start() {
        task.schedule(0);
    }

    final void waitFinished() {
        task.waitFinished();
    }

    @Override
    public final void run() {
        for (ErrorProvider p : errorProviders) {
            computeHints(ErrorProvider.Kind.ERRORS, p);
            computeHints(ErrorProvider.Kind.HINTS, p);
        }
    }

    private void computeHints(final ErrorProvider.Kind type, ErrorProvider p) {
        final String prefix = p.hintsLayerNameFor(type);
        if (prefix == null) {
            return;
        }
        List<ErrorDescription> arr = new ArrayList<>();
        ErrorProvider.Context errorCtx = new ErrorProvider.Context(file, type);
        List<? extends Diagnostic> errors = p.computeErrors(errorCtx);
        if (errors != null) {
            for (Diagnostic e : errors) {
                final Severity s;
                switch(e.getSeverity()) {
                    case Error:
                        s = Severity.ERROR; break;
                    case Warning:
                        s = Severity.WARNING; break;
                    case Information:
                    case Hint:
                    default:
                        s = Severity.HINT; break;
                }
                ErrorDescription descr = ErrorDescriptionFactory.createErrorDescription(s,
                        e.getDescription(),
                        file,
                        e.getStartPosition().getOffset(),
                        e.getEndPosition().getOffset()
                );
                arr.add(descr);
            }
            applyHints(prefix, p, arr);
        }
    }

    protected void applyHints(final String prefix, ErrorProvider p, List<ErrorDescription> arr) {
        HintsController.setErrors(file, prefix + ":" + p.getClass().getName(), arr);
    }

    @Override
    public final void insertUpdate(DocumentEvent e) {
        start();
    }

    @Override
    public final void removeUpdate(DocumentEvent e) {
        start();
    }

    @Override
    public final void changedUpdate(DocumentEvent e) {
        start();
    }
}
