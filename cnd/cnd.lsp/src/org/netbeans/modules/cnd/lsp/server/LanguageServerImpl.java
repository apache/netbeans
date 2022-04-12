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
package org.netbeans.modules.cnd.lsp.server;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.editor.mimelookup.MimeRegistrations;
import org.netbeans.api.project.Project;
import org.netbeans.modules.cnd.makeproject.api.MakeProject;
import org.netbeans.modules.cnd.utils.MIMENames;
import org.netbeans.modules.lsp.client.spi.LanguageServerProvider;
import org.netbeans.modules.lsp.client.spi.MultiMimeLanguageServerProvider;
import org.netbeans.modules.lsp.client.spi.ServerRestarter;
import org.openide.util.Lookup;
import org.openide.util.Pair;

/**
 * The MultiMimeLanguageServerProvider that adds LSP capabilities to
 * C_MIME_TYPE, CPLUSPLUS_MIME_TYPE and HEADER_MIME_TYPE based files in
 * Apache NetBeans.
 */
@MimeRegistrations({
    @MimeRegistration(service = LanguageServerProvider.class,
            mimeType = MIMENames.C_MIME_TYPE),
    @MimeRegistration(service = LanguageServerProvider.class,
            mimeType = MIMENames.CPLUSPLUS_MIME_TYPE),
    @MimeRegistration(service = LanguageServerProvider.class,
            mimeType = MIMENames.HEADER_MIME_TYPE)
})
public class LanguageServerImpl implements MultiMimeLanguageServerProvider {

    private static final Logger LOG = Logger.getLogger(LanguageServerImpl.class.getName());

    private static final Map<Project, Pair<Process, LanguageServerDescription>> prj2Server = new HashMap<>();

    private ServerRestarter serverRestarter;

    @Override
    public LanguageServerDescription startServer(Lookup lookup) {

        MakeProject project = lookup.lookup(MakeProject.class);
        if (project == null) {
            return null;
        }

        this.serverRestarter = lookup.lookup(ServerRestarter.class);
        if (serverRestarter == null) {
            LOG.log(Level.INFO, "Cannot restart LSP server because Project {0} has no SererRestarter", project.getProjectDirectory().toString());
            return null;
        }

        LOG.log(Level.INFO, "MakeProject@{0}", System.identityHashCode(project));

        LSPServerSupport projectLSPSupport = project.getLookup().lookup(LSPServerSupport.class);
        if (projectLSPSupport == null) {
            LOG.log(Level.INFO, "Cannot start LSP server because Project {0} has no LSPServerSupport", project.getProjectDirectory().toString());
            return null;
        }

        ClangdProcess clangd = ClangdProcess.getInstance();

        if (clangd.getState() == LSPServerState.RUNNING) {
            return clangd.getLanguageServerDescription();
        } else {
            try {
                clangd.start(serverRestarter);
            } catch (IOException ex) {
                LOG.log(Level.SEVERE, "Failed to start clangd for project {0}", project.getProjectDirectory().toString());
                return null;
            }
        }
        return clangd.getLanguageServerDescription();
    }

    private static final Set<String> MIME_TYPES = new HashSet<>(Arrays.asList(
        MIMENames.C_MIME_TYPE,
        MIMENames.CPLUSPLUS_MIME_TYPE,
        MIMENames.HEADER_MIME_TYPE
    ));

    @Override
    public Set<String> getMimeTypes() {
        return MIME_TYPES;
    }

}
