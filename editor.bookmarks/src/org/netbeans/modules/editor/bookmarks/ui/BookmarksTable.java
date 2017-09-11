/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.editor.bookmarks.ui;

import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import javax.swing.Action;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import org.netbeans.swing.etable.ETable;
import org.openide.util.NbBundle;

/**
 * Table displaying bookmarks.
 *
 * @author Miloslav Metelka
 */
public class BookmarksTable extends ETable {
    
    BookmarksTable() {
        super(new BookmarksTableModel(false));
        init();
    }

    private void init() {
        setRowSelectionAllowed(true);
        setColumnSelectionAllowed(false);
        getTableHeader().setResizingAllowed(true);
        setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
        setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        TableColumnModel colModel = getColumnModel();
        TableColumn col;
        col = colModel.getColumn(BookmarksTableModel.NAME_COLUMN);
        col.setHeaderValue(NbBundle.getMessage(BookmarksTable.class, "LBL_BookmarkName"));

        col = colModel.getColumn(BookmarksTableModel.KEY_COLUMN);
        col.setHeaderValue(NbBundle.getMessage(BookmarksTable.class, "LBL_BookmarkKey"));

        col = colModel.getColumn(BookmarksTableModel.LOCATION_COLUMN);
        col.setHeaderValue(NbBundle.getMessage(BookmarksTable.class, "LBL_BookmarkLocation"));
        col.setCellRenderer(new BookmarkNodeRenderer(false));

        getTableHeader().addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                JTableHeader header = (JTableHeader) e.getSource();
                int columnIndex = header.columnAtPoint(e.getPoint());
                if (columnIndex != -1) {
                    header.setToolTipText(getHeaderToolTipText(columnIndex));
                }
            }
        });

        // doubleclick jumps to source in editor
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 || SwingUtilities.isRightMouseButton(e)) {
                    if (getModel() instanceof BookmarksTableModel) {
                        BookmarksTableModel model = (BookmarksTableModel) getModel();
                        int row = convertRowIndexToModel(((JTable) e.getSource()).getSelectedRow());

                        final BookmarkNode node = model.getEntry(row);
                        if (e.getClickCount() == 2) {
                            node.openInEditor();
                        } else if (SwingUtilities.isRightMouseButton(e)) {
                            JTable table = ((JTable) e.getSource());
                            int r = table.rowAtPoint(e.getPoint());
                            if (r >= 0 && r < table.getRowCount()) {
                                table.setRowSelectionInterval(r, r);
                                final BookmarkNode rightClickNode = model.getEntry(convertRowIndexToModel(r));
                                JPopupMenu menu = rightClickNode.getContextMenu();
                                menu.remove(2);
                                Action a = getActionMap().get("delete");
                                a.putValue(Action.NAME, "Delete");
                                menu.add(a);
                                menu.show(BookmarksTable.this, e.getPoint().x, e.getPoint().y);
                            }   
                        }
                    }
                }
            }
        });
        
        getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "delete"); // NOI18N
    }
    
    private String getHeaderToolTipText(int columnIndex) {
        switch (columnIndex) {
            case BookmarksTableModel.NAME_COLUMN:
                return NbBundle.getMessage(BookmarksTable.class, "LBL_BookmarkNameDescription");
            case BookmarksTableModel.KEY_COLUMN:
                return NbBundle.getMessage(BookmarksTable.class, "LBL_BookmarkKeyDescription");
            case BookmarksTableModel.LOCATION_COLUMN:
                return NbBundle.getMessage(BookmarksTable.class, "LBL_BookmarkLocationDescription");
            default:
                return null;
        }
    }

    @Override
    public String getToolTipText(MouseEvent event) {
        String toolTipText;
        Point p = event.getPoint();
        int hitColumnIndex = columnAtPoint(p);
        int hitRowIndex = rowAtPoint(p);
        if (hitRowIndex != -1 && hitColumnIndex != -1) {
            toolTipText = ((BookmarksTableModel)getModel()).getToolTipText(hitRowIndex, hitColumnIndex);
        } else {
            toolTipText = super.getToolTipText(event);
        }
        return toolTipText;
    }

}
