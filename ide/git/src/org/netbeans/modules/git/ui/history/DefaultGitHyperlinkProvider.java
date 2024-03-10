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

package org.netbeans.modules.git.ui.history;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.IntStream;
import org.netbeans.libs.git.GitClient;
import org.netbeans.libs.git.GitException;
import org.netbeans.libs.git.GitRemoteConfig;
import org.netbeans.libs.git.GitRepository;
import org.netbeans.libs.git.GitURI;
import org.netbeans.libs.git.progress.ProgressMonitor.DefaultProgressMonitor;
import org.netbeans.modules.versioning.util.VCSHyperlinkProvider;
import org.openide.awt.StatusDisplayer;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle.Messages;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.ServiceProvider;

/**
 * Looks for '#123' patterns in text (e.g commit messages) and tries to link
 * them to a fitting web page.
 * 
 * <p>The repository host is identified by inspecting the registered remotes
 * of the local git repository.
 * 
 * @author mbien
 */
@Messages({
    "# {0} - issue ID",
    "TT_DefaultGitHyperlinkProvider.link=Click to open {0} in Browser",
    "MSG_DefaultGitHyperlinkProvider.link.failed=Could not resolve issue link."
})
@ServiceProvider(service=VCSHyperlinkProvider.class, position = Integer.MAX_VALUE)
public class DefaultGitHyperlinkProvider extends VCSHyperlinkProvider {
    
    private static final Pattern ids = Pattern.compile("#[0-9]+");

    @Override
    public int[] getSpans(String text) {
        return ids.matcher(text).results()
                                .flatMapToInt(r -> IntStream.of(r.start(), r.end()))
                                .toArray();
    }

    @Override
    public String getTooltip(String text, int offsetStart, int offsetEnd) {
        return Bundle.TT_DefaultGitHyperlinkProvider_link(text.substring(offsetStart, offsetEnd));
    }

    @Override
    public void onClick(File file, String text, int offsetStart, int offsetEnd) {
        Path path = file.toPath();
        while (!Files.isDirectory(path) || Files.notExists(path.resolve(".git"))) {
            path = path.getParent();
            if (path == null) {
                return;
            }
        }
        Path repo = path;
        RequestProcessor.getDefault().post(() -> {
            openIssue(repo, text.substring(offsetStart + 1, offsetEnd));
        });
    }

    private static void openIssue(Path repo, String id) {
        try (GitClient client = GitRepository.getInstance(repo.toFile()).createClient()) {
            Map<String, GitRemoteConfig> remotes = client.getRemotes(new DefaultProgressMonitor());
            // probe well known remotes
            GitRemoteConfig origin =
                    remotes.getOrDefault("origin4nb",
                        remotes.getOrDefault("upstream",
                            remotes.getOrDefault("origin",
                                remotes.getOrDefault("origin/HEAD", null))));
            // fallback: try any of the 'origin/*' remotes
            if (origin == null) {
                origin = remotes.values().stream()
                                .filter(r -> r.getRemoteName().startsWith("origin/"))
                                .findFirst()
                                .orElse(null);
            }
            if (origin != null) {
                for (String str : origin.getUris()) {
                    GitURI uri = new GitURI(str);
                    String path = uri.getPath().substring(0, uri.getPath().length() - 4); // remove .git postfix
                    if (!path.startsWith("/")) {
                        path = "/" + path;
                    }
                    String page = null;
                    if (uri.getHost().equals("github.com")) {
                        page = "/pull/" + id; // url works for issues too
                    } else if (uri.getHost().contains("gitlab")) {
                        page = "/-/issues/" + id; // does gitlab auto link merge_requests ?
                    }
                    if (page != null) {
                        URI link = URI.create("https://" + uri.getHost() + path + page);
                        Desktop.getDesktop().browse(link);
                        return;
                    }
                }
            }
        } catch (IOException | URISyntaxException | GitException ex) {
            Exceptions.printStackTrace(ex);
        }
        StatusDisplayer.getDefault().setStatusText(Bundle.MSG_DefaultGitHyperlinkProvider_link_failed());
    }

}
