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
package org.openide.windows;

import java.awt.Frame;
import java.awt.Image;
import java.beans.PropertyChangeListener;
import java.util.Set;
import javax.swing.Action;
import org.netbeans.junit.NbTestCase;
import org.openide.nodes.Node;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.windows.WindowManager.Component;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class OnShowingTest extends NbTestCase{
    private InstanceContent stop;
    private InstanceContent start;
    private OnShowingHandler onShowing;
    
    public OnShowingTest(String n) {
        super(n);
    }

    @Override
    protected void setUp() throws Exception {
        start = new InstanceContent();
        stop = new InstanceContent();
        onShowing = new OnShowingHandler(
            new AbstractLookup(start),
            new WindowManager() {
                @Override
                public void invokeWhenUIReady(Runnable run) {
                    run.run();
                }


                @Override
                public Mode findMode(String name) {
                    throw new UnsupportedOperationException("Not supported yet.");
                }

                @Override
                public Mode findMode(TopComponent tc) {
                    throw new UnsupportedOperationException("Not supported yet.");
                }

                @Override
                public Set<? extends Mode> getModes() {
                    throw new UnsupportedOperationException("Not supported yet.");
                }

                @Override
                public Frame getMainWindow() {
                    throw new UnsupportedOperationException("Not supported yet.");
                }

                @Override
                public void updateUI() {
                    throw new UnsupportedOperationException("Not supported yet.");
                }

                @Override
                protected Component createTopComponentManager(TopComponent c) {
                    throw new UnsupportedOperationException("Not supported yet.");
                }

                @Override
                public Workspace createWorkspace(String name, String displayName) {
                    throw new UnsupportedOperationException("Not supported yet.");
                }

                @Override
                public Workspace findWorkspace(String name) {
                    throw new UnsupportedOperationException("Not supported yet.");
                }

                @Override
                public Workspace[] getWorkspaces() {
                    throw new UnsupportedOperationException("Not supported yet.");
                }

                @Override
                public void setWorkspaces(Workspace[] workspaces) {
                    throw new UnsupportedOperationException("Not supported yet.");
                }

                @Override
                public Workspace getCurrentWorkspace() {
                    throw new UnsupportedOperationException("Not supported yet.");
                }

                @Override
                public TopComponentGroup findTopComponentGroup(String name) {
                    throw new UnsupportedOperationException("Not supported yet.");
                }

                @Override
                public void addPropertyChangeListener(PropertyChangeListener l) {
                    throw new UnsupportedOperationException("Not supported yet.");
                }

                @Override
                public void removePropertyChangeListener(PropertyChangeListener l) {
                    throw new UnsupportedOperationException("Not supported yet.");
                }

                @Override
                protected void topComponentOpen(TopComponent tc) {
                    throw new UnsupportedOperationException("Not supported yet.");
                }

                @Override
                protected void topComponentClose(TopComponent tc) {
                    throw new UnsupportedOperationException("Not supported yet.");
                }

                @Override
                protected void topComponentRequestActive(TopComponent tc) {
                    throw new UnsupportedOperationException("Not supported yet.");
                }

                @Override
                protected void topComponentRequestVisible(TopComponent tc) {
                    throw new UnsupportedOperationException("Not supported yet.");
                }

                @Override
                protected void topComponentDisplayNameChanged(TopComponent tc, String displayName) {
                    throw new UnsupportedOperationException("Not supported yet.");
                }

                @Override
                protected void topComponentHtmlDisplayNameChanged(TopComponent tc, String htmlDisplayName) {
                    throw new UnsupportedOperationException("Not supported yet.");
                }

                @Override
                protected void topComponentToolTipChanged(TopComponent tc, String toolTip) {
                    throw new UnsupportedOperationException("Not supported yet.");
                }

                @Override
                protected void topComponentIconChanged(TopComponent tc, Image icon) {
                    throw new UnsupportedOperationException("Not supported yet.");
                }

                @Override
                protected void topComponentActivatedNodesChanged(TopComponent tc, Node[] activatedNodes) {
                    throw new UnsupportedOperationException("Not supported yet.");
                }

                @Override
                protected boolean topComponentIsOpened(TopComponent tc) {
                    throw new UnsupportedOperationException("Not supported yet.");
                }

                @Override
                protected Action[] topComponentDefaultActions(TopComponent tc) {
                    throw new UnsupportedOperationException("Not supported yet.");
                }

                @Override
                protected String topComponentID(TopComponent tc, String preferredID) {
                    throw new UnsupportedOperationException("Not supported yet.");
                }

                @Override
                public TopComponent findTopComponent(String tcID) {
                    throw new UnsupportedOperationException("Not supported yet.");
                }
            }
        );
    }
    
    public void testShowingInvokedDuringInit() {
        final boolean[] ok = { false };
        start.add(new Runnable() {
            @Override public void run() {
                ok[0] = true;
            }
        });
        
        onShowing.initialize();
        
        assertTrue("Initialized", ok[0]);
    }

    public void testShowingIsInvokedWhenModuleIsAdded() {
        final boolean[] ok = { false };
        
        onShowing.initialize();
        start.add(new Runnable() {
            @Override public void run() {
                ok[0] = true;
            }
        });
        
        assertTrue("Initialized", ok[0]);
    }
}
