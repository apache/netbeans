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

package org.netbeans.jellytools.testutils;

import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.actions.DeleteAction;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.operators.JDialogOperator;

/**
 * An extension to NodeUtils which adds support for safe delete dialog.
 *
 * @author Vojtech.Sigler@sun.com
 */
public class JavaNodeUtils extends NodeUtils {

    public static void closeSafeDeleteDialog()
    {
        String safeDeleteTitle = Bundle.getString("org.netbeans.modules.refactoring.java.ui.Bundle",
                "LBL_SafeDel_Delete"); // NOI18N
        new JDialogOperator(safeDeleteTitle).requestClose();
    }

    public static void performSafeDelete(Node node) {
        new DeleteAction().performAPI(node);
        // wait for one of Delete dialogs
        NbDialogOperator deleteDialogOper = new NbDialogOperator("Delet");
        if (deleteDialogOper.getTitle().equals("Delete")) {
            // "Delete" - safe delete when scanning is not running
            deleteDialogOper.ok();
        } else {
            // "Confirm Object Deletion" - if scanning is in progress
            deleteDialogOper.yes();
        }
        deleteDialogOper.waitClosed();
    }
}
