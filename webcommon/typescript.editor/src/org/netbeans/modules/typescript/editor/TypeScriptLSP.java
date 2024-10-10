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
package org.netbeans.modules.typescript.editor;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.options.OptionsDisplayer;
import org.netbeans.modules.javascript.nodejs.api.NodeJsSupport;
import org.netbeans.modules.lsp.client.spi.LanguageServerProvider;
import org.openide.awt.NotificationDisplayer;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle.Messages;

import static org.netbeans.modules.typescript.editor.TypeScriptEditorKit.TYPESCRIPT_ICON;
import static org.netbeans.modules.typescript.editor.TypeScriptEditorKit.TYPESCRIPT_MIME_TYPE;

/**
 *
 * @author jlahoda
 */
@MimeRegistration(mimeType=TYPESCRIPT_MIME_TYPE, service=LanguageServerProvider.class)
public class TypeScriptLSP implements LanguageServerProvider {

    private static final Logger LOG = Logger.getLogger(TypeScriptLSP.class.getName());

    private final AtomicBoolean missingNodeWarningIssued = new AtomicBoolean();

    @Override
    @Messages({"WARN_NoNode=node.js not found, TypeScript support disabled.",
               "DESC_NoNode=Please specify node.js location in the Tools/Options, and restart the IDE."
    })
    public LanguageServerDescription startServer(Lookup lookup) {
        String node = NodeJsSupport.getInstance().getNode(null);
        if (node == null || node.isEmpty()) {
            if (!missingNodeWarningIssued.getAndSet(true)) {
                NotificationDisplayer.getDefault().notify(Bundle.WARN_NoNode(), ImageUtilities.loadImageIcon(TYPESCRIPT_ICON, true), Bundle.DESC_NoNode(), evt -> {
                    OptionsDisplayer.getDefault().open("Html5/NodeJs");
                });
            }
            return null;
        }
        File server = InstalledFileLocator.getDefault().locate("typescript-lsp/node_modules/typescript-language-server/lib/cli.mjs", "org.netbeans.modules.typescript.editor", false);
        try {
            Process p = new ProcessBuilder(new String[] {node, server.getAbsolutePath(), "--stdio"}).directory(server.getParentFile().getParentFile()).redirectError(ProcessBuilder.Redirect.INHERIT).start();
            return LanguageServerDescription.create(p.getInputStream(), p.getOutputStream(), p);
        } catch (IOException ex) {
            LOG.log(Level.FINE, null, ex);
            return null;
        }
    }

}
