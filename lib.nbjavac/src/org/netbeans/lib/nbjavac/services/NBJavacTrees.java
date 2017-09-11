/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.lib.nbjavac.services;

import com.sun.source.doctree.DocCommentTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.util.TreePath;
import com.sun.tools.javac.api.JavacTrees;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.Context;
import java.util.HashMap;
import java.util.Map;
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
                JCTree result = super.visitClass(node, p);

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
}
