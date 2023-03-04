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

import java.util.EventObject;

import org.openide.util.Lookup;
import org.openide.filesystems.*;

/** Adapter for listening on changes of fileobjects and refreshing data
* shadows/broken links
*
* @author Ales Kemr
*/
class ShadowChangeAdapter extends Object implements OperationListener {

    /** Creates new ShadowChangeAdapter */
    ShadowChangeAdapter() {

        /* listen on loader pool to refresh datashadows after
        * create/delete dataobject
        */
        DataLoaderPool.getDefault().addOperationListener(this);
    }

    /** Checks for BrokenDataShadows */
    static void checkBrokenDataShadows(EventObject ev) {
        BrokenDataShadow.checkValidity(ev);
    }
    
    /** Checks for DataShadows */
    static void checkDataShadows(EventObject ev) {
        DataShadow.checkValidity(ev);
    }
    
    /** Object has been recognized by
     * {@link DataLoaderPool#findDataObject}.
     * This allows listeners
     * to attach additional cookies, etc.
     *
     * @param ev event describing the action
    */
    public void operationPostCreate(OperationEvent ev) {
        checkBrokenDataShadows(ev);
    }
    
    /** Object has been successfully copied.
     * @param ev event describing the action
    */
    public void operationCopy(OperationEvent.Copy ev) {
    }
    
    /** Object has been successfully moved.
     * @param ev event describing the action
    */
    public void operationMove(OperationEvent.Move ev) {
        checkDataShadows(ev);
        checkBrokenDataShadows(ev);
    }
    
    /** Object has been successfully deleted.
     * @param ev event describing the action
    */
    public void operationDelete(OperationEvent ev) {
        checkDataShadows(ev);
    }
    
    /** Object has been successfully renamed.
     * @param ev event describing the action
    */
    public void operationRename(OperationEvent.Rename ev) {
        checkDataShadows(ev);
        checkBrokenDataShadows(ev);
    }
    
    /** A shadow of a data object has been created.
     * @param ev event describing the action
    */
    public void operationCreateShadow(OperationEvent.Copy ev) {
    }
    
    /** New instance of an object has been created.
     * @param ev event describing the action
    */
    public void operationCreateFromTemplate(OperationEvent.Copy ev) {
        checkBrokenDataShadows(ev);
    }
    
}
