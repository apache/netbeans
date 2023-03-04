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


/** Adapter for changes in <code>FileObject</code>s. Can be attached to any {@link FileObject}.
*
* @see FileChangeListener
*
* @author Jaroslav Tulach, Petr Hamernik
* @version 0.15 December 14, 1997
*/
public class FileChangeAdapter extends Object implements FileChangeListener {
    /** Fired when a new folder is created. This action can only be
    * listened to in folders containing the created folder up to the root of
    * filesystem.
    *
    * @param fe the event describing context where action has taken place
    */
    public void fileFolderCreated(FileEvent fe) {
    }

    /** Fired when a new file is created. This action can only be
    * listened in folders containing the created file up to the root of
    * filesystem.
    *
    * @param fe the event describing context where action has taken place
    */
    public void fileDataCreated(FileEvent fe) {
    }

    /** Fired when a file is changed.
    * @param fe the event describing context where action has taken place
    */
    public void fileChanged(FileEvent fe) {
    }

    /** Fired when a file is deleted.
    * @param fe the event describing context where action has taken place
    */
    public void fileDeleted(FileEvent fe) {
    }

    /** Fired when a file is renamed.
    * @param fe the event describing context where action has taken place
    *           and the original name and extension.
    */
    public void fileRenamed(FileRenameEvent fe) {
    }

    /** Fired when a file attribute is changed.
    * @param fe the event describing context where action has taken place,
    *           the name of attribute and the old and new values.
    */
    public void fileAttributeChanged(FileAttributeEvent fe) {
    }
}
