/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2016 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2016 Sun Microsystems, Inc.
 */
package org.netbeans.modules.debugger.jpda.projects;

import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaFileObject;
import org.netbeans.api.debugger.jpda.InvalidExpressionException;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.TreePathHandle;
import org.openide.filesystems.FileObject;

/**
 * Compiles an extra code snippet in a Java source.
 * 
 * @author Martin
 */
class CodeSnippetCompiler {
    
    private static final String INVOCATION_CLASS_NAME = "OrgNetBeansDebuggerInvocationInnerClass";  // NOI18N
    private static final AtomicLong LAST_CLASS_ID = new AtomicLong(0);
    
    static ClassToInvoke compileToClass(CompilationInfo ci, String code, int codeOffset,
                                        JavaSource js, FileObject fo, int line,
                                        TreePath treePath, Tree tree,
                                        boolean staticContext) throws InvalidExpressionException {
        TreePathHandle tph = TreePathHandle.create(treePath, ci);
        String className = INVOCATION_CLASS_NAME + LAST_CLASS_ID.incrementAndGet();
        IntroduceClass introClass = new IntroduceClass(code, codeOffset, staticContext);
        boolean success = introClass.computeIntroduceMethod(tph, ci, treePath, tree);
        if (!success) {
            return null;
        }
        String methodInvoke = introClass.getMethodInvoke();
        String fullCode;
        Map<String, byte[]> compiledClass;
        try {
            fullCode = introClass.computeIntroduceClass(className, fo);
            DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
            compiledClass = CodeCompiler.compile(fo, js, fullCode, diagnostics);
            if (compiledClass == null) {
                return null;
            }
            for (Diagnostic<? extends JavaFileObject> diag : diagnostics.getDiagnostics()) {
                if (Diagnostic.Kind.ERROR.equals(diag.getKind()) &&
                    diag.getSource().isNameCompatible(className, JavaFileObject.Kind.CLASS)) {
                    
                    throw new InvalidExpressionException(diag.getMessage(null));
                }
            }
        } catch (IOException ioe) {
            throw new InvalidExpressionException(ioe);
        }
        String classFQN = null;
        Map<String, byte[]> innerClasses = null;
        for (String ccName : compiledClass.keySet()) {
            if (ccName.endsWith(className)) {
                classFQN = ccName;
            } else if (ccName.contains(className)) {
                // A sub-class
                if (innerClasses == null) {
                    innerClasses = new LinkedHashMap<>();
                }
                innerClasses.put(ccName, compiledClass.get(ccName));
            }
        }
        if (classFQN == null) {
            return null;
        }
        return new ClassToInvoke(classFQN,
                                 compiledClass.get(classFQN),
                                 "new "+className+"()."+methodInvoke,
                                 innerClasses);
    }
}
