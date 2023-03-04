/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.spi.lsp;

import java.util.concurrent.CompletableFuture;
import javax.swing.text.Document;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.spi.editor.mimelookup.MimeLocation;

/**
 * Interface for resolving hover information at a given document position.
 * Implementations of the interface should be registered in MimeLookup.
 * <pre>
 *
 *  {@code @MimeRegistration(mimeType = "text/foo", service = HoverProvider.class)
 *   public class FooHoverProvider implements HoverProvider {
 *     ...
 *   }
 *  }
 * </pre>
 *
 * @author Dusan Balek
 * @since 1.2
 */
@MimeLocation(subfolderName = "HoverProviders")
public interface HoverProvider {

    /**
     * Resolves a hover information at the given document offset and returns its
     * content.
     *
     * @param doc document on which to operate.
     * @param offset offset within document
     * @return a HTML formatted content
     *
     * @since 1.2
     */
    @NonNull
    CompletableFuture<String> getHoverContent(@NonNull Document doc, int offset);
}
