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
package org.netbeans.modules.gradle.editor.hints;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.netbeans.api.editor.document.EditorDocumentUtils;
import org.netbeans.api.editor.document.LineDocument;
import org.netbeans.api.editor.document.LineDocumentUtils;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.lsp.Diagnostic;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.gradle.api.GradleReport;
import org.netbeans.modules.gradle.editor.MimeTypes;
import org.netbeans.spi.lsp.ErrorProvider;
import org.openide.filesystems.FileObject;

/**
 * Bridges GradleReports to LSP API
 * @author sdedic
 */
@MimeRegistration(service = ErrorProvider.class, mimeType = MimeTypes.GRADLE_FILE)
public class LspErrorProvider implements ErrorProvider {
    @Override
    public List<? extends Diagnostic> computeErrors(Context context) {
        FileObject fo = context.file();
        if (fo == null || !fo.getMIMEType(MimeTypes.GRADLE_FILE).equals(MimeTypes.GRADLE_FILE)) {
            return Collections.emptyList();
        }
        
        Project p = FileOwnerQuery.getOwner(fo);
        if (p == null) {
            return Collections.emptyList();
        }
        GradleHintsProvider hintProv = p.getLookup().lookup(GradleHintsProvider.class);
        if (hintProv == null) {
            return Collections.emptyList();
        }
        Map<LineDocument, List<GradleReport>> documentReports = hintProv.openReportDocuments(true);
        if (hintProv == null) {
            return Collections.emptyList();
        }
        
        List<Diagnostic> diags = new ArrayList<>();
        for (Map.Entry<LineDocument, List<GradleReport>> it : documentReports.entrySet()) {
            LineDocument doc = it.getKey();

            // let's get document location from the linedoc
            FileObject f = EditorDocumentUtils.getFileObject(doc);
            int[] idx = new int[] { 1 };
            doc.render(() -> {
                for (GradleReport r : it.getValue()) {
                    int l = r.getLine();
                    // report non-locations at line 1 for now, later we might add some string matching to guess the correct line 
                    // if Gradle does not report it
                    // Gradle numbers lines from 1, our utilities work with 0-based line indexes.
                    if (l < 1) {
                        l = 1;
                    }
                    int start = LineDocumentUtils.getLineStartFromIndex(doc, l - 1);
                    // let it span the entire line, we don't know 
                    int end = LineDocumentUtils.getLineStartFromIndex(doc, l);
                    if (end < 0) {
                        end = doc.getLength() - 1;
                    }
                    int fEnd = end;
                    Diagnostic.Builder b = Diagnostic.Builder.create(() -> start, () -> fEnd, r.formatReportForHintOrProblem(false, null));
                    String id = "errors:" + (idx[0]++);
                    b.setCode(id);
                    b.setSeverity(Diagnostic.Severity.Error);
                    
                    diags.add(b.build());
                }
            });
        }
        
        return diags;
    }
}
