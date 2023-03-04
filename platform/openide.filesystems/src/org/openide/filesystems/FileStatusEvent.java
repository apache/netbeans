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

import java.util.Collections;
import java.util.EventObject;
import java.util.Set;

/** Event describing a change in annotation of files.
*
* @author Jaroslav Tulach
*/
public final class FileStatusEvent extends EventObject {
    static final long serialVersionUID = -6428208118782405291L;

    /** changed files */
    private Set<? extends FileObject> files;

    /** icon changed? */
    private boolean icon;

    /** name changed? */
    private boolean name;

    /** Creates new FileStatusEvent
    * @param fs filesystem that causes the event
    * @param files set of FileObjects that has been changed
    * @param icon has icon changed?
    * @param name has name changed?
    */
    public FileStatusEvent(FileSystem fs, Set<? extends FileObject> files, boolean icon, boolean name) {
        super(fs);
        this.files = files;
        this.icon = icon;
        this.name = name;
    }

    /** Creates new FileStatusEvent
    * @param fs filesystem that causes the event
    * @param file file object that has been changed
    * @param icon has icon changed?
    * @param name has name changed?
    */
    public FileStatusEvent(FileSystem fs, FileObject file, boolean icon, boolean name) {
        this(fs, Collections.singleton(file), icon, name);
    }

    /** Creates new FileStatusEvent. This does not specify the
    * file that changed annotation, assuming that everyone should update
    * its annotation. Please notice that this can be time consuming
    * and should be fired only when really necessary.
    *
    * @param fs filesystem that causes the event
    * @param icon has icon changed?
    * @param name has name changed?
    */
    public FileStatusEvent(FileSystem fs, boolean icon, boolean name) {
        this(fs, (Set<FileObject>) null, icon, name);
    }

    /** Getter for filesystem that caused the change.
    * @return filesystem
    */
    public FileSystem getFileSystem() {
        return (FileSystem) getSource();
    }

    /** Is the change change of name?
    */
    public boolean isNameChange() {
        return name;
    }

    /** Do the files changed their icons?
    */
    public boolean isIconChange() {
        return icon;
    }

    /** Check whether the given file has been changed.
    * @param file file to check
    * @return true if the file has been affected by the change
    */
    public boolean hasChanged(FileObject file) {
        if (files == null) {
            // all files on source filesystem are said to change
            try {
                return file.getFileSystem() == getSource();
            } catch (FileStateInvalidException ex) {
                // invalid files should not be changed
                return false;
            }
        } else {
            // specified set of files, so check it
            return files.contains(file);
        }
    }
}
