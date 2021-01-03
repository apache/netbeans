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
package org.netbeans.modules.python.debugger;

import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.spi.viewmodel.ModelListener;
import org.netbeans.spi.viewmodel.TreeModel;
import org.netbeans.spi.viewmodel.UnknownTypeException;

import java.util.Vector;
import org.netbeans.modules.python.debugger.backend.DebuggerContextChangeListener;
import org.netbeans.modules.python.debugger.backend.PythonThreadInfos;
import org.netbeans.spi.debugger.ui.Constants;
import org.netbeans.spi.viewmodel.ModelEvent;
import org.netbeans.spi.viewmodel.NodeModel;
import org.netbeans.spi.viewmodel.TableModel;

/**
 * Manage Python threading instances
 *
 */
public class ThreadsTreeModel
        implements TreeModel,
        TableModel,
        NodeModel,
        DebuggerContextChangeListener {

  private static final String _RUNNING_THREAD_ =
          "org/netbeans/modules/python/debugger/resources/RunningThread";
  private static final String _CURRENT_THREAD_ =
          "org/netbeans/modules/python/debugger/resources/CurrentThread";
  private static final String _SHORT_DESCRIPTION_ = "Python thread name";
  private static final String _CURRENT_ = "Current";
  private static final String _RUNNING_ = "Running";
  private PythonDebugger _debugger;
  private ContextProvider _lookupProvider;
  private Vector _listeners = new Vector();

  /**
   * Creates a new instance of ThreadsTreeModel
   */
  public ThreadsTreeModel(ContextProvider lookupProvider) {
    _debugger = (PythonDebugger) lookupProvider.lookupFirst(null, PythonDebugger.class);
    _lookupProvider = lookupProvider;
  }

  /**
   * Returns the translated root node of the tree or null, if the tree is empty.
   *
   * @return the translated root node of the tree or null
   */
  @Override
  public Object getRoot() {
    return ROOT;
  }

  /**
   * Registers given listener.
   *
   * @param l the listener to add
   */
  @Override
  public void addModelListener(ModelListener l) {
    _listeners.add(l);
    // provide a way to get called back by Python debugger
    _debugger.addThreadListChangeListener(this);
  }

  /**
   * Unregisters given listener.
   *
   * @param l the listener to remove
   */
  @Override
  public void removeModelListener(ModelListener l) {
    _listeners.remove(l);
    // provide a way to get called back by Python debugger
    _debugger.removeThreadListChangeListener(this);
  }

  @Override
  public void fireContextChanged() {
    Object[] ls;
    synchronized (_listeners) {
      ls = _listeners.toArray();
    }
    ModelEvent ev = new ModelEvent.TreeChanged(this);
    for (Object l : ls) {
      ((ModelListener) l).modelChanged(ev);
    }
  }

  /**
   * Returns number of children for given node.
   *
   * @param  node the parent node
   *
   * @return 0 if node is leaf or number of threads from debugger instance
   *
   * @throws UnknownTypeException if this TreeModel implementation is not able
   *                              to resolve children for given node type
   */
  @Override
  public int getChildrenCount(Object node) throws UnknownTypeException {
    if (node.equals(ROOT)) {
      return _debugger.getThreadCount();
    }

    return 0;
  }

  /**
   * Returns true if node is leaf.
   *
   * @return true if node is leaf
   *
   * @throws UnknownTypeException if this TreeModel implementation is not able
   *                              to resolve dchildren for given node type
   */
  @Override
  public boolean isLeaf(Object node) throws UnknownTypeException {
    if (node instanceof PythonThreadInfos) {
      return true;
    }
    if (node == ROOT) {
      return false;
    }
    throw new UnknownTypeException(node);
  }

  /**
   * Returns translated children for given parent on given indexes.
   *
   * @param  parent a parent of returned nodes
   *
   * @return translated children for given parent on given indexes
   *
   * @throws NoInformationException if the set of children can not be resolved
   * @throws ComputingException     if the children resolving process is time
   *                                consuming, and will be performed off-line
   * @throws UnknownTypeException   if this TreeModel implementation is not able
   *                                to resolve dchildren for given node type
   */
  @Override
  public Object[] getChildren(Object parent, int from, int to)
          throws UnknownTypeException {
    if (parent.equals(ROOT)) {
      return _debugger.getThreads();
    }

    return null;
  }

  /** unused */
  @Override
  public void setValueAt(Object node, String ColumnID, Object value) {
  }

  @Override
  public boolean isReadOnly(Object node, String columnID) {
    return true;
  }

  @Override
  public Object getValueAt(Object node, String columnID) {
    if (node == ROOT) {
      return null;
    }
    if (columnID.equals( Constants.THREAD_STATE_COLUMN_ID) ) {
      if (node instanceof PythonThreadInfos) {
        PythonThreadInfos curThread = (PythonThreadInfos) node;
        if (curThread.isCurrent()) {
          return _CURRENT_;
        } else {
          return _RUNNING_;
        }
      }
    }
    if (columnID.equals(Constants.THREAD_SUSPENDED_COLUMN_ID ) ){
      if (node instanceof PythonThreadInfos) {
        PythonThreadInfos curThread = (PythonThreadInfos) node;
        return curThread.isSuspended() ;
      }
    }
    return ("");
  }

  @Override
  public String getShortDescription(Object node) {
    return _SHORT_DESCRIPTION_;
  }

  /**
   * Returns display name for given node.
   *
   * @throws  ComputingException if the display name resolving process 
   *          is time consuming, and the value will be updated later
   * @throws  UnknownTypeException if this NodeModel implementation is not
   *          able to resolve display name for given node type
   * @return  display name for given node
   */
  @Override
  public String getDisplayName(Object node)
          throws UnknownTypeException {
    if (node == ROOT) {
      return ROOT.toString();
    }
    if (node instanceof PythonThreadInfos) {
      PythonThreadInfos curThread = (PythonThreadInfos) node;
      return curThread.get_name();
    }
    throw new UnknownTypeException(node);
  }

  /**
   * Returns icon for given node.
   *
   * @throws  ComputingException if the icon resolving process 
   *          is time consuming, and the value will be updated later
   * @throws  UnknownTypeException if this NodeModel implementation is not
   *          able to resolve icon for given node type
   * @return  icon for given node
   */
  @Override
  public String getIconBase(Object node)
          throws UnknownTypeException {
    if (node == ROOT) {
      return null;
    }
    if (node instanceof PythonThreadInfos) {
      PythonThreadInfos curThread = (PythonThreadInfos) node;
      if (curThread.isCurrent()) {
        return _CURRENT_THREAD_;
      }
      return _RUNNING_THREAD_;
    }
    throw new UnknownTypeException(node);
  }
}
