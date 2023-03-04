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

package org.netbeans.modules.subversion.ui.status;

import java.io.IOException;
import org.openide.nodes.*;
import org.openide.util.*;
import org.openide.util.lookup.Lookups;
import org.openide.util.actions.SystemAction;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.netbeans.modules.subversion.SvnFileNode;
import org.netbeans.modules.subversion.FileInformation;
import org.netbeans.modules.subversion.Subversion;
import org.netbeans.modules.subversion.ui.update.ResolveConflictsAction;
import org.netbeans.modules.subversion.ui.diff.DiffAction;
import org.netbeans.modules.subversion.util.SvnUtils;
import javax.swing.*;
import java.lang.reflect.InvocationTargetException;
import java.io.File;
import java.util.logging.Level;
import org.netbeans.modules.subversion.SvnModuleConfig;
import org.netbeans.modules.subversion.WorkingCopyAttributesCache;
import org.netbeans.modules.subversion.client.SvnClientExceptionHandler;
import org.tigris.subversion.svnclientadapter.SVNClientException;

/**
 * The node that is rendered in the SyncTable view. It gets values to display from the
 * CvsFileNode which serves as the 'data' node for this 'visual' node.
 * 
 * @author Maros Sandor
 */
public class SyncFileNode extends AbstractNode {
    
    private SvnFileNode node;

    static final String COLUMN_NAME_NAME        = "name"; // NOI18N
    static final String COLUMN_NAME_PATH        = "path"; // NOI18N
    static final String COLUMN_NAME_STATUS      = "status"; // NOI18N
    static final String COLUMN_NAME_BRANCH      = "branch"; // NOI18N
    
    private String htmlDisplayName;

    private RequestProcessor.Task repoload;

    private final VersioningPanel panel;
    private DataObject dobj;

    SyncFileNode(SvnFileNode node, VersioningPanel _panel) {
        this(Children.LEAF, node, _panel);
        
    }

    private SyncFileNode(Children children, SvnFileNode node, VersioningPanel _panel) {
        super(children, Lookups.fixed(node.getLookupObjects()));
        this.node = node;
        this.panel = _panel;
        init();
        initProperties();
        refreshHtmlDisplayName();
    }
    
    /**
     * Careful, returned file may not be normalized
     * @return 
     */
    public File getFile() {
        return node.getFile();
    }

    public FileInformation getFileInformation() {
        return node.getInformation();
    }
    
    @Override
    public String getName() {
        return node.getName();
    }
    
    public String getCopy() {
        return node.getCopy();
    }

    @Override
    public Action getPreferredAction() {
        if ((node.getInformation().getStatus() & FileInformation.STATUS_VERSIONED_CONFLICT) != 0) {
            return SystemAction.get(ResolveConflictsAction.class);
        }
        return SystemAction.get(DiffAction.class);
    }

    /**
     * Provide cookies to actions.
     * If a node represents primary file of a DataObject
     * it has respective DataObject cookies.
     */
    @Override
    public <T extends Cookie> T getCookie(Class<T> klass) {
        if (dobj == null) {
            return super.getCookie(klass);
        } else {
            return dobj.getCookie(klass);
        }
    }

    private void init () {
        FileObject fo = node.getFileObject();
        if (fo != null) {
            try {
                dobj = DataObject.find(fo);
            } catch (DataObjectNotFoundException e) {
                // ignore file without data objects
            }
        }
    }

    private void initProperties() {
        if (node.getFile().isDirectory()) setIconBaseWithExtension("org/openide/loaders/defaultFolder.gif"); // NOI18N

        Sheet sheet = Sheet.createDefault();
        Sheet.Set ps = Sheet.createPropertiesSet();
        
        ps.put(new NameProperty());
        ps.put(new PathProperty());
        ps.put(new StatusProperty());
        ps.put(new BranchProperty());
        
        sheet.put(ps);
        setSheet(sheet);        
    }

    private void refreshHtmlDisplayName() {
        FileInformation info = node.getInformation(); 
        int status = info.getStatus();
        // Special treatment: Mergeable status should be annotated as Conflict in Versioning view according to UI spec
        if (status == FileInformation.STATUS_VERSIONED_MERGE) {
            status = FileInformation.STATUS_VERSIONED_CONFLICT;
        }
        htmlDisplayName = Subversion.getInstance().getAnnotator().annotateNameHtml(node.getFile().getName(), info, null);
        fireDisplayNameChange(node.getName(), node.getName());
    }

    @Override
    public String getHtmlDisplayName() {
        return htmlDisplayName;
    }

    public void refresh() {
        refreshHtmlDisplayName();
    }

    private abstract class SyncFileProperty extends org.openide.nodes.PropertySupport.ReadOnly<String> {

