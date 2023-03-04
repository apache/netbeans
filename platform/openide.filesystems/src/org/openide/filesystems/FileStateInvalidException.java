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


/** Signals that the file object is somehow corrupted.
* The required operation is not possible due to a previous deletion, or
* an unexpected (external) change in the filesystem.
*
* @author Jaroslav Tulach
* @version 0.10 October 7, 1997
*/
public class FileStateInvalidException extends java.io.IOException {
    /** generated Serialized Version UID */
    static final long serialVersionUID = -4987532595879330362L;

    /** The name of the filesystem containing the bad FileObject */
    private String fileSystemName;

    /** Create new <code>FileStateInvalidException</code>.
    */
    public FileStateInvalidException() {
        super();
    }

    /** Create new <code>FileStateInvalidException</code> with the specified text.
    * @param s the text describing the exception
    */
    public FileStateInvalidException(String s) {
        super(s);
    }

    /** Create new <code>FileStateInvalidException</code> with the specified text.
    * @param s the text describing the exception
    * @param fsName the name of the filesystem containing the bad FileObject
    */
    FileStateInvalidException(String s, String fsName) {
        super(s);
        fileSystemName = fsName;
    }

    /** @return the name of the fileSystem containing the bad FileObject.  null
    * if this information is unavailable.
    * @since 1.30
    */
    public String getFileSystemName() {
        return fileSystemName;
    }
}
