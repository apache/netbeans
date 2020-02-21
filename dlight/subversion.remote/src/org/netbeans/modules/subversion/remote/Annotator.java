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
package org.netbeans.modules.subversion.remote;

import org.netbeans.modules.subversion.remote.client.SvnClientFactory;
import java.awt.EventQueue;
import java.awt.Image;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.regex.Pattern;
import javax.swing.Action;
import org.netbeans.modules.subversion.remote.options.AnnotationColorProvider;
import org.netbeans.modules.subversion.remote.ui.blame.BlameAction;
import org.netbeans.modules.subversion.remote.ui.commit.CommitAction;
import org.netbeans.modules.subversion.remote.ui.history.SearchHistoryAction;
import org.netbeans.modules.subversion.remote.ui.menu.CopyMenu;
import org.netbeans.modules.subversion.remote.ui.menu.DiffMenu;
import org.netbeans.modules.subversion.remote.ui.menu.IgnoreMenu;
import org.netbeans.modules.subversion.remote.ui.menu.PatchesMenu;
import org.netbeans.modules.subversion.remote.ui.menu.UpdateMenu;
import org.netbeans.modules.subversion.remote.ui.menu.WorkingCopyMenu;
import org.netbeans.modules.subversion.remote.ui.properties.SvnPropertiesAction;
import org.netbeans.modules.subversion.remote.ui.properties.VersioningInfoAction;
import org.netbeans.modules.subversion.remote.ui.status.StatusAction;
import org.netbeans.modules.subversion.remote.ui.update.ResolveConflictsAction;
import org.netbeans.modules.subversion.remote.ui.update.RevertModificationsAction;
import org.netbeans.modules.subversion.remote.util.Context;
import org.netbeans.modules.subversion.remote.util.SvnUtils;
import org.netbeans.modules.remotefs.versioning.api.VCSFileProxySupport;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;
import org.netbeans.modules.versioning.core.api.VersioningSupport;
import org.netbeans.modules.versioning.core.spi.VCSAnnotator;
import org.netbeans.modules.versioning.core.spi.VCSContext;
import org.netbeans.modules.versioning.util.SystemActionBridge;
import org.netbeans.modules.versioning.util.Utils;
import org.openide.awt.Actions;
import org.openide.filesystems.FileSystem;
import org.openide.nodes.Node;
import org.openide.util.ContextAwareAction;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;
import org.openide.util.lookup.Lookups;

/**
 * Annotates names for display in Files and Projects view (and possible elsewhere). Uses
 * Filesystem support for this feature (to be replaced later in Core by something more generic).
 *
 * 
 */
public class Annotator extends VCSAnnotator {
    private static final String badgeModified = "org/netbeans/modules/subversion/remote/resources/icons/modified-badge.png"; //NOI18N
    private static final String badgeConflicts = "org/netbeans/modules/subversion/remote/resources/icons/conflicts-badge.png"; //NOI18N

    private static final String toolTipModified = "<img src=\"" + Annotator.class.getClassLoader().getResource(badgeModified) + "\">&nbsp;" //NOI18N
            + NbBundle.getMessage(Annotator.class, "MSG_Contains_Modified_Locally"); //NOI18N
    private static final String toolTipConflict = "<img src=\"" + Annotator.class.getClassLoader().getResource(badgeConflicts) + "\">&nbsp;" //NOI18N
            + NbBundle.getMessage(Annotator.class, "MSG_Contains_Conflicts"); //NOI18N

    private static final int STATUS_TEXT_ANNOTABLE = FileInformation.STATUS_NOTVERSIONED_EXCLUDED |
            FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY | FileInformation.STATUS_VERSIONED_UPTODATE |
            FileInformation.STATUS_VERSIONED_MODIFIEDLOCALLY | FileInformation.STATUS_VERSIONED_CONFLICT |
            FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY | FileInformation.STATUS_VERSIONED_DELETEDLOCALLY |
            FileInformation.STATUS_VERSIONED_ADDEDLOCALLY;

    private static final Pattern lessThan = Pattern.compile("<");  // NOI18N

    public static final String ANNOTATION_REVISION    = "revision"; //NOI18N
    public static final String ANNOTATION_STATUS      = "status"; //NOI18N
    public static final String ANNOTATION_LOCK        = "lock"; //NOI18N
    public static final String ANNOTATION_FOLDER      = "folder"; //NOI18N
    public static final String ANNOTATION_MIME_TYPE   = "mime_type"; //NOI18N
    public static final String ANNOTATION_COMMIT_REVISION = "commit_revision"; //NOI18N
    public static final String ANNOTATION_COMMIT_DATE = "date"; //NOI18N
    public static final String ANNOTATION_COMMIT_AUTHOR = "author"; //NOI18N

