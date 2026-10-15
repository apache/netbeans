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

package org.netbeans.modules.git;

import org.netbeans.modules.git.client.GitProgressSupport;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.libs.git.GitException;
import org.netbeans.libs.git.GitRepository;
import org.netbeans.modules.git.client.CredentialsCallback;
import org.netbeans.modules.git.client.GitClient;
import org.netbeans.modules.git.ui.shelve.ShelveChangesAction;
import org.netbeans.modules.git.utils.GitUtils;
import org.netbeans.modules.versioning.shelve.ShelveChangesActionsRegistry;
import org.netbeans.modules.versioning.spi.VersioningSupport;
import org.netbeans.modules.versioning.util.RootsToFile;
import org.netbeans.modules.versioning.util.Utils;
import org.netbeans.modules.versioning.util.VCSHyperlinkProvider;
import org.openide.util.Lookup;
import org.openide.util.Lookup.Result;
import org.openide.util.RequestProcessor;

/**
 *
 * @author ondra
 */
public final class Git {

    private static Git instance;
    private Annotator annotator;
    private FilesystemInterceptor interceptor;
    public static final Logger LOG = Logger.getLogger("org.netbeans.modules.git"); //NOI18N
    public static final Logger STATUS_LOG = Logger.getLogger("org.netbeans.modules.git.status"); //NOI18N;
    private final PropertyChangeSupport support = new PropertyChangeSupport(this);
    private FileStatusCache fileStatusCache;
    private HashMap<File, RequestProcessor> processorsToUrl;
    public static final String PROP_ANNOTATIONS_CHANGED = "annotationsChanged"; // NOI18N
    static final String PROP_VERSIONED_FILES_CHANGED = "versionedFilesChanged"; // NOI18N

    private RootsToFile rootsToFile;
    private GitVCS gitVCS;
    private Result<? extends VCSHyperlinkProvider> hpResult;
    private HistoryProvider historyProvider;
    
    private Git () {}

    public static synchronized Git getInstance () {
        if (instance == null) {
            instance = new Git();
            instance.init();
        }
        return instance;
    }

    private void init() {
        fileStatusCache = new FileStatusCache();
        annotator = new Annotator();
        interceptor = new FilesystemInterceptor();

        int statisticsFrequency;
        String s = System.getProperty("git.root.stat.frequency", "0"); //NOI18N
        try {
            statisticsFrequency = Integer.parseInt(s);
        } catch (NumberFormatException ex) {
            statisticsFrequency = 0;
        }
        rootsToFile = new RootsToFile(new RootsToFile.Callback() {
            @Override
            public boolean repositoryExistsFor (File file) {
                return GitUtils.repositoryExistsFor(file);
            }

            @Override
            public File getTopmostManagedAncestor (File file) {
                // skip exceptions - hidden folders, we already think the file is under a repository.
                return Git.this.getTopmostManagedAncestor(file, true);
            }
        }, Logger.getLogger("org.netbeans.modules.git.RootsToFile"), statisticsFrequency); //NOI18N
        ModuleLifecycleManager.getInstance().disableOtherModules();
    }

    void registerGitVCS(final GitVCS gitVCS) {
        this.gitVCS = gitVCS;
        fileStatusCache.addPropertyChangeListener(gitVCS);
        addPropertyChangeListener(gitVCS);
        getRequestProcessor().post(new Runnable() {
            @Override
            public void run () {
                ShelveChangesActionsRegistry.getInstance().registerAction(gitVCS, ShelveChangesAction.getProvider());
            }
        });
    }

    public Annotator getVCSAnnotator() {
        return annotator;
    }

    FilesystemInterceptor getVCSInterceptor() {
        return interceptor;
    }

    void getOriginalFile (File workingCopy, File originalFile) {
        File repository = getRepositoryRoot(workingCopy);
        if (repository != null) {
            GitClient client = null;
            try {
                client = getClient(repository);
                FileOutputStream fos = new FileOutputStream(originalFile);
                boolean ok;                
                try {
                    ok = client.catFile(workingCopy, GitUtils.HEAD, fos, GitUtils.NULL_PROGRESS_MONITOR);
                } finally {
                    fos.close();
                }
                if (!ok) {
                    originalFile.delete();
                }
            } catch (java.io.FileNotFoundException ex) {
                LOG.log(Level.SEVERE, "Parent folder [{0}] does not exist", originalFile.getParentFile()); //NOI18N
                LOG.log(Level.SEVERE, null, ex);
            } catch (GitException.MissingObjectException ex) {
                LOG.log(Level.FINE, null, ex); //NOI18N
                originalFile.delete();
            } catch (GitException ex) {
                LOG.log(Level.INFO, "Error retrieving file", ex); //NOI18N
                originalFile.delete();
            } catch (IOException ex) {
                LOG.log(Level.INFO, "IO exception", ex); //NOI18N
            } finally {
                if (client != null) {
                    client.release();
                }
            }
        }
    }

