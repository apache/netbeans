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

package org.netbeans.lib.nbjavac.services;

import com.sun.tools.javac.code.Symbol.ClassSymbol;
import com.sun.tools.javac.comp.Enter;
import com.sun.tools.javac.tree.JCTree.JCClassDecl;
import com.sun.tools.javac.tree.JCTree.JCCompilationUnit;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.JCDiagnostic.DiagnosticPosition;
import com.sun.tools.javadoc.main.JavadocEnter;
import com.sun.tools.javadoc.main.Messager;
import org.netbeans.lib.nbjavac.services.NBTreeMaker.IndexedClassDecl;

/**
 * JavadocEnter which doesn't ignore class duplicates unlike the base JavadocEnter
 * Enter - does't ignore duplicates
 * JavadocEnter - ignors duplicates
 * NBJavadocEnter - does't ignore duplicates
 * @author Tomas Zezula
 */
public class NBJavadocEnter extends JavadocEnter {
        
    public static void preRegister(final Context context) {
        context.put(enterKey, new Context.Factory<Enter>() {
            public Enter make(Context c) {
                return new NBJavadocEnter(c);
            }
        });
    }

    private final Messager messager;
    private final CancelService cancelService;

    protected NBJavadocEnter(Context context) {
        super(context);
        messager = Messager.instance0(context);
        cancelService = CancelService.instance(context);
    }

    public @Override void main(com.sun.tools.javac.util.List<JCCompilationUnit> trees) {
        //Todo: Check everytime after the java update that JavaDocEnter.main or Enter.main
        //are not changed.
        this.complete(trees, null);
    }

    @Override
    protected void duplicateClass(DiagnosticPosition pos, ClassSymbol c) {
        messager.error(pos, "duplicate.class", c.fullname);
    }
    
    @Override
    public void visitClassDef(JCClassDecl tree) {
        cancelService.abortIfCanceled();
        super.visitClassDef(tree);
    }

    @Override
    protected int getIndex(JCClassDecl clazz) {
        return clazz instanceof IndexedClassDecl ? ((IndexedClassDecl) clazz).index : -1;
    }
}
