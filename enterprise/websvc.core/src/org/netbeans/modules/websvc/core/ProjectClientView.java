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

package org.netbeans.modules.websvc.core;

import java.util.ArrayList;
import java.util.Collection;
import org.netbeans.api.project.Project;
import org.netbeans.modules.websvc.api.jaxws.client.JAXWSClientView;
import org.openide.nodes.Node;
import org.openide.util.Lookup;

/**
 *
 * @author mkuchtiak
 */
public class ProjectClientView {
    
    private static final Lookup.Result<ProjectClientViewProvider> clientViewProviders =
        Lookup.getDefault().lookup(new Lookup.Template<ProjectClientViewProvider>(ProjectClientViewProvider.class));
    
    public static Node[] createClientView(Project project) {
        ArrayList<Node> views = new ArrayList<Node>();
        Collection<? extends ProjectClientViewProvider> instances = clientViewProviders.allInstances();
        for (ProjectClientViewProvider impl: instances) {
            Node view = impl.createClientView(project);
            if (view != null) {
                // Its better to return views from all impls
                // To accomodate projects with mixed jaxrpc and jaxws clients.
                views.add(view);
            }
        }
        return views.toArray(new Node[0]);
    }
    
}
