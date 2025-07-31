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
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.modules.lsp.client.LSPBindings;
import org.netbeans.modules.lsp.client.Utils;
import org.netbeans.spi.lsp.StructureProvider;
import org.netbeans.spi.navigator.NavigatorPanel;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service = NavigatorPanel.DynamicRegistration.class)
public final class LspNavigatorPanelDynamicRegistration implements NavigatorPanel.DynamicRegistration {

    @Override
    public Collection<? extends NavigatorPanel> panelsFor(URI uri) {
        try {
            FileObject file = URLMapper.findFileObject(uri.toURL());
            if (file != null) {
                List<LSPBindings> bindings = LSPBindings.getBindings(file);
                if (!bindings.isEmpty()) {
                    return bindings.stream()
                                   .filter(server -> Utils.isEnabled(Utils.getCapabilities(server).getDocumentSymbolProvider()))
                                   .map(NavigatorPanelImpl::new)
                                   .toList();
                } else {
                    String mime = file.getMIMEType();
                    for (StructureProvider sp : MimeLookup.getLookup(mime).lookupAll(StructureProvider.class)) {
                        if (sp != null) {
                            FileObject navigators = FileUtil.getConfigFile("Navigator/Panels");
                            if (navigators != null) {
                                FileObject mimeFolder = navigators.getFileObject(mime);
                                if (mimeFolder != null && mimeFolder.getChildren().length > 0) {
                                    // don't use StructureProvider when there is normal
                                    // NavigatorPanel registration for the MIME type
                                    return Collections.emptyList();
                                }
                            }
                            return Collections.singletonList(LspStructureNavigatorPanel.INSTANCE);
                        }
                    }
                }
            }
        } catch (MalformedURLException ex) {
            //ignore
        }
        return Collections.emptyList();
    }

}
