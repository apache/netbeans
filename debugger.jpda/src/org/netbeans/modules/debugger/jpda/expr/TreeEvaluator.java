/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.debugger.jpda.expr;

import com.sun.jdi.ClassNotLoadedException;
import com.sun.jdi.ClassNotPreparedException;
import com.sun.jdi.ClassObjectReference;
import com.sun.jdi.ClassType;
import com.sun.jdi.IncompatibleThreadStateException;
import com.sun.jdi.InternalException;
import com.sun.jdi.InvalidStackFrameException;
import com.sun.jdi.InvalidTypeException;
import com.sun.jdi.InvocationException;
import com.sun.jdi.Location;
import com.sun.jdi.Method;
import com.sun.jdi.Mirror;
import com.sun.jdi.NativeMethodException;
import com.sun.jdi.ObjectCollectedException;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.StackFrame;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.VMDisconnectedException;
import com.sun.jdi.Value;
import java.beans.PropertyVetoException;
import java.io.IOException;
import java.util.HashMap;

import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.debugger.jpda.InvalidExpressionException;
import org.netbeans.api.debugger.jpda.ObjectVariable;

import org.netbeans.modules.debugger.jpda.EditorContextBridge;
import org.netbeans.modules.debugger.jpda.JDIExceptionReporter;
import org.netbeans.modules.debugger.jpda.JPDADebuggerImpl;
import org.netbeans.modules.debugger.jpda.SourcePath;
import org.netbeans.modules.debugger.jpda.jdi.ClassNotPreparedExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.ClassTypeWrapper;
import org.netbeans.modules.debugger.jpda.jdi.IllegalThreadStateExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.InternalExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.InvalidStackFrameExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.LocationWrapper;
import org.netbeans.modules.debugger.jpda.jdi.ObjectCollectedExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.ObjectReferenceWrapper;
import org.netbeans.modules.debugger.jpda.jdi.ReferenceTypeWrapper;
import org.netbeans.modules.debugger.jpda.jdi.StackFrameWrapper;
import org.netbeans.modules.debugger.jpda.jdi.ThreadReferenceWrapper;
import org.netbeans.modules.debugger.jpda.jdi.UnsupportedOperationExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.VMDisconnectedExceptionWrapper;
import org.netbeans.modules.debugger.jpda.models.JPDAThreadImpl;
import org.netbeans.modules.debugger.jpda.remote.RemoteClass;
import org.netbeans.modules.debugger.jpda.remote.RemoteServices;
import org.netbeans.spi.debugger.jpda.Evaluator;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.Pair;

/**
 *
 * @author Martin Entlicher
 */
public class TreeEvaluator {

    private final JavaExpression expression;
    private final EvaluationContext evaluationContext;
    private final CompilationInfoHolder ciHolder;

    private static final Logger loggerMethod = Logger.getLogger("org.netbeans.modules.debugger.jpda.invokeMethod"); // NOI18N

    TreeEvaluator(JavaExpression expression, EvaluationContext context,
                  CompilationInfoHolder ciHolder) {
        this.expression = expression;
        this.evaluationContext = context;
        this.ciHolder = ciHolder;
    }

