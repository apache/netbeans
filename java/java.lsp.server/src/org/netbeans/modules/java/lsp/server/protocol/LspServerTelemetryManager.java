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
import java.lang.ref.WeakReference;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.WeakHashMap;
import java.util.concurrent.Future;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import org.eclipse.lsp4j.ConfigurationItem;
import org.eclipse.lsp4j.ConfigurationParams;
import org.eclipse.lsp4j.MessageType;
import org.eclipse.lsp4j.services.LanguageClient;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.queries.CompilerOptionsQuery;
import org.netbeans.api.java.queries.CompilerOptionsQuery.Result;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ui.ProjectProblems;
import org.netbeans.modules.java.platform.implspi.JavaPlatformProvider;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;

/**
 *
 * @author atalati
 */
public class LspServerTelemetryManager {

    private static final Logger LOG = Logger.getLogger(LspServerTelemetryManager.class.getName());
    public static final String SCAN_START_EVT = "SCAN_START_EVT";
    public static final String SCAN_END_EVT = "SCAN_END_EVT";
    public static final String WORKSPACE_INFO_EVT = "workspaceChange";

    private static final String ENABLE_PREVIEW = "--enable-preview";

    public static enum ProjectType {
        standalone,
        maven,
        gradle;
    }

    private LspServerTelemetryManager() {
    }

    public static LspServerTelemetryManager getInstance() {
        return Singleton.instance;
    }

    private static class Singleton {

        private static final LspServerTelemetryManager instance = new LspServerTelemetryManager();
    }

    private final WeakHashMap<LanguageClient, WeakReference<Future<Void>>> clients = new WeakHashMap<>();
    private volatile boolean telemetryEnabled = false;
    private long lspServerIntializationTime;

    public boolean isTelemetryEnabled() {
        return telemetryEnabled;
    }

    public void connect(LanguageClient client, Future<Void> future) {
        synchronized (clients) {
            clients.put(client, new WeakReference<>(future));
            telemetryEnabled = true;
            lspServerIntializationTime = System.currentTimeMillis();
        }
    }

    public void sendTelemetry(TelemetryEvent event) {
        if (telemetryEnabled) {
            ArrayList<LanguageClient> clientsCopy = new ArrayList<>(2);
            synchronized (clients) {
                Iterator<Map.Entry<LanguageClient, WeakReference<Future<Void>>>> iterator = clients.entrySet().iterator();
                while (iterator.hasNext()) {
                    Map.Entry<LanguageClient, WeakReference<Future<Void>>> e = iterator.next();
                    if (isInvalidClient(e.getValue())) {
                        iterator.remove();
                    } else {
                        clientsCopy.add(e.getKey());
                    }
                }
                if (clientsCopy.isEmpty()) {
                    telemetryEnabled = false;
                }
            }
            clientsCopy.forEach(c -> sendTelemetryToValidClient(c, event));
        }
    }

    public void sendTelemetry(LanguageClient client, TelemetryEvent event) {
        if (telemetryEnabled) {
            WeakReference<Future<Void>> closeListener = clients.get(client);
            if (isInvalidClient(closeListener)) {
                synchronized (clients) {
                    if (clients.remove(client, closeListener) && clients.isEmpty()) {
                        telemetryEnabled = false;
                    }
                }
            } else {
                sendTelemetryToValidClient(client, event);
            }
        }
    }

    private void sendTelemetryToValidClient(LanguageClient client, TelemetryEvent event) {
        try {
            client.telemetryEvent(event);
        } catch (Exception e) {
            LOG.log(Level.INFO, "telemetry send failed: {0}", e.getMessage());
        }
    }

    private boolean isInvalidClient(WeakReference<Future<Void>> closeListener) {
        Future<Void> close = closeListener == null ? null : closeListener.get();
        return close == null || close.isDone();
    }

    public void sendWorkspaceInfo(LanguageClient client, List<FileObject> workspaceClientFolders, Collection<Project> projects, long timeToOpenProjects) {
        JsonObject properties = new JsonObject();
        JsonArray prjProps = new JsonArray();

        NavigableMap<String, Project> mp = projects.stream()
                .collect(Collectors.toMap(project -> project.getProjectDirectory().getPath(), project -> project, (p1, p2) -> p1, TreeMap<String, Project>::new));

        for (FileObject workspaceFolder : workspaceClientFolders) {
            try {
                boolean noProjectFound = true;
                String prjPath = workspaceFolder.getPath();
                String prjPathWithSlash = null;
                for (Map.Entry<String, Project> p : mp.tailMap(prjPath, true).entrySet()) {
                    String projectPath = p.getKey();
                    if (prjPathWithSlash == null) {
                        if (prjPath.equals(projectPath)) {
                            prjProps.add(createProjectInfo(prjPath, p.getValue(), workspaceFolder, client));
                            noProjectFound = false;
                            break;
                        }
                        prjPathWithSlash = prjPath + '/';                        
                    }
                    if (projectPath.startsWith(prjPathWithSlash)) {
                        prjProps.add(createProjectInfo(p.getKey(), p.getValue(), workspaceFolder, client));
                        noProjectFound = false;
                        continue;
                    }
                    break;
                }
                if (noProjectFound) {
                    // No project found
                    prjProps.add(createProjectInfo(prjPath, null, workspaceFolder, client));
                }
            } catch (NoSuchAlgorithmException e) {
                LOG.log(Level.INFO, "NoSuchAlgorithmException while creating workspaceInfo event: {0}", e.getMessage());
            } catch (Exception e) {
                LOG.log(Level.INFO, "Exception while creating workspaceInfo event: {0}", e.getMessage());
            }
        }

        properties.add("projectInfo", prjProps);

        properties.addProperty("projInitTimeTaken", timeToOpenProjects);
        properties.addProperty("numProjects", workspaceClientFolders.size());
        properties.addProperty("lspInitTimeTaken", System.currentTimeMillis() - this.lspServerIntializationTime);

        this.sendTelemetry(client, new TelemetryEvent(MessageType.Info.toString(), LspServerTelemetryManager.WORKSPACE_INFO_EVT, properties));
    }

