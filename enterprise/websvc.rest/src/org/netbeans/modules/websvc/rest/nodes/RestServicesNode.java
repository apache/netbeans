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
package org.netbeans.modules.websvc.rest.nodes;

import java.awt.Image;
import java.beans.BeanInfo;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import org.netbeans.api.project.Project;
import org.netbeans.modules.websvc.rest.spi.RestSupport;
import org.netbeans.spi.project.ui.PrivilegedTemplates;
import org.netbeans.spi.project.ui.support.CommonProjectActions;
import org.openide.actions.FindAction;
import org.openide.actions.PasteAction;
import org.openide.actions.PropertiesAction;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;
import org.openide.util.lookup.Lookups;

public class RestServicesNode extends AbstractNode { //implements PropertyChangeListener{


    private static final Image REST_SERVICES_BADGE = ImageUtilities.loadImage("org/netbeans/modules/websvc/rest/nodes/resources/restservices.png", true); // NOI18N

    static Icon folderIconCache;
    static Icon openedFolderIconCache;

    public RestServicesNode(Project project, RestSupport restSupport) {
        //super(new RestServicesChildren(project), createLookup(project));
        super(Children.create( new RestServiceChildFactory(project, restSupport), true),
                createLookup(project));
        setDisplayName(NbBundle.getBundle(RestServicesNode.class).getString("LBL_RestServices"));
    }

    public Image getIcon(int type) {
        return computeIcon(false, type);
    }

    public Image getOpenedIcon(int type) {
        return computeIcon(true, type);
    }

    /**
     * Returns Icon of folder on active platform
     * @param opened should the icon represent opened folder
     * @return the folder icon
     */
    static synchronized Icon getFolderIcon(boolean opened) {
        if (openedFolderIconCache == null) {
            Node n = DataFolder.findFolder(FileUtil.getConfigRoot()).getNodeDelegate();
            openedFolderIconCache = ImageUtilities.image2Icon(n.getOpenedIcon(BeanInfo.ICON_COLOR_16x16));
            folderIconCache = ImageUtilities.image2Icon(n.getIcon(BeanInfo.ICON_COLOR_16x16));
        }
        if (opened) {
            return openedFolderIconCache;
        } else {
            return folderIconCache;
        }
    }

    private Image computeIcon(boolean opened, int type) {
        Icon icon = getFolderIcon(opened);
        Image image = ((ImageIcon) icon).getImage();
        image = ImageUtilities.mergeImages(image, REST_SERVICES_BADGE, 7, 7);
        return image;
    }

    @Override
    public Action[] getActions(boolean context) {
        return new Action[] {
                    CommonProjectActions.newFileAction(),
                    SystemAction.get(TestRestServicesAction.class),
                    null,
                    SystemAction.get(FindAction.class),
                    null,
                    SystemAction.get(PasteAction.class),
                    null,
                    SystemAction.get(PropertiesAction.class)
                };
    }

    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    private static Lookup createLookup(Project project) {
        return Lookups.fixed(new Object[]{project,
                    new PrivilegedTemplates() {

                        public String[] getPrivilegedTemplates() {
                            return new String[]{
                                        "Templates/WebServices/RestServicesFromEntities", // NOI18N
                                        "Templates/WebServices/RestServicesFromPatterns", // NOI18N
                                        "Templates/WebServices/RestServicesFromDatabase" // NOI18N

                                    };
                        }
                    }
                });
    }
}
