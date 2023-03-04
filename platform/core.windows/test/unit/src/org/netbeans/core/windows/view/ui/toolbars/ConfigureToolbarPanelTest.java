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
