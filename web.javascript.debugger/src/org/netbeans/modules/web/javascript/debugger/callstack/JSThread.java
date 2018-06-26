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

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Collections;
import java.util.List;
import org.netbeans.api.debugger.Breakpoint;
import org.netbeans.modules.javascript2.debug.breakpoints.JSLineBreakpoint;
import org.netbeans.modules.web.webkit.debugging.api.Debugger;
import org.netbeans.modules.web.webkit.debugging.api.debugger.CallFrame;
import org.netbeans.spi.debugger.ui.DebuggingView;

/**
 * The default JavaScript thread.
 * 
 * @author Martin Entlicher
 */
public class JSThread implements DebuggingView.DVThread {
    
    private final Debugger debugger;
    private final DebuggingView.DVSupport dvSupport;
    private final PropertyChangeSupport pchs = new PropertyChangeSupport(this);
    
    public JSThread(Debugger debugger, DebuggingView.DVSupport dvSupport) {
        this.debugger = debugger;
        this.dvSupport = dvSupport;
        ChangeListener chl = new ChangeListener();
        debugger.addListener(chl);
    }

    @Override
    public String getName() {
        return "default";
    }

    @Override
    public boolean isSuspended() {
        return debugger.isSuspended();
    }

    @Override
    public void resume() {
        debugger.resume();
    }

    @Override
    public void suspend() {
        debugger.pause();
    }

    @Override
    public void makeCurrent() {
    }

    @Override
    public DebuggingView.DVSupport getDVSupport() {
        return dvSupport;
    }

    @Override
    public List<DebuggingView.DVThread> getLockerThreads() {
        return Collections.EMPTY_LIST;
    }

    @Override
    public void resumeBlockingThreads() {
    }

    @Override
    public Breakpoint getCurrentBreakpoint() {
        return null; // TODO
    }

    @Override
    public boolean isInStep() {
        return false; // Used when a deadlock occurs during stepping.
    }
    
    @Override
    public void addPropertyChangeListener(PropertyChangeListener pcl) {
        pchs.addPropertyChangeListener(pcl);
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener pcl) {
        pchs.removePropertyChangeListener(pcl);
    }

    private class ChangeListener implements Debugger.Listener {

        public ChangeListener() {}

        @Override
        public void paused(List<CallFrame> callStack, String reason) {
            pchs.firePropertyChange(DebuggingView.DVThread.PROP_SUSPENDED, null, true);
        }

        @Override
        public void resumed() {
            pchs.firePropertyChange(DebuggingView.DVThread.PROP_SUSPENDED, null, false);
        }

        @Override
        public void reset() {
        }

        @Override
        public void enabled(boolean enabled) {
        }
        
        
    }
    
}
