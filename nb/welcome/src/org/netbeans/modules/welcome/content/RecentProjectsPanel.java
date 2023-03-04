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

package org.netbeans.modules.welcome.content;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import org.netbeans.api.progress.ProgressUtils;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.project.ui.api.RecentProjects;
import org.netbeans.modules.project.ui.api.UnloadedProjectInformation;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;

/**
 * Panel showing all recent projects as clickable buttons.
 * 
 * @author S. Aubrecht
 */
public class RecentProjectsPanel extends JPanel implements Constants, Runnable {
    
    private static final int MAX_PROJECTS = 10;
    private PropertyChangeListener changeListener;
    
    private static final RequestProcessor RP = new RequestProcessor("RecentProjects"); //NOI18N
    
    /** Creates a new instance of RecentProjectsPanel */
    public RecentProjectsPanel() {
        super( new BorderLayout() );
        setOpaque(false);
    }

    @Override
    public void addNotify() {
        super.addNotify();
        removeAll();
        add( startBuildingContent(), BorderLayout.CENTER );
        RecentProjects.getDefault().addPropertyChangeListener( getPropertyChangeListener() );
    }

    @Override
    public void removeNotify() {
        super.removeNotify();
        RecentProjects.getDefault().removePropertyChangeListener( getPropertyChangeListener() );
    }
    
    private PropertyChangeListener getPropertyChangeListener() {
        if( null == changeListener ) {
            changeListener = new PropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent e) {
                    if( RecentProjects.PROP_RECENT_PROJECT_INFO.equals( e.getPropertyName() ) ) {
                        SwingUtilities.invokeLater(RecentProjectsPanel.this);
                    }
                }
            };
        }
        return changeListener;
    }

    @Override
    public void run() {
        removeAll();
        add( rebuildContent(RecentProjects.getDefault().getRecentProjectInformation()), BorderLayout.CENTER );
        invalidate();
        revalidate();
        repaint();
    }
    
    private JPanel startBuildingContent() {
        JPanel panel = new JPanel( new BorderLayout() );
        panel.setOpaque( false );
        JLabel lbl = new JLabel(BundleSupport.getLabel( "LoadingProjects" ));
        lbl.setHorizontalAlignment(JLabel.CENTER);
        panel.add( lbl, BorderLayout.CENTER );
        
        loadProjects();
        
        return panel;
    }
    
    private void loadProjects() {
        RP.post(new Runnable() {

            @Override
            public void run() {
                List<UnloadedProjectInformation> projects = new ArrayList<UnloadedProjectInformation>(RecentProjects.getDefault().getRecentProjectInformation());
                final List<UnloadedProjectInformation> existingProjects = new ArrayList<UnloadedProjectInformation>(projects.size());
                for( UnloadedProjectInformation p : projects ) {
                    try {
                        File projectDir = Utilities.toFile( p.getURL().toURI() );
                        if( !projectDir.exists() || !projectDir.isDirectory() )
                            continue;
                        existingProjects.add(p);
                        if( existingProjects.size() >= MAX_PROJECTS )
                            break;
                    } catch( Exception e ) {
                        Logger.getLogger( RecentProjectsPanel.class.getName() ).log( Level.FINER, null, e );
                    }
                }
                SwingUtilities.invokeLater(new Runnable() {

                    @Override
                    public void run() {
                        removeAll();
                        add( rebuildContent(existingProjects), BorderLayout.CENTER );
                        invalidate();
                        revalidate();
                        repaint();
                    }
                });
            }
        });
    }
    
    private JPanel rebuildContent(List<UnloadedProjectInformation> projects) {
        JPanel panel = new JPanel( new GridBagLayout() );
        panel.setOpaque( false );
        int row = 0;
        for( UnloadedProjectInformation p : projects ) {
            addProject( panel, row++, p );
            if( row >= MAX_PROJECTS )
                break;
        }
        if( 0 == row ) {
            panel.add( new JLabel(BundleSupport.getLabel( "NoRecentProject" )), //NOI18N
                    new GridBagConstraints( 0,row,1,1,1.0,1.0,
                        GridBagConstraints.CENTER, GridBagConstraints.NONE,
                        new Insets(10,10,10,10), 0, 0 ) );
        } else {
            panel.add( new JLabel(), new GridBagConstraints( 0,row,1,1,0.0,1.0,
                GridBagConstraints.CENTER, GridBagConstraints.NONE,
                new Insets(0,0,0,0), 0, 0 ) );
        }
        return panel;
    }
    
    private void addProject( JPanel panel, int row, final UnloadedProjectInformation project ) {
        OpenProjectAction action = new OpenProjectAction( project );
        ActionButton b = new ActionButton( action, project.getURL().toString(), false, "RecentProject" ); //NOI18N
        b.setFont( BUTTON_FONT );
        b.getAccessibleContext().setAccessibleName( b.getText() );
        b.getAccessibleContext().setAccessibleDescription( 
                BundleSupport.getAccessibilityDescription( "RecentProject", b.getText() ) ); //NOI18N
        b.setIcon(project.getIcon());
        panel.add( b, new GridBagConstraints( 0,row,1,1,1.0,0.0,
            GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, new Insets(2,2,2,2), 0, 0 ) );
    }
    
    private class OpenProjectAction extends AbstractAction {
        private final UnloadedProjectInformation project;
        public OpenProjectAction( UnloadedProjectInformation project ) {
            super( project.getDisplayName(), project.getIcon() );
            this.project = project;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            Runnable r = new Runnable() {
                @Override
                public void run() {
                    URL url = project.getURL();
                    Project prj = null;

                    FileObject dir = URLMapper.findFileObject( url );
                    if ( dir != null && dir.isFolder() ) {
                        try {
                            prj = ProjectManager.getDefault().findProject( dir );
                        }
                        catch ( IOException ioEx ) {
                            // Ignore invalid folders
                        }
                    }

                    if ( prj != null ) {
                        OpenProjects.getDefault().open( new Project[] { prj }, false, true );
                    } else {
                        String msg = BundleSupport.getMessage("ERR_InvalidProject", project.getDisplayName()); //NOI18N
                        NotifyDescriptor nd = new NotifyDescriptor.Message( msg );
                        DialogDisplayer.getDefault().notify( nd );
                        startBuildingContent();
                    }
                }
            };
            ProgressUtils.runOffEventDispatchThread( r, BundleSupport.getLabel("OPEN_RECENT_PROJECT"), new AtomicBoolean(false), false );
        }
    }
}