    private JsonObject createProjectInfo(String prjPath, Project prj, FileObject workspaceFolder, LanguageClient client) throws NoSuchAlgorithmException {
        JsonObject obj = new JsonObject();
        String prjId = getPrjId(prjPath);
        obj.addProperty("id", prjId);
        FileObject projectDirectory;
        ProjectType projectType;
        if (prj == null) {
            projectType = ProjectType.standalone;
            projectDirectory = workspaceFolder;
        } else {
            projectType = getProjectType(prj);
            projectDirectory = prj.getProjectDirectory();
            boolean projectHasProblems;
            try {
                projectHasProblems = ProjectProblems.isBroken(prj);
            } catch (RuntimeException e) {
                LOG.log(Level.INFO, "Exception while checking project problems for workspaceInfo event: {0}", e.getMessage());
                projectHasProblems = true;
            }
            obj.addProperty("isOpenedWithProblems", projectHasProblems);
        }
        String javaVersion = getProjectJavaVersion();
        obj.addProperty("javaVersion", javaVersion);
        obj.addProperty("buildTool", projectType.name());
        boolean isPreviewFlagEnabled = isPreviewEnabled(projectDirectory, projectType, client);
        obj.addProperty("isPreviewEnabled", isPreviewFlagEnabled);
        return obj;
    }

    public boolean isPreviewEnabled(FileObject source, ProjectType prjType) {
        return isPreviewEnabled(source, prjType, null);
    }

    public boolean isPreviewEnabled(FileObject source, ProjectType prjType, LanguageClient languageClient) {
        if (prjType == ProjectType.standalone) {
            NbCodeLanguageClient client = languageClient instanceof NbCodeLanguageClient ? (NbCodeLanguageClient) languageClient : null ;
            if (client == null) {
                client = Lookup.getDefault().lookup(NbCodeLanguageClient.class);
                if (client == null) {
                    return false;
                }
            }
            boolean[] isEnablePreviewSet = {false};
            ConfigurationItem conf = new ConfigurationItem();
            conf.setSection(client.getNbCodeCapabilities().getConfigurationPrefix() + "runConfig.vmOptions");
            client.configuration(new ConfigurationParams(Collections.singletonList(conf)))
                    .thenAccept(c -> {
                        isEnablePreviewSet[0] = c != null && !c.isEmpty()
                                && ((JsonPrimitive) c.get(0)).getAsString().contains(ENABLE_PREVIEW);
                    });
            return isEnablePreviewSet[0];
        }

        Result result = CompilerOptionsQuery.getOptions(source);
        return result.getArguments().contains(ENABLE_PREVIEW);
    }

    private String getPrjId(String prjPath) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(prjPath.getBytes(StandardCharsets.UTF_8));

        BigInteger number = new BigInteger(1, hash);

        StringBuilder hexString = new StringBuilder(number.toString(16));

        while (hexString.length() < 64) {
            hexString.insert(0, '0');
        }

        return hexString.toString();
    }

    private String getProjectJavaVersion() {
        final JavaPlatformProvider javaPlatformProvider = Lookup.getDefault().lookup(JavaPlatformProvider.class);
        final JavaPlatform defaultPlatform = javaPlatformProvider == null ? null : javaPlatformProvider.getDefaultPlatform();
        final Map<String, String> props = defaultPlatform == null ? null : defaultPlatform.getSystemProperties();
        final Function<String, String> propLookup = props == null ? System::getProperty : props::get;

        return getJavaRuntimeVersion(propLookup) + ';' + getJavaVmVersion(propLookup) + ';' + getJavaVmName(propLookup);
    }

    public static String getJavaRuntimeVersion(Function<String, String> propertyLookup) {
        String version = propertyLookup.apply("java.runtime.version");
        if (version == null) {
            version = propertyLookup.apply("java.version");
        }
        return version;
    }

    public static String getJavaVmVersion(Function<String, String> propertyLookup) {
        String version = propertyLookup.apply("java.vendor.version");
        if (version == null) {
            version = propertyLookup.apply("java.vm.version");
            if (version == null) {
                version = propertyLookup.apply("java.version");
            }
        }
        return version;
    }

    public static String getJavaVmName(Function<String, String> propertyLookup) {
        return propertyLookup.apply("java.vm.name");
    }

    public ProjectType getProjectType(Project prj) {
        ProjectManager.Result r = ProjectManager.getDefault().isProject2(prj.getProjectDirectory());
        String projectType = r == null ? null : r.getProjectType();
        return projectType != null && projectType.contains(ProjectType.maven.name()) ? ProjectType.maven : ProjectType.gradle;
    }
}
