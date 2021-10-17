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
package org.netbeans.modules.websvc.core;

import java.awt.Image;
import java.beans.BeanInfo;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.spi.project.ui.PrivilegedTemplates;
import org.netbeans.spi.project.ui.support.CommonProjectActions;
import org.netbeans.spi.project.ui.support.NodeFactory;
import org.netbeans.spi.project.ui.support.NodeList;
import org.openide.actions.FindAction;
import org.openide.actions.PasteAction;
import org.openide.actions.PropertiesAction;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.ChildFactory;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.ChangeSupport;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.WeakListeners;
import org.openide.util.actions.SystemAction;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Ajit Bhate
 */
@NodeFactory.Registration(projectType="org-netbeans-modules-java-j2seproject",position=260)
public class ProjectWebServiceNodeFactory implements NodeFactory {

    @NodeFactory.Registration(projectType="org-netbeans-modules-web-project",position=400)
    public static ProjectWebServiceNodeFactory ejbproject() {
        return new ProjectWebServiceNodeFactory();
    }

    @NodeFactory.Registration(projectType="org-netbeans-modules-j2ee-ejbjarproject",position=155)
    public static ProjectWebServiceNodeFactory j2seproject() {
        return new ProjectWebServiceNodeFactory();
    }

    @NodeFactory.Registration(projectType="org-netbeans-modules-j2ee-clientproject",position=180)
    public static ProjectWebServiceNodeFactory j2eeClientProject() {
        return new ProjectWebServiceNodeFactory();
    }

    /** Creates a new instance of ProjectWebServiceNodeFactory */
    public ProjectWebServiceNodeFactory() {
    }

    @Override
    public NodeList<ProjectWebServiceView.ViewType> createNodes(Project p) {
        assert p != null;
        return new WsNodeList(p);
    }

    private static class WsNodeList implements NodeList<ProjectWebServiceView.ViewType>, ChangeListener {

        private Project project;
        private ChangeSupport changeSupport;
        private ProjectWebServiceView view;
        private WSRootNode serviceNode,  clientNode;
        private ChangeListener weakL;

        public WsNodeList(Project proj) {
            project = proj;
            changeSupport = new ChangeSupport(this);
        }

        @Override
        public List<ProjectWebServiceView.ViewType> keys() {
            initView();
            List<ProjectWebServiceView.ViewType> result = new ArrayList<ProjectWebServiceView.ViewType>();
                if (!view.isViewEmpty(ProjectWebServiceView.ViewType.SERVICE)) {
                    result.add(ProjectWebServiceView.ViewType.SERVICE);
                }
                if (!view.isViewEmpty(ProjectWebServiceView.ViewType.CLIENT)) {
                    result.add(ProjectWebServiceView.ViewType.CLIENT);
                }
            return result;
        }

        public synchronized void addChangeListener(ChangeListener l) {
            changeSupport.addChangeListener(l);
        }

        public synchronized void removeChangeListener(ChangeListener l) {
            changeSupport.removeChangeListener(l);
        }

        private void fireChange() {
            changeSupport.fireChange();
        }

        public Node node(ProjectWebServiceView.ViewType key) {
            switch (key) {
                case SERVICE:
                    if (serviceNode == null) {
                        serviceNode = new WSRootNode(new WSChildrenFactory(view, key), 
                                createLookup(project, new WsPrivilegedTemplates()));
                        serviceNode.setDisplayName(NbBundle.getBundle(
                                ProjectWebServiceNodeFactory.class).getString(
                                        "LBL_WebServices"));        // NOI18N
                    }
                    return serviceNode;
                case CLIENT:
                    if (clientNode == null) {
                        clientNode = new WSRootNode(new WSChildrenFactory(view, key), 
                                createLookup(project, new WsClientPrivilegedTemplates()));
                        clientNode.setDisplayName(NbBundle.getBundle(
                                ProjectWebServiceNodeFactory.class).getString(
                                        "LBL_ServiceReferences"));  // NOI18N
                    }
                    return clientNode;
            }
            return null;
        }

        public void addNotify() {
            initView();
            weakL = WeakListeners.change(this, view);
            view.addChangeListener(weakL, ProjectWebServiceView.ViewType.SERVICE);
            view.addChangeListener(weakL, ProjectWebServiceView.ViewType.CLIENT);
            view.addNotify();
        }

        public void removeNotify() {
            if (view != null) {
                view.removeChangeListener(weakL, ProjectWebServiceView.ViewType.SERVICE);
                view.removeChangeListener(weakL, ProjectWebServiceView.ViewType.CLIENT);
                view.removeNotify();
                weakL = null;
            }
        }

        public void stateChanged(final ChangeEvent e) {
            SwingUtilities.invokeLater(new Runnable() {

                public void run() {
                    fireChange();
                    Object source = e.getSource();
                    if(source instanceof ProjectWebServiceViewImpl) {
                        //ProjectWebServiceViewImpl view = (ProjectWebServiceViewImpl) source;
                        if (serviceNode != null) {
                            serviceNode.getFactory().updateKeys();
                        }
                        if (clientNode != null) {
                            clientNode.getFactory().updateKeys();
                        }
                    }
                }
            });
        }

