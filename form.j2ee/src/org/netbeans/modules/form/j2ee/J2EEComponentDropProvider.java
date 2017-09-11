/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
