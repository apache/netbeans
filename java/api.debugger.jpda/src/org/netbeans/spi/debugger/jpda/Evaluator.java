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

package org.netbeans.spi.debugger.jpda;

import com.sun.jdi.ClassType;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.StackFrame;
import com.sun.jdi.Value;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.netbeans.api.debugger.jpda.CallStackFrame;
import org.netbeans.api.debugger.jpda.InvalidExpressionException;
import org.netbeans.api.debugger.jpda.ObjectVariable;
import org.netbeans.api.debugger.jpda.Variable;
import org.netbeans.spi.debugger.ContextProvider;
import org.openide.util.Lookup;

/**
 * Evaluator service for a language that compiles into bytecode.
 * Implementation class should register using the annotation {@link Registration} for the desired language.
 *
 * @author Martin Entlicher
 *
 * @since 2.21
 */
public interface Evaluator<PreprocessedInfo> {

    /**
     * Evaluates given expression and provide the result.
     *
     * @param expression the expression to be evaluated
     * @param context the context in which the expression is evaluated
     * @return value of evaluated expression
     * @throws InvalidExpressionException when the expression is invalid or other
     * error occurs during the evaluation process.
     */
    Result evaluate(Expression<PreprocessedInfo> expression, Context context) throws InvalidExpressionException;

    /**
     * Representation of an expression that is a subject of evaluation.
     * String expressions are evaluated. But this class allows to attach
     * a custom preprocessed structure to the expression,
     * which can be used on subsequent evaluations of the same expression.
     * Clients can, but does not have to, set a pre-processed object and
     * during repeated evaluations of the same expression use that
     * pre-processed object to speed up the evaluation. The preprocessed
     * object should not be {@link Context}-sensitive.
     */
    public static final class Expression<PreprocessedInfo> {

        private String expression;
        private PreprocessedInfo preprocessed;

        /**
         * Creates a new expression from the string representation.
         * @param expression The string expression.
         */
        public Expression(String expression) {
            this.expression = expression;
        }

        /**
         * Get the string representation of this expression.
         * @return string expression
         */
        public String getExpression() {
            return expression;
        }

        /**
         * Set a pre-processed object of the string expression.
         * A custom, client-specific object is expected, which represents
         * the expression. This can be a syntax tree structure or whatever
         * that speeds up repeated evaluations of the same expression.
         * 
         * @param preprocessed object holding the information about pre-processed
         *        expression
         */
        public void setPreprocessedObject(PreprocessedInfo preprocessed) {
            this.preprocessed = preprocessed;
        }

        /**
         * Get the pre-processed object of the string expression.
         * The object set by {@link #setPreprocessedObject(java.lang.Object)} is
         * returned.
         *
         * @return the preprocessed object or <code>null</code>.
         */
        public PreprocessedInfo getPreprocessedObject() {
            return preprocessed;
        }
    }

    /**
     * Context of the evaluation.
     * This class provides the evaluation context - stack frame and context variable.
     * Two sets of APIs can be used during the evaluation:
     * <ul>
     *    <li>JPDA Debugger API (which is a safe abstraction of JDI,
     *                           but with limited functionality)<br>
     *        Provided {@link CallStackFrame} and {@link ObjectVariable}
     *        can be used to compute the resulting {@link Variable}.
     *    </li>
     *    <li>JDI API (providing full access, but can throw unexpected exceptions
     *                 and clients must notify the context about method invocations)<br>
     *        Provided {@link StackFrame} and {@link ObjectReference} can be used
     *        to compute the resulting {@link Value}. When a method invocation
     *        is necessary, {@link Context#notifyMethodToBeInvoked()} must be called
     *        before the method invocation.
     *    </li>
     * </ul>
     */
    public static final class Context {

        private CallStackFrame callStackFrame;
        private ObjectVariable contextVariable;
        private StackFrame stackFrame;
        private int stackDepth;
        private ObjectReference contextObject;
        private Runnable methodToBeInvokedNotifier;

        /** Creates the context, do not call directly */
        public Context(Lookup context) {
            this.callStackFrame = context.lookup(CallStackFrame.class);
            this.contextVariable = context.lookup(ObjectVariable.class);
            this.stackFrame = context.lookup(StackFrame.class);
            this.stackDepth = context.lookup(Integer.class);
            this.contextObject = context.lookup(ObjectReference.class);
            this.methodToBeInvokedNotifier = context.lookup(Runnable.class);
        }

        /**
         * Get the context call stack frame.
         * This frame corresonds to the JDI frame returned from {@link #getStackFrame()}.
         * @return call stack frame in which the evaluation is performed
         */
        public CallStackFrame getCallStackFrame() {
            return callStackFrame;
        }

        /**
         * Get an optional context variable. When non-null,
         * all methods and fields should be treated relative to the variable
         * instance.
         * This variable corresonds to the JDI reference returned from {@link #getContextObject()}.
         * @return optional context variable or <code>null</code>.
         */
        public ObjectVariable getContextVariable() {
            return contextVariable;
        }

        /**
         * Get the context stack frame in JDI APIs.
         * This frame corresonds to the JPDA frame returned from {@link #getCallStackFrame()}.
         * @return stack frame in which the evaluation is performed
         */
        public StackFrame getStackFrame() {
            return stackFrame;
        }

        /**
         * Get the depth of stack frame returned from {@link #getStackFrame()}.
         * @return the depth of stack frame
         */
        public int getStackDepth() {
            return stackDepth;
        }

        /**
         * Get an optional context object. When non-null,
         * all methods and fields should be treated relative to the object
         * instance.
         * This object corresonds to the JPDA variable returned from {@link #getContextVariable()}.
         * @return optional context object or <code>null</code>.
         */
        public ObjectReference getContextObject() {
            return contextObject;
        }

        /**
         * This method is <b>required</b> to be called before a call to JDI
         * that cause the current thread (<code>sf.thread()</code>) to resume - e.g.
         * {@link ObjectReference#invokeMethod(com.sun.jdi.ThreadReference, com.sun.jdi.Method, java.util.List, int)},
         * {@link ClassType#invokeMethod(com.sun.jdi.ThreadReference, com.sun.jdi.Method, java.util.List, int)},
         * {@link ClassType#newInstance(com.sun.jdi.ThreadReference, com.sun.jdi.Method, java.util.List, int)}.
         */
        public void notifyMethodToBeInvoked() {
            methodToBeInvokedNotifier.run();
        }
    }

    /**
     * Evaluation result.
     * Depending on the APIs used by the evaluation, result is either
     * a {@link Variable} or {@link Value}.
     */
    public static final class Result {

        private Variable var;
        private Value v;

        /**
         * Create result from {@link Variable}.
         * @param var result variable
         */
        public Result(Variable var) {
            this.var = var;
        }

        /**
         * Create result from {@link Value}.
         * @param v result value
         */
        public Result(Value v) {
            this.v = v;
        }

        /**
         * Get the result variable
         * @return the variable or <code>null</code>.
         */
        public Variable getVariable() {
            return var;
        }

        /**
         * Get the result value
         * @return the value or <code>null</code>.
         */
        public Value getValue() {
            return v;
        }
    }
    
    /**
     * Declarative registration of Evaluator implementation.
     * By marking the implementation class with this annotation,
     * you automatically register that implementation for use by debugger.
     * The class must be public and have a public constructor which takes
     * no arguments or takes {@link ContextProvider} as an argument.
     */
    @Retention(RetentionPolicy.SOURCE)
    @Target({ElementType.TYPE})
    public @interface Registration {
        /**
         * The language to register this evaluator for.
         */
        String language();

    }

}
