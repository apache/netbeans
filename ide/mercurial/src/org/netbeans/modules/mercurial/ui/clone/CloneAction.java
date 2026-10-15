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
package org.netbeans.modules.mercurial.ui.clone;

import org.netbeans.modules.versioning.util.Utils;
import java.net.URISyntaxException;
import java.util.MissingResourceException;
import org.netbeans.modules.versioning.spi.VCSContext;
import javax.swing.*;
import java.io.File;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.mercurial.HgException;
import org.netbeans.modules.mercurial.HgProgressSupport;
import org.netbeans.modules.mercurial.Mercurial;
import org.netbeans.modules.mercurial.OutputLogger;
import org.netbeans.modules.mercurial.config.HgConfigFiles;
import org.netbeans.modules.mercurial.util.HgCommand;
import org.netbeans.modules.mercurial.util.HgUtils;
import org.netbeans.modules.mercurial.util.HgProjectUtils;
import org.netbeans.modules.mercurial.ui.actions.ContextAction;
import org.netbeans.modules.mercurial.ui.repository.HgURL;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.Node;
import org.openide.util.Cancellable;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;
import static org.netbeans.modules.mercurial.ui.properties.HgProperties.HGPROPNAME_DEFAULT_PULL;
import static org.netbeans.modules.mercurial.ui.properties.HgProperties.HGPROPNAME_DEFAULT_PUSH;

/**
 * Clone action for mercurial: 
 * hg clone - Create a copy of an existing repository in a new directory.
 * 
 * @author John Rice
 */
@NbBundle.Messages({
    "# {0} - repository folder name",
    "CTL_MenuItem_CloneLocal=&Clone - {0}",
    "CTL_MenuItem_CloneRepository=&Clone - Selected Repository..."
})
public class CloneAction extends ContextAction {
    @Override
    protected boolean enable(Node[] nodes) {
        VCSContext context = HgUtils.getCurrentContext(nodes);
        return HgUtils.isFromHgRepository(context);
    }

    @Override
    protected String getBaseName(Node[] nodes) {
        VCSContext ctx = HgUtils.getCurrentContext(nodes);
        Set<File> roots = HgUtils.getRepositoryRoots(ctx);
        return roots.size() == 1 ? "CTL_MenuItem_CloneLocal" : "CTL_MenuItem_CloneRepository"; // NOI18N
    }

    @Override
    public String getName(String role, Node[] activatedNodes) {
        VCSContext ctx = HgUtils.getCurrentContext(activatedNodes);
        Set<File> roots = HgUtils.getRepositoryRoots(ctx);
        String name = getBaseName(activatedNodes);
        return roots.size() == 1 ? NbBundle.getMessage(CloneAction.class, name, roots.iterator().next().getName())
                : NbBundle.getMessage(CloneAction.class, name);
    }

    @Override
    protected void performContextAction (final Node[] nodes) {
        HgUtils.runIfHgAvailable(new Runnable() {
            @Override
            public void run () {
                Utils.logVCSActionEvent("HG"); //NOI18N
                VCSContext context = HgUtils.getCurrentContext(nodes);
                final File roots[] = HgUtils.getActionRoots(context);
                if (roots == null || roots.length == 0) return;
                final File root = Mercurial.getInstance().getRepositoryRoot(roots[0]);

                // Get unused Clone Folder name
                File tmp = root.getParentFile();
                File projFile = Utils.getProjectFile(context);
                String folderName = root.getName();
                Boolean projIsRepos = true;
                if (!root.equals(projFile))  {
                    // Mercurial Repository is not the same as project root
                    projIsRepos = false;
                }
                for(int i = 0; i < 10000; i++){
                    if (!new File(tmp,folderName+"_clone"+i).exists()){ // NOI18N
                        tmp = new File(tmp, folderName +"_clone"+i); // NOI18N
                        break;
                    }
                }
                Clone clone = new Clone(root, tmp);
                if (!clone.showDialog()) {
                    return;
                }
                performClone(new HgURL(root), clone.getTargetDir(), projIsRepos, projFile, true, null, null, true);
            }
        });
    }

    /**
     * 
     * @param source password is nulled
     * @param target
     * @param projIsRepos
     * @param projFile
     * @param pullPath password is nulled
     * @param pushPath password is nulled
     * @param scanForProjects
     * @return
     */
    public static RequestProcessor.Task performClone(final HgURL source, final File target, boolean projIsRepos,
            File projFile, final HgURL pullPath, final HgURL pushPath, boolean scanForProjects) {
        return performClone(source, target, projIsRepos, projFile, false, pullPath, pushPath, scanForProjects);
    }

