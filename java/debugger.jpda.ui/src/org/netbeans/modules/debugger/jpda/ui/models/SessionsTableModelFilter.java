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

package org.netbeans.modules.debugger.jpda.ui.models;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Vector;

import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.api.debugger.Session;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.spi.debugger.DebuggerServiceRegistration;
import org.netbeans.spi.debugger.ui.Constants;
import org.netbeans.spi.viewmodel.TableModel;
import org.netbeans.spi.viewmodel.TableModelFilter;
import org.netbeans.spi.viewmodel.ModelListener;
import org.netbeans.spi.viewmodel.UnknownTypeException;
import org.openide.util.NbBundle;


/**
 *
 * @author   Jan Jancura
 */
@DebuggerServiceRegistration(path="SessionsView", types=TableModelFilter.class)
public class SessionsTableModelFilter implements TableModelFilter, Constants,
PropertyChangeListener {
    
    private static String loc(String key) {
        return NbBundle.getBundle (SessionsTableModelFilter.class).getString (key);
    }

    private Vector listeners = new Vector ();
    private boolean addedAsListener;

    
    public SessionsTableModelFilter () {
    }

    public Object getValueAt (TableModel original, Object row, String columnID) throws 
    UnknownTypeException {
        if (row instanceof Session && isJPDASession((Session) row)) {
            if (SESSION_STATE_COLUMN_ID.equals (columnID))
                return getSessionState ((Session) row);
            else
            if (SESSION_LANGUAGE_COLUMN_ID.equals (columnID))
                return row;
            else
            if (SESSION_HOST_NAME_COLUMN_ID.equals (columnID))
                return ((Session) row).getLocationName ();
            else
                throw new UnknownTypeException (row);
        }
        return original.getValueAt(row, columnID);
    }
    
    public boolean isReadOnly (TableModel original, Object row, String columnID) throws 
    UnknownTypeException {
        if (row instanceof Session && isJPDASession((Session) row)) {
            if (SESSION_STATE_COLUMN_ID.equals (columnID))
                return true;
            else
            if (SESSION_LANGUAGE_COLUMN_ID.equals (columnID))
                return false;
            else
            if (SESSION_HOST_NAME_COLUMN_ID.equals (columnID))
                return true;
            else
                throw new UnknownTypeException (row);
        }
        return original.isReadOnly(row, columnID);
    }
    
    public void setValueAt (TableModel original, Object row, String columnID, Object value) 
    throws UnknownTypeException {
        original.setValueAt(row, columnID, value);
    }

    
    // other methods ...........................................................

    static boolean isJPDASession(Session s) {
        DebuggerEngine e = s.getCurrentEngine ();
        if (e == null) {
            return false;
        }
        JPDADebugger d = e.lookupFirst(null, JPDADebugger.class);
        return d != null;
    }
    
    private String getSessionState (Session s) {
        DebuggerEngine e = s.getCurrentEngine ();
        if (e == null)
            return loc ("MSG_Session_State_Starting");
        JPDADebugger d = e.lookupFirst(null, JPDADebugger.class);
        synchronized (this) {
            if (!addedAsListener) {
                d.addPropertyChangeListener (JPDADebugger.PROP_STATE, this);
            }
        }
        switch (d.getState ()) {
            case JPDADebugger.STATE_DISCONNECTED:
                return loc ("MSG_Session_State_Disconnected");
            case JPDADebugger.STATE_RUNNING:
                return loc ("MSG_Session_State_Running");
            case JPDADebugger.STATE_STARTING:
                return loc ("MSG_Session_State_Starting");
            case JPDADebugger.STATE_STOPPED:
                return loc ("MSG_Session_State_Stopped");
        }
        return null;
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
    
    private void fireTreeChanged () {
        Vector v = (Vector) listeners.clone ();
        int i, k = v.size ();
        for (i = 0; i < k; i++)
            ((ModelListener) v.get (i)).modelChanged (null);
    }
    
    private static final Integer SD = JPDADebugger.STATE_DISCONNECTED;
    
    public void propertyChange (PropertyChangeEvent e) {
        fireTreeChanged ();
        if (e.getNewValue ().equals (SD))
            ((JPDADebugger) e.getSource ()).removePropertyChangeListener (
                JPDADebugger.PROP_STATE, this
            );
    }
}
