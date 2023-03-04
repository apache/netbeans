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

import javax.swing.table.TableModel;
import javax.swing.tree.AbstractLayoutCache;
import javax.swing.tree.TreeModel;

/** A model for an Outline (&quot;tree-table&quot;).  Implements both
 * TreeModel and TableModel (the default implementation, DefaultOutlineModel,
 * wraps a supplied TreeModel and TableModel).  It is vastly easier to
 * use <code>DefaultOutlineModel</code> than to implement this interface
 * directly.
 *
 * @author  Tim Boudreau  */
public interface OutlineModel extends TableModel, TreeModel {
    /** Get the <code>TreePathSupport</code> object this model uses to manage
     * information about expanded nodes.  <code>TreePathSupport</code> implements
     * logic for tracking expanded nodes, manages <code>TreeWillExpandListener</code>s,
     * and is a repository for preserving expanded state information about nodes whose parents
     * are currently collapsed.  JTree implements very similar logic internally
     * to itself.
     * <p>
     * <i>(PENDING) It is not yet determined if TreePathSupport will remain a
     * public class.</i>
     */
    public TreePathSupport getTreePathSupport ();
    /** Get the layout cache which is used to track the visual state of nodes.
     * This is typically one of the standard JDK layout cache classes, such
     * as <code>VariableHeightLayoutCache</code> or <code>
     * FixedHeightLayoutCache</code>.  */
    public AbstractLayoutCache getLayout ();
    /** Determine if the model is a large-model.  Large model trees keep less
     * internal state information, relying on the TreeModel more.  Essentially
     * they trade performance for scalability. An OutlineModel may be large
     * model or small model; primarily this affects the type of layout cache
     * used, just as it does with JTree.  */
    public boolean isLargeModel();
}
