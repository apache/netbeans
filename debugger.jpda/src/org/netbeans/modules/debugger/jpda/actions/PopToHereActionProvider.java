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

package org.netbeans.modules.debugger.jpda.actions;

import com.sun.jdi.AbsentInformationException;
import java.util.Collections;
import java.util.Set;
import org.netbeans.api.debugger.ActionsManager;

import org.netbeans.api.debugger.jpda.CallStackFrame;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.api.debugger.jpda.JPDAThread;
import org.netbeans.modules.debugger.jpda.JPDADebuggerImpl;
import org.netbeans.modules.debugger.jpda.models.JPDAThreadImpl;
import org.netbeans.spi.debugger.ActionsProvider;


/**
* Pop to Here action implementation.
*
* @author   Jan Jancura
*/
@ActionsProvider.Registration(path="netbeans-JPDASession", actions={"popTopmostCall"})
public class PopToHereActionProvider extends JPDADebuggerActionProvider {
    
    public PopToHereActionProvider (ContextProvider lookupProvider) {
        super (
            (JPDADebuggerImpl) lookupProvider.lookupFirst 
                (null, JPDADebugger.class) 
        );
        setProviderToDisableOnLazyAction(this);
    }
    
    @Override
    public Set getActions () {
        return Collections.singleton (ActionsManager.ACTION_POP_TOPMOST_CALL);
    }

    @Override
    public void doAction (Object action) {
        runAction();
    }
    
    @Override
    public void postAction(Object action, final Runnable actionPerformedNotifier) {
        doLazyAction(action, new Runnable() {
            @Override
            public void run() {
                try {
                    runAction();
                } finally {
                    actionPerformedNotifier.run();
                }
            }
        });
    }
    
    public void runAction() {
        try {
            JPDAThread t = getDebuggerImpl ().getCurrentThread ();
            ((JPDAThreadImpl) t).accessLock.writeLock().lock();
            try {
                CallStackFrame[] frames = t.getCallStack (0, 2);
                if (frames.length > 1) {
                    frames[0].popFrame ();
                }
            } finally {
                ((JPDAThreadImpl) t).accessLock.writeLock().unlock();
            }
        } catch (AbsentInformationException ex) {
        }
    }
    
    @Override
    protected void checkEnabled (int debuggerState) {
        if (!getDebuggerImpl().canPopFrames()) {
            setEnabled (
                ActionsManager.ACTION_POP_TOPMOST_CALL,
                false
            );
            return;
        }
        JPDAThread t;
        if (debuggerState == JPDADebugger.STATE_STOPPED) {
            t = getDebuggerImpl ().getCurrentThread ();
        } else {
            t = null;
        }
        boolean enabled;
        if (t == null) {
            enabled = false;
        } else {
            enabled = t.isSuspended();
        }
        setEnabled (
            ActionsManager.ACTION_POP_TOPMOST_CALL,
            enabled
        );
    }
}