    /**
     * Evaluates the expression for which it was created.
     *
     * @return the result of evaluating the expression as a JDI Value object.
     *         It returns null if the result itself is null.
     * @throws EvaluationException if the expression cannot be evaluated for whatever reason
     * @throws IncompatibleThreadStateException if the context thread is in an
     * incompatible state (running, dead)
     */
    public Value evaluate() throws EvaluationException, IncompatibleThreadStateException, InvalidExpressionException, InternalExceptionWrapper, VMDisconnectedExceptionWrapper, InvalidStackFrameExceptionWrapper, ObjectCollectedExceptionWrapper
    {
        //StackFrame frame = evaluationContext.getFrame();
        ThreadReference frameThread = StackFrameWrapper.thread(evaluationContext.getFrame());
        int frameIndex = -1;
        try {
            StackFrame currentFrame;
            int numTries = 100; // Not nice at all, but what can we do when we're randomly invalidated?
            do {
                currentFrame = evaluationContext.getFrame();
                try {
                    frameIndex = indexOf(ThreadReferenceWrapper.frames(frameThread), currentFrame);
                } catch (InvalidStackFrameExceptionWrapper isfex) {
                    if (numTries-- > 0) {
                        continue;
                    } else {
                        throw isfex;
                    }
                }
            } while (false);
        } catch (ObjectCollectedExceptionWrapper ocex) {
            throw new InvalidExpressionException(NbBundle.getMessage(
                TreeEvaluator.class, "CTL_EvalError_collected_context"));
        } catch (IllegalThreadStateExceptionWrapper ex) {
            // Thread died
            throw new InvalidExpressionException(ex.getCause().getLocalizedMessage());
        }
        if (frameIndex == -1) {
            throw new IncompatibleThreadStateException("Thread does not contain current frame");
        }
        /*
        String currentPackage = ReferenceTypeWrapper.name(LocationWrapper.declaringType(
                StackFrameWrapper.location(frame)));
        int idx = currentPackage.lastIndexOf('.');
        currentPackage = (idx > 0) ? currentPackage.substring(0, idx + 1) : "";
        */
        int line;
        String url;
        ObjectReference contextVar =  evaluationContext.getContextVariable();
        if (contextVar != null) {
            String className = contextVar.referenceType().name();
            String relPath = SourcePath.convertClassNameToRelativePath(className);
            url = evaluationContext.getDebugger().getEngineContext().getURL(relPath, true);
            line = EditorContextBridge.getContext().getFieldLineNumber(url, className, null);
        } else {
            line = LocationWrapper.lineNumber(StackFrameWrapper.location(evaluationContext.getFrame()));
            url = evaluationContext.getDebugger().getEngineContext().getURL(evaluationContext.getFrame(), "Java");//evaluationContext.getDebugger().getSession().getCurrentLanguage());
            if (url == null) {
                // Debugger suspended in an unknown location. Evaluator will not work without some context.
                // Try to define some context that should work...
                FileObject systemFO = org.netbeans.api.java.classpath.GlobalPathRegistry.getDefault().findResource("java/lang/System.java");    // NOI18N
                if (systemFO != null) {
                    try {
                        url = systemFO.getURL().toString();
                        line = 100;
                    } catch (FileStateInvalidException ex) {
                    }
                }
            }
        }
        /*try {
            url = frame.location().sourcePath(expression.getLanguage());
        } catch (AbsentInformationException ex) {
            return null;
        }*/
        //Tree exprTree = EditorContextBridge.getExpressionTree(expression.getExpression(), url, line);
        //if (exprTree == null) return null;
        Mirror mirror = null;
        Map<String, ObjectVariable> uploadedClasses = new HashMap<>();
        Evaluator.Expression<Object> evex = new Evaluator.Expression<>(expression.getExpression());
        if (ciHolder != null) {
            evex.setPreprocessedObject(ciHolder.getParsedData());
        }
        try {
            mirror = EditorContextBridge.interpretOrCompileCode(evex, url, line,
                                                                new CanInterpretVisitor(),
                                                                new EvaluatorVisitor(expression), evaluationContext,
                                                                evaluationContext.getContextObject() == null,
                                                                (namedClass) -> uploadClass(namedClass, uploadedClasses, evaluationContext),
                                                                evaluationContext.getDebugger().getEngineContext().getContext());
            if (mirror instanceof EvaluatorVisitor.ArtificialMirror) {
                mirror = ((EvaluatorVisitor.ArtificialMirror) mirror).getVMMirror();
            }
            if (mirror instanceof Value || mirror == null) {
                return (Value) mirror;
            } else {
                throw new InvalidExpressionException(NbBundle.getMessage(
                    TreeEvaluator.class, "CTL_EvalError_notAValue") + ": " + expression.getExpression());
            }
            //return exprTree.accept(new EvaluatorVisitor(), evaluationContext);
        } catch (IllegalStateException isex) {
            loggerMethod.log(Level.CONFIG, "During evaluation of '"+expression.getExpression()+"'", isex); // Just log the expression.
            Throwable thr = isex.getCause();
            if (thr instanceof IncompatibleThreadStateException) {
                throw (IncompatibleThreadStateException) thr;
            }
            if (thr instanceof InvalidExpressionException) {
                throw (InvalidExpressionException) thr;
            }
            if (thr instanceof ClassNotLoadedException) {
                throw new InvalidExpressionException("Class "+((ClassNotLoadedException) thr).className()+" not loaded.");
            }
            throw isex;
        } catch (InternalException e) {
            if (Exceptions.findLocalizedMessage(e) != null) {
                throw new InvalidExpressionException (Exceptions.findLocalizedMessage(e));
            } else {
                JDIExceptionReporter.report(e);
                throw new InvalidExpressionException (e.getLocalizedMessage());
            }
        } catch (VMDisconnectedException e) {
            throw new InvalidExpressionException(NbBundle.getMessage(
                TreeEvaluator.class, "CTL_EvalError_disconnected"));
        } catch (ObjectCollectedException e) {
            loggerMethod.log(Level.CONFIG, "During evaluation of '"+expression.getExpression()+"'", e); // Just log it.
            throw new InvalidExpressionException(NbBundle.getMessage(
                TreeEvaluator.class, "CTL_EvalError_collected"));
        } catch (ClassNotPreparedException e) {
            throw new InvalidExpressionException (e);
        } catch (NativeMethodException e) {
            throw new InvalidExpressionException (e.getLocalizedMessage());
        } catch (InvalidStackFrameException e) {
            JPDAThreadImpl t = evaluationContext.getThread();
            e = Exceptions.attachMessage(e, t.getThreadStateLog());
            Exceptions.printStackTrace(Exceptions.attachMessage(e, "During evaluation of '"+expression.getExpression()+"'")); // Should not occur
            throw new InvalidExpressionException (NbBundle.getMessage(
                    JPDAThreadImpl.class, "MSG_NoCurrentContext"));
        } catch (RuntimeException re) {
            re = Exceptions.attachMessage(re, "During evaluation of '"+expression.getExpression()+"'"); // NOI18N
            throw re;
        } finally {
            // Garbage collection for the returned value "mirror" is left disabled. Context enable it as soon as the thread is resumed.
            evaluationContext.enableCollectionOfObjects((mirror instanceof Value) ? ((Value) mirror) : null);
            for (ObjectVariable var : uploadedClasses.values()) {
                evaluationContext.getDebugger().markObject(var, null);
            }
            if (ciHolder != null) {
                ciHolder.setParsedData(evex.getPreprocessedObject());
            }
        }
    }

