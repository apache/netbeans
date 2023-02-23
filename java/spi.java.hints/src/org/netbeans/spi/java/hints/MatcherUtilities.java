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

package org.netbeans.spi.java.hints;

import com.sun.source.tree.BlockTree;
import com.sun.source.tree.Scope;
import com.sun.source.tree.StatementTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.lang.model.type.TypeMirror;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.modules.java.hints.spiimpl.Utilities;
import org.netbeans.modules.java.hints.spiimpl.pm.PatternCompiler;
import org.netbeans.api.java.source.matching.Matcher;
import org.netbeans.api.java.source.matching.Occurrence;
import org.netbeans.api.java.source.matching.Pattern;

/**XXX: cancelability
 * TODO: needed?
 *
 * @author lahvac
 */
public class MatcherUtilities {

    public static boolean matches(@NonNull HintContext ctx, @NonNull TreePath variable, @NonNull String pattern) {
        return matches(ctx, variable, pattern, null, null, null);
    }

    public static boolean matches(@NonNull HintContext ctx, @NonNull TreePath variable, @NonNull String pattern, boolean fillInVariablesHack) {
        return matches(ctx, variable, pattern, ctx.getVariables(), ctx.getMultiVariables(), ctx.getVariableNames(), 
                fillInVariablesHack ? ctx.getConstraints() : Collections.<String, TypeMirror>emptyMap());
    }

    public static boolean matches(@NonNull HintContext ctx, @NonNull TreePath variable, @NonNull String pattern, Map<String, TreePath> outVariables, Map<String, Collection<? extends TreePath>> outMultiVariables, Map<String, String> outVariables2Names) {
        return matches(ctx, variable, pattern, outVariables, outMultiVariables, outVariables2Names, Collections.<String, TypeMirror>emptyMap());
    }

    public static boolean matches(@NonNull HintContext ctx, @NonNull TreePath variable, @NonNull String pattern, Map<String, TreePath> outVariables, 
                        Map<String, Collection<? extends TreePath>> outMultiVariables, Map<String, String> outVariables2Names, Map<String, TypeMirror> variable2Type) {
        Pattern p = PatternCompiler.compile(ctx.getInfo(), pattern, variable2Type, Collections.<String>emptyList());
        Map<String, TreePath> variables = new HashMap<>(ctx.getVariables());
        Map<String, Collection<? extends TreePath>> multiVariables = new HashMap<>(ctx.getMultiVariables());
        Map<String, String> variables2Names = new HashMap<>(ctx.getVariableNames());
        Iterable<? extends Occurrence> occurrences = Matcher.create(ctx.getInfo()).setCancel(new AtomicBoolean()).setPresetVariable(variables, multiVariables, variables2Names).setSearchRoot(variable).setTreeTopSearch().match(p);

        if (occurrences.iterator().hasNext()) {
            Occurrence od = occurrences.iterator().next();
            outVariables(outVariables, od.getVariables(), ctx.getVariables());
            outVariables(outMultiVariables, od.getMultiVariables(), ctx.getMultiVariables());
            outVariables(outVariables2Names, od.getVariables2Names(), ctx.getVariableNames());

            return true;
        }

        return false;
    }

    public static boolean matches(@NonNull HintContext ctx, @NonNull Collection<? extends TreePath> variable, @NonNull String pattern, Map<String, TreePath> outVariables, Map<String, Collection<? extends TreePath>> outMultiVariables, Map<String, String> outVariables2Names) {
        Scope s = Utilities.constructScope(ctx.getInfo(), Collections.<String, TypeMirror>emptyMap());
        Tree  patternTree = Utilities.parseAndAttribute(ctx.getInfo(), pattern, s);
        List<? extends Tree> patternTrees;

        if (Utilities.isFakeBlock(patternTree)) {
            List<? extends StatementTree> statements = ((BlockTree) patternTree).getStatements();

            patternTrees = statements.subList(1, statements.size() - 1);
        } else {
            patternTrees = Collections.singletonList(patternTree);
        }

        if (variable.size() != patternTrees.size()) return false;
        
        Map<String, TreePath> variables = new HashMap<>(ctx.getVariables());
        Map<String, Collection<? extends TreePath>> multiVariables = new HashMap<>(ctx.getMultiVariables());
        Map<String, String> variables2Names = new HashMap<>(ctx.getVariableNames());
        Iterator<? extends TreePath> variableIt = variable.iterator();
        Iterator<? extends Tree> patternTreesIt = patternTrees.iterator();

        while (variableIt.hasNext() && patternTreesIt.hasNext()) {
            TreePath patternTreePath = new TreePath(new TreePath(ctx.getInfo().getCompilationUnit()), patternTreesIt.next());
            Pattern p = Pattern.createPatternWithFreeVariables(patternTreePath, Collections.<String, TypeMirror>emptyMap());
            Iterable<? extends Occurrence> occurrences = Matcher.create(ctx.getInfo()).setCancel(new AtomicBoolean()).setPresetVariable(variables, multiVariables, variables2Names).setSearchRoot(variableIt.next()).setTreeTopSearch().match(p);

            if (!occurrences.iterator().hasNext()) {
                return false;
            }

            Occurrence od = occurrences.iterator().next();

            variables = od.getVariables();
            multiVariables = od.getMultiVariables();
            variables2Names = od.getVariables2Names();
        }

        if (variableIt.hasNext() == patternTreesIt.hasNext()) {
            outVariables(outVariables, variables, ctx.getVariables());
            outVariables(outMultiVariables, multiVariables, ctx.getMultiVariables());
            outVariables(outVariables2Names, variables2Names, ctx.getVariableNames());

            return true;
        }

        return false;
    }

    private static <T> void outVariables(Map<String, T> outMap, Map<String, T> currentValues, Map<String, T> origValues) {
        if (outMap == null) return;

        currentValues.keySet().removeAll(origValues.keySet());

        outMap.putAll(currentValues);
    }

}
