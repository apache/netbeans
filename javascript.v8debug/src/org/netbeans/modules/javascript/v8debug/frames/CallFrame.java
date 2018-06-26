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

package org.netbeans.modules.javascript.v8debug.frames;

import java.util.concurrent.atomic.AtomicBoolean;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.lib.v8debug.V8Frame;
import org.netbeans.lib.v8debug.V8Script;
import org.netbeans.lib.v8debug.vars.ReferencedValue;
import org.netbeans.lib.v8debug.vars.V8Function;
import org.netbeans.lib.v8debug.vars.V8Object;
import org.netbeans.lib.v8debug.vars.V8ScriptValue;
import org.netbeans.lib.v8debug.vars.V8Value;
import org.netbeans.modules.javascript.v8debug.ReferencedValues;
import org.netbeans.modules.javascript.v8debug.V8Debugger;
import org.netbeans.modules.javascript2.debug.NamesTranslator;
import org.netbeans.modules.web.common.sourcemap.SourceMapsTranslator;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

/**
 *
 * @author Martin Entlicher
 */
public final class CallFrame {
    
    private final V8Debugger dbg;
    private final V8Frame frame;
    private final ReferencedValues rvals;
    private final boolean topFrame;
    private NamesTranslator nt;
    private final AtomicBoolean checkNamesTranslator = new AtomicBoolean(true);
    
    public CallFrame(V8Debugger dbg, V8Frame frame, ReferencedValues rvals, boolean topFrame) {
        this.dbg = dbg;
        this.frame = frame;
        this.rvals = rvals;
        this.topFrame = topFrame;
    }

    public V8Frame getFrame() {
        return frame;
    }

    public ReferencedValues getRvals() {
        return rvals;
    }
    
    public boolean isTopFrame() {
        return topFrame;
    }
    
    @CheckForNull
    public V8Script getScript() {
        long ref = frame.getScriptRef();
        V8Value val = rvals.getReferencedValue(ref);
        if (val instanceof V8ScriptValue) {
            return ((V8ScriptValue) val).getScript();
        } else {
            return null;
        }
    }
    
    @CheckForNull
    public String getThisName() {
        ReferencedValue receiver = frame.getReceiver();
        V8Value thisValue;
        if (receiver.hasValue()) {
            thisValue = receiver.getValue();
        } else {
            thisValue = rvals.getReferencedValue(receiver.getReference());
        }
        if (!(thisValue instanceof V8Object)) {
            return null;
        }
        String className = ((V8Object) thisValue).getClassName();
        NamesTranslator nt = getNamesTranslator();
        if (nt != null) {
            className = nt.translate(className);
        }
        return className;
    }
    
    @NonNull
    @NbBundle.Messages("CTL_anonymousFunction=anonymous")
    public String getFunctionName() {
        ReferencedValue functionRV = frame.getFunction();
        V8Value functionValue;
        if (functionRV.hasValue()) {
            functionValue = functionRV.getValue();
        } else {
            functionValue = rvals.getReferencedValue(functionRV.getReference());
        }
        String name;
        if (functionValue instanceof V8Function) {
            V8Function function = (V8Function) functionValue;
            name = function.getName();
            if (name == null || name.isEmpty()) {
                name = function.getInferredName();
            }
        } else {
            name = null;
        }
        if (name == null || name.isEmpty()) {
            name = "["+Bundle.CTL_anonymousFunction()+"]";
        } else {
            NamesTranslator nt = getNamesTranslator();
            if (nt != null) {
                name = nt.translateDeclarationNodeName(name);
            }
        }
        return name;
    }
    
    @CheckForNull
    public NamesTranslator getNamesTranslator() {
        synchronized (checkNamesTranslator) {
            if (checkNamesTranslator.get()) {
                checkNamesTranslator.set(false);
                SourceMapsTranslator smt = dbg.getScriptsHandler().getSourceMapsTranslator();
                if (smt != null) {
                    V8Script script = getScript();
                    if (script != null) {
                        FileObject fo = dbg.getScriptsHandler().getFile(script);
                        int line = (int) frame.getLine();
                        int column = (int) frame.getColumn();
                        if (column < 0) {
                            column = 0;
                        } else {
                            if (line == 0) {
                                column -= dbg.getScriptsHandler().getScriptFirstLineColumnShift(fo);
                            }
                        }
                        nt = NamesTranslator.create(smt, fo, line, column);
                    }
                }
            }
            return nt;
        }
    }
    
    @CheckForNull
    public SourceMapsTranslator.Location getTranslatedLocation() {
        SourceMapsTranslator smt = dbg.getScriptsHandler().getSourceMapsTranslator();
        if (smt != null) {
            V8Script script = getScript();
            if (script != null) {
                FileObject fo = dbg.getScriptsHandler().getFile(script);
                int line = (int) frame.getLine();
                int column = (int) frame.getColumn();
                if (column < 0) {
                    column = 0;
                } else {
                    if (line == 0) {
                        column -= dbg.getScriptsHandler().getScriptFirstLineColumnShift(fo);
                    }
                }
                return smt.getSourceLocation(new SourceMapsTranslator.Location(fo, line, column));
            }
        }
        return null;
    }

}
