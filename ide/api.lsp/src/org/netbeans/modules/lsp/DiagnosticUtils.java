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
package org.netbeans.modules.lsp;

import java.util.Collection;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.lsp.Diagnostic;
import org.netbeans.spi.lsp.DiagnosticReporter;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;

/**
 *
 * @author sdedic
 */
public class DiagnosticUtils {
    private static final Diagnostic.ReporterControl DUMMY = new Diagnostic.ReporterControl() {
        @Override
        public void diagnosticChanged(Collection<FileObject> files, String mimeType) {
        }
    };
    
    public static Diagnostic.ReporterControl findReporterControl(@NullAllowed Lookup context, @NullAllowed FileObject file) {
        if (context == null) {
            context = Lookup.getDefault();
        }
        for (DiagnosticReporter rc : Lookup.getDefault().lookupAll(DiagnosticReporter.class)) {
            Diagnostic.ReporterControl ctrl = rc.findDiagnosticControl(context, file);
            if (ctrl != null) {
                return ctrl;
            }
        }
        return DUMMY;
    }
}
