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
package org.netbeans.modules.python.debugger.spi;

import java.util.Enumeration;
import java.util.Vector;
import org.netbeans.modules.python.debugger.PythonDebugger;
import org.netbeans.api.debugger.Session;
import org.netbeans.spi.debugger.ui.Constants;
import org.netbeans.spi.viewmodel.ModelEvent;
import org.netbeans.spi.viewmodel.ModelListener;
import org.netbeans.spi.viewmodel.TableModel;
import org.netbeans.spi.viewmodel.TableModelFilter;
import org.netbeans.spi.viewmodel.UnknownTypeException;

public class SessionsModel
        implements TableModelFilter {

  private Vector<ModelListener> _listeners = new Vector<>();

  /** Creates a new instance of SessionsModel */
  public SessionsModel() {
    System.out.println("entering Sessions instance creation");
  }

  /** 
   * Registers given listener.
   *
   * @param l the listener to add
   */
  @Override
  public void addModelListener(ModelListener l) {
    _listeners.add(l);
  }

  /** 
   * Unregisters given listener.
   *
   * @param l the listener to remove
   */
  @Override
  public void removeModelListener(ModelListener l) {
    _listeners.remove(l);
  }

  /**
   * Used by Python debugger session to populate it's state back to 
   *session's view
   */
  public void populateNewSessionState(Object source) {
    Enumeration lList = _listeners.elements();
    ModelEvent evt = new ModelEvent.TableValueChanged(source, source, Constants.SESSION_STATE_COLUMN_ID);
    while (lList.hasMoreElements()) {
      ((ModelListener) lList.nextElement()).modelChanged(evt);
    }

  }

  @Override
  public Object getValueAt(TableModel original, Object node, String columnId)
          throws UnknownTypeException {
    if (!(node instanceof Session)) {
      throw new UnknownTypeException(node);
    }
    PythonDebugger pySession = PythonDebugger.map((Session) node);
    //if ( columnId.equals( Constants.SESSION_STATE_COLUMN_ID  ) )
    //  return ( pySession.getDebuggerState(this) ) ;
    return (original.getValueAt(node, columnId));
  }

  @Override
  public boolean isReadOnly(TableModel original, Object node, String columnId)
          throws UnknownTypeException {
    return original.isReadOnly(node, columnId);
  }

  @Override
  public void setValueAt(TableModel original, Object node, String columnId, Object value)
          throws UnknownTypeException {
    original.setValueAt(node, columnId, value);
  }
}
