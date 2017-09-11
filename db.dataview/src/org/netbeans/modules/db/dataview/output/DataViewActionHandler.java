/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2011 Oracle and/or its affiliates. All rights reserved.
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
package org.netbeans.modules.db.dataview.output;

import org.netbeans.modules.db.dataview.meta.DBTable;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;
import org.openide.windows.WindowManager;

/**
 * Handles all the DataView Panel actions.
 *
 * @author Ahimanikya Satapathy
 */
class DataViewActionHandler {

    private final DataViewPageContext dataPage;
    private final SQLExecutionHelper execHelper;
    private final DataViewUI dataViewUI;
    private final DataView dataView;

    DataViewActionHandler(DataViewUI dataViewUI, DataView dataView, DataViewPageContext pageContext) {
        this.dataView = dataView;
        this.dataViewUI = dataViewUI;

        this.dataPage = pageContext;
        this.execHelper = dataView.getSQLExecutionHelper();
    }

    private boolean rejectModifications() {
        boolean doCalculation = true;
        if (dataViewUI.isCommitEnabled()) {
            String msg = NbBundle.getMessage(DataViewActionHandler.class, "MSG_confirm_commit_changes");
            if ((showYesAllDialog(msg, NbBundle.getMessage(DataViewActionHandler.class, "MSG_confirm_navigation"))).equals(NotifyDescriptor.NO_OPTION)) {
                doCalculation = false;
            }
        }
        return doCalculation;
    }

    void cancelEditPerformed(boolean selectedOnly) {

        synchronized (dataView) {
            if (selectedOnly) {
                DataViewTableUI rsTable = dataViewUI.getDataViewTableUI();
                DataViewTableUIModel updatedRowCtx = dataPage.getModel();
                int[] rows = rsTable.getSelectedRows();
                for (int i = 0; i < rows.length; i++) {
                    int row = rsTable.convertRowIndexToModel(rows[i]);
                    updatedRowCtx.removeUpdateForSelectedRow(row, true);
                }

                if (updatedRowCtx.getUpdateKeys().isEmpty()) {
                    dataViewUI.setCancelEnabled(false);
                    dataViewUI.setCommitEnabled(false);
                }
            } else {
                dataPage.getModel().removeAllUpdates(true);
                dataViewUI.setCancelEnabled(false);
                dataViewUI.setCommitEnabled(false);
            }
        }
    }

    void updateActionPerformed() {
        if (rejectModifications()) {
            int pageSize = dataViewUI.getPageSize();
            dataPage.setPageSize(pageSize);
            org.netbeans.modules.db.dataview.api.DataViewPageContext
                    .setStoredPageSize(pageSize);
            dataPage.first();
            execHelper.executeQueryOffEDT();
        }
    }

    void firstActionPerformed() {
        if (rejectModifications()) {
            dataPage.first();
            execHelper.executeQueryOffEDT();
        }
    }

    void previousActionPerformed() {
        if (rejectModifications()) {
            dataPage.previous();
            execHelper.executeQueryOffEDT();
        }
    }

    void nextActionPerformed() {
        if (rejectModifications()) {
            dataPage.next();
            execHelper.executeQueryOffEDT();
        }
    }

    void commitActionPerformed(boolean selectedOnly) {
        assert dataPage.getTableMetaData().getTableCount() == 1 : "Only one table allowed in resultset if update is invoked";

        if (dataViewUI.isDirty()) {
            execHelper.executeUpdateRow(
                    dataPage.getTableMetaData().getTable(0),
                    dataViewUI.getDataViewTableUI(),
                    selectedOnly);
        }
    }

    void insertActionPerformed() {
        DBTable table = dataPage.getTableMetaData().getTable(0);
        InsertRecordDialog dialog = new InsertRecordDialog(dataView, dataPage, table);
        dialog.setLocationRelativeTo(WindowManager.getDefault().getMainWindow());
        dialog.setVisible(true);
    }

    void truncateActionPerformed() {
        assert dataPage.getTableMetaData().getTableCount() == 1 : "Only one table allowed in resultset if delete is invoked";

        String confirmMsg = NbBundle.getMessage(DataViewActionHandler.class, "MSG_confirm_truncate_table") + dataPage.getTableMetaData().getTable(0).getDisplayName();
        if ((showYesAllDialog(confirmMsg, confirmMsg)).equals(NotifyDescriptor.YES_OPTION)) {
            execHelper.executeTruncate(dataPage, dataPage.getTableMetaData().getTable(0));
        }
    }

    void deleteRecordActionPerformed() {
        assert dataPage.getTableMetaData().getTableCount() == 1 : "Only one table allowed in resultset if delete is invoked";

        DataViewTableUI rsTable = dataViewUI.getDataViewTableUI();
        if (rsTable.getSelectedRowCount() == 0) {
            String msg = NbBundle.getMessage(DataViewActionHandler.class, "MSG_select_delete_rows");
            dataView.setInfoStatusText(msg);
        } else {
            String msg = NbBundle.getMessage(DataViewActionHandler.class, "MSG_confirm_permanent_delete");
            if ((showYesAllDialog(msg, NbBundle.getMessage(DataViewActionHandler.class, "MSG_confirm_delete"))).equals(NotifyDescriptor.YES_OPTION)) {
                DBTable table = dataPage.getTableMetaData().getTable(0);
                execHelper.executeDeleteRow(dataPage, table, rsTable);
            }
        }
    }

    void refreshActionPerformed() {
        execHelper.executeQueryOffEDT();
    }

    private static Object showYesAllDialog(Object msg, String title) {
        NotifyDescriptor nd = new NotifyDescriptor(msg, title, NotifyDescriptor.YES_NO_OPTION, NotifyDescriptor.QUESTION_MESSAGE, null, NotifyDescriptor.NO_OPTION);
        DialogDisplayer.getDefault().notify(nd);
        return nd.getValue();
    }
}
