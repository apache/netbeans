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

package org.netbeans.modules.mercurial.api;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javax.swing.SwingUtilities;
import org.netbeans.modules.mercurial.FileInformation;
import org.netbeans.modules.mercurial.FileStatusCache;
import org.netbeans.modules.mercurial.HgFileNode;
import org.netbeans.modules.mercurial.HgModuleConfig;
import org.netbeans.modules.mercurial.HgProgressSupport;
import org.netbeans.modules.mercurial.OutputLogger;
import org.netbeans.modules.versioning.hooks.HgHook;
import org.netbeans.modules.mercurial.ui.clone.CloneAction;
import org.netbeans.modules.mercurial.ui.commit.CommitAction;
import org.netbeans.modules.mercurial.ui.commit.CommitOptions;
import org.netbeans.modules.mercurial.ui.push.PushAction;
import org.netbeans.modules.mercurial.ui.repository.HgURL;
import org.netbeans.modules.mercurial.ui.repository.RepositoryConnection;
import org.netbeans.modules.mercurial.ui.wizards.CloneWizardAction;
import org.netbeans.modules.mercurial.util.HgCommand;
import org.netbeans.modules.mercurial.util.HgUtils;
import org.netbeans.modules.versioning.util.VCSBugtrackingAccessor;
import org.openide.util.Lookup;
import org.openide.util.NbPreferences;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Marian Petras
 */
public class Mercurial {

    /**
     * Clones the given repository to the given directory. The method blocks
     * until the whole checkout is done. Do not call in AWT.
     *
     * @param  repositoryUrl  URL of the Mercurial repository to be cloned
     * @param  targetDir  target where  cloned repository should be created
     * @param  cloneName  name of the cloned repository
     *                    (name of the root folder of the clone)
     * @param  defaultPull  initial URL for pulling updates
     * @param  defaultPush  initial URL for pushing updates
     * @param  scanForProjects true will start project scan after the clone finishes
     */
    public static void cloneRepository(String repositoryUrl,
                                       File targetDir,
                                       String cloneName,
                                       String defaultPull,
                                       String defaultPush,
                                       boolean scanForProjects) throws MalformedURLException {
        assert !SwingUtilities.isEventDispatchThread() : "Accessing remote repository. Do not call in awt!";
        cloneRepository(repositoryUrl,
                        targetDir,
                        cloneName,
                        defaultPull,
                        defaultPush,
                        null,
                        null,
                        scanForProjects);
    }

    /**
     * Clones the given repository to the given directory. The method blocks
     * until the whole checkout is done. Do not call in AWT.
     *
     * @param  repositoryUrl  URL of the Mercurial repository to be cloned
     * @param  targetDir  target where  cloned repository should be created
     * @param  cloneName  name of the cloned repository
     *                    (name of the root folder of the clone)
     * @param  defaultPull  initial URL for pulling updates
     * @param  defaultPush  initial URL for pushing updates
     * @param  username  username for access to the given repository
     * @param  password  password for access to the given repository
     * @param  scanForProjects true will start scanning for projects after the clone finishes
     */
    public static void cloneRepository(String repositoryUrl,
                                       File targetDir,
                                       String cloneName,
                                       String pullUrl,
                                       String pushUrl,
                                       String username,
                                       String password,
                                       boolean scanForProjects)
            throws MalformedURLException {
        assert !SwingUtilities.isEventDispatchThread() : "Accessing remote repository. Do not call in awt!";

        if(!isClientAvailable(true)) {
            org.netbeans.modules.mercurial.Mercurial.LOG.log(Level.WARNING, "Mercurial client is unavailable");
            return;
        }

        if (repositoryUrl == null) {
            throw new IllegalArgumentException("repository URL is null"); //NOI18N
        }

        HgURL hgUrl, pullPath, pushPath;
        try {
            hgUrl = new HgURL(repositoryUrl, username, password != null ? password.toCharArray() : null);
        } catch (URISyntaxException ex) {
            throw new MalformedURLException(ex.getMessage());
        }

        pullUrl = getNonEmptyString(pullUrl);
        pushUrl = getNonEmptyString(pushUrl);
        try {
            pullPath = (pullUrl != null) ? new HgURL(pullUrl) : null;
        } catch (URISyntaxException ex) {
            throw new MalformedURLException("Invalid pull URL: " + ex.getMessage());
        }
        try {
            pushPath = (pushUrl != null) ? new HgURL(pushUrl) : null;
        } catch (URISyntaxException ex) {
            throw new MalformedURLException("Invalid push URL: " + ex.getMessage());
        }

        File cloneFile = new File(targetDir, cloneName);
        CloneAction.performClone(hgUrl,
                                 cloneFile,
                                 true,
                                 null,
                                 pullPath,
                                 pushPath,
                                 scanForProjects).waitFinished();

        try {
            storeWorkingDir(new URL(repositoryUrl), targetDir.toURI().toURL());
        } catch (Exception e) {
            Logger.getLogger(Mercurial.class.getName()).log(Level.FINE, "Cannot store mercurial workdir preferences", e);
        }

        VCSBugtrackingAccessor bugtrackingSupport = Lookup.getDefault().lookup(VCSBugtrackingAccessor.class);
        if(bugtrackingSupport != null) {
            bugtrackingSupport.setFirmAssociations(new File[]{cloneFile}, repositoryUrl);
        }
    }

