/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.php.nette2.ui.codegen;

import javax.swing.table.DefaultTableModel;
import org.netbeans.modules.php.nette2.codegen.ActionRenderMethodChecker;
import org.openide.util.NbBundle;

/**
 *
 * @author Ondrej Brejla <obrejla@netbeans.org>
 */
public class ActionRenderCodeGeneratorTableModel  extends DefaultTableModel {
    private static final String ACTION_COLUMN_TITLE = "action<action>()"; //NOI18N
    private static final String RENDER_COLUMN_TITLE = "render<action>()"; //NOI18N
    private final ActionRenderMethodChecker methodChecker;
    private static final Class[] TYPES = new Class[] {
        String.class, Boolean.class, Boolean.class
    };

    @NbBundle.Messages("LBL_ActionName=Action name:")
    public ActionRenderCodeGeneratorTableModel(ActionRenderMethodChecker methodChecker) {
        super(
                null,
                new String[]{
                    Bundle.LBL_ActionName(),
                    ACTION_COLUMN_TITLE,
                    RENDER_COLUMN_TITLE
                });
        this.methodChecker = methodChecker;
    }

    @Override
    public boolean isCellEditable(int row, int column) {
        return column != 0;
    }

    @Override
    public Class getColumnClass(int columnIndex) {
        return TYPES[columnIndex];
    }

    @Override
    public void setValueAt(Object aValue, int row, int column) {
        String action = (String) getValueAt(row, 0);
        switch (column) {
            case 1:
                if (methodChecker.existsActionMethod(action)) {
                    super.setValueAt(false, row, column);
                } else {
                    super.setValueAt(aValue, row, column);
                }
                break;
            case 2:
                if (methodChecker.existsRenderMethod(action)) {
                    super.setValueAt(false, row, column);
                } else {
                    super.setValueAt(aValue, row, column);
                }
                break;
            default:
                super.setValueAt(aValue, row, column);
        }
    }
}
