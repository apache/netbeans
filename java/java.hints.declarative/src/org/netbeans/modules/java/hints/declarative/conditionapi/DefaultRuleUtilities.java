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

package org.netbeans.modules.java.hints.declarative.conditionapi;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Set;
import java.util.regex.Pattern;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import org.netbeans.api.annotations.common.NonNull;

/**
 *
 * @author lahvac
 */
public final class DefaultRuleUtilities {

    private final Context context;
    private final Matcher matcher;

    DefaultRuleUtilities(Context context, Matcher matcher) {
        this.context = context;
        this.matcher = matcher;
    }
    
    public boolean referencedIn(Variable variable, Variable in) {
        return matcher.referencedIn(variable, in);
    }

    /**
     * @deprecated Use {@link #sourceVersionGE(int)} instead.
     */
    @Deprecated
    public boolean sourceVersionGE(SourceVersion source) {
        return context.sourceVersion().compareTo(source) >= 0;
    }

    /**
     * Returns true if the provided feature version is less or equals the
     * source version of the inspected file.
     * @see Runtime.Version#feature()
     */
    public boolean sourceVersionLE(int feature) {
        return context.sourceVersion().ordinal() <= feature;
    }

    /**
     * Returns true if the provided feature version is greater or equals the
     * source version of the inspected file.
     * @see Runtime.Version#feature()
     */
    public boolean sourceVersionGE(int feature) {
        return context.sourceVersion().ordinal() >= feature;
    }

    public boolean hasModifier(Variable variable, Modifier modifier) {
        return context.modifiers(variable).contains(modifier);
    }

    public boolean parentMatches(String pattern) {
        Variable parent = context.parent(context.variableForName("$_"));
        
        if (parent == null) {
            return false;
        }
        
        return matcher.matchesAny(parent, pattern); //XXX: $_ currently not part of variables map, so this won't work!!!
    }

    public boolean elementKindMatches(Variable variable, ElementKind... kind) {
        Set<ElementKind> kinds = EnumSet.noneOf(ElementKind.class);
        
        kinds.addAll(Arrays.asList(kind));

        return kinds.contains(context.elementKind(variable));
    }

    public boolean isNullLiteral(@NonNull Variable var) {
        return context.isNullLiteral(var);
    }
    
    public boolean matches(String pattern) {
        Variable current = context.variableForName("$_");

        assert current != null;

        return matchesAny(current, pattern); //XXX: $_ currently not part of variables map, so this won't work!!!
    }

    public boolean matchesWithBind(Variable var, String pattern) {
        return matcher.matchesWithBind(var, pattern);
    }

    public boolean matchesAny(Variable var, String... patterns) {
        return matcher.matchesAny(var, patterns);
    }

    public boolean containsAny(Variable var, String... patterns) {
        return matcher.containsAny(var, patterns);
    }

    /**Tests whether the current occurrences is enclosed (directly or indirectly)
     * by any of the specified classes.
     *
     * @param className canonical class names of possibly enclosing classes
     * @return true if and only if the current occurrence is directly or indirectly
     *              enclosed by any of the given classes.
     */
    public boolean inClass(String... className) {
        Pattern p = constructRegexp(className);
        Variable current = context.variableForName("$_");

        assert current != null;

        for (String canonicalName : context.enclosingClasses(current)) {
            if (p.matcher(canonicalName).matches()) return true;
        }

        return false;
    }

    /**Tests whether the current occurrences is in any of the given packages.
     *
     * @param packageName names of possibly enclosing packages
     * @return true if and only if the current occurrence is inside any of the
     *              given packages
     */
    public boolean inPackage(String... packageName) {
        Pattern p = constructRegexp(packageName);

        return p.matcher(context.enclosingPackage()).matches();
    }

    private static Pattern constructRegexp(String[] pattern) {
        StringBuilder regexp = new StringBuilder();
        boolean first = true;
        for (String p : pattern) {
            if (first) regexp.append("|");

            regexp.append("(");
            regexp.append(Pattern.quote(p));
            regexp.append(")");
            first = false;
        }
        Pattern p = Pattern.compile(regexp.toString());
        return p;
    }

    /**Checks whether the given Java element is available in the particular source
     * code or not.
     * 
     * The <code>elementDescription</code> format is as follows:
     * <dl>
     *   <dt>for type (class, enum, interface or annotation type)</dt>
     *     <dd><em>the FQN of the type</em></dd>
     *   <dt>for field or enum constant</dt>
     *     <dd><em>the FQN of the enclosing type</em><code>.</code><em>field name</em></dd>
     *   <dt>for method</dt>
     *     <dd><em>the FQN of the enclosing type</em><code>.</code><em>method name</em><code>(</code><em>comma separated parameter types</em><code>)</code><br>
     *         The parameter types may include type parameters, but these are ignored. The last parameter type can use ellipsis (...) to denote vararg method.</dd>
     *   <dt>for constructor</dt>
     *     <dd><em>the FQN of the enclosing type</em><code>.</code><em>simple name of enclosing type</em><code>(</code><em>comma separated parameter types</em><code>)</code><br>
     *         See method format for more details on parameter types.</dd>
     * </dl>
     * 
     * @param elementDescription the description of the element that should be checked for existence
     * @return true if and only the specified element exists while processing the current source
     * @since nb74
     */
    public boolean isAvailable(@NonNull String elementDescription) {
        return context.isAvailable(elementDescription);
    }
}
