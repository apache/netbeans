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
package org.netbeans.modules.git.api;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.PasswordAuthentication;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collections;
import javax.swing.SwingUtilities;
import org.netbeans.libs.git.GitException;
import org.netbeans.libs.git.GitRemoteConfig;
import org.netbeans.libs.git.GitURI;
import org.netbeans.modules.git.GitModuleConfig;
import org.netbeans.modules.git.client.GitClient;
import org.netbeans.modules.git.client.GitClientExceptionHandler;
import org.netbeans.modules.git.ui.clone.CloneAction;
import org.netbeans.modules.git.ui.history.SearchHistoryAction;
import org.netbeans.modules.git.ui.repository.remote.ConnectionSettings;
import org.netbeans.modules.git.utils.GitUtils;
import org.netbeans.modules.versioning.spi.VersioningSupport;

/**
 *
 * @author Tomas Stupka
 */
public final class Git {

    public static File cloneRepository (String url, String userName, char[] password) throws URISyntaxException {
        assert !SwingUtilities.isEventDispatchThread() : "Accessing remote repository. Do not call in awt!";
        
        if (url == null) {
            throw new IllegalArgumentException("repository URL is null"); //NOI18N
        }

        return CloneAction.performClone(url, userName == null || password == null
                ? null
                : new PasswordAuthentication(userName, password), true); 
        
    }

    public static void addRecentUrl(String url) throws URISyntaxException {
        GitModuleConfig.getDefault().insertRecentConnectionSettings(new ConnectionSettings(new GitURI(url)));
    }

    public static boolean isOwner (File file) {
        return org.netbeans.modules.git.Git.getInstance().isManaged(file);
    }

    public static void openSearchHistory (File file, String commitId) {
        SearchHistoryAction.openSearch(org.netbeans.modules.git.Git.getInstance().getRepositoryRoot(file), 
                file, file.getName(), commitId, commitId);
    }
    
    public static void openSearchHistoryBranch (File file, String branch) {
        SearchHistoryAction.openSearch(org.netbeans.modules.git.Git.getInstance().getRepositoryRoot(file), new File[] {file}, branch, file.getName(), true);
    }
    
    public static void openSearchHistory (File file, String commitIdFrom, String commitIdTo) {
        SearchHistoryAction.openSearch(org.netbeans.modules.git.Git.getInstance().getRepositoryRoot(file), 
                file, file.getName(), commitIdFrom, commitIdTo);
    }

    public static void initializeRepository (File localFolder, String repositoryUrl, PasswordAuthentication credentials) throws URISyntaxException {
        GitClient client = null;
        try {
            client = org.netbeans.modules.git.Git.getInstance().getClient(localFolder);
            client.init(GitUtils.NULL_PROGRESS_MONITOR);
            String remoteName = GitUtils.REMOTE_ORIGIN;
            client.setRemote(new GitRemoteConfig(remoteName, Arrays.asList(repositoryUrl),
                    Collections.<String>emptyList(),
                    Arrays.asList(GitUtils.getRefSpec("*", remoteName)),
                    Collections.<String>emptyList()),
                    GitUtils.NULL_PROGRESS_MONITOR);
            ConnectionSettings setts = new ConnectionSettings(new GitURI(repositoryUrl));
            if (credentials != null) {
                String user = credentials.getUserName();
                char[] passw = credentials.getPassword();
                if (user != null) {
                    setts.setUser(user);
                }
                if (passw != null) {
                    setts.setPassword(passw.clone());
                    setts.setSaveCredentials(true);
                }
            }
            GitModuleConfig.getDefault().insertRecentConnectionSettings(setts);
            createBranchRef(GitUtils.getGitFolderForRoot(localFolder), GitUtils.MASTER, remoteName);
            org.netbeans.modules.git.Git.getInstance().versionedFilesChanged();                       
        } catch (GitException ex) {
            GitClientExceptionHandler.notifyException(ex, true);
        } catch (IOException ex) {
            GitClientExceptionHandler.notifyException(ex, true);
        } finally {
            if (client != null) {
                client.release();
            }
            org.netbeans.modules.git.Git.getInstance().clearAncestorCaches();
            VersioningSupport.versionedRootsChanged();
        }
    }

    private static void createBranchRef (File gitFolder, String branch, String remoteName) throws IOException {
        StringBuilder sb = new StringBuilder();
        BufferedReader br = null;
        PrintWriter pw = null;
        File configFile = new File(gitFolder, "config");
        File configFileTmp = new File(gitFolder, "config.tmp");
        try {
            br = new BufferedReader(new FileReader(configFile));
            for (String line = br.readLine(); line != null; line = br.readLine()) {
                sb.append(line).append('\n');
            }
            br.close();
            sb.append("[branch \"").append(branch).append("\"]\n\tremote = ")
                    .append(remoteName).append("\n\tmerge = refs/heads/")
                    .append(branch).append('\n');
            pw = new PrintWriter(configFileTmp);
            pw.print(sb.toString());
        } finally {
            if (pw != null) {
                pw.close();
            }
            if (br != null) {
                br.close();
            }
        }
        if (configFileTmp.exists()) {
            if (!configFileTmp.renameTo(configFile)) {
                configFile.delete();
                configFileTmp.renameTo(configFile);
            }
        }
    }
    
}