    public static final List<String> LABELS = Collections.unmodifiableList(Arrays.asList(ANNOTATION_REVISION, ANNOTATION_STATUS, ANNOTATION_LOCK, ANNOTATION_FOLDER, ANNOTATION_MIME_TYPE, ANNOTATION_COMMIT_REVISION,
                                                        ANNOTATION_COMMIT_DATE, ANNOTATION_COMMIT_AUTHOR));
    public static final String ACTIONS_PATH_PREFIX = "Actions/SubversionRemote/";          // NOI18N

    private final FileStatusCache cache;
    private final Map<FileSystem, AnnotationFormat> annotationFormat = new HashMap<>();

    public static final class AnnotationFormat {
        public MessageFormat format;
        public String emptyFormat;
        public boolean mimeTypeFlag;
    }
    
    Annotator(Subversion svn) {
        this.cache = svn.getStatusCache();
        initDefaults();
    }

    private void initDefaults() {
        refresh();
    }

    public void refresh() {
        for(FileSystem fileSystem : VCSFileProxySupport.getConnectedFileSystems()) {
            initFormat(fileSystem);
        }
    }

    private AnnotationFormat initFormat(FileSystem fileSystem) {
        AnnotationFormat af = annotationFormat.get(fileSystem);
        if (af == null) {
            af = new AnnotationFormat();
            annotationFormat.put(fileSystem, af);
        }
        String string = SvnModuleConfig.getDefault(fileSystem).getAnnotationFormat(); //System.getProperty("netbeans.experimental.svn.ui.statusLabelFormat");  // NOI18N
        if (string != null && !string.trim().equals("")) { // NOI18N
            af.mimeTypeFlag = string.contains("{mime_type}"); //NOI18N
            string = SvnUtils.createAnnotationFormat(string);
            if (!SvnUtils.isAnnotationFormatValid(string)) {
                Subversion.LOG.log(Level.WARNING, "Bad annotation format, switching to defaults"); //NOI18N
                string = org.openide.util.NbBundle.getMessage(Annotator.class, "Annotator.defaultFormat"); // NOI18N
                af.mimeTypeFlag = string.contains("{4}"); //NOI18N
            }
            af.format = new MessageFormat(string);
            af.emptyFormat = af.format.format(new String[] {"", "", "", "", "", "", "", "", ""} , new StringBuffer(), null).toString().trim(); //NOI18N
        }
        return af;
    }

    public AnnotationFormat getAnnotationFormat(FileSystem fileSystem) {
        AnnotationFormat af = annotationFormat.get(fileSystem);
        if (af == null) {
            af = initFormat(fileSystem);
            annotationFormat.put(fileSystem, af);
        }
        return af;
    }
    
