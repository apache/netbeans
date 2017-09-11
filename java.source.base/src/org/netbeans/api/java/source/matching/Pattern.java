/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
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
