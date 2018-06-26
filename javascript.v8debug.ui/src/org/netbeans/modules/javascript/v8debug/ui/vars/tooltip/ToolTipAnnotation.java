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

package org.netbeans.modules.javascript.v8debug.ui.vars.tooltip;

import java.util.concurrent.CancellationException;
import javax.swing.SwingUtilities;
import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.api.debugger.Session;
import org.netbeans.editor.ext.ToolTipSupport;
import org.netbeans.lib.v8debug.V8Frame;
import org.netbeans.lib.v8debug.vars.V8Object;
import org.netbeans.lib.v8debug.vars.V8Value;
import org.netbeans.modules.javascript.v8debug.V8Debugger;
import org.netbeans.modules.javascript.v8debug.frames.CallFrame;
import org.netbeans.modules.javascript.v8debug.vars.EvaluationError;
import org.netbeans.modules.javascript.v8debug.vars.V8Evaluator;
import org.netbeans.modules.javascript.v8debug.vars.Variable;
import org.netbeans.modules.javascript.v8debug.ui.vars.models.VariablesModel;
import org.netbeans.modules.javascript2.debug.ui.tooltip.AbstractJSToolTipAnnotation;
import org.openide.util.Pair;

/**
 *
 * @author Martin Entlicher
 */
public class ToolTipAnnotation extends AbstractJSToolTipAnnotation {

    @Override
    protected void handleToolTipClose(DebuggerEngine engine, final ToolTipSupport tts) {
        V8Debugger debugger = engine.lookupFirst(null, V8Debugger.class);
        if (debugger == null) {
            return ;
        }
        handleToolTipClose(debugger, tts);
    }
    
    public static void handleToolTipClose(V8Debugger debugger, final ToolTipSupport tts) {
        V8Debugger.Listener listener = new V8Debugger.Listener() {
            @Override
            public void notifySuspended(boolean suspended) {
                if (!suspended) {
                    doClose();
                }
            }

            @Override
            public void notifyCurrentFrame(CallFrame cf) {
                doClose();
            }

            @Override
            public void notifyFinished() {
                doClose();
            }
            
            private void doClose() {
                SwingUtilities.invokeLater(() ->
                    tts.setToolTipVisible(false)
                );
            }
        };
        debugger.addListener(listener);
        tts.addPropertyChangeListener(pl -> {
            if (ToolTipSupport.PROP_STATUS.equals(pl.getPropertyName()) &&
                    !tts.isToolTipVisible()) {
                debugger.removeListener(listener);
            }
        });
    }
    
    @Override
    protected Pair<String, Object> evaluate(String expression, DebuggerEngine engine) throws CancellationException {
        String toolTipText;
        Variable var = null;
        V8Debugger debugger = engine.lookupFirst(null, V8Debugger.class);
        if (debugger == null || !debugger.isSuspended()) {
            return null;
        }
        try {
            V8Value value = V8Evaluator.evaluate(debugger, expression);
            if (value == null) {
                throw new CancellationException();
            }
            toolTipText = expression + " = " + V8Evaluator.getStringValue(value);
            if (VariablesModel.hasChildren(value)) {
                var = new Variable(Variable.Kind.LOCAL, expression, value.getHandle(), value, false);
            }
        } catch (EvaluationError ex) {
            toolTipText = expression + " = >" + ex.getMessage () + "<";
        }
        return Pair.of(toolTipText, (Object) var);
    }
    
}
