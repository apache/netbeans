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

package org.netbeans.modules.web.debug.watchesfiltering;

import org.netbeans.api.debugger.Watch;
import org.netbeans.api.debugger.jpda.InvalidExpressionException;
import org.netbeans.api.debugger.jpda.Variable;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.openide.util.WeakListeners;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import org.openide.util.NbBundle;

/**
 * Represents a JSP EL watch.
 *
 * @author Maros Sandor
 */
public class JspElWatch implements PropertyChangeListener {

    private final Watch     watch;
    
    private boolean         evaluated = false;
    private JPDADebugger    debugger;
    private Variable        variable;
    private Exception       exception;

    public JspElWatch(Watch w, JPDADebugger debugger) {
        watch = w;
        this.debugger = debugger;
        w.addPropertyChangeListener((PropertyChangeListener) WeakListeners.create(PropertyChangeListener.class, this, w));    
    }

    public String getExpression () {
        return watch.getExpression();
    }

    public String getType() {
        if (!watch.isEnabled()) {
            return "";
        }
        if (!evaluated) {
            evaluate();
        }
        return variable == null ? "" : variable.getType();
    }

    public String getValue() {
        if (!watch.isEnabled()) {
            return NbBundle.getMessage(JspElWatch.class, "CTL_WatchDisabled");
        }
        if (!evaluated) {
            evaluate();
        }
        return variable == null ? "" : variable.getValue();
    }

    public String getExceptionDescription() {
        if (!watch.isEnabled()) {
            return null;
        }
        if (!evaluated) {
            evaluate();
        }
        return exception == null ? null : exception.getMessage();
    }

    public String getToStringValue() throws InvalidExpressionException {
        return getValue().toString();
    }

    public Watch getWatch() {
        return watch;
    }
    
    private synchronized void evaluate() {
        String text = watch.getExpression ();
        text = text.replace("\"", "\\\"");
        text = "pageContext.getExpressionEvaluator().evaluate(\"" + text + "\", "+
                        "java.lang.String.class, "+
                        "((javax.servlet.jsp.PageContext) pageContext).getVariableResolver(), "+
                        "null)";
        try {
            variable = debugger.evaluate(text);
            exception = null;
        } catch (Exception e) {
            exception = e;
        } finally {
            evaluated = true;
        }
    }

    public void propertyChange(PropertyChangeEvent evt) {
        setUnevaluated();
    }
    
    public void setUnevaluated() {
        evaluated = false;
    }
    
}
