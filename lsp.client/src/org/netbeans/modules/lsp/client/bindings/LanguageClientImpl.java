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

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.swing.text.Document;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.eclipse.lsp4j.MessageActionItem;
import org.eclipse.lsp4j.MessageParams;
import org.eclipse.lsp4j.PublishDiagnosticsParams;
import org.eclipse.lsp4j.ShowMessageRequestParams;
import org.eclipse.lsp4j.services.LanguageClient;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.ErrorDescriptionFactory;
import org.netbeans.spi.editor.hints.HintsController;
import org.netbeans.spi.editor.hints.Severity;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;
import org.openide.util.Exceptions;

/**
 *
 * @author lahvac
 */
public class LanguageClientImpl implements LanguageClient {

    private static final Logger LOG = Logger.getLogger(LanguageClientImpl.class.getName());

    @Override
    public void telemetryEvent(Object arg0) {
        System.err.println("telemetry: " + arg0);
    }

    @Override
    public void publishDiagnostics(PublishDiagnosticsParams arg0) {
        try {
            FileObject file = URLMapper.findFileObject(new URI(arg0.getUri()).toURL());
            EditorCookie ec = file.getLookup().lookup(EditorCookie.class);
            Document doc = ec != null ? ec.getDocument() : null;
            if (doc == null)
                return ; //ignore...
            List<ErrorDescription> diags = arg0.getDiagnostics().stream().map(d -> 
                    ErrorDescriptionFactory.createErrorDescription(severityMap.get(d.getSeverity()), d.getMessage(), file, Utils.getOffset(doc, d.getRange().getStart()), Utils.getOffset(doc, d.getRange().getEnd()))
            ).collect(Collectors.toList());
            HintsController.setErrors(doc, LanguageClientImpl.class.getName(), diags);
        } catch (URISyntaxException | MalformedURLException ex) {
            LOG.log(Level.FINE, null, ex);
        }
        System.err.println("arg0: " + arg0.getDiagnostics().size());
        System.err.println("publishDiagnostics: " + arg0);
    }

    private static final Map<DiagnosticSeverity, Severity> severityMap = new EnumMap<>(DiagnosticSeverity.class);
    
    static {
        severityMap.put(DiagnosticSeverity.Error, Severity.ERROR);
        severityMap.put(DiagnosticSeverity.Hint, Severity.HINT);
        severityMap.put(DiagnosticSeverity.Information, Severity.HINT);
        severityMap.put(DiagnosticSeverity.Warning, Severity.HINT);
    }

    @Override
    public void showMessage(MessageParams arg0) {
        System.err.println("showMessage: " + arg0);
    }

    @Override
    public CompletableFuture<MessageActionItem> showMessageRequest(ShowMessageRequestParams arg0) {
        System.err.println("showMessageRequest");
        return null; //???
    }

    @Override
    public void logMessage(MessageParams arg0) {
        System.err.println("logMessage: " + arg0);
    }

}
