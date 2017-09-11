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
package org.netbeans.api.java.source.matching;

import com.sun.source.util.TreePath;
import java.util.Collection;
import java.util.Map;
import javax.lang.model.element.Element;

/**A found occurrence of a pattern.
 *
 * @author lahvac
 */
public final class Occurrence {
    private final TreePath occurrenceRoot;
    private final Map<String, TreePath> variables;
    private final Map<String, Collection<? extends TreePath>> multiVariables;
    private final Map<String, String> variables2Names;
    private final Map<Element, Element> variablesRemapToElement;
    private final Map<Element, TreePath> variablesRemapToTrees;

    Occurrence(TreePath occurrenceRoot, Map<String, TreePath> variables, Map<String, Collection<? extends TreePath>> multiVariables, Map<String, String> variables2Names, Map<Element, Element> variablesRemapToElement, Map<Element, TreePath> variablesRemapToTrees) {
        this.occurrenceRoot = occurrenceRoot;
        this.variables = variables;
        this.multiVariables = multiVariables;
        this.variables2Names = variables2Names;
        this.variablesRemapToElement = variablesRemapToElement;
        this.variablesRemapToTrees = variablesRemapToTrees;
    }

    /**The tree node that represents the occurrence. For multi-part patterns {@link Pattern#createSimplePattern(java.lang.Iterable) },
     * returns the {@link TreePath} corresponding to the first part (TODO: verify!).
     *
     * @return root tree of the occurrence
     */
    public TreePath getOccurrenceRoot() {
        return occurrenceRoot;
    }

    /**For patterns with free variables, contains mapping of single-variable
     * names to the tree node that was bound to the variable. That means,
     * the tree node that corresponds to the first occurrence of the variable
     * in the pattern.
     *
     * @return mapping of free variable names to the tree nodes that were bound to them
     */
    public Map<String, TreePath> getVariables() {
        return variables;
    }

    /**For patterns with free variables, contains mapping of multi-variable
     * names to the tree nodes that were bound to the variable. That means,
     * the tree nodes that corresponds to the first occurrence of the variable
     * in the pattern.
     *
     * @return mapping of free variable names to the tree nodes that were bound to them
     */
    public Map<String, Collection<? extends TreePath>> getMultiVariables() {
        return multiVariables;
    }

    /**For patterns with free variables, contains mapping of variable
     * names to a name that was bound to the variable.
     *
     * @return mapping of free variable names to names that were bound to them
     */
    public Map<String, String> getVariables2Names() {
        return variables2Names;
    }

    /**For patterns which allow variable remap, contains mapping of the element
     * in the pattern to the corresponding element in this occurrence.
     *
     * @return mapping of elements in the pattern to the elements in this occurrence
     */
    public Map<Element, Element> getVariablesRemapToElement() {
        return variablesRemapToElement;
    }

    /**For patterns which allow variable remap to trees, contains mapping of the element
     * in the pattern to the corresponding tree node in this occurrence.
     *
     * @return mapping of elements in the pattern to the elements in this occurrence
     */
    public Map<Element, TreePath> getVariablesRemapToTrees() {
        return variablesRemapToTrees;
    }

}
