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

package org.netbeans.modules.java.hints.declarative.conditionapi;

import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import org.netbeans.api.java.source.support.ErrorAwareTreePathScanner;
import java.util.Collection;
import java.util.LinkedList;
import javax.lang.model.element.Element;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.spi.java.hints.MatcherUtilities;

/**
 *
 * @author lahvac
 */
public final class Matcher {

    private final Context ctx;

    //XXX: should not be public:
    public Matcher(Context ctx) {
        this.ctx = ctx;
    }

    public boolean matches(@NonNull Variable var, @NonNull String pattern) {
        return matchesAny(var, pattern);
    }

    public boolean matchesAny(@NonNull Variable var, @NonNull String /*of @NonNull*/... patterns) {
        Iterable<? extends TreePath> paths = ctx.getVariable(var);

        if (paths == null) return false;

        for (String pattern : patterns) {
            for (TreePath toSearch : paths) {
                if (MatcherUtilities.matches(ctx.ctx, toSearch, pattern)) {
                    return true;
                }
            }
        }

        return false;
    }

    public boolean containsAny(@NonNull Variable var, @NonNull final String /*of @NonNull*/... patterns) {
        Iterable<? extends TreePath> paths = ctx.getVariable(var);
        final boolean[] result = new boolean[1];

        if (paths == null) return false;

        for (TreePath toSearch : paths) {
            //XXX:
            new ErrorAwareTreePathScanner<Void, Void>() {
                @Override
                public Void scan(Tree tree, Void p) {
                    if (tree == null) return null;

                    TreePath tp = new TreePath(getCurrentPath(), tree);

                    for (String pattern : patterns) {
                        if (MatcherUtilities.matches(ctx.ctx, tp, pattern)) {
                            result[0] = true;
                        }
                    }

                    return super.scan(tree, p);
                }
            }.scan(toSearch, null);
        }

        return result[0];
    }

    public boolean referencedIn(@NonNull Variable variable, @NonNull Variable in) {
        final Element e = ctx.ctx.getInfo().getTrees().getElement(ctx.getSingleVariable(variable));

        if (e == null) { //TODO: check also error
            return false;
        }

        for (TreePath tp : ctx.getVariable(in)) {
            boolean occurs = new ErrorAwareTreePathScanner<Boolean, Void>() {
                @Override
                public Boolean scan(Tree tree, Void p) {
                    if (tree == null) {
                        return false;
                    }

                    TreePath currentPath = new TreePath(getCurrentPath(), tree);
                    Element currentElement = ctx.ctx.getInfo().getTrees().getElement(currentPath);

                    if (e.equals(currentElement)) {
                        return true; //TODO: throwing an exception might be faster...
                    }

                    return super.scan(tree, p);
                }

                @Override
                public Boolean reduce(Boolean r1, Boolean r2) {
                    if (r1 == null) {
                        return r2;
                    }

                    if (r2 == null) {
                        return r1;
                    }

                    return r1 || r2;
                }

            }.scan(tp, null) == Boolean.TRUE;

            if (occurs) {
                return true;
            }
        }

        return false;
    }

    public boolean matchesWithBind(Variable var, String pattern) {
        TreePath path = ctx.getSingleVariable(var);

        if (path != null) {
            return MatcherUtilities.matches(ctx.ctx, path, pattern, ctx.variables.get(0), ctx.multiVariables.get(0), ctx.variableNames.get(0));
        }

        Iterable<? extends Variable> multiVar = ctx.getIndexedVariables(var);
        Collection<TreePath> multi = new LinkedList<TreePath>();

        for (Variable v : multiVar) {
            multi.add(ctx.getSingleVariable(v));
        }

        return MatcherUtilities.matches(ctx.ctx, multi, pattern, ctx.variables.get(0), ctx.multiVariables.get(0), ctx.variableNames.get(0));
    }

}
