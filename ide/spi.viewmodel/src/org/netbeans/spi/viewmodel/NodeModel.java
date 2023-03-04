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
 * Provides display name, icon and tool tip value for some type of objects.
 * Designed to be used with {@link TreeModel}.
 *
 * @author   Jan Jancura
 */
public interface NodeModel extends Model {

    /**
     * Returns display name for given node.
     *
     * @return  display name for given node
     */
    public abstract String getDisplayName (Object node)
    throws UnknownTypeException;

    /**
     * Returns icon for given node.
     *
     * @throws  UnknownTypeException if this NodeModel implementation is not
     *          able to resolve icon for given node type
     * @return  icon for given node
     */
    public abstract String getIconBase (Object node) 
    throws UnknownTypeException;
    
    /**
     * Returns tool tip for given node.
     *
     * @throws  UnknownTypeException if this NodeModel implementation is not
     *          able to resolve tool tip for given node type
     * @return  tool tip for given node
     */
    public abstract String getShortDescription (Object node) 
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
