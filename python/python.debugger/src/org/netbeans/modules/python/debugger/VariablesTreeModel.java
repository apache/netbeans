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

import java.util.Vector;
import org.netbeans.modules.python.debugger.backend.DebuggerContextChangeListener;
import org.netbeans.modules.python.debugger.gui.PythonDebugContainer;
import org.netbeans.modules.python.debugger.gui.PythonVariableTreeDataNode;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.spi.debugger.ui.Constants;
import org.netbeans.spi.viewmodel.ColumnModel;
import org.netbeans.spi.viewmodel.ModelEvent;
import org.netbeans.spi.viewmodel.ModelListener;
import org.netbeans.spi.viewmodel.NodeModel;
import org.netbeans.spi.viewmodel.TableModel;
import org.netbeans.spi.viewmodel.TreeModel;
import org.netbeans.spi.viewmodel.UnknownTypeException;

/**
 * Local / Global Python variable View implementation 
 */
public class VariablesTreeModel
        implements TreeModel,
        NodeModel,
        TableModel,
        DebuggerContextChangeListener {

  private static final String _LOCAL_ =
          "org/netbeans/modules/python/debugger/resources/Field";
  private static final String _COMPOSITEVAR_ICON_ =
          "org/netbeans/modules/python/debugger/resources/LocalVariable";
  private static final String _MODULE_ICON_ =
          "org/netbeans/modules/python/debugger/resources/module";
  private static final String _CLASS_ICON_ =
          "org/netbeans/modules/python/debugger/resources/class";
  private static final String _METHOD_ICON_ =
          "org/netbeans/modules/python/debugger/resources/method";
  private static final String _LIST_ICON_ =
          "org/netbeans/modules/python/debugger/resources/pylist";
  private static final String _MAP_ICON_ =
          "org/netbeans/modules/python/debugger/resources/pymap";
  private final static String _FUNCTION_ = "<function";
  private final static String _UNBOUND_ = "<unbound method";
  private final static String _BOUND_ = "<bound method";
  private final static String _METHOD_ = "<method";
  private final static String _METHOD_WRAPPER_ = "<method-wrapper";
  private final static String _MODULE_ = "<module";
  private final static String _BUILTIN_ = "<built-in";
  private final static String _CLASS_ = "<class";
  private final static String _MAP_ = "MAP";
  private final static String _LIST_ = "LIST";
  private PythonDebugger _debugger;
  private ContextProvider _lookupProvider;
  private Vector _listeners = new Vector();

  /**
   * Creates a new instance of VariablesTreeModel
   */
  public VariablesTreeModel(ContextProvider lookupProvider) {
    _debugger = (PythonDebugger) lookupProvider.lookupFirst(null, PythonDebugger.class);
    _lookupProvider = lookupProvider;
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
    _debugger.addVarListChangeListener(this);
  }

  /**
   * Unregisters given listener.
   *
   * @param l the listener to remove
   */
  @Override
  public void removeModelListener(ModelListener l) {
    _listeners.remove(l);
    _debugger.removeVarListChangeListener(this);
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
   * @param   node the parent node
   * @throws  NoInformationException if the set of children can not be
   *          resolved
   * @throws  ComputingException if the children resolving process
   *          is time consuming, and will be performed off-line
   * @throws  UnknownTypeException if this TreeModel implementation is not
   *          able to resolve children for given node type
   *
   * @return  true if node is leaf
   * @since 1.1
   */
  @Override
  public int getChildrenCount(Object node) throws UnknownTypeException {
    if (node == ROOT) {
      return _debugger.getVariablesCount(PythonDebugContainer.ROOTNODE);
    }
    if (node instanceof PythonVariableTreeDataNode) {
      PythonVariableTreeDataNode curNode = (PythonVariableTreeDataNode) node;
      return _debugger.getVariablesCount(curNode);
    }
    throw new UnknownTypeException(node);
  }

  /**
   * Returns true if node is leaf.
   *
   * @throws  UnknownTypeException if this TreeModel implementation is not
   *          able to resolve dchildren for given node type
   * @return  true if node is leaf
   */
  @Override
  public boolean isLeaf(Object node) throws UnknownTypeException {
    if (node == ROOT) {
      return false;
    }
    if (node instanceof PythonVariableTreeDataNode) {
      PythonVariableTreeDataNode curNode = (PythonVariableTreeDataNode) node;
      return curNode.isLeaf();
    }
    throw new UnknownTypeException(node);
  }

  /** 
   * Returns children for given parent on given indexes.
   *
   * @param   parent a parent of returned nodes
   * @param   from a start index
   * @param   to a end index
   *
   * @throws  NoInformationException if the set of children can not be
   *          resolved
   * @throws  ComputingException if the children resolving process
   *          is time consuming, and will be performed off-line
   * @throws  UnknownTypeException if this TreeModel implementation is not
   *          able to resolve children for given node type
   *
   * @return  children for given parent on given indexes
   */
  @Override
  public Object[] getChildren(Object parent, int from, int to)
          throws UnknownTypeException {
    if (parent == ROOT) {
      return _debugger.getVariables(PythonDebugContainer.ROOTNODE);
    } else if (parent instanceof PythonVariableTreeDataNode) {
      PythonVariableTreeDataNode curNode = (PythonVariableTreeDataNode) parent;
      return _debugger.getVariables(curNode);
    }
    throw new UnknownTypeException(parent);
  }

  /** 
   * Returns the root node of the tree or null, if the tree is empty.
   *
   * @return the root node of the tree or null
   */
  @Override
  public Object getRoot() {
    return ROOT;
  }

  /**
   * Returns tooltip for given node.
   *
   * @throws  ComputingException if the tooltip resolving process
   *          is time consuming, and the value will be updated later
   * @throws  UnknownTypeException if this NodeModel implementation is not
   *          able to resolve tooltip for given node type
   * @return  tooltip for given node
   */
  @Override
  public String getShortDescription(Object node)
          throws UnknownTypeException {
    if (node instanceof String) {
      return null;
    }
    if (node instanceof PythonVariableTreeDataNode) {
      return null;
    }
    throw new UnknownTypeException(node);
  }

  private boolean isMethodOrFunction(String curVal) {
    if (curVal.startsWith(_UNBOUND_) ||
            curVal.startsWith(_BOUND_) ||
            curVal.startsWith(_METHOD_) ||
            curVal.startsWith(_METHOD_WRAPPER_) ||
            curVal.startsWith(_FUNCTION_) ||
            curVal.startsWith(_BUILTIN_)) {
      return true;
    }
    return false;
  }

  private boolean isModule(String curVal) {
    if (curVal.startsWith(_MODULE_)) {
      return true;
    }
    return false;
  }

  private boolean isClass(String curVal) {
    if (curVal.startsWith(_CLASS_)) {
      return true;
    }
    return false;
  }

  private boolean isList(String curType) {
    if (curType.equals(_LIST_)) {
      return true;
    }
    return false;
  }

  private boolean isMap(String curType) {
    if (curType.equals(_MAP_)) {
      return true;
    }
    return false;
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
  public String getIconBase(Object node) throws UnknownTypeException {
    if (node == ROOT) {
      return null;
    }
    if (node instanceof PythonVariableTreeDataNode) {
      PythonVariableTreeDataNode curNode = (PythonVariableTreeDataNode) node;
      String curVal = curNode.get_varContent();
      if (isModule(curVal)) {
        return _MODULE_ICON_;
      } else if (isMethodOrFunction(curVal)) {
        return _METHOD_ICON_;
      } else if (isClass(curVal)) {
        return _CLASS_ICON_;
      } else if (isList(curNode.get_varType())) {
        return _LIST_ICON_;
      } else if (isMap(curNode.get_varType())) {
        return _MAP_ICON_;
      } else {
        if (curNode.isLeaf()) {
          return _LOCAL_;
        } else {
          return _COMPOSITEVAR_ICON_;
        }
      }
    }
    throw new UnknownTypeException(node);
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
  public String getDisplayName(Object node) throws UnknownTypeException {
    if (node == ROOT) {
      return ROOT.toString();
    }
    if (node instanceof PythonVariableTreeDataNode) {
      PythonVariableTreeDataNode vNode = (PythonVariableTreeDataNode) node;
      return vNode.get_varName();
    }
    throw new UnknownTypeException(node);
  }

  /**
   * Changes a value displayed in column <code>columnID</code>
   * and row <code>node</code>. Column ID is defined in by
   * {@link ColumnModel#getID}, and rows are defined by values returned from
   * {@link TreeModel#getChildren}.
   *
   * @param node a object returned from {@link TreeModel#getChildren} for this row
   * @param columnID a id of column defined by {@link ColumnModel#getID}
   * @param value a new value of variable on given position
   * @throws UnknownTypeException if there is no TableModel defined for given
   *         parameter type
   */
  @Override
  public void setValueAt(Object node, String columnID, Object value)
          throws UnknownTypeException {
    if ((node instanceof PythonVariableTreeDataNode) &&
            (columnID.equals(Constants.LOCALS_VALUE_COLUMN_ID))) {
      System.out.println("updating python values");
    } else {
      throw new UnknownTypeException(node);
    }
  }

  /**
   * Returns true if value displayed in column <code>columnID</code>
   * and row <code>node</code> is read only. Column ID is defined in by 
   * {@link ColumnModel#getID}, and rows are defined by values returned from 
   * {@link TreeModel#getChildren}.
   *
   * @param node a object returned from {@link TreeModel#getChildren} for this row
   * @param columnID a id of column defined by {@link ColumnModel#getID}
   * @throws UnknownTypeException if there is no TableModel defined for given
   *         parameter type
   *
   * @return true if variable on given position is read only
   */
  @Override
  public boolean isReadOnly(Object node, String columnID) throws
          UnknownTypeException {
    if (node == ROOT) {
      return true;
    }
    if ((node instanceof PythonVariableTreeDataNode) &&
            (columnID.equals(Constants.LOCALS_VALUE_COLUMN_ID))) {
      return false;
    }
    if ((node instanceof PythonVariableTreeDataNode) &&
            (columnID.equals(Constants.LOCALS_TYPE_COLUMN_ID))) {
      return true;
    }
    throw new UnknownTypeException(node);
  }

  /**
   * Returns value to be displayed in column <code>columnID</code>
   * and row identified by <code>node</code>. Column ID is defined in by 
   * {@link ColumnModel#getID}, and rows are defined by values returned from 
   * {@link org.netbeans.spi.viewmodel.TreeModel#getChildren}.
   *
   * @param node a object returned from 
   *         {@link org.netbeans.spi.viewmodel.TreeModel#getChildren} for this row
   * @param columnID a id of column defined by {@link ColumnModel#getID}
   * @throws ComputingException if the value is not known yet and will 
   *         be computed later
   * @throws UnknownTypeException if there is no TableModel defined for given
   *         parameter type
   *
   * @return value of variable representing given position in tree table.
   */
  @Override
  public Object getValueAt(Object node, String columnID) throws
          UnknownTypeException {
    if (node == ROOT) {
      return null;
    }
    if ((node instanceof PythonVariableTreeDataNode) &&
            (columnID.equals(Constants.LOCALS_VALUE_COLUMN_ID))) {
      PythonVariableTreeDataNode var = (PythonVariableTreeDataNode) node;
      return var.get_varContent();
    }
    if ((node instanceof PythonVariableTreeDataNode) &&
            (columnID.equals(Constants.LOCALS_TYPE_COLUMN_ID))) {
      PythonVariableTreeDataNode var = (PythonVariableTreeDataNode) node;
      return var.get_varType();
    }
    if ((node instanceof PythonVariableTreeDataNode) &&
            (columnID.equals(Constants.LOCALS_TO_STRING_COLUMN_ID ))) {
      PythonVariableTreeDataNode var = (PythonVariableTreeDataNode) node;
      return var.get_toStringContent();
    }
    throw new UnknownTypeException(node);
  }
}
