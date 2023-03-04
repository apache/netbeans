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

package org.netbeans.modules.j2ee.ejbcore.ui.logicalview.entries;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.api.ejbjar.EjbJar;
import org.netbeans.modules.j2ee.spi.ejbjar.support.J2eeProjectView;
import org.openide.nodes.Children;
import org.openide.nodes.Node;

/**
 * Provides EJB tree of all open projects. This class is not used for displaying Enterprise Beans node in project view.
 */
public final class EJBListViewChildren extends Children.Keys<EJBListViewChildren.KEY> {

    public enum KEY { EJB }

    private final Project project;

    public EJBListViewChildren(Project project) {
        assert project != null;
        this.project = project;
    }

    @Override
    protected void addNotify() {
        super.addNotify();
        createNodes();
    }

    private void createNodes() {
        List<KEY> keys = new ArrayList<KEY>();
        keys.add(KEY.EJB);
        setKeys(keys);
    }

    @Override
    protected void removeNotify() {
        setKeys(Collections.<KEY>emptySet());
        super.removeNotify();
    }

    public Node[] createNodes(KEY key) {
        Node node = null;
        if (key == KEY.EJB) {
            EjbJar[] apiEjbJars = EjbJar.getEjbJars(project);
            if (null != apiEjbJars && apiEjbJars.length > 0)
                node = J2eeProjectView.createEjbsView(apiEjbJars[0], project);
        }
        return node == null ? new Node[0] : new Node[] {node};
    }

}


