/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2015 Sun Microsystems, Inc.
 */
package org.netbeans.spi.knockout;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.netbeans.spi.knockout.BindingsProvider.Response;
import org.openide.filesystems.FileObject;
import org.openide.util.BaseUtilities;
import org.openide.util.Lookup;
import org.openide.util.TopologicalSortException;

/**
 * Allows structural description of a <code>ko.applyBindings</code> parameter.
 * Register as {@link BindingsProvider}. When called build description of JSON
 * types using methods like {@link #stringProperty(java.lang.String, boolean)}
 * and then pass the result into
 * {@link Response#applyBindings(org.netbeans.spi.knockout.Bindings)} method.
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public final class Bindings {

    private final String name;
    private final List<Bindings> subBindings = new ArrayList<>();
    private final List<Object> props = new ArrayList<>();

    private Bindings(String name) {
        this.name = name;
    }

    /**
     * Defines new variable with provided name and assigns a JSON object into
     * it. The created instance is a builder - continue calling its various
     * builder methods like {@link #intProperty(java.lang.String, boolean)}.
     *
     * @param name name of variable (must be valid JavaScript identifier)
     * @return empty JSON object ready to be filled using
     * {@link #doubleProperty(java.lang.String, boolean)}, and other methods
     */
    public static Bindings create(String name) {
        return new Bindings(name);
    }

    /**
     * Generates a boolean property (with value <code>true</code>) into the JSON
     * class.
     *
     * @param name name of variable (must be valid JavaScript identifier)
     * @param array should this property be an array of just a single value
     * @return this object
     */
    public final Bindings booleanProperty(String name, boolean array) {
        addProp(name, array, "true");
        return this;
    }

    /**
     * Generates a string property (with value <code>''</code>) into the JSON
     * class.
     *
     * @param name name of variable (must be valid JavaScript identifier)
     * @param array should this property be an array of just a single value
     * @return this object
     */
    public final Bindings stringProperty(String name, boolean array) {
        addProp(name, array, "''");
        return this;
    }

    /**
     * Generates a integer property (with value <code>0</code>) into the JSON
     * class.
     *
     * @param name name of variable (must be valid JavaScript identifier)
     * @param array should this property be an array of just a single value
     * @return this object
     */
    public final Bindings intProperty(String name, boolean array) {
        addProp(name, array, "0");
        return this;
    }

    /**
     * Generates a floating point property (with value <code>0.1</code>) into
     * the JSON class.
     *
     * @param name name of variable (must be valid JavaScript identifier)
     * @param array should this property be an array of just a single value
     * @return this object
     */
    public final Bindings doubleProperty(String name, boolean array) {
        addProp(name, array, "0.1");
        return this;
    }

    /**
     * Generates a function (empty) into the JSON class.
     *
     * @param name name of variable (must be valid JavaScript identifier)
     * @return this object
     *
     * @since 1.1
     */
    public final Bindings function(String name) {
        addProp(name, false, "function() {}");
        return this;
    }

    /**
     * Generates complex subtype based on another {@link Bindings} class.
     *
     * @param name name of variable (must be valid JavaScript identifier)
     * @param binding another description of a JSON like object
     * @param array should this property be an array of just a single value
     * @return this object
     */
    public final Bindings modelProperty(String name, Bindings binding, boolean array) {
        subBindings.add(binding);
        addProp(name, array, binding);
        return this;
    }

    final String generate() {
        StringBuilder sb = new StringBuilder();
        //sb.append("(function() {\n");
        HashSet<Bindings> visited = new HashSet<>();
        Collection<Bindings> lhs = new LinkedHashSet<>();
        StringBuilder delayedInit = new StringBuilder();
        if (!walkBindings(visited, lhs)) {
            Map<Bindings, List<Bindings>> edges = new HashMap<>();
            for (Bindings b : lhs) {
                edges.put(b, b.subBindings);
            }
            try {
                BaseUtilities.topologicalSort(lhs, edges);
                // There is a cycle, so the resolution should fail
                throw new IllegalStateException();
            } catch (TopologicalSortException ex) {
                lhs = Collections.checkedList(ex.partialSort(), Bindings.class);
            }
        }
        for (Bindings b : lhs) {
            b.generate(sb, delayedInit, visited);
            visited.remove(b);
        }
        sb.append(delayedInit);
        sb.append("\nko.applyBindings(").append(name).append(");\n");
        //sb.append("}());");
        return sb.toString();
    }

    private void generate(StringBuilder sb, StringBuilder delayedInit, Set<Bindings> notYetProcessed) {
        sb.append("var ").append(name).append(" = {");
        String sep = "\n";
        for (int i = 0; i < props.size(); i += 3) {
            String propName = (String)props.get(i);
            Boolean array = (Boolean)props.get(i + 1);
            Object value = props.get(i + 2);
            
            if (value instanceof Bindings) {
                Bindings b = (Bindings) value;
                if (notYetProcessed.contains(b)) {
                    delayedInit.append("\n").append(this.name).append("[\"").
                        append(propName).append("\"] = ").append(b.name).
                        append(";");
                    continue;
                }
                value = b.name;
            }
            
            if (array) {
                value = "[ " + value + " ]";
            }
            sb.append(sep).append("  ").append('\"').append(propName).append("\" : ").append(value);
            sep = ",\n";
        }
        sb.append("\n};\n");
    }

    private void addProp(String name, boolean array, Object value) {
        if (name.contains("\"")) {
            throw new IllegalStateException("Wrong name " + name);
        }
        props.add(name);
        props.add(array);
        props.add(value);
    }

    private boolean walkBindings(Set<Bindings> visited, Collection<Bindings> collect) {
        if (!visited.add(this)) {
            return false;
        }
        boolean ok = true;
        for (Bindings b : subBindings) {
            ok &= b.walkBindings(visited, collect);
        }
        collect.add(this);
        return ok;
    }

    /**
     * Queries registered providers for additional knockout.js bindings for
     * given file. Generates a JavaScript code describing JSON-like structures
     * used by knockout.js that can be used to enhance code-completion for HTML
     * files using knockout.js bindings.
     *
     * @param htmlFile the HTML file opened in editor
     * @param versionOfFormat the format of the output. Use <code>1</code> for
     * now.
     * @return either <code>null</code> if no provider or format has been found
     * for given file or a JavaScript code formated accordingly
     */
    public static String findBindings(FileObject htmlFile, int versionOfFormat) {
        if (versionOfFormat != 1) {
            return null;
        }
        for (BindingsProvider p : Lookup.getDefault().lookupAll(BindingsProvider.class)) {
            final Response r = p.findBindings(htmlFile);
            if (r.bindings != null) {
                return r.bindings.generate();
            }
        }
        return null;
    }
}
