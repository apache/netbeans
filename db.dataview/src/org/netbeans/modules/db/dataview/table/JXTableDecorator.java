/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.db.dataview.table;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import org.jdesktop.swingx.JXTable;
import org.netbeans.modules.db.dataview.util.ColorHelper;

/**
 *
 * @author Ahimanikya Satapathy
 */
public class JXTableDecorator extends JXTable {

    public static final Color ROW_COLOR = ColorHelper.getTableBackground();
    public static final Color ALTERNATE_ROW_COLOR = ColorHelper.getTableAltbackground();
    public static final Color GRID_COLOR = ColorHelper.getTableGrid();
    public static final Color ROLLOVER_ROW_COLOR = ColorHelper.getTableRollOverRowBackground();

    JXTableDecorator() {
        super();
    }

    public JXTableDecorator(TableModel model, TableColumnModel columnModel, ListSelectionModel selectionModel) {
        super(model, columnModel, selectionModel);
    }

    public JXTableDecorator(TableModel dm, TableColumnModel cm) {
        super(dm, cm);
    }

    public JXTableDecorator(TableModel dm) {
        super(dm);
    }

    /**
     * Paints empty rows too
     */
    @Override
    public void paint(Graphics g) {
        super.paint(g);
        paintEmptyRows(g);
    }

    /**
     * Paints the backgrounds of the implied empty rows when the
     * table model is insufficient to fill all the visible area
     * available to us. We don't involve cell renderers, because
     * we have no data.
     */
    protected void paintEmptyRows(Graphics g) {
        final int rowCount = getRowCount();
        final Rectangle clip = g.getClipBounds();
        final int height = clip.y + clip.height;
        if (rowCount * rowHeight < height) {
            for (int i = rowCount; i <= height / rowHeight; ++i) {
                g.setColor(backgroundColorForRow(i));
                g.fillRect(clip.x, i * rowHeight, clip.width, rowHeight);
                drawHorizontalLine(g, clip, i);
            }
            drawVerticalLines(g, rowCount, height);
        }
    }

    protected void drawHorizontalLine(Graphics g, final Rectangle clip, int i) {
        g.setColor(ResultSetJXTable.GRID_COLOR);
        g.drawLine(clip.x, i * rowHeight - 1, clip.x + clip.width, i * rowHeight - 1);
    }

    protected void drawVerticalLines(Graphics g, final int rowCount, final int height) {

        g.setColor(ResultSetJXTable.GRID_COLOR);
        TableColumnModel colModel = getColumnModel();
        int x = 0;
        for (int i = 0; i < colModel.getColumnCount(); ++i) {
            TableColumn column = colModel.getColumn(i);
            x += column.getWidth();
            g.drawLine(x - 1, rowCount * rowHeight, x - 1, height);
        }
    }

    protected Color backgroundColorForRow(int row) {
        return (row % 2 == 0) ? ResultSetJXTable.ROW_COLOR : ResultSetJXTable.ALTERNATE_ROW_COLOR;
    }
}
