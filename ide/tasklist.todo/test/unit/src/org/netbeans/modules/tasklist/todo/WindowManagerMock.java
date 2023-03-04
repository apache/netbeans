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
package org.netbeans.modules.tasklist.todo;

import java.awt.Frame;
import java.awt.Image;
import java.beans.PropertyChangeListener;
import java.util.Set;
import javax.swing.Action;
import org.openide.nodes.Node;
import org.openide.windows.Mode;
import org.openide.windows.TopComponent;
import org.openide.windows.TopComponentGroup;
import org.openide.windows.WindowManager;
import org.openide.windows.Workspace;

/**
 *
 * @author pzajac
 */
public class WindowManagerMock extends WindowManager {

    TopComponent otc;
    public WindowManagerMock() {
    }

    public void setOpenedEditorTopComponent(TopComponent tc) {
        otc = tc;
    }
    @Override
    public boolean isEditorTopComponent(TopComponent tc) {
        return true;
    }

    @Override
    public boolean isOpenedEditorTopComponent(TopComponent tc) {
        return  (tc != null && tc == otc);
    }
    
    public Mode findMode(String name) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Mode findMode(TopComponent tc) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Set<? extends Mode> getModes() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Frame getMainWindow() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void updateUI() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    protected Component createTopComponentManager(TopComponent c) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Workspace createWorkspace(String name, String displayName) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Workspace findWorkspace(String name) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Workspace[] getWorkspaces() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setWorkspaces(Workspace[] workspaces) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Workspace getCurrentWorkspace() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public TopComponentGroup findTopComponentGroup(String name) {
        return null;
    }

    public void addPropertyChangeListener(PropertyChangeListener l) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void removePropertyChangeListener(PropertyChangeListener l) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    protected void topComponentOpen(TopComponent tc) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    protected void topComponentOpenAtTabPosition(TopComponent tc, int position) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    protected int topComponentGetTabPosition(TopComponent tc) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    protected void topComponentClose(TopComponent tc) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    protected void topComponentRequestActive(TopComponent tc) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    protected void topComponentRequestVisible(TopComponent tc) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    protected void topComponentDisplayNameChanged(TopComponent tc,
                                                  String displayName) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    protected void topComponentHtmlDisplayNameChanged(TopComponent tc,
                                                      String htmlDisplayName) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    protected void topComponentToolTipChanged(TopComponent tc, String toolTip) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    protected void topComponentIconChanged(TopComponent tc, Image icon) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    protected void topComponentActivatedNodesChanged(TopComponent tc,
                                                     Node[] activatedNodes) {
//        throw new UnsupportedOperationException("Not supported yet.");
    }

    protected boolean topComponentIsOpened(TopComponent tc) {
       return false;
       // throw new UnsupportedOperationException("Not supported yet.");
    }

    protected Action[] topComponentDefaultActions(TopComponent tc) {
        
        throw new UnsupportedOperationException("Not supported yet.");
    }

    protected String topComponentID(TopComponent tc, String preferredID) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public TopComponent findTopComponent(String tcID) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
