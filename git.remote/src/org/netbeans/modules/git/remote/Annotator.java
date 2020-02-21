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

package org.netbeans.modules.git.remote;

import java.awt.Color;
import java.awt.Image;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import javax.swing.Action;
import org.netbeans.modules.git.remote.cli.GitBranch;
import org.netbeans.modules.git.remote.cli.GitRepositoryState;
import org.netbeans.modules.git.remote.cli.GitTag;
import org.netbeans.modules.git.remote.FileInformation.Status;
import org.netbeans.modules.git.remote.options.AnnotationColorProvider;
import org.netbeans.modules.git.remote.ui.actions.AddAction;
import org.netbeans.modules.git.remote.ui.actions.ContextHolder;
import org.netbeans.modules.git.remote.ui.blame.AnnotateAction;
import org.netbeans.modules.git.remote.ui.checkout.RevertChangesAction;
import org.netbeans.modules.git.remote.ui.commit.CommitAction;
import org.netbeans.modules.git.remote.ui.conflicts.ResolveConflictsAction;
import org.netbeans.modules.git.remote.ui.history.SearchHistoryAction;
import org.netbeans.modules.git.remote.ui.menu.BranchMenu;
import org.netbeans.modules.git.remote.ui.menu.CheckoutMenu;
import org.netbeans.modules.git.remote.ui.menu.DiffMenu;
import org.netbeans.modules.git.remote.ui.menu.IgnoreMenu;
import org.netbeans.modules.git.remote.ui.menu.PatchesMenu;
import org.netbeans.modules.git.remote.ui.menu.RemoteMenu;
import org.netbeans.modules.git.remote.ui.menu.RepositoryMenu;
import org.netbeans.modules.git.remote.ui.menu.RevertMenu;
import org.netbeans.modules.git.remote.ui.menu.ShelveMenu;
import org.netbeans.modules.git.remote.ui.repository.RepositoryBrowserAction;
import org.netbeans.modules.git.remote.ui.repository.RepositoryInfo;
import org.netbeans.modules.git.remote.ui.status.StatusAction;
import org.netbeans.modules.git.remote.utils.GitUtils;
import org.netbeans.modules.remotefs.versioning.api.VCSFileProxySupport;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;
import org.netbeans.modules.versioning.core.spi.VCSAnnotator;
import org.netbeans.modules.versioning.core.spi.VCSContext;
import org.netbeans.modules.versioning.core.api.VersioningSupport;
import org.netbeans.modules.versioning.util.SystemActionBridge;
import org.netbeans.modules.versioning.util.Utils;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.Node;
import org.openide.util.ContextAwareAction;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;
import org.openide.util.lookup.Lookups;

/**
 */
public class Annotator extends VCSAnnotator implements PropertyChangeListener {
    private static final EnumSet<FileInformation.Status> STATUS_IS_IMPORTANT = EnumSet.noneOf(Status.class);
    private static final EnumSet<FileInformation.Status> STATUS_BADGEABLE = EnumSet.complementOf(
            EnumSet.of(Status.NOTVERSIONED_EXCLUDED, Status.NOTVERSIONED_NOTMANAGED, Status.UNKNOWN)
    );
    private String projectFormat;
    static {
        STATUS_IS_IMPORTANT.addAll(FileInformation.STATUS_LOCAL_CHANGES);
        STATUS_IS_IMPORTANT.addAll(EnumSet.of(FileInformation.Status.UPTODATE, FileInformation.Status.NOTVERSIONED_EXCLUDED));
    }
    private static final Pattern lessThan = Pattern.compile("<");  // NOI18N
    private static final String badgeModified = "org/netbeans/modules/git/remote/resources/icons/modified-badge.png";
    private static final String badgeConflicts = "org/netbeans/modules/git/remote/resources/icons/conflicts-badge.png";
    private static final String tooltipBranch = "<img src=\"" + Annotator.class.getClassLoader().getResource("org/netbeans/modules/git/remote/resources/icons/branch-badge.png") + "\">&nbsp;";
    private static final String toolTipModified = "<img src=\"" + Annotator.class.getClassLoader().getResource(badgeModified) + "\">&nbsp;"
            + NbBundle.getMessage(Annotator.class, "MSG_Contains_Modified");
    private static final String toolTipConflict = "<img src=\"" + Annotator.class.getClassLoader().getResource(badgeConflicts) + "\">&nbsp;"
            + NbBundle.getMessage(Annotator.class, "MSG_Contains_Conflicts");

