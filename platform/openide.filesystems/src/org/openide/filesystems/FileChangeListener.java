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

package org.openide.filesystems;

import java.util.EventListener;

/** Listener for changes in <code>FileObject</code>s. Can be attached to any <code>FileObject</code>.
* <P>
* When attached to a file it listens for file changes (due to saving from inside NetBeans) and
* for deletes and renames.
* <P>
* When attached to a folder it listens for all actions taken on this folder.
* These include any modifications of data files or folders,
* and creation of new data files or folders.
*
* @see FileObject#addFileChangeListener
*
* @author Jaroslav Tulach, Petr Hamernik
*/
public interface FileChangeListener extends EventListener {
    /** Fired when a new folder is created. This action can only be
     * listened to in folders containing the created folder up to the root of
     * filesystem.
      *
     * @param fe the event describing context where action has taken place
     */
    public abstract void fileFolderCreated(FileEvent fe);

    /** Fired when a new file is created. This action can only be
    * listened in folders containing the created file up to the root of
    * filesystem.
    *
    * @param fe the event describing context where action has taken place
    */
    public abstract void fileDataCreated(FileEvent fe);

    /** Fired when a file is changed.
    * @param fe the event describing context where action has taken place
    */
    public abstract void fileChanged(FileEvent fe);

    /** Fired when a file is deleted.
    * @param fe the event describing context where action has taken place
    */
    public abstract void fileDeleted(FileEvent fe);

    /** Fired when a file is renamed.
    * @param fe the event describing context where action has taken place
    *           and the original name and extension.
    */
    public abstract void fileRenamed(FileRenameEvent fe);

    /** Fired when a file attribute is changed.
    * @param fe the event describing context where action has taken place,
    *           the name of attribute and the old and new values.
    */
    public abstract void fileAttributeChanged(FileAttributeEvent fe);
}
