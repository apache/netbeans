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
package org.netbeans.api.lsp;

import java.util.concurrent.CompletableFuture;
import javax.swing.text.Document;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.lib.editor.util.swing.DocumentUtilities;
import org.netbeans.spi.lsp.HoverProvider;

/**
 * Represents a hover information at a given text document position.
 *
 * @author Dusan Balek
 * @since 1.2
 */
public final class Hover {

    /**
     * Resolves a hover information at the given document offset and returns its
     * content. Example usage can be illustrated by:
     * {@snippet file="org/netbeans/api/lsp/HoverTest.java" region="testHoverContent"}
     *
     * @param doc document on which to operate.
     * @param offset offset within document
     * @return a HTML formatted content
     *
     * @since 1.2
     */
    @NonNull
    public static CompletableFuture<String> getContent(@NonNull Document doc, int offset) {
        MimePath mimePath = MimePath.parse(DocumentUtilities.getMimeType(doc));
        CompletableFuture<String>[] futures = MimeLookup.getLookup(mimePath).lookupAll(HoverProvider.class).stream()
                .map(provider -> provider.getHoverContent(doc, offset)).toArray(CompletableFuture[]::new);
        return CompletableFuture.allOf(futures).thenApply(value -> {
            StringBuilder sb = new StringBuilder();
            for (CompletableFuture<String> future : futures) {
                String content = future.getNow(null);
                if (content != null) {
                    if (sb.length() > 0) {
                        sb.append("<p>");
                    }
                    sb.append(content);
                }
            }
            return sb.length() > 0 ? sb.toString() : null;
        });
    }
}
