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

package org.netbeans.modules.debugger.jpda;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.api.io.InputOutput;
import org.netbeans.modules.debugger.jpda.console.DebuggerOutput;
import org.netbeans.spi.debugger.ContextProvider;

/**
 *
 * @author Martin Entlicher
 */
public final class DebuggerConsoleIO {
    
    private final JPDADebuggerImpl debugger;
    private final DebuggerOutput output;
    
    DebuggerConsoleIO(JPDADebuggerImpl debugger, ContextProvider lookupProvider) {
        this.debugger = debugger;
        this.output = new DebuggerOutput(debugger, lookupProvider);
    }
    
    @CheckForNull
    public InputOutput getIO() {
        return output.getIOManager().getIO();
    }
    
    public void println(String text, Line line) {
        output.getIOManager().println(text, line);
    }
    
    public void println(String text, Line line, boolean important) {
        output.getIOManager().println(text, line, important);
    }
    
    public final static class Line {
        
        private final String url;
        private final int lineNumber;
        private final Reference<JPDADebugger> debuggerRef;
        
        public Line (String url, int lineNumber, JPDADebugger debuggerTimeStamp) {
            this.url = url;
            this.lineNumber = lineNumber;
            this.debuggerRef = new WeakReference<>(debuggerTimeStamp);
        }
        
        public void show () {
            EditorContextBridge.getContext().showSource (url, lineNumber, debuggerRef.get());
        }
    }
}
