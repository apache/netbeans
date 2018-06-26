/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */

package org.netbeans.modules.web.javascript.debugger.callstack;

import java.awt.Image;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.netbeans.api.debugger.Session;
import org.netbeans.modules.web.webkit.debugging.api.Debugger;
import org.netbeans.modules.web.webkit.debugging.api.debugger.CallFrame;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.spi.debugger.ui.DebuggingView;

/**
 *
 * @author Martin Entlicher
 */
@DebuggingView.DVSupport.Registration(path="javascript-session")
public class DebuggingViewSupportImpl extends DebuggingView.DVSupport {
    
    private final Session session;
    private final Debugger dbg;
    private final JSThread defaultThread;
    
    public DebuggingViewSupportImpl(ContextProvider lookupProvider) {
        session = lookupProvider.lookupFirst(null, Session.class);
        dbg = lookupProvider.lookupFirst(null, Debugger.class);
        defaultThread = new JSThread(dbg, this);
        Debugger.Listener chl = new ChangeListener();
        dbg.addListener(chl);
    }

    @Override
    public STATE getState() {
        if (!dbg.isEnabled()) {
            return STATE.DISCONNECTED;
        } else {
            return STATE.RUNNING;
        }
    }

    @Override
    public List<DebuggingView.DVThread> getAllThreads() {
        return Collections.<DebuggingView.DVThread>singletonList(defaultThread);
    }

    @Override
    public DebuggingView.DVThread getCurrentThread() {
        return defaultThread;
    }

    @Override
    public String getDisplayName(DebuggingView.DVThread thread) {
        return thread.getName();
    }

    @Override
    public Image getIcon(DebuggingView.DVThread thread) {
        return null;
    }

    @Override
    public Session getSession() {
        return session;
    }

    @Override
    public void resume() {
        dbg.resume();
    }

    @Override
    public Set<DebuggingView.Deadlock> getDeadlocks() {
        return null;
    }

    @Override
    protected List<DebuggingView.DVFilter> getFilters() {
        return Collections.EMPTY_LIST;
    }
    
    private class ChangeListener implements Debugger.Listener {
        
        public ChangeListener() {}

        @Override
        public void paused(List<CallFrame> callStack, String reason) {
        }

        @Override
        public void resumed() {
        }

        @Override
        public void reset() {
        }

        @Override
        public void enabled(boolean enabled) {
            firePropertyChange(PROP_STATE, null, enabled ? STATE.RUNNING : STATE.DISCONNECTED);
        }
        
    }
    
}
