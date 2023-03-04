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
package org.netbeans.modules.nbcode.integration.commands;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionParams;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.cloud.oracle.OCIManager;
import org.netbeans.modules.cloud.oracle.OCIProfile;
import org.netbeans.modules.cloud.oracle.adm.AuditOptions;
import org.netbeans.modules.cloud.oracle.adm.ProjectVulnerability;
import org.netbeans.modules.java.lsp.server.protocol.CodeActionsProvider;
import org.netbeans.modules.java.lsp.server.protocol.NbCodeLanguageClient;
import org.netbeans.modules.java.lsp.server.protocol.UIContext;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author sdedic
 */
@ServiceProvider(service = CodeActionsProvider.class)
public class ProjectAuditCommand extends CodeActionsProvider {
    private static final Logger LOG = Logger.getLogger(ProjectAuditCommand.class.getName());
    
    /**
     * Force executes the project audit using the supplied compartment and knowledgebase IDs.
     */
    private static final String COMMAND_EXECUTE_AUDIT = "nbls.gcn.projectAudit.execute"; // NOI18N
    
    /**
     * Displays the audit from the Knowledgebase and compartment.
     */
    private static final String COMMAND_LOAD_AUDIT = "nbls.gcn.projectAudit.display"; // NOI18N
    
    public static final Set<String> COMMANDS = new HashSet<>(Arrays.asList(
            COMMAND_EXECUTE_AUDIT,
            COMMAND_LOAD_AUDIT
    ));
    
    @Override
    public List<CodeAction> getCodeActions(ResultIterator resultIterator, CodeActionParams params) throws Exception {
        return Collections.emptyList();
    }
    
    private final Gson gson = new Gson();

    @NbBundle.Messages({
        "# {0} - project name",
        "# {1} - cause message",
        "ERR_KnowledgeBaseSearchFailed=Could not search for knowledge base of project {0}: {1}"
    })
    @Override
    public CompletableFuture<Object> processCommand(NbCodeLanguageClient client, String command, List<Object> arguments) {
        if (arguments.size() < 3) {
            throw new IllegalArgumentException("Expected 3 parameters: resource, compartment, knowledgebase");
        }
        
        FileObject f = Utils.extractFileObject(arguments.get(0), gson);
        Project p = FileOwnerQuery.getOwner(f);
        if (p == null) {
            throw new IllegalArgumentException("Not part of a project " + f);
        }
        ProjectVulnerability v = p.getLookup().lookup(ProjectVulnerability.class);
        ProjectInformation pi = ProjectUtils.getInformation(p);
        String n = pi.getDisplayName();
        if (n == null) {
            n = pi.getName();
        }
        if (n == null) {
            n = p.getProjectDirectory().getName();
        }
        final String fn = n;
        if (v == null) {
            throw new IllegalArgumentException("Project " + n + " does not support vulnerability audits");
        }
        if (arguments.size() < 3 || !(arguments.get(1) instanceof JsonPrimitive)) {
            throw new IllegalArgumentException("Expected 3 parameters: resource, knowledgebase, options");
        }
        
        String knowledgeBase = ((JsonPrimitive) arguments.get(1)).getAsString();
        Object o = arguments.get(2);
        if (!(o instanceof JsonObject)) {
            throw new IllegalArgumentException("Expected structure, got  " + o);
        }
        JsonObject options = (JsonObject)o;
        
        // PENDING: this is for debugging, temporary. Can be removed in the future when the messaging is stabilized
        UIContext ctx = Lookup.getDefault().lookup(UIContext.class);
        LOG.log(Level.FINE, "Running audit command with context: {0}", ctx);
        
        boolean forceAudit = options.has("force") && options.get("force").getAsBoolean();
        String preferredName = options.has("auditName") ? options.get("auditName").getAsString() : null;
        
        final OCIProfile auditWithProfile;
        
        if (options.has("profile")) {
            String id = options.get("profile").getAsString();
            Path path;
            
            if (options.has("configPath")) {
                path = Paths.get(options.get("configPath").getAsString());
            } else {
                path = null;
            }
            
            auditWithProfile = OCIManager.forConfig(path, id);
        } else {
            auditWithProfile = OCIManager.getDefault().getActiveProfile();
        }
        
        return OCIManager.usingSession(auditWithProfile, () -> v.findKnowledgeBase(knowledgeBase).
                exceptionally(th -> {
                    DialogDisplayer.getDefault().notifyLater(new NotifyDescriptor.Message(Bundle.ERR_KnowledgeBaseSearchFailed(fn, th.getMessage()),
                            NotifyDescriptor.ERROR_MESSAGE));
                    return null;
                }).thenCompose((kb) -> {
            if (kb == null) {
                return CompletableFuture.completedFuture(null);
            }
            CompletableFuture<String> exec;
            
            switch (command) {
                case COMMAND_EXECUTE_AUDIT:
                    exec = v.runProjectAudit(kb, AuditOptions.makeNewAudit().useSession(auditWithProfile).setAuditName(preferredName));
                    break;
                case COMMAND_LOAD_AUDIT: {
                    exec = v.runProjectAudit(kb, new AuditOptions().useSession(auditWithProfile).setRunIfNotExists(forceAudit).setAuditName(preferredName));
                }
                default:
                    return CompletableFuture.completedFuture(null);
                    
            }
            return (CompletableFuture<Object>)(CompletableFuture)exec;
        }));
    } 

    @Override
    public Set<String> getCommands() {
        return COMMANDS;
    }
}
