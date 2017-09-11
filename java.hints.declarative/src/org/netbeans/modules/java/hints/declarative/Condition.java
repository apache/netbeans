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

package org.netbeans.modules.java.hints.declarative;

import com.sun.source.util.TreePath;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import javax.lang.model.type.TypeMirror;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.modules.java.hints.declarative.conditionapi.Context;

/**
 *
 * @author lahvac
 */
public abstract class Condition {

    public final boolean not;

    private Condition(boolean not) {
        this.not = not;
    }

    public abstract boolean holds(Context ctx, boolean global);

    @Override
    public abstract String toString();
    
    public static final class Instanceof extends Condition {

        public final String variable;
        public final String constraint;
        public final int[]  constraintSpan;

        public Instanceof(boolean not, String variable, String constraint, int[]  constraintSpan) {
            super(not);
            this.variable = variable;
            this.constraint = constraint;
            this.constraintSpan = constraintSpan;
        }

        @Override
        public boolean holds(Context ctx, boolean global) {
            if (global && !not) {
                //if this is a global condition, not == false, then the computation should always lead to true
                //note that ctx.getVariables().get(variable) might even by null (implicit this)
                return true;
            }

            TreePath boundTo = APIAccessor.IMPL.getSingleVariable(ctx, ctx.variableForName(variable));
            CompilationInfo info = APIAccessor.IMPL.getHintContext(ctx).getInfo();
            TypeMirror realType = info.getTrees().getTypeMirror(boundTo);
            TypeMirror designedType = Hacks.parseFQNType(info, constraint);

            return not ^ info.getTypes().isSubtype(realType, designedType);
        }

        @Override
        public String toString() {
            return "(INSTANCEOF " + (not ? "!" : "") + variable + "/" + constraint + ")";
        }

    }

    public static final class MethodInvocation extends Condition {

        private final String methodName;
        private final Map<? extends String, ? extends ParameterKind> params;
        private final MethodInvocationContext mic;
        private final AtomicReference<Method> toCall = new AtomicReference<Method>();

        public MethodInvocation(boolean not, String methodName, Map<? extends String, ? extends ParameterKind> params, MethodInvocationContext mic) {
            super(not);
            this.methodName = methodName;
            this.params = params;
            this.mic = mic;
        }

        @Override
        public boolean holds(Context ctx, boolean global) {
            if (toCall.get() == null) {
                //not linked yet?
                if (!link()) {
                    throw new IllegalStateException();
                }
            }

            return mic.invokeMethod(ctx, toCall.get(), params) ^ not;
        }

        boolean link() {
            Method m = mic.linkMethod(methodName, params);

            toCall.set(m);

            return m != null;
        }

        @Override
        public String toString() {
            return "(METHOD_INVOCATION " + (not ? "!" : "") + ":" + methodName + "(" + params.toString() + "))";
        }

        public enum ParameterKind {
            VARIABLE,
            STRING_LITERAL,
            ENUM_CONSTANT;
        }
    }

    public static final class False extends Condition {

        public False() {
            super(false);
        }

        @Override
        public boolean holds(Context ctx, boolean global) {
            return false;
        }

        @Override
        public String toString() {
            return "(FALSE)";
        }
    }

    public static final class Otherwise extends Condition {

        public Otherwise() {
            super(false);
        }

        @Override
        public boolean holds(Context ctx, boolean global) {
            return false;
        }

        @Override
        public String toString() {
            return "(OTHERWISE)";
        }
    }
    
}