    /**
     * Tests whether a file or directory should receive the STATUS_NOTVERSIONED_NOTMANAGED status.

     * @param file a file or directory
     * @return false if the file should receive the STATUS_NOTVERSIONED_NOTMANAGED status, true otherwise
     */
    public boolean isManaged(File file) {
        return VersioningSupport.getOwner(file) instanceof GitVCS && !GitUtils.isPartOfGitMetadata(file);
    }

    public FileStatusCache getFileStatusCache() {
        return fileStatusCache;
    }

    public File getRepositoryRoot (File file) {
        return rootsToFile.getRepositoryRoot(file);
    }

    public GitClient getClient (File repository) throws GitException {
        return getClient(repository, null);
    }

    public GitClient getClient (File repository, GitProgressSupport progressSupport) throws GitException {
        return getClient(repository, progressSupport, true);
    }
    
    public GitClient getClient (File repository, GitProgressSupport progressSupport, boolean handleAuthenticationIssues) throws GitException {
        GitClient client = new GitClient(singleInstanceRepositoryRoot(repository), progressSupport, handleAuthenticationIssues);
        client.setCallback(new CredentialsCallback());
        return client;
    }
    
    public GitRepository getRepository (File repository) throws GitException {
        return GitRepository.getInstance(singleInstanceRepositoryRoot(repository));
    }

    public RequestProcessor getRequestProcessor() {
        return getRequestProcessor(null);
    }

    /**
     * @param  repositoryRoot  repository root or {@code null}
     */
    public RequestProcessor getRequestProcessor (File repositoryRoot) {
        if(processorsToUrl == null) {
            processorsToUrl = new HashMap<File, RequestProcessor>();
        }

        RequestProcessor rp = processorsToUrl.get(repositoryRoot);
        if (rp == null) {
            if(repositoryRoot == null) {
                String rpName = "Git - ANY_KEY";//NOI18N
                rp = new RequestProcessor(rpName, 50, true);                
            } else {    
                String rpName = "Git - " + repositoryRoot.toString();//NOI18N
                rp = new RequestProcessor(rpName, 1, true);
            }
            processorsToUrl.put(repositoryRoot, rp);
        }
        return rp;
    }

    public void refreshAllAnnotations() {
        support.firePropertyChange(PROP_ANNOTATIONS_CHANGED, null, null);
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        support.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        support.removePropertyChangeListener(listener);
    }

    public void headChanged (Set<File> files) {
        assert gitVCS != null;
        gitVCS.refreshStatus(files);
    }

    public void versionedFilesChanged () {
        clearAncestorCaches();
        support.firePropertyChange(PROP_VERSIONED_FILES_CHANGED, null, null);
    }

    /**
     * Runs a given callable and disable listening for external repository events for the time the callable is running.
     * Refreshes cached modification timestamp of metadata for the given git repository after.
     * @param callable code to run
     * @param repository
     * @param commandName name of the git command if available
     */
    public <T> T runWithoutExternalEvents(File repository, String commandName, Callable<T> callable) throws Exception {
        return getVCSInterceptor().runWithoutExternalEvents(repository, commandName, callable);
    }

    /**
     * Returns a set of known repository roots (those visible or open in IDE)
     * @param repositoryRoot
     * @return
     */
    public Set<File> getSeenRoots (File repositoryRoot) {
        return getVCSInterceptor().getSeenRoots(repositoryRoot);
    }
    
    private Set<File> knownRoots = Collections.synchronizedSet(new HashSet<File>());
    private final Set<File> unversionedParents = Collections.synchronizedSet(new HashSet<File>(20));

    public File getTopmostManagedAncestor (File file) {
        return getTopmostManagedAncestor(file, false);
    }
    
