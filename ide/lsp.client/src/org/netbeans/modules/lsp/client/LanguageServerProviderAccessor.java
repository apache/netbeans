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
package org.netbeans.modules.lsp.client;

import java.io.InputStream;
import java.io.OutputStream;
import org.eclipse.lsp4j.services.LanguageServer;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.modules.lsp.client.spi.LanguageServerProvider.LanguageServerDescription;
import org.openide.util.Exceptions;

/**
 *
 * @author lahvac
 */
public abstract class LanguageServerProviderAccessor {

    public static synchronized LanguageServerProviderAccessor getINSTANCE () {
        if (INSTANCE == null) {
            try {
                Class.forName(LanguageServerDescription.class.getName(), true, LanguageServerDescription.class.getClassLoader());   //NOI18N            
                assert INSTANCE != null;
            } catch (ClassNotFoundException e) {
                Exceptions.printStackTrace(e);
            }
        }
        return INSTANCE;
    }
    
    public static void setINSTANCE (LanguageServerProviderAccessor instance) {
        assert instance != null;
        INSTANCE = instance;
    }
    
    private static volatile LanguageServerProviderAccessor INSTANCE;

    public abstract InputStream getInputStream(LanguageServerDescription desc);
    public abstract OutputStream getOutputStream(LanguageServerDescription desc);
    public abstract Process getProcess(LanguageServerDescription desc);
    public abstract LanguageServer getServer(LanguageServerDescription desc);
    public abstract LSPBindings getBindings(LanguageServerDescription desc);
    public abstract void setBindings(LanguageServerDescription desc, LSPBindings bindings);
    public abstract LanguageServerDescription createLanguageServerDescription(@NonNull LanguageServer server);
}
