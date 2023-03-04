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

package org.netbeans.modules.project.ui;

import java.util.Collections;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.junit.Log;
import org.netbeans.modules.project.ui.actions.TestSupport.TestProject;
import org.netbeans.spi.project.ui.LogicalViewProvider;
import org.openide.loaders.DataObject;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

/** 
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class ProjectsRootNodePreferredOpen2Test extends ProjectsRootNodePreferredOpenTest {
    private CharSequence log;
    
    public ProjectsRootNodePreferredOpen2Test(String testName) {
        super(testName);
    }            
    
    @Override
    Lookup createLookup(TestProject project, Object instance) {
        return Lookups.fixed(instance, new MyTestProjectAdditions(project));
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        log = Log.enable("", Level.WARNING);
        Logger.getLogger("org.netbeans.ui").setLevel(Level.OFF);
        Logger.getLogger("org.openide.util").setLevel(Level.OFF);
         //compute project root node children in sync mode
        System.setProperty("test.projectnode.sync", "true");
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        
        if (log.length() > 0) {
            fail("No warnings printed to log:\n" + log);
        }
    }
    
    
    
    private static final class MyTestProjectAdditions
            implements LogicalViewProvider {
        private TestProject project;
        public MyTestProjectAdditions(TestProject project) {
            this.project = project;
        }

        public Node createLogicalView() {
            return new AbstractNode(Children.LEAF, Lookups.singleton(project));
        }

        public Node findPath(Node root, Object target) {
            return null;
        }
    }
}
