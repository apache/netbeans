/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */package org.netbeans.modules.python.debugger.utils;

import javax.swing.tree.TreeModel;

/**
 * TreeTableModel is the model used by a JTreeTable. It extends TreeModel
 * to add methods for getting inforamtion about the set of columns each
 * node in the TreeTableModel may have. Each column, like a column in
 * a TableModel, has a name and a type associated with it. Each node in
 * the TreeTableModel can return a value for each of the columns and
 * set that value if isCellEditable() returns true.
 *
 */
public interface TreeTableModel extends TreeModel {

  /**
   * Returns the number ofs availible column.
   */
  public int getColumnCount();

  /**
   * Returns the name for column number <code>column</code>.
   */
  public String getColumnName(int column);

  /**
   * Returns the type for column number <code>column</code>.
   */
  public Class getColumnClass(int column);

  /**
   * Returns the value to be displayed for node <code>node</code>,
   * at column number <code>column</code>.
   */
  public Object getValueAt(Object node, int column);

  /**
   * Indicates whether the the value for node <code>node</code>,
   * at column number <code>column</code> is editable.
   */
  public boolean isCellEditable(Object node, int column);

  /**
   * Sets the value for node <code>node</code>,
   * at column number <code>column</code>.
   */
  public void setValueAt(Object aValue, Object node, int column);
}
