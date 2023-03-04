/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.maven.hints.pom;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.api.editor.document.LineDocument;
import org.netbeans.api.editor.document.LineDocumentUtils;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.lsp.Diagnostic;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.Severity;
import org.netbeans.spi.lsp.ErrorProvider;
import org.openide.filesystems.FileObject;

/**
 * ErrorProvider that runs explicitly file and selection tasks and reports Diagnostic to LSP
 * interface. 
 * Note that this ErrorProvider is a workaround for Schedulers not working properly in headless/NBLS
 * mode; should be removed after this is fixed, and the *Tasks are run by Parsing API infrastructure.
 * 
 * @author sdedic
 */
@MimeRegistration(service = ErrorProvider.class, mimeType = "text/x-maven-pom+xml")
public class LspMavenErrorProvider implements ErrorProvider {
    
    static final class T extends UserTask {
        final Context context;
        List<Diagnostic> diagnostics = new ArrayList<>();
        
        public T(Context context) {
            this.context = context;
        }
        
        @Override
        public void run(ResultIterator resultIterator) throws Exception {
            if (!(resultIterator.getParserResult() instanceof MavenResult)) {
                return;
            }
            MavenResult result = ((MavenResult)resultIterator.getParserResult());
            FileObject fo = result.getPomFile();
            Project p = FileOwnerQuery.getOwner(fo);
            Document doc = result.getSnapshot().getSource().getDocument(false);
            
            if (fo == null || p == null || doc == null) {
                return;
            }

            List<ErrorDescription> errors;
            if (context.getOffset() > -1) {
                errors = MavenSelectionHintsTask.computeErrors(result, context.getOffset(), context.getOffset(), context.getOffset());
            } else {
                errors = new ArrayList<>();
            }
            errors.addAll(PomModelUtils.findHints(result.getProjectModel(), p));
            
            LineDocument lineDocument = doc != null ? LineDocumentUtils.as(doc, LineDocument.class) : null;
            int idx = 0;
            boolean wantsError = context.errorKind() == ErrorProvider.Kind.ERRORS;
            for (ErrorDescription error : errors) {
                boolean isE = error.getSeverity() == Severity.ERROR || error.getSeverity() == Severity.VERIFIER;
                if (isE != wantsError) {
                    continue;
                }
                diagnostics.add(error2Diagnostic(error, lineDocument, ++idx));
            }
        }
    }
    
    static private Diagnostic error2Diagnostic(ErrorDescription error, LineDocument lineDocument, int idx) {
        int s = error.getRange().getBegin().getOffset();
        int e = error.getRange().getEnd().getOffset();
        Diagnostic.Builder diagBuilder = Diagnostic.Builder.create(() -> {
            if (lineDocument != null) {
                try {
                    return LineDocumentUtils.getLineFirstNonWhitespace(lineDocument, s);
                } catch (BadLocationException ex) {
                    return s;
                }
            }
            return s;
        }, () -> {
            if (lineDocument != null) {
                try {
                    return LineDocumentUtils.getLineLastNonWhitespace(lineDocument, e);
                } catch (BadLocationException ex) {
                    return e;
                }
            }
            return e;
        }, error.getDescription());
        switch (error.getSeverity()) {
            case VERIFIER:
            case ERROR:
                diagBuilder.setSeverity(Diagnostic.Severity.Error);
                break;
            case WARNING:
                diagBuilder.setSeverity(Diagnostic.Severity.Warning);
                break;
            case HINT:
                diagBuilder.setSeverity(Diagnostic.Severity.Information);
                break;
        }
        String id = "errors:" + idx + "-" + error.getId();
        diagBuilder.setCode(id);
        return diagBuilder.build();
    }

    @Override
    public List<? extends Diagnostic> computeErrors(Context context) {
        try {
            FileObject f = context.file();
            T task = new T(context);
            ParserManager.parse(Collections.singleton(Source.create(f)), task);
            return task.diagnostics;
        } catch (ParseException ex) {
            // will be remoted through CompletableFuture to the client.
            throw new IllegalStateException("Parsing failed", ex);
        }
    }

}