    private final FileStatusCache cache;
    public static final String ACTIONS_PATH_PREFIX = "Actions/GitRemote/";                        // NOI18N
    static final String DEFAULT_ANNOTATION_PROJECT = "[{repository_state} {branch}]"; //NOI18N
    
    @NbBundle.Messages({
        "Annotator.variable.repositoryState=stands for repository state",
        "Annotator.variable.branch=stands for the current branch label"
    })
    private static final LabelVariables PROJECT_ANNOTATION_VARIABLES = new LabelVariables(
            new LabelVariable("repository_state", "{repository_state}", Bundle.Annotator_variable_repositoryState()),
            new LabelVariable("branch", "{branch}", Bundle.Annotator_variable_branch()));

    public Annotator() {
        cache = Git.getInstance().getFileStatusCache();
    }

    @Override
    public Action[] getActions (VCSContext context, ActionDestination destination) {
        Set<VCSFileProxy> roots = GitUtils.getRepositoryRoots(context);
        boolean noneVersioned = (roots == null || roots.isEmpty());

        List<Action> actions = new LinkedList<>();
        if (destination.equals(ActionDestination.MainMenu)) {
            if (noneVersioned) {
                FileSystem defaultFileSystem = VCSFileProxySupport.getDefaultFileSystem();
                if (defaultFileSystem != null) {
                    addAction("org-netbeans-modules-git-remote-ui-clone-CloneAction", null, actions, true);
                    addAction("org-netbeans-modules-git-remote-ui-init-InitAction", null, actions, true);
                    actions.add(null);
                    actions.add(SystemAction.get(RepositoryBrowserAction.class));
                }
            } else {            
                actions.add(SystemAction.get(StatusAction.class));
                actions.add(new DiffMenu(destination, null));
                actions.add(SystemAction.get(AddAction.class));
                actions.add(SystemAction.get(CommitAction.class));
                actions.add(new CheckoutMenu(ActionDestination.MainMenu, null));
                actions.add(SystemAction.get(RevertChangesAction.class));
                actions.add(SystemAction.get(AnnotateAction.class));
                actions.add(SystemAction.get(SearchHistoryAction.class));
                actions.add(SystemAction.get(ResolveConflictsAction.class));
                actions.add(null);
                
                actions.add(new IgnoreMenu(null));
                actions.add(new PatchesMenu(ActionDestination.MainMenu, null));
                actions.add(null);
                
                actions.add(new BranchMenu(ActionDestination.MainMenu, null, null));
                actions.add(new RemoteMenu(ActionDestination.MainMenu, null, null));
                actions.add(new RevertMenu(ActionDestination.MainMenu, null));
                actions.add(null);

                actions.add(new RepositoryMenu(ActionDestination.MainMenu, null));
            }
            Utils.setAcceleratorBindings(ACTIONS_PATH_PREFIX, actions.toArray(new Action[actions.size()]));
        } else {
            Lookup lkp = context.getElements();
            if (noneVersioned) {
                Set<VCSFileProxy> files = context.getFiles();
                if (files != null && files.size() > 0 && files.iterator().next().toFile() == null) {
                    addAction("org-netbeans-modules-git-remote-ui-init-InitAction", context, actions);
                }
            } else {
                Node [] nodes = context.getElements().lookupAll(Node.class).toArray(new Node[0]);
                actions.add(SystemActionBridge.createAction(SystemAction.get(StatusAction.class), NbBundle.getMessage(StatusAction.class, "LBL_StatusAction.popupName"), lkp));
                actions.add(new DiffMenu(ActionDestination.PopupMenu, lkp));
                actions.add(SystemActionBridge.createAction(SystemAction.get(AddAction.class), NbBundle.getMessage(AddAction.class, "LBL_AddAction.popupName"), lkp));
                actions.add(SystemActionBridge.createAction(SystemAction.get(CommitAction.class), NbBundle.getMessage(CommitAction.class, "LBL_CommitAction.popupName"), lkp));
                actions.add(new CheckoutMenu(ActionDestination.PopupMenu, lkp));
                actions.add(SystemActionBridge.createAction(SystemAction.get(RevertChangesAction.class), NbBundle.getMessage(RevertChangesAction.class, "LBL_RevertChangesAction_PopupName"), lkp)); //NOI18N
                actions.add(SystemActionBridge.createAction(SystemAction.get(AnnotateAction.class), SystemAction.get(AnnotateAction.class).visible(nodes)
                        ? NbBundle.getMessage(AnnotateAction.class, "LBL_HideAnnotateAction_PopupName") //NOI18N
                        : NbBundle.getMessage(AnnotateAction.class, "LBL_ShowAnnotateAction_PopupName"), lkp)); //NOI18N
                actions.add(SystemActionBridge.createAction(SystemAction.get(SearchHistoryAction.class), NbBundle.getMessage(SearchHistoryAction.class, "LBL_SearchHistoryAction_PopupName"), lkp)); //NOI18N
                actions.add(SystemActionBridge.createAction(SystemAction.get(ResolveConflictsAction.class), NbBundle.getMessage(ResolveConflictsAction.class, "LBL_ResolveConflictsAction_PopupName"), lkp));
                actions.add(null);
                
                actions.add(new IgnoreMenu(lkp));
                actions.add(new PatchesMenu(ActionDestination.PopupMenu, lkp));
                actions.add(null);
                
                actions.add(new BranchMenu(ActionDestination.PopupMenu, lkp, context));
                actions.add(new RemoteMenu(ActionDestination.PopupMenu, lkp, context));
                actions.add(new RevertMenu(ActionDestination.PopupMenu, lkp));
                actions.add(null);
                
                actions.add(new RepositoryMenu(ActionDestination.PopupMenu, lkp));
                actions.add(null);
                
                actions.add(new ShelveMenu(ActionDestination.PopupMenu, lkp, context));
            }
        }

        return actions.toArray(new Action[actions.size()]);
    }

