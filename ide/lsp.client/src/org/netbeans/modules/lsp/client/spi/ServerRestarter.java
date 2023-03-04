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
package org.netbeans.modules.lsp.client.spi;

/**
 * Allows to stop a running LSP server.
 * Passed in the Lookup to {@link LanguageServerProvider#startServer(org.openide.util.Lookup) },
 * when since is called, the existing server (if any) will be stoped, and {@code startServer}
 * will be called again.
 *
 * @since 1.8
 */
public interface ServerRestarter {

    /**
     * Stop the running LSP server.
     */
    public void restart();

}
