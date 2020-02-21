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

package org.netbeans.modules.cnd.diagnostics.clank.ui.views;

import javax.swing.text.Document;
import org.clang.tools.services.ClankDiagnosticInfo;
import org.netbeans.spi.editor.highlighting.support.OffsetsBag;
import org.openide.ErrorManager;
import org.openide.text.Annotation;
import org.openide.text.Line;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;


/**
 * Debugger Annotation class.
 *
 */
public class DiagnosticsAnnotation extends Annotation implements Lookup.Provider {

    /** Annotation type constant. */
    static final String DIAGNOSTIC_WARNING_ANNOTATION_TYPE = "PinnedDiagnosticWarning";//NOI18N
    static final String DIAGNOSTIC_ERROR_ANNOTATION_TYPE = "PinnedDiagnosticError";//NOI18N

    private final Line        line;
    private final String      type;
    private ClankDiagnosticInfo             diagnosticInfo;

    DiagnosticsAnnotation (String type, Line line) {
        this.type = type;
        this.line = line;
        attach (line);
    }
    
    DiagnosticsAnnotation (String type, Line.Part linePart) {
        this.type = type;
        this.line = linePart.getLine();
        attach (linePart);
    }
    
    @Override
    public String getAnnotationType () {
        return type;
    }
    
    void setDiagnostic(ClankDiagnosticInfo diagnosticInfo) {
        this.diagnosticInfo = diagnosticInfo;
    }
    
    Line getLine () {
        return line;
    }
    
    @Override
    public String getShortDescription () {
        if (DIAGNOSTIC_WARNING_ANNOTATION_TYPE.equals(type) || DIAGNOSTIC_ERROR_ANNOTATION_TYPE.equals(type) ) {
            return diagnosticInfo.getMessage();
        }
        ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, new IllegalStateException("Unknown annotation type '"+type+"'."));//NOI18N
        return null;
    }
    
    static synchronized OffsetsBag getHighlightsBag(Document doc) {
        OffsetsBag bag = (OffsetsBag) doc.getProperty(DiagnosticsAnnotation.class);
        if (bag == null) {
            doc.putProperty(DiagnosticsAnnotation.class, bag = new OffsetsBag(doc, true));
        }
        return bag;
    }
    
    @Override
    public Lookup getLookup() {
        if (diagnosticInfo == null) {
            return Lookup.EMPTY;
        } else {
            return Lookups.singleton(diagnosticInfo);
        }
    }
    
}
