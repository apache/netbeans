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

package org.netbeans.libs.git.jgit.commands;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.eclipse.jgit.ignore.IgnoreNode.MatchResult;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.CoreConfig;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.util.FS;
import org.netbeans.libs.git.GitException;
import org.netbeans.libs.git.jgit.GitClassFactory;
import org.netbeans.libs.git.jgit.IgnoreRule;
import org.netbeans.libs.git.jgit.Utils;
import org.netbeans.libs.git.progress.FileListener;
import org.netbeans.libs.git.progress.ProgressMonitor;

/**
 *
 * @author ondra
 */
public abstract class IgnoreUnignoreCommand extends GitCommand {

    protected final File[] files;
    private final ProgressMonitor monitor;
    private final Set<File> ignoreFiles;
    private final FileListener listener;
    protected static final Logger LOG = Logger.getLogger(IgnoreUnignoreCommand.class.getName());
    
    public IgnoreUnignoreCommand (Repository repository, GitClassFactory gitFactory, File[] files, ProgressMonitor monitor, FileListener listener) {
        super(repository, gitFactory, monitor);
        this.files = files;
        this.monitor = monitor;
        this.ignoreFiles = new LinkedHashSet<>();
        this.listener = listener;
    }

    @Override
    protected boolean prepareCommand () throws GitException {
        boolean retval = super.prepareCommand();
        if (retval) {
            String message = null;
            if (files.length == 0) {
                message = "Files to ignore must not be empty.";
            } else if (Arrays.asList(files).contains(getRepository().getWorkTree())) {
                message = "Cannot ignore working tree root.";
            }
            if (message != null) {
                monitor.preparationsFailed(message);
                throw new GitException(message);
            }
        }
        return retval;
    }

    @Override
    protected void run () {
        File workTree = getRepository().getWorkTree();
        for (int i = 0; i < files.length && !monitor.isCanceled(); ++i) {
            File f = files[i];
            try {
                changeIgnoreStatus(f);
                listener.notifyFile(f, Utils.getRelativePath(workTree, f));
            } catch (IOException ex) {
                LOG.log(Level.INFO, ex.getLocalizedMessage(), ex);
                monitor.notifyError(ex.getLocalizedMessage());
            }
        }
    }
    
    private void changeIgnoreStatus (File f) throws IOException {
        File parent = f;
        boolean isDirectory = f.isDirectory() && (! Files.isSymbolicLink(f.toPath()));
        StringBuilder sb = new StringBuilder();
        if (isDirectory) {
            sb.append('/');
        }
        boolean cont = true;
        while (cont) {
            sb.insert(0, parent.getName()).insert(0, '/');
            parent = parent.getParentFile();
            String path = sb.toString();
            if (parent.equals(getRepository().getWorkTree())) {
                if (addStatement(new File(parent, Constants.DOT_GIT_IGNORE), path, isDirectory, false) && handleAdditionalIgnores(path, isDirectory)) {
                    addStatement(new File(parent, Constants.DOT_GIT_IGNORE), path, isDirectory, true);
                }
                cont = false;
            } else {
                cont = addStatement(new File(parent, Constants.DOT_GIT_IGNORE), path, isDirectory, false);
            }
        }
    }
    
    private boolean addStatement (File gitIgnore, String path, boolean isDirectory, boolean forceWrite) throws IOException {
        List<IgnoreRule> ignoreRules = parse(gitIgnore);
        return addStatement(ignoreRules, gitIgnore, path, isDirectory, forceWrite, true) == MatchResult.CHECK_PARENT;
    }

    protected final void save(File gitIgnore, List<IgnoreRule> ignoreRules) throws IOException {
        try {
            Path tmpFile = Files.createTempFile(gitIgnore.getParentFile().toPath(), Constants.DOT_GIT_IGNORE, "tmp"); //NOI18N
            try {
                String lineSeparator = probeLineSeparator(gitIgnore.toPath());
                try (BufferedWriter writer = Files.newBufferedWriter(tmpFile)) {
                    for (IgnoreRule rule : ignoreRules) {
                        writer.write(rule.getPattern(false));
                        writer.write(lineSeparator);
                    }
                }
                Files.move(tmpFile, gitIgnore.toPath(), StandardCopyOption.REPLACE_EXISTING);
            } finally {
                Files.deleteIfExists(tmpFile);
            }
        } catch (IOException ex) {
            throw new IOException("Cannot update .gitignore at " + gitIgnore.getAbsolutePath(), ex);
        }
        ignoreFiles.add(gitIgnore);
    }

    private List<IgnoreRule> parse(File gitIgnore) throws IOException {
        if (gitIgnore.exists()) {
            try (Stream<String> lines = Files.lines(gitIgnore.toPath())) {
                return lines.map(IgnoreRule::new)
                            .collect(Collectors.toCollection(LinkedList::new));
            }
        }
        return new LinkedList<>();
    }

    @SuppressWarnings("NestedAssignment")
    private static String probeLineSeparator(Path file) throws IOException {
        if (Files.exists(file)) {
            try (BufferedReader br = Files.newBufferedReader(file)) {
                int current;
                int last = -1;
                while ((current = br.read()) != -1) {
                    if (current == '\n') {
                        return last == '\r' ? "\r\n" : "\n";
                    }
                    last = current;
                }
            }
        }
        return System.lineSeparator();
    }

    public File[] getModifiedIgnoreFiles () {
        return ignoreFiles.toArray(File[]::new);
    }

    protected abstract MatchResult addStatement (List<IgnoreRule> ignoreRules, File gitIgnore, String path, boolean isDirectory, boolean forceWrite, boolean writable) throws IOException;

    protected static String escapeChars (String path) {
        return path.replace("[", "[[]").replace("*", "[*]").replace("?", "[?]"); //NOI18N
    }

    protected final MatchResult checkExcludeFile (String path, boolean isDirectory) throws IOException {
        File excludeFile = new File(getRepository().getDirectory(), "info/exclude");
        List<IgnoreRule> ignoreRules = parse(excludeFile);
        return addStatement(ignoreRules, excludeFile, path, isDirectory, false, true);
    }

    protected final MatchResult checkGlobalExcludeFile (String path, boolean directory) throws IOException {
        File excludeFile = getGlobalExcludeFile();
        if (excludeFile != null && excludeFile.canRead()) {
            List<IgnoreRule> ignoreRules = parse(excludeFile);
            return addStatement(ignoreRules, excludeFile, path, directory, false, false);
        }
        return MatchResult.NOT_IGNORED;
    }

    private File getGlobalExcludeFile () {
        Repository repository = getRepository();
        String path = repository.getConfig().get(CoreConfig.KEY).getExcludesFile();
        File excludesfile = null;
        FS fs = repository.getFS();
        if (path != null) {
            if (path.startsWith("~/")) {
                excludesfile = fs.resolve(fs.userHome(), path.substring(2));
            } else {
                excludesfile = fs.resolve(null, path);
            }
        }
        return excludesfile;
    }

    protected abstract boolean handleAdditionalIgnores (String path, boolean directory) throws IOException;

}