    /**
     * Adds rendering attributes to an arbitrary String based on a SVN status. The name is usually a file or folder
     * display name and status is usually its SVN status as reported by FileStatusCache.
     *
     * @param name name to annotate
     * @param info status that an object with the given name has
     * @param file file this annotation belongs to. It is used to determine sticky tags for textual annotations. Pass
     * null if you do not want textual annotations to appear in returned markup
     * @return String html-annotated name that can be used in Swing controls that support html rendering. Note: it may
     * also return the original name String
     */
    public String annotateNameHtml(String name, FileInformation info, VCSFileProxy file) {
        if(!checkClientAvailable("annotateNameHtml", file == null ? new VCSFileProxy[0] : new VCSFileProxy[] {file})) { //NOI18N
            return name;
        }
        name = htmlEncode(name);
        int status = info.getStatus();
        String textAnnotation;
        boolean annotationsVisible = VersioningSupport.getPreferences().getBoolean(VersioningSupport.PREF_BOOLEAN_TEXT_ANNOTATIONS_VISIBLE, false);
        FileSystem fileSystem = null;
        if (annotationsVisible && file != null && (status & STATUS_TEXT_ANNOTABLE) != 0) {
            fileSystem = VCSFileProxySupport.getFileSystem(file);
            AnnotationFormat af = getAnnotationFormat(fileSystem);
            if (af.format != null) {
                textAnnotation = formatAnnotation(info, file);
            } else {
                String lockString = getLockString(info.getStatus());
                String lockStringAnnPart = (lockString.isEmpty() ? "" : (lockString + "; ")); //NOI18N
                String sticky = cache.getLabelsCache().getLabelInfo(file, false).getStickyString();
                if (status == FileInformation.STATUS_VERSIONED_UPTODATE && "".equals(sticky)) { //NOI18N
                    textAnnotation = lockString;  // NOI18N
                } else if (status == FileInformation.STATUS_VERSIONED_UPTODATE) {
                    textAnnotation = " [" + lockStringAnnPart + sticky + "]"; // NOI18N
                } else if ("".equals(sticky)) {                         //NOI18N
                    String statusText = info.getShortStatusText();
                    if(!statusText.equals("")) {
                        textAnnotation = " [" + lockStringAnnPart + info.getShortStatusText() + "]"; // NOI18N
                    } else {
                        textAnnotation = lockString;
                    }
                } else {
                    textAnnotation = " [" + info.getShortStatusText() + "; " + lockStringAnnPart + sticky + "]"; // NOI18N
                }
            }
        } else {
            textAnnotation = ""; // NOI18N
        }
        final AnnotationColorProvider annotationProvider = getAnnotationProvider(fileSystem);
        if (textAnnotation.length() > 0) {
            textAnnotation = annotationProvider.TEXT_ANNOTATION.getFormat().format(new Object[] { textAnnotation });
        }

        // aligned with SvnUtils.getComparableStatus

        if (0 != (status & FileInformation.STATUS_VERSIONED_CONFLICT_TREE)) {
            return annotationProvider.TREECONFLICT_FILE.getFormat().format(new Object [] { name, textAnnotation });
        } else if (0 != (status & FileInformation.STATUS_VERSIONED_CONFLICT)) {
            return annotationProvider.CONFLICT_FILE.getFormat().format(new Object [] { name, textAnnotation });
        } else if (0 != (status & FileInformation.STATUS_VERSIONED_MERGE)) {
            return annotationProvider.MERGEABLE_FILE.getFormat().format(new Object [] { name, textAnnotation });
        } else if (0 != (status & FileInformation.STATUS_VERSIONED_DELETEDLOCALLY)) {
            return annotationProvider.DELETED_LOCALLY_FILE.getFormat().format(new Object [] { name, textAnnotation });
        } else if (0 != (status & FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY)) {
            return annotationProvider.REMOVED_LOCALLY_FILE.getFormat().format(new Object [] { name, textAnnotation });
        } else if (0 != (status & FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY)) {
            return annotationProvider.NEW_LOCALLY_FILE.getFormat().format(new Object [] { name, textAnnotation });
        } else if (0 != (status & FileInformation.STATUS_VERSIONED_ADDEDLOCALLY)) {
            return annotationProvider.ADDED_LOCALLY_FILE.getFormat().format(new Object [] { name, textAnnotation });
        } else if (0 != (status & FileInformation.STATUS_VERSIONED_MODIFIEDLOCALLY)) {
            return annotationProvider.MODIFIED_LOCALLY_FILE.getFormat().format(new Object [] { name, textAnnotation });

        // repository changes - lower annotator priority

        } else if (0 != (status & FileInformation.STATUS_VERSIONED_REMOVEDINREPOSITORY)) {
            return annotationProvider.REMOVED_IN_REPOSITORY_FILE.getFormat().format(new Object [] { name, textAnnotation });
        } else if (0 != (status & FileInformation.STATUS_VERSIONED_NEWINREPOSITORY)) {
            return annotationProvider.NEW_IN_REPOSITORY_FILE.getFormat().format(new Object [] { name, textAnnotation });
        } else if (0 != (status & FileInformation.STATUS_VERSIONED_MODIFIEDINREPOSITORY)) {
            return annotationProvider.MODIFIED_IN_REPOSITORY_FILE.getFormat().format(new Object [] { name, textAnnotation });
        } else if (0 != (status & FileInformation.STATUS_VERSIONED_UPTODATE)) {
            return annotationProvider.UP_TO_DATE_FILE.getFormat().format(new Object [] { name, textAnnotation });
        } else if (0 != (status & FileInformation.STATUS_NOTVERSIONED_EXCLUDED)) {
            return annotationProvider.EXCLUDED_FILE.getFormat().format(new Object [] { name, textAnnotation });
        } else if (0 != (status & FileInformation.STATUS_NOTVERSIONED_NOTMANAGED)) {
            return name;
        } else if (status == FileInformation.STATUS_UNKNOWN) {
            return name;
        } else {
            throw new IllegalArgumentException("Unknown status: " + status); // NOI18N
        }
    }

