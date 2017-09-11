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

package org.netbeans.modules.debugger.jpda.ui.actions;

import org.netbeans.api.debugger.ActionsManager;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.modules.debugger.jpda.DebuggerConsoleIO;
import org.netbeans.modules.debugger.jpda.JPDADebuggerImpl;
import org.netbeans.modules.debugger.jpda.actions.ActionErrorMessageCallback;
import org.netbeans.modules.debugger.jpda.actions.ActionMessageCallback;
import org.netbeans.modules.debugger.jpda.actions.ActionStatusDisplayCallback;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.spi.debugger.DebuggerServiceRegistration;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.StatusDisplayer;

/**
 *
 * @author Martin Entlicher
 */
@DebuggerServiceRegistration(path = "netbeans-JPDASession", types={ ActionMessageCallback.class,
                                                                    ActionErrorMessageCallback.class,
                                                                    ActionStatusDisplayCallback.class })
public class ActionMessageCallbackUIImpl implements ActionMessageCallback,
                                                    ActionErrorMessageCallback,
                                                    ActionStatusDisplayCallback {
    
    private final DebuggerConsoleIO consoleIO;
    
    public ActionMessageCallbackUIImpl(ContextProvider lookupProvider) {
        JPDADebuggerImpl dbg = (JPDADebuggerImpl) lookupProvider.lookupFirst(null, JPDADebugger.class);
        this.consoleIO = dbg.getConsoleIO();
    }

    @Override
    public void messageCallback(Object action, String message) {
        if (action == ActionsManager.ACTION_FIX) {
            // Special handling of messages coming from apply code changes - print into debug output.
            consoleIO.println(message, null, false);
        } else {
            NotifyDescriptor.Message descriptor = new NotifyDescriptor.Message(message);
            DialogDisplayer.getDefault().notify(descriptor);
        }
    }

    @Override
    public void errorMessageCallback(Object action, String message) {
        if (action == ActionsManager.ACTION_FIX) {
            // Special handling of failures of apply code changes - print into debug output.
            consoleIO.println(message, null, true);
        } else {
            NotifyDescriptor.Message descriptor = new NotifyDescriptor.Message(message, NotifyDescriptor.Message.ERROR_MESSAGE);
            DialogDisplayer.getDefault().notifyLater(descriptor);
        }
    }

    @Override
    public void statusDisplayCallback(Object action, String status) {
        StatusDisplayer.getDefault().setStatusText(status);
    }
    
}
