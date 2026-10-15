/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.java.source.parsing;

import com.sun.tools.javac.util.JCDiagnostic;
import com.sun.tools.javac.util.JCDiagnostic.DiagnosticFlag;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.tools.Diagnostic;
import org.netbeans.modules.java.source.parsing.CompilationInfoImpl.RichDiagnostic;

/**
 *
 * @author lahvac
 */
public class Hacks {
    private static final Logger LOG = Logger.getLogger(Hacks.class.getName());

    public static boolean isSyntaxError(Diagnostic<?> d) {
        JCDiagnostic jcd = getJCDiagnostic(d);
        if (jcd == null) {
            return false;
        }
        return jcd.isFlagSet(DiagnosticFlag.SYNTAX);
    }
    
    public static Diagnostic[] getNestedDiagnostics(Diagnostic<?> d) {
        List<Diagnostic> diags = new ArrayList<>();
        getNestedDiagnostics(d, diags);
        if (diags.isEmpty()) {
            return null;
        }
        return diags.toArray(new Diagnostic[0]);
    }
    
    private static void getNestedDiagnostics(Diagnostic<?> d, List<Diagnostic> diags) {
        JCDiagnostic jcd = getJCDiagnostic(d);
        if (jcd == null) {
            return;
        }
        Object[] args = jcd.getArgs();
        if (args == null || args.length == 0) {
            return;
        }
        for (Object o : args) {
            if (o instanceof Diagnostic) {
                diags.add((Diagnostic)o);
                getNestedDiagnostics((Diagnostic)o, diags);
                break;
            }
        }
    }
    
    private static JCDiagnostic getJCDiagnostic(Diagnostic<?> d) {
        if (d instanceof JCDiagnostic) {
            return ((JCDiagnostic)d);
        } else if (d instanceof RichDiagnostic && ((RichDiagnostic) d).getDelegate() instanceof JCDiagnostic) {
            return (JCDiagnostic)((RichDiagnostic)d).getDelegate();
        } else if ("org.netbeans.modules.java.source.parsing.CompilationInfoImpl$DiagnosticListenerImpl$D".equals(d.getClass().getName())) {
            try {
                Field delegate = d.getClass().getDeclaredField("delegate");
                delegate.setAccessible(true);
                return getJCDiagnostic((Diagnostic<?>) delegate.get(d));
            } catch (Exception ex) {
                LOG.log(Level.FINE, null, ex);
            }
        }
        return null;
    }
    
    /**
     * Extracts diagnostic params from a diagnostic. Gets under hood of Javac
     * Diagnostic objects and extracts parameters which are otherwise just used
     * to produce a message. <b>Keep in mind that the positions and types of parameters
     * may change in each nbjavac update!</b>
     * @param d diagnostic
     * @param index parameter index to extract
     * @return parameter value, null if index is out of range
     */
    public static Object getDiagnosticParam(Diagnostic<?> d, int index) {
        JCDiagnostic jcd = getJCDiagnostic(d);
        if (jcd == null) {
            return null;
        }
        Object[] args = jcd.getArgs();
        if (args == null || args.length <= index) {
            return null;
        }
        return args[index];
    }
    
}