    private int indexOf(List<StackFrame> frames, StackFrame frame) throws InternalExceptionWrapper, VMDisconnectedExceptionWrapper, InvalidStackFrameExceptionWrapper {
        int n = frames.size();
        Location loc = StackFrameWrapper.location(frame);
        for (int i = 0; i < n; i++) {
            if (loc.equals(StackFrameWrapper.location(frames.get(i)))) return i;
        }
        return -1;
    }

    public static Value invokeVirtual (
        ObjectReference objectReference,
        Method method,
        ThreadReference evaluationThread,
        List<Value> args,
        JPDADebuggerImpl debugger
     ) throws InvalidExpressionException {
        return invokeVirtual(objectReference, null, method, evaluationThread, args, debugger, null);
    }

    public static Value invokeVirtual (
        ObjectReference objectReference,
        Method method,
        ThreadReference evaluationThread,
        List<Value> args,
        JPDADebuggerImpl debugger,
        InvocationExceptionTranslated existingInvocationException
     ) throws InvalidExpressionException {
        return invokeVirtual(objectReference, null, method, evaluationThread, args, debugger, existingInvocationException);
    }

    public static Value invokeVirtual (
        ClassType classType,
        Method method,
        ThreadReference evaluationThread,
        List<Value> args,
        JPDADebuggerImpl debugger
     ) throws InvalidExpressionException {
        return invokeVirtual(null, classType, method, evaluationThread, args, debugger, null);
    }

    public static Value invokeVirtual (
        ClassType classType,
        Method method,
        ThreadReference evaluationThread,
        List<Value> args,
        JPDADebuggerImpl debugger,
        InvocationExceptionTranslated existingInvocationException
     ) throws InvalidExpressionException {
        return invokeVirtual(null, classType, method, evaluationThread, args, debugger, existingInvocationException);
    }

