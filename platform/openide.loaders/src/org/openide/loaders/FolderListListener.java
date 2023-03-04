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

/** Listener that watches progress of recognizing objects
* in a folder. The listener may even influence the data object recognition
* and, in such a way, act as a filter.
*
* <p>Normally the methods of this class are called in the process of a task
* to collect the data objects within a folder, e.g. in
* {@link FolderList#computeChildrenList(FolderListListener)}. In such a task
* implementations of {@link #process(DataObject, java.util.List)} may act as
* filters by not added the data object to the result list. Implementations
* of {@link #finished(java.util.List)} may be used to inform the caller about
* the result of the task and for further processing of the result. E.g.
* {@link FolderList#computeChildrenList(FolderListListener)} has as its return
* value the task to compute the list and not the computed children. An
* implementation of {@link #finished(java.util.List)} may be used by the caller
* of {@link FolderList#computeChildrenList(FolderListListener)} to get informed
* about the result of children computation.</p>
*
* @author Jaroslav Tulach
*/
interface FolderListListener {
    /** Another object has been recognized.
    * @param obj the object recognized
    * @param arr array where the implementation should add the 
    *    object
    */
    public void process (DataObject obj, java.util.List<DataObject> arr);

    /** All objects has been recognized.
    * @param arr list of DataObjects
    */
    public void finished (java.util.List<DataObject> arr);
}