    public void refreshFormat () {
        projectFormat = null;
        Git.getInstance().refreshAllAnnotations();
    }

    public LabelVariable[] getProjectVariables () {
        return PROJECT_ANNOTATION_VARIABLES.toArray(new LabelVariable[PROJECT_ANNOTATION_VARIABLES.size()]);
    }

    private void addAction(String name, VCSContext context, List<Action> actions) {
        addAction(name, context, actions, false);
    }

    private void addAction(String name, VCSContext context, List<Action> actions, boolean accelerate) {
        Action action;
        if(accelerate) {
            action = Utils.getAcceleratedAction(ACTIONS_PATH_PREFIX + name + ".instance");
        } else {
            // or use Actions.forID
            action = (Action) FileUtil.getConfigObject(ACTIONS_PATH_PREFIX + name + ".instance", Action.class);
        }
        if(action instanceof ContextAwareAction) {
            action = ((ContextAwareAction)action).createContextAwareInstance(Lookups.singleton(new ContextHolder(context)));
        }            
        if(action != null) {
            actions.add(action);
        }
    }

    @Override
    public Image annotateIcon(Image icon, VCSContext context) {
        boolean folderAnnotation = false;
        for (VCSFileProxy file : context.getRootFiles()) {
            if (file.isDirectory()) {
                folderAnnotation = true;
                break;
            }
        }

        if (folderAnnotation == false && context.getRootFiles().size() > 1) {
            folderAnnotation = !VCSFileProxySupport.isFromMultiFileDataObject(context);
        }

        if (folderAnnotation == false) {
            return annotateFileIcon(context, icon);
        } else {
            return annotateFolderIcon(context, icon);
        }
    }

    @Override
    public String annotateName(String name, VCSContext context) {
        FileInformation mostImportantInfo = null;
        VCSFileProxy mostImportantFile = null;
        boolean folderAnnotation = false;
        for (final VCSFileProxy file : context.getRootFiles()) {
            FileInformation info = cache.getStatus(file);
            if (!info.containsStatus(STATUS_IS_IMPORTANT)) {
                continue;
            }
            if (isMoreImportant(info, mostImportantInfo)) {
                mostImportantInfo = info;
                mostImportantFile = file;
                folderAnnotation = info.isDirectory();
            }
        }

        if (folderAnnotation == false && context.getRootFiles().size() > 1) {
            folderAnnotation = !VCSFileProxySupport.isFromMultiFileDataObject(context);
        }

        if (mostImportantInfo == null) {
            return null;
        }
        return folderAnnotation ?
            annotateFolderNameHtml(name, context, mostImportantInfo, mostImportantFile) :
            annotateNameHtml(name, mostImportantInfo, mostImportantFile);
    }

