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

/** Exception signalling that the data object for this file cannot
* be created because there already is an object for the primary file.
*
* @author Jaroslav Tulach
* @version 0.10, Mar 30, 1998
*/
public class DataObjectExistsException extends java.io.IOException {
    /** generated Serialized Version UID */
    static final long serialVersionUID = 4719319528535266801L;
    /** data object */
    private DataObject obj;

    /** Create new exception.
    * @param obj data object which already exists
    */
    public DataObjectExistsException (DataObject obj) {
        this.obj = obj;
    }

    /** Get the object which already exists.
     * @return the data object
    */
    public DataObject getDataObject () {
        //
        // we have to consult the DataObjectPool to check whether
        // the constructor of our DataObject has finished
        //
        DataObjectPool.getPOOL().waitNotified (obj);
        
        // now it should be safe to return the objects
        return obj;
    }

    /** Performance trick */
    @Override
    public /*final*/ Throwable fillInStackTrace() {
        return this;
    }
}
