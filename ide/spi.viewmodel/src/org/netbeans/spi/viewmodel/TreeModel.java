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

package org.netbeans.spi.viewmodel;



/**
 * Defines data model for tree.
 *
 * @author   Jan Jancura
 */
public interface TreeModel extends Model {

    /**
     * Constant for root node. This root node should be used if root node
     * does not represent any valuable information and should not be visible in
     * tree.
     */
    public static final String ROOT = "Root";

    /**
     * Returns the root node of the tree or null, if the tree is empty.
     *
     * @return the root node of the tree or null
     */
    public abstract Object getRoot ();
    
    /** 
     * Returns children for given parent on given indexes.<p>
     * This method works in pair with {@link #getChildrenCount}, the <code>to</code>
     * parameter is up to the value that is returned from {@link #getChildrenCount}.
     * If the list of children varies over time, the implementation code
     * needs to pay attention to bounds and check the <code>from</code> and
     * <code>to</code> parameters, especially if {@link #getChildrenCount}
     * returns <code>Integer.MAX_VALUE</code>. Caching of the children between
     * {@link #getChildrenCount} and {@link #getChildren} can be used as well,
     * if necessary.
     *
     * @param   parent a parent of returned nodes
     * @param   from a start index
     * @param   to a end index
     *
     * @throws  UnknownTypeException if this TreeModel implementation is not
     *          able to resolve children for given node type
     *
     * @return  children for given parent on given indexes
     * @see #getChildrenCount
     */
    public abstract Object[] getChildren (Object parent, int from, int to) 
        throws UnknownTypeException;
    
    /**
     * Returns true if node is leaf.
     * 
     * @throws  UnknownTypeException if this TreeModel implementation is not
     *          able to resolve children for given node type
     * @return  true if node is leaf
     */
    public abstract boolean isLeaf (Object node) throws UnknownTypeException;
    
    /**
     * Returns the number of children for given node.<p>
     * This method works in pair with {@link #getChildren}, which gets
     * this returned value (or less) as the <code>to</code> parameter. This method
     * is always called before a call to {@link #getChildren}. This method can
     * return e.g. <code>Integer.MAX_VALUE</code> when all children should be
     * loaded.
     * 
     * @param   node the parent node
     * @throws  UnknownTypeException if this TreeModel implementation is not
     *          able to resolve children for given node type
     *
     * @return  the children count
     * @since 1.1
     * @see #getChildren
     */
    public abstract int getChildrenCount (Object node) 
    throws UnknownTypeException;

    /** 
     * Registers given listener.
     * 
     * @param l the listener to add
     */
    public abstract void addModelListener (ModelListener l);

    /** 
     * Unregisters given listener.
     *
     * @param l the listener to remove
     */
    public abstract void removeModelListener (ModelListener l);
}
