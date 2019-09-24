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

import com.sun.source.doctree.DocCommentTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.util.TreePath;
import com.sun.tools.javac.api.JavacTrees;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeInfo;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.Context;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.element.Element;
import org.netbeans.lib.nbjavac.services.NBTreeMaker.IndexedClassDecl;

/**
 *
 * @author lahvac
 */
public class NBJavacTrees extends JavacTrees {

    private final Map<Element, TreePath> element2paths = new HashMap<>();
    
    public static void preRegister(Context context) {
        context.put(JavacTrees.class, new Context.Factory<JavacTrees>() {
            public JavacTrees make(Context c) {
                return new NBJavacTrees(c);
            }
        });
    }
    protected NBJavacTrees(Context context) {
        super(context);
    }

    @Override
    protected Copier createCopier(TreeMaker make) {
        return new Copier(make) {
            @Override public JCTree visitClass(ClassTree node, JCTree p) {
                JCTree result;
                try {
                    MethodHandle superVisitClass = MethodHandles.lookup().findSpecial(Copier.class, "visitClass", MethodType.methodType(JCTree.class, new Class[]{ClassTree.class, JCTree.class}), getClass());
                    result = (JCTree) superVisitClass.invokeExact(this, node, p);
                } catch (Throwable ex) {
                    Logger.getLogger(NBJavacTrees.class.getName()).log(Level.FINE, null, ex);
                    result = super.visitClass(node, p);
                }

                if (node instanceof IndexedClassDecl && result instanceof IndexedClassDecl) {
                    ((IndexedClassDecl) result).index = ((IndexedClassDecl) node).index;
                }

                return result;
            }
        };
    }

    @Override
    public TreePath getPath(Element e) {
        TreePath path = super.getPath(e);
        return path != null ? path : element2paths.get(e);
    }
    
    void addPathForElement(Element elem, TreePath path) {
        element2paths.put(elem, path);
    }

    @Override
    public Symbol getElement(TreePath path) {
        return TreeInfo.symbolFor((JCTree) path.getLeaf());
    }

}
