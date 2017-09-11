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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
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
