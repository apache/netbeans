/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
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
