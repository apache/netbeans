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

package org.netbeans.modules.tasklist.projectint;

import java.awt.Image;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.spi.tasklist.TaskScanningScope;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.windows.TopComponent;

/**
 * Task scanning scope for all opened projects.
 * 
 * @author S. Aubrecht
 */
public class OpenedProjectsScanningScope extends TaskScanningScope 
        implements PropertyChangeListener {
    
    private Callback callback;
    private InstanceContent lookupContent = new InstanceContent();
    private Lookup lookup;
    private Project[] currentProjects;
    
    /** Creates a new instance of OpenedProjectsScanningScope 
     * @param displayName 
     * @param description
     * @param icon 
     */
    private OpenedProjectsScanningScope( String displayName, String description, Image icon ) {
        super( displayName, description, icon );
        Map<String,String> labels = new HashMap<String,String>(1);
        labels.put( Utils.KEY_STATUS_BAR_LABEL, 
                NbBundle.getMessage(OpenedProjectsScanningScope.class, "LBL_OpenedProjectsStatusBar") ); //NOI18N
        lookupContent.add( labels );
    }
        
    /**
     * @return New instance of OpenedProjectsScanningScope
     */
    public static OpenedProjectsScanningScope create() {
        return new OpenedProjectsScanningScope( 
                NbBundle.getBundle( MainProjectScanningScope.class ).getString( "LBL_OpenedProjectsScope" ), //NOI18N
                NbBundle.getBundle( MainProjectScanningScope.class ).getString( "HINT_OpenedProjectsScope" ), //NOI18N
                ImageUtilities.loadImage( "org/netbeans/modules/tasklist/projectint/opened_projects_scope.png" ) //NOI18N
                );
    }
    
    public Iterator<FileObject> iterator() {
        return new OpenedProjectsIterator();
    }
    
    @Override
    public boolean isInScope( FileObject resource ) {
        if( null == resource || null == currentProjects )
            return false;
        for( Project p : currentProjects ) {
            Sources sources = ProjectUtils.getSources( p );
            SourceGroup[] groups = sources.getSourceGroups( Sources.TYPE_GENERIC );
            for( SourceGroup group : groups ) {
                FileObject rootFolder = group.getRootFolder();
                if( FileUtil.isParentOf( rootFolder, resource ) || rootFolder.equals( resource ) )
                    return true;
            }
        }
        
        return false;
    }

    public Lookup getLookup() {
        synchronized( this ) {
            if( null == lookup ) {
                lookup = new AbstractLookup( lookupContent );
            }
        }
        return lookup;
    }
    
    public void attach( Callback newCallback ) {
        Project[] openProjects = OpenProjects.getDefault().getOpenProjects();
        synchronized( this ) {
            if( null != newCallback && null == callback ) {
                OpenProjects.getDefault().addPropertyChangeListener( this );
                TopComponent.getRegistry().addPropertyChangeListener( this );
                setLookupContent( openProjects );
            } else if( null == newCallback && null != callback ) {
                OpenProjects.getDefault().removePropertyChangeListener( this );
                TopComponent.getRegistry().removePropertyChangeListener( this );
                setLookupContent( null );
            }
            this.callback = newCallback;
        }
    }
    
    public void propertyChange( PropertyChangeEvent e ) {
        if( OpenProjects.PROPERTY_OPEN_PROJECTS.equals( e.getPropertyName() ) ) {
            Project[] projects = OpenProjects.getDefault().getOpenProjects();
            synchronized( this ) {
                if( null != callback ) {
                    setLookupContent( projects );
                    callback.refresh();
                }
            }
        }
    }
    
    private void setLookupContent( Project[] newProjects ) {
        if( null != currentProjects ) {
            for( Project p : currentProjects ) {
                lookupContent.remove( p );
            }
        }
        if( null != newProjects ) {
            for( Project p : newProjects ) {
                lookupContent.add( p );
            }
        }
        currentProjects = newProjects;
    }
}
