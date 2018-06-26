/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */

package org.netbeans.modules.web.javascript.debugger.breakpoints;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.net.URL;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.DebuggerManagerAdapter;
import org.netbeans.api.debugger.Session;
import org.netbeans.modules.javascript2.debug.breakpoints.JSBreakpointsInfo;
import org.netbeans.modules.web.javascript.debugger.MiscEditorUtil;
import org.netbeans.modules.web.webkit.debugging.api.Debugger;
import org.openide.filesystems.FileObject;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Martin
 */
@ServiceProvider(service = JSBreakpointsInfo.class)
public class WebBreakpointsActiveService implements JSBreakpointsInfo {

    private volatile boolean active = true;
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    
    public WebBreakpointsActiveService() {
        SessionActiveListener sal = new SessionActiveListener();
        DebuggerManager.getDebuggerManager().addDebuggerListener(DebuggerManager.PROP_CURRENT_SESSION, sal);
    }
    
    @Override
    public boolean areBreakpointsActivated() {
        return active;
    }
    
    private void setActive(boolean active) {
        if (this.active != active) {
            this.active = active;
            pcs.firePropertyChange(PROP_BREAKPOINTS_ACTIVE, !active, active);
        }
    }
    
    @Override
    public boolean isAnnotatable(FileObject fo) {
        String mimeType = fo.getMIMEType();
        return MiscEditorUtil.isJSOrWrapperMIMEType(mimeType);
    }

    @Override
    public boolean isTransientURL(URL url) {
        return false;
    }
    
    @Override
    public void addPropertyChangeListener(PropertyChangeListener l) {
        pcs.addPropertyChangeListener(l);
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener l) {
        pcs.removePropertyChangeListener(l);
    }
    
    private class SessionActiveListener extends DebuggerManagerAdapter {
        
        private Debugger currentDebugger;
        
        public SessionActiveListener() {
            currentDebugger = getCurrentDebugger();
            if (currentDebugger != null) {
                active = currentDebugger.areBreakpointsActive();
            }
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            String propertyName = evt.getPropertyName();
            if (DebuggerManager.PROP_CURRENT_SESSION.equals(propertyName)) {
                Debugger newDebugger = getCurrentDebugger();
                synchronized (this) {
                    if (currentDebugger != null) {
                        currentDebugger.removePropertyChangeListener(this);
                    }
                    currentDebugger = newDebugger;
                }
                if (newDebugger != null) {
                    setActive(newDebugger.areBreakpointsActive());
                } else {
                    setActive(true);
                }
            }
            if (Debugger.PROP_BREAKPOINTS_ACTIVE.equals(propertyName)) {
                setActive(((Debugger) evt.getSource()).areBreakpointsActive());
            }
        }
        
        private Debugger getCurrentDebugger() {
            Session s = DebuggerManager.getDebuggerManager().getCurrentSession();
            if (s != null) {
                Debugger debugger = s.lookupFirst(null, Debugger.class);
                if (debugger != null) {
                    debugger.addPropertyChangeListener(this);
                }
                return debugger;
            } else {
                return null;
            }
        }

    }
}
