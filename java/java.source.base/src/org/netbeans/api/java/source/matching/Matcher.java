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
package org.netbeans.api.java.source.matching;

import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.util.TreePath;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.modules.java.source.matching.CopyFinder;
import org.netbeans.modules.java.source.matching.CopyFinder.Cancel;
import org.netbeans.modules.java.source.matching.CopyFinder.Options;
import org.netbeans.modules.java.source.matching.CopyFinder.State;
import org.netbeans.modules.java.source.matching.CopyFinder.VariableAssignments;
import org.openide.util.Cancellable;

/**Searches for occurrences of a {@link Pattern}.
 *
 * @author lahvac
 */
public class Matcher implements Cancellable {

    /**Create the Matcher for the given {@link CompilationInfo}. See the set methods for default
     * settings of the Matcher.
     *
     * @param info for which the Matcher should be created
     * @return newly created Matcher
     */
    public static @NonNull Matcher create(@NonNull CompilationInfo info) {
        return new Matcher(info);
    }

    private final CompilationInfo info;
    private       AtomicBoolean givenCancel;
    private final AtomicBoolean privateCancel = new AtomicBoolean();
    private       TreePath root;
    private Set<Options> options = EnumSet.noneOf(Options.class);
    private Map<String, TreePath> variables;
    private Map<String, Collection<? extends TreePath>> multiVariables;
    private Map<String, String> variables2Names;

    private Matcher(CompilationInfo info) {
        this.info = info;
        this.root = new TreePath(info.getCompilationUnit());
        this.options.add(Options.ALLOW_GO_DEEPER);
    }

    /**Search for the occurrences only under the given tree. By default, the full {@link CompilationUnitTree}
     * of the given {@link CompilationInfo} is used.
     *
     * @param root where to start the search
     * @return the matcher itself
     */
    public @NonNull Matcher setSearchRoot(@NonNull TreePath root) {
        this.root = root;
        return this;
    }

    /**The matcher should perform the tree-top search: the occurrence of the pattern must correspond directly
     * to the search root. By default, tree-top search is not performed, and all occurrences of the
     * pattern under the specified root will be reported.
     *
     * @return the matcher itself
     */
    public @NonNull Matcher setTreeTopSearch() {
        this.options.remove(Options.ALLOW_GO_DEEPER);
        return this;
    }

    /**The matching should ignore type attributes on the trees. By default, types
     * specified in the pattern must match the actual types.
     *
     * @return the matcher itself
     */
    public @NonNull Matcher setUntypedMatching() {
        this.options.add(Options.NO_ELEMENT_VERIFY);
        return this;
    }

    /**When set, trees (like implicit receivers) that are not in the source code,
     * but have variable in the pattern, will get a synthetic entry in variables.
     *
     * @return the matcher itself
     * @since 2.40
     */
    public @NonNull Matcher setKeepSyntheticTrees() {
        this.options.add(Options.KEEP_SYNTHETIC_THIS);
        return this;
    }

    /**Preset values of free variables in the pattern (see {@link Pattern#createPatternWithFreeVariables(com.sun.source.util.TreePath, java.util.Map)}).
     * A tree node will be marked as an occurrence of the pattern only if the subtree
     * corresponding to a specified free variable will match the given value.
     *
     * By default, no values are preset for any variables.
     * 
     * @param variables
     * @param multiVariables
     * @param variables2Names
     * @return the matcher itself
     */
    public @NonNull Matcher setPresetVariable(@NonNull Map<String, TreePath> variables, @NonNull Map<String, Collection<? extends TreePath>> multiVariables, @NonNull Map<String, String> variables2Names) {
        this.variables = variables;
        this.multiVariables = multiVariables;
        this.variables2Names = variables2Names;
        return this;
    }

    /**Make the matching cancelable by setting {@code true} to the given {@link AtomicBoolean}.
     *
     * @param cancel {@link AtomicBoolean} which should be set to {@code true} to stop the matching as soon as possible
     * @return the matcher itself
     */
    public @NonNull Matcher setCancel(@NonNull AtomicBoolean cancel) {
        this.givenCancel = cancel;
        return this;
    }

    /**Search for occurrences of the given pattern in the given subject tree.
     * 
     * TODO: "multipattern" matching
     * @param pattern for which the search should be performed
     * @return descriptions of found pattern occurrences
     */
    public @NonNull Collection<? extends Occurrence> match(Pattern pattern) {
        Set<Options> opts = EnumSet.noneOf(Options.class);

        opts.addAll(options);

        if (pattern.variable2Type != null) {
            opts.add(Options.ALLOW_VARIABLES_IN_PATTERN);
        }

        if (pattern.allowRemapToTrees) {
            opts.add(Options.ALLOW_REMAP_VARIABLE_TO_EXPRESSION);
        }

        List<Occurrence> result = new ArrayList<Occurrence>();
        State preinitializeState;

        if (variables != null) {
            preinitializeState = State.from(variables, multiVariables, variables2Names);
        } else {
            preinitializeState = null;
        }

        Cancel cancel = new Cancel() {
            @Override public boolean isCancelled() {
                return privateCancel.get() || (givenCancel != null && givenCancel.get());
            }
        };

        for (Entry<TreePath, VariableAssignments> e : CopyFinder.internalComputeDuplicates(info, pattern.pattern, root, preinitializeState, pattern.remappable, cancel, pattern.variable2Type, opts.toArray(new Options[0])).entrySet()) {
            result.add(new Occurrence(e.getKey(), e.getValue().variables, e.getValue().multiVariables, e.getValue().variables2Names, e.getValue().variablesRemapToElement, e.getValue().variablesRemapToTrees));
        }

        return Collections.unmodifiableCollection(result);
    }

    @Override
    public boolean cancel() {
        privateCancel.set(true);
        return true;
    }

}
