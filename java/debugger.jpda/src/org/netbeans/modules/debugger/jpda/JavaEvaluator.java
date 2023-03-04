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

package org.netbeans.modules.debugger.jpda;

import com.sun.jdi.IncompatibleThreadStateException;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.StackFrame;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.Value;
import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import org.netbeans.api.debugger.jpda.CallStackFrame;
import org.netbeans.api.debugger.jpda.InvalidExpressionException;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.api.debugger.jpda.JPDAThread;
import org.netbeans.modules.debugger.jpda.expr.CompilationInfoHolder;
import org.netbeans.modules.debugger.jpda.expr.EvaluationContext;
import org.netbeans.modules.debugger.jpda.expr.EvaluationException;
import org.netbeans.modules.debugger.jpda.expr.JavaExpression;
import org.netbeans.modules.debugger.jpda.expr.TreeEvaluator;
import org.netbeans.modules.debugger.jpda.expr.VMCache;
import org.netbeans.modules.debugger.jpda.jdi.InternalExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.InvalidStackFrameExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.ObjectCollectedExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.StackFrameWrapper;
import org.netbeans.modules.debugger.jpda.jdi.VMDisconnectedExceptionWrapper;
import org.netbeans.modules.debugger.jpda.models.CallStackFrameImpl;
import org.netbeans.modules.debugger.jpda.models.JPDAThreadImpl;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.spi.debugger.jpda.Evaluator;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 * @author Martin Entlicher
 */
@Evaluator.Registration(language="Java")
public class JavaEvaluator implements Evaluator<JavaExpression> {

    private final JPDADebuggerImpl debugger;
    private final VMCache vmCache;
    private final Map<Value, EvaluationContext.VariableInfo> valueContainers =
            Collections.synchronizedMap(new IdentityHashMap<Value, EvaluationContext.VariableInfo>());

