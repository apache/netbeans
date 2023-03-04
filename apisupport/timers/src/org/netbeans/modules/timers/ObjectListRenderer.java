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
package org.netbeans.modules.timers;

import java.awt.Component;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.logging.Level;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import org.openide.explorer.view.NodeRenderer;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;

/** Renderer for various NetBeans objects.
 *
 * @author Jaroslav Tulach
 */
final class ObjectListRenderer extends DefaultListCellRenderer {

    private NodeRenderer r;

    ObjectListRenderer() {
        super();
        r = new NodeRenderer();
    }

    @Override
    public Component getListCellRendererComponent(JList list, Object wr, int index, boolean isSelected, boolean cellHasFocus) {
        Object value = ((WeakReference) wr).get();
        if (value instanceof FileObject) {
            try {
                FileObject fo = (FileObject) value;
                value = DataObject.find(fo);
            } catch (IOException e) {
                FileObject fo = (FileObject) value;
                value = "FO: " + fo;
            }
        }
        
        if (value instanceof DataObject) {
            DataObject obj = (DataObject)value;
            for (;;) {
                if (obj.isValid()) {
                    try {
                        value = obj.getNodeDelegate();
                    } catch (IllegalStateException ex) {
                        TimeComponentPanel.LOG.log(Level.INFO, "Object became invalid " + obj.getPrimaryFile(), ex);
                        continue;
                    }
                } else {
                    value = obj.getName();
                }
                break;
            }
        }
        
        if (value instanceof Node) {
            Node node = (Node)value;
            return r.getListCellRendererComponent(list, node, index, isSelected, cellHasFocus);
        }

        return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
    }
}
