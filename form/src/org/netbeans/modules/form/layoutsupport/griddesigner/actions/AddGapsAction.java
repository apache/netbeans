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

import javax.swing.Action;
import org.netbeans.modules.form.FormLoaderSettings;
import org.netbeans.modules.form.layoutsupport.griddesigner.DesignerContext;
import org.netbeans.modules.form.layoutsupport.griddesigner.GridInfoProvider;
import org.netbeans.modules.form.layoutsupport.griddesigner.GridManager;
import org.netbeans.modules.form.layoutsupport.griddesigner.GridUtils;
import org.openide.util.NbBundle;

/**
 * Action that adds gaps between rows and columns.
 *
 * @author Petr Somol
 * @author Jan Stola
 */
public class AddGapsAction extends AbstractGridAction {
    private String name;

    public AddGapsAction() {
        name = NbBundle.getMessage(AddGapsAction.class, "AddGapsAction_Name"); // NOI18N
    }

    @Override
    public Object getValue(String key) {
        return key.equals(Action.NAME) ? name : null;
    }

    @Override
    public boolean isEnabled(DesignerContext context) {
        return !context.getGridInfo().hasGaps();
    }

    @Override
    public GridBoundsChange performAction(GridManager gridManager, DesignerContext context) {
        GridInfoProvider gridInfo = gridManager.getGridInfo();
        int[] originalColumnBounds = gridInfo.getColumnBounds();
        int[] originalRowBounds = gridInfo.getRowBounds();
        int columnCount = Math.max( 2 * originalColumnBounds.length - 3, 1);
        int rowCount = Math.max(2 * originalRowBounds.length - 3, 1);

        GridUtils.removePaddingComponents(gridManager);
        int gapWidth = FormLoaderSettings.getInstance().getGapWidth();
        int gapHeight = FormLoaderSettings.getInstance().getGapHeight();
        gridManager.addGaps(gapWidth, gapHeight);
        GridUtils.addPaddingComponents(gridManager, columnCount, rowCount);
        GridUtils.revalidateGrid(gridManager);

        int[] newColumnBounds = gridInfo.getColumnBounds();
        int[] newRowBounds = gridInfo.getRowBounds();
        int[] columnBounds;
        int[] rowBounds;

        columnBounds = new int[newColumnBounds.length];
        columnBounds[0] = originalColumnBounds[0];
        for(int i=1; i<originalColumnBounds.length - 1; i++) {
            columnBounds[2 * i] = columnBounds[2 * i - 1] = originalColumnBounds[i];
        }
        columnBounds[newColumnBounds.length - 1] = originalColumnBounds[originalColumnBounds.length - 1];
        rowBounds = new int[newRowBounds.length];
        rowBounds[0] = originalRowBounds[0];
        for(int j=1; j<originalRowBounds.length - 1; j++) {
            rowBounds[2 * j] = rowBounds[2 * j - 1] = originalRowBounds[j];
        }
        rowBounds[newRowBounds.length - 1] = originalRowBounds[originalRowBounds.length - 1];
        return new GridBoundsChange(columnBounds, rowBounds, newColumnBounds, newRowBounds);
    }

}
