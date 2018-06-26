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
/*
 * DDTableModel.java -- synopsis.
 *
 */
package org.netbeans.modules.j2ee.sun.ide.editors.ui;

import java.util.*;

import javax.swing.table.*;

/**
 * Table model used for displaying Deployment
 * Descriptor entries that contain multiple key/value
 * pairs (ie. can be modeled as arrays).
 *
 * @author Joe Warzecha
 */
//
// 29-may-2001
//	Changes for bug 4457984. Changed the signature of addRowAt
//	to get the value of the newly created row and added the
//	methods newElementCancelled and editsCancelled to deal with
//	new rows. (joecorto)
//
public interface DDTableModel extends TableModel {

    /**
     * get name to use in dialog titles
     */
    public String getModelName();
  
    public DDTableModelEditor getEditor();

    public Object [] getValue ();

    public Object getValueAt (int row);

    public void setValueAt (int row, Object value);

    public Object makeNewElement ();

    /**
     * Called when a user cancels adding a row.
     */
    public void newElementCancelled(Object newRow);
  
    public void addRowAt (int row, Object newRow, Object editedValue);


    public void removeRowAt(int row);

    /**
     * Verify that the edits performed are OK.
     * NOTE: This method simply returns true or false which
     *       indicate if the edits are OK. Any error dialogs
     *       that would need to be displayed must be done by
     *       the implementing class to allow for greater flexibility
     *       in the error reporting.
     * return true if edit is OK
     * return false if the edit should not applied after all.
     */
    public boolean isEditValid (Object rowValue, int row);

    /**
     * Check to see if supplied row can be deleted.
     */
    public List canRemoveRow (int row);
    
    /**
     * invoke underlying model to validate integrity of data. 
     * @return empty list if valid, otherwise list of all errors
     */
    public List isValueValid(Object rowValue, int fromRow);

    /**
     * Called when the user cancels all edits to the table.
     */
    public void editsCancelled();
}
