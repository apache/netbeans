/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */

package org.netbeans.modules.debugger.jpda.jsui.vars.tooltip;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.concurrent.CancellationException;
import javax.swing.SwingUtilities;
import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.api.debugger.Session;
import org.netbeans.api.debugger.jpda.CallStackFrame;
import org.netbeans.api.debugger.jpda.InvalidExpressionException;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.api.debugger.jpda.ObjectVariable;
import org.netbeans.api.debugger.jpda.Variable;
import org.netbeans.editor.ext.ToolTipSupport;
import org.netbeans.modules.debugger.jpda.js.JSUtils;
import org.netbeans.modules.debugger.jpda.js.vars.DebuggerSupport;
import org.netbeans.modules.debugger.jpda.js.vars.JSVariable;
import org.netbeans.modules.javascript2.debug.ui.tooltip.AbstractJSToolTipAnnotation;
import org.openide.util.Pair;

/**
 *
 * @author Martin
 */
public final class ToolTipAnnotation extends AbstractJSToolTipAnnotation {
    
    @Override
    protected void handleToolTipClose(DebuggerEngine engine, ToolTipSupport tts) {
        JPDADebugger d = engine.lookupFirst(null, JPDADebugger.class);
        if (d == null) {
            return ;
        }
        PropertyChangeListener l = (PropertyChangeEvent evt) -> {
            int state = ((Integer) evt.getNewValue());
            if (JPDADebugger.STATE_DISCONNECTED == state ||
                    JPDADebugger.STATE_RUNNING == state) {
                SwingUtilities.invokeLater(() ->
                        tts.setToolTipVisible(false)
                );
            }
        };
        d.addPropertyChangeListener(JPDADebugger.PROP_STATE, l);
        tts.addPropertyChangeListener(pl -> {
            if (ToolTipSupport.PROP_STATUS.equals(pl.getPropertyName()) &&
                    !tts.isToolTipVisible()) {
                d.removePropertyChangeListener(JPDADebugger.PROP_STATE, l);
            }
        });
    }
    
    @Override
    protected Pair<String, Object> evaluate(String expression, DebuggerEngine engine) throws CancellationException {
        Session session = engine.lookupFirst(null, Session.class);
        if (engine != session.getEngineForLanguage(JSUtils.JS_STRATUM)) {
            return null;
        }
        JPDADebugger d = engine.lookupFirst(null, JPDADebugger.class);
        if (d == null) {
            return null;
        }
        CallStackFrame frame = d.getCurrentCallStackFrame();
        if (frame == null) {
            return null;
        }
        String toolTipText;
        JSVariable jsresult = null;
        try {
            Variable result = DebuggerSupport.evaluate(d, frame, expression);
            if (result == null) {
                throw new CancellationException();
            }
            if (result instanceof ObjectVariable) {
                jsresult = JSVariable.createIfScriptObject(d, (ObjectVariable) result, expression);
            }
            if (jsresult != null) {
                toolTipText = expression + " = " + jsresult.getValue();
            } else {
                toolTipText = expression + " = " + DebuggerSupport.getVarValue(d, result);
            }
        } catch (InvalidExpressionException ex) {
            toolTipText = expression + " = >" + ex.getMessage () + "<";
        }
        return Pair.of(toolTipText, (Object) jsresult);
    }
    
}
