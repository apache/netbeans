/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.ant.debugger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.api.debugger.Session;
import org.netbeans.spi.debugger.DebuggerServiceRegistration;
import org.netbeans.spi.debugger.ui.Constants;
import org.netbeans.spi.viewmodel.ModelEvent;
import org.netbeans.spi.viewmodel.ModelListener;
import org.netbeans.spi.viewmodel.TableModel;
import org.netbeans.spi.viewmodel.TableModelFilter;
import org.netbeans.spi.viewmodel.UnknownTypeException;
import org.openide.util.NbBundle;

/**
 *
 * @author Martin Entlicher
 */
@DebuggerServiceRegistration(path="SessionsView", types=TableModelFilter.class)
public class SessionsTableModelFilter implements TableModelFilter, Constants {
    
    private final List<Session> sessionListeners = new ArrayList<Session>();
    private final List<ModelListener> modelListeners = new CopyOnWriteArrayList<ModelListener>();
    
    public SessionsTableModelFilter() {}

    @Override
    public Object getValueAt(TableModel original, Object node, String columnID) throws UnknownTypeException {
        if (node instanceof Session && isANTSession((Session) node)) {
            if (SESSION_STATE_COLUMN_ID.equals (columnID)) {
                return getSessionState ((Session) node);
            } else
            if (SESSION_LANGUAGE_COLUMN_ID.equals (columnID)) {
                return node;
            } else
            if (SESSION_HOST_NAME_COLUMN_ID.equals (columnID)) {
                return ((Session) node).getLocationName ();
            } else {
                throw new UnknownTypeException (node);
            }
        }
        return original.getValueAt(node, columnID);
    }

    @Override
    public boolean isReadOnly(TableModel original, Object node, String columnID) throws UnknownTypeException {
        if (node instanceof Session && isANTSession((Session) node)) {
            if (SESSION_STATE_COLUMN_ID.equals (columnID)) {
                return true;
            } else
            if (SESSION_LANGUAGE_COLUMN_ID.equals (columnID)) {
                return true;
            } else
            if (SESSION_HOST_NAME_COLUMN_ID.equals (columnID)) {
                return true;
            } else {
                throw new UnknownTypeException (node);
            }
        }
        return original.isReadOnly(node, columnID);
    }

    @Override
    public void setValueAt(TableModel original, Object node, String columnID, Object value) throws UnknownTypeException {
        original.setValueAt(node, columnID, value);
    }

    private static boolean isANTSession(Session s) {
        DebuggerEngine e = s.getCurrentEngine ();
        if (e == null) {
            return false;
        }
        AntDebugger d = e.lookupFirst(null, AntDebugger.class);
        return d != null;
    }
    
    @NbBundle.Messages({"MSG_Session_State_Starting=Starting",
                        "MSG_Session_State_Finished=Finished",
                        "MSG_Session_State_Running=Running",
                        "MSG_Session_State_Stopped=Stopped"})
    private String getSessionState (Session s) {
        DebuggerEngine e = s.getCurrentEngine ();
        if (e == null) {
            return Bundle.MSG_Session_State_Starting();
        }
        AntDebugger d = e.lookupFirst(null, AntDebugger.class);
        if (d.isFinished()) {
            return Bundle.MSG_Session_State_Finished();
        }
        synchronized (sessionListeners) {
            if (!sessionListeners.contains(s)) {
                AntDebugger.StateListener asl = new ANTSessionStateListener(s, d);
                d.addStateListener(asl);
                sessionListeners.add(s);
            }
        }
        if (d.isSuspended()) {
            return Bundle.MSG_Session_State_Stopped();
        } else {
            return Bundle.MSG_Session_State_Running();
        }
    }
    
    private void fireModelChanged(Object node) {
        ModelEvent me = new ModelEvent.TableValueChanged(this, node, SESSION_STATE_COLUMN_ID);
        for (ModelListener ml : modelListeners) {
            ml.modelChanged(me);
        }
    }
    
    @Override
    public void addModelListener(ModelListener l) {
        modelListeners.add(l);
    }

    @Override
    public void removeModelListener(ModelListener l) {
        modelListeners.remove(l);
    }
    
    private class ANTSessionStateListener implements AntDebugger.StateListener {
        
        private final Session s;
        private final AntDebugger d;
        
        ANTSessionStateListener(Session s, AntDebugger d) {
            this.s = s;
            this.d = d;
        }

        @Override
        public void suspended(boolean suspended) {
            fireModelChanged(s);
        }

        @Override
        public void finished() {
            fireModelChanged(s);
            d.removeStateListener(this);
            synchronized (sessionListeners) {
                sessionListeners.remove(s);
            }
        }
        
    }
    
}
