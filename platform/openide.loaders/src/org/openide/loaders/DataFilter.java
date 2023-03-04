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


/** Allows certain data objects to be excluded from being displayed.
* @see RepositoryNodeFactory
* @author Jaroslav Tulach
*/
public interface DataFilter extends java.io.Serializable {
    /** Should the data object be displayed or not?
    * @param obj the data object
    * @return <CODE>true</CODE> if the object should be displayed,
    *    <CODE>false</CODE> otherwise
    */
    public boolean acceptDataObject (DataObject obj);

    /** Default filter that accepts everything.
    */
    /*public static final*/ DataFilter ALL = new DataFilterAll ();

    /** @deprecated Only public by accident. */
    @Deprecated
    /* public static final */ long serialVersionUID = 0L;

    /** Additional interface that can be implemented by the {@link DataFilter}
     * implementors to do low level filtering based on the file objects.
     * @since 7.4
     */
    public interface FileBased extends DataFilter {
        /** Should this fileobject be considered for displaying?
         *
         * @param fo the file object
         * @return <CODE>true</CODE> if the object should be displayed,
         *    <CODE>false</CODE> otherwise
         */
        public boolean acceptFileObject(FileObject fo);
    }
}
