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

package org.netbeans.modules.java.hints.declarative;

import com.sun.source.util.TreePath;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import javax.lang.model.type.TypeMirror;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.modules.java.hints.declarative.conditionapi.Context;
import org.netbeans.modules.java.hints.declarative.conditionapi.Variable;

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
            TreePath boundTo = APIAccessor.IMPL.getSingleVariable(ctx, new Variable(variable));

            if (boundTo == null) {
                throw new IllegalStateException();
            }

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
        private final AtomicReference<Method> toCall = new AtomicReference<>();

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
            INT_LITERAL,
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
