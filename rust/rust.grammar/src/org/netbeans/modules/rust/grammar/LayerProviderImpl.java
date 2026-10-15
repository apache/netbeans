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
package org.netbeans.modules.rust.grammar;

import java.net.URL;
import java.util.Collection;
import java.util.logging.Logger;
import org.netbeans.modules.rust.options.api.RustAnalyzerOptions;
import org.openide.filesystems.Repository.LayerProvider;
import org.openide.util.lookup.ServiceProvider;

/**
 * The rust.grammer module can provide services directly or using rust-analyzer
 * (the rust lsp). If the user configured rust-analyzer, that should get
 * priority as it also can provide code completion and improved structure
 * scanning.
 *
 * For this to work, the normal NetBeans behavior to generate layer entries from
 * annotations (MimeRegistration of LSP, LanguageRegistration) has to be
 * customized as the two options can't coexist completely.
 *
 * Therefore the layer entries from org.netbeans.modules.rust.grammar.RustLanguageConfig
 * and org.netbeans.modules.rust.grammar.lsp.RustLSP are removed from the
 * generated-layer.xml (build/classes/META-INF/generated-layer.xml), the
 * annotations are commented out and the entries from the generated layer are
 * checked whether they were created by one of the mentioned classes (see XML
 * comments for reference) and are distributed into:
 *
 * - layer.xml holds everything that applies to both LSP and non-LSP mode
 * - lsp-layer.xml holds everything specific to the LSP
 * - non-lsp-layer.xml hold everything specific to the non-LSP run
 *
 * `layer.xml` is loaded by the module system unconditionally. The two variants
 * `lsp-layer.xml` and `non-lsp-layer.xml` are loaded using this class.
 */
@ServiceProvider(service = LayerProvider.class)
public class LayerProviderImpl extends LayerProvider {

    private static final Logger LOG = Logger.getLogger(LayerProviderImpl.class.getName());

    @Override
    protected void registerLayers(Collection<? super URL> context) {
        URL layerUrl;
        if (RustAnalyzerOptions.getRustAnalyzerLocation(false, false) != null) {
            layerUrl = LayerProviderImpl.class.getResource("lsp-layer.xml");
        } else {
            layerUrl = LayerProviderImpl.class.getResource("non-lsp-layer.xml");
        }
        context.add(layerUrl);
    }
}
