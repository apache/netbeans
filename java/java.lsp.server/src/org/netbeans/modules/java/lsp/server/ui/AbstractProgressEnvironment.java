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
package org.netbeans.modules.java.lsp.server.ui;

import java.util.regex.Pattern;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.modules.java.lsp.server.progress.LspInternalHandle;
import org.netbeans.modules.java.lsp.server.progress.LspProgressUIWorker;
import org.netbeans.modules.java.lsp.server.progress.OperationContext;
import org.netbeans.modules.java.lsp.server.protocol.NbCodeLanguageClient;
import org.netbeans.modules.progress.spi.Controller;
import org.netbeans.modules.progress.spi.InternalHandle;
import org.netbeans.modules.progress.spi.ProgressEnvironment;
import org.openide.util.Cancellable;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author sdedic
 */
public abstract class AbstractProgressEnvironment implements ProgressEnvironment {
    /**
     * Some cancellables should not be accepted: they actually do weird things like
     * RepositoryUpdater's one: it will do further UI operations when cancelled. Add
     * impl class patterns here to make such progresses not cancellable in LSP clients.
     */
    private static final String[] MASK_CANCELLABLES = {
        "org.netbeans.modules.parsing.impl.indexing.RepositoryUpdater$"
    };
    
    private static final Controller NO_CONTEXT_CONTROLLER = new Controller(null);
    private final Lookup env;
    
    private Pattern patternMaskingCancellables;
    
    protected AbstractProgressEnvironment() {
        this(Lookups.proxy(() -> Lookup.getDefault()));
    }

    protected AbstractProgressEnvironment(Lookup env) {
        this.env = env;
    }
    
    Pattern maskCancellablesPattern() {
        if (patternMaskingCancellables == null) {
            StringBuilder re = new StringBuilder();
            for (String pref : MASK_CANCELLABLES) {
                if (re.length() > 0) {
                    re.append("|");
                }
                re.append(Pattern.quote(pref) + ".*");
            }
            patternMaskingCancellables = Pattern.compile(re.toString());
        }
        return patternMaskingCancellables;
    }
    
    @Override
    public ProgressHandle createHandle(String displayname, Cancellable c, boolean userInit) {
        NbCodeLanguageClient client = env.lookup(NbCodeLanguageClient.class);
        OperationContext ctx = OperationContext.find(env);
        if (client == null) {
            if (ctx == null) {
                return new NoContextHandle(displayname, c, userInit).createProgressHandle();
            }
            client = ctx.getClient();
        }
        if (c != null && maskCancellablesPattern().matcher(c.getClass().getName()).matches()) {
            c = null;
        }
        return new LspInternalHandle(
                ctx, client, this::findController, displayname, c, userInit).createProgressHandle();
    }
    
    public Controller findController(InternalHandle h) {
        if (!(h instanceof LspInternalHandle)) {
            return getController();
        }
        OperationContext ctx = ((LspInternalHandle)h).getContext();
        NbCodeLanguageClient client = ctx.getClient();
        if (client == null) {
            return getController();
        }
        return new Controller(
            new LspProgressUIWorker()
        );
    }

    @Override
    public Controller getController() {
        NbCodeLanguageClient client = Lookup.getDefault().lookup(NbCodeLanguageClient.class);
        if (client == null) {
            return NO_CONTEXT_CONTROLLER;
        }
        return new Controller(
            new LspProgressUIWorker()
        );
    }
    
    static class NoContextHandle extends InternalHandle {

        public NoContextHandle(String displayName, Cancellable cancel, boolean userInitiated) {
            super(displayName, cancel, userInitiated);
        }
        

        @Override
        public synchronized void start(String message, int workunits, long estimate) {
            try {
                setController(NO_CONTEXT_CONTROLLER);
            } catch (IllegalStateException ex) {
                // controller already set
            }
            super.start(message, workunits, estimate);
        }
    }
    
}
