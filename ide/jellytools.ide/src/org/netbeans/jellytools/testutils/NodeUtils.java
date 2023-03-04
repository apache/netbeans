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

import java.awt.Toolkit;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.actions.DeleteAction;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.properties.PropertySheetOperator;
import org.netbeans.jemmy.JemmyException;
import org.netbeans.jemmy.Waitable;
import org.netbeans.jemmy.Waiter;
import org.netbeans.jemmy.operators.JDialogOperator;


/** Utilities to test nodes
 *
 * @author Jiri.Skrivanek@sun.com
 */
public class NodeUtils {

    /** Test cut */
    public static void testClipboard(final Object clipboard1) {        
        Waiter waiter = new Waiter(new Waitable() {
                public Object actionProduced(Object obj) {
                    Object clipboard2 = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);
                    return clipboard1 != clipboard2 ? Boolean.TRUE : null;
                }
                public String getDescription() {
                    return("Wait clipboard contains data"); // NOI18N
                }
        });
        try {
            waiter.waitAction(null);
        } catch (InterruptedException e) {
            throw new JemmyException("Waiting interrupted.", e);
        }
    }

    /** Close "Confirm Object Deletion" dialog. **/
    public static void closeConfirmDeleteDialog() {
        // "Confirm Object Deletion"
        String confirmTitle = Bundle.getString("org.openide.explorer.Bundle",
                                               "MSG_ConfirmDeleteObjectTitle"); // NOI18N
        new JDialogOperator(confirmTitle).requestClose();
    }
    
    /** Close "Rename" dialog. **/
    public static void closeRenameDialog() {
        String renameTitle = Bundle.getString("org.openide.actions.Bundle", "CTL_RenameTitle"); 
        new JDialogOperator(renameTitle).requestClose();
    }
 
    /** Close properties */
    public static void closeProperties(String objectName) {
        new PropertySheetOperator(objectName).close();
    }

    /** Perform delete action and confirm refactoring dialog. */
    public static void performDelete(Node node) {

        new DeleteAction().performAPI(node);
        // "Confirm Object Deletion"
        String deleteTitle = Bundle.getString("org.openide.explorer.Bundle",
                                               "MSG_ConfirmDeleteObjectTitle"); // NOI18N

        NbDialogOperator deleteOper = new NbDialogOperator(deleteTitle);
        deleteOper.yes();
    }

}
