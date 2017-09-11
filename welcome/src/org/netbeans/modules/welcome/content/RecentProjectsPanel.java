/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
