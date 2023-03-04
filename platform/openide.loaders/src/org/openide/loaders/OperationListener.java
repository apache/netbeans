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

package org.openide.loaders;

/** Listener to operations on data objects. Can be attached to
* the {@link DataLoaderPool} and will receive information about operations taken on all
* {@link DataObject}s.
*
* @author Jaroslav Tulach
*/
public interface OperationListener extends java.util.EventListener {
    /** Object has been recognized by
    * {@link DataLoaderPool#findDataObject}.
    * This allows listeners
    * to attach additional cookies, etc.
    *
    * @param ev event describing the action
    */
    public void operationPostCreate (OperationEvent ev);

    /** Object has been successfully copied.
    * @param ev event describing the action
    */
    public void operationCopy (OperationEvent.Copy ev);

    /** Object has been successfully moved.
    * @param ev event describing the action
    */
    public void operationMove (OperationEvent.Move ev);

    /** Object has been successfully deleted.
    * @param ev event describing the action
    */
    public void operationDelete (OperationEvent ev);

    /** Object has been successfully renamed.
    * @param ev event describing the action
    */
    public void operationRename (OperationEvent.Rename ev);

    /** A shadow of a data object has been created.
    * @param ev event describing the action
    */
    public void operationCreateShadow (OperationEvent.Copy ev);

    /** New instance of an object has been created.
    * @param ev event describing the action
    */
    public void operationCreateFromTemplate (OperationEvent.Copy ev);
}
