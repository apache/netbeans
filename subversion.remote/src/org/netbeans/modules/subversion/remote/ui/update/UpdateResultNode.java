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
package org.netbeans.modules.subversion.remote.ui.update;

import java.awt.EventQueue;
import org.openide.nodes.*;
import org.openide.util.NbBundle;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;

import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;
import java.util.logging.Level;
import org.netbeans.modules.subversion.remote.Subversion;
import org.netbeans.modules.subversion.remote.SvnModuleConfig;
import org.netbeans.modules.subversion.remote.api.SVNClientException;
import org.netbeans.modules.subversion.remote.client.SvnClientExceptionHandler;
import org.netbeans.modules.subversion.remote.util.Context;
import org.netbeans.modules.subversion.remote.util.SvnUtils;
import org.netbeans.modules.remotefs.versioning.api.VCSFileProxySupport;
import org.openide.util.lookup.Lookups;

/**
 * The node that is rendered in the Update Results view.
 * 
 * 
 */
class UpdateResultNode extends AbstractNode {
    
    private final FileUpdateInfo info;

    static final String COLUMN_NAME_NAME        = "name";   // NOI18N
    static final String COLUMN_NAME_PATH        = "path";   // NOI18N
    static final String COLUMN_NAME_STATUS      = "status"; // NOI18N
    
    private final MessageFormat conflictFormat  = new MessageFormat("<font color=\"#FF0000\">{0}</font>");  // NOI18N
    private final MessageFormat mergedFormat    = new MessageFormat("<font color=\"#0000FF\">{0}</font>");  // NOI18N
    private final MessageFormat removedFormat   = new MessageFormat("<font color=\"#999999\">{0}</font>");  // NOI18N
    private final MessageFormat addedFormat     = new MessageFormat("<font color=\"#008000\">{0}</font>");    // NOI18N   
    
    private String statusDisplayName;
    
    private String htmlDisplayName;
    private String relativePath;

    /**
     * I/O accessed, do not call in AWT
     * @param info
     */
    public UpdateResultNode(FileUpdateInfo info) {
        super(Children.LEAF, Lookups.fixed(new Object [] { info }));
        assert !EventQueue.isDispatchThread();
        this.info = info;
        initProperties();
        refreshHtmlDisplayName();
    }

    public FileUpdateInfo getInfo() {
        return info;
    }

    @Override
    public String getName() {
        String name = info.getFile().getName() + ( (info.getAction() & FileUpdateInfo.ACTION_TYPE_PROPERTY) != 0 ? " - Property" : "" ); //NOI18N
        return name;
    }
    
    /**
     * Provide cookies to actions.
     * If a node represents primary file of a DataObject
     * it has respective DataObject cookies.
     */
    @Override
    public <T extends Node.Cookie> T getCookie(Class<T> klass) {
        FileObject fo = info.getFile().toFileObject();
        if (fo != null) {
            try {
                DataObject dobj = DataObject.find(fo);
                if (fo.equals(dobj.getPrimaryFile())) {
                    return dobj.getLookup().lookup(klass);
                }
            } catch (DataObjectNotFoundException e) {
                // ignore file without data objects
            }
        }
        return super.getLookup().lookup(klass);
    }

    private void initProperties() {
        Sheet sheet = Sheet.createDefault();
        Sheet.Set ps = Sheet.createPropertiesSet();
        
        ps.put(new UpdateResultNode.NameProperty());
        ps.put(new UpdateResultNode.PathProperty());
        ps.put(new UpdateResultNode.FileStatusProperty());
        
        sheet.put(ps);
        setSheet(sheet);        
    }
    
