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

package org.apache.tools.ant.module.nodes;

import java.util.Arrays;
import java.util.List;
import org.apache.tools.ant.module.xml.AntProjectSupport;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.Children;
import org.openide.nodes.Node;

/**
 * Test children of an Ant project.
 * @author Jesse Glick
 */
public class AntProjectChildrenTest extends NbTestCase {

    public AntProjectChildrenTest(String name) {
        super(name);
    }
    
    private FileObject testdir;
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        testdir = FileUtil.toFileObject(this.getDataDir());
        assertNotNull("testdir unit/data exists", testdir);
    }
    
//lazy children calculation makes this fail. more or less same code is tested by TargetListerTest
    public void testBasicChildren() throws Exception {
//        FileObject simple = testdir.getFileObject("targetlister/simple.xml");
//        assertNotNull("simple.xml found", simple);
//        assertEquals("correct children of simple.xml",
//            Arrays.asList(new String[] {"described", "-internal", "-internal-described", "main", "undescribed"}),
//            displayNamesForChildrenOf(simple));
    }
//    
    public void testImportedChildren() throws Exception {
//        // #44491 caused this to fail.
//        FileObject importing = testdir.getFileObject("targetlister/importing.xml");
//        assertNotNull("importing.xml found", importing);
//        assertEquals("correct children of importing.xml",
//            Arrays.asList(new String[] {"main", "subtarget1", "subtarget2", "subtarget3", "whatever"}),
//            displayNamesForChildrenOf(importing));
    }
//    
//    private static List<String> displayNamesForChildrenOf(FileObject fo) {
//        Children ch = new AntProjectChildren(new AntProjectSupport(fo));
//        Node[] nodes = ch.getNodes(true);
//        return displayNamesFor(nodes);
//    }
    
    private static List<String> displayNamesFor(Node[] nodes) {
        String[] names = new String[nodes.length];
        for (int i = 0; i < nodes.length; i++) {
            names[i] = nodes[i].getDisplayName();
        }
        return Arrays.asList(names);
    }
    
}
