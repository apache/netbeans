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

package org.netbeans.modules.debugger.ui.models;

import java.util.Vector;
import org.netbeans.api.debugger.Session;
import org.netbeans.spi.debugger.ui.Constants;
import org.netbeans.spi.viewmodel.TableModel;
import org.netbeans.spi.viewmodel.ModelListener;
import org.netbeans.spi.viewmodel.UnknownTypeException;


/**
 *
 * @author   Jan Jancura
 */
public class SessionsTableModel implements TableModel, Constants {

    private Vector listeners = new Vector ();


    public Object getValueAt (Object row, String columnID) throws
    UnknownTypeException {
        if (row instanceof Session) {
            if (columnID.equals (SESSION_STATE_COLUMN_ID))
                return "";
            else
            if (columnID.equals (SESSION_LANGUAGE_COLUMN_ID))
                return row;
            else
            if (columnID.equals (SESSION_HOST_NAME_COLUMN_ID))
                return ((Session) row).getLocationName ();
        }
        throw new UnknownTypeException (row);
    }
    
    public boolean isReadOnly (Object row, String columnID) throws 
    UnknownTypeException {
        if (row instanceof Session) {
            if (columnID.equals (SESSION_STATE_COLUMN_ID))
                return true;
            else
            if (columnID.equals (SESSION_LANGUAGE_COLUMN_ID))
                return false;
            else
            if (columnID.equals (SESSION_HOST_NAME_COLUMN_ID))
                return true;
        }
        throw new UnknownTypeException (row);
    }
    
    public void setValueAt (Object row, String columnID, Object value) 
    throws UnknownTypeException {
        throw new UnknownTypeException (row);
    }
    
    /** 
     * Registers given listener.
     * 
     * @param l the listener to add
     */
    public void addModelListener (ModelListener l) {
        listeners.add (l);
    }

    /** 
     * Unregisters given listener.
     *
     * @param l the listener to remove
     */
    public void removeModelListener (ModelListener l) {
        listeners.remove (l);
    }

}
