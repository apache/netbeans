/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
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
