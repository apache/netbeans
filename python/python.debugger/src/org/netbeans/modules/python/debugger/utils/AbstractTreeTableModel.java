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
package org.netbeans.modules.python.debugger.utils;

import javax.swing.tree.*;
import javax.swing.event.*;

public abstract class AbstractTreeTableModel implements TreeTableModel {

  protected Object root;
  protected EventListenerList listenerList = new EventListenerList();

  public AbstractTreeTableModel(Object root) {
    this.root = root;
  }

  //
  // Default implmentations for methods in the TreeModel interface.
  //
  @Override
  public Object getRoot() {
    return root;
  }

  @Override
  public boolean isLeaf(Object node) {
    return getChildCount(node) == 0;
  }

  @Override
  public void valueForPathChanged(TreePath path, Object newValue) {
  }

  // This is not called in the JTree's default mode: use a naive implementation.
  @Override
  public int getIndexOfChild(Object parent, Object child) {
    for (int i = 0; i < getChildCount(parent); i++) {
      if (getChild(parent, i).equals(child)) {
        return i;
      }
    }
    return -1;
  }

  @Override
  public void addTreeModelListener(TreeModelListener l) {
    listenerList.add(TreeModelListener.class, l);
  }

  @Override
  public void removeTreeModelListener(TreeModelListener l) {
    listenerList.remove(TreeModelListener.class, l);
  }

  /*
   * Notify all listeners that have registered interest for
   * notification on this event type.  The event instance
   * is lazily created using the parameters passed into
   * the fire method.
   * @see EventListenerList
   */
  protected void fireTreeNodesChanged(Object source, Object[] path,
          int[] childIndices,
          Object[] children) {
    // Guaranteed to return a non-null array
    Object[] listeners = listenerList.getListenerList();
    TreeModelEvent e = null;
    // Process the listeners last to first, notifying
    // those that are interested in this event
    for (int i = listeners.length - 2; i >= 0; i -= 2) {
      if (listeners[i] == TreeModelListener.class) {
        // Lazily create the event:
        if (e == null) {
          e = new TreeModelEvent(source, path,
                  childIndices, children);
        }
        ((TreeModelListener) listeners[i + 1]).treeNodesChanged(e);
      }
    }
  }

  /*
   * Notify all listeners that have registered interest for
   * notification on this event type.  The event instance
   * is lazily created using the parameters passed into
   * the fire method.
   * @see EventListenerList
   */
  protected void fireTreeNodesInserted(Object source, Object[] path,
          int[] childIndices,
          Object[] children) {
    // Guaranteed to return a non-null array
    Object[] listeners = listenerList.getListenerList();
    TreeModelEvent e = null;
    // Process the listeners last to first, notifying
    // those that are interested in this event
    for (int i = listeners.length - 2; i >= 0; i -= 2) {
      if (listeners[i] == TreeModelListener.class) {
        // Lazily create the event:
        if (e == null) {
          e = new TreeModelEvent(source, path,
                  childIndices, children);
        }
        ((TreeModelListener) listeners[i + 1]).treeNodesInserted(e);
      }
    }
  }

  /*
   * Notify all listeners that have registered interest for
   * notification on this event type.  The event instance
   * is lazily created using the parameters passed into
   * the fire method.
   * @see EventListenerList
   */
  protected void fireTreeNodesRemoved(Object source, Object[] path,
          int[] childIndices,
          Object[] children) {
    // Guaranteed to return a non-null array
    Object[] listeners = listenerList.getListenerList();
    TreeModelEvent e = null;
    // Process the listeners last to first, notifying
    // those that are interested in this event
    for (int i = listeners.length - 2; i >= 0; i -= 2) {
      if (listeners[i] == TreeModelListener.class) {
        // Lazily create the event:
        if (e == null) {
          e = new TreeModelEvent(source, path,
                  childIndices, children);
        }
        ((TreeModelListener) listeners[i + 1]).treeNodesRemoved(e);
      }
    }
  }

  /*
   * Notify all listeners that have registered interest for
   * notification on this event type.  The event instance
   * is lazily created using the parameters passed into
   * the fire method.
   * @see EventListenerList
   */
  protected void fireTreeStructureChanged(Object source, Object[] path,
          int[] childIndices,
          Object[] children) {
    // Guaranteed to return a non-null array
    Object[] listeners = listenerList.getListenerList();
    TreeModelEvent e = null;
    // Process the listeners last to first, notifying
    // those that are interested in this event
    for (int i = listeners.length - 2; i >= 0; i -= 2) {
      if (listeners[i] == TreeModelListener.class) {
        // Lazily create the event:
        if (e == null) {
          e = new TreeModelEvent(source, path,
                  childIndices, children);
        }
        ((TreeModelListener) listeners[i + 1]).treeStructureChanged(e);
      }
    }
  }

  //
  // Default impelmentations for methods in the TreeTableModel interface.
  //
  @Override
  public Class getColumnClass(int column) {
    return Object.class;
  }

  /** By default, make the column with the Tree in it the only editable one.
   *  Making this column editable causes the JTable to forward mouse
   *  and keyboard events in the Tree column to the underlying JTree.
   */
  @Override
  public boolean isCellEditable(Object node, int column) {
    return getColumnClass(column) == TreeTableModel.class;
  }

  @Override
  public void setValueAt(Object aValue, Object node, int column) {
  }
  // Left to be implemented in the subclass:

  /*
   *   public Object getChild(Object parent, int index)
   *   public int getChildCount(Object parent)
   *   public int getColumnCount()
   *   public String getColumnName(Object node, int column)
   *   public Object getValueAt(Object node, int column)
   */
}