    private static boolean isMoreImportant (FileInformation a, FileInformation b) {
        if (b == null) {
            return true;
        }
        if (a == null) {
            return false;
        }
        return a.getComparableStatus() < b.getComparableStatus();
    }


    private Image annotateFileIcon (VCSContext context, Image icon) throws IllegalArgumentException {
        FileInformation mostImportantInfo = null;
        for (final VCSFileProxy file : context.getRootFiles()) {
            FileInformation info = cache.getStatus(file);
            if (!info.containsStatus(STATUS_IS_IMPORTANT)) {
                continue;
            }
            if (isMoreImportant(info, mostImportantInfo)) {
                mostImportantInfo = info;
            }
        }
        if(mostImportantInfo == null) {
            return null;
        }
        String tooltip = null;
        String statusText = mostImportantInfo.getStatusText(FileInformation.Mode.HEAD_VS_WORKING_TREE);
        if (mostImportantInfo.containsStatus(Status.NOTVERSIONED_EXCLUDED)) {
            // File is IGNORED
            tooltip = getAnnotationProvider().EXCLUDED_FILE_TOOLTIP.getFormat().format(new Object[] { statusText });
        } else if (mostImportantInfo.getStatus().equals(EnumSet.of(Status.NEW_HEAD_INDEX, Status.REMOVED_INDEX_WORKING_TREE))) {
            // ADDED to index but REMOVED in WT
            tooltip = getAnnotationProvider().UP_TO_DATE_FILE_TOOLTIP.getFormat().format(new Object[]{statusText});
        } else if (mostImportantInfo.getStatus().equals(EnumSet.of(Status.MODIFIED_HEAD_INDEX, Status.MODIFIED_INDEX_WORKING_TREE))) {
            // MODIFIED in index, MODIFIED in WT, but in WT same as HEAD
            tooltip = getAnnotationProvider().UP_TO_DATE_FILE_TOOLTIP.getFormat().format(new Object[]{statusText});
        } else if (mostImportantInfo.containsStatus(Status.REMOVED_HEAD_WORKING_TREE)) {
            // DELETED in WT
            tooltip = getAnnotationProvider().REMOVED_FILE_TOOLTIP.getFormat().format(new Object[] { statusText });
        } else if (mostImportantInfo.getStatus().equals(EnumSet.of(Status.NEW_INDEX_WORKING_TREE, Status.REMOVED_HEAD_INDEX))) {
            // recreated in WT
            tooltip = getAnnotationProvider().UP_TO_DATE_FILE_TOOLTIP.getFormat().format(new Object[]{statusText});
        } else if (mostImportantInfo.getStatus().equals(EnumSet.of(Status.NEW_INDEX_WORKING_TREE, Status.REMOVED_HEAD_INDEX, Status.MODIFIED_HEAD_WORKING_TREE))) {
            // recreated in WT and modified
            tooltip = getAnnotationProvider().MODIFIED_FILE_TOOLTIP.getFormat().format(new Object[] { statusText });
        } else if (mostImportantInfo.containsStatus(Status.NEW_INDEX_WORKING_TREE)) {
            // NEW in WT and unversioned
            tooltip = getAnnotationProvider().NEW_FILE_TOOLTIP.getFormat().format(new Object[] { statusText });
        } else if (mostImportantInfo.containsStatus(Status.NEW_HEAD_INDEX)) {
            // ADDED to index
            tooltip = getAnnotationProvider().ADDED_FILE_TOOLTIP.getFormat().format(new Object[] { statusText });
        } else if (mostImportantInfo.containsStatus(Status.MODIFIED_HEAD_WORKING_TREE)) {
            tooltip = getAnnotationProvider().MODIFIED_FILE_TOOLTIP.getFormat().format(new Object[] { statusText });
        } else if (mostImportantInfo.containsStatus(Status.UPTODATE)) {
            tooltip = null;
        } else if (mostImportantInfo.containsStatus(Status.IN_CONFLICT)) {
            tooltip = getAnnotationProvider().CONFLICT_FILE_TOOLTIP.getFormat().format(new Object[] { statusText });
        } else if (mostImportantInfo.containsStatus(Status.NOTVERSIONED_NOTMANAGED)) {
            tooltip = null;
        } else if (mostImportantInfo.containsStatus(Status.UNKNOWN)) {
            tooltip = null;
        } else {
            throw new IllegalStateException("Unknown status: " + mostImportantInfo.getStatus()); //NOI18N
        }
        return tooltip != null ? ImageUtilities.addToolTipToImage(icon, tooltip) : null;
    }

