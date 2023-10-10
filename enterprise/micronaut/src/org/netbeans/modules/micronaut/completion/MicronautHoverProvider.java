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
package org.netbeans.modules.micronaut.completion;

import java.util.concurrent.CompletableFuture;
import javax.swing.text.Document;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.modules.micronaut.MicronautConfigUtilities;
import org.netbeans.spi.lsp.HoverProvider;
import org.springframework.boot.configurationmetadata.ConfigurationMetadataProperty;

/**
 *
 * @author Dusan Balek
 */
public class MicronautHoverProvider implements HoverProvider {

    @MimeRegistration(mimeType = MicronautConfigUtilities.YAML_MIME, service = HoverProvider.class)
    public static MicronautHoverProvider createYamlProvider() {
        return new MicronautHoverProvider();
    }

    @MimeRegistration(mimeType = MicronautConfigUtilities.PROPERTIES_MIME, service = HoverProvider.class)
    public static MicronautHoverProvider createPropertiesProvider() {
        return new MicronautHoverProvider();
    }

    @Override
    public CompletableFuture<String> getHoverContent(Document doc, int offset) {
        ConfigurationMetadataProperty property = MicronautConfigUtilities.resolveProperty(doc, offset, null, null);
        return CompletableFuture.completedFuture(property != null ? new MicronautConfigDocumentation(property).getText() : null);
    }
}