    private static RequestProcessor.Task performClone(final HgURL source, final File target,
            final boolean projIsRepos, final File projFile, final boolean isLocalClone, final HgURL pullPath, final HgURL pushPath,
            final boolean scanForProjects) {

        RequestProcessor rp = Mercurial.getInstance().getRequestProcessor(source);
        final HgProgressSupport support = new HgProgressSupport() {
            @Override
            public void perform() {
                String projName = (projFile != null)
                                  ? HgProjectUtils.getProjectName(projFile)
                                  : null;

                OutputLogger logger = getLogger();
                try {
                    // TODO: We need to annotate the cloned project 
                    // See http://qa.netbeans.org/issues/show_bug.cgi?id=112870
                    logger.outputInRed(
                            NbBundle.getMessage(CloneAction.class,
                            "MSG_CLONE_TITLE")); // NOI18N
                    logger.outputInRed(
                            NbBundle.getMessage(CloneAction.class,
                            "MSG_CLONE_TITLE_SEP")); // NOI18N
                    List<String> list = HgUtils.runWithoutIndexing(new Callable<List<String>>() {
                        @Override
                        public List<String> call () throws Exception {
                            return HgCommand.doClone(source, target, getLogger());
                        }
                    }, target);
                    if (!new File(HgUtils.getHgFolderForRoot(target), HgConfigFiles.HG_RC_FILE).canRead() || list.contains("transaction abort!")) { //NOI18N
                        // does not seem to be really cloned
                        logger.output(list);
                        Mercurial.LOG.log(Level.WARNING, "Hg clone seems to fail: {0}", list); //NOI18N
                        return;
                    }
                    if(list != null && !list.isEmpty()){
                        HgUtils.createIgnored(target);
                        logger.output(list);
               
                        if (projName != null) {
                            logger.outputInRed(
                                    NbBundle.getMessage(CloneAction.class,
                                    "MSG_CLONE_FROM", projName, source)); // NOI18N
                            logger.outputInRed(
                                    NbBundle.getMessage(CloneAction.class,
                                    "MSG_CLONE_TO", projName, target)); // NOI18N
                        } else {
                            logger.outputInRed(
                                    NbBundle.getMessage(CloneAction.class,
                                    "MSG_EXTERNAL_CLONE_FROM", source)); // NOI18N
                            logger.outputInRed(
                                    NbBundle.getMessage(CloneAction.class,
                                    "MSG_EXTERNAL_CLONE_TO", target)); // NOI18N

                        }
                        logger.output(""); // NOI18N

                        if (isLocalClone){
                            Mercurial hg = Mercurial.getInstance();
                            ProjectManager projectManager = ProjectManager.getDefault();
                            File normalizedCloneFolder = FileUtil.normalizeFile(target);
                            File cloneProjFile;
                            if (!projIsRepos) {
                                String name = (projFile != null)
                                              ? projFile.getAbsolutePath().substring(source.getPath().length() + 1)
                                              : target.getAbsolutePath();
                                cloneProjFile = new File (normalizedCloneFolder, name);
                            } else {
                                cloneProjFile = normalizedCloneFolder;
                            }
                            openProject(cloneProjFile, projectManager, hg);
                        } else if (scanForProjects) {
                            CloneCompleted cc = new CloneCompleted(target);
                            if (!isCanceled()) {
                                cc.scanForProjects(this);
                            }
                        }
                    }
                    HgConfigFiles hgConfigFiles = new HgConfigFiles(target);
                    if (hgConfigFiles.getException() == null) {
                        Utils.logVCSExternalRepository("HG", source.toHgCommandUrlStringWithoutUserInfo()); //NOI18N
                        initializeDefaultPullPushUrl(hgConfigFiles);
                    } else {
                        Mercurial.LOG.log(Level.WARNING, "{0}: Cannot set default push and pull path", this.getClass().getName()); // NOI18N
                        Mercurial.LOG.log(Level.INFO, null, hgConfigFiles.getException());
                    }
                } catch (HgException.HgCommandCanceledException ex) {
                    // canceled by user, do nothing
                } catch (HgException ex) {
                    HgUtils.notifyException(ex);
                }finally {    
                    if(!isLocalClone){
                        logger.outputInRed(NbBundle.getMessage(CloneAction.class, "MSG_CLONE_DONE")); // NOI18N
                        logger.output(""); // NOI18N
                    }
                    source.clearPassword();
                    if (pullPath != null) {
                        pullPath.clearPassword();
                    }
                    if (pushPath != null) {
                        pushPath.clearPassword();
                    }
                }
            }

            private void initializeDefaultPullPushUrl(HgConfigFiles hgConfigFiles) {
                /*
                 * Mercurial itself sets just "default" in 'hgrc' file.
                 * We make sure that "default-push" is set, too - see
                 * bug #125835 ("default-push should be set
                 * automatically").
                 */
                String defaultPull = hgConfigFiles.getDefaultPull(false);
                if (defaultPull == null) {
                    return;
                }
                HgURL defaultPullURL;
                try {
                    defaultPullURL = new HgURL(defaultPull);
                    if ((pullPath == null) && (pushPath == null)) {
                        hgConfigFiles.setProperty(HGPROPNAME_DEFAULT_PUSH, defaultPull);
                    } else if ((pullPath != null) && (pushPath == null)) {
                        defaultPull = new HgURL(pullPath.toHgCommandUrlStringWithoutUserInfo(),
                                defaultPullURL.getUserInfo(), null).toHgCommandUrlString();
                        hgConfigFiles.setProperty(HGPROPNAME_DEFAULT_PULL, defaultPull);
                        hgConfigFiles.setProperty(HGPROPNAME_DEFAULT_PUSH, defaultPull);
                    } else if ((pullPath == null) && (pushPath != null)) {
                        String defaultPush = new HgURL(pushPath.toHgCommandUrlStringWithoutUserInfo(),
                                defaultPullURL.getUserInfo(), null).toHgCommandUrlString();
                        hgConfigFiles.setProperty(HGPROPNAME_DEFAULT_PUSH, defaultPush);
                    } else if ((pullPath != null) && (pushPath != null)) {
                        defaultPull = new HgURL(pullPath.toHgCommandUrlStringWithoutUserInfo(),
                                defaultPullURL.getUserInfo(), null).toHgCommandUrlString();
                        String defaultPush = new HgURL(pushPath.toHgCommandUrlStringWithoutUserInfo(),
                                defaultPullURL.getUserInfo(), null).toHgCommandUrlString();
                        hgConfigFiles.setProperty(HGPROPNAME_DEFAULT_PULL, defaultPull);
                        hgConfigFiles.setProperty(HGPROPNAME_DEFAULT_PUSH, defaultPush);
                    }
                } catch (URISyntaxException ex) {
                    Mercurial.LOG.log(Level.INFO, null, ex);
                }
            }

            private void openProject(final File clonePrjFile, final ProjectManager projectManager, final Mercurial hg) throws MissingResourceException {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        // Open and set focus on the cloned project if possible
                        OutputLogger logger = getLogger();
                        try {
                            FileObject cloneProj = FileUtil.toFileObject(clonePrjFile);
                            Project prj = null;
                            if (clonePrjFile != null && cloneProj != null) {
                                prj = projectManager.findProject(cloneProj);
                            }
                            if (prj != null) {
                                HgProjectUtils.openProject(prj, this);
                                hg.versionedFilesChanged();
                                hg.refreshAllAnnotations();
                            } else {
                                logger.outputInRed(NbBundle.getMessage(CloneAction.class, "MSG_EXTERNAL_CLONE_PRJ_NOT_FOUND_CANT_SETASMAIN")); // NOI18N
                            }
                        } catch (java.lang.Exception ex) {
                            HgUtils.notifyException(ex);
                        } finally {
                            logger.outputInRed(NbBundle.getMessage(CloneAction.class, "MSG_CLONE_DONE")); // NOI18N
                            logger.output(""); // NOI18N
                        }
                    }
                });
            }
        };
        support.setRepositoryRoot(source);
        support.setCancellableDelegate(new Cancellable(){
            @Override
            public boolean cancel() {
                if(!Utilities.isWindows()) 
                    return true;
                
                OutputLogger logger = support.getLogger();
                logger.outputInRed(NbBundle.getMessage(CloneAction.class, "MSG_CLONE_CANCEL_ATTEMPT")); // NOI18N
                JOptionPane.showMessageDialog(Utilities.findDialogParent(),
                    NbBundle.getMessage(CloneAction.class, "MSG_CLONE_CANCEL_NOT_SUPPORTED"),// NOI18N
                    NbBundle.getMessage(CloneAction.class, "MSG_CLONE_CANCEL_NOT_SUPPORTED_TITLE"),// NOI18N
                    JOptionPane.INFORMATION_MESSAGE);
                return false;
            }
        });
        return support.start(rp, source, org.openide.util.NbBundle.getMessage(CloneAction.class, "LBL_Clone_Progress", source)); // NOI18N
    }
   
}