    @NbBundle.Messages({
        "# {0} - branch name", "MSG_Annotator.tooltip.branch=On branch \"{0}\".",
        "MSG_Annotator.tooltip.nobranch=Not on a branch (detached HEAD state)."
    })
    private Image annotateFolderIcon(VCSContext context, Image icon) {
        VCSFileProxy root = null;
        for (Iterator i = context.getRootFiles().iterator(); i.hasNext();) {
            VCSFileProxy file = (VCSFileProxy) i.next();
            // There is an assumption here that annotateName was already
            // called and FileStatusCache.getStatus was scheduled if
            // FileStatusCache.getCachedStatus returned null.
            FileInformation info = cache.getStatus(file);
            if (info.containsStatus(STATUS_BADGEABLE)) {
                root = file;
                break;
            }
        }
        if (root == null) {
            return null;
        }
        String branchLabel = null;
        Set<VCSFileProxy> roots = context.getRootFiles();
        Set<VCSFileProxy> repositories = GitUtils.getRepositoryRoots(roots);
        VCSFileProxy repository = repositories.isEmpty() ? null : repositories.iterator().next();
        if (repository != null && (roots.size() > 1 || root.equals(repository))) {
            // project node or repository root
            RepositoryInfo info = RepositoryInfo.getInstance(repository);
            addFileWithRepositoryAnnotation(info, root);
            GitBranch branch = info.getActiveBranch();
            if (branch != null) {
                String branchName = branch.getName();
                branchLabel = tooltipBranch;
                if (branchName == GitBranch.NO_BRANCH) { // do not use equals
                    branchLabel += Bundle.MSG_Annotator_tooltip_nobranch();
                } else {
                    branchLabel += Bundle.MSG_Annotator_tooltip_branch(branchName);
                }
            }
        }
        Image retval = icon;
        if (branchLabel != null) {
            retval = ImageUtilities.addToolTipToImage(retval, branchLabel);
        }
        Image badge = null;
        if (cache.containsFiles(context, EnumSet.of(Status.IN_CONFLICT), false)) {
            badge = ImageUtilities.assignToolTipToImage(ImageUtilities.loadImage(badgeConflicts, true), toolTipConflict);
        } else if (cache.containsFiles(context, FileInformation.STATUS_LOCAL_CHANGES, false)) {
            badge = ImageUtilities.assignToolTipToImage(ImageUtilities.loadImage(badgeModified, true), toolTipModified);
        }
        if (badge != null) {
            retval = ImageUtilities.mergeImages(retval, badge, 16, 9);
        }
        return retval;
    }

    private final Map<RepositoryInfo, Set<VCSFileProxy>> filesWithRepositoryAnnotations = new WeakHashMap<>(3);
    
