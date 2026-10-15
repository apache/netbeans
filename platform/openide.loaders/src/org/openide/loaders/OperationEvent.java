/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.openide.loaders;

import java.util.EventObject;

import org.openide.util.Lookup;
import org.openide.filesystems.FileObject;

/** Event that describes operations taken on
* a data object.
*
* @author Jaroslav Tulach
*/
public class OperationEvent extends EventObject {
    /** package private numbering of methods */
    static final int COPY = 1, MOVE = 2, DELETE = 3, RENAME = 4, SHADOW = 5, TEMPL = 6, CREATE = 7;

    /** data object */
    private DataObject obj;
    private static final DataLoaderPool pl = DataLoaderPool.getDefault();
    static final long serialVersionUID =-3884037468317843808L;
    OperationEvent(DataObject obj) {
        super (pl);
        this.obj = obj;
    }

    /** Get the data object that has been modified.
    * @return the data object
    */
    public DataObject getObject () {
        return obj;
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append(super.toString());
        sb.append(":");
        sb.append(" for ");
        sb.append(obj);
        writeDebug(sb);
        return sb.toString();
    }

    /** For subclasses in this package to write debug info */
    void writeDebug(StringBuffer sb) {
    }

    /** Notification of a rename of a data object.
    */
    public static final class Rename extends OperationEvent {
        /** name */
        private String name;

        static final long serialVersionUID =-1584168503454848519L;
        /** @param obj renamed object
        * @param name original name
        */
        Rename (DataObject obj, String name) {
            super (obj);
            this.name = name;
        }

        /** Get the old name of the object.
         * @return the old name
        */
        public String getOriginalName () {
            return name;
        }

        @Override
        final void writeDebug(StringBuffer sb) {
            sb.append(" originalname: ");
            sb.append(name);
        }
    }

    /** Notification of a move of a data object.
    */
    public static final class Move extends OperationEvent {
        /** original file */
        private FileObject file;

        static final long serialVersionUID =-7753279728025703632L;
        /** @param obj renamed object
        * @param file original primary file
        */
        Move (DataObject obj, FileObject file) {
            super (obj);
            this.file = file;
        }

        /** Get the original primary file.
        * @return the file
        */
        public FileObject getOriginalPrimaryFile () {
            return file;
        }

        @Override
        final void writeDebug(StringBuffer sb) {
            sb.append(" originalfile: ");
            sb.append(file);
        }
    }

    /** Notification of a copy action of a data object, creation of a shadow,
    * or creation from a template.
    */
    public static final class Copy extends OperationEvent {
        /** original data object */
        private DataObject orig;

        static final long serialVersionUID =-2768331988864546290L;
        /** @param obj renamed object
        * @param orig original object
        */
        Copy (DataObject obj, DataObject orig) {
            super (obj);
            this.orig = orig;
        }


        /** Get the original data object.
        * @return the data object
        */
        public DataObject getOriginalDataObject () {
            return orig;
        }


        @Override
        final void writeDebug(StringBuffer sb) {
            sb.append(" originalobj: ");
            sb.append(orig);
        }
    }
}
