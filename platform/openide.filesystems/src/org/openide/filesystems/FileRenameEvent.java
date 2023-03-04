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


/** Event indicating a file rename.
*
* @author Petr Hamernik
*/
public class FileRenameEvent extends FileEvent {
    /** generated Serialized Version UID */
    private static final long serialVersionUID = -3947658371806653711L;

    /** Original name of the file. */
    private String name;

    /** Original extension of the file. */
    private String ext;

    /** Creates new <code>FileRenameEvent</code>. The <code>FileObject</code> where the action took place
    * is assumed to be the same as the source object.
    * @param src source file which sent this event
    * @param name original file name
    * @param ext original file extension
    */
    public FileRenameEvent(FileObject src, String name, String ext) {
        this(src, src, name, ext);
    }

    /** Creates new <code>FileRenameEvent</code>, specifying an event location.
    * @param src source file which sent this event
    * @param file file object where the action took place
    * @param name original file name
    * @param ext original file extension
    */
    public FileRenameEvent(FileObject src, FileObject file, String name, String ext) {
        this(src, file, name, ext, false);
    }

    /** Creates new <code>FileRenameEvent</code>, specifying an event location
    * and whether the event was expected by the system.
    * @param src source file which sent this event
    * @param file file object where the action took place
    * @param name original file name
    * @param ext original file extension
    * @param expected whether the value was expected
    */
    public FileRenameEvent(FileObject src, FileObject file, String name, String ext, boolean expected) {
        super(src, file, expected);
        this.name = name;
        this.ext = ext;
    }

    /** Get original name of the file.
    * @return old name of the file
    */
    public String getName() {
        return name;
    }

    /** Get original extension of the file.
    * @return old extension of the file
    */
    public String getExt() {
        return ext;
    }

    @Override
    void insertIntoToString(StringBuilder b) {
        b.append(",name.ext=");
        b.append(name);
        b.append('.');
        b.append(ext);
    }

}