    private String annotateFolderNameHtml (String name, VCSContext context, FileInformation mostImportantInfo, VCSFileProxy mostImportantFile) {
        boolean annotationsVisible = VersioningSupport.getPreferences().getBoolean(VersioningSupport.PREF_BOOLEAN_TEXT_ANNOTATIONS_VISIBLE, false);
        String nameHtml = htmlEncode(name);
        VCSFileProxy repository = Git.getInstance().getRepositoryRoot(mostImportantFile);
        if (!mostImportantFile.equals(repository) && mostImportantInfo.containsStatus(Status.NOTVERSIONED_EXCLUDED)) {
            return getAnnotationProvider().EXCLUDED_FILE.getFormat().format(new Object [] { nameHtml, ""}); // NOI18N
        }
        
        String folderAnnotation = ""; //NOI18N
        Set<VCSFileProxy> roots = context.getRootFiles();
        if (repository == null) {
            Git.STATUS_LOG.log(Level.WARNING, "annotateFolderNameHtml: null repository for {0} having status {1}", //NOI18N
                    new Object[] { mostImportantFile, mostImportantInfo });
        } else if (annotationsVisible && (roots.size() > 1 || mostImportantFile.equals(repository))) {
            // project node or repository root
            String branchLabel = ""; //NOI18N
            RepositoryInfo info = RepositoryInfo.getInstance(repository);
            addFileWithRepositoryAnnotation(info, mostImportantFile);
            GitBranch branch = info.getActiveBranch();
            if (branch != null) {
                branchLabel = branch.getName();
                if (branchLabel == GitBranch.NO_BRANCH) { // do not use equals
                    Map<String, GitTag> tags = info.getTags();
                    StringBuilder tagLabel = new StringBuilder(); //NOI18N
                    String branchID = branch.getId();
                    for (GitTag tag : tags.values()) {
                        if (branchID.equals(tag.getTaggedObjectId())) {
                            tagLabel.append(",").append(tag.getTagName());
                        }
                    }
                    if (tagLabel.length() <= 1) {
                        // not on a branch or tag, show at least part of commit id
                        branchLabel = branchID;
                        if (branchLabel.length() > 7) {
                            branchLabel = branchLabel.substring(0, 7) + "..."; //NOI18N
                        }
                    } else {
                        tagLabel.delete(0, 1);
                        branchLabel = tagLabel.toString();
                    }
                }
            }
            GitRepositoryState repositoryState = info.getRepositoryState();
            String format = getAnnotationProjectFormat();
            if (repositoryState != GitRepositoryState.SAFE) {
                folderAnnotation = MessageFormat.format(format, repositoryState.toString(), branchLabel);
            } else {
                format = format.replace("{0} ", "{0}"); //NOI18N
                folderAnnotation = MessageFormat.format(format, "", branchLabel); //NOI18N
            }
            folderAnnotation = folderAnnotation.replace("  ", " "); //NOI18N
        }

        MessageFormat uptodateFormat = getAnnotationProvider().UP_TO_DATE_FILE.getFormat();
        return uptodateFormat.format(new Object [] { nameHtml, folderAnnotation.isEmpty() ? "" //NOI18N
                : getAnnotationProvider().TEXT_ANNOTATION.getFormat().format(new Object[] { " " + folderAnnotation + "" } ) //NOI18N
        });
    }

    private void addFileWithRepositoryAnnotation (RepositoryInfo info, VCSFileProxy file) {
        info.removePropertyChangeListener(this);
        synchronized (filesWithRepositoryAnnotations) {
            Set<VCSFileProxy> files = filesWithRepositoryAnnotations.get(info);
            if (files == null) {
                filesWithRepositoryAnnotations.put(info, files = new HashSet<>());
            }
            files.add(file);
        }
        info.addPropertyChangeListener(this);
    }

    @Override
    public void propertyChange (final PropertyChangeEvent evt) {
        if (evt.getPropertyName() == RepositoryInfo.PROPERTY_ACTIVE_BRANCH || evt.getPropertyName() == RepositoryInfo.PROPERTY_STATE
                || evt.getPropertyName() == RepositoryInfo.PROPERTY_HEAD && ((GitBranch) evt.getNewValue()).getName() == GitBranch.NO_BRANCH) {
            Utils.post(new Runnable() {
                @Override
                public void run() {
                    RepositoryInfo info = (RepositoryInfo) evt.getSource();
                    Set<VCSFileProxy> filesToRefresh;
                    synchronized (filesWithRepositoryAnnotations) {
                        filesToRefresh = filesWithRepositoryAnnotations.remove(info);
                    }
                    if (filesToRefresh != null && !filesToRefresh.isEmpty()) {
                        Git.getInstance().headChanged(filesToRefresh);
                    }
                }
            }, 400);
        }
    }

