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
