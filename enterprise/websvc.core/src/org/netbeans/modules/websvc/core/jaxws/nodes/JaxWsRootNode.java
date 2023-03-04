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

package org.netbeans.modules.websvc.core.jaxws.nodes;

import java.awt.Image;
import java.beans.BeanInfo;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.common.ProjectUtil;
import org.netbeans.modules.websvc.api.jaxws.project.config.JaxWsModel;
import org.netbeans.modules.websvc.core.WSStackUtils;
import org.netbeans.modules.websvc.jaxws.api.JAXWSSupport;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.ui.support.CommonProjectActions;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.actions.FindAction;
import org.openide.actions.PasteAction;
import org.openide.actions.PropertiesAction;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.WeakListeners;
import org.openide.util.actions.SystemAction;
import org.openide.util.lookup.Lookups;

public class JaxWsRootNode extends AbstractNode implements PropertyChangeListener{
    private PropertyEvaluator evaluator;
    private Project project;
    private boolean jsr109Supported;
    private static final String SERVICES_BADGE = "org/netbeans/modules/websvc/core/webservices/ui/resources/webservicegroup.png"; // NOI18N
    private Icon folderIconCache;
    private Icon openedFolderIconCache;
    private java.awt.Image cachedServicesBadge;
    
    public JaxWsRootNode(Project project, JaxWsModel jaxWsModel, FileObject[] srcRoots) {
        super(new JaxWsRootChildren(jaxWsModel,srcRoots), Lookups.fixed(project));
        setDisplayName(NbBundle.getBundle(JaxWsRootNode.class).getString("LBL_WebServices"));
        this.project=project;
        if(!ProjectUtil.isJavaEE5orHigher(project)){
            listenToServerChanges();
            WSStackUtils stackUtils = new WSStackUtils(project);
            jsr109Supported = stackUtils.isJsr109Supported();
        }
    }
    
    @Override
    public Image getIcon( int type ) {
        return computeIcon( false );
    }
    
    @Override
    public Image getOpenedIcon( int type ) {
        return computeIcon( true );
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
    private Icon getFolderIcon (boolean opened) {
        if (openedFolderIconCache == null) {
            Node n = DataFolder.findFolder(FileUtil.getConfigRoot()).getNodeDelegate();
            openedFolderIconCache = new ImageIcon(n.getOpenedIcon(BeanInfo.ICON_COLOR_16x16));
            folderIconCache = new ImageIcon(n.getIcon(BeanInfo.ICON_COLOR_16x16));
        }
        if (opened) {
            return openedFolderIconCache;
        }
        else {
            return folderIconCache;
        }
    }

    private Image computeIcon( boolean opened) {        
        Icon icon = getFolderIcon(opened);
        Image image = ((ImageIcon)icon).getImage();
        image = ImageUtilities.mergeImages(image, getServicesImage(), 7, 7 );
        return image;        
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
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }
    
    private void listenToServerChanges(){
        JAXWSSupport wss = JAXWSSupport.getJAXWSSupport(project.getProjectDirectory());
        if (wss != null) {
            evaluator = wss.getAntProjectHelper().getStandardPropertyEvaluator();
            PropertyChangeListener pcl = WeakListeners.propertyChange(this, evaluator);
            evaluator.addPropertyChangeListener(pcl);
        }
    }
    
    public void propertyChange(PropertyChangeEvent evt){
        JAXWSSupport wss = JAXWSSupport.getJAXWSSupport(project.getProjectDirectory());
        if (wss!=null && wss.getServices().size()>0) {
            if ("j2ee.server.instance".equals(evt.getPropertyName())){
                WSStackUtils stackUtils = new WSStackUtils(project);
                boolean newJsr109Supported = stackUtils.isJsr109Supported();
                if(jsr109Supported != newJsr109Supported) {
                    JaxWsModel jaxWsModel = (JaxWsModel)project.getLookup().lookup(JaxWsModel.class);
                    boolean isJsr109Project = jaxWsModel.getJsr109();
                    if(isJsr109Project != newJsr109Supported){
                        String msg = NbBundle.getMessage(JaxWsRootNode.class, "MSG_IncompatibleWSServer"); //NOI18N
                        DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(msg, NotifyDescriptor.INFORMATION_MESSAGE));
                    }
                    jsr109Supported = newJsr109Supported;
                }
            }
        }
    }
    
    // the following templates should be available on this node
    /*
    private static class WSRecommendedTemplates implements RecommendedTemplates, PrivilegedTemplates  {
        private static final String[] TYPES = new String[] { 
        "web-services",         // NOI18N
        };
        private static final String[] WS_TEMPLATES = new String[] {           
            "Templates/WebServices/WebService.java",    // NOI18N
            "Templates/WebServices/WebServiceFromWSDL.java",    // NOI18N
        };
        
        public String[] getRecommendedTypes() {
            return TYPES;
        }
        
        public String[] getPrivilegedTemplates() {
            return WS_TEMPLATES;
        }
    }
    */
}