    /**
     * Commits all local changes under the given roots
     *
     * @param roots
     * @param message
     * @throws IOException when an error occurrs
     */
    public static void commit(final File[] roots, final String message) {
        assert !SwingUtilities.isEventDispatchThread() : "Accessing remote repository. Do not call in awt!";

        if(!isClientAvailable(true)) {
            org.netbeans.modules.mercurial.Mercurial.LOG.log(Level.WARNING, "Mercurial client is unavailable");
            return;
        }
        Set<File> repositories = HgUtils.getRepositoryRoots(new HashSet<File>(Arrays.asList(roots)));
        org.netbeans.modules.mercurial.Mercurial hg = org.netbeans.modules.mercurial.Mercurial.getInstance();
        if (repositories.size() == 0) {
            // this is necessary because kenai seems to copy metadata from a temp folder and the project would be treated as unversioned
            hg.versionedFilesChanged();
            repositories = HgUtils.getRepositoryRoots(new HashSet<File>(Arrays.asList(roots)));
        }
        if (repositories.size() != 1) {
            org.netbeans.modules.mercurial.Mercurial.LOG.log(Level.WARNING, "Committing for {0} repositories", repositories.size());
            return;
        }
        final File repository = repositories.iterator().next();
        final Set<File> rootFiles = new HashSet<File>(Arrays.asList(roots));

        FileStatusCache cache = hg.getFileStatusCache();
        cache.refreshAllRoots(Collections.singletonMap(repository, rootFiles));
        File[] files = cache.listFiles(roots, FileInformation.STATUS_LOCAL_CHANGE);

        if (files.length == 0) {
            return;
        }

        HgFileNode[] nodes = new HgFileNode[files.length];
        for (int i = 0; i < files.length; i++) {
            nodes[i] = new HgFileNode(repository, files[i]);
        }
        CommitOptions[] commitOptions = HgUtils.createDefaultCommitOptions(nodes, HgModuleConfig.getDefault().getExludeNewFiles());
        final HashMap<HgFileNode, CommitOptions> commitFiles = new HashMap<HgFileNode, CommitOptions>(nodes.length);
        for (int i = 0; i < nodes.length; i++) {
            commitFiles.put(nodes[i], commitOptions[i]);
        }

        RequestProcessor rp = hg.getRequestProcessor(repository);
        HgProgressSupport support = new HgProgressSupport() {
            public void perform() {
                OutputLogger logger = getLogger();
                CommitAction.performCommit(message, commitFiles, Collections.singletonMap(repository, rootFiles), this, logger, Collections.<HgHook>emptyList());
            }
        };
        support.start(rp, repository, org.openide.util.NbBundle.getMessage(CommitAction.class, "LBL_Commit_Progress")).waitFinished(); // NOI18N
    }

    /**
     * Pushes outgoing changes to default push repository
     *
     * @param repository
     * @throws IOException when an error occurrs
     */
    public static void pushToDefault (final File repository) {
        assert !SwingUtilities.isEventDispatchThread() : "Accessing remote repository. Do not call in awt!"; //NOI18N

        if(!isClientAvailable(true)) {
            org.netbeans.modules.mercurial.Mercurial.LOG.log(Level.WARNING, "Mercurial client is unavailable"); //NOI18N
            return;
        }

        if (repository == null) {
            throw new IllegalArgumentException("repository is null");   //NOI18N
        }

        RequestProcessor rp = org.netbeans.modules.mercurial.Mercurial.getInstance().getRequestProcessor(repository);
        HgProgressSupport support = new HgProgressSupport() {
            @Override
            public void perform() {
                PushAction.getDefaultAndPerformPush(repository, null, null, this.getLogger());
            }
        };
        support.start(rp, repository, org.openide.util.NbBundle.getMessage(PushAction.class, "MSG_PUSH_PROGRESS")).waitFinished(); //NOI18N
    }

