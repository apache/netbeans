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


package org.netbeans.modules.i18n;

import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;




/**
 * This class implements a filter node that can be used to display a
 * hierarchy of nodes filtered by a criteriea defined by
 * <code>NodeFilter</code>. We don't use NodeAcceptor because of
 * efficiency - node acceptor requires an array as a parameter,
 * which we don't want to create.
 */
public class FilteredNode extends FilterNode {

  private NodeFilter filter;
  private String newName = null;

  /**
   * Decides which nodes should be included in the hierarchy and which
   * not.
   */
  public interface NodeFilter {
    public boolean acceptNode(Node node) ;
  }

  public FilteredNode(Node original, NodeFilter filter ) {
      this(original, filter, null);
  }

    @Override
    public boolean canRename() {
        return false;
    }


  public FilteredNode(Node original, NodeFilter filter, String newName) {
    super(original, new FilteredChildren(original, filter));
    this.filter = filter;
    this.newName = newName;
  }

  public String getDisplayName() { 
    if (newName != null) return newName; else return super.getDisplayName();
  }
    
  public Node cloneNode() {
    return new FilteredNode(this.getOriginal().cloneNode(), this.filter);
  }

    


  /**
   * A mutualy recursive children that ensure propagation of the
   * filter to deeper levels of hiearachy. That is, it creates
   * FilteredNodes filtered by the same filter.
   */
  public static class FilteredChildren extends FilterNode.Children {
    private NodeFilter filter;

    public FilteredChildren(Node original, NodeFilter filter) {
      super(original);
      this.filter = filter;
    }

    protected Node copyNode(Node node) {
      return new FilteredNode(node, this.filter);
    }

    protected Node[] createNodes(Node key) {
      if (filter.acceptNode(key)) 
	return super.createNodes(key);
      else 
	return new Node [0];
    }

  }

}