        protected SyncFileProperty(String name, String displayName, String shortDescription) {
            super(name, String.class, displayName, shortDescription);
        }

        @Override
        public String toString() {
            try {
                return getValue().toString();
            } catch (Exception e) {
                Subversion.LOG.log(Level.INFO, null, e);
                return e.getLocalizedMessage();
            }
        }
    }
    
    private class BranchProperty extends SyncFileProperty {

        public BranchProperty() {
            super(COLUMN_NAME_BRANCH, NbBundle.getMessage(SyncFileNode.class, "BK2001"), NbBundle.getMessage(SyncFileNode.class, "BK2002")); // NOI18N
        }

        @Override
        public String getValue() {
            String copyName = node.getCopy();
            return copyName == null ? "" : copyName;
        }
    }
    
    private class PathProperty extends SyncFileProperty {

        private String shortPath;
        private boolean reading;

        public PathProperty() {
            super(COLUMN_NAME_PATH, NbBundle.getMessage(SyncFileNode.class, "BK2003"), NbBundle.getMessage(SyncFileNode.class, "BK2004")); // NOI18N
            setValue("sortkey", "\u65000\t" + SyncFileNode.this.getName()); // NOI18N
        }

        @Override
        public String getValue() throws IllegalAccessException, InvocationTargetException {
            if (shortPath == null && !reading) {
                reading = true;
                Runnable run = new Runnable() {
                    @Override
                    public void run() {
                        if (shortPath != null) {
                            return;
                        }
                        try {
                            shortPath = SvnModuleConfig.getDefault().isRepositoryPathPrefixed()
                                    ? SvnUtils.decodeToString(SvnUtils.getRepositoryUrl(node.getFile())) : SvnUtils.getRelativePath(node.getFile());
                        } catch (SVNClientException ex) {
                            if (WorkingCopyAttributesCache.getInstance().isSuppressed(ex)) {
                                try {
                                    WorkingCopyAttributesCache.getInstance().logSuppressed(ex, node.getFile());
                                } catch (SVNClientException e) { }
                            } else {
                                SvnClientExceptionHandler.notifyException(ex, false, false);
                            }
                        }
                        if (shortPath == null) {
                            shortPath = org.openide.util.NbBundle.getMessage(SyncFileNode.class, "LBL_Location_NotInRepository"); // NOI18N
                        }
                        SwingUtilities.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                setValue("sortkey", shortPath + "\t" + SyncFileNode.this.getName()); // NOI18N
                                firePropertyChange(COLUMN_NAME_PATH, null, null);
                            }
                        });
                    }
                };
                repoload = Subversion.getInstance().getRequestProcessor().post(run);
            }
            return shortPath == null ? org.openide.util.NbBundle.getMessage(SyncFileNode.class, "LBL_RepositoryPath_LoadingProgress") : shortPath;
        }
    }

    // XXX it's not probably called, are there another Node lifecycle events
    @Override
    public void destroy() throws IOException {
        super.destroy();
        if (repoload != null) {
            repoload.cancel();
        }
    }
    
    private class NameProperty extends SyncFileProperty {

        public NameProperty() {
            super(COLUMN_NAME_NAME, NbBundle.getMessage(SyncFileNode.class, "BK2005"), NbBundle.getMessage(SyncFileNode.class, "BK2006")); // NOI18N
            setValue("sortkey", SyncFileNode.this.getName()); // NOI18N
        }

        @Override
        public String getValue() throws IllegalAccessException, InvocationTargetException {
            return SyncFileNode.this.getDisplayName();
        }
    }

    private static final String [] zeros = new String [] { "", "00", "0", "" }; // NOI18N
    
    private class StatusProperty extends SyncFileProperty {
        private final FileInformation finfo;
        
        public StatusProperty() {
            super(COLUMN_NAME_STATUS, NbBundle.getMessage(SyncFileNode.class, "BK2007"), NbBundle.getMessage(SyncFileNode.class, "BK2008")); // NOI18N
            finfo = node.getInformation();
            finfo.getEntry(node.getFile());  // XXX not interested in return value, side effect loads ISVNStatus structure
            String shortPath = "path";//SvnUtils.getRelativePath(node.getFile()); // NOI18N
            String sortable = Integer.toString(SvnUtils.getComparableStatus(finfo.getStatus()));
            setValue("sortkey", zeros[sortable.length()] + sortable + "\t" + shortPath + "\t" + SyncFileNode.this.getName()); // NOI18N
        }

        @Override
        public String getValue() throws IllegalAccessException, InvocationTargetException {
            int mask = panel.getDisplayStatuses();
            return finfo.getStatusText(mask);
        }
    }
}
