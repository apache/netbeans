/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.web.javascript.debugger.annotation;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CancellationException;
import javax.swing.BorderFactory;
import javax.swing.JEditorPane;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import javax.swing.text.StyledDocument;
import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.Session;
import org.netbeans.editor.EditorUI;
import org.netbeans.editor.PopupManager;
import org.netbeans.editor.Utilities;
import org.netbeans.editor.ext.ToolTipSupport;
import org.netbeans.modules.javascript2.debug.ui.tooltip.AbstractJSToolTipAnnotation;
import org.netbeans.modules.web.javascript.debugger.eval.Evaluator;
import org.netbeans.modules.web.javascript.debugger.locals.VariablesModel;
import org.netbeans.modules.web.javascript.debugger.locals.VariablesModel.ScopedRemoteObject;
import org.netbeans.modules.web.javascript.debugger.watches.WatchesModel;
import org.netbeans.modules.web.webkit.debugging.api.Debugger;
import org.netbeans.modules.web.webkit.debugging.api.debugger.CallFrame;
import org.netbeans.modules.web.webkit.debugging.api.debugger.RemoteObject;
import org.netbeans.modules.web.webkit.debugging.api.debugger.RemoteObject.Type;
import org.netbeans.spi.debugger.ui.EditorContextDispatcher;
import org.openide.cookies.EditorCookie;
import org.openide.loaders.DataObject;
import org.openide.text.Annotation;
import org.openide.text.DataEditorSupport;
import org.openide.text.Line;
import org.openide.text.NbDocument;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.Pair;
import org.openide.util.RequestProcessor;


/**
 * @author ads
 *
 */
@NbBundle.Messages({
    "# {0} - variable name",
    "var.undefined={0} is not defined"
})
public class ToolTipAnnotation extends AbstractJSToolTipAnnotation
{
    
    @Override
    protected void handleToolTipClose(DebuggerEngine engine, ToolTipSupport tts) {
        Debugger d = engine.lookupFirst(null, Debugger.class);
        if (d == null || !d.isSuspended()) {
            return ;
        }
        handleToolTipClose(d, tts);
    }
    
    static void handleToolTipClose(Debugger d, final ToolTipSupport tts) {
        Debugger.Listener dl = new Debugger.Listener() {
            @Override
            public void paused(List<CallFrame> callStack, String reason) {}

            @Override
            public void resumed() {
                doClose();
            }

            @Override
            public void reset() {
                doClose();
            }

            @Override
            public void enabled(boolean enabled) {
                if (!enabled) {
                    doClose();
                }
            }
            
            private void doClose() {
                tts.setToolTipVisible(false);
            }
        };
        d.addListener(dl);
        tts.addPropertyChangeListener(pl -> {
            if (ToolTipSupport.PROP_STATUS.equals(pl.getPropertyName()) &&
                    !tts.isToolTipVisible()) {
                d.removeListener(dl);
            }
        });
    }
    
    @Override
    protected Pair<String, Object> evaluate(String expression, DebuggerEngine engine) throws CancellationException {
        Debugger d = engine.lookupFirst(null, Debugger.class);
        if (d == null || !d.isSuspended()) {
            return null;
        }
        CallFrame currentCallFrame = d.getCurrentCallFrame();
        VariablesModel.ScopedRemoteObject sv = Evaluator.evaluateExpression(currentCallFrame, expression, true);
        Object tooltipVariable = null;
        String tooltipText;
        if (sv != null) {
            RemoteObject var = sv.getRemoteObject();
            String value = getStringValue(var);
            Type type = var.getType();
            if (type == Type.OBJECT) {
                tooltipVariable = sv;
                // TODO: add obj ID
            }
            if (type != Type.UNDEFINED) {
                tooltipText = expression + " = " + value;
            } else {
                tooltipText = var.getDescription();
                if (tooltipText == null) {
                    tooltipText = Bundle.var_undefined(expression);
                }
            }
        } else {
            throw new CancellationException();
        }
        return Pair.of(tooltipText, tooltipVariable);
    }
    
    static String getStringValue(RemoteObject var) {
        String value = var.getValueAsString();
        Type type = var.getType();
        switch (type) {
            case STRING:
                value = "\"" + value + "\"";
                break;
            case FUNCTION:
                value = var.getDescription();
                break;
            case OBJECT:
                String clazz = var.getClassName();
                if (clazz == null) {
                    clazz = type.getName();
                }
                if (value.isEmpty()) {
                    value = var.getDescription();
                }
                value = "("+clazz+") "+value;
                break;
        }
        return value;
    }

}
