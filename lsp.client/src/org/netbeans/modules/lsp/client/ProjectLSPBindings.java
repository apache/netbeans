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
package org.netbeans.modules.lsp.client;

import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.eclipse.lsp4j.InitializeParams;
import org.eclipse.lsp4j.InitializeResult;
import org.eclipse.lsp4j.services.LanguageServer;
import org.eclipse.lsp4j.services.TextDocumentService;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.lsp.client.bindings.LanguageClientImpl;
import org.netbeans.modules.lsp.client.bindings.TextDocumentSyncServerCapabilityHandler;
import org.netbeans.modules.lsp.client.spi.LanguageServerProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author lahvac
 */
public class ProjectLSPBindings {

    private static final Map<Project, Map<String, ProjectLSPBindings>> project2MimeType2Server = new WeakHashMap<>();
    
    public static ProjectLSPBindings getBindings(FileObject file) {
        Project prj = FileOwnerQuery.getOwner(file);

        if (prj == null)
            return null;

        String mimeType = FileUtil.getMIMEType(file);
        
        ProjectLSPBindings bindings =
                project2MimeType2Server.computeIfAbsent(prj, p -> new HashMap<>())
                                       .computeIfAbsent(mimeType, mt -> {
                                           LanguageClientImpl lci = new LanguageClientImpl();
                                           for (LanguageServerProvider provider : MimeLookup.getLookup(mimeType).lookupAll(LanguageServerProvider.class)) {
                                               LanguageServer server = provider.startServer(prj, lci);

                                               if (server != null) {
                                                   try {
                                                       InitializeParams initParams = new InitializeParams();
                                                       initParams.setRootUri(prj.getProjectDirectory().toURI().toString()); //XXX: what if a different root is expected????
                                                       initParams.setProcessId(0);
                                                       InitializeResult result = server.initialize(initParams).get();
                                                       return new ProjectLSPBindings(server, result);
                                                   } catch (InterruptedException | ExecutionException ex) {
                                                       LOG.log(Level.FINE, null, ex);
                                                   }
                                               }
                                           }
                                           return new ProjectLSPBindings(null, null);
                                       });
        
        return bindings.server != null ? bindings : null;
    }
    private static final Logger LOG = Logger.getLogger(ProjectLSPBindings.class.getName());

    private final LanguageServer server;
    private final InitializeResult initResult;

    private ProjectLSPBindings(LanguageServer server, InitializeResult initResult) {
        this.server = server;
        this.initResult = initResult;
    }

    public TextDocumentService getTextDocumentService() {
        return server.getTextDocumentService();
    }

    public InitializeResult getInitResult() {
        //XXX: defenzive copy?
        return initResult;
    }
    
}