    private File getTopmostManagedAncestor (File file, boolean noExceptions) {
        long t = System.currentTimeMillis();
        LOG.log(Level.FINE, "getTopmostManagedParent {0}", new Object[] { file });
        if(unversionedParents.contains(file)) {
            LOG.fine(" cached as unversioned");
            return null;
        }
        LOG.log(Level.FINE, "getTopmostManagedParent {0}", new Object[] { file });
        File parent = getKnownParent(file);
        if(parent != null) {
            LOG.log(Level.FINE, "  getTopmostManagedParent returning known parent {0}", parent);
            return parent;
        }

        if (GitUtils.isPartOfGitMetadata(file)) {
            for (;file != null; file = file.getParentFile()) {
                if (GitUtils.isAdministrative(file)) {
                    file = file.getParentFile();
                    // the parent folder of .hg metadata cannot be unversioned, it's nonsense
                    unversionedParents.remove(file);
                    break;
                }
            }
        }
        Set<File> done = new HashSet<File>();
        File topmost = null;
        for (;file != null; file = file.getParentFile()) {
            if(unversionedParents.contains(file)) {
                LOG.log(Level.FINE, " already known as unversioned {0}", new Object[] { file });
                break;
            }
            if (!noExceptions && VersioningSupport.isExcluded(file)) break;
            // is the folder a special one where metadata should not be looked for?
            boolean forbiddenFolder = Utils.isForbiddenFolder(file);
            if (!forbiddenFolder && GitUtils.repositoryExistsFor(file)) {
                LOG.log(Level.FINE, " found managed parent {0}", new Object[] { file });
                done.clear();   // all folders added before must be removed, they ARE in fact managed by git
                topmost =  file;
                if (topmost.getParentFile() == null) {
                    LOG.log(Level.WARNING, "found managed root folder {0}", file); //NOI18N
                }
            } else {
                LOG.log(Level.FINE, " found unversioned {0}", new Object[] { file });
                if(file.exists()) { // could be created later ...
                    done.add(file);
                }
            }
        }
        if(done.size() > 0) {
            LOG.log(Level.FINE, " storing unversioned");
            unversionedParents.addAll(done);
        }
        if(LOG.isLoggable(Level.FINE)) {
            LOG.log(Level.FINE, " getTopmostManagedParent returns {0} after {1} millis", new Object[] { topmost, System.currentTimeMillis() - t });
        }
        if(topmost != null) {
            if (knownRoots.add(topmost)) {
                String homeDir = System.getProperty("user.home"); //NOI18N
                if (homeDir != null && homeDir.startsWith(topmost.getAbsolutePath())) {
                    LOG.log(Level.WARNING, "Home folder {0} lies under a git versioned root {1}. " //NOI18N
                            + "Expecting lots of performance issues.", new Object[] { homeDir, topmost }); //NOI18N
                }
            }
        }

        return topmost;
    }

    private File singleInstanceRepositoryRoot (File repository) {
        // get the only instance for the repository folder, so we can synchronize on it
        File repositoryFolder = getRepositoryRoot(repository);
        if (repositoryFolder != null && repository.equals(repositoryFolder)) {
            repository = repositoryFolder;
        }
        return repository;
    }
    
    private File getKnownParent(File file) {
        File[] roots = knownRoots.toArray(new File[0]);
        File knownParent = null;
        for (File r : roots) {
            if(!VersioningSupport.isExcluded(file) && Utils.isAncestorOrEqual(r, file) && (knownParent == null || Utils.isAncestorOrEqual(knownParent, r))) {
                knownParent = r;
            }
        }
        return knownParent;
    }

    public void clearAncestorCaches() {
        unversionedParents.clear();
        knownRoots.clear();
        rootsToFile.clear();
    }
    
    /**
     *
     * @return registered hyperlink providers
     */
    public List<VCSHyperlinkProvider> getHyperlinkProviders() {
        if (hpResult == null) {
            hpResult = (Result<? extends VCSHyperlinkProvider>) Lookup.getDefault().lookupResult(VCSHyperlinkProvider.class);
        }
        if (hpResult == null) {
            return Collections.<VCSHyperlinkProvider>emptyList();
        }
        Collection<? extends VCSHyperlinkProvider> providersCol = hpResult.allInstances();
        List<VCSHyperlinkProvider> providersList = new ArrayList<VCSHyperlinkProvider>(providersCol.size());
        providersList.addAll(providersCol);
        return Collections.unmodifiableList(providersList);
    }

    public Collection<File> getCreatedFolders () {
        return getVCSInterceptor().getCreatedFolders();
    }

    public HistoryProvider getHistoryProvider () {
        if (historyProvider == null) {
            historyProvider = new HistoryProvider();
        }
        return historyProvider;
    }
}
