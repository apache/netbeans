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

import java.util.Collections;
import java.util.Set;

import org.netbeans.api.debugger.ActionsManager;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.modules.debugger.jpda.JPDADebuggerImpl;
import org.netbeans.modules.debugger.jpda.ThreadsCollectorImpl;
import org.netbeans.spi.debugger.ActionsProvider;


/**
* Pause action implementation.
*
* @author   Jan Jancura
* @author  Marian Petras
*/
@ActionsProvider.Registration(path="netbeans-JPDASession", actions={"pause"})
public class PauseActionProvider extends JPDADebuggerActionProvider {
    
    private final ThreadsCollectorImpl threadsCollector;

    public PauseActionProvider (ContextProvider contextProvider) {
        super (
            (JPDADebuggerImpl) contextProvider.lookupFirst 
                (null, JPDADebugger.class)
        );
        threadsCollector = (ThreadsCollectorImpl) debugger.getThreadsCollector();
        threadsCollector.addPropertyChangeListener(this);
        setProviderToDisableOnLazyAction(this);
    }
    
    @Override
    public Set getActions () {
        return Collections.singleton (ActionsManager.ACTION_PAUSE);
    }

    @Override
    public void doAction (Object action) {
        ((JPDADebuggerImpl) getDebuggerImpl ()).suspend ();
    }
    
    @Override
    public void postAction(Object action, final Runnable actionPerformedNotifier) {
        doLazyAction(action, new Runnable() {
            @Override
            public void run() {
                try {
                    ((JPDADebuggerImpl) getDebuggerImpl ()).suspend ();
                } finally {
                    actionPerformedNotifier.run();
                }
            }
        });
    }
    
    @Override
    protected void checkEnabled (int debuggerState) {
        setEnabled (
            ActionsManager.ACTION_PAUSE,
            threadsCollector.isSomeThreadRunning()
        );
    }
    
}
