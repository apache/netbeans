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
package org.netbeans.modules.java.hints.suggestions;

import com.sun.source.tree.ArrayTypeTree;
import com.sun.source.tree.BlockTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.NewArrayTree;
import com.sun.source.tree.StatementTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.lang.model.element.Modifier;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.GeneratorUtilities;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;
import static org.netbeans.modules.java.hints.suggestions.Bundle.*;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.Fix;
import org.netbeans.spi.editor.hints.Severity;
import org.netbeans.spi.java.hints.ErrorDescriptionFactory;
import org.netbeans.spi.java.hints.Hint;
import org.netbeans.spi.java.hints.HintContext;
import org.netbeans.spi.java.hints.JavaFix;
import org.netbeans.spi.java.hints.TriggerPattern;
import org.openide.util.NbBundle;

/**
 * 
 * @author Ralph Benjamin Ruijs <ralphbenjamin@netbeans.org>
 */
@NbBundle.Messages({"DN_org.netbeans.modules.java.hints.suggestions.Move.moveInitialization=Move initialization to constructor",
                    "DESC_org.netbeans.modules.java.hints.suggestions.Move.moveInitialization=Moves a fields initialization expression to the constructors.",
                    "FIX_moveInitialization=Move initializer to constructor(s)"})
public class Move {
    
    @Hint(displayName = "#DN_org.netbeans.modules.java.hints.suggestions.Move.moveInitialization",
            description = "#DESC_org.netbeans.modules.java.hints.suggestions.Move.moveInitialization",
            category="suggestions", hintKind=Hint.Kind.ACTION, severity=Severity.HINT)
    @TriggerPattern(value="$mods$ $type $name = $init;")
    public static ErrorDescription moveInitialization(HintContext ctx) {
        final TreePath path = ctx.getPath();
        Tree.Kind parentKind = path.getParentPath().getLeaf().getKind();

        if (parentKind != Tree.Kind.CLASS ||
                path.getLeaf().getKind() != Tree.Kind.VARIABLE) {
            return null;
        }
        VariableTree var = (VariableTree) path.getLeaf();
        if(var.getModifiers().getFlags().contains(Modifier.STATIC)) {
            return null;
        }

        String displayName = FIX_moveInitialization();
        Fix fix = new FixImpl(ctx.getInfo(), ctx.getPath()).toEditorFix();

        return ErrorDescriptionFactory.forName(ctx, ctx.getPath(), displayName, fix);
    }
    
    private static final class FixImpl extends JavaFix {

        public FixImpl(CompilationInfo info, TreePath tp) {
            super(info, tp);
        }

        @Override
        protected String getText() {
            return FIX_moveInitialization();
        }

        @Override
        protected void performRewrite(JavaFix.TransformationContext ctx) {
            TreePath tp = ctx.getPath();
            TreePath parentPath = tp.getParentPath();
            ClassTree parent = (ClassTree) parentPath.getLeaf();
            VariableTree var = (VariableTree) tp.getLeaf();
            
            WorkingCopy wc = ctx.getWorkingCopy();
            TreeMaker make = wc.getTreeMaker();
            Map<Tree, Tree> original2translated = new HashMap<Tree, Tree>();
            ClassTree translated = parent;
            
            VariableTree newVar = make.Variable(var.getModifiers(), var.getName(), var.getType(), null);
            original2translated.put(var, newVar);
            List<? extends Tree> members = parent.getMembers();
            for (Tree tree : members) {
                if(tree.getKind() == Tree.Kind.METHOD &&
                        ((MethodTree)tree).getName().contentEquals("<init>")) {
                    MethodTree method = (MethodTree)tree;
                    boolean synthetic = wc.getTreeUtilities().isSynthetic(new TreePath(parentPath, tree));
                    boolean isStatic = var.getModifiers().getFlags().contains(Modifier.STATIC);
                    BlockTree body = method.getBody();
                    if (body == null) {
                        continue;
                    }
                    List<StatementTree> statements = new LinkedList<StatementTree>(body.getStatements());
                    statements.add(1, make.ExpressionStatement(make.Assignment(
                            make.Identifier((isStatic? parent.getSimpleName().toString() : "this") + "." + var.getName()),
                            transformInitializer(var.getInitializer(), var.getType(), make))));
                    
                    if(synthetic) {
                        Tree constructor;
                        if (parentPath.getParentPath().getLeaf().getKind() == Kind.NEW_CLASS) {
                            constructor = make.Block(Collections.singletonList(statements.get(1)), false);
                        } else {
                            constructor = make.Constructor(make.Modifiers(method.getModifiers().getFlags(), method.getModifiers().getAnnotations()),
                                                                method.getTypeParameters(), method.getParameters(), method.getThrows(), make.Block(statements, false));
                        }
                        translated = GeneratorUtilities.get(wc).insertClassMember(translated, constructor);
                    } else {
                        original2translated.put(body, make.Block(statements, false));
                    }
                }
            }
            
            translated = (ClassTree) wc.getTreeUtilities().translate(translated, original2translated);
            wc.rewrite(parent, translated);
        }

        private ExpressionTree transformInitializer(ExpressionTree initializer, Tree type, TreeMaker make) {
            if(initializer.getKind() == Tree.Kind.NEW_ARRAY) {
                NewArrayTree nat = (NewArrayTree) initializer;
                if(nat.getType() == null) {
                    if(type.getKind() == Tree.Kind.ARRAY_TYPE) {
                        ArrayTypeTree arrayTypeTree = (ArrayTypeTree) type;
                        type = arrayTypeTree.getType();
                    }
                    return make.NewArray(type, nat.getDimensions(), nat.getInitializers());
                }
            }
            return initializer;
        }

    }
}
