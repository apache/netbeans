/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
