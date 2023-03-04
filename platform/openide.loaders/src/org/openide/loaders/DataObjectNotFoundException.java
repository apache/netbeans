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

import org.openide.filesystems.FileObject;

/** Exception signalling that the data object for a given file object could not
* be found in {@link DataObject#find}.
*
* @author Jaroslav Tulach
*/
public class DataObjectNotFoundException extends java.io.IOException {
    /** generated Serialized Version UID */
    static final long serialVersionUID = 1646623156535839081L;
    /** data object */
    private FileObject obj;

    /** Create a new exception.
    * @param obj the file that does not have a data object
    */
    public DataObjectNotFoundException (FileObject obj) {
        super (obj.toString ());
        this.obj = obj;
    }

    /** Get the file which does not have a data object.
     * @return the file
    */
    public FileObject getFileObject () {
        return obj;
    }
}
