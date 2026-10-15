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

import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;
import com.sun.tools.javac.api.JavacScope;
import com.sun.tools.javac.api.JavacTrees;
import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.comp.AttrContext;
import com.sun.tools.javac.comp.Check;
import com.sun.tools.javac.comp.Check.CheckContext;
import com.sun.tools.javac.comp.Env;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.JCVariableDecl;
import com.sun.tools.javac.tree.TreeInfo;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.Context;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.element.Element;

/**
 *
 * @author lahvac
 */
public class NBJavacTrees extends JavacTrees {

    private static final Logger LOG = Logger.getLogger(NBJavacTrees.class.getName());
    private final Map<Element, TreePath> element2paths = new HashMap<>();
    private final CheckContext chkBasicHandler;
    
    public static void preRegister(Context context) {
        context.put(JavacTrees.class, new Context.Factory<JavacTrees>() {
            public JavacTrees make(Context c) {
                return new NBJavacTrees(c);
            }
        });
    }
    protected NBJavacTrees(Context context) {
        super(context);
        Check chk = Check.instance(context);

        CheckContext chkBasicHandlerTemp = null;

        try {
            if (basicHandlerField != null) {
                chkBasicHandlerTemp = (CheckContext) basicHandlerField.get(chk);
            }
        } catch (ReflectiveOperationException ex) {
            LOG.log(Level.FINE, null, ex);
        }

        chkBasicHandler = chkBasicHandlerTemp;
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

    @Override
    protected Copier createCopier(TreeMaker maker) {
        return new Copier(maker) {
            @Override
            public JCTree visitVariable(VariableTree node, JCTree p) {
                JCVariableDecl old = (JCVariableDecl) node;
                JCVariableDecl nue = (JCVariableDecl) super.visitVariable(node, p);
                if (old.sym != null) {
                    nue.mods.flags |= old.sym.flags_field & Flags.EFFECTIVELY_FINAL;
                }
                return nue;
            }
        };
    }

    @Override
    public JavacScope getScope(TreePath path) {
        JavacScope result = super.getScope(path);

        if (returnResultField != null) {
            Env<AttrContext> env = result.getEnv();

            try {
                Object returnResult = returnResultField.get(env.info);
                if (returnResult != null) {
                    //ensure the returnResult's checkContext is the Check.basicHandler:
                    returnResultField.set(env.info, dupMethod.invoke(returnResult, chkBasicHandler));
                }
            } catch (ReflectiveOperationException ex) {
                LOG.log(Level.FINE, null, ex);
            }
        }

        return result;
    }

    private static final Field basicHandlerField;
    private static final Field returnResultField;
    private static final Method dupMethod;

    static {
        Field basicHandlerFieldTemp;
        Field returnResultFieldTemp;
        Method dupMethodTemp;

        try {
            basicHandlerFieldTemp = Check.class.getDeclaredField("basicHandler");
            basicHandlerFieldTemp.setAccessible(true);
            returnResultFieldTemp = AttrContext.class.getDeclaredField("returnResult");
            returnResultFieldTemp.setAccessible(true);
            dupMethodTemp = Class.forName("com.sun.tools.javac.comp.Attr$ResultInfo")
                                    .getDeclaredMethod("dup", CheckContext.class);
            dupMethodTemp.setAccessible(true);
        } catch (ReflectiveOperationException ex) {
            LOG.log(Level.FINE, null, ex);
            basicHandlerFieldTemp = null;
            returnResultFieldTemp = null;
            dupMethodTemp = null;
        }

        basicHandlerField = basicHandlerFieldTemp;
        returnResultField = returnResultFieldTemp;
        dupMethod = dupMethodTemp;
    }
}
