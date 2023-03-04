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

package org.netbeans.modules.openfile;

import java.awt.Component;
import java.awt.Frame;
import java.awt.Image;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.Set;
import javax.swing.Action;
import org.netbeans.api.sendopts.CommandLine;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.Node;
import org.openide.nodes.NodeAcceptor;
import org.openide.nodes.NodeOperation;
import org.openide.util.UserCancelException;
import org.openide.windows.*;

/** 
 *
 * @author Jaroslav Tulach
 */
public class OpenCLITest extends NbTestCase {
    File dir;
    
    public OpenCLITest(String testName) {
        super(testName);
    }
    
    @Override
    protected boolean runInEQ() {
        return true;
    }
    
    @Override
    protected void setUp() throws Exception {
        dir = new File(getWorkDir(), "tstdir");
        dir.mkdirs();
        
        MockServices.setServices(MockWindowManager.class,
                MockNodeOperation.class);
        MockNodeOperation.explored = null;
    }

    @Override
    protected void tearDown() throws Exception {
    }
    
    public void testOpenFolder() throws Exception {
        CommandLine.create(Handler.class).process(
                new String[] { "--open", dir.getPath()});

        assertNotNull("A node has been explored", MockNodeOperation.explored);
        
        FileObject root = MockNodeOperation.explored.getLookup().lookup(FileObject.class);
        assertNotNull("There is a file object in lookup", root);
        
        assertEquals("It is our dir", dir, FileUtil.toFile(root));
    }
    
    public static final class MockNodeOperation extends NodeOperation {
        public static Node explored;
        
        @Override
        public boolean customize(Node n) {
            fail("No customize");
            return false;
        }

        @Override
        public void explore(Node n) {
            assertNull("No explore before", explored);
            explored = n;
        }

        @Override
        public void showProperties(Node n) {
            fail("no props");
        }

        @Override
        public void showProperties(Node[] n) {
            fail("no props");
        }

        @Override
        public Node[] select(String title, String rootTitle, Node root, NodeAcceptor acceptor, Component top) throws UserCancelException {
            fail("no select");
            return null;
        }
        
    }

    public static final class MockWindowManager extends WindowManager {

        @Override
        public void invokeWhenUIReady(Runnable runnable) {
            // run immediatelly
            runnable.run();
        }

        @Override
        public Mode findMode(String name) {
            throw unsupp();
        }

        @Override
        public Mode findMode(TopComponent tc) {
            throw unsupp();
        }

        @Override
        public Set<? extends Mode> getModes() {
            throw unsupp();
        }

        @Override
        public Frame getMainWindow() {
            throw unsupp();
        }

        @Override
        public void updateUI() {
            throw unsupp();
        }

        @Override
        protected Component createTopComponentManager(TopComponent c) {
            throw unsupp();
        }

        @Override
        public Workspace createWorkspace(String name, String displayName) {
            throw unsupp();
        }

        @Override
        public Workspace findWorkspace(String name) {
            throw unsupp();
        }

        @Override
        public Workspace[] getWorkspaces() {
            throw unsupp();
        }

        @Override
        public void setWorkspaces(Workspace[] workspaces) {
            throw unsupp();
        }

        @Override
        public Workspace getCurrentWorkspace() {
            throw unsupp();
        }

        @Override
        public TopComponentGroup findTopComponentGroup(String name) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void addPropertyChangeListener(PropertyChangeListener l) {
            throw unsupp();
        }

        @Override
        public void removePropertyChangeListener(PropertyChangeListener l) {
            throw unsupp();
        }

        @Override
        protected void topComponentOpen(TopComponent tc) {
            throw unsupp();
        }

        @Override
        protected void topComponentClose(TopComponent tc) {
            throw unsupp();
        }

        @Override
        protected void topComponentRequestActive(TopComponent tc) {
            throw unsupp();
        }

        @Override
        protected void topComponentRequestVisible(TopComponent tc) {
            throw unsupp();
        }

        @Override
        protected void topComponentDisplayNameChanged(TopComponent tc, String displayName) {
            throw unsupp();
        }

        @Override
        protected void topComponentHtmlDisplayNameChanged(TopComponent tc, String htmlDisplayName) {
            throw unsupp();
        }

        @Override
        protected void topComponentToolTipChanged(TopComponent tc, String toolTip) {
            throw unsupp();
        }

        @Override
        protected void topComponentIconChanged(TopComponent tc, Image icon) {
            throw unsupp();
        }

        @Override
        protected void topComponentActivatedNodesChanged(TopComponent tc,
                Node[] activatedNodes) {
            throw unsupp();
        }

        @Override
        protected boolean topComponentIsOpened(TopComponent tc) {
            throw unsupp();
        }

        @Override
        protected Action[] topComponentDefaultActions(TopComponent tc) {
            throw unsupp();
        }

        @Override
        protected String topComponentID(TopComponent tc, String preferredID) {
            throw unsupp();
        }

        @Override
        public TopComponent findTopComponent(String tcID) {
            throw unsupp();
        }

        private UnsupportedOperationException unsupp() {
            return new UnsupportedOperationException("Not supported yet.");
        }
    }
}