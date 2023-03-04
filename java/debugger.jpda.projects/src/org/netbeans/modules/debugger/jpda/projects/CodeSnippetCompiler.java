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
                if (Diagnostic.Kind.ERROR == diag.getKind() &&
                    diag.getSource() != null &&
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
