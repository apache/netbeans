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

/*
 * Copyright 2021 HCL America, Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *    http://www.apache.org/licenses/LICENSE-2.0
 *    
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package org.netbeans.modules.versionvault.ui.selectors;

import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.Lookups;

import javax.swing.*;
import java.util.Collections;
import java.util.List;
import java.awt.*;
import java.beans.BeanInfo;

/**
 * Represents a path in repository, its children
 * subfolders.
 *
 * <p>Lookup contains a string identifing rpresented path.
 *
 * @author Petr Kuzel
 */
public class RepositoryPathNode extends AbstractNode {

    public static RepositoryPathNode create(String root, String path) {

        RepositoryPathChildren kids = new RepositoryPathChildren(root, path);
        Lookup lookup = Lookups.singleton(path);
        RepositoryPathNode node = new RepositoryPathNode(kids, lookup);
        
        String name = root;
        if (path.equals("") == false) { // NOI18N
            String[] atoms = path.split("/");    // NOI18N
            if (atoms.length > 0) {
                name = atoms[atoms.length -1];
            }
        }
        node.setDisplayName(name);
        return node;
    }

    private RepositoryPathNode(Children children, Lookup lookup) {
        super(children, lookup);
        setIconBaseWithExtension("org/netbeans/modules/versionvault/ui/selectors/defaultFolder.gif"); // NOI18N
    }

    public Image getIcon(int type) {
        Image img = null;
        if (type == BeanInfo.ICON_COLOR_16x16) {
            img = (Image)UIManager.get("Nb.Explorer.Folder.icon");  // NOI18N
        }
        if (img == null) {
            img = super.getIcon(type);
        }
        return img;
    }

    public Image getOpenedIcon(int type) {
        Image img = null;
        if (type == BeanInfo.ICON_COLOR_16x16) {
            img = (Image)UIManager.get("Nb.Explorer.Folder.openedIcon");  // NOI18N
        }
        if (img == null) {
            img = super.getIcon(type);
        }
        return img;
    }

    static class RepositoryPathChildren extends Children.Keys implements Runnable {

        private final String root;
        private String path;
        private RequestProcessor.Task task;

        public RepositoryPathChildren(String root, String path) {
            this.root = root;
            this.path = path;
        }

        protected void addNotify() {
            super.addNotify();
            AbstractNode waitNode = new WaitNode(org.openide.util.NbBundle.getMessage(RepositoryPathNode.class, "BK2024"));
            setKeys(Collections.singleton(waitNode));
            RequestProcessor rp = RequestProcessor.getDefault();
            task = rp.post(this);
        }

        protected void removeNotify() {
            task.cancel();
            setKeys(Collections.EMPTY_SET);
            super.removeNotify();
        }

        protected Node[] createNodes(Object key) {
            if (key instanceof Node) {
                return new Node[] {(Node)key};
            }
            path="";
            String relPath = path.equals("") ? (String) key : path + "/" + key; // NOI18N
            Node pathNode = RepositoryPathNode.create(root, relPath);
            return new Node[] {pathNode};
        }

        public void run() {
            try {
                List keys = ModuleSelector.listRepositoryPath(root, path);
                setKeys(keys);
            } catch (Exception e) {
                e.printStackTrace();
                setKeys(Collections.singleton(errorNode(e)));
            } 
        }

        private Node errorNode(Exception ex) {
            AbstractNode errorNode = new AbstractNode(Children.LEAF);
            errorNode.setDisplayName(org.openide.util.NbBundle.getMessage(RepositoryPathNode.class, "BK2025"));
            errorNode.setShortDescription(ex.getLocalizedMessage());
            return errorNode;
        }
    }

}