    /**
     * Applies custom format.
     */
    private String formatAnnotation(FileInformation info, VCSFileProxy file) {
        String statusString = "";  // NOI18N
        int status = info.getStatus();
        if (status != FileInformation.STATUS_VERSIONED_UPTODATE) {
            statusString = info.getShortStatusText();
        }
        String lockString = getLockString(status);
        AnnotationFormat af = getAnnotationFormat(VCSFileProxySupport.getFileSystem(file));
        FileStatusCache.FileLabelInfo labelInfo;
        labelInfo = cache.getLabelsCache().getLabelInfo(file, af.mimeTypeFlag);
        String revisionString = labelInfo.getRevisionString();
        String binaryString = labelInfo.getBinaryString();
        String stickyString = labelInfo.getStickyString();
        String lastRev = labelInfo.getLastRevisionString();
        String lastDate = labelInfo.getLastDateString();
        String lastAuthor = labelInfo.getLastAuthorString();

        Object[] arguments = new Object[] {
            revisionString,
            statusString,
            stickyString,
            lockString,
            binaryString,
            lastRev,
            lastDate,
            lastAuthor
        };

        String annotation = af.format.format(arguments, new StringBuffer(), null).toString().trim();
        if(annotation.equals(af.emptyFormat)) {
            return ""; //NOI18N
        } else {
            return " " + annotation; //NOI18N
        }
    }

    private String annotateFolderNameHtml(String name, FileInformation info, VCSFileProxy file) {
        name = htmlEncode(name);
        int status = info.getStatus();
        String textAnnotation;
        boolean annotationsVisible = VersioningSupport.getPreferences().getBoolean(VersioningSupport.PREF_BOOLEAN_TEXT_ANNOTATIONS_VISIBLE, false);
        FileSystem fileSystem = null;
        if (annotationsVisible && file != null && (status & FileInformation.STATUS_MANAGED) != 0) {
            fileSystem = VCSFileProxySupport.getFileSystem(file);
            AnnotationFormat af = getAnnotationFormat(fileSystem);

            if (af.format != null) {
                textAnnotation = formatAnnotation(info, file);
            } else {
                String sticky = cache.getLabelsCache().getLabelInfo(file, false).getStickyString();
                if (status == FileInformation.STATUS_VERSIONED_UPTODATE && "".equals(sticky)) { //NOI18N
                    textAnnotation = ""; // NOI18N
                } else if (status == FileInformation.STATUS_VERSIONED_UPTODATE) {
                    textAnnotation = " [" + sticky + "]"; // NOI18N
                } else if ("".equals(sticky)) {                         //NOI18N
                    String statusText = info.getShortStatusText();
                    if(!statusText.equals("")) { // NOI18N
                        textAnnotation = " [" + info.getShortStatusText() + "]"; // NOI18N
                    } else {
                        textAnnotation = ""; // NOI18N
                    }
                } else {
                    textAnnotation = " [" + info.getShortStatusText() + "; " + sticky + "]"; // NOI18N
                }
            }
        } else {
            textAnnotation = ""; // NOI18N
        }
        final AnnotationColorProvider annotationProvider = getAnnotationProvider(fileSystem);
        if (textAnnotation.length() > 0) {
            textAnnotation = annotationProvider.TEXT_ANNOTATION.getFormat().format(new Object[] { textAnnotation });
        }

        if (status == FileInformation.STATUS_UNKNOWN) {
            return name;
        } else if (match(status, FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY)) {
            return name;
        } else if (match(status, FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY)) {
            return annotationProvider.UP_TO_DATE_FILE.getFormat().format(new Object [] { name, textAnnotation });
        } else if (match(status, FileInformation.STATUS_VERSIONED_ADDEDLOCALLY)) {
            return annotationProvider.UP_TO_DATE_FILE.getFormat().format(new Object [] { name, textAnnotation });
        } else if (match(status, FileInformation.STATUS_VERSIONED_UPTODATE)) {
            return annotationProvider.UP_TO_DATE_FILE.getFormat().format(new Object [] { name, textAnnotation });
        } else if (match(status, FileInformation.STATUS_NOTVERSIONED_EXCLUDED)) {
            return annotationProvider.EXCLUDED_FILE.getFormat().format(new Object [] { name, textAnnotation });
        } else if (match(status, FileInformation.STATUS_VERSIONED_DELETEDLOCALLY)) {
            return name;
        } else if (match(status, FileInformation.STATUS_VERSIONED_NEWINREPOSITORY)) {
            return name;
        } else if (match(status, FileInformation.STATUS_VERSIONED_MODIFIEDINREPOSITORY)) {
            return name;
        } else if (match(status, FileInformation.STATUS_VERSIONED_REMOVEDINREPOSITORY)) {
            return name;
        } else if (match(status, FileInformation.STATUS_NOTVERSIONED_NOTMANAGED)) {
            return name;
        } else if (match(status, FileInformation.STATUS_VERSIONED_MERGE)) {
            return name;
        } else if (match(status, FileInformation.STATUS_VERSIONED_MODIFIEDLOCALLY)) {
            return annotationProvider.UP_TO_DATE_FILE.getFormat().format(new Object [] { name, textAnnotation });
        } else if (match(status, FileInformation.STATUS_VERSIONED_CONFLICT)) {
            return name;
        } else {
            throw new IllegalArgumentException("Unknown status: " + status); // NOI18N
        }
    }

