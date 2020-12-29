/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
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
