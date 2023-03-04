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

package org.netbeans.modules.debugger.jpda.expr;

import com.sun.jdi.ArrayReference;
import com.sun.jdi.ClassType;
import com.sun.jdi.Field;
import com.sun.jdi.IntegerValue;
import com.sun.jdi.InvocationException;
import com.sun.jdi.Method;
import com.sun.jdi.ObjectCollectedException;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.StringReference;
import com.sun.jdi.VMMismatchException;
import com.sun.jdi.Value;
import com.sun.jdi.VirtualMachine;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.debugger.jpda.InvalidExpressionException;
import org.netbeans.api.debugger.jpda.JPDAThread;
import org.netbeans.api.debugger.jpda.ObjectVariable;
import org.netbeans.modules.debugger.jpda.JPDADebuggerImpl;
import org.netbeans.modules.debugger.jpda.jdi.ArrayReferenceWrapper;
import org.netbeans.modules.debugger.jpda.jdi.ClassNotPreparedExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.ClassTypeWrapper;
import org.netbeans.modules.debugger.jpda.jdi.IntegerValueWrapper;
import org.netbeans.modules.debugger.jpda.jdi.InternalExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.ObjectCollectedExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.ObjectReferenceWrapper;
import org.netbeans.modules.debugger.jpda.jdi.ReferenceTypeWrapper;
import org.netbeans.modules.debugger.jpda.jdi.StringReferenceWrapper;
import org.netbeans.modules.debugger.jpda.jdi.TypeWrapper;
import org.netbeans.modules.debugger.jpda.jdi.VMDisconnectedExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.ValueWrapper;
import org.netbeans.modules.debugger.jpda.jdi.VirtualMachineWrapper;
import org.netbeans.modules.debugger.jpda.models.JPDAThreadImpl;
import org.openide.util.Exceptions;

/**
 *
 * @author Martin
 */
public class InvocationExceptionTranslated extends Exception {
    
    private static final Logger logger = Logger.getLogger(InvocationExceptionTranslated.class.getName());
    
    private ObjectReference exeption;
    private JPDADebuggerImpl debugger;
    private JPDAThreadImpl preferredThread;
    
    private String invocationMessage;
    private String message;
    private String localizedMessage;
    private Throwable cause;
    private StackTraceElement[] stackTrace;
    private final Throwable createdAt;

    public InvocationExceptionTranslated(InvocationException iex, JPDADebuggerImpl debugger) {
        this(iex.getMessage(), iex.exception(), debugger);
    }

    public InvocationExceptionTranslated(ObjectVariable exception, JPDADebuggerImpl debugger) {
        this(null, (ObjectReference) ((JDIVariable) exception).getJDIValue(), debugger);
    }

    private InvocationExceptionTranslated(String invocationMessage, ObjectReference exeption, JPDADebuggerImpl debugger) {
        super(InvocationException.class.getName(), null);
        this.invocationMessage = invocationMessage;
        this.exeption = exeption;
        this.debugger = debugger;
        VirtualMachine evm = exeption.virtualMachine();
        VirtualMachine dvm = debugger.getVirtualMachine();
        if (evm != dvm) {
            logger.log(Level.INFO,
                       invocationMessage+
                       ",\n evm = "+printVM(evm)+",\n dvm = "+printVM(dvm),     // NOI18N
                       new IllegalStateException("Stack Trace Info"));          // NOI18N
        }
        this.createdAt = new Throwable().fillInStackTrace();
    }
    
    public void resetInvocationMessage() {
        this.invocationMessage = null;
    }

    public void setPreferredThread(JPDAThreadImpl preferredThread) {
        this.preferredThread = preferredThread;
    }
    
    public InvocationExceptionTranslated preload(JPDAThreadImpl preferredThread) {
        this.preferredThread = preferredThread;
        getMessage();
        getLocalizedMessage();
        Throwable c = getCause();
        getStackTrace();
        toString();
        if (c instanceof InvocationExceptionTranslated) {
            ((InvocationExceptionTranslated) c).preload(preferredThread);
        }
        return this;
    }
    
