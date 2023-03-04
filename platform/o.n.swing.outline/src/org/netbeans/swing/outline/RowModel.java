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
package org.netbeans.swing.outline;

/** A model for the rows in an Outline.  This is passed the object in
 * column 0 of an Outline table (the tree column), and provides objects
 * for the other columns - essentially a model for the data in the
 * rows of an Outline.
 * <p>
 * Note that all column indexes passed to this interface are 0-based -
 * that is, column 0 is the first column <strong>after</strong> the
 * tree node column, so the object returned by <code>getValueFor(someObject, 0)</code>
 * is the object that should appear in column <strong>1</strong> of the
 * actual table.
 * <p>
 *
 * @author Tim Boudreau
 */
public interface RowModel {
    /** Get the column count.  Do not include the base (nodes) column
     * of the Outline, only the number of columns in addition to it
     * that should be displayed. 
     * @return the number of columns this model will contribute to the
     *  OutlineModel, not including the tree column */
    public int getColumnCount();
    /** Get the value at a given column.  
     * @param node The node in column 0 of the Outline
     * @param column The index of the column minus the nodes column  
     * @return the value that should be displayed in the specified column,
     *  given the node in the tree column */
    public Object getValueFor (Object node, int column);
    /** Get the object class for the column.  Analogous to 
     * <code>TableModel.getColumnClass(int column)</code> 
     * @param column an index into the columns represented by this model (0
     *  based - does not include the tree column of the OutlineModel)
     * @return the class of object that will be displayed in the specified
     * column */
    public Class getColumnClass (int column);
    /** Determine if the cell in this column is editable for the passed
     * node.
     * @param node the object displayed in the tree column of the Outline
     * @param column the column index into the columns defined by this
     *  RowModel  */
    public boolean isCellEditable (Object node, int column);
    /** Set the value of the object in this column.  Typically this may
     * call a setter on the node object in column 0.  */
    public void setValueFor (Object node, int column, Object value);
    /** Get a localized name of this column that can be displayed in
     * the table header
     * @param column the column a name is requested for
     * @return a localized name for the column  */
    public String getColumnName (int column);
}
