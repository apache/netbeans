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
package org.netbeans.api.java.source.matching;

import com.sun.source.tree.IdentifierTree;
import com.sun.source.util.TreePath;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import org.netbeans.api.annotations.common.NonNull;
import org.openide.util.Parameters;

//XXX: patterns as TreePath
/**A pattern occurrences of which can be searched using {@link Matcher}.
 *
 * @author lahvac
 */
public class Pattern {

    final Collection<? extends TreePath> pattern;
    final Map<String, TypeMirror> variable2Type;
    final Collection<? extends VariableElement> remappable;
    final boolean allowRemapToTrees;

    /**Creates a simple pattern. Tree nodes that are semantically equivalent
     * to the given pattern will match it.
     *
     * @param pattern to search for
     * @return the pattern
     */
    public static @NonNull Pattern createSimplePattern(@NonNull TreePath pattern) {
        return new Pattern(Arrays.asList(pattern), null, null, false);
    }

    /**Creates a simple pattern for multiple consequent patterns. A sequence of
     * tree nodes that are semantically equivalent
     * to the given pattern nodes will match it.
     *
     * @param pattern to search for
     * @return the pattern
     */
    public static @NonNull Pattern createSimplePattern(@NonNull Iterable<? extends TreePath> pattern) {
        return new Pattern(toCollection(pattern), null, null, false);
    }
    
    /**Creates a pattern that contains free variables. Tree nodes that are semantically equivalent
     * to the given pattern will match it. The pattern can contain variables, denoted
     * by {@link IdentifierTree} which name starts with the dollar ('$') sign.
     * If the variable name ends with the dollar sign, it is considered to be a "multivariable",
     * which means that any number of consequent tree node can be added to that variable.
     *
     * There may be a type constraint specified for each of the variables. If such
     * a constraint is specified, the variable will be bound only to tree node which
     * type is assignable to the constraint.
     *
     * TODO: variables for names
     * TODO: repeated variables
     * @param pattern to search for
     * @param variable2Type
     * @return the pattern
     */
    public static @NonNull Pattern createPatternWithFreeVariables(@NonNull TreePath pattern, @NonNull Map<String, TypeMirror> variable2Type) {
        return new Pattern(Collections.singletonList(pattern), variable2Type, null, false);
    }

    /**Creates a pattern that contains free variables for multiple consequent patterns.
     * A sequence of
     * tree nodes that are semantically equivalent
     * to the given pattern nodes will match it. The pattern can contain variables, denoted
     * by {@link IdentifierTree} which name starts with the dollar ('$') sign.
     * If the variable name ends with the dollar sign, it is considered to be a "multivariable",
     * which means that any number of consequent tree node can be added to that variable.
     *
     * There may be a type constraint specified for each of the variables. If such
     * a constraint is specified, the variable will be bound only to tree node which
     * type is assignable to the constraint.
     *
     * TODO: variables for names
     * TODO: repeated variables
     * @param pattern to search for
     * @param variable2Type
     * @return the pattern
     */
    public static @NonNull Pattern createPatternWithFreeVariables(@NonNull Iterable<? extends TreePath> pattern, @NonNull Map<String, TypeMirror> variable2Type) {
        return new Pattern(toCollection(pattern), variable2Type, null, false);
    }

    public static @NonNull Pattern createPatternWithRemappableVariables(@NonNull TreePath pattern, @NonNull Collection<? extends VariableElement> remappable, boolean allowRemapToTrees) {
        return new Pattern(Collections.singletonList(pattern), null, remappable, allowRemapToTrees);
    }

    public static @NonNull Pattern createPatternWithRemappableVariables(@NonNull Iterable<? extends TreePath> pattern, @NonNull Collection<? extends VariableElement> remappable, boolean allowRemapToTrees) {
        return new Pattern(toCollection(pattern), null, remappable, allowRemapToTrees);
    }

    private static Collection<? extends TreePath> toCollection(Iterable<? extends TreePath> pattern) {
        Collection<TreePath> result = new ArrayList<TreePath>();

        for (TreePath tp : pattern) {
            Parameters.notNull("pattern", tp);
            result.add(tp);
        }

        return result;
    }
    
    private Pattern(Collection<? extends TreePath> pattern, Map<String, TypeMirror> variable2Type, Collection<? extends VariableElement> remappable, boolean allowRemapToTrees) {
        this.pattern = pattern;
        this.variable2Type = variable2Type;
        this.remappable = remappable;
        this.allowRemapToTrees = allowRemapToTrees;
    }

}
