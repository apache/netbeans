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

package org.netbeans.modules.javaee.wildfly.nodes;

import java.awt.Image;
import org.netbeans.modules.javaee.wildfly.nodes.actions.RefreshModulesAction;
import org.netbeans.modules.javaee.wildfly.nodes.actions.RefreshModulesCookie;
import org.netbeans.modules.javaee.wildfly.nodes.actions.Refreshable;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;

/**
 *
 * @author Emmanuel Hugonnet (ehsavoie) <ehsavoie@netbeans.org>
 */
public class WildflyResourcesItemNode extends AbstractNode {

    private final String icon;

    public WildflyResourcesItemNode(Children children, String name, String icon) {
        super(children);
        setDisplayName(name);
        this.icon = icon;
        if (getChildren() instanceof Refreshable) {
            getCookieSet().add(new RefreshModulesCookieImpl((Refreshable) getChildren()));
        }
    }

    @Override
    public Image getIcon(int type) {
        return ImageUtilities.loadImage(icon);
    }

    @Override
    public Image getOpenedIcon(int type) {
        return ImageUtilities.loadImage(icon);
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
