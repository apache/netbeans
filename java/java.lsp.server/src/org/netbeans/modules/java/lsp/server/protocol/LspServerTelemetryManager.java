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

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import org.eclipse.lsp4j.ConfigurationItem;
import org.eclipse.lsp4j.ConfigurationParams;
import org.eclipse.lsp4j.MessageType;
import org.eclipse.lsp4j.services.LanguageClient;
import org.netbeans.api.java.queries.CompilerOptionsQuery;
import org.netbeans.api.java.queries.CompilerOptionsQuery.Result;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ui.ProjectProblems;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;

/**
 *
 * @author atalati
 */
public class LspServerTelemetryManager {

    public final String SCAN_START_EVT = "SCAN_START_EVT";
    public final String SCAN_END_EVT = "SCAN_END_EVT";
    public final String WORKSPACE_INFO_EVT = "WORKSPACE_INFO_EVT";

    private final String ENABLE_PREVIEW = "--enable-preview";
    private final String STANDALONE_PRJ = "Standalone";
    private final WeakHashMap<LanguageClient, Future<Void>> clients = new WeakHashMap<>();
    private long lspServerIntiailizationTime;

    public synchronized void connect(LanguageClient client, Future<Void> future) {
        clients.put(client, future);
        lspServerIntiailizationTime = System.currentTimeMillis();
    }

    public synchronized void sendTelemetry(TelemetryEvent event) {
        Set<LanguageClient> toRemove = new HashSet<>();
        List<LanguageClient> toSendTelemetry = new ArrayList<>();

        synchronized (clients) {
            for (Map.Entry<LanguageClient, Future<Void>> entry : clients.entrySet()) {
                if (entry.getValue().isDone()) {
                    toRemove.add(entry.getKey());
                } else {
                    toSendTelemetry.add(entry.getKey());
                }
            }
            clients.keySet().removeAll(toRemove);
        }

        for (LanguageClient client : toSendTelemetry) {
            client.telemetryEvent(event);
        }
    }
    
    public void sendTelemetry(LanguageClient client, TelemetryEvent event) {
        boolean shouldSendTelemetry = false;

        synchronized (clients) {
            if(clients.containsKey(client)){
                if (clients.get(client).isDone()) {
                    clients.remove(client);
                } else {
                    shouldSendTelemetry = true;
                }
            }
        }

        if (shouldSendTelemetry) {
            client.telemetryEvent(event);
        }
    }

    public void sendWorkspaceInfo(LanguageClient client, List<FileObject> workspaceClientFolders, Collection<Project> prjs, long timeToOpenPrjs) {
        JsonObject properties = new JsonObject();
        JsonArray prjProps = new JsonArray();

        Map<String, Project> mp = prjs.stream()
                .collect(Collectors.toMap(project -> project.getProjectDirectory().getPath(), project -> project));

        for (FileObject workspaceFolder : workspaceClientFolders) {
            try {
                JsonObject obj = new JsonObject();
                String prjPath = workspaceFolder.getPath();
                String prjId = this.getPrjId(prjPath);
                obj.addProperty("id", prjId);
                
                // In future if different JDK is used for different project then this can be updated 
                obj.addProperty("javaVersion", System.getProperty("java.version"));

                if (mp.containsKey(prjPath)) {
                    Project prj = mp.get(prjPath);

                    ProjectManager.Result r = ProjectManager.getDefault().isProject2(prj.getProjectDirectory());
                    String projectType = r.getProjectType();
                    obj.addProperty("buildTool", (projectType.contains("maven") ? "MavenProject" : "GradleProject"));

                    obj.addProperty("openedWithProblems", ProjectProblems.isBroken(prj));

                    boolean isPreviewFlagEnabled = this.isEnablePreivew(prj.getProjectDirectory(), projectType);
                    obj.addProperty("enablePreview", isPreviewFlagEnabled);
                } else {
                    obj.addProperty("buildTool", this.STANDALONE_PRJ);
                    obj.addProperty("javaVersion", System.getProperty("java.version"));
                    obj.addProperty("openedWithProblems", false);

                    boolean isPreviewFlagEnabled = this.isEnablePreivew(workspaceFolder, this.STANDALONE_PRJ);
                    obj.addProperty("enablePreview", isPreviewFlagEnabled);
                }

                prjProps.add(obj);

            } catch (NoSuchAlgorithmException ex) {
                Exceptions.printStackTrace(ex);
            }
        }

        properties.add("prjsInfo", prjProps);

        properties.addProperty("timeToOpenPrjs", timeToOpenPrjs);
        properties.addProperty("numOfPrjsOpened", workspaceClientFolders.size());
        properties.addProperty("lspServerInitializationTime", System.currentTimeMillis() - this.lspServerIntiailizationTime);

        this.sendTelemetry(client, new TelemetryEvent(MessageType.Info.toString(), this.WORKSPACE_INFO_EVT, properties));
    }
    
    private boolean isEnablePreivew(FileObject source, String prjType) {
        if (prjType.equals(this.STANDALONE_PRJ)) {
            NbCodeLanguageClient client = Lookup.getDefault().lookup(NbCodeLanguageClient.class);
            if (client == null) {
                return false;
            }
            AtomicBoolean isEnablePreviewSet = new AtomicBoolean(false);
            ConfigurationItem conf = new ConfigurationItem();
            conf.setSection(client.getNbCodeCapabilities().getAltConfigurationPrefix() + "runConfig.vmOptions");
            client.configuration(new ConfigurationParams(Collections.singletonList(conf))).thenAccept(c -> {
                String config = ((JsonPrimitive) ((List<Object>) c).get(0)).getAsString();
                isEnablePreviewSet.set(config.contains(this.ENABLE_PREVIEW));
            });
            
            return isEnablePreviewSet.get();
        }
        
        Result result = CompilerOptionsQuery.getOptions(source);
        return result.getArguments().contains(this.ENABLE_PREVIEW);
    }

    private String getPrjId(String prjPath) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(prjPath.getBytes(StandardCharsets.UTF_8));

        BigInteger number = new BigInteger(1, hash);

        // Convert message digest into hex value
        StringBuilder hexString = new StringBuilder(number.toString(16));

        // Pad with leading zeros
        while (hexString.length() < 64) {
            hexString.insert(0, '0');
        }

        return hexString.toString();
    }
    
}