    private static boolean match(int status, int mask) {
        return (status & mask) != 0;
    }

    private String htmlEncode(String name) {
        if (name.indexOf('<') == -1) {
            return name;
        }
        return lessThan.matcher(name).replaceAll("&lt;"); // NOI18N
    }

    public String annotateNameHtml(VCSFileProxy file, FileInformation info) {
        return annotateNameHtml(file.getName(), info, file);
    }

    public String annotateNameHtml(String name, VCSContext context, int includeStatus) {
        if(!checkClientAvailable("annotateNameHtml", context.getRootFiles().toArray(new VCSFileProxy[context.getRootFiles().size()]))) { //NOI18N
            return name;
        }
        FileInformation mostImportantInfo = null;
        VCSFileProxy mostImportantFile = null;
        boolean folderAnnotation = false;

        for (VCSFileProxy file : context.getRootFiles()) {
            if (SvnUtils.isPartOfSubversionMetadata(file)) {
                // no need to handle .svn files, eliminates some warnings as 'no repository url found for managed file .svn'
                // happens e.g. when annotating a Project folder
                continue;
            }
            FileInformation info = cache.getCachedStatus(file);
            if (info == null) {
                // status not in cache, plan refresh
                VCSFileProxy parentFile = file.getParentFile();
                if ( Subversion.LOG.isLoggable(Level.FINE)) {
                    Subversion.LOG.log(Level.FINE, "null cached status for: {0} in {1}", new Object[] {file, parentFile});
                }
                cache.refreshAsync(file);
                info = new FileInformation(FileInformation.STATUS_VERSIONED_UPTODATE, false);
            }
            int status = info.getStatus();
            if ((status & includeStatus) == 0) {
                continue;
            }

            if (isMoreImportant(info, mostImportantInfo)) {
                mostImportantInfo = info;
                mostImportantFile = file;
                folderAnnotation = file.isDirectory();
            }
        }

        if (folderAnnotation == false && context.getRootFiles().size() > 1) {
            folderAnnotation = !VCSFileProxySupport.isFromMultiFileDataObject(context);
        }

        if (mostImportantInfo == null) {
            return null;
        }
        return folderAnnotation ?
                annotateFolderNameHtml(name, mostImportantInfo, mostImportantFile) :
                annotateNameHtml(name, mostImportantInfo, mostImportantFile);
    }

    private boolean isMoreImportant(FileInformation a, FileInformation b) {
        if (b == null) {
            return true;
        }
        if (a == null) {
            return false;
        }
        return SvnUtils.getComparableStatus(a.getStatus()) < SvnUtils.getComparableStatus(b.getStatus());
    }

    /**
     * Returns array of versioning actions that may be used to construct a popup menu. These actions
     * will act on the supplied context.
     *
     * @param ctx context similar to {@link org.openide.util.ContextAwareAction#createContextAwareInstance(org.openide.util.Lookup)}
     * @param destination
     * @return Action[] array of versioning actions that may be used to construct a popup menu. These actions
     * will act on currently activated nodes.
     */
    @Override
    public Action [] getActions(VCSContext ctx, VCSAnnotator.ActionDestination destination) {
        return getStaticActions(ctx, destination);
    }
    
