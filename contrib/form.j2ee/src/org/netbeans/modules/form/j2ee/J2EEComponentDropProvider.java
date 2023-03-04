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

package org.netbeans.modules.form.j2ee;

import java.awt.datatransfer.Transferable;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.netbeans.api.db.explorer.DatabaseMetaDataTransfer;
import org.netbeans.modules.form.FormModel;
import org.netbeans.modules.form.NewComponentDrop;
import org.netbeans.modules.form.NewComponentDropProvider;

/**
 * Provider of <code>NewComponentDrop</code>s for DB and J2EE objects.
 *
 * @author Jan Stola
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.form.NewComponentDropProvider.class)
public class J2EEComponentDropProvider implements NewComponentDropProvider {
    
    /**
     * Processes given <code>transferable</code> and returns the corresponding
     * <code>NewComponentDrop</code>.
     *
     * @param formModel corresponding form model.
     * @param transferable description of transferred data.
     * @return <code>NewComponentDrop</code> that corresponds to given
     * <code>transferable</code> or <code>null</code> if this provider
     * don't understand to or don't want to process this data transfer.
     */
    @Override
    public NewComponentDrop processTransferable(FormModel formModel, Transferable transferable) {
        if (J2EEUtils.supportsJPA(formModel)) {
            try {
                if (transferable.isDataFlavorSupported(DatabaseMetaDataTransfer.CONNECTION_FLAVOR)) {
                    DatabaseMetaDataTransfer.Connection connection = (DatabaseMetaDataTransfer.Connection)transferable.getTransferData(DatabaseMetaDataTransfer.CONNECTION_FLAVOR);
                    return new DBConnectionDrop(formModel, connection);
                } else if (transferable.isDataFlavorSupported(DatabaseMetaDataTransfer.COLUMN_FLAVOR)) {
                    DatabaseMetaDataTransfer.Column column = (DatabaseMetaDataTransfer.Column)transferable.getTransferData(DatabaseMetaDataTransfer.COLUMN_FLAVOR);
                    return new DBColumnDrop(formModel, column);
                } else if (transferable.isDataFlavorSupported(DatabaseMetaDataTransfer.TABLE_FLAVOR)) {
                    DatabaseMetaDataTransfer.Table table = (DatabaseMetaDataTransfer.Table)transferable.getTransferData(DatabaseMetaDataTransfer.TABLE_FLAVOR);
                    return new DBTableDrop(formModel, table);
                }
            } catch (Exception ex) {
                // should not happen
                Logger.getLogger(getClass().getName()).log(Level.INFO, ex.getMessage(), ex);
            }
        }
        return null;
    }

}