    private static Value invokeVirtual (
        ObjectReference objectReference,
        ClassType classType,
        Method method,
        ThreadReference evaluationThread,
        List<Value> args,
        JPDADebuggerImpl debugger,
        InvocationExceptionTranslated existingInvocationException
     ) throws InvalidExpressionException {

        try {
            if (loggerMethod.isLoggable(Level.FINE)) {
                if (objectReference != null) {
                    loggerMethod.fine("STARTED : "+objectReference+"."+method+" ("+args+") in thread "+evaluationThread);
                } else {
                    loggerMethod.fine("STARTED : "+classType+"."+method+" ("+args+") in thread "+evaluationThread);
                }
            }
            EvaluatorVisitor.autoboxArguments(method.argumentTypes(), args, evaluationThread, null);
            Value value;
            if (objectReference != null) {
                value =
                    ObjectReferenceWrapper.invokeMethod(objectReference, evaluationThread, method,
                                                        args,
                                                        ObjectReference.INVOKE_SINGLE_THREADED);
            } else {
                value =
                    ClassTypeWrapper.invokeMethod(classType, evaluationThread, method,
                                                  args,
                                                  ObjectReference.INVOKE_SINGLE_THREADED);
            }
            if (loggerMethod.isLoggable(Level.FINE)) {
                loggerMethod.fine("   return = "+value);
            }
            return value;
        } catch (InvalidTypeException itex) {
            throw new InvalidExpressionException (itex);
        } catch (ClassNotLoadedException cnlex) {
            throw new InvalidExpressionException (cnlex);
        } catch (IncompatibleThreadStateException itsex) {
            String message = NbBundle.getMessage(EvaluatorVisitor.class, "MSG_IncompatibleThreadStateMessage");
            InvalidExpressionException ieex = new InvalidExpressionException (message, itsex);
            throw ieex;
        } catch (InvocationException iex) {
            InvocationExceptionTranslated ex;
            if (existingInvocationException != null) {
                ex = existingInvocationException;
            } else {
                ex = new InvocationExceptionTranslated(iex, debugger);
                JPDAThreadImpl trImpl = debugger.getThread(evaluationThread);
                { // Init exception translation:
                    ex.setPreferredThread(trImpl);
                    trImpl.notifyMethodInvokeDone();
                    ex.getMessage();
                    ex.getLocalizedMessage();
                    ex.getStackTrace();
                }
            }
            InvalidExpressionException ieex = new InvalidExpressionException (ex, true);
            throw ieex;
        } catch (UnsupportedOperationException uoex) {
            InvalidExpressionException ieex = new InvalidExpressionException (uoex);
            throw ieex;
        } catch (InternalExceptionWrapper iex) {
            throw new InvalidExpressionException(iex.getLocalizedMessage());
        } catch (ObjectCollectedExceptionWrapper ocex) {
            throw new InvalidExpressionException(NbBundle.getMessage(
                TreeEvaluator.class, "CTL_EvalError_collected"));
        } catch (VMDisconnectedExceptionWrapper e) {
            throw new InvalidExpressionException(NbBundle.getMessage(
                TreeEvaluator.class, "CTL_EvalError_disconnected"));
        } finally {
            if (loggerMethod.isLoggable(Level.FINE)) {
                if (objectReference != null) {
                    loggerMethod.fine("FINISHED: "+objectReference+"."+method+" ("+args+") in thread "+evaluationThread);
                } else {
                    loggerMethod.fine("FINISHED: "+classType+"."+method+" ("+args+") in thread "+evaluationThread);
                }
            }
        }
    }
    
    private boolean uploadClass(Pair<String, byte[]> namedClass,
                                Map<String, ObjectVariable> classes,
                                EvaluationContext evaluationContext) {
        ObjectVariable clazz = uploadClass(namedClass);
        if (clazz != null) {
            String className = namedClass.first();
            int simpleNameIndex = className.replace('$', '.').lastIndexOf('.');
            if (simpleNameIndex > 0) {
                className = className.substring(simpleNameIndex + 1);
            }
            classes.put(className, clazz);
            evaluationContext.getDebugger().markObject(clazz, className);
            return true;
        } else {
            return false;
        }
    }

    private ObjectVariable uploadClass(Pair<String, byte[]> namedClass) {
        if (!evaluationContext.canInvokeMethods()) {
            return null;
        }
        evaluationContext.methodToBeInvoked();
        try {
            ClassObjectReference newClass = RemoteServices.uploadClass(
                    evaluationContext.getThread().getThreadReference(),
                    new RemoteClass(namedClass.first(), namedClass.second()));
            if (newClass != null) {
                evaluationContext.registerDisabledCollectionOf(newClass);
                return (ObjectVariable) evaluationContext.getDebugger().getVariable(newClass);
            } else {
                return null;
            }
        } catch (InvalidTypeException | ClassNotLoadedException |
                 IncompatibleThreadStateException |
                 IOException | PropertyVetoException | InternalExceptionWrapper |
                 ObjectCollectedExceptionWrapper |
                 UnsupportedOperationExceptionWrapper | ClassNotPreparedExceptionWrapper ex) {
            Exceptions.printStackTrace(ex);
            return null;
        } catch (InvocationException iex) {
            InvocationExceptionTranslated ex = new InvocationExceptionTranslated(iex, evaluationContext.getDebugger());
            JPDAThreadImpl trImpl = evaluationContext.getThread();
            { // Init exception translation:
                ex.setPreferredThread(trImpl);
                trImpl.notifyMethodInvokeDone();
                ex.getMessage();
                ex.getLocalizedMessage();
                ex.getStackTrace();
            }
            InvalidExpressionException ieex = new InvalidExpressionException (ex, true);
            Exceptions.printStackTrace(ieex);
            return null;
        } catch (VMDisconnectedExceptionWrapper ex) {
            return null;
        }
    }
}
