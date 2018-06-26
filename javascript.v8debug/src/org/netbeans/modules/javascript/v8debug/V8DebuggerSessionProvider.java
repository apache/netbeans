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

package org.netbeans.modules.javascript.v8debug;

import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.spi.debugger.SessionProvider;
import org.openide.util.NbBundle;

/**
 *
 * @author Martin Entlicher
 */
@SessionProvider.Registration(path=V8DebuggerSessionProvider.DEBUG_INFO)
public class V8DebuggerSessionProvider extends SessionProvider {
    
    public static final String SESSION_NAME = "javascript-v8session";           // NOI18N
    static final String DEBUG_INFO = "javascript-v8debuginfo";                  // NOI18N
    
    private final ContextProvider context;
    
    public V8DebuggerSessionProvider(ContextProvider context) {
        this.context = context;
    }

    @NbBundle.Messages({"# {0} - host name", "# {1} - port number",
                        "CTL_V8RemoteAttach=Node.js at {0}:{1}",
                        "# {0} - port number",
                        "CTL_V8LocalAttach=Node.js at port {0}"})
    public static String getSessionName(@NullAllowed String host, int port) {
        if (host != null && !host.isEmpty()) {
            return Bundle.CTL_V8RemoteAttach(host, Integer.toString(port));
        } else {
            return Bundle.CTL_V8LocalAttach(Integer.toString(port));
        }
    }
    
    @Override
    public String getSessionName() {
        V8Debugger dbg = context.lookupFirst(null, V8Debugger.class);
        return getSessionName(dbg.getHost(), dbg.getPort());
    }

    @Override
    public String getLocationName() {
        return null;
    }

    @Override
    public String getTypeID() {
        return SESSION_NAME;
    }

    @Override
    public Object[] getServices() {
        return new Object[] {};
    }
}
