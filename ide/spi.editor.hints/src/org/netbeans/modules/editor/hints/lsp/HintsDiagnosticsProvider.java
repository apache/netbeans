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
package org.netbeans.modules.editor.hints.lsp;

import java.util.ArrayList;
import java.util.List;
import javax.swing.text.Document;
import org.netbeans.api.editor.document.LineDocument;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.lsp.Diagnostic;
import org.netbeans.modules.editor.hints.AnnotationHolder;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.HintsController;
import org.netbeans.spi.lsp.ErrorProvider;
import org.openide.filesystems.FileObject;
import org.openide.text.PositionBounds;

/**
 * A simple implementation of {@link ErrorProvider} that converts errors + hints collected by
 * {@link HintsController} to LSP {@link Diagnostic}. The implementation <b>does not support</b> code actions yet.
 * <p>
 * As {@link ErrorProvider}s are registered in MIME Lookup, this implementation is enumerated after those
 * possibly registered for specific MIME types.
 * 
 * @author sdedic
 */
@MimeRegistration(mimeType = "", service = ErrorProvider.class)
public class HintsDiagnosticsProvider implements ErrorProvider {
    public HintsDiagnosticsProvider() {
    }
    
    @Override
    public List<? extends Diagnostic> computeErrors(Context context) {
        FileObject file = context.file();
        AnnotationHolder ah = AnnotationHolder.getInstance(file);
        if (ah == null) {
            return null;
        }
        Document doc = ah.getDocument();
        if (!(doc instanceof LineDocument)) {
            return null;
        }
        int reportOffset = context.getOffset();
        List<Diagnostic>  result = new ArrayList<>();
        for (ErrorDescription d : ah.getErrors()) {
            PositionBounds range = d.getRange();
            
            if (reportOffset > 0 && range.getBegin().getOffset() > reportOffset || range.getEnd().getOffset() <= reportOffset) {
                continue;
            }
            
            Diagnostic.Builder b = Diagnostic.Builder.create(range.getBegin()::getOffset, range.getEnd()::getOffset, d.getDescription());
            b.setCode(d.getId());
            Diagnostic.Severity s;
            
            switch (d.getSeverity()) {
                case ERROR:
                case VERIFIER:
                    if (context.errorKind() != ErrorProvider.Kind.ERRORS) {
                        continue;
                    }
                    s = Diagnostic.Severity.Error;
                    break;
                case WARNING:
                    if (context.errorKind() != ErrorProvider.Kind.ERRORS) {
                        continue;
                    }
                    s = Diagnostic.Severity.Warning;
                    break;
                case HINT:
                    if (context.errorKind() != ErrorProvider.Kind.HINTS) {
                        continue;
                    }
                    s = Diagnostic.Severity.Hint;
                    break;
                default:
                    // should not happen
                    s = Diagnostic.Severity.Information;
                    break;
            }
            
            result.add(b.build());
        }
        return result;
    }
}
