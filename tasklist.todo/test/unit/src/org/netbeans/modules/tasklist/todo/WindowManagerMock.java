/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