        private void initView() {
            if(view==null){
                view = ProjectWebServiceView.getProjectWebServiceView(project);
            }
        }

        private Lookup createLookup(Project project, PrivilegedTemplates privilegedTemplates) {
            return Lookups.fixed(new Object[]{project, privilegedTemplates});
        }

    }
    
    private static class WSChildrenFactory extends ChildFactory<Pair> {

        private ProjectWebServiceView.ViewType viewType;
        private ProjectWebServiceView view;

        public WSChildrenFactory(ProjectWebServiceView view,
                ProjectWebServiceView.ViewType viewType) 
        {
            super();
            this.view = view;
            this.viewType = viewType;
        }
        
        /* (non-Javadoc)
         * @see org.openide.nodes.ChildFactory#createKeys(java.util.List)
         */
        @Override
        protected boolean createKeys( List<Pair> keys )
        {
            if (view != null && !view.isViewEmpty(viewType) ) {
                List<ProjectWebServiceViewImpl> webServiceViews = 
                    view.getWebServiceViews();
                if ( Thread.interrupted() ){
                    return false;
                }
                for (ProjectWebServiceViewImpl projectWebServiceViewImpl : webServiceViews)
                {
                    if ( Thread.interrupted() ){
                        return false;
                    }
                    Node[] nodes = projectWebServiceViewImpl.createView(viewType);
                    keys.add( new Pair( projectWebServiceViewImpl , nodes ));
                }
            } 
            return true;
        }
        
        /* (non-Javadoc)
         * @see org.openide.nodes.ChildFactory#createNodesForKey(java.lang.Object)
         */
        @Override
        protected Node[] createNodesForKey( Pair key ) {
            return key.getNodes();
        }
        
        void updateKeys(){
            refresh(true);
        }

    }
    
    private static class Pair {
        
        Pair(ProjectWebServiceViewImpl impl, Node[] nodes){
            this.impl = impl;
            this.nodes = nodes;
        }
        
        ProjectWebServiceViewImpl getView(){
            return impl;
        }
        
        Node[] getNodes(){
            return nodes;
        }
        
        private final ProjectWebServiceViewImpl impl;
        private final Node[] nodes; 
    }

    private static class WSRootNode extends AbstractNode {

        private static final String SERVICES_BADGE = 
            "org/netbeans/modules/websvc/core/webservices/ui/resources/webservicegroup.png"; // NOI18N

        private Icon folderIconCache;
        private Icon openedFolderIconCache;
        private Image cachedServicesBadge;
        private WSChildrenFactory factory;

        public WSRootNode(WSChildrenFactory factory, Lookup lookup) {
            super(Children.create(factory, true), lookup);
            this.factory = factory;
        }

        @Override
        public Action[] getActions(boolean context) {
            return new Action[]{
                        CommonProjectActions.newFileAction(),
                        null,
                        SystemAction.get(FindAction.class),
                        null,
                        SystemAction.get(PasteAction.class),
                        null,
                        SystemAction.get(PropertiesAction.class)
                    };
        }

        @Override
        public Image getIcon(int type) {
            return computeIcon(false);
        }

        @Override
        public Image getOpenedIcon(int type) {
            return computeIcon(true);
        }
        
        WSChildrenFactory getFactory(){
            return factory;
        }

        private java.awt.Image getServicesImage() {
            if (cachedServicesBadge == null) {
                cachedServicesBadge = ImageUtilities.loadImage(SERVICES_BADGE);
            }
            return cachedServicesBadge;
        }

        /**
         * Returns Icon of folder on active platform
         * @param opened should the icon represent opened folder
         * @return the folder icon
         */
        private Icon getFolderIcon(boolean opened) {
            if (openedFolderIconCache == null) {
                Node n = DataFolder.findFolder(FileUtil.getConfigRoot()).getNodeDelegate();
                openedFolderIconCache = new ImageIcon(n.getOpenedIcon(BeanInfo.ICON_COLOR_16x16));
                folderIconCache = new ImageIcon(n.getIcon(BeanInfo.ICON_COLOR_16x16));
            }
            if (opened) {
                return openedFolderIconCache;
            } else {
                return folderIconCache;
            }
        }

        private Image computeIcon(boolean opened) {
            Icon icon = getFolderIcon(opened);
            Image image = ((ImageIcon) icon).getImage();
            image = ImageUtilities.mergeImages(image, getServicesImage(), 7, 7);
            return image;
        }
    }
    
    private static class WsPrivilegedTemplates implements PrivilegedTemplates {

        public String[] getPrivilegedTemplates() {
            return new String[] {
                "Templates/WebServices/WebService.java",    // NOI18N
                "Templates/WebServices/WebServiceFromWSDL.java",    // NOI18N
                "Templates/WebServices/MessageHandler.java", // NOI18N
                "Templates/WebServices/LogicalHandler.java" // NOI18N
            };
        }
    }
    private static class WsClientPrivilegedTemplates implements PrivilegedTemplates {

        public String[] getPrivilegedTemplates() {
            return new String[] {
                "Templates/WebServices/WebServiceClient", // NOI18N
                "Templates/WebServices/MessageHandler.java", // NOI18N
                "Templates/WebServices/LogicalHandler.java" // NOI18N
            };
        }
    }
}
