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
package org.netbeans.modules.testng.ui.actions;

import org.netbeans.api.project.Project;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.testng.ui.impl.ProjectImpl;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author lukas
 */
public abstract class TestActionT extends NbTestCase {

    protected static final Node[] EMPTY_ARRAY = new Node[0];
    protected static final Node[] EMPTY_NODES = new Node[] {new NodeImpl(), new NodeImpl()};
    protected Node[] DATAOBJECT_NODE;
    protected Node[] PROJECT_NODE;
    protected Node[] FILEOBJECT_NODE;
    protected Project p;

    static {
        //NodeActionsInfraHid.install();
    }

    public TestActionT(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        FileObject root = FileUtil.toFileObject(getWorkDir());
        p = new ProjectImpl(root, Lookup.EMPTY);
        PROJECT_NODE = new Node[] {new NodeImpl(p)};
        FILEOBJECT_NODE = new Node[] {new NodeImpl(root)};
        DATAOBJECT_NODE = new Node[] {new NodeImpl(DataObject.find(root))};
    }

    static class NodeImpl extends AbstractNode {

        NodeImpl(Object... toLookup) {
            super(Children.LEAF, toLookup.length < 1 ? Lookup.EMPTY : Lookups.fixed(toLookup));
        }
    }
}
