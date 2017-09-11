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
package org.netbeans.modules.java.source.parsing;

import com.sun.tools.javac.util.JCDiagnostic;
import com.sun.tools.javac.util.JCDiagnostic.DiagnosticFlag;
import java.util.ArrayList;
import java.util.List;
import javax.tools.Diagnostic;
import org.netbeans.modules.java.source.parsing.CompilationInfoImpl.RichDiagnostic;

/**
 *
 * @author lahvac
 */
public class Hacks {
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
        return diags.toArray(new Diagnostic[diags.size()]);
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
