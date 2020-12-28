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

import java.util.*;

/**
 * Python variable DataNode to be used inside PythonVariableTreeDataModel
 * @author jean-yves Mengant
 *
 */
public class PythonVariableTreeDataNode {

  public final static String COMPOSITE = "COMPOSITE";
  public final static String LIST = "LIST";
  public final static String TUPLE = "TUPLE";
  public final static String MAP = "MAP";
  public final static String STRING = "String";
  public final static String UNICODE = "Unicode";
  private PythonVariableTreeDataNode _parent = null;
  private String _varName;
  private String _varContent;
  private String _varType;
  private boolean _composite = false;
  private boolean _list = false;
  private boolean _map = false;
  private boolean _string = false;
  private boolean _unicode = false;
  private PythonVariableTreeDataNode[] _children = null;

  public PythonVariableTreeDataNode[] get_children() {
    return _children;
  }

  public int get_childrenSize() {
    if (_children == null) {
      return 0;
    }
    return _children.length;
  }

  public boolean hasChildren() {
    if (_children != null) {
      return true;
    }
    return false;
  }

  public boolean is_list() {
    return _list;
  }

  public boolean is_map() {
    return _map;
  }

  public boolean is_unicode() {
    return _unicode;
  }

  public boolean is_string() {
    return _string;
  }

  /**
   * Returns true if the node is not a composite variable node
   */
  public boolean isLeaf() {
    return !_composite;
  }

  @Override
  public String toString() {
    return _varName;
  }

  /**
   * populate node children from Python's value hashtable
   * @param values
   */
  public void set_children(TreeMap values, TreeMap types) {
    Set memberList = values.keySet();
    Iterator it = memberList.iterator();
    int ii = 0;
    _children = null;
    if (values != null) {
      _children = new PythonVariableTreeDataNode[values.size()];
    }
    int decimals = Integer.toString(values.size()).length();
    while (it.hasNext()) {
      // in order to have correctly sorted numbers on list/maps items
      // padd with 0
      String curName = (String) it.next();
      String curValue = (String) values.get(curName);
      String curType = (String) types.get(curName);
      _children[ii] = new PythonVariableTreeDataNode(this, curName, curValue, curType);
      ii++;
    }

  }

  public String get_varName() {
    return _varName;
  }

  public PythonVariableTreeDataNode get_parent() {
    return _parent;
  }

  public PythonVariableTreeDataNode[] getPathToRoot(PythonVariableTreeDataNode aNode, int depth) {
    PythonVariableTreeDataNode[] retNodes;

    if (aNode == null) {
      if (depth == 0) {
        return null;
      }
      retNodes = new PythonVariableTreeDataNode[depth];
    } else {
      depth++;
      retNodes = getPathToRoot(aNode.get_parent(), depth);
      retNodes[retNodes.length - depth] = aNode;
    }
    return retNodes;
  }

  /**
   * check for python complex type variable patterns values 
   * assume starting with '<' and ending with '>' 
   *
   */
  private void checkVarType() {
    if (_parent == null) {
      _composite = true; // force _composite on root node
    }
    if (_varType.equals(COMPOSITE) ||
            _varType.equals(MAP) ||
            _varType.equals(TUPLE) ||
            _varType.equals(LIST)) {
      _composite = true;
    }
    if (_varType.equals(MAP)) {
      _map = true;
    }
    if (_varType.equals(LIST) || _varType.equals(TUPLE)) {
      _list = true;
    }
    if (_varType.equals(UNICODE) || _varType.equals(STRING)) {
      _string = true;
    }
    if (_varType.equals(UNICODE)) {
      _unicode = true;
    }

  }

  public PythonVariableTreeDataNode(PythonVariableTreeDataNode parent,
          String varName,
          String varContent,
          String varType) {
    _parent = parent;
    _varName = varName;
    _varContent = varContent;
    _varType = varType;
    checkVarType();
  }

  /**
   * Gets the path from the root to the receiver.
   */
  public PythonVariableTreeDataNode[] getPath() {
    return this.getPathToRoot(this, 0);
  }

  public String get_varContent() {
    return _varContent;
  }

  public void set_varContent(String varContent) {
    _varContent = varContent;
  }

  public String get_varType() {
    return _varType;
  }

  public String get_toStringContent() {
    // handle toString for misc composite cases
    StringBuffer returned = new StringBuffer();
    returned.append('\'');
    returned.append(get_varContent());
    returned.append('\'');
    return returned.toString();
  }

  public static void handlePythonTypes(StringBuffer buffer,
          PythonVariableTreeDataNode node,
          PythonVariableTreeDataNode parent) {
    if (parent.is_list() || parent.is_map()) {
      // list element case
      buffer.append('[');
      boolean hasQuote = false;
      if (parent.is_map()) {
        if (node.get_varName().indexOf('\'') != -1) {
          hasQuote = true;
        }
        if (hasQuote) {
          buffer.append('"');
        } else {
          buffer.append('\'');
        }
      }
      buffer.append(node.get_varName());
      if (parent.is_map()) {
        if (hasQuote) {
          buffer.append('"');
        } else {
          buffer.append('\'');
        }
      }
      buffer.append(']');
    } else {
      // standard case   
      if (buffer.length() > 0) {
        buffer.append('.');
      }
      buffer.append(node.get_varName());
    }
  }

  public static String buildPythonName(PythonVariableTreeDataNode nodes[]) {
    StringBuffer name = new StringBuffer();
    for (int ii = 1; ii < nodes.length; ii++) {
      handlePythonTypes(name, nodes[ii], nodes[ii - 1]);
    }
    return name.toString();
  }

  /**
   * factory to build a datanode out of a python hashtable variable list
   * name/value pair
   * @param parent parent node or null if root
   * @param nodeName current name of node 
   * @param nodeValue current value of Node
   * @param values dependance hashTable.
   * @return the built PythonVariableTreeDataNode 
   */
  public static PythonVariableTreeDataNode buildDataNodes(PythonVariableTreeDataNode parent,
          String nodeName,
          String nodeValue,
          String nodeType) {
    PythonVariableTreeDataNode returned = new PythonVariableTreeDataNode(parent, nodeName, nodeValue, nodeType);
    return returned;
  }
}
