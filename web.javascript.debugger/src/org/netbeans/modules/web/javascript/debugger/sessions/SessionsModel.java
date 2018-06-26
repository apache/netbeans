/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.web.javascript.debugger.sessions;

import java.util.List;
import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.api.debugger.Session;
import org.netbeans.modules.javascript2.debug.ui.models.ViewModelSupport;
import org.netbeans.modules.web.webkit.debugging.api.Debugger;
import org.netbeans.modules.web.webkit.debugging.api.debugger.CallFrame;
import org.netbeans.spi.debugger.DebuggerServiceRegistration;
import static org.netbeans.spi.debugger.ui.Constants.*;
import org.netbeans.spi.viewmodel.TableModel;
import org.netbeans.spi.viewmodel.TableModelFilter;
import org.netbeans.spi.viewmodel.UnknownTypeException;
import org.openide.util.NbBundle;

@NbBundle.Messages({
    "Session_running=Running",
    "Session_paused=Paused",
    "Session_none="
})
@DebuggerServiceRegistration(path="SessionsView", types=TableModelFilter.class)
public final class SessionsModel extends ViewModelSupport implements TableModelFilter, Debugger.Listener  {

    private Debugger d;
    
    public SessionsModel() {
    }

    @Override
    public Object getValueAt(TableModel original, Object row, String columnID) throws UnknownTypeException {
        if (row instanceof Session && isDebuggerSession((Session) row)) {
            if (SESSION_STATE_COLUMN_ID.equals(columnID)) {
                return getSessionState((Session) row);
            }
        }
        return original.getValueAt(row, columnID);
    }

    @Override
    public boolean isReadOnly(TableModel original, Object row, String columnID) throws UnknownTypeException {
        if (row instanceof Session && isDebuggerSession((Session) row)) {
            if (SESSION_STATE_COLUMN_ID.equals(columnID)) {
                return true;
            }
        }
        return original.isReadOnly(row, columnID);
    }

    @Override
    public void setValueAt(TableModel original, Object row, String columnID, Object value) throws UnknownTypeException {
        original.setValueAt(row, columnID, value);
    }

    private static boolean isDebuggerSession(final Session s) {
        DebuggerEngine e = s.getCurrentEngine();
        if (e == null) {
            return false;
        }
        return e.lookupFirst(null, Debugger.class) != null;
    }
    
    private Object getSessionState(final Session s) {
        Debugger debugger = s.getCurrentEngine().lookupFirst(null, Debugger.class);
        if (debugger == null) {
            return "";
        }
        if (d == null) {
            d = debugger;
            d.addListener(this);
        }
        if (debugger.isEnabled()) {
            if (debugger.isSuspended()) {
                return Bundle.Session_paused();
            } else {
                return Bundle.Session_running();
            }
        }
        return Bundle.Session_none();
    }

    @Override
    public void paused(List<CallFrame> callStack, String reason) {
        refresh();
    }

    @Override
    public void resumed() {
        refresh();
    }

    @Override
    public void reset() {
    }

    @Override
    public void enabled(boolean enabled) {
    }
    
}
