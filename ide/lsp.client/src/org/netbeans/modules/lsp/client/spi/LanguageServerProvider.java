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

import java.io.InputStream;
import java.io.OutputStream;
import org.eclipse.lsp4j.services.LanguageServer;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.modules.lsp.client.LSPBindings;
import org.netbeans.modules.lsp.client.LanguageServerProviderAccessor;
import org.openide.util.Lookup;

/**
 * Possibly start a language server. Should be installed in the Mime Lookup for the
 * given mime type that is handled by the given server.
 *
 * @author lahvac
 */
public interface LanguageServerProvider {
    /**Possibly start a language server. {@code lookup} contains additional
     * information. May return null, meaning no server was started.
     *
     * @param lookup additional information
     * @return a description of the running language server handling requests for
     *         the given mime type, or null if not handled.
     */
    public @CheckForNull LanguageServerDescription startServer(@NonNull Lookup lookup);

    /**
     * A description of a running language server.
     */
    public static final class LanguageServerDescription {

        /**
         * Create the description of a running language server.
         *
         * @param in the InputStream that should be used to communicate with the server
         * @param out the OutputStream that should be used to communicate with the server
         * @param process the process of the running language server, or null if none.
         * @return an instance of LanguageServerDescription
         */
        public static @NonNull LanguageServerDescription create(@NonNull InputStream in, @NonNull OutputStream out, @NullAllowed Process process) {
            return new LanguageServerDescription(in, out, process, null);
        }

        static @NonNull LanguageServerDescription create(@NonNull LanguageServer server) {
            return new LanguageServerDescription(null, null, null, server);
        }

        private final InputStream in;
        private final OutputStream out;
        private final Process process;
        private final LanguageServer server;
        private LSPBindings bindings;

        private LanguageServerDescription(InputStream in, OutputStream out, Process process, LanguageServer server) {
            this.in = in;
            this.out = out;
            this.process = process;
            this.server = server;
        }

        static {
            LanguageServerProviderAccessor.setINSTANCE(new LanguageServerProviderAccessor() {
                @Override
                public InputStream getInputStream(LanguageServerDescription desc) {
                    return desc.in;
                }

                @Override
                public OutputStream getOutputStream(LanguageServerDescription desc) {
                    return desc.out;
                }

                @Override
                public Process getProcess(LanguageServerDescription desc) {
                    return desc.process;
                }

                @Override
                public LanguageServer getServer(LanguageServerDescription desc) {
                    return desc.server;
                }

                @Override
                public LSPBindings getBindings(LanguageServerDescription desc) {
                    return desc.bindings;
                }

                @Override
                public void setBindings(LanguageServerDescription desc, LSPBindings bindings) {
                    desc.bindings = bindings;
                }

                @Override
                public LanguageServerDescription createLanguageServerDescription(LanguageServer server) {
                    return LanguageServerDescription.create(server);
                }
            });
        }

    }
}