    public String annotateNameHtml(String name, FileInformation mostImportantInfo, VCSFileProxy mostImportantFile) {
        name = htmlEncode(name);

        String textAnnotation;
        boolean annotationsVisible = VersioningSupport.getPreferences().getBoolean(VersioningSupport.PREF_BOOLEAN_TEXT_ANNOTATIONS_VISIBLE, false);

        if (annotationsVisible && mostImportantFile != null && mostImportantInfo.containsStatus(STATUS_IS_IMPORTANT)) {
            String statusText = mostImportantInfo.getShortStatusText();
            if(!statusText.isEmpty()) {
                textAnnotation = " [" + mostImportantInfo.getShortStatusText() + "]"; // NOI18N
                textAnnotation = getAnnotationProvider().TEXT_ANNOTATION.getFormat().format(new Object[] { textAnnotation });
            } else {
                textAnnotation = ""; // NOI18N
            }
        } else {
            textAnnotation = ""; // NOI18N
        }

        if (mostImportantInfo.containsStatus(Status.NOTVERSIONED_EXCLUDED)) {
            // IGNORED
            return getAnnotationProvider().EXCLUDED_FILE.getFormat().format(new Object [] { name, textAnnotation });
        } else if (mostImportantInfo.getStatus().equals(EnumSet.of(Status.NEW_HEAD_INDEX, Status.REMOVED_INDEX_WORKING_TREE))) {
            // ADDED to index but REMOVED in WT
            return getAnnotationProvider().UP_TO_DATE_FILE.getFormat().format(new Object [] { name, textAnnotation });
        } else if (mostImportantInfo.getStatus().equals(EnumSet.of(Status.MODIFIED_HEAD_INDEX, Status.MODIFIED_INDEX_WORKING_TREE))) {
            // MODIFIED in index, MODIFIED in WT, but in WT same as HEAD
            return getAnnotationProvider().UP_TO_DATE_FILE.getFormat().format(new Object [] { name, textAnnotation });
        } else if (mostImportantInfo.containsStatus(Status.REMOVED_HEAD_WORKING_TREE)) {
            // DELETED in WT
            return getAnnotationProvider().REMOVED_FILE.getFormat().format(new Object [] { name, textAnnotation });
        } else if (mostImportantInfo.getStatus().equals(EnumSet.of(Status.NEW_INDEX_WORKING_TREE, Status.REMOVED_HEAD_INDEX))) {
            // recreated in WT
            return getAnnotationProvider().UP_TO_DATE_FILE.getFormat().format(new Object [] { name, textAnnotation });
        } else if (mostImportantInfo.getStatus().equals(EnumSet.of(Status.NEW_INDEX_WORKING_TREE, Status.REMOVED_HEAD_INDEX, Status.MODIFIED_HEAD_WORKING_TREE))) {
            // recreated in WT and modified
            return getAnnotationProvider().MODIFIED_FILE.getFormat().format(new Object [] { name, textAnnotation });
        } else if (mostImportantInfo.containsStatus(Status.NEW_INDEX_WORKING_TREE)) {
            // NEW in WT and unversioned
            return getAnnotationProvider().NEW_FILE.getFormat().format(new Object [] { name, textAnnotation });
        } else if (mostImportantInfo.containsStatus(Status.NEW_HEAD_INDEX)) {
            // ADDED to index
            return getAnnotationProvider().ADDED_FILE.getFormat().format(new Object [] { name, textAnnotation });
        } else if (mostImportantInfo.containsStatus(Status.MODIFIED_HEAD_WORKING_TREE)) {
            return getAnnotationProvider().MODIFIED_FILE.getFormat().format(new Object [] { name, textAnnotation });
        } else if (mostImportantInfo.containsStatus(Status.UPTODATE)) {
            return getAnnotationProvider().UP_TO_DATE_FILE.getFormat().format(new Object [] { name, textAnnotation });
        } else if (mostImportantInfo.containsStatus(Status.IN_CONFLICT)) {
            return getAnnotationProvider().CONFLICT_FILE.getFormat().format(new Object [] { name, textAnnotation });
        } else if (mostImportantInfo.containsStatus(Status.NOTVERSIONED_NOTMANAGED)) {
            return name;
        } else if (mostImportantInfo.containsStatus(Status.UNKNOWN)) {
            return name;
        } else {
            throw new IllegalStateException("Unknown status: " + mostImportantInfo.getStatus()); //NOI18N
        }
    }

