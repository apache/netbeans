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
package org.netbeans.modules.java.lsp.server.protocol;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.eclipse.lsp4j.MessageActionItem;
import org.eclipse.lsp4j.MessageParams;
import org.eclipse.lsp4j.MessageType;
import org.eclipse.lsp4j.ShowMessageRequestParams;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.modules.java.lsp.server.input.QuickPickItem;
import org.netbeans.modules.java.lsp.server.input.ShowInputBoxParams;
import org.netbeans.modules.java.lsp.server.input.ShowMutliStepInputParams;
import org.netbeans.modules.java.lsp.server.input.ShowQuickPickParams;
import org.openide.awt.StatusDisplayer.Message;
import org.openide.util.Lookup;

public abstract class UIContext {
    private static final Logger LOG = Logger.getLogger(UIContext.class.getName());
    
    private static Reference<UIContext> lastCtx = new WeakReference<>(null);
    
    /**
     * Allows to pass Lookup as a context to locate UIContext implementation; can be useful for tests. If not found
     * in the context `lkp', will be searched in the default Lookup (if lkp is not the default one).
     * @param lkp context lookup
     * @return UIContext.
     */
    @NonNull
    public static synchronized UIContext find(Lookup lkp) {
        UIContext ctx = lkp.lookup(UIContext.class);
        if (ctx != null) {
            LOG.log(Level.FINE, "Acquired user context from lookup: {0}, context instance: {1}", new Object[] { lkp, ctx });
            return ctx;
        }
        Lookup def = Lookup.getDefault();
        if (lkp != def) {
            ctx = def.lookup(UIContext.class);
            LOG.log(Level.FINE, "Acquired user context from default lookup: {0}, context instance: {1}", new Object[] { lkp, ctx });
        }
        if (ctx == null) {
            // PENDING: better context transfer between threads is needed; this way the UIContext can remote to a bad
            // LSP client window
            ctx = lastCtx.get();
            if (ctx != null && !ctx.isValid()) {
                lastCtx.clear();
                ctx = null;
            }
        }

        if (ctx == null) {
            ctx = LogImpl.DEFAULT;
        } else {
            if (lastCtx.get() != ctx) {
                lastCtx = new WeakReference<>(ctx);
            }
        }

        return ctx;
    }

    @NonNull
    public static synchronized UIContext find() {
        return find(Lookup.getDefault());
    }

    public abstract boolean isValid();
    public abstract void showMessage(MessageParams msg);
    public CompletableFuture<String> showHtmlPage(HtmlPageParams msg) {
        showMessage(new MessageParams(MessageType.Log, msg.getText()));
        return CompletableFuture.completedFuture(null);
    }
    public abstract CompletableFuture<MessageActionItem> showMessageRequest(ShowMessageRequestParams msg);
    public abstract void logMessage(MessageParams msg);
    public abstract Message showStatusMessage(ShowStatusMessageParams msg);
    
    /**
     * Shows an input box to ask the user for a text input.
     *
     * @param params properties of input to display
     * @return future that yields the entered value
     * @since 1.18
     */
    public CompletableFuture<String> showInputBox(ShowInputBoxParams params) {
        throw new AbstractMethodError();
    }

    public CompletableFuture<List<QuickPickItem>> showQuickPick(ShowQuickPickParams params) {
        throw new AbstractMethodError();
    }

    public CompletableFuture<Map<String, Either<List<QuickPickItem>, String>>> showMultiStepInput(ShowMutliStepInputParams params) {
        throw new AbstractMethodError();
    }

    private static final class LogImpl extends UIContext {
        static final LogImpl DEFAULT = new LogImpl();

        private LogImpl() {
        }

        @Override
        public CompletableFuture<MessageActionItem> showMessageRequest(ShowMessageRequestParams msg) {
            System.err.println(msg.getType() + ": " + msg.getMessage());
            CompletableFuture<MessageActionItem> ai = CompletableFuture.completedFuture(null);
            return ai;
        }

        @Override
        public void showMessage(MessageParams msg) {
            System.err.println(msg.getType() + ": " + msg.getMessage());
        }

        @Override
        public void logMessage(MessageParams msg) {
            System.err.println(msg.getType() + ": " + msg.getMessage());
        }

        @Override
        public Message showStatusMessage(ShowStatusMessageParams msg) {
            System.err.println(msg.getType() + ": " + msg.getMessage());
            return (int timeInMillis) -> {};
        }

        @Override
        public boolean isValid() {
            return true;
        }

        @Override
        public CompletableFuture<String> showHtmlPage(HtmlPageParams msg) {
            System.err.println("Open in browser: " + msg.getText());
            return CompletableFuture.completedFuture(null);
        }

        @Override
        public CompletableFuture<String> showInputBox(ShowInputBoxParams params) {
            System.err.println("input: " + params.getPrompt());
            CompletableFuture<String> ai = CompletableFuture.completedFuture(null);
            return ai;
        }

        @Override
        public CompletableFuture<List<QuickPickItem>> showQuickPick(ShowQuickPickParams params) {
            System.err.println("quickPick: " + params.getPlaceHolder());
            return CompletableFuture.completedFuture(Collections.emptyList());
        }
    }
}
