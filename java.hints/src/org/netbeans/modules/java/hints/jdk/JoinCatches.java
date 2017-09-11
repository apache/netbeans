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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.java.hints.jdk;

import com.sun.source.tree.CatchTree;
import com.sun.source.tree.StatementTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.tree.TryTree;
import com.sun.source.tree.UnionTypeTree;
import com.sun.source.util.TreePath;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.UnionType;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.TypeMirrorHandle;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.api.java.source.matching.Matcher;
import org.netbeans.api.java.source.matching.Pattern;
import org.netbeans.spi.java.hints.Hint;
import org.netbeans.spi.java.hints.TriggerPattern;
import org.netbeans.spi.java.hints.TriggerPatterns;
import org.netbeans.spi.java.hints.HintContext;
import org.netbeans.spi.java.hints.JavaFix;
import org.netbeans.spi.java.hints.ErrorDescriptionFactory;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.api.java.source.matching.Occurrence;
import org.netbeans.modules.java.hints.errors.Utilities;
import org.openide.util.NbBundle;

/**
 * Joins several catch branches into a multicatch.
 * Functional note: if types from two branches are releated by subclassing, entire catches are now excluded from the join. This may seem strange in a case
 * when the excluded catch clause is a multicatch already, and only one (or not all) caught types are subtypes or supertypes of other catch branch.
 * <p/>
 * Implementing the hierarchy exclusion precisely would mean to rewrite individual catch clauses, remove types, which are OK to join and leave the rest. Even
 * more sophisticated solution is to identify groups of handlers, with identical code, collect their types and make joins.
 * <p/>
 * When working with types, it is important to use not TypeMirrors to generate new declarations, but to use original Trees (TreePathHandles) so that comments
 * possibly made in between the types are preserved. 
 * 
 * @author lahvac
 */
@Hint(displayName = "#DN_org.netbeans.modules.java.hints.jdk.JoinCatches", description = "#DESC_org.netbeans.modules.java.hints.jdk.JoinCatches", category="rules15",
        minSourceVersion = "7")
public class JoinCatches {

    @TriggerPatterns({
        @TriggerPattern(
                "try ($resources$) { $trystmts$; } catch ($type1 $var1) { $catchstmts1$; } catch $inter$ catch ($type2 $var2) { $catchstmts2$; } catch $more$"
        ),
        @TriggerPattern(
                "try ($resources$) { $trystmts$; } catch (final $type1 $var1) { $catchstmts1$; } catch $inter$ catch (final $type2 $var2) { $catchstmts2$; } catch $more$"
        ),
        @TriggerPattern(
                "try ($resources$) { $trystmts$; } catch ($type1 $var1) { $catchstmts1$; } catch $inter$ catch ($type2 $var2) { $catchstmts2$; } catch $more$ finally {$finstmts$;}"
        ),
        @TriggerPattern(
                "try ($resources$) { $trystmts$; } catch (final $type1 $var1) { $catchstmts1$; } catch $inter$ catch (final $type2 $var2) { $catchstmts2$; } catch $more$ finally {$finstmts$;}"
        )
    })
    public static ErrorDescription hint(HintContext ctx) {
        TryTree tt = (TryTree) ctx.getPath().getLeaf();

        List<? extends CatchTree> catches = new ArrayList<CatchTree>(tt.getCatches());

        if (catches.size() <= 1) return null;

        for (int i = 0; i < catches.size(); i++) {
            CatchTree toTest = catches.get(i);
            TreePath toTestPath = new TreePath(ctx.getPath(), toTest);
            TreePath mainVar = new TreePath(toTestPath, toTest.getParameter());
            VariableElement excVar = (VariableElement) ctx.getInfo().getTrees().getElement(mainVar);
            TypeMirror mainVarType = ctx.getInfo().getTrees().getTypeMirror(mainVar);

            if (excVar == null || !Utilities.isValidType(mainVarType)) continue;

            Map<TypeMirror, Integer> duplicates = new LinkedHashMap<TypeMirror, Integer>();

            duplicates.put(mainVarType, i);
            for (int j = i + 1; j < catches.size(); j++) {
                Pattern pattern = Pattern.createPatternWithRemappableVariables(new TreePath(toTestPath, toTest.getBlock()), Collections.singleton(excVar), false);
                Iterable<? extends Occurrence> found = 
                        Matcher.create(ctx.getInfo()).
                                setCancel(new AtomicBoolean()).
                                setPresetVariable(ctx.getVariables(), ctx.getMultiVariables(), ctx.getVariableNames()).
                                setSearchRoot(
                                    new TreePath(
                                            new TreePath(ctx.getPath(), catches.get(j)), 
                                            ((CatchTree)catches.get(j)).getBlock()
                                    )).
                                setTreeTopSearch().
                                match(pattern);
                
                if (found.iterator().hasNext()) {
                    TreePath catchPath = new TreePath(ctx.getPath(), catches.get(j));
                    TreePath var = new TreePath(catchPath, ((CatchTree)catches.get(j)).getParameter());
                    Collection<TreePath> statements = new ArrayList<TreePath>();
                    TreePath blockPath = new TreePath(catchPath, ((CatchTree)catches.get(j)).getBlock());

                    for (StatementTree t : ((CatchTree)catches.get(j)).getBlock().getStatements()) {
                        statements.add(new TreePath(blockPath, t));
                    }

                    if (UseSpecificCatch.assignsTo(ctx, var, statements)) {
                        continue;
                    }
                    
                    TypeMirror varType = ctx.getInfo().getTrees().getTypeMirror(var);

                    if (!Utilities.isValidType(varType)) continue;

                    boolean process = true;
                    List<TypeMirror> varTypes = new ArrayList<>();
                    if (varType.getKind() == TypeKind.UNION) {
                        varTypes.addAll(((UnionType) varType).getAlternatives());
                    } else {
                        varTypes.add(varType);
                    }
                    DUP: for (Iterator<TypeMirror> it = duplicates.keySet().iterator(); it.hasNext();) {
                        TypeMirror existingType = it.next();
                        Iterable<? extends TypeMirror> caughtList;
                        
                        if (existingType.getKind() == TypeKind.UNION) {
                            caughtList = ((UnionType) existingType).getAlternatives();
                        } else {
                            caughtList = Collections.singletonList(existingType);
                        }
                        
                        for (Iterator<TypeMirror> vtI = varTypes.iterator(); vtI.hasNext(); ) {
                            TypeMirror vt = vtI.next();
                            for (TypeMirror caught : caughtList) {
                                if (ctx.getInfo().getTypes().isSubtype(caught, vt)) {
                                    // the subtype cannot be added into the union, supertype will be used.
                                    Integer index = duplicates.get(existingType);
                                    // if the broader catch would move BEFORE the more specific one,
                                    // discard it.
                                    if (index != null) {
                                        it.remove();
                                        process &= index > findMin(duplicates.values());
                                    }
                                } else if (ctx.getInfo().getTypes().isSubtype(vt, caught)) {
                                    // should be an error, but must be handled gracefully
                                    process = false;
                                }
                            }
                            if (!process) {
                                break DUP;
                            }
                        }
                    }
                    if (process && !varTypes.isEmpty()) {
                        duplicates.put(varType, j);
                    }
                }
            }

            if (duplicates.size() >= 2) {
                String displayName = NbBundle.getMessage(JoinCatches.class, "ERR_JoinCatches");
                List<TypeMirrorHandle> types = new ArrayList<>();
                for (TypeMirror t : duplicates.keySet()) {
                    types.add(TypeMirrorHandle.create(t));
                }
                return ErrorDescriptionFactory.forName(ctx, toTest.getParameter().getType(), displayName, 
                        new FixImpl(ctx.getInfo(), ctx.getPath(), 
                        new ArrayList<Integer>(duplicates.values()),
                        types).toEditorFix());
            }
        }

        return null;
    }
    
