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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.form.layoutsupport.griddesigner.actions;

import java.awt.Component;
import javax.swing.Action;
import org.netbeans.modules.form.layoutsupport.griddesigner.DesignerContext;
import org.netbeans.modules.form.layoutsupport.griddesigner.GridInfoProvider;
import org.netbeans.modules.form.layoutsupport.griddesigner.GridManager;
import org.netbeans.modules.form.layoutsupport.griddesigner.GridUtils;
import org.openide.util.NbBundle;

/**
 * Splits the focused row into two rows.
 *
 * @author Jan Stola
 * @author Petr Somol
 */
public class SplitRowAction extends AbstractGridAction {
    private String name;

    public SplitRowAction() {
        name = NbBundle.getMessage(SplitRowAction.class, "SplitRowAction_Name"); // NOI18N
    }

    @Override
    public Object getValue(String key) {
        return key.equals(Action.NAME) ? name : null;
    }

    @Override
    public boolean isEnabled(DesignerContext context) {
        return (context.getFocusedRow() != -1);
    }

    @Override
    public GridBoundsChange performAction(GridManager gridManager, DesignerContext context) {
        GridInfoProvider gridInfo = gridManager.getGridInfo();
        boolean gapSupport = gridInfo.hasGaps();
        int[] originalColumnBounds = gridInfo.getColumnBounds();
        int[] originalRowBounds = gridInfo.getRowBounds();
        int row = context.getFocusedRow();

        GridUtils.removePaddingComponents(gridManager);
        gridManager.insertRow(row + (gapSupport ? 2 : 1));

        for (Component component : gridManager.getContainer().getComponents()) {
            int y = gridInfo.getGridY(component);
            int height = gridInfo.getGridHeight(component);
            if (y + height - 1 == row) {
                int x = gridInfo.getGridX(component);
                int width = gridInfo.getGridWidth(component);
                gridManager.setGridPosition(component, x, y, width, height + (gapSupport ? 2 : 1));
            }
        }

        GridUtils.revalidateGrid(gridManager);
        gridManager.updateGaps(false);
        GridUtils.addPaddingComponents(gridManager, originalColumnBounds.length - 1, originalRowBounds.length - 1 + (gapSupport ? 2 : 1));
        GridUtils.revalidateGrid(gridManager);

        row += (gapSupport ? 2 : 1);
        int[] newColumnBounds = gridInfo.getColumnBounds();
        int[] newRowBounds = gridInfo.getRowBounds();
        int[] oldRowBounds = new int[originalRowBounds.length + (gapSupport ? 2 : 1)];
        if(gapSupport) {
            if(originalRowBounds.length == row) {
                // inserting after bottommost row
                System.arraycopy(originalRowBounds, 0, oldRowBounds, 0, row);
                oldRowBounds[row] = oldRowBounds[row - 1];
                oldRowBounds[row + 1] = oldRowBounds[row - 1];
            } else {
                System.arraycopy(originalRowBounds, 0, oldRowBounds, 0, row + 1);
                oldRowBounds[row + 1] = oldRowBounds[row];
                oldRowBounds[row + 2] = oldRowBounds[row];
                System.arraycopy(originalRowBounds, row + 1, oldRowBounds, row + 3, originalRowBounds.length - row - 1);
            }
        } else {
            System.arraycopy(originalRowBounds, 0, oldRowBounds, 0, row + 1);
            oldRowBounds[row + 1] = oldRowBounds[row];
            System.arraycopy(originalRowBounds, row + 1, oldRowBounds, row + 2, originalRowBounds.length - row - 1);
        }
        return new GridBoundsChange(originalColumnBounds, oldRowBounds, newColumnBounds, newRowBounds);
    }

}
