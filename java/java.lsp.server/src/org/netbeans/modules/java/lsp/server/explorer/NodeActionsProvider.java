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
package org.netbeans.modules.java.lsp.server.explorer;

import org.netbeans.modules.java.lsp.server.ui.AbstractGlobalActionContext;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.awt.event.ActionEvent;
import java.beans.PropertyVetoException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import javax.swing.Action;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionParams;
import org.netbeans.modules.java.lsp.server.protocol.CodeActionsProvider;
import org.netbeans.modules.java.lsp.server.protocol.NbCodeLanguageClient;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.openide.awt.Actions;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.Node;
import org.openide.util.ContextAwareAction;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;

/**
 * Command provider for node-based commands. The provider handles commands with form:
 * <code>"nbls:" Category ":" ActionID</code>. It attempts to execute an action 
 * created by {@link Actions#forID(java.lang.String, java.lang.String)} applied on the 
 * Node passed as 1st argument to the command. Returns true, if the action was found & executed, 
 * false in case of some issue.
 * <p/>
 * 
 * @author sdedic
 */
public class NodeActionsProvider extends CodeActionsProvider {
    private static final String ATTRIBUTE_ACTION_PREFIX = "action:"; // NOI18N
    private static final String NBLS_ACTION_PREFIX = "nbls:"; // NOI18N
    private static final String CATEGORY_SEPARATOR = ":"; // NOI18N
            
    private final Set<String>  commands;
    private final Gson gson = new Gson();

    NodeActionsProvider(Set<String> commands) {
        this.commands = commands;
    }

    @Override
    public Set<String> getCommands() {
        return commands;
    }
    
    public static NodeActionsProvider forFile(FileObject f) {
        Set<String> commandNames = new HashSet<>();
        Enumeration<String> att = f.getAttributes();
        while (att.hasMoreElements()) {
            String a = att.nextElement();
            if (!a.startsWith(ATTRIBUTE_ACTION_PREFIX)) {
                continue;
            }
            String id = a.substring(ATTRIBUTE_ACTION_PREFIX.length());
            String category = f.getAttribute(a).toString();
            
            String cmd = NBLS_ACTION_PREFIX + category + CATEGORY_SEPARATOR + id;
            commandNames.add(cmd);
        }
        return new NodeActionsProvider(commandNames);
    }

    @Override
    public List<CodeAction> getCodeActions(NbCodeLanguageClient client, ResultIterator resultIterator, CodeActionParams params) throws Exception {
        return Collections.emptyList();
    }
    
    @Override
    public CompletableFuture<Object> processCommand(NbCodeLanguageClient client, String command, List<Object> arguments) {
        JsonElement el = null;
        if (arguments.size() > 0) {
            JsonObject item = gson.fromJson(gson.toJson(arguments.get(0)), JsonObject.class);
            el = item.get("data"); // NOI18N
        }
        int id = -1;
        
        if (el != null) {
            JsonElement nodeId = el.getAsJsonObject().get("id"); // NOI18N
            if (nodeId != null && nodeId.isJsonPrimitive()) {
                id = nodeId.getAsJsonPrimitive().getAsInt();
            }
        }
        
        String categoryAndId = command.substring(NBLS_ACTION_PREFIX.length());
        String category;
        String aid;
        
        int col = categoryAndId.indexOf(CATEGORY_SEPARATOR);
        if (col != -1) {
            aid = categoryAndId.substring(col + 1);
            category = categoryAndId.substring(0, col);
        } else {
            category = null;
            aid = categoryAndId;
        }
        
        if (id == -1) {
            return invokeAction(client, category, aid, arguments);
        }

        //gson.fromJson(arguments.get(0), JSONObject.class);
        // hack through Lookup.getDefault
        TreeNodeRegistry srv = Lookup.getDefault().lookup(TreeNodeRegistry.class);
        Node target = srv.findNode(id);
        if (target == null) {
            return CompletableFuture.completedFuture(false);
        }
        
        TreeViewProvider provider = srv.providerOf(id);
        try {
            provider.getExplorerManager().setSelectedNodes(new Node[] { target });
        } catch (PropertyVetoException ex) {
            CompletableFuture f = new CompletableFuture();
            f.completeExceptionally(ex);
            return f;
        }
        
        
        final Lookup targetLookup = new ProxyLookup(target.getLookup(), provider.getLookup());
        
        AbstractGlobalActionContext.withActionContext(provider.getLookup(), () -> {
            Action a = Actions.forID(category, aid);
            if (a == null) {
                return CompletableFuture.completedFuture(false);
            }
            if (a instanceof ContextAwareAction) {
                a = ((ContextAwareAction)a).createContextAwareInstance(targetLookup);
            }
            final Action a2 = a;

            a2.actionPerformed(new ActionEvent(target, 0, aid));
            return null;
        });
        
        return CompletableFuture.completedFuture(true);
    }
    
    CompletableFuture<Object> invokeAction(NbCodeLanguageClient client, String category, String aid, List<Object> arguments) {
        String path = "Actions/" + category + "/" + aid.replace('.', '-') + ".instance"; //NOI18N
        FileObject config = FileUtil.getConfigFile(path);
        String contextType = (String) config.getAttribute("type"); //NOI18N
        try {
            if (contextType == null) {
                Action a = Actions.forID(category, aid);
                a.actionPerformed(new ActionEvent(client, 0, aid));
                return CompletableFuture.completedFuture(false);
            }
            Class<?> clazz = Thread.currentThread().getContextClassLoader().loadClass(contextType);
            Object context = gson.fromJson(gson.toJson(arguments.get(0)), clazz);
            if (context != null) {
                Lookup targetLookup = Lookups.singleton(context);
                AbstractGlobalActionContext.withActionContext(targetLookup, () -> {
                    Action a = Actions.forID(category, aid);
                    if (a == null) {
                        return CompletableFuture.completedFuture(false);
                    }
                    if (a instanceof ContextAwareAction) {
                        a = ((ContextAwareAction)a).createContextAwareInstance(targetLookup);
                    }
                    final Action a2 = a;

                    a2.actionPerformed(new ActionEvent(client, 0, aid));
                    return null;
                });
            } else {
                return CompletableFuture.completedFuture(false);
            }
        } catch (ClassNotFoundException ex) {
            return completeExceptionally(ex);
        }
        return CompletableFuture.completedFuture(true);
    }
    
    private CompletableFuture completeExceptionally(Throwable t) {
        CompletableFuture f = new CompletableFuture();
        f.completeExceptionally(t);
        return f;
    }
}
