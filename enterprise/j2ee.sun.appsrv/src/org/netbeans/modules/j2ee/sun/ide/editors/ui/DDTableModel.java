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
