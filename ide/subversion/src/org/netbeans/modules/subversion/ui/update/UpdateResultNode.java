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
package org.netbeans.modules.subversion.ui.update;

import java.awt.EventQueue;
import org.openide.nodes.*;
import org.openide.util.NbBundle;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;

import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;
import java.util.logging.Level;
import org.netbeans.modules.subversion.Subversion;
import org.netbeans.modules.subversion.SvnModuleConfig;
import org.netbeans.modules.subversion.client.SvnClientExceptionHandler;
import org.netbeans.modules.subversion.options.AnnotationColorProvider;
import org.netbeans.modules.subversion.util.SvnUtils;
import org.openide.util.lookup.Lookups;
import org.tigris.subversion.svnclientadapter.SVNClientException;

/**
 * The node that is rendered in the Update Results view.
 * 
 * @author Maros Sandor
 */
class UpdateResultNode extends AbstractNode {
    
    private final FileUpdateInfo info;

    static final String COLUMN_NAME_NAME        = "name";   // NOI18N
    static final String COLUMN_NAME_PATH        = "path";   // NOI18N
    static final String COLUMN_NAME_STATUS      = "status"; // NOI18N
    
    private String statusDisplayName;
    
    private String htmlDisplayName;
    private String relativePath;
    private boolean displayNameHasHtml;

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

    public String getName() {
        String name = info.getFile().getName() + ( (info.getAction() & FileUpdateInfo.ACTION_TYPE_PROPERTY) != 0 ? " - Property" : "" );        
        return name;
    }
    
    /**
     * Provide cookies to actions.
     * If a node represents primary file of a DataObject
     * it has respective DataObject cookies.
     */
    public <T extends Node.Cookie> T getCookie(Class<T> klass) {
        FileObject fo = FileUtil.toFileObject(info.getFile());
        if (fo != null) {
            try {
                DataObject dobj = DataObject.find(fo);
                if (fo.equals(dobj.getPrimaryFile())) {
                    return dobj.getCookie(klass);
                }
            } catch (DataObjectNotFoundException e) {
                // ignore file without data objects
            }
        }
        return super.getCookie(klass);
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
        AnnotationColorProvider acp = AnnotationColorProvider.getInstance();
        displayNameHasHtml = false;
        if ( (info.getAction() & FileUpdateInfo.ACTION_ADDED) != 0 ) { 
            htmlDisplayName = acp.ADDED_LOCALLY_FILE.getFormat().format(new Object [] {  name, "" } );     
            statusDisplayName = NbBundle.getMessage(UpdateResultNode.class, "LBL_Status_Added"); // NOI18N 
            displayNameHasHtml = true;
        } else if ( (info.getAction() & FileUpdateInfo.ACTION_CONFLICTED) != 0 ) {
            htmlDisplayName = acp.CONFLICT_FILE.getFormat().format(new Object [] { name, "" });
            statusDisplayName = NbBundle.getMessage(UpdateResultNode.class, "LBL_Status_Conflict"); // NOI18N
            displayNameHasHtml = true;
        } else if ( (info.getAction() & FileUpdateInfo.ACTION_DELETED) != 0 ) {
            htmlDisplayName = acp.REMOVED_LOCALLY_FILE.getFormat().format(new Object [] { name, "" });            
            statusDisplayName = NbBundle.getMessage(UpdateResultNode.class, "LBL_Status_Removed"); // NOI18N
            displayNameHasHtml = true;
        } else if ( (info.getAction() & FileUpdateInfo.ACTION_MERGED) != 0 ) {
            htmlDisplayName = acp.MODIFIED_LOCALLY_FILE.getFormat().format(new Object [] { name, "" });            
            statusDisplayName = NbBundle.getMessage(UpdateResultNode.class, "LBL_Status_Merged"); // NOI18N
            displayNameHasHtml = true;
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

    public String getHtmlDisplayName() {
        return htmlDisplayName;
    }

    public String getHtmlDisplayName (boolean addHtmlPrefix) {
        String retval = htmlDisplayName;
        if (displayNameHasHtml) {
            retval = "<html>" + retval;
        }
        return retval;
    }

    public void refresh() {
        refreshHtmlDisplayName();
    }

    private String getLocation () {
        if (relativePath == null) {
            try {
                relativePath = SvnModuleConfig.getDefault().isRepositoryPathPrefixed()
                        ? SvnUtils.decodeToString(SvnUtils.getRepositoryUrl(info.getFile())) : SvnUtils.getRelativePath(info.getFile());
            } catch (SVNClientException ex) {
                SvnClientExceptionHandler.notifyException(ex, false, false);
                relativePath = "";                                      //NOI18N
            }
        }
        return relativePath;
    }

    private abstract class SyncFileProperty extends PropertySupport.ReadOnly<String> {
        protected SyncFileProperty(String name, Class<String> type, String displayName, String shortDescription) {
            super(name, type, displayName, shortDescription);
        }
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
        private String shortPath;
        public PathProperty() {
            super(COLUMN_NAME_PATH, String.class, NbBundle.getMessage(UpdateResultNode.class, "LBL_Path_Name"), NbBundle.getMessage(UpdateResultNode.class, "LBL_Path_Desc")); // NOI18N
            shortPath = getLocation();
            setValue("sortkey", shortPath + "\t" + UpdateResultNode.this.getName()); // NOI18N
        }
        public String getValue() throws IllegalAccessException, InvocationTargetException {
            return shortPath;
        }
    } 
    
    private class NameProperty extends UpdateResultNode.SyncFileProperty {
        public NameProperty() {
            super(COLUMN_NAME_NAME, String.class, NbBundle.getMessage(UpdateResultNode.class, "LBL_Name_Name"), NbBundle.getMessage(UpdateResultNode.class, "LBL_Name_Desc")); // NOI18N
            setValue("sortkey", UpdateResultNode.this.getName()); // NOI18N
        }
        public String getValue() throws IllegalAccessException, InvocationTargetException {
            return UpdateResultNode.this.getDisplayName();
        }
    }

    private final String [] zeros = new String [] { "", "00", "0", "" }; // NOI18N
    
    private class FileStatusProperty extends UpdateResultNode.SyncFileProperty {        
        private String shortPath;        
        public FileStatusProperty() {            
            super(COLUMN_NAME_STATUS, String.class, NbBundle.getMessage(UpdateResultNode.class, "LBL_Status_Name"), NbBundle.getMessage(UpdateResultNode.class, "LBL_Status_Desc"));            
            shortPath = getLocation();
            String sortable = Integer.toString(info.getAction());
            setValue("sortkey", zeros[sortable.length()] + sortable + "\t" + shortPath + "\t" + UpdateResultNode.this.getName());
        }
        public String getValue() throws IllegalAccessException, InvocationTargetException {
            return statusDisplayName;
        }
    }
}
