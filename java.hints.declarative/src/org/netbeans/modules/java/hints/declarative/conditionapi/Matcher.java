/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.java.hints.declarative.conditionapi;

import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import com.sun.source.util.TreePathScanner;
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
            new TreePathScanner<Void, Void>() {
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
            boolean occurs = new TreePathScanner<Boolean, Void>() {
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
