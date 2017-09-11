/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.lib.nbjavac.services;

import com.sun.tools.javac.code.Symbol.ClassSymbol;
import com.sun.tools.javac.code.Symtab;
import com.sun.tools.javac.code.Types;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.JCClassDecl;
import com.sun.tools.javac.tree.JCTree.JCCompilationUnit;
import com.sun.tools.javac.tree.JCTree.JCExpression;
import com.sun.tools.javac.tree.JCTree.JCModifiers;
import com.sun.tools.javac.tree.JCTree.JCTypeParameter;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.Name;
import com.sun.tools.javac.util.Names;

/**
 *
 * @author lahvac
 */
public class NBTreeMaker extends TreeMaker {

    public static void preRegister(Context context) {
        context.put(treeMakerKey, new Context.Factory<TreeMaker>() {
            public TreeMaker make(Context c) {
                return new NBTreeMaker(c);
            }
        });
    }

    private final Names names;
    private final Types types;
    private final Symtab syms;

    protected NBTreeMaker(Context context) {
        super(context);
        this.names = Names.instance(context);
        this.types = Types.instance(context);
        this.syms = Symtab.instance(context);
    }

    protected NBTreeMaker(JCCompilationUnit toplevel, Names names, Types types, Symtab syms) {
        super(toplevel, names, types, syms);
        this.names = names;
        this.types = types;
        this.syms = syms;
    }

    @Override
    public TreeMaker forToplevel(JCCompilationUnit toplevel) {
        return new NBTreeMaker(toplevel, names, types, syms);
    }

    @Override
    public IndexedClassDecl ClassDef(JCModifiers mods, Name name, List<JCTypeParameter> typarams, JCExpression extending, List<JCExpression> implementing, List<JCTree> defs) {
        IndexedClassDecl result = new IndexedClassDecl(mods, name, typarams, extending, implementing, defs, null);

        result.pos = pos;

        return result;
    }

    public static class IndexedClassDecl extends JCClassDecl {
        public int index;
        protected IndexedClassDecl(JCModifiers mods,
                                   Name name,
                                   List<JCTypeParameter> typarams,
                                   JCExpression extending,
                                   List<JCExpression> implementing,
                                   List<JCTree> defs,
                                   ClassSymbol sym) {
            super(mods, name, typarams, extending, implementing, defs, sym);
            this.index = -1;
        }
    }
}
