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
package org.netbeans.lib.v8debug;

import java.util.Map;
import org.netbeans.lib.v8debug.vars.ReferencedValue;
import org.netbeans.lib.v8debug.vars.V8Function;

/**
 *
 * @author Martin Entlicher
 */
public final class V8Frame {
    
    private final PropertyLong index;
    private final ReferencedValue receiver;
    private final ReferencedValue<V8Function> func;
    private final long scriptRef;
    private final boolean constructCall;
    private final boolean atReturn;
    private final boolean debuggerFrame;
    private final Map<String, ReferencedValue> argumentRefs;
    private final Map<String, ReferencedValue> localRefs;
    private final long position;
    private final long line;
    private final long column;
    private final String sourceLineText;
    private final V8Scope[] scopes;
    private final String text;
    
    public V8Frame(Long index, ReferencedValue receiver, ReferencedValue<V8Function> func, long scriptRef,
                   boolean constructCall, boolean atReturn, boolean debuggerFrame,
                   Map<String, ReferencedValue> argumentRefs,
                   Map<String, ReferencedValue> localRefs,
                   long position, long line, long column, String sourceLineText,
                   V8Scope[] scopes, String text) {
        this.index = new PropertyLong(index);
        this.receiver = receiver;
        this.func = func;
        this.scriptRef = scriptRef;
        this.constructCall = constructCall;
        this.atReturn = atReturn;
        this.debuggerFrame = debuggerFrame;
        this.argumentRefs = argumentRefs;
        this.localRefs = localRefs;
        this.position = position;
        this.line = line;
        this.column = column;
        this.sourceLineText = sourceLineText;
        this.scopes = scopes;
        this.text = text;
    }

    public PropertyLong getIndex() {
        return index;
    }

    public ReferencedValue getReceiver() {
        return receiver;
    }

    public ReferencedValue<V8Function> getFunction() {
        return func;
    }

    public long getScriptRef() {
        return scriptRef;
    }

    public boolean isConstructCall() {
        return constructCall;
    }

    public boolean isAtReturn() {
        return atReturn;
    }

    public boolean isDebuggerFrame() {
        return debuggerFrame;
    }

    public Map<String, ReferencedValue> getArgumentRefs() {
        return argumentRefs;
    }

    public Map<String, ReferencedValue> getLocalRefs() {
        return localRefs;
    }

    public long getPosition() {
        return position;
    }

    public long getLine() {
        return line;
    }

    public long getColumn() {
        return column;
    }

    public String getSourceLineText() {
        return sourceLineText;
    }

    public V8Scope[] getScopes() {
        return scopes;
    }
    
    public String getText() {
        return text;
    }
}
