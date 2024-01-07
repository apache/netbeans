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
import org.netbeans.modules.micronaut.expression.MicronautExpressionLanguageUtilities;
import org.netbeans.spi.lsp.HoverProvider;

/**
 *
 * @author Dusan Balek
 */
@MimeRegistration(mimeType = "text/x-java", service = HoverProvider.class)
public class MicronautExpressionLanguageHoverProvider implements HoverProvider {

    @Override
    public CompletableFuture<String> getHoverContent(Document doc, int offset) {
        return CompletableFuture.completedFuture(MicronautExpressionLanguageUtilities.resolve(doc, offset, (info, element) -> {
            return MicronautExpressionLanguageUtilities.getJavadocText(info, element, false, 1);
        }, (property, source) -> {
            return new MicronautConfigDocumentation(property).getText();
        }));
    }
}