    public static Action [] getStaticActions(VCSContext ctx, VCSAnnotator.ActionDestination destination) {
        List<Action> actions = new ArrayList<>(20);
        VCSFileProxy[] files = ctx.getRootFiles().toArray(new VCSFileProxy[ctx.getRootFiles().size()]);
        boolean noneVersioned;
        if (EventQueue.isDispatchThread() && !Subversion.getInstance().getStatusCache().ready()) {
            noneVersioned = true;
            Subversion.LOG.log(Level.INFO, "Cache not yet initialized, showing default actions"); //NOI18N
        } else {
            noneVersioned = isNothingVersioned(files);
        }
        if (destination == VCSAnnotator.ActionDestination.MainMenu) {
            if (noneVersioned) {
                FileSystem defaultFileSystem = VCSFileProxySupport.getDefaultFileSystem();
                if (defaultFileSystem != null) {
                    // XXX use Actions.forID
                    Action a = Utils.getAcceleratedAction("Actions/SubversionRemote/org-netbeans-modules-subversion-remote-ui-checkout-CheckoutAction.instance"); //NOI18N
                    if(a != null) {
                        actions.add(a);
                    }
                    a = Utils.getAcceleratedAction("Actions/SubversionRemote/org-netbeans-modules-subversion-remote-ui-project-ImportAction.instance"); //NOI18N
                    if(a instanceof ContextAwareAction) {
                        a = ((ContextAwareAction)a).createContextAwareInstance(Lookups.fixed((Object[]) files));
                    }
                    if(a != null) {
                        actions.add(a);
                    }
                }
            } else {
                actions.add(SystemAction.get(StatusAction.class));
                actions.add(new DiffMenu(destination, null));
                actions.add(SystemAction.get(CommitAction.class));
                actions.add(new UpdateMenu(destination, null));
                actions.add(SystemAction.get(RevertModificationsAction.class));
                actions.add(SystemAction.get(BlameAction.class));
                actions.add(SystemAction.get(SearchHistoryAction.class));
                actions.add(SystemAction.get(ResolveConflictsAction.class));
                actions.add(null);
                
                actions.add(new IgnoreMenu(null, null));
                actions.add(new PatchesMenu(destination, null));
                actions.add(null);
                
                actions.add(new CopyMenu(destination, null));
                Action a = Utils.getAcceleratedAction("Actions/SubversionRemote/org-netbeans-modules-subversion-remote-ui-checkout-CheckoutAction.instance"); //NOI18N
                if(a != null) actions.add(a);
                actions.add(null);
                
                actions.add(new WorkingCopyMenu(destination, null));
                actions.add(SystemAction.get(VersioningInfoAction.class));
                actions.add(SystemAction.get(SvnPropertiesAction.class));
            }
            Utils.setAcceleratorBindings(ACTIONS_PATH_PREFIX, actions.toArray(new Action[actions.size()]));
        } else {
            ResourceBundle loc = NbBundle.getBundle(Annotator.class);
            Lookup context = ctx.getElements();
            if (noneVersioned) {
                if (files.length > 0 && files[0].toFile() == null) {
                    Action a = Actions.forID("SubversionRemote", "org.netbeans.modules.subversion.remote.ui.project.ImportAction"); //NOI18N
                    if(a instanceof ContextAwareAction) {
                        a = ((ContextAwareAction)a).createContextAwareInstance(Lookups.fixed((Object[]) files));
                    }
                    if(a != null) {
                        actions.add(a);
                    }
                }
            } else {
                Node[] nodes = ctx.getElements().lookupAll(Node.class).toArray(new Node[0]);
                actions.add(SystemActionBridge.createAction(SystemAction.get(StatusAction.class), loc.getString("CTL_PopupMenuItem_Status"), context));
                actions.add(new DiffMenu(destination, context));
                actions.add(SystemActionBridge.createAction(SystemAction.get(CommitAction.class), loc.getString("CTL_PopupMenuItem_Commit"), context));
                actions.add(new UpdateMenu(destination, context));
                actions.add(SystemActionBridge.createAction(SystemAction.get(RevertModificationsAction.class), loc.getString("CTL_PopupMenuItem_GetClean"), context));
                actions.add(SystemActionBridge.createAction(SystemAction.get(BlameAction.class),
                        (SystemAction.get(BlameAction.class)).visible(nodes)
                        ? loc.getString("CTL_PopupMenuItem_HideAnnotations")
                        : loc.getString("CTL_PopupMenuItem_ShowAnnotations"), context));
                actions.add(SystemActionBridge.createAction(SystemAction.get(SearchHistoryAction.class), loc.getString("CTL_PopupMenuItem_SearchHistory"), context));
                actions.add(SystemActionBridge.createAction(SystemAction.get(ResolveConflictsAction.class), loc.getString("CTL_PopupMenuItem_ResolveConflicts"), context));
                actions.add(null);
                
                actions.add(new IgnoreMenu(context, nodes));
                actions.add(new PatchesMenu(destination, context));
                actions.add(null);
                
                actions.add(new CopyMenu(destination, context));
                Action a = Actions.forID("SubversionRemote", "org.netbeans.modules.subversion.remote.ui.checkout.CheckoutAction"); //NOI18N
                if(a != null) {
                    actions.add(a);
                }
                actions.add(null);
                
                actions.add(new WorkingCopyMenu(destination, context));
                actions.add(SystemActionBridge.createAction(
                                SystemAction.get(VersioningInfoAction.class),
                                loc.getString("CTL_PopupMenuItem_VersioningInfo"), context));
                actions.add(SystemActionBridge.createAction(
                                SystemAction.get(SvnPropertiesAction.class),
                                loc.getString("CTL_PopupMenuItem_Properties"), context));
            }
        }
        return actions.toArray(new Action[actions.size()]);
    }

