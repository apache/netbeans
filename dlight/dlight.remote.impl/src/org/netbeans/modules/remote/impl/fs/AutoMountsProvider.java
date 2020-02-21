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
package org.netbeans.modules.remote.impl.fs;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import org.netbeans.modules.dlight.libs.common.PathUtilities;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.HostInfo;
import org.netbeans.modules.nativeexecution.api.util.CommonTasksSupport;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager;
import org.netbeans.modules.nativeexecution.api.util.HostInfoUtils;
import org.netbeans.modules.remote.impl.RemoteLogger;
import org.openide.util.NbPreferences;

/**
 */
public class AutoMountsProvider {

    private final ExecutionEnvironment env;
    private static final String AUTO_MOUNT_KEY = "auto.mounts"; //NOI18N

    public AutoMountsProvider(ExecutionEnvironment env) {
        this.env = env;        
    }

    public static List<String> restoreAutoMounts() {
        String restored = NbPreferences.forModule(AutoMountsProvider.class).get(AUTO_MOUNT_KEY, null);
        if (restored == null || restored.isEmpty()) {
            return getFixedAutoMounts();
        } else {
            Set<String> result = new TreeSet<>();
            for (String p : restored.split(",")) { //NOI18N
                if (p.startsWith("/")) { //NOI18N
                    result.add(p);
                }
            }
            appendExplicitlySet(result);
            return new ArrayList<>(result);
        }
    }
    
    private List<String> storeAutoMounts(List<String> autoMounts) {
        StringBuilder sb = new StringBuilder();
        for (String p : autoMounts) {
            if (sb.length() > 0) {
                sb.append(',');
            }
            sb.append(p);
        }        
        NbPreferences.forModule(AutoMountsProvider.class).put(AUTO_MOUNT_KEY, sb.toString());
        return autoMounts;
    }

    private static List<String> getFixedAutoMounts() {
        Set<String> set = new TreeSet<>(Arrays.asList("/net", "/set", "/import", "/shared", "/home", "/ade_autofs", "/ade", "/ws", "/workspace")); //NOI18N
        appendExplicitlySet(set);
        return new ArrayList<>(set);
    }

    private static void appendExplicitlySet(Collection<String> list) {
        String t = System.getProperty("remote.autofs.list"); //NOI18N
        if (t != null) {
            String[] paths = t.split(","); //NOI18N
            for (String p : paths) {
                if (p.startsWith("/")) { //NOI18N
                    list.add(p);
                }
            }
        }
    }

    public List<String> analyze() {
        try {
            if (!ConnectionManager.getInstance().isConnectedTo(env)) {
                return null;
            }
            HostInfo hostInfo = HostInfoUtils.getHostInfo(env);
            switch (hostInfo.getOSFamily()) {
                case SUNOS:
                    return storeAutoMounts(analyzeSolarisAutoMounts());
                case LINUX:
                    return storeAutoMounts(analyzeLinuxAutoMounts());
                case WINDOWS:
                case MACOSX:
                case UNKNOWN:
                default:
                    return null;
            }
        } catch (IOException | ConnectionManager.CancellationException | InterruptedException | ExecutionException ex) {
            RemoteLogger.fine(ex);
        }
        return null;
    }

    private List<String> readFile(String path) throws IOException, InterruptedException, ExecutionException {
        File dstFile = File.createTempFile(path.length() < 3 ? path + "___" : path, ".tmp"); //NOI18N
        try {
            Future<Integer> task = CommonTasksSupport.downloadFile(path, env, dstFile, null);
            Integer rc = task.get();
            if (rc != 0) {
                throw new IOException("Error reading file " + path + " rc=" + rc); //NOI18N
            }
            List<String> result = new ArrayList<>();
            try (BufferedReader rdr = new BufferedReader(new FileReader(dstFile))) {
                String line;
                while ((line = rdr.readLine()) != null) {
                    result.add(line);
                }
            }
            if (Boolean.getBoolean("remote.dump.automounts")) {
                StringBuilder sb = new StringBuilder("AutoMounts analyzer: the content of "); //NOI18N
                sb.append(env).append(':').append(path).append(" [comments filtered out]:"); // NOI18N
                for (String l : result) {
                    if (!l.startsWith("#")) { //NOI18N
                        sb.append('\n').append(l);
                    }
                }
                System.out.println(sb);
            }
            return result;
        } finally {
            dstFile.delete();
        }
    }

    private List<String> analyzeLinuxAutoMounts() throws IOException, InterruptedException, ExecutionException {
        Set<String> autoMounts = new TreeSet<>();
        List<String> lines = readFile("/etc/auto.master"); //NOI18N
        for (String l : lines) {
            if (l.startsWith("/")) { //NOI18N
                String[] words = l.split("\\s+"); // NOI18N
                if (words.length > 0) {
                    String path = words[0];
                    if (!path.equals("/-")) { // NOI18N
                        autoMounts.add(path);
                    }
                }
            }
        }
        return new ArrayList<>(autoMounts);
    }

    private List<String> analyzeSolarisAutoMounts() throws IOException, InterruptedException, ExecutionException {
        Set<String> autoMounts = new TreeSet<>();
        List<String> lines = readFile("/etc/auto_master"); //NOI18N
        for (String l : lines) {
            if (l.startsWith("/")) { //NOI18N
                String[] words = l.split("\\s+"); // NOI18N
                if (words.length > 0) {
                    String path = words[0];
                    if (!path.equals("/-")) { // NOI18N
                        autoMounts.add(path);
                    }
                }
            }
        }
        lines = readFile("/etc/mnttab"); //NOI18N
        for (String l : lines) {
            if (l.startsWith("auto_")) { //NOI18N
                String[] words = l.split("\\s+"); // NOI18N
                if (words.length > 1) {
                    String path = words[1];
                    if (!path.equals("/-") && !containsParent(autoMounts, path)) { // NOI18N
                        autoMounts.add(path);
                    }
                }
            }
        }
        return new ArrayList<>(autoMounts);
    }

    private boolean containsParent(Collection<String> autoMounts, String path) {
        for (String parent = PathUtilities.getDirName(path);
                parent != null && !parent.isEmpty();
                parent = PathUtilities.getDirName(parent)) {
            if (autoMounts.contains(parent)) {
                return true;
            }
        }
        return false;
    }
}
