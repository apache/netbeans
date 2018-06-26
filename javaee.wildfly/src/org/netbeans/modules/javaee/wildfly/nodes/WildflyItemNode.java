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
package org.netbeans.modules.javaee.wildfly.nodes;

import java.awt.Image;
import javax.enterprise.deploy.shared.ModuleType;
import org.netbeans.modules.j2ee.deployment.plugins.api.UISupport;
import org.netbeans.modules.j2ee.deployment.plugins.api.UISupport.ServerIcon;
import org.netbeans.modules.javaee.wildfly.nodes.actions.RefreshModulesAction;
import org.netbeans.modules.javaee.wildfly.nodes.actions.RefreshModulesCookie;
import org.netbeans.modules.javaee.wildfly.nodes.actions.Refreshable;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.actions.SystemAction;

/**
 * Default Node which can have refresh action enabled and which has default
 * icon.
 *
 * @author Michal Mocnak
 * @author Emmanuel Hugonnet (ehsavoie) <ehsavoie@netbeans.org>
 */
public class WildflyItemNode extends AbstractNode {

    private ModuleType moduleType;

    public WildflyItemNode(Children children, String name) {
        super(children);
        setDisplayName(name);
        if (getChildren() instanceof Refreshable) {
            getCookieSet().add(new RefreshModulesCookieImpl((Refreshable) getChildren()));
        }
    }

    public WildflyItemNode(Children children, String name, ModuleType moduleType) {
        this(children, name);
        this.moduleType = moduleType;
    }

    @Override
    public Image getIcon(int type) {
        if (ModuleType.WAR.equals(moduleType)) {
            return UISupport.getIcon(ServerIcon.WAR_FOLDER);
        } else if (ModuleType.EAR.equals(moduleType)) {
            return UISupport.getIcon(ServerIcon.EAR_FOLDER);
        } else if (ModuleType.EJB.equals(moduleType)) {
            return UISupport.getIcon(ServerIcon.EJB_FOLDER);
        } else {
            return getIconDelegate().getIcon(type);
        }
    }

    @Override
    public Image getOpenedIcon(int type) {
        if (ModuleType.WAR.equals(moduleType)) {
            return UISupport.getIcon(ServerIcon.WAR_OPENED_FOLDER);
        } else if (ModuleType.EAR.equals(moduleType)) {
            return UISupport.getIcon(ServerIcon.EAR_OPENED_FOLDER);
        } else if (ModuleType.EJB.equals(moduleType)) {
            return UISupport.getIcon(ServerIcon.EJB_OPENED_FOLDER);
        } else {
            return getIconDelegate().getOpenedIcon(type);
        }
    }

    private Node getIconDelegate() {
        return DataFolder.findFolder(FileUtil.getConfigRoot()).getNodeDelegate();
    }

    @Override
    public javax.swing.Action[] getActions(boolean context) {
        if (getChildren() instanceof Refreshable) {
            return new SystemAction[]{
                SystemAction.get(RefreshModulesAction.class)
            };
        }

        return new SystemAction[]{};
    }

    /**
     * Implementation of the RefreshModulesCookie
     */
    private static class RefreshModulesCookieImpl implements RefreshModulesCookie {

        Refreshable children;

        public RefreshModulesCookieImpl(Refreshable children) {
            this.children = children;
        }

        @Override
        public void refresh() {
            children.updateKeys();
        }
    }
}
