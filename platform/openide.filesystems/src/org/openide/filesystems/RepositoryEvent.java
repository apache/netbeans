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


/** Event describing adding a filesystem to, or removing a filesystem from, the filesystem pool.
*
* @author Jaroslav Tulach
* @version 0.10 November 4, 1997
*/
public class RepositoryEvent extends java.util.EventObject {
    /** generated Serialized Version UID */
    static final long serialVersionUID = 5466690014963965717L;

    /** the modifying filesystem */
    private FileSystem fileSystem;

    /** added or removed */
    private boolean add;

    /** Create a new filesystem pool event.
    * @param fsp filesystem pool that is being modified
    * @param fs filesystem that is either being added or removed
    * @param add <CODE>true</CODE> if the filesystem is added,
    *    <CODE>false</CODE> if removed
    */
    public RepositoryEvent(Repository fsp, FileSystem fs, boolean add) {
        super(fsp);
        this.fileSystem = fs;
        this.add = add;
    }

    /** Getter for the filesystem pool that is modified.
    * @return the filesystem pool
    */
    public Repository getRepository() {
        return (Repository) getSource();
    }

    /** Getter for the filesystem that is added or removed.
    * @return the filesystem
    */
    public FileSystem getFileSystem() {
        return fileSystem;
    }

    /** Is the filesystem added or removed?
    * @return <CODE>true</CODE> if the filesystem is added, <code>false</code> if removed
    */
    public boolean isAdded() {
        return add;
    }
}
