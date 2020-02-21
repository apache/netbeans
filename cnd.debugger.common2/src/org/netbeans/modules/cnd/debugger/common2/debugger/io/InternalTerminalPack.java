/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.cnd.debugger.common2.debugger.io;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.pty.Pty;
import org.netbeans.modules.nativeexecution.api.pty.PtySupport;
import org.netbeans.modules.terminal.api.IOTerm;
import org.openide.windows.InputOutput;

/**
 *
 */
class InternalTerminalPack extends IOPack {
    private Pty pty = null;
    private final InputOutput io;
    private String slaveName = null;
    
    private static final Boolean fixEraseKeyInTerminal = Boolean.valueOf(System.getProperty("fixEraseKeyInTerminal", "true")); // NOI18N;

    public InternalTerminalPack(TermComponent console, InputOutput io, ExecutionEnvironment exEnv) {
        super(console, exEnv, false);
        this.io = io;
    }

    @Override
    public boolean start() {
        try {
            pty = PtySupport.allocate(exEnv);
        } catch (IOException ex) {
            slaveName = null;
            return false;
        }
        PtySupport.connect(io, pty);
        slaveName = pty.getSlaveName();
        if (fixEraseKeyInTerminal) {
            PtySupport.setBackspaceAsEraseChar(exEnv, slaveName);
        }
        return true;
    }

    @Override
    public String getSlaveName() {
        return slaveName;
    }

    @Override
    public void switchTo() {
        super.switchTo();
        // show output
        io.select();
    }

    @Override
    public void close() {
        IOTerm.disconnect(io, null);
        if (pty != null) {
            try {
                pty.close();
            } catch (IOException ex) {
                Logger.getLogger(InternalTerminalPack.class.getName()).log(Level.INFO, "Pty is already closed: ", ex);
            }
        }
    }
}