    public JavaEvaluator (ContextProvider lookupProvider) {
        debugger = (JPDADebuggerImpl) lookupProvider.lookupFirst (null, JPDADebugger.class);
        debugger.addPropertyChangeListener(JPDADebugger.PROP_CURRENT_CALL_STACK_FRAME, new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                valueContainers.clear();
            }
        });
        vmCache = new VMCache(debugger);
    }

    @Override
    public Result evaluate(Expression<JavaExpression> expression, final Context context) throws InvalidExpressionException {
        return evaluate(expression, context, null);
    }

    public Result evaluate(Expression<JavaExpression> expression, Context context,
                           CompilationInfoHolder ciHolder) throws InvalidExpressionException {
        JavaExpression expr = expression.getPreprocessedObject();
        if (expr == null) {
            expr = JavaExpression.parse(expression.getExpression(), JavaExpression.LANGUAGE_JAVA_1_5);
            expression.setPreprocessedObject(expr);
        }
        Value v = evaluateIn(expr, context.getCallStackFrame(), context.getStackFrame(), context.getStackDepth(),
                             context.getContextObject(), ciHolder,
                             debugger.methodCallsUnsupportedExc == null,
                             new Runnable() { public void run() { context.notifyMethodToBeInvoked(); } });
        return new Result(v);
    }

    /*@Override
    public Value evaluate(String expression, StackFrame csf, int stackDepth,
                          ObjectReference var, boolean canInvokeMethods,
                          Runnable methodInvokePreprocessor) throws InvalidExpressionException {
        JavaExpression expr = JavaExpression.parse(expression, JavaExpression.LANGUAGE_JAVA_1_5);
        return evaluateIn(expr, csf, stackDepth, var, canInvokeMethods, methodInvokePreprocessor);
    }*/

    // * Might be changed to return a variable with disabled collection. When not used any more,
    // * it's collection must be enabled again.
    private Value evaluateIn (org.netbeans.modules.debugger.jpda.expr.JavaExpression expression,
                              CallStackFrame csf,
                              final StackFrame frame, int frameDepth,
                              ObjectReference var, CompilationInfoHolder ciHolder,
                              boolean canInvokeMethods,
                              Runnable methodInvokePreprocessor) throws InvalidExpressionException {
        // should be already synchronized on the frame's thread
        if (csf == null)
            throw new InvalidExpressionException
                    (NbBundle.getMessage(JPDADebuggerImpl.class, "MSG_NoCurrentContextStackFrame"));

        // TODO: get imports from the source file
        CallStackFrameImpl csfi = (CallStackFrameImpl) csf;
        List<String> imports = new ImportsLazyList(csfi);
        List<String> staticImports = new ArrayList<String>();
        try {
            JPDAThreadImpl trImpl = (JPDAThreadImpl) csf.getThread();
            EvaluationContext context;
            TreeEvaluator evaluator =
                expression.evaluator(
                    context = new EvaluationContext(
                        trImpl,
                        frame,
                        frameDepth,
                        var,
                        imports,
                        staticImports,
                        canInvokeMethods,
                        methodInvokePreprocessor,
                        debugger,
                        vmCache
                    ),
                    ciHolder
                );
            try {
                Value v = evaluator.evaluate ();
                TreePath treePath = context.getTreePath();
                if (treePath != null) {
                    Tree tree = treePath.getLeaf();
                    EvaluationContext.VariableInfo vi = context.getVariableInfo(tree);
                    if (vi != null) {
                        valueContainers.put(v, vi);
                    }
                }
                return v;
            } finally {
                if (debugger.methodCallsUnsupportedExc == null && !context.canInvokeMethods()) {
                    debugger.methodCallsUnsupportedExc =
                            new InvalidExpressionException(new UnsupportedOperationException());
                }
                context.destroy();
            }
        } catch (InternalExceptionWrapper e) {
            throw new InvalidExpressionException(e.getLocalizedMessage());
        } catch (ObjectCollectedExceptionWrapper e) {
            throw new InvalidExpressionException(NbBundle.getMessage(
                TreeEvaluator.class, "CTL_EvalError_collected"));
        } catch (VMDisconnectedExceptionWrapper e) {
            throw new InvalidExpressionException(NbBundle.getMessage(
                TreeEvaluator.class, "CTL_EvalError_disconnected"));
        } catch (InvalidStackFrameExceptionWrapper e) {
            JPDAThreadImpl t = (JPDAThreadImpl) csf.getThread();
            e = Exceptions.attachMessage(e, t.getThreadStateLog());
            Exceptions.printStackTrace(Exceptions.attachMessage(e, "During evaluation of '"+expression.getExpression()+"'")); // Should not occur
            throw new InvalidExpressionException (NbBundle.getMessage(
                    JPDAThreadImpl.class, "MSG_NoCurrentContext"));
        } catch (EvaluationException e) {
            InvalidExpressionException iee = new InvalidExpressionException (e);
            Exceptions.attachMessage(iee, "Expression = '"+expression.getExpression()+"'");
            throw iee;
        } catch (IncompatibleThreadStateException itsex) {
            InvalidExpressionException isex = new InvalidExpressionException(itsex.getLocalizedMessage());
            isex.initCause(itsex);
            throw isex;
        }
    }

    /**
     * Get a variable containing evaluated value, if any.
     * @param v A value
     * @return Info about variable containing the value, or <code>null</code> when
     *         no such variable exist.
     */
    public EvaluationContext.VariableInfo getValueContainer(Value v) {
        return valueContainers.get(v);
    }
    
    private class ImportsLazyList extends AbstractList<String> {
        
        private final CallStackFrameImpl csfi;
        private List<String> imports;
        
        ImportsLazyList(CallStackFrameImpl csfi) {
            this.csfi = csfi;
        }
        
        private synchronized List<String> getImports() {
            if (imports == null) {
                imports = createImports();
            }
            return imports;
        }

        private List<String> createImports() {
            List<String> im = new ArrayList<String>();
            im.add ("java.lang.*");    // NOI18N
            try {
                String[] frameImports = EditorContextBridge.getContext().getImports (
                        debugger.getEngineContext ().getURL (csfi.getStackFrame(), "Java") // NOI18N
                );
                if (frameImports != null) {
                    im.addAll (Arrays.asList (frameImports));
                }
            } catch (InternalExceptionWrapper | InvalidStackFrameExceptionWrapper |
                     ObjectCollectedExceptionWrapper | VMDisconnectedExceptionWrapper ex) {
            }
            return im;
        }
        
        @Override
        public String get(int index) {
            return getImports().get(index);
        }

        @Override
        public int size() {
            return getImports().size();
        }

    }

}
