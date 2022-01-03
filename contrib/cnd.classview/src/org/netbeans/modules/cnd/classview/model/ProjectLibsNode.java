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

package org.netbeans.modules.cnd.classview.model;

import java.awt.Image;
import java.io.IOException;
import java.util.Collection;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.classview.ChildrenUpdater;
import org.netbeans.modules.cnd.classview.ProjectsKeyArray;
import org.netbeans.modules.cnd.classview.resources.I18n;
import org.openide.nodes.Children;
import org.openide.util.ImageUtilities;


public final class ProjectLibsNode extends BaseNode {

    private final ChildrenUpdater updater;

    public ProjectLibsNode(CsmProject project, ChildrenUpdater updater) {
        super(createChildren(project, updater));
        this.updater = updater;
        setName("dummy"); // NOI18N
        setDisplayName(I18n.getMessage("Libs")); // NOI18N
    }

    private static Children createChildren(CsmProject project, ChildrenUpdater updater) {
//        Collection<CsmProject> libs = project.getLibraries();
//        if (libs.isEmpty()) {
//            return Children.LEAF;
//        } else {
            ProjectsKeyArray keys = new ProjectsKeyArray(project, updater);
            updater.register(keys);
            return keys;
//        }
    }

    @Override
    public void destroy() throws IOException {
        Children children = getChildren();
        if (children instanceof ProjectsKeyArray) {
            updater.unregister((ProjectsKeyArray) children);
        }
        super.destroy();
    }

    @Override
    public CsmObject getCsmObject() {
	return null;
    }

    @Override
    public Image getIcon(int param) {
        return ImageUtilities.loadImage("org/netbeans/modules/cnd/classview/resources/libraries_folder.gif"); //NOI18N
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ProjectLibsNode) {
            return true;
        }
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        return hash;
    }
}
