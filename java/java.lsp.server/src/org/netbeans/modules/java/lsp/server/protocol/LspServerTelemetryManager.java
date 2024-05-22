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
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.Future;
import org.eclipse.lsp4j.MessageType;
import org.eclipse.lsp4j.services.LanguageClient;
import org.netbeans.api.java.queries.CompilerOptionsQuery;
import org.netbeans.api.java.queries.CompilerOptionsQuery.Result;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ui.ProjectProblems;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

/**
 *
 * @author atalati
 */
public class LspServerTelemetryManager {

    public final String SCAN_START_EVT = "SCAN_START_EVT";
    public final String SCAN_END_EVT = "SCAN_END_EVT";
    public final String WORKSPACE_INFO_EVT = "WORKSPACE_INFO_EVT";
    
    private final String ENABLE_PREVIEW = "--enable-preview"; 
    private final WeakHashMap<LanguageClient, Future<Void>> clients = new WeakHashMap<>();
    private long lspServerIntiailizationTime;

    public synchronized void connect(LanguageClient client, Future<Void> future) {
        clients.put(client, future);
        lspServerIntiailizationTime = System.currentTimeMillis();
    }

    public void sendTelemetry(TelemetryEvent event) {
        Set<LanguageClient> toRemove = new HashSet<>();
        for (Map.Entry<LanguageClient, Future<Void>> entry : clients.entrySet()) {
            if (entry.getValue().isDone()) {
                toRemove.add(entry.getKey());
            } else {
                entry.getKey().telemetryEvent(event);
            }
        }
        for (LanguageClient lc : toRemove) {
            clients.remove(lc);
        }
    }

    public void sendWorkspaceInfo(Project[] prjs, long timeToOpenPrjs) {
        JsonObject properties = new JsonObject();
        JsonArray prjProps = new JsonArray();

        for (Project prj : prjs) {
            try {
                
                JsonObject obj = new JsonObject();
                
                String prjPath = prj.getProjectDirectory().getPath();
                String prjId = this.getPrjId(prjPath);
                obj.addProperty("id", prjId);
                
                String buildToolName = prj.getClass().getSimpleName();
                obj.addProperty("buildTool", (buildToolName.equals("NbGradleProjectImpl") ? "GradleProject" : "MavenProject"));
                
                obj.addProperty("javaVersion", System.getProperty("java.version"));
                obj.addProperty("openedWithProblems", ProjectProblems.isBroken(prj));
                
                boolean isPreviewFlagEnabled = this.isEnablePreivew(prj);
                obj.addProperty("enablePreview", isPreviewFlagEnabled);
                
                prjProps.add(obj);
            } catch (NoSuchAlgorithmException ex) {
                Exceptions.printStackTrace(ex);
            }

        }
        properties.add("prjsInfo", prjProps);
        if (prjs.length == 0) {
            JsonObject obj = new JsonObject();

            obj.addProperty("buildTool", "Standalone");
            obj.addProperty("javaVersion", System.getProperty("java.version"));
            obj.addProperty("openedWithProblems", false);
            prjProps.add(obj);
        }
        properties.addProperty("timeToOpenPrjs", timeToOpenPrjs);
        properties.addProperty("numOfPrjsOpened", prjs.length);
        properties.addProperty("lspServerInitializationTime", System.currentTimeMillis() - this.lspServerIntiailizationTime);

        this.sendTelemetry(new TelemetryEvent(MessageType.Info.toString(), this.WORKSPACE_INFO_EVT, properties));
    }

    public boolean isEnablePreivew(Project prj){
        FileObject prjDir = prj.getProjectDirectory();
        Result result = CompilerOptionsQuery.getOptions(prjDir);
        return result.getArguments().contains(this.ENABLE_PREVIEW);
    }
            
    public String getPrjId(String prjPath) throws NoSuchAlgorithmException
    {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(prjPath.getBytes(StandardCharsets.UTF_8));
        
        BigInteger number = new BigInteger(1, hash);
 
        // Convert message digest into hex value
        StringBuilder hexString = new StringBuilder(number.toString(16));
 
        // Pad with leading zeros
        while (hexString.length() < 64)
        {
            hexString.insert(0, '0');
        }
 
        return hexString.toString();
    }
    
}