    /**
     * Trims leading and trailing spaces from the given string the same way
     * as method String.trim(). The difference is that if the passed string
     * is {@code null} or if the string contains just spaces, {@code null}
     * is returned.
     *
     * @param  s  string to trim the spaces off
     * @return  trimmed string, or {@code null} if the given string was
     *          {@code null} or if the given string contained just spaces
     */
    private static String getNonEmptyString(String s) {
        if (s == null) {
            return null;
        }

        s = s.trim();
        return (s.length() != 0) ? s : null;
    }

    private static final String WORKINGDIR_KEY_PREFIX = "working.dir."; //NOI18N

    /**
     * Stores working directory for specified remote root
     * into NetBeans preferences.
     * These are later used in kenai.ui module
     */
    private static void storeWorkingDir(URL remoteUrl, URL localFolder) {
        Preferences prf = NbPreferences.forModule(Mercurial.class);
        prf.put(WORKINGDIR_KEY_PREFIX + remoteUrl, localFolder.toString());
    }    

    /**
     * Adds a remote url for the combos used in Clone wizard
     *
     * @param url
     * @throws java.net.MalformedURLException
     */
    public static void addRecentUrl(String url) throws MalformedURLException {
        RepositoryConnection rc;
        try {
            rc = new RepositoryConnection(url);
        } catch (URISyntaxException ex) {
            org.netbeans.modules.mercurial.Mercurial.LOG.log(
                    Level.INFO,
                    "Could not add URL to the list of recent URLs:",    //NOI18N
                    ex);
            return;
        }
        HgModuleConfig.getDefault().insertRecentUrl(rc);
    }

    /**
     * Tries to resolve the given URL and determine if the URL represents a mercurial repository.
     * Should not be called inside AWT, this accesses network.
     * @param url repository URL
     * @return true if given url denotes an existing mercurial repository
     */
    public static boolean isRepository (final String url) {
        if(!isClientAvailable(false)) {
            org.netbeans.modules.mercurial.Mercurial.LOG.log(Level.INFO, "Mercurial client is unavailable");
            return false;
        }
        boolean retval = false;
        HgURL hgUrl = null;
        try {
            hgUrl = new HgURL(url);
        } catch (URISyntaxException ex) {
            org.netbeans.modules.mercurial.Mercurial.LOG.log(Level.FINE, "Invalid mercurial url " + url, ex);
        }

        if (hgUrl != null) {
            retval = HgCommand.checkRemoteRepository(hgUrl.toHgCommandUrlString());
        }

        return retval;
    }

    /**
     * Opens standard clone wizard. Is not blocking, does not wait for the clone task to finish
     * @param url repository url to checkout
     * @throws java.net.MalformedURLException in case the url is invalid
     */
    public static void openCloneWizard (final String url) throws MalformedURLException {
        openCloneWizard(url, false);
    }
    
    /**
     * Opens standard clone wizard
     * @param url repository url to checkout
     * @return destination folder
     * @throws java.net.MalformedURLException in case the url is invalid
     */
    public static File openCloneWizard (final String url, boolean waitFinished) throws MalformedURLException {
        if(!isClientAvailable(true)) {
            org.netbeans.modules.mercurial.Mercurial.LOG.log(Level.WARNING, "Mercurial client is unavailable");
            return null;
        }
        addRecentUrl(url);
        CloneWizardAction wiz = CloneWizardAction.getInstance();
        return wiz.performClone(waitFinished);
    }

    /**
     * Returns true if mercurial client is installed and has a supported version.<br/>
     * Does not show any warning dialog.
     * @return true if mercurial client is available.
     */
    public static boolean isClientAvailable() {
        return isClientAvailable(false);
    }

    public static boolean isClientAvailable (boolean notifyUI) {
        return org.netbeans.modules.mercurial.Mercurial.getInstance().isAvailable(true, notifyUI);
    }
}