    public Color getAnnotatedColor (FileInformation info) {
        if (info.containsStatus(Status.NOTVERSIONED_EXCLUDED)) {
            // IGNORED
            return getAnnotationProvider().EXCLUDED_FILE.getActualColor();
        } else if (info.getStatus().equals(EnumSet.of(Status.NEW_HEAD_INDEX, Status.REMOVED_INDEX_WORKING_TREE))) {
            // ADDED to index but REMOVED in WT
            return getAnnotationProvider().UP_TO_DATE_FILE.getActualColor();
        } else if (info.getStatus().equals(EnumSet.of(Status.MODIFIED_HEAD_INDEX, Status.MODIFIED_INDEX_WORKING_TREE))) {
            // MODIFIED in index, MODIFIED in WT, but in WT same as HEAD
            return getAnnotationProvider().UP_TO_DATE_FILE.getActualColor();
        } else if (info.containsStatus(Status.REMOVED_HEAD_WORKING_TREE)) {
            // DELETED in WT
            return getAnnotationProvider().REMOVED_FILE.getActualColor();
        } else if (info.getStatus().equals(EnumSet.of(Status.NEW_INDEX_WORKING_TREE, Status.REMOVED_HEAD_INDEX))) {
            // recreated in WT
            return getAnnotationProvider().UP_TO_DATE_FILE.getActualColor();
        } else if (info.getStatus().equals(EnumSet.of(Status.NEW_INDEX_WORKING_TREE, Status.REMOVED_HEAD_INDEX, Status.MODIFIED_HEAD_WORKING_TREE))) {
            // recreated in WT and modified
            return getAnnotationProvider().MODIFIED_FILE.getActualColor();
        } else if (info.containsStatus(Status.NEW_INDEX_WORKING_TREE)) {
            // NEW in WT and unversioned
            return getAnnotationProvider().NEW_FILE.getActualColor();
        } else if (info.containsStatus(Status.NEW_HEAD_INDEX)) {
            // ADDED to index
            return getAnnotationProvider().ADDED_FILE.getActualColor();
        } else if (info.containsStatus(Status.MODIFIED_HEAD_WORKING_TREE)) {
            return getAnnotationProvider().MODIFIED_FILE.getActualColor();
        } else if (info.containsStatus(Status.UPTODATE)) {
            return getAnnotationProvider().UP_TO_DATE_FILE.getActualColor();
        } else if (info.containsStatus(Status.IN_CONFLICT)) {
            return getAnnotationProvider().CONFLICT_FILE.getActualColor();
        } else {
            return null;
        }
    }

    private static String htmlEncode(String name) {
        if (name.indexOf('<') == -1) {
            return name;
        }
        return lessThan.matcher(name).replaceAll("&lt;"); // NOI18N
    }

    private AnnotationColorProvider getAnnotationProvider() {
        return AnnotationColorProvider.getInstance();
    }

    private String getAnnotationProjectFormat () {
        if (projectFormat == null) {
            String format = GitModuleConfig.getDefault().getProjectAnnotationFormat();
            format = Utils.skipUnsupportedVariables(format, PROJECT_ANNOTATION_VARIABLES.toPatterns());
            try {
                int i = 0;
                for (LabelVariable var : PROJECT_ANNOTATION_VARIABLES) {
                    format = format.replaceAll("\\{" + var.getVariable() + "\\}", "\\{" + i++ + "\\}"); // NOI18N
                }
                projectFormat = format;
            } catch (IllegalArgumentException ex) {
                Logger.getLogger(Annotator.class.getName()).log(Level.INFO, "Invalid annotation format: {0}", //NOI18N
                        GitModuleConfig.getDefault().getProjectAnnotationFormat());
                format = DEFAULT_ANNOTATION_PROJECT;
                int i = 0;
                for (LabelVariable var : PROJECT_ANNOTATION_VARIABLES) {
                    format = format.replaceAll("\\{" + var.getVariable() + "\\}", "\\{" + i++ + "\\}"); // NOI18N
                }
                projectFormat = format;
            }
        }
        return projectFormat;
    }
    
    @NbBundle.Messages({
        "# {0} - variable name", "# {1} - variable description", "LabelVariable.displayName={0} - {1}"
    })
    public static class LabelVariable {
        
        private final String variable;
        private final String pattern;
        private final String description;

        public LabelVariable (String variable, String pattern, String description) {
            this.variable = variable;
            this.pattern = pattern;
            this.description = description;
        }

        public String getDescription () {
            return description;
        }

        public String getPattern () {
            return pattern;
        }

        public String getVariable () {
            return variable;
        }

        @Override
        public String toString () {
            return getPattern();
        }
        
    }
    
    private static class LabelVariables extends LinkedHashSet<LabelVariable> {

        public LabelVariables (LabelVariable... initVars) {
            super();
            for (LabelVariable var : initVars) {
                add(var);
            }
        }
        
        String[] toPatterns () {
            List<String> patterns = new ArrayList<>(size());
            for (LabelVariable var : this) {
                patterns.add(var.getPattern());
            }
            return patterns.toArray(new String[patterns.size()]);
        }
    }
}
