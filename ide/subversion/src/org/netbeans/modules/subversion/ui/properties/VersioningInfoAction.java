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

package org.netbeans.modules.subversion.ui.properties;

import java.awt.EventQueue;
import java.io.File;
import java.text.DateFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.subversion.FileInformation;
import org.netbeans.modules.subversion.FileStatusCache;
import org.netbeans.modules.subversion.Subversion;
import org.netbeans.modules.subversion.client.SvnClient;
import org.netbeans.modules.subversion.client.SvnClientExceptionHandler;
import org.netbeans.modules.subversion.client.SvnProgressSupport;
import org.netbeans.modules.subversion.ui.actions.ContextAction;
import org.netbeans.modules.subversion.util.Context;
import org.netbeans.modules.subversion.util.SvnUtils;
import org.netbeans.modules.versioning.util.VersioningInfo;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.tigris.subversion.svnclientadapter.ISVNInfo;
import org.tigris.subversion.svnclientadapter.ISVNStatus;
import org.tigris.subversion.svnclientadapter.SVNClientException;
import org.tigris.subversion.svnclientadapter.SVNUrl;

/**
 * 
 * @author Peter Pis
 */
@NbBundle.Messages({
    "CTL_MenuItem_VersioningInfoAction=Versionin&g Info",
    "CTL_VersioningInfoAction=Versionin&g Info"
})
public final class VersioningInfoAction extends ContextAction {

    private static final Logger LOG = Logger.getLogger(VersioningInfoAction.class.getName());

    @Override
    protected boolean enable(Node[] nodes) {
        return super.enable(nodes);
    }
    
    @Override
    protected int getFileEnabledStatus() {
        return FileInformation.STATUS_VERSIONED | FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY | FileInformation.STATUS_NOTVERSIONED_EXCLUDED;
    }

    @Override
    protected int getDirectoryEnabledStatus() {
        return FileInformation.STATUS_VERSIONED | FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY | FileInformation.STATUS_NOTVERSIONED_EXCLUDED;
    }

    @Override
    public String getName() {
        return Bundle.CTL_VersioningInfoAction();
    }

    @Override
    protected String getBaseName(Node[] activatedNodes) {
        return "CTL_MenuItem_VersioningInfo";   // NOI18N
    }