    @Override
    public synchronized String getMessage() {
        if (message == null) {
            try {
                Method getMessageMethod = ClassTypeWrapper.concreteMethodByName((ClassType) ValueWrapper.type(exeption),
                            "getMessage", "()Ljava/lang/String;");  // NOI18N
                if (getMessageMethod == null) {
                    if (invocationMessage != null) {
                        message = "";
                    } else {
                        message = "Unknown exception message";
                    }
                } else {
                    try {
                        StringReference sr = null;
                        while (sr == null) {
                            sr = (StringReference) debugger.invokeMethod (
                                preferredThread,
                                exeption,
                                getMessageMethod,
                                new Value [0],
                                this
                            );
                            if (sr == null) break;
                            try {
                                sr.disableCollection();
                            } catch (ObjectCollectedException ex) {
                                sr = null;
                            }
                        }
                        if (sr != null) {
                            message = StringReferenceWrapper.value(sr);
                            sr.enableCollection();
                        } else {
                            message = ""; // NOI18N
                        }
                    } catch (InvalidExpressionException ex) {
                        if (ex.getTargetException() == this) {
                            String msg = getMessageFromField();
                            if (msg == null) {
                                if (invocationMessage != null) {
                                    message = "";
                                } else {
                                    message = "Unknown exception message";
                                }
                            }
                        } else {
                            return ex.getMessage();
                        }
                    } catch (VMMismatchException vmMismatchEx) {
                        VirtualMachine ptvm = ((preferredThread != null) ? preferredThread.getThreadReference().virtualMachine() : null);
                        VirtualMachine ctvm = null;
                        JPDAThread currentThread = debugger.getCurrentThread();
                        if (currentThread != null) {
                            ctvm = ((JPDAThreadImpl) currentThread).getThreadReference().virtualMachine();
                        }
                        throw Exceptions.attachMessage(vmMismatchEx, "DBG VM = "+printVM(debugger.getVirtualMachine())+
                                                                   ", preferredThread VM = "+printVM(ptvm)+
                                                                   ", currentThread VM = "+printVM(ctvm)+
                                                                   ", exeption VM = "+printVM(exeption.virtualMachine()));
                    }
                }
            } catch (InternalExceptionWrapper iex) {
                return iex.getMessage();
            } catch (VMDisconnectedExceptionWrapper vdex) {
                return vdex.getMessage();
            } catch (ObjectCollectedExceptionWrapper ocex) {
                Exceptions.printStackTrace(ocex);
                return ocex.getMessage();
            } catch (ClassNotPreparedExceptionWrapper cnpex) {
                return cnpex.getMessage();
            }
        }
        if (invocationMessage != null) {
            return invocationMessage + ": " + message;
        } else {
            return message;
        }
    }
    
    private String getMessageFromField() throws InternalExceptionWrapper,
                                                VMDisconnectedExceptionWrapper,
                                                ObjectCollectedExceptionWrapper,
                                                ClassNotPreparedExceptionWrapper {
        List<ReferenceType> throwableClasses = VirtualMachineWrapper.classesByName(exeption.virtualMachine(), Throwable.class.getName());
        if (throwableClasses.isEmpty()) {
            return null;
        }
        Field detailMessageField = ReferenceTypeWrapper.fieldByName(throwableClasses.get(0), "detailMessage");
        if (detailMessageField != null) {
            Value messageValue = ObjectReferenceWrapper.getValue(exeption, detailMessageField);
            if (messageValue instanceof StringReference) {
                message = StringReferenceWrapper.value((StringReference) messageValue);
                if (invocationMessage != null) {
                    return invocationMessage + ": " + message;
                } else {
                    return message;
                }
            }
        }
        return null;
    }
    
    static String printVM(VirtualMachine vm) {
        if (vm == null) {
            return "null";
        }
        String sequenceNumber;
        try {
            java.lang.reflect.Field sequenceNumberField = vm.getClass().getDeclaredField("sequenceNumber");
            sequenceNumberField.setAccessible(true);
            Object sn = sequenceNumberField.get(vm);
            sequenceNumber = Objects.toString(sn);
        } catch (Exception ex) {
            sequenceNumber = ex.toString();
            logger.log(Level.INFO, "Retrieving VM's sequenceNumber", ex);
        }
        String target;
        try {
            java.lang.reflect.Field targetField = vm.getClass().getDeclaredField("target");
            targetField.setAccessible(true);
            Object t = targetField.get(vm);
            target = Objects.toString(t);
        } catch (Exception ex) {
            target = ex.toString();
            logger.log(Level.INFO, "Retrieving VM's target", ex);
        }
        return vm.toString() + " #"+sequenceNumber+"["+vm.name()+", "+vm.description()+", "+vm.version()+"\nTargetVM="+target+"]";
    }

