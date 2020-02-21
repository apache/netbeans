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

package org.netbeans.modules.cnd.debugger.common2.debugger.debugging;

import java.awt.Image;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import org.netbeans.api.debugger.Session;
import org.netbeans.modules.cnd.debugger.common2.debugger.NativeDebugger;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.spi.debugger.ui.DebuggingView;

/**
 *
 */

public abstract class NativeDVSupportImpl extends DebuggingView.DVSupport{    
    private final NativeDebugger debugger;
    
    public NativeDVSupportImpl(ContextProvider lookupProvider) {
        debugger = lookupProvider.lookupFirst(null, NativeDebugger.class);
    }

    @Override
    public STATE getState() {
        if (debugger.state().isRunning) {
            return STATE.RUNNING;
        }

        return STATE.DISCONNECTED;
    }

    @Override
    public List<DebuggingView.DVThread> getAllThreads() {
        return Arrays.asList((DebuggingView.DVThread[])  debugger.getThreadsWithStacks());
    }

    @Override
    public DebuggingView.DVThread getCurrentThread() {
        for (DebuggingView.DVThread dVThread : getAllThreads()) {
            if (((org.netbeans.modules.cnd.debugger.common2.debugger.Thread) dVThread).isCurrent()) {
                return dVThread;
            }
        }
        return null;
    }

    @Override
    public String getDisplayName(DebuggingView.DVThread thread) {
        return "";
    }

    @Override
    public Image getIcon(DebuggingView.DVThread thread) {
        return null;
    }

    @Override
    public Session getSession() {
        return debugger.session().coreSession();
    }

    @Override
    public void resume() {
         debugger.go();
    }

    @Override
    public Set<DebuggingView.Deadlock> getDeadlocks() {
        return null;
    }

    @Override
    protected List<DebuggingView.DVFilter> getFilters() {
        List<DebuggingView.DVFilter> list = new ArrayList<DebuggingView.DVFilter>();
//        list.add(DebuggingView.DVFilter.getDefault(DebuggingView.DVFilter.DefaultFilter.showSuspendedThreadsOnly));
//        list.add(DebuggingView.DVFilter.getDefault(DebuggingView.DVFilter.DefaultFilter.showThreadGroups));
//        list.add(DebuggingView.DVFilter.getDefault(DebuggingView.DVFilter.DefaultFilter.showSuspendTable));
//        list.add(DebuggingView.DVFilter.getDefault(DebuggingView.DVFilter.DefaultFilter.showSystemThreads));
//        list.add(DebuggingView.DVFilter.getDefault(DebuggingView.DVFilter.DefaultFilter.showMonitors));
//        list.add(DebuggingView.DVFilter.getDefault(DebuggingView.DVFilter.DefaultFilter.showQualifiedNames));
//        list.add(DebuggingView.DVFilter.getDefault(DebuggingView.DVFilter.DefaultFilter.sortSuspend));
//        list.add(DebuggingView.DVFilter.getDefault(DebuggingView.DVFilter.DefaultFilter.sortAlphabetic));
//        list.add(DebuggingView.DVFilter.getDefault(DebuggingView.DVFilter.DefaultFilter.sortNatural));
        return list;
    }
}