    @Override
    protected void performContextAction(Node[] nodes) {       
        final Context ctx = getContext(nodes);
        final File[] roots = ctx.getRootFiles();
        if (roots == null || roots.length == 0) {
            LOG.log(Level.FINE, "No versioned folder in the selected context for {0}", nodes); //NOI18N
            return;
        }

        File root = roots[0];

        SVNUrl repositoryUrl = null;
        try {
            repositoryUrl = SvnUtils.getRepositoryRootUrl(root);
        } catch (SVNClientException ex) {
            SvnClientExceptionHandler.notifyException(ex, true, true);
            return;
        }
        if(repositoryUrl == null) {
            LOG.log(Level.WARNING, "Could not retrieve repository root for context file {0}", new Object[]{ root }); //NOI18N
            return;
        }
        RequestProcessor rp = Subversion.getInstance().getRequestProcessor(repositoryUrl);
        SvnProgressSupport support = new SvnProgressSupport() {
            private FileStatusCache cache;
            @Override
            protected void perform() {
                Arrays.sort(roots, new Comparator<File>() {
                    @Override
                    public int compare(File f1, File f2) {
                        return f1.getName().compareTo(f2.getName());
                    }
                });
                final LinkedHashMap<File, Map<String, String>> properties = new LinkedHashMap<File, Map<String, String>>(roots.length);
                cache = Subversion.getInstance().getStatusCache();
                for (File root : roots) {
                    FileInformation fi = cache.getStatus(root);
                    LinkedHashMap<String, String> fileProps = new LinkedHashMap<String, String>();
                    properties.put(root, fileProps);
                    String relativePath = getMessage("LBL_VersioningInfo_Property_Unknown"); //NOI18N
                    try {
                        relativePath = SvnUtils.getRelativePath(root);
                    } catch (SVNClientException ex) {
                        //
                    }
                    fileProps.put(getMessage("LBL_VersioningInfo_Property_RelativePath"), relativePath); //NOI18N
                    fileProps.put(getMessage("LBL_VersioningInfo_Property_Status"), fi.getStatusText()); //NOI18N
                    if ((fi.getStatus() & FileInformation.STATUS_VERSIONED) != 0) {
                        putPropsForVersioned(fileProps, root, fi);
                    }
                }
                EventQueue.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        VersioningInfo.show(properties);
                    }
                });
            }

            private void putPropsForVersioned(LinkedHashMap<String, String> fileProps, File file, FileInformation fi) {
                ISVNStatus status = fi.getEntry(file);
                if (status == null || status.getUrl() == null) {
                    // still probably unversioned
                    return;
                }
                FileStatusCache.FileLabelCache.FileLabelInfo labelInfo;
                labelInfo = cache.getLabelsCache().getLabelInfo(file, true);

                String repositoryRootUrl = getMessage("LBL_VersioningInfo_Property_Unknown"); //NOI18N
                String binaryString = labelInfo.getBinaryString();
                String stickyString = labelInfo.getStickyString();
                try {
                    SVNUrl url = SvnUtils.getRepositoryRootUrl(file);
                    if (url != null) {
                        repositoryRootUrl = SvnUtils.decodeToString(url);
                    }
                } catch (SVNClientException ex) {
                    //
                }
                fileProps.put(getMessage("LBL_VersioningInfo_Property_RepositoryUrl"), status.getUrlString()); //NOI18N
                fileProps.put(getMessage("LBL_VersioningInfo_Property_RepositoryRootUrl"), repositoryRootUrl); //NOI18N
                if (status.getRevision() != null && status.getRevision().getNumber() > 0) {
                    fileProps.put(getMessage("LBL_VersioningInfo_Property_Revision"), status.getRevision().toString()); //NOI18N
                }
                if (!"".equals(binaryString)) { //NOI18N
                    fileProps.put(getMessage("LBL_VersioningInfo_Property_Mime"), binaryString); //NOI18N
                }
                if (!"".equals(stickyString)) { //NOI18N
                    fileProps.put(getMessage("LBL_VersioningInfo_Property_Branch"), stickyString); //NOI18N
                }
                if ((fi.getStatus() & FileInformation.STATUS_IN_REPOSITORY) != 0) {
                    boolean lockedLocally = status.getLockOwner() != null;
                    if (lockedLocally) {
                        fileProps.put(getMessage("LBL_VersioningInfo_Property_Lock"), getMessage("LBL_VersioningInfo_Property_LockPresent")); //NOI18N
                        fileProps.put(getMessage("LBL_VersioningInfo_Property_LockOwner"), status.getLockOwner()); //NOI18N
                        if (status.getLockCreationDate() != null) {
                            fileProps.put(getMessage("LBL_VersioningInfo_Property_LockCreationDate"), DateFormat.getDateTimeInstance().format(status.getLockCreationDate())); //NOI18N
                        }
                        if (status.getLockComment() != null) {
                            fileProps.put(getMessage("LBL_VersioningInfo_Property_LockComment"), status.getLockComment()); //NOI18N
                        }
                    }
                    try {
                        SvnClient client = Subversion.getInstance().getClient(file);
                        SVNUrl url = status.getUrl();
                        ISVNInfo info = null;
                        if (url == null) {
                            LOG.log(Level.WARNING, "putPropsForVersioned: though versioned it has no svn url: {0}, {1}, {2}, {3}, {4}", //NOI18N
                                    new Object[] { file, fi, status.getTextStatus(), status.getUrlString(), status.getFile() });
                        } else {
                            info = client.getInfo(url);
                        }
                        if (info != null) {
                            if (!lockedLocally && info.getLockOwner() != null) {
                                fileProps.put(getMessage("LBL_VersioningInfo_Property_Lock"), getMessage("LBL_VersioningInfo_Property_LockRemote")); //NOI18N
                                fileProps.put(getMessage("LBL_VersioningInfo_Property_LockOwner"), info.getLockOwner()); //NOI18N
                                if (info.getLockCreationDate() != null) {
                                    fileProps.put(getMessage("LBL_VersioningInfo_Property_LockCreationDate"), DateFormat.getDateTimeInstance().format(info.getLockCreationDate())); //NOI18N
                                }
                                if (info.getLockComment() != null) {
                                    fileProps.put(getMessage("LBL_VersioningInfo_Property_LockComment"), info.getLockComment()); //NOI18N
                                }
                            }
                            fileProps.put(getMessage("LBL_VersioningInfo_Property_LastChangedAuthor"), info.getLastCommitAuthor()); //NOI18N
                            fileProps.put(getMessage("LBL_VersioningInfo_Property_LastChangedDate"), DateFormat.getDateTimeInstance().format(info.getLastChangedDate())); //NOI18N
                            fileProps.put(getMessage("LBL_VersioningInfo_Property_LastChangedRevision"), info.getLastChangedRevision().toString()); //NOI18N
                        }
                    } catch (SVNClientException ex) {
                        LOG.log(Level.FINE, null, ex);
                    }
                }
            }
        };
        support.start(rp, repositoryUrl, NbBundle.getMessage(VersioningInfoAction.class, "LBL_VersioningInfo_Progress")); //NOI18N
    }

    private static String getMessage (String resourceName) {
        return NbBundle.getMessage(VersioningInfoAction.class, resourceName);
    }
}