    @Override
    public String getLocalizedMessage() {
        if (localizedMessage == null) {
            try {
                Method getMessageMethod = ClassTypeWrapper.concreteMethodByName((ClassType) ValueWrapper.type(exeption),
                            "getLocalizedMessage", "()Ljava/lang/String;");  // NOI18N
                if (getMessageMethod == null) {
                    if (invocationMessage != null) {
                        localizedMessage = "";
                    } else {
                        localizedMessage = "Unknown exception message";
                    }
                } else {
                    try {
                        StringReference sr = null;
                        while (sr == null) {
                            sr = (StringReference) debugger.invokeMethod (
                                preferredThread,
                                exeption,
                                getMessageMethod,
                                new Value [0],
                                this
                            );
                            if (sr == null) break;
                            try {
                                sr.disableCollection();
                            } catch (ObjectCollectedException ex) {
                                sr = null;
                            }
                        }
                        if (sr != null) {
                            localizedMessage = StringReferenceWrapper.value(sr);
                            sr.enableCollection();
                        } else {
                            localizedMessage = ""; // NOI18N
                        }
                    } catch (InvalidExpressionException ex) {
                        if (ex.getTargetException() == this) {
                            String msg = getMessageFromField();
                            if (msg == null) {
                                if (invocationMessage != null) {
                                    localizedMessage = "";
                                } else {
                                    localizedMessage = "Unknown exception message";
                                }
                            }
                        } else {
                            return ex.getLocalizedMessage();
                        }
                    } catch (AssertionError ae) {
                        Exceptions.printStackTrace(new IllegalStateException("Do preload the exception content", createdAt));
                        throw ae;
                    }
                }
            } catch (InternalExceptionWrapper iex) {
                return iex.getMessage();
            } catch (VMDisconnectedExceptionWrapper vdex) {
                return vdex.getMessage();
            } catch (ObjectCollectedExceptionWrapper ocex) {
                Exceptions.printStackTrace(ocex);
                return ocex.getMessage();
            } catch (ClassNotPreparedExceptionWrapper cnpex) {
                return cnpex.getMessage();
            }
        }
        if (invocationMessage != null) {
            return invocationMessage + ": " + localizedMessage;
        } else {
            return localizedMessage;
        }
    }
    
    
    
    public String getOriginalLocalizedMessage() {
        getLocalizedMessage();
        return localizedMessage;
    }

    @Override
    public synchronized Throwable getCause() {
        if (cause == null) {
            try {
                Method getCauseMethod = ClassTypeWrapper.concreteMethodByName((ClassType) ValueWrapper.type(exeption),
                            "getCause", "()Ljava/lang/Throwable;");  // NOI18N
                try {
                    ObjectReference or;
                    if (getCauseMethod == null) {
                        or = null;
                    } else {
                        or = (ObjectReference) debugger.invokeMethod (
                            preferredThread,
                            exeption,
                            getCauseMethod,
                            new Value [0],
                            this
                        );
                    }
                    if (or != null) {
                        cause = new InvocationExceptionTranslated(null, or, debugger);
                    } else {
                        cause = this;
                    }
                } catch (InvalidExpressionException ex) {
                    if (ex.getTargetException() == this) {
                        cause = this;
                    }
                    return null;
                }
            } catch (InternalExceptionWrapper iex) {
                return null;
            } catch (VMDisconnectedExceptionWrapper vdex) {
                return null;
            } catch (ObjectCollectedExceptionWrapper vdex) {
                return null;
            } catch (ClassNotPreparedExceptionWrapper cnpex) {
                return null;
            }
        }
        return (cause == this ? null : cause);
    }

    /**
     * Hard copy from Throwable, our implementation of getOurStackTrace() is used.
     */
    @Override
    public void printStackTrace(PrintStream s) {
        synchronized (s) {
            s.println(this);
            StackTraceElement[] trace = getOurStackTrace();
            for (int i=0; i < trace.length; i++)
                s.println("\tat " + trace[i]);

            InvocationExceptionTranslated ourCause = (InvocationExceptionTranslated) getCause();
            if (ourCause != null)
                ourCause.printStackTraceAsCause(s, trace);
        }
    }

