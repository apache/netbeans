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
package org.netbeans.modules.lsp.client.spi;

import org.openide.filesystems.FileObject;

/**
 * LSP uses languageId as a carrier to inform what language/type of file is
 * being edited. Even a single language server can provide support for multiple
 * languages. One such example is the typescript language server, which defaults
 * to typescript, but also supports the typescript variant that embeds react
 * templates (TSX).
 *
 * <p>Language servers can provide a language id mapper to allow customization
 * of the resolving process.</p>
 *
 * @since 1.33.0
 */
public interface LanguageIdResolver {
    /**
     * Resolve the language id for the given file object.
     *
     * @param fileObject target to resolve the langeuge id for
     * @return the determined language id or {@code null} if that can't be found.
     */
    public String resolveLanguageId(FileObject fileObject);
}