    private static boolean isNothingVersioned(VCSFileProxy[] files) {
        for (VCSFileProxy file : files) {
            if (SvnUtils.isManaged(file)) {
                return false;
            }
        }
        return true;
    }

    public Image annotateIcon(Image icon, VCSContext context, int includeStatus) {
        if(!checkClientAvailable("annotateIcon", context.getRootFiles().toArray(new VCSFileProxy[context.getRootFiles().size()]))) { //NOI18N
            return null;
        }
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
            return annotateFileIcon(context, icon, includeStatus);
        } else {
            return annotateFolderIcon(context, icon);
        }
    }

    private Image annotateFileIcon(VCSContext context, Image icon, int includeStatus) {
        FileInformation mostImportantInfo = null;

        List<VCSFileProxy> filesToRefresh = new LinkedList<>();
        for (VCSFileProxy file : context.getRootFiles()) {
            FileInformation info = cache.getCachedStatus(file);
            if (info == null) {
                VCSFileProxy parentFile = file.getParentFile();
                if ( Subversion.LOG.isLoggable(Level.FINE)) {
                    Subversion.LOG.log(Level.FINE, "null cached status for: {0} in {1}", new Object[] {file, parentFile});
                }
                filesToRefresh.add(file);
                info = new FileInformation(FileInformation.STATUS_VERSIONED_UPTODATE, false);
            }
            int status = info.getStatus();
            if ((status & includeStatus) == 0) continue;

            if (isMoreImportant(info, mostImportantInfo)) {
                mostImportantInfo = info;
            }
        }
        cache.refreshAsync(filesToRefresh);

        if(mostImportantInfo == null) {
            return null;
        }
        final AnnotationColorProvider annotationProvider = getAnnotationProvider(VCSFileProxySupport.getFileSystem(context.getRootFiles().iterator().next()));
        String statusText = null;
        int status = mostImportantInfo.getStatus();
        if (0 != (status & FileInformation.STATUS_VERSIONED_CONFLICT_TREE)) {
            statusText = annotationProvider.TREECONFLICT_FILE_TOOLTIP.getFormat().format(new Object [] { mostImportantInfo.getStatusText() });
        } else if (0 != (status & FileInformation.STATUS_VERSIONED_CONFLICT)) {
            statusText = annotationProvider.CONFLICT_FILE_TOOLTIP.getFormat().format(new Object [] { mostImportantInfo.getStatusText() });
        } else if (0 != (status & FileInformation.STATUS_VERSIONED_MERGE)) {
            statusText = annotationProvider.MERGEABLE_FILE_TOOLTIP.getFormat().format(new Object [] { mostImportantInfo.getStatusText() });
        } else if (0 != (status & FileInformation.STATUS_VERSIONED_DELETEDLOCALLY)) {
            statusText = annotationProvider.DELETED_LOCALLY_FILE_TOOLTIP.getFormat().format(new Object [] { mostImportantInfo.getStatusText() });
        } else if (0 != (status & FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY)) {
            statusText = annotationProvider.REMOVED_LOCALLY_FILE_TOOLTIP.getFormat().format(new Object [] { mostImportantInfo.getStatusText() });
        } else if (0 != (status & FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY)) {
            statusText = annotationProvider.NEW_LOCALLY_FILE_TOOLTIP.getFormat().format(new Object [] { mostImportantInfo.getStatusText() });
        } else if (0 != (status & FileInformation.STATUS_VERSIONED_ADDEDLOCALLY)) {
            statusText = annotationProvider.ADDED_LOCALLY_FILE_TOOLTIP.getFormat().format(new Object [] { mostImportantInfo.getStatusText() });
        } else if (0 != (status & FileInformation.STATUS_VERSIONED_MODIFIEDLOCALLY)) {
            statusText = annotationProvider.MODIFIED_LOCALLY_FILE_TOOLTIP.getFormat().format(new Object [] { mostImportantInfo.getStatusText() });

        // repository changes - lower annotator priority

        } else if (0 != (status & FileInformation.STATUS_VERSIONED_REMOVEDINREPOSITORY)) {
            statusText = annotationProvider.REMOVED_IN_REPOSITORY_FILE_TOOLTIP.getFormat().format(new Object [] { mostImportantInfo.getStatusText() });
        } else if (0 != (status & FileInformation.STATUS_VERSIONED_NEWINREPOSITORY)) {
            statusText = annotationProvider.NEW_IN_REPOSITORY_FILE.getFormat().format(new Object [] { mostImportantInfo.getStatusText() });
        } else if (0 != (status & FileInformation.STATUS_VERSIONED_MODIFIEDINREPOSITORY)) {
            statusText = annotationProvider.MODIFIED_IN_REPOSITORY_FILE_TOOLTIP.getFormat().format(new Object [] { mostImportantInfo.getStatusText() });
        } else if (0 != (status & FileInformation.STATUS_VERSIONED_UPTODATE)) {
            statusText = null;
        } else if (0 != (status & FileInformation.STATUS_NOTVERSIONED_EXCLUDED)) {
            statusText = annotationProvider.EXCLUDED_FILE_TOOLTIP.getFormat().format(new Object [] { mostImportantInfo.getStatusText() });
        } else if (0 != (status & FileInformation.STATUS_NOTVERSIONED_NOTMANAGED)) {
            statusText = null;
        } else if (status == FileInformation.STATUS_UNKNOWN) {
            statusText = null;
        } else {
            throw new IllegalArgumentException("Unknown status: " + status); // NOI18N
        }
        return statusText != null ? ImageUtilities.addToolTipToImage(icon, statusText) : null; // NOI18
    }

    private Image annotateFolderIcon(VCSContext context, Image icon) {
        List<VCSFileProxy> filesToRefresh = new LinkedList<>();
        for (Iterator<VCSFileProxy> i = context.getRootFiles().iterator(); i.hasNext();) {
            VCSFileProxy file = i.next();
            FileInformation info = cache.getCachedStatus(file);
            if (info == null) {
                filesToRefresh.add(file);
            }
        }
        cache.refreshAsync(filesToRefresh);

        if(cache.ready()) {
            if(cache.containsFiles(context.getRootFiles(), FileInformation.STATUS_VERSIONED_CONFLICT, false)) {
                return getBadge(badgeConflicts, icon, toolTipConflict);
            } else if(cache.containsFiles(context.getRootFiles(), FileInformation.STATUS_LOCAL_CHANGE, false)) {
                return getBadge(badgeModified, icon, toolTipModified);
            }
        }

        return icon;
    }

    private Image getBadge(String badgePath, Image origIcon, String toolTip) {
        Image ret = ImageUtilities.assignToolTipToImage(ImageUtilities.loadImage(badgePath, true), toolTip);
        ret = ImageUtilities.mergeImages(origIcon, ret, 16, 9);
        return ret;
    }

    private boolean clientInitStarted;
    private boolean checkClientAvailable (String methodName, final VCSFileProxy[] files) {
        boolean available = true;
        final Context context = new Context(files);
        if (!SvnClientFactory.isInitialized(context) && EventQueue.isDispatchThread()) {
            if (!clientInitStarted) {
                clientInitStarted = true;
                Subversion.getInstance().getRequestProcessor().post(new Runnable() {
                    @Override
                    public void run() {
                        SvnClientFactory.getInstance(context);
                        Subversion.getInstance().refreshAllAnnotations();
                    }
                });
            }
            if (Subversion.LOG.isLoggable(Level.FINE)) {
                Subversion.LOG.log(Level.FINE, " skipping {0} due to not yet initialized client", methodName); //NOI18N
            }
            available = false;
        } else if(!SvnClientFactory.isClientAvailable(new Context(files))) {
            if (Subversion.LOG.isLoggable(Level.FINE)) {
                Subversion.LOG.log(Level.FINE, " skipping {0} due to missing client", methodName); //NOI18N
            }
            available = false;
        }
        return available;
    }

    private AnnotationColorProvider getAnnotationProvider(FileSystem fileSystem) {
        return AnnotationColorProvider.getInstance();
    }

    private String getLockString (int status) {
        String lockString = ""; //NOI18N
        if ((status & FileInformation.STATUS_LOCKED) != 0) {
            lockString = "K"; //NOI18N
        } else if ((status & FileInformation.STATUS_LOCKED_REMOTELY) != 0) {
            lockString = "O"; //NOI18N
        }
        return lockString;
    }
}
