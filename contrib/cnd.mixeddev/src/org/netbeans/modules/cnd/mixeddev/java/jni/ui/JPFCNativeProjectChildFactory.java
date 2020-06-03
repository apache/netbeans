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
package org.netbeans.modules.cnd.mixeddev.java.jni.ui;

import java.util.List;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.cnd.api.project.NativeProject;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.ChildFactory;
import org.openide.nodes.Children;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;

/**
 *
 */
public class JPFCNativeProjectChildFactory extends ChildFactory<FileObject> {
    
    private final NativeProject node;

    public JPFCNativeProjectChildFactory(NativeProject node) {
        this.node = node;
    }

    @Override
    protected boolean createKeys(List<FileObject> toPopulate) {
        Project proj = node.getProject().getLookup().lookup(Project.class);
        SourceGroup[] sourceGroups = ProjectUtils.getSources(proj).getSourceGroups(Sources.TYPE_GENERIC);
        for (SourceGroup srcGrp : sourceGroups) {
            toPopulate.add(srcGrp.getRootFolder());
        }
        return true;
    }

    @Override
    protected Node createNodeForKey(FileObject key) {
        try {
            DataObject dObj = DataObject.find(key);
            return new FilterNode(dObj.getNodeDelegate(), Children.create(new JPFCFileObjectChildFactory(key), false));
            //return new FileObjectNode(Children.create(new JPFCFileObjectChildFactory(key), false), key);
        } catch (DataObjectNotFoundException ex) {
            return new AbstractNode(Children.LEAF);
        }
    }
}
