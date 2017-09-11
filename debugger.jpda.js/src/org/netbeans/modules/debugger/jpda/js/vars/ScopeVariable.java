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

package org.netbeans.modules.debugger.jpda.js.vars;

import java.util.Arrays;
import java.util.List;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.api.debugger.jpda.LocalVariable;
import org.netbeans.api.debugger.jpda.ObjectVariable;

/**
 *
 * @author Martin Entlicher
 */
public final class ScopeVariable {
    
    private final JPDADebugger debugger;
    private final LocalVariable lv;
    private final String name;
    private JSVariable[] scopeVars;
    
    private ScopeVariable(JPDADebugger debugger, LocalVariable lv) {
        this.debugger = debugger;
        this.lv = lv;
        //((ObjectVariable) lv).getClassType().getName();
        String vv = DebuggerSupport.getVarValue(debugger, lv);
        if (vv.startsWith("[") && vv.endsWith("]")) {
            vv = vv.substring(1, vv.length() - 1).trim();
        }
        if (vv.toLowerCase().startsWith("object")) {
            vv = vv.substring("object".length()).trim();
        }
        vv = Character.toUpperCase(vv.charAt(0)) + vv.substring(1);
        name = vv;
    }
    
    public static ScopeVariable create(JPDADebugger debugger, LocalVariable lv) {
        return new ScopeVariable(debugger, lv);
    }
    
    public String getName() {
        return name;
    }
    
    public JSVariable[] getScopeVars() {
        if (scopeVars == null) {
            scopeVars = JSVariable.createScopeVars(debugger, lv);
        }
        return scopeVars;
    }
}
