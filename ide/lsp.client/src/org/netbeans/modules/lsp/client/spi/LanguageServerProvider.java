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
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.project.Project;
import org.netbeans.modules.lsp.client.LanguageServerProviderAccessor;

/**
 *
 * @author lahvac
 */
public interface LanguageServerProvider {
    public @CheckForNull LanguageServerDescription startServer(@NonNull Project prj);
    public static final class LanguageServerDescription {
        private final InputStream in;
        private final OutputStream out;
        private final Process process;

        public LanguageServerDescription(InputStream in, OutputStream out, Process process) {
            this.in = in;
            this.out = out;
            this.process = process;
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
            });
        }

    }
}
