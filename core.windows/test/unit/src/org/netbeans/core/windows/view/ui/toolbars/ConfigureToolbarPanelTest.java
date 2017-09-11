/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.core.windows.view.ui.toolbars;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.io.IOException;
import javax.swing.AbstractAction;
import javax.swing.Icon;
import org.netbeans.junit.NbTestCase;
import org.openide.cookies.InstanceCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.InstanceDataObject;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.actions.CallbackSystemAction;

public class ConfigureToolbarPanelTest extends NbTestCase {
    private DataFolder folder;
    private Node node;
    
    public ConfigureToolbarPanelTest(String n) {
        super(n);
    }

    @Override
    protected void setUp() throws Exception {
        clearWorkDir();
        FileObject fo = FileUtil.toFileObject(getWorkDir());
        folder = DataFolder.findFolder(fo);
        node = ConfigureToolbarPanel.createFolderActionNode(folder);
    }

    public void testSystemActionWithNoIconResource() throws IOException {
        InstanceDataObject.create(folder, null, NoIcon.class);
        Node[] arr = node.getChildren().getNodes(true);
        assertEquals("Action is not visible", 0, arr.length);
    }

    public void testActionDefinesSmallIcon() throws IOException, ClassNotFoundException {
        InstanceDataObject.create(folder, null, SmallIcon.class);
        Node[] arr = node.getChildren().getNodes(true);
        assertEquals("Action is visible", 1, arr.length);
        final InstanceCookie ic = arr[0].getLookup().lookup(InstanceCookie.class);
        assertEquals("Right class", SmallIcon.class, ic.instanceClass());
    }

    public void testSubfolder() throws Exception {
        DataFolder sub = DataFolder.findFolder(folder.getPrimaryFile().createFolder("sub"));
        InstanceDataObject.create(sub, null, NoIcon.class);
        InstanceDataObject.create(sub, null, SmallIcon.class);
        Node[] arr = node.getChildren().getNodes(true);
        assertEquals(1, arr.length);
        arr = node.getChildren().getNodeAt(0).getChildren().getNodes(true);
        assertEquals(1, arr.length);
    }
    
    public static final class NoIcon extends CallbackSystemAction {

        @Override
        public String getName() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public HelpCtx getHelpCtx() {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        
    }
    
    public static final class SmallIcon extends AbstractAction implements Icon {
        public SmallIcon() {
            putValue(SMALL_ICON, this);
        }
        @Override
        public void actionPerformed(ActionEvent e) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public int getIconWidth() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public int getIconHeight() {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        
    } // end of SmallIcon
}
