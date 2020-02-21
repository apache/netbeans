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

package org.netbeans.modules.cnd.editor.filecreation;

import java.awt.Component;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentListener;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.cnd.api.remote.RemoteProject;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.remote.spi.FileSystemProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.ChangeSupport;
import org.openide.util.NbBundle;

/**
 *
 */
public abstract class CndPanelGUI  extends javax.swing.JPanel implements DocumentListener {

    protected final Project project;
    protected final char fileSeparatorChar;
    protected final String fileSeparator;
    protected SourceGroup[] folders;

    private static final java.awt.Dimension PREF_DIM = new java.awt.Dimension (500, 340);
    protected static final String NEW_FILE_PREFIX = getMessage("LBL_NewCndFileChooserPanelGUI_NewFilePrefix"); // NOI18N
    protected final ListCellRenderer CELL_RENDERER = new GroupCellRenderer();
        
    public CndPanelGUI(Project project, SourceGroup[] folders) {
        this.project = project;
        this.folders = folders;
        fileSeparatorChar = FileSystemProvider.getFileSeparatorChar(project.getProjectDirectory());
        fileSeparator = ""+fileSeparatorChar;
    }

    protected final ChangeSupport changeSupport = new ChangeSupport(this);
    public void addChangeListener(ChangeListener l) {
        changeSupport.addChangeListener(l);
    }
    
    public void removeChangeListener(ChangeListener l) {
        changeSupport.removeChangeListener(l);
    }
    
    public abstract SourceGroup getTargetGroup();
    public abstract String getTargetFolder();
    public abstract String getTargetName();
    protected abstract void updateCreatedFile();
    public abstract void initValues( FileObject template, FileObject preselectedFolder, String documentName );

    protected Project getProject() {
        return project;
    }

    @Override
    public void changedUpdate(javax.swing.event.DocumentEvent e) {
        updateCreatedFile();
    }    
    
    @Override
    public void insertUpdate(javax.swing.event.DocumentEvent e) {
        updateCreatedFile();
    }
    
    @Override
    public void removeUpdate(javax.swing.event.DocumentEvent e) {
        updateCreatedFile();
    }

    @Override
    public java.awt.Dimension getPreferredSize() {
        return PREF_DIM;
    }
    
    protected String getProjectDisplayName(Project project) {
        String name = ProjectUtils.getInformation(project).getDisplayName();
        RemoteProject remoteProject = project.getLookup().lookup(RemoteProject.class);
        if (remoteProject != null) {
            final ExecutionEnvironment env = remoteProject.getSourceFileSystemHost();
            if (env.isRemote()) {
                name += " [" + env.getDisplayName() + ']'; // NOI18N
                //name = "<html>" + name + "<font color='!textInactiveText'>" + env.getDisplayName() + "</html>";
            }
        }
        return name;
    }
    
    protected static SourceGroup getPreselectedGroup( SourceGroup[] groups, FileObject folder ) {        
        for( int i = 0; folder != null && i < groups.length; i++ ) {
            if( FileUtil.isParentOf( groups[i].getRootFolder(), folder )
                || groups[i].getRootFolder().equals(folder)) {
                return groups[i];
            }
        }
        return groups[0];
    }
    
    protected String getRelativeNativeName( FileObject root, FileObject folder ) {
        if (root == null) {
            throw new NullPointerException("null root passed to getRelativeNativeName"); // NOI18N
        }
        
        String path;
        
        if (folder == null) {
            path = ""; // NOI18N
        }
        else {
            path = FileUtil.getRelativePath( root, folder );            
        }
        
        return path == null ? "" : path.replace( '/', fileSeparatorChar ); // NOI18N
    }

    protected static String generateUniqueSuffix(FileObject folder, String prefix, String... extensions) {
        for (int i = 0; true; ++i) {
            String suffix = i == 0? "" : String.valueOf(i);
            String filename = prefix + suffix;
            boolean unique = true;
            for (String ext : extensions) {
                if (folder.getFileObject(filename, ext) != null) {
                    unique = false;
                    break;
                }
            }
            if (unique) {
                return suffix;
            }
        }
    }

    protected static String getMessage(String name) {
        return NbBundle.getMessage( CndPanelGUI.class, name);
    }

    protected final class GroupCellRenderer extends JLabel implements ListCellRenderer {
    
        public GroupCellRenderer() {
            setOpaque( true );
        }
        
        @Override
        public Component getListCellRendererComponent( JList list, Object value, int index, boolean isSelected, boolean cellHasFocus ) {
            if (value instanceof SourceGroup) {
                SourceGroup group = (SourceGroup)value;
                FileObject rootFolder = group.getRootFolder();
                String displayName = rootFolder.getPath();
                if (FileSystemProvider.getExecutionEnvironment(rootFolder).isLocal()) {
                    displayName = displayName.replace('/', fileSeparatorChar);
                }
                setText(displayName);                
                setIcon( group.getIcon( false ) );
            } 
            else {
                setText( value == null? "" : value.toString() ); // NOI18N
                setIcon( null );
            }
            if ( isSelected ) {
                setBackground(list.getSelectionBackground());
                setForeground(list.getSelectionForeground());             
            }
            else {
                setBackground(list.getBackground());
                setForeground(list.getForeground());
             
            }
            return this;        
        }
                
    }
}
