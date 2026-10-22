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
package org.netbeans.modules.java.hints.bugs;

import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.modules.java.hints.introduce.Flow;
import org.netbeans.modules.java.hints.introduce.Flow.FlowResult;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.Severity;
import org.netbeans.spi.java.hints.HintContext;
import org.netbeans.spi.java.hints.Hint;
import org.netbeans.spi.java.hints.Hint.Options;
import org.netbeans.spi.java.hints.TriggerPattern;
import org.netbeans.spi.java.hints.ErrorDescriptionFactory;

/**
 *
 * @author nbalyam
 */
@Hint(displayName = "Track mutable methods on immutable collections",
        description = "Track mutable methods on immutable collections",
        category = "bugs",
        id = "MutableMethodsOnImmutableCollections",
        severity = Severity.WARNING,
        options = Options.QUERY)

public class MutableMethodsOnImmutableCollections {

    private static final Set<String> MUTATING_METHODS_IN_LIST = Set.of(
            "add", "addAll", "remove", "removeAll", "clear", "set", "replaceAll", "sort"
    );

    private static final Set<String> MUTATING_METHODS_IN_SET = Set.of(
            "add", "addAll", "remove", "removeAll", "retainAll", "clear"
    );

    @TriggerPattern(value = "java.util.List.of($args$)")
    public static List<ErrorDescription> immutableList(HintContext ctx) {
        return checkForMutableMethodInvocations(ctx, MUTATING_METHODS_IN_LIST, "Attempting to modify an immutable List created via List.of()");
    }

    @TriggerPattern(value = "java.util.Set.of($args$)")
    public static List<ErrorDescription> immutableSet(HintContext ctx) {
        return checkForMutableMethodInvocations(ctx, MUTATING_METHODS_IN_SET, "Attempting to modify an immutable Set created via Set.of()");
    }

    private static List<ErrorDescription> checkForMutableMethodInvocations(HintContext ctx, Set<String> mutatingMethods, String warningMessage) {
        List<ErrorDescription> errors = new ArrayList<>();
        FlowResult flow = Flow.assignmentsForUse(ctx.getInfo(), () -> ctx.isCanceled());
        List<MemberSelectTree> invocations = checkForUsagesAndMarkInvocations(ctx.getInfo(), flow, ctx.getPath());

        for (MemberSelectTree mst : invocations) {
            String method = mst.getIdentifier().toString();
            if (mutatingMethods.contains(method)) {
                errors.add(ErrorDescriptionFactory.forName(
                        ctx,
                        TreePath.getPath(ctx.getInfo().getCompilationUnit(), mst),
                        warningMessage
                ));

            }
        }
        return errors;
    }

    private static List<MemberSelectTree> checkForUsagesAndMarkInvocations(CompilationInfo info, FlowResult flow, TreePath initPattern) {
        List<MemberSelectTree> usedInvocationsWithIdentifier = new ArrayList<>();
        Function<Tree, Set<TreePath>> findIdentifierTreePaths = (Tree tree) -> {
            return flow.getValueUsers(tree)
                    .stream()
                    .filter(IdentifierTree.class::isInstance)
                    .map(t -> flow.findPath(t, info.getCompilationUnit()))
                    .filter(treePath -> treePath.getLeaf() instanceof IdentifierTree)
                    .collect(Collectors.toSet());
        };
        Set<TreePath> identfiersPointingToInitializer = Optional.of(initPattern.getLeaf())
                .map(findIdentifierTreePaths)
                .orElse(Set.of());
        while (!identfiersPointingToInitializer.isEmpty()) {
            identfiersPointingToInitializer.forEach(indentifierPath -> {
                var ancestorPath = Optional.of(indentifierPath)
                        .map(tpath -> tpath.getParentPath())
                        .map(tpath -> tpath.getParentPath())
                        .map(tpath -> tpath.getLeaf());
                ancestorPath.ifPresent(ancestor -> {
                    if (ancestor instanceof MethodInvocationTree mit && mit.getMethodSelect() instanceof MemberSelectTree mst) {
                        usedInvocationsWithIdentifier.add(mst);
                    }
                });
            });
            identfiersPointingToInitializer = identfiersPointingToInitializer
                    .parallelStream()
                    .map(tpath -> tpath.getLeaf())
                    .map(findIdentifierTreePaths)
                    .flatMap(tpaths -> tpaths.parallelStream())
                    .collect(Collectors.toSet());
        }
        return usedInvocationsWithIdentifier;
    }

}
