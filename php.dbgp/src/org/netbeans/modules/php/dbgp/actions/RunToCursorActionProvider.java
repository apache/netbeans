/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.php.dbgp.actions;

import java.util.Collections;
import java.util.Set;
import org.netbeans.api.debugger.ActionsManager;
import org.netbeans.modules.php.dbgp.DebugSession;
import org.netbeans.modules.php.dbgp.SessionId;
import org.netbeans.modules.php.dbgp.breakpoints.Utils;
import org.netbeans.modules.php.dbgp.packets.BrkpntCommandBuilder;
import org.netbeans.modules.php.dbgp.packets.BrkpntSetCommand;
import org.netbeans.modules.php.dbgp.packets.RunCommand;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.spi.debugger.ui.EditorContextDispatcher;
import org.openide.text.Line;

/**
 * @author ads
 *
 */
public class RunToCursorActionProvider extends AbstractActionProvider {

    public RunToCursorActionProvider(ContextProvider contextProvider) {
        super(contextProvider);
    }

    @Override
    public void doAction(Object action) {
        SessionId id = getSessionId();
        if (id == null) {
            return;
        }
        hideSuspendAnnotations();
        DebugSession session = getSession();
        if (session == null) {
            return;
        }
        Line line = Utils.getCurrentLine();
        if (line == null) {
            return;
        }

        BrkpntSetCommand command = BrkpntCommandBuilder.buildLineBreakpoint(
                id, session.getTransactionId(),
                EditorContextDispatcher.getDefault().getCurrentFile(),
                line.getLineNumber());
        command.setTemporary(true);
        session.sendCommandLater(command);

        hideSuspendAnnotations();
        RunCommand runCommand = new RunCommand(session.getTransactionId());
        session.sendCommandLater(runCommand);
    }

    @Override
    public Set getActions() {
        return Collections.singleton(ActionsManager.ACTION_RUN_TO_CURSOR);
    }

}
