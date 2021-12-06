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

import org.openide.nodes.*;
import org.openide.util.UserCancelException;
import org.openide.util.HelpCtx;

import org.netbeans.modules.versionvault.ui.wizard.RepositoryStep;

import java.util.*;
import java.util.List;
import java.io.File;
import java.io.IOException;

/**
 * Prototype impl of defined modules listing.
 *
 * @author Petr Kuzel
 */
public final class ModuleSelector {

    private static RepositoryStep repositoryStep;
    /**
     * Asks user to select which module to checkout. Popups a modal UI,
     * @param root identifies repository
     * @return Set of String, possibly empty
     */
    public Set selectModules(String root) {

        // create top level node that categorizes to aliases and raw browser

        Children.Array kids = new Children.Array();
        Node pathsNode = RepositoryPathNode.create(root, "");  // NOI18N
        kids.add(new Node[] {pathsNode});
        Node rootNode = new AbstractNode(kids);

        try {
            NodeOperation2 op = new NodeOperation2();
            op.setRootVisible(false);
            op.setHelpCtx(new HelpCtx(ModuleSelector.class));
            Node[] selected = op.select(org.openide.util.NbBundle.getMessage(ModuleSelector.class, "BK2019"), 
                                        org.openide.util.NbBundle.getMessage(ModuleSelector.class, "BK2020"), 
                                        org.openide.util.NbBundle.getMessage(ModuleSelector.class, "BK2020"), 
                                        rootNode, 
                                        org.openide.util.NbBundle.getMessage(ModuleSelector.class, "BK2020"), 
                                        org.openide.util.NbBundle.getMessage(ModuleSelector.class, "BK2020"), 
                                        new NodeAcceptor() {
                public boolean acceptNodes(Node[] nodes) {
                    boolean ret = nodes.length > 0;
                    for (int i = 0; i < nodes.length; i++) {
                        Node node = nodes[i];
                        String path = (String) node.getLookup().lookup(String.class);
                        ret &= path != null;
                    }
                    return ret;
                }
            });

            Set  modules = new LinkedHashSet();
            for (int i = 0; i < selected.length; i++) {
                Node node = selected[i];
                String path = (String) node.getLookup().lookup(String.class);
                modules.add(path);
            }
            return modules;
        } catch (UserCancelException e) {
            return Collections.EMPTY_SET;
        }
    }

    /*
     * Pupup modal UI and let user select repositpry path.
     *
     * @param root identifies repository
     * @param proxy defines which proxy to use or null
     *        to use one from ClearcaseRootSettings.
     * @return '/' separated path or null on cancel.
     */
    public String selectRepositoryPath(String root) {

        
        Node pathsNode = RepositoryPathNode.create(root, "");  // NOI18N

        try {
            Node[] selected = NodeOperation.getDefault().select(org.openide.util.NbBundle.getMessage(ModuleSelector.class, "BK2021"), org.openide.util.NbBundle.getMessage(ModuleSelector.class, "BK2022"), pathsNode, new NodeAcceptor() {
                public boolean acceptNodes(Node[] nodes) {
                    if (nodes.length == 1) {
                        String path = (String) nodes[0].getLookup().lookup(String.class);
                        return path != null;
                    }
                    return false;
                }
            });

            String path = null;
            if (selected.length == 1) {
                path = (String) selected[0].getLookup().lookup(String.class);
            }
            return path;
        } catch (UserCancelException e) {
            return null;
        }
    }
    
    /**
     * Lists subfolders in given repository folder.
     *
     * @param client engine to be used
     * @param root identifies repository
     * @return folders never <code>null</code>
     */
    public static List listRepositoryPath(String root, String path) throws Exception {

        final List list = new ArrayList();
        
        if (!path.equals(""))
            root = path;
        try {
            File pathName = new File(root);
            String[] fileNames = pathName.list();
            
            for (int i = 0; i < fileNames.length; i++) {
                File tf = new File(pathName.getPath(), fileNames[i]);
                if (tf.isDirectory() && !tf.getName().equals("lost+found")) {
                    list.add(tf.getCanonicalPath());
                }
            } 
            path = null;
        } catch (IOException e) {
        }
        
        return list;
    }
}
