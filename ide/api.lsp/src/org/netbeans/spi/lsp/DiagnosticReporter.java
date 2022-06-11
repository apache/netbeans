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
package org.netbeans.spi.lsp;

import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.netbeans.api.lsp.Diagnostic.ReporterControl;

/**
 * Allows to control diagnostics push to the LSP client on behalf of the
 * LSP server background processes. The {@code pushDiagnostics} is a server-initiated
 * message. Traditionally the diagnostics are collected and pushed whenever a
 * file is operated on, but there may be different events (for example a background
 * analytical process finishes) that may change the diagnostic information for a file.
 * <p>
 * 
 * 
 * @author sdedic
 */
public interface DiagnosticReporter {
    /**
     * Returns a Control object appropriate for the context and the file. The returned 
     * object can be used to fire changes to LSP client(s). It is important to call
     * the method while the context is in effect, i.e. during the client's request. May
     * return {@code null} if no suitable LSP can be found.
     * 
     * @param context the Optional. Context used to identify the LSP client. If {@code null},
     * the default Lookup will be used.
     * @param file Optional. The file or folder whose diagnostic will be reported.
     * @return the control object.
     */
    @CheckForNull
    public ReporterControl findDiagnosticControl(@NullAllowed Lookup context, @NullAllowed FileObject file);
}
