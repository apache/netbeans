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

import com.sun.tools.javac.code.Symtab;
import com.sun.tools.javac.code.Types;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.JCCompilationUnit;
import com.sun.tools.javac.tree.JCTree.JCPackageDecl;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.Names;
import java.util.function.Consumer;

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
    private       Consumer<JCPackageDecl> packageCreatedCallback;

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
    public JCPackageDecl PackageDecl(List<JCTree.JCAnnotation> annotations, JCTree.JCExpression pid) {
        JCPackageDecl pack = super.PackageDecl(annotations, pid);

        if (packageCreatedCallback != null) {
            packageCreatedCallback.accept(pack);
        }

        return pack;
    }

    public Consumer<JCPackageDecl> setPackageCreatedCallback(Consumer<JCPackageDecl> callback) {
        Consumer<JCPackageDecl> prev = packageCreatedCallback;

        packageCreatedCallback = callback;
        return prev;
    }
}