    private void refreshHtmlDisplayName() {
        String name = getName();        
        if ( (info.getAction() & FileUpdateInfo.ACTION_ADDED) != 0 ) { 
            htmlDisplayName = addedFormat.format(new Object [] {  name } );     
            statusDisplayName = NbBundle.getMessage(UpdateResultNode.class, "LBL_Status_Added"); // NOI18N 
        } else if ( (info.getAction() & FileUpdateInfo.ACTION_CONFLICTED) != 0 ) {
            htmlDisplayName = conflictFormat.format(new Object [] { name });
            statusDisplayName = NbBundle.getMessage(UpdateResultNode.class, "LBL_Status_Conflict"); // NOI18N
        } else if ( (info.getAction() & FileUpdateInfo.ACTION_DELETED) != 0 ) {
            htmlDisplayName = removedFormat.format(new Object [] { name });            
            statusDisplayName = NbBundle.getMessage(UpdateResultNode.class, "LBL_Status_Removed"); // NOI18N
        } else if ( (info.getAction() & FileUpdateInfo.ACTION_MERGED) != 0 ) {
            htmlDisplayName = mergedFormat.format(new Object [] { name });            
            statusDisplayName = NbBundle.getMessage(UpdateResultNode.class, "LBL_Status_Merged"); // NOI18N
        } else if ( (info.getAction() & FileUpdateInfo.ACTION_UPDATED) != 0 ) {
            htmlDisplayName = name;            
            statusDisplayName = NbBundle.getMessage(UpdateResultNode.class, "LBL_Status_Updated"); // NOI18N
        } else if ( (info.getAction() & FileUpdateInfo.ACTION_CONFLICTED_RESOLVED) != 0 ) {
            htmlDisplayName = name;            
            statusDisplayName = NbBundle.getMessage(UpdateResultNode.class, "LBL_Status_Conflict_Resolved"); // NOI18N
        } else {
            throw new IllegalStateException("Unhandled update type: " + info.getAction()); // NOI18N
        }
        fireDisplayNameChange(htmlDisplayName, htmlDisplayName);
    }

    @Override
    public String getHtmlDisplayName() {
        return htmlDisplayName;
    }

    public void refresh() {
        refreshHtmlDisplayName();
    }

    private String getLocation () {
        if (relativePath == null) {
            try {
                relativePath = SvnModuleConfig.getDefault(VCSFileProxySupport.getFileSystem(info.getFile())).isRepositoryPathPrefixed()
                        ? SvnUtils.decodeToString(SvnUtils.getRepositoryUrl(info.getFile())) : SvnUtils.getRelativePath(info.getFile());
            } catch (SVNClientException ex) {
                SvnClientExceptionHandler.notifyException(new Context(info.getFile()), ex, false, false);
                relativePath = "";                                      //NOI18N
            }
        }
        return relativePath;
    }

    private abstract class SyncFileProperty extends PropertySupport.ReadOnly<String> {
        protected SyncFileProperty(String name, Class<String> type, String displayName, String shortDescription) {
            super(name, type, displayName, shortDescription);
        }
        @Override
        public String toString() {
            try {
                return getValue();
            } catch (Exception e) {
                Subversion.LOG.log(Level.INFO, null, e);
                return e.getLocalizedMessage();
            }
        }
    }
    
    private class PathProperty extends UpdateResultNode.SyncFileProperty {
        private final String shortPath;
        public PathProperty() {
            super(COLUMN_NAME_PATH, String.class, NbBundle.getMessage(UpdateResultNode.class, "LBL_Path_Name"), NbBundle.getMessage(UpdateResultNode.class, "LBL_Path_Desc")); // NOI18N
            shortPath = getLocation();
            setValue("sortkey", shortPath + "\t" + UpdateResultNode.this.getName()); // NOI18N
        }
        @Override
        public String getValue() throws IllegalAccessException, InvocationTargetException {
            return shortPath;
        }
    } 
    
    private class NameProperty extends UpdateResultNode.SyncFileProperty {
        public NameProperty() {
            super(COLUMN_NAME_NAME, String.class, NbBundle.getMessage(UpdateResultNode.class, "LBL_Name_Name"), NbBundle.getMessage(UpdateResultNode.class, "LBL_Name_Desc")); // NOI18N
            setValue("sortkey", UpdateResultNode.this.getName()); // NOI18N
        }
        @Override
        public String getValue() throws IllegalAccessException, InvocationTargetException {
            return UpdateResultNode.this.getDisplayName();
        }
    }

    private final String [] zeros = new String [] { "", "00", "0", "" }; // NOI18N
    
    private class FileStatusProperty extends UpdateResultNode.SyncFileProperty {        
        private final String shortPath;        
        public FileStatusProperty() {            
            super(COLUMN_NAME_STATUS, String.class, NbBundle.getMessage(UpdateResultNode.class, "LBL_Status_Name"), NbBundle.getMessage(UpdateResultNode.class, "LBL_Status_Desc"));            
            shortPath = getLocation();
            String sortable = Integer.toString(info.getAction());
            setValue("sortkey", zeros[sortable.length()] + sortable + "\t" + shortPath + "\t" + UpdateResultNode.this.getName()); //NOI18N
        }
        @Override
        public String getValue() throws IllegalAccessException, InvocationTargetException {
            return statusDisplayName;
        }
    }
}
