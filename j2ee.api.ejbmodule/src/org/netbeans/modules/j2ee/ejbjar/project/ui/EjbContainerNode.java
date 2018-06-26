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
