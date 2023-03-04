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

import javax.swing.Action;


/**
 * Filters actions provided by some original {@link NodeActionsProvider}.
 * It can be used to add some new actions to nodes pop-up menu, remove
 * some actions or redefine behaviour of some actions.
 *
 * @author   Jan Jancura
 */
public interface NodeActionsProviderFilter extends Model {


    /**
     * Performs default action for given node. You should not throw
     * UnknownTypeException directly from this method!
     *
     * @throws  UnknownTypeException this exception can be thrown from 
     *          <code>original.performDefaultAction (...)</code> method call only!
     */
    public abstract void performDefaultAction (
        NodeActionsProvider original,
        Object node
    ) throws UnknownTypeException;
    
    /**
     * Returns set of actions for given node. You should not throw UnknownTypeException
     * directly from this method!
     *
     * @throws  UnknownTypeException this exception can be thrown from 
     *          <code>original.getActions (...)</code> method call only!
     * @return  set of actions for given node
     */
    public abstract Action[] getActions (
         NodeActionsProvider original,
         Object node
    ) throws UnknownTypeException;

    /** 
     * Registers given listener.
     * 
     * @param l the listener to add
     */
//    public abstract void addModelListener (ModelListener l);

    /** 
     * Unregisters given listener.
     *
     * @param l the listener to remove
     */
//    public abstract void removeModelListener (ModelListener l);
}
