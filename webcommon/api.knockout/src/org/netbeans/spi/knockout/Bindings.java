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
 * {@link Response#create(Bindings)} method.
 *
 * @author Jaroslav Tulach &lt;jtulach@netbeans.org&gt;
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
