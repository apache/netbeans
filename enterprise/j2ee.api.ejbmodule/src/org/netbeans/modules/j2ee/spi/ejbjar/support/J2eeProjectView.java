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

package org.netbeans.modules.j2ee.spi.ejbjar.support;

import java.awt.Image;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Action;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.EventListenerList;
import org.netbeans.api.project.Project;
import org.netbeans.api.queries.VisibilityQuery;
import org.netbeans.modules.j2ee.api.ejbjar.EjbJar;
import org.netbeans.modules.j2ee.ejbjar.project.ui.EjbContainerNode;
import org.netbeans.modules.j2ee.ejbjar.project.ui.ServerResourceNode;
import org.netbeans.modules.j2ee.spi.ejbjar.EjbNodesFactory;
import org.openide.actions.FindAction;
import org.openide.filesystems.FileObject;
import org.openide.loaders.ChangeableDataFilter;
import org.openide.loaders.DataFilter;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;

/** Utility class for creating J2EE project nodes.
 *
 * @author Pavel Buzek
 */
public final class J2eeProjectView {
    
    /**
     * The programmatic name (returned by {@link org.openide.nodes.Node.getName})
     * of the node created by the {@link createConfigFilesView} method.
     *
     * @since 1.6
     */
    public static final String CONFIG_FILES_VIEW_NAME = "configurationFiles"; // NOI18N
    
    private static final Logger LOGGER = Logger.getLogger(J2eeProjectView.class.getName());

    private static EjbNodesFactory factoryInstance = null;
    
    private J2eeProjectView() {
    }

    /** Returns an instance of EjbNodesFactory if there is any
     * available in default lookup. Otherwise returns null.
     */
    public static EjbNodesFactory getEjbNodesFactory () {
        if (factoryInstance == null) {
            factoryInstance = Lookup.getDefault().lookup(EjbNodesFactory.class);
        }
        if (factoryInstance == null) {
            Logger.getLogger("global").log(Level.INFO, "No EjbNodesFactory instance available: Enterprise Beans nodes cannot be creater");
        }
        return factoryInstance;
    }
    
    public static Node createServerResourcesNode (Project p) {
        try {
            return new ServerResourceNode(p);
        } catch (DataObjectNotFoundException ex) {
            // Happens in cases of project deletion or unaccessible sources - do not display broken logical view
            LOGGER.log(Level.INFO, "Project directory FileObject became invalid.", ex);
            return null;
        }
    }
    
    public static Node createEjbsView(EjbJar ejbModule, Project p){
        try {
            return new EjbContainerNode(ejbModule, p, getEjbNodesFactory());
        } catch (DataObjectNotFoundException ex) {
            // Happens in cases of project deletion or unaccessible sources - do not display broken logical view
            LOGGER.log(Level.INFO, "Project directory FileObject became invalid.", ex);
            return null;
        }
    }
    
    public static Node createConfigFilesView (FileObject folder) {
        return new DocBaseNode(DataFolder.findFolder(folder));
    }
    
    private static final class DocBaseNode extends FilterNode {
        private static final DataFilter VISIBILITY_QUERY_FILTER = new VisibilityQueryDataFilter();

        private static Image CONFIGURATION_FILES_BADGE =
                ImageUtilities.loadImage( "org/netbeans/modules/j2ee/ejbjar/project/ui/ejbjar.gif", true ); // NOI18N

        public DocBaseNode(DataFolder folder) {
            super(folder.getNodeDelegate(), folder.createNodeChildren(VISIBILITY_QUERY_FILTER));
        }

        public Image getIcon( int type ) {
            return computeIcon( false, type );
        }

        public Image getOpenedIcon( int type ) {
            return computeIcon( true, type );
        }

        private Image computeIcon( boolean opened, int type ) {
            Node folderNode = getOriginal();
            Image image = opened ? folderNode.getOpenedIcon( type ) : folderNode.getIcon( type );
            return ImageUtilities.mergeImages( image, CONFIGURATION_FILES_BADGE, 7, 7 );
        }
        
        public String getName() {
            return CONFIG_FILES_VIEW_NAME;
        }

        public String getDisplayName() {
            return NbBundle.getMessage(J2eeProjectView.class, "LBL_Node_ConfigFiles"); //NOI18N
        }

        public boolean canCopy() {
            return false;
        }

        public boolean canCut() {
            return false;
        }

        public boolean canRename() {
            return false;
        }

        public boolean canDestroy() {
            return false;
        }

        public Action[] getActions( boolean context ) {
            return new Action[] {
                SystemAction.get(FindAction.class),
            };
        }
    }
    
    private static final class VisibilityQueryDataFilter implements ChangeListener, ChangeableDataFilter {
        
        EventListenerList ell = new EventListenerList();        
        
        public VisibilityQueryDataFilter() {
            VisibilityQuery.getDefault().addChangeListener( this );
        }
                
        public boolean acceptDataObject(DataObject obj) {                
            FileObject fo = obj.getPrimaryFile();                
            return VisibilityQuery.getDefault().isVisible( fo );
        }
        
        public void stateChanged( ChangeEvent e) {            
            Object[] listeners = ell.getListenerList();     
            ChangeEvent event = null;
            for (int i = listeners.length-2; i>=0; i-=2) {
                if (listeners[i] == ChangeListener.class) {             
                    if ( event == null) {
                        event = new ChangeEvent( this );
                    }
                    ((ChangeListener)listeners[i+1]).stateChanged( event );
                }
            }
        }        
    
        public void addChangeListener( ChangeListener listener ) {
            ell.add( ChangeListener.class, listener );
        }        
                        
        public void removeChangeListener( ChangeListener listener ) {
            ell.remove( ChangeListener.class, listener );
        }
        
    }

    
}
