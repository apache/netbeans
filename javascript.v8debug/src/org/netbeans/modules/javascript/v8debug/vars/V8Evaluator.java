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

package org.netbeans.modules.javascript.v8debug.vars;

import org.netbeans.lib.v8debug.V8Command;
import org.netbeans.lib.v8debug.V8Request;
import org.netbeans.lib.v8debug.V8Response;
import org.netbeans.lib.v8debug.commands.Evaluate;
import org.netbeans.lib.v8debug.vars.V8Boolean;
import org.netbeans.lib.v8debug.vars.V8Function;
import org.netbeans.lib.v8debug.vars.V8Number;
import org.netbeans.lib.v8debug.vars.V8Object;
import org.netbeans.lib.v8debug.vars.V8String;
import org.netbeans.lib.v8debug.vars.V8Value;
import org.netbeans.modules.javascript.v8debug.V8Debugger;
import org.netbeans.modules.javascript.v8debug.frames.CallFrame;
import org.netbeans.modules.javascript2.debug.NamesTranslator;

/**
 *
 * @author Martin Entlicher
 */
public class V8Evaluator {
    
    public static V8Value evaluate(V8Debugger debugger, String expression) throws EvaluationError {
        final V8Value[] valueRef = new V8Value[] { null };
        final String[] errRef = new String[] { null };
        NamesTranslator nt = null;
        CallFrame cf = debugger.getCurrentFrame();
        if (cf != null) {
            nt = cf.getNamesTranslator();
        }
        if (nt != null) {
            expression = nt.reverseTranslate(expression);
        }
        V8Request request =
                debugger.sendCommandRequest(V8Command.Evaluate,
                                            new Evaluate.Arguments(expression),
                                            new V8Debugger.CommandResponseCallback() {
            @Override
            public void notifyResponse(V8Request request, V8Response response) {
                synchronized (valueRef) {
                    if (response != null) {
                        if (!response.isSuccess()) {
                            errRef[0] = response.getErrorMessage();
                        } else {
                            valueRef[0] = ((Evaluate.ResponseBody) response.getBody()).getValue();
                        }
                    }
                    valueRef.notifyAll();
                }
            }
        });
        if (request == null) {
            return null;
        }
        synchronized (valueRef) {
            if (valueRef[0] == null && errRef[0] == null) {
                try {
                    valueRef.wait();
                } catch (InterruptedException ex) {
                    throw new EvaluationError(ex.getLocalizedMessage());
                }
            }
        }
        if (errRef[0] != null) {
            throw new EvaluationError(errRef[0]);
        }
        return valueRef[0];
    }
    
    public static String getStringValue(V8Value value) {
        switch (value.getType()) {
            case Boolean:
                return Boolean.toString(((V8Boolean) value).getValue());
            case Function:
                V8Function function = (V8Function) value;
                String source = function.getSource();
                if (source != null) {
                    return source;
                }
                String name = function.getName();
                if (name == null || name.isEmpty()) {
                    name = function.getInferredName();
                }
                return "function "+name+"()";
            case Null:
                return String.valueOf((Object) null);
            case Number:
                V8Number n = (V8Number) value;
                switch (n.getKind()) {
                    case Double:
                        return Double.toString(n.getDoubleValue());
                    case Long:
                        return Long.toString(n.getLongValue());
                    default:
                        throw new IllegalStateException("Unknown kind: "+n.getKind());
                }
            case Object:
                V8Object o = (V8Object) value;
                StringBuilder sb = new StringBuilder("(");
                sb.append(o.getClassName());
                sb.append(')');
                if (o.getText() != null) {
                    sb.append(' ');
                    sb.append(o.getText());
                }
                V8Object.Array arr = o.getArray();
                if (arr != null) {
                    sb.append(" length=");
                    sb.append(arr.getLength());
                }
                /*if (o.getProperties() != null) {
                    Map<String, V8Object.Property> properties = o.getProperties();
                    String newLine = System.getProperty("line.separator");
                    for (String propName : properties.keySet()) {
                        sb.append(newLine);
                        sb.append("  ");
                        sb.append(propName);
                        sb.append(" = ");
                        V8Object.Property property = properties.get(propName);
                        sb.append('(');
                        sb.append(property.getType());
                        sb.append(") ref: ");
                        sb.append(property.getReference());
                    }
                }*/
                return sb.toString();
            case String:
                return "\""+((V8String) value).getValue()+"\"";
            case Undefined:
                return "undefined";
            default:
                if (value.getText() != null) {
                    return value.getText();
                }
                throw new IllegalStateException("Unknown value type: "+value.getType());
        }
    }
    
    public static String getStringType(V8Value value) {
        V8Value.Type type = value.getType();
        if (type == V8Value.Type.Object) {
            V8Object obj = (V8Object) value;
            return obj.getClassName();
        }
        return type.toString();
    }
    
}
