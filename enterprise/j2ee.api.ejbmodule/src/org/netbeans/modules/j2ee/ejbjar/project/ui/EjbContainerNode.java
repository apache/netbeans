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
package org.netbeans.modules.j2ee.ejbjar.project.ui;

import java.awt.Image;
import javax.swing.Action;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.api.ejbjar.EjbJar;
import org.netbeans.modules.j2ee.spi.ejbjar.EjbNodesFactory;
import org.netbeans.spi.project.ui.support.CommonProjectActions;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.*;
import org.openide.util.HelpCtx;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;

/**
 * @author Chris Webster
 */
public class EjbContainerNode extends AbstractNode {

    public static final String NAME = "EJBS"; // NOI18N
    private static final String EJB_BADGE = "org/netbeans/modules/j2ee/ejbjar/project/ui/enterpriseBeansBadge.png"; // NOI18N

    public EjbContainerNode(EjbJar ejbModule, Project p, EjbNodesFactory nodesFactory) throws DataObjectNotFoundException {
        super(new EjbContainerChildren(ejbModule, nodesFactory, p), Lookups.fixed(new Object[]{p, DataFolder.find(p.getProjectDirectory())}));
        setName(EjbNodesFactory.CONTAINER_NODE_NAME);
        setDisplayName(NbBundle.getMessage(EjbContainerNode.class, "LBL_node"));
        setShortDescription(NbBundle.getMessage(EjbContainerNode.class, "HINT_node"));
    }

    public Action[] getActions(boolean context) {
        return new Action[]{
                    CommonProjectActions.newFileAction()
                };
    }

    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    // When you have help, change to:
    // return new HelpCtx(EjbContainerNode.class);
    }

    public Image getIcon(int type) {
        return computeIcon(false, type);
    }

    public Image getOpenedIcon(int type) {
        return computeIcon(true, type);
    }

    private Image computeIcon(boolean opened, int type) {
        Image image;
        Node iconDelegate = getIconDelegate();
        if (opened) {
            image = iconDelegate != null ? iconDelegate.getOpenedIcon(type) : super.getOpenedIcon(type);
        } else {
            image = iconDelegate != null ? iconDelegate.getIcon(type) : super.getIcon(type);
        }
        Image badge = ImageUtilities.loadImage(EJB_BADGE);
        return ImageUtilities.mergeImages(image, badge, 7, 7);
    }

    private Node getIconDelegate() {
        try {
            return DataFolder.find(FileUtil.getConfigRoot()).getNodeDelegate();
        } catch (DataObjectNotFoundException donfe) {
            return null;
        }
    }
}
