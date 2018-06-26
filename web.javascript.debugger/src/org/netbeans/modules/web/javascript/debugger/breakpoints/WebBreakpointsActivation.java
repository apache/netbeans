/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2015 Sun Microsystems, Inc.
 */

package org.netbeans.modules.web.javascript.debugger.breakpoints;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.api.debugger.ActiveBreakpoints;
import org.netbeans.modules.web.webkit.debugging.api.Debugger;
import org.netbeans.spi.debugger.BreakpointsActivationProvider;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.spi.debugger.DebuggerServiceRegistration;
import org.openide.util.RequestProcessor;

/**
 * WEB JavaScript activation/deactivation of breakpoints.
 * 
 * @author Martin Entlicher
 */
@DebuggerServiceRegistration(path="javascript-debuggerengine", types = BreakpointsActivationProvider.class)
public class WebBreakpointsActivation implements BreakpointsActivationProvider {

    private static final RequestProcessor RP = new RequestProcessor(WebBreakpointsActivation.class.getName());
    private final Debugger debugger;
    private final Map<PropertyChangeListener, DelegateListener> listenersMap = Collections.synchronizedMap(new HashMap<PropertyChangeListener, DelegateListener>());
    
    public WebBreakpointsActivation(ContextProvider contextProvider) {
        this.debugger = contextProvider.lookupFirst(null, Debugger.class);
    }

    @Override
    public boolean areBreakpointsActive() {
        return debugger.areBreakpointsActive();
    }

    @Override
    public void setBreakpointsActive(final boolean active) {
        RP.post(new Runnable() {
            @Override
            public void run() {
                debugger.setBreakpointsActive(active);
            }
        });
    }

    @Override
    public void addPropertyChangeListener(final PropertyChangeListener l) {
        DelegateListener dl = new DelegateListener(this, l);
        listenersMap.put(l, dl);
        debugger.addPropertyChangeListener(dl);
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener l) {
        DelegateListener dl = listenersMap.remove(l);
        if (dl != null) {
            debugger.removePropertyChangeListener(dl);
        }
    }
    
    private static class DelegateListener implements PropertyChangeListener {
        
        private final Object source;
        private final PropertyChangeListener l;
        
        public DelegateListener(Object source, PropertyChangeListener l) {
            this.source = source;
            this.l = l;
        }
        
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (Debugger.PROP_BREAKPOINTS_ACTIVE.equals(evt.getPropertyName())) {
                l.propertyChange(new PropertyChangeEvent(
                        source,
                        ActiveBreakpoints.PROP_BREAKPOINTS_ACTIVE,
                        evt.getOldValue(),
                        evt.getNewValue()));
            }
        }
    }

}
