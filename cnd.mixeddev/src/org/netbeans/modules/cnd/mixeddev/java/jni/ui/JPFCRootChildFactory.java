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
import org.netbeans.modules.cnd.api.project.NativeProject;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.ChildFactory;
import org.openide.nodes.Children;
import org.openide.nodes.Node;

/**
 *
 */
public class JPFCRootChildFactory extends ChildFactory<NativeProject> {
    
    private final List<NativeProject> projects;

    public JPFCRootChildFactory(List<NativeProject> projects) {
        this.projects = projects;
    }

    @Override
    protected boolean createKeys(List<NativeProject> toPopulate) {
        for (NativeProject project : projects) {
            toPopulate.add(project);
        }
        return true;
    }

    @Override
    protected Node createNodeForKey(NativeProject key) {
        return new NativeProjectNode(Children.create(new JPFCNativeProjectChildFactory(key), true), key);
    }
}
