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
 */
package org.netbeans.modules.python.debugger.gui;

import org.netbeans.modules.python.debugger.utils.AbstractTreeTableModel;
import org.netbeans.modules.python.debugger.utils.TreeTableModel;

public class PythonVariableTreeTableModel
        extends AbstractTreeTableModel {

  // Types of the columns.
  static protected Class[] cTypes = {TreeTableModel.class, Object.class};
  private String[] _columnNames = null;
  private PythonContainer _parent = null;
  /** true when table conatins global variables references */
  private boolean _global = false;

  public PythonVariableTreeTableModel(Object root,
          String columnNames[],
          boolean global) {
    // initiate root Node
    super(root);
    _columnNames = columnNames;
    _global = global;
  }

  public void set_parent(PythonContainer parent) {
    _parent = parent;
  }

  //
  // Some convenience methods.
  //
  protected PythonVariableTreeDataNode getDataNode(Object node) {
    return ((PythonVariableTreeDataNode) node);
  }

  protected Object[] getChildren(Object node) {
    PythonVariableTreeDataNode datanode = ((PythonVariableTreeDataNode) node);
    return datanode.get_children();
  }

  //
  // The TreeModel interface
  //
  @Override
  public int getChildCount(Object node) {
    Object[] children = getChildren(node);
    return (children == null) ? 0 : children.length;
  }

  @Override
  public boolean isCellEditable(Object node, int column) {
    if (getColumnClass(column) == TreeTableModel.class) {
      return true;
    }
    if (node instanceof PythonVariableTreeDataNode) {
      PythonVariableTreeDataNode cur = (PythonVariableTreeDataNode) node;
      if (cur.isLeaf()) {
        return true;
      }
    }
    return false;
  }

  @Override
  public Object getChild(Object node, int i) {

    return getChildren(node)[i];
  }

  // The superclass's implementation would work, but this is more efficient.
  @Override
  public boolean isLeaf(Object node) {
    PythonVariableTreeDataNode cur = (PythonVariableTreeDataNode) node;
    return cur.isLeaf();
  }

  //
  //  The TreeTableNode interface.
  //
  @Override
  public int getColumnCount() {
    return _columnNames.length;
  }

  @Override
  public String getColumnName(int column) {
    return _columnNames[column];
  }

  @Override
  public Class getColumnClass(int column) {
    return cTypes[column];
  }

  /**
   * Can be invoked when a node has changed, will create the
   * appropriate event.
   */
  protected void nodeChanged(PythonVariableTreeDataNode candidate) {
    PythonVariableTreeDataNode parent = candidate.get_parent();
    if (parent != null) {
      PythonVariableTreeDataNode[] path = parent.getPath();
      int[] index = {getIndexOfChild(parent, candidate)};
      Object[] children = {candidate};

      fireTreeNodesChanged(PythonVariableTreeTableModel.this,
              path,
              index,
              children);
    }
  }

  protected void nodeStructureChange(PythonVariableTreeDataNode candidate) {
    fireTreeStructureChanged(this, candidate.getPath(), null, null);
  }

  @Override
  public Object getValueAt(Object node, int column) {
    PythonVariableTreeDataNode dataNode = getDataNode(node);

    switch (column) {
      case 0:
        return dataNode;
      case 1:
        // populate Variable content value
        return dataNode.get_varContent();
    }
    return null;
  }

  @Override
  public void setValueAt(Object newValue, Object node, int column) {
    if (column == 0) // tree
    {
      super.setValueAt(newValue, node, column);
    } else {
      PythonVariableTreeDataNode dataNode = getDataNode(node);
      // Complex names may need to get built from tree path
      PythonVariableTreeDataNode path[] = dataNode.getPath();
      String varName = PythonVariableTreeDataNode.buildPythonName(path);
      dataNode.set_varContent((String) newValue);
      // populate newDataValue to python side
      if (_parent != null) {
        _parent.dbgVariableChanged(varName, (String) newValue, _global);
      }

    }
  }
}
