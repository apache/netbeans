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
