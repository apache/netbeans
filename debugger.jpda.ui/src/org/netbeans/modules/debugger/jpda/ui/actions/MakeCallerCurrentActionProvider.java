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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.debugger.jpda.ui.actions;

import com.sun.jdi.AbsentInformationException;

import java.util.Collections;
import java.util.Set;

import org.netbeans.api.debugger.ActionsManager;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.api.debugger.jpda.CallStackFrame;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.api.debugger.jpda.JPDAThread;
import org.netbeans.spi.debugger.ActionsProvider;

import org.openide.util.RequestProcessor;


/**
* Representation of a debugging session.
*
* @author   Jan Jancura
*/
@ActionsProvider.Registration(path="netbeans-JPDASession", actions="makeCallerCurrent")
public class MakeCallerCurrentActionProvider extends JPDADebuggerAction {
    
    private RequestProcessor rp;
    
    public MakeCallerCurrentActionProvider (ContextProvider lookupProvider) {
        super (
            lookupProvider.lookupFirst(null, JPDADebugger.class)
        );
        rp = lookupProvider.lookupFirst(null, RequestProcessor.class);
        getDebuggerImpl ().addPropertyChangeListener 
            (JPDADebugger.PROP_CURRENT_CALL_STACK_FRAME, this);
    }
    
    @Override
    public Set getActions () {
        return Collections.singleton (ActionsManager.ACTION_MAKE_CALLER_CURRENT);
    }

    @Override
    public void doAction (Object action) {
        JPDAThread t = getDebuggerImpl ().getCurrentThread ();
        if (t == null) return;
        int i = getCurrentCallStackFrameIndex (getDebuggerImpl ());
        if (i >= (t.getStackDepth () - 1)) return;
        setCurrentCallStackFrameIndex (getDebuggerImpl (), ++i);
    }
    
    @Override
    protected void checkEnabled (int debuggerState) {
        if (debuggerState == JPDADebugger.STATE_STOPPED) {
            JPDAThread t = getDebuggerImpl ().getCurrentThread ();
            if (t != null) {
                checkEnabledLazySingleAction(debuggerState, rp);
                return;
            }
        }
        setEnabledSingleAction(false);
    }

    @Override
    protected boolean checkEnabledLazyImpl(int debuggerState) {
        int i = getCurrentCallStackFrameIndex (getDebuggerImpl ());
        JPDAThread t = getDebuggerImpl ().getCurrentThread ();
        if (t == null) return false;
        return i < (t.getStackDepth () - 1);

    }

    static int getCurrentCallStackFrameIndex (JPDADebugger debuggerImpl) {
        JPDAThread t = debuggerImpl.getCurrentThread ();
        if (t == null) return -1;
        CallStackFrame csf = debuggerImpl.getCurrentCallStackFrame ();
        if (csf == null) return -1;
        return csf.getFrameDepth();
    }
    
    static void setCurrentCallStackFrameIndex (
        JPDADebugger debuggerImpl,
        int index
    ) {
        try {
            JPDAThread t = debuggerImpl.getCurrentThread ();
            if (t == null) return;
            if (t.getStackDepth () <= index) return;
            final CallStackFrame csf = t.getCallStack (index, index + 1) [0];
            csf.makeCurrent ();
        } catch (AbsentInformationException e) {
        }
    }
}