    private static int findMin(Collection<Integer> x) {
        int m = Integer.MAX_VALUE;
        for (int i : x) {
            if (i < m) {
                m = i;
            }
        }
        return m;
    }

    private static final class FixImpl extends JavaFix {

        private final List<Integer> duplicates;
        private final List<TypeMirrorHandle> typeHandles;
        
        public FixImpl(CompilationInfo info, TreePath tryStatement, List<Integer> duplicates, List<TypeMirrorHandle> types) {
            super(info, tryStatement);
            this.duplicates = duplicates;
            this.typeHandles = types;
        }

        @Override
        protected String getText() {
            return NbBundle.getMessage(JoinCatches.class, "FIX_JoinCatches");
        }

        private void addDisjointType(List<Tree> to, Tree type) {
            if (type == null) return;
            if (type.getKind() == Kind.UNION_TYPE) {
                to.addAll(((UnionTypeTree) type).getTypeAlternatives());
            } else {
                to.add(type);
            }
        }

        @Override
        protected void performRewrite(TransformationContext ctx) {
            WorkingCopy wc = ctx.getWorkingCopy();
            TreePath tp = ctx.getPath();
            List<Tree> disjointTypes = new LinkedList<Tree>();
            TryTree tt = (TryTree) tp.getLeaf();
            int first = duplicates.get(0);
            List<Integer> remainingDuplicates = duplicates.subList(1, duplicates.size());
            
            addDisjointType(disjointTypes, tt.getCatches().get(first).getParameter().getType());

            for (Integer d : remainingDuplicates) {
                addDisjointType(disjointTypes, tt.getCatches().get((int) d).getParameter().getType());
            }

            List<CatchTree> newCatches = new LinkedList<CatchTree>();
            int c = 0;

            for (CatchTree ct : tt.getCatches()) {
                if (c == first) {
                    wc.rewrite(ct.getParameter().getType(), wc.getTreeMaker().UnionType(disjointTypes));
                }
                
                if (remainingDuplicates.contains(c++)) continue;

                newCatches.add(ct);
            }

            TryTree nue = wc.getTreeMaker().Try(tt.getResources(), tt.getBlock(), newCatches, tt.getFinallyBlock());

            wc.rewrite(tt, nue);
        }

    }
}
