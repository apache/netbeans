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
package org.netbeans.modules.editor.bookmarks.ui;

import java.awt.Component;
import java.awt.Image;
import java.beans.BeanInfo;
import javax.swing.Icon;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import org.netbeans.modules.editor.bookmarks.BookmarkInfo;
import org.openide.nodes.Node;
import org.openide.util.ImageUtilities;

/**
 * Renderer of BookmarkNode in a JTable.
 *
 * @author Miloslav Metelka
 */
public class BookmarkNodeRenderer extends DefaultTableCellRenderer {
    
    private boolean forHistoryPopup;
    
    BookmarkNodeRenderer(boolean forHistoryPopup) {
        this.forHistoryPopup = forHistoryPopup;
    }
        
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

        Icon icon = null;
        BookmarksTableModel model = (BookmarksTableModel) table.getModel();
        BookmarkNode bNode = model.getEntry(row);
        BookmarkInfo bookmark = bNode.getBookmarkInfo();
        Node fNode = bNode.getParentNode();
        if (fNode != null) {
            if (!isSelected) {
                String text = fNode.getHtmlDisplayName();
                if (text != null) {
                    text = bookmark.getDescription(text, forHistoryPopup, forHistoryPopup, true);
                    setText("<html>" + text + "</html>");
                } // else leave original text set by "super"
            }
            Image image = fNode.getIcon(BeanInfo.ICON_COLOR_16x16);
            if (image != null) {
                icon = ImageUtilities.image2Icon(image);
            }
        }
        setIcon(icon);
        return this;

    }

}
