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

package org.netbeans.modules.nbform.actions;

import java.util.Arrays;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.nbform.palette.BeanInstaller;
import org.netbeans.spi.project.ui.RecommendedTemplates;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.actions.NodeAction;

/**
 * InstallToPalette action - installs selected classes as beans to palette.
 *
 * @author   Ian Formanek
 */

public class InstallToPaletteAction extends NodeAction {

    private static String name;

    public InstallToPaletteAction () {
        putValue("noIconInMenu", Boolean.TRUE); // NOI18N
    }
    
    @Override
    public String getName() {
        if (name == null)
            name = org.openide.util.NbBundle.getBundle(InstallToPaletteAction.class)
                     .getString("ACT_InstallToPalette"); // NOI18N
        return name;
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx("beans.adding"); // NOI18N
    }

    @Override
    protected boolean asynchronous() {
        return false;
    }

    @Override
    protected void performAction(Node[] activatedNodes) {
        BeanInstaller.installBeans(activatedNodes);
    }

    @Override
    protected boolean enable(Node[] activatedNodes) {
        if (activatedNodes.length == 0) {
            return false;
        }
        for (Node n: activatedNodes) {
            FileObject fobj = n.getLookup().lookup(FileObject.class);
            if (fobj == null || JavaSource.forFileObject(fobj) == null) {
                return false;
            }
            // Issue 73641
            Project project = FileOwnerQuery.getOwner(fobj);
            if (project != null) {
                RecommendedTemplates info = project.getLookup().lookup(RecommendedTemplates.class);
                if ((info != null) && !Arrays.asList(info.getRecommendedTypes()).contains("java-forms")) { // NOI18N
                    return false;
                }
            }
        }
        
        return true;
    }

}
