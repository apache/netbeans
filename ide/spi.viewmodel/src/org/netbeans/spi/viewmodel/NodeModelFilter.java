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
 * Filters content of some existing {@link NodeModel}. You can change display
 * name, tooltip, or icon for some existing object here.
 *
 * @author   Jan Jancura
 */
public interface NodeModelFilter extends Model {

    /**
     * Returns filterred display name for given node. You should not
     * throw UnknownTypeException directly from this method!
     *
     * @throws  UnknownTypeException this exception can be thrown from
     *          <code>original.getDisplayName (...)</code> method call only!
     * @return  display name for given node
     */
    public abstract String getDisplayName (NodeModel original, Object node) 
    throws UnknownTypeException;
    
    /**
     * Returns filterred icon for given node. You should not throw 
     * UnknownTypeException directly from this method!
     *
     * @throws  UnknownTypeException this exception can be thrown from 
     *          <code>original.getIconBase (...)</code> method call only!
     * @return  icon for given node
     */
    public abstract String getIconBase (NodeModel original, Object node) 
    throws UnknownTypeException;
    
    /**
     * Returns filterred tooltip for given node. You should not throw 
     * UnknownTypeException directly from this method!
     *
     * @throws  UnknownTypeException this exception can be thrown from 
     *          <code>original.getShortDescription (...)</code> method call only!
     * @return  tooltip for given node
     */
    public abstract String getShortDescription (NodeModel original, Object node) 
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