    /**
     * Print our stack trace as a cause for the specified stack trace.
     * 
     * Hard copy from Throwable, our implementation of getOurStackTrace() is used.
     */
    private void printStackTraceAsCause(PrintStream s,
                                        StackTraceElement[] causedTrace)
    {
        // assert Thread.holdsLock(s);

        // Compute number of frames in common between this and caused
        StackTraceElement[] trace = getOurStackTrace();
        int m = trace.length-1, n = causedTrace.length-1;
        while (m >= 0 && n >=0 && trace[m].equals(causedTrace[n])) {
            m--; n--;
        }
        int framesInCommon = trace.length - 1 - m;

        s.println("Caused by: " + this);
        for (int i=0; i <= m; i++)
            s.println("\tat " + trace[i]);
        if (framesInCommon != 0)
            s.println("\t... " + framesInCommon + " more");

        // Recurse if we have a cause
        InvocationExceptionTranslated ourCause = (InvocationExceptionTranslated) getCause();
        if (ourCause != null)
            ourCause.printStackTraceAsCause(s, trace);
    }

    /**
     * Hard copy from Throwable, our implementation of getOurStackTrace() is used.
     */
    @Override
    public void printStackTrace(PrintWriter s) {
        synchronized (s) {
            s.println(this);
            StackTraceElement[] trace = getOurStackTrace();
            for (int i=0; i < trace.length; i++)
                s.println("\tat " + trace[i]);

            InvocationExceptionTranslated ourCause = (InvocationExceptionTranslated) getCause();
            if (ourCause != null)
                ourCause.printStackTraceAsCause(s, trace);
        }
    }
    
    /**
     * Print our stack trace as a cause for the specified stack trace.
     * 
     * Hard copy from Throwable, our implementation of getOurStackTrace() is used.
     */
    private void printStackTraceAsCause(PrintWriter s,
                                        StackTraceElement[] causedTrace)
    {
        // assert Thread.holdsLock(s);

        // Compute number of frames in common between this and caused
        StackTraceElement[] trace = getOurStackTrace();
        int m = trace.length-1, n = causedTrace.length-1;
        while (m >= 0 && n >=0 && trace[m].equals(causedTrace[n])) {
            m--; n--;
        }
        int framesInCommon = trace.length - 1 - m;

        s.println("Caused by: " + this);
        for (int i=0; i <= m; i++)
            s.println("\tat " + trace[i]);
        if (framesInCommon != 0)
            s.println("\t... " + framesInCommon + " more");

        // Recurse if we have a cause
        InvocationExceptionTranslated ourCause = (InvocationExceptionTranslated) getCause();
        if (ourCause != null)
            ourCause.printStackTraceAsCause(s, trace);
    }

    /**
     * Hard copy from Throwable, our implementation of getOurStackTrace() is used.
     */
    @Override
    public StackTraceElement[] getStackTrace() {
        return (StackTraceElement[]) getOurStackTrace().clone();
    }

    
    private synchronized StackTraceElement[] getOurStackTrace() {
        // Initialize stack trace if this is the first call to this method
        if (stackTrace == null) {
            try {
                Method getStackTraceMethod = ClassTypeWrapper.concreteMethodByName((ClassType) ValueWrapper.type(exeption),
                            "getStackTrace", "()[Ljava/lang/StackTraceElement;");  // NOI18N
                if (getStackTraceMethod == null) {
                    return new StackTraceElement[0];
                }
                ArrayReference ar = (ArrayReference) debugger.invokeMethod (
                        preferredThread,
                        exeption,
                        getStackTraceMethod,
                        new Value [0],
                        this
                    );
                int depth = ArrayReferenceWrapper.length(ar);
                stackTrace = new StackTraceElement[depth];
                for (int i=0; i < depth; i++) {
                    stackTrace[i] = getStackTraceElement((ObjectReference) ArrayReferenceWrapper.getValue(ar, i));
                }
            } catch (InvalidExpressionException ex) {
                if (ex.getTargetException() == this) {
                    stackTrace = new StackTraceElement[] {};
                }
                // Leave stackTrace unset to reload next time
                return new StackTraceElement[0];
            } catch (ClassNotPreparedExceptionWrapper ex) {
                return new StackTraceElement[0];
            } catch (InternalExceptionWrapper ex) {
                return new StackTraceElement[0];
            } catch (VMDisconnectedExceptionWrapper ex) {
                return new StackTraceElement[0];
            } catch (ObjectCollectedExceptionWrapper ex) {
                return new StackTraceElement[0];
            }
        }
        return stackTrace;
    }

