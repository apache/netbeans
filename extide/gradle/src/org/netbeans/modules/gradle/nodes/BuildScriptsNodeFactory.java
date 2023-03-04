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

package org.netbeans.modules.gradle.nodes;

import org.netbeans.modules.gradle.NbGradleProjectImpl;
import org.netbeans.modules.gradle.api.NbGradleProject;
import org.netbeans.api.project.Project;
import org.netbeans.spi.project.ui.support.NodeFactory;
import org.netbeans.spi.project.ui.support.NodeFactorySupport;
import org.netbeans.spi.project.ui.support.NodeList;

/**
 *
 * @author Laszlo Kishalmi
 */
@NodeFactory.Registration(projectType=NbGradleProject.GRADLE_PROJECT_TYPE, position=200)
public class BuildScriptsNodeFactory implements NodeFactory {

    @Override
    public NodeList<?> createNodes(Project prj) {
        NbGradleProjectImpl project = prj.getLookup().lookup(NbGradleProjectImpl.class);
        return NodeFactorySupport.fixedNodeList(new BuildScriptsNode(project));
    }
    
}