    private StackTraceElement getStackTraceElement(ObjectReference stElement) {
        String declaringClass;
        String methodName;
        String fileName;
        int    lineNumber;

        try {
            Method getMethod;
            getMethod = ClassTypeWrapper.concreteMethodByName((ClassType) ValueWrapper.type(stElement),
                    "getClassName", "()Ljava/lang/String;");  // NOI18N
            if (getMethod == null) {
                declaringClass = "unknown";
            } else {
                try {
                    StringReference sr = null;
                    while (sr == null) {
                        sr = (StringReference) debugger.invokeMethod (
                            preferredThread,
                            stElement,
                            getMethod,
                            new Value [0],
                            this
                        );
                        try {
                            sr.disableCollection();
                        } catch (ObjectCollectedException ex) {
                            sr = null;
                        }
                    }
                    declaringClass = StringReferenceWrapper.value(sr);
                    sr.enableCollection();
                } catch (InvalidExpressionException ex) {
                    declaringClass = ex.getLocalizedMessage();
                }
                if (declaringClass == null) {
                    declaringClass = "unknown";
                }
            }
            getMethod = ClassTypeWrapper.concreteMethodByName((ClassType) ValueWrapper.type(stElement),
                    "getMethodName", "()Ljava/lang/String;");  // NOI18N
            if (getMethod == null) {
                methodName = "unknown";
            } else {
                try {
                    StringReference sr = null;
                    while (sr == null) {
                        sr = (StringReference) debugger.invokeMethod (
                            preferredThread,
                            stElement,
                            getMethod,
                            new Value [0],
                            this
                        );
                        if (sr == null) break;
                        try {
                            sr.disableCollection();
                        } catch (ObjectCollectedException ex) {
                            sr = null;
                        }
                    }
                    if (sr != null) {
                        methodName = StringReferenceWrapper.value(sr);
                        sr.enableCollection();
                    } else {
                        methodName = null;
                    }
                } catch (InvalidExpressionException ex) {
                    methodName = ex.getLocalizedMessage();
                }
                if (methodName == null) {
                    methodName = "unknown";
                }
            }
            getMethod = ClassTypeWrapper.concreteMethodByName((ClassType) ValueWrapper.type(stElement),
                    "getFileName", "()Ljava/lang/String;");  // NOI18N
            if (getMethod == null) {
                fileName = "unknown";
            } else {
                try {
                    StringReference sr = null;
                    while (sr == null) {
                        sr = (StringReference) debugger.invokeMethod (
                            preferredThread,
                            stElement,
                            getMethod,
                            new Value [0],
                            this
                        );
                        if (sr == null) break;
                        try {
                            sr.disableCollection();
                        } catch (ObjectCollectedException ex) {
                            sr = null;
                        }
                    }
                    if (sr == null) {
                        fileName = null;
                    } else {
                        fileName = StringReferenceWrapper.value(sr);
                        sr.enableCollection();
                    }
                } catch (InvalidExpressionException ex) {
                    fileName = ex.getLocalizedMessage();
                }
            }
            getMethod = ClassTypeWrapper.concreteMethodByName((ClassType) ValueWrapper.type(stElement),
                        "getLineNumber", "()I");  // NOI18N
            if (getMethod == null) {
                lineNumber = -1;
            } else {
                try {
                    IntegerValue iv = (IntegerValue) debugger.invokeMethod (
                            preferredThread,
                            stElement,
                            getMethod,
                            new Value [0],
                            this
                        );
                    lineNumber = IntegerValueWrapper.value(iv);
                } catch (InvalidExpressionException ex) {
                    lineNumber = -1;
                }
            }
            return new StackTraceElement(declaringClass, methodName, fileName, lineNumber);
        } catch (InternalExceptionWrapper ex) {
            String msg = ex.getLocalizedMessage();
            return new StackTraceElement(msg, msg, msg, -1);
        } catch (VMDisconnectedExceptionWrapper ex) {
            String msg = ex.getLocalizedMessage();
            return new StackTraceElement(msg, msg, msg, -1);
        } catch (ObjectCollectedExceptionWrapper ex) {
            Exceptions.printStackTrace(ex);
            String msg = ex.getLocalizedMessage();
            return new StackTraceElement(msg, msg, msg, -1);
        } catch (ClassNotPreparedExceptionWrapper ex) {
            String msg = ex.getLocalizedMessage();
            return new StackTraceElement(msg, msg, msg, -1);
        }
    }

    
    @Override
    public String toString() {
        String s;
        try {
            s = TypeWrapper.name(ValueWrapper.type(exeption));
        } catch (ObjectCollectedExceptionWrapper ex) {
            return "Collected";
        } catch (InternalExceptionWrapper ex) {
            return ex.getLocalizedMessage();
        } catch (VMDisconnectedExceptionWrapper ex) {
            return "Disconnected";
        }
        String message = getOriginalLocalizedMessage();
        return (message != null) ? (s + ": " + message) : s;
    }

}
