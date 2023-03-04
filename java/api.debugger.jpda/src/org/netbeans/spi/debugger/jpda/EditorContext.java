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

import java.beans.PropertyChangeListener;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.netbeans.api.debugger.jpda.JPDAThread;
import org.netbeans.api.debugger.jpda.Variable;
import org.netbeans.modules.debugger.jpda.apiregistry.DebuggerProcessor;
import org.netbeans.spi.debugger.ContextAwareService;
import org.netbeans.spi.debugger.ContextAwareSupport;
import org.netbeans.spi.debugger.ContextProvider;

/**
 * Defines bridge to editor and src hierarchy. It allows to use different
 * source viewer for debugger (like some UML view).
 *
 * @author Jan Jancura
 */
public abstract class EditorContext {

    /** Annotation type constant. */
    public static final String BREAKPOINT_ANNOTATION_TYPE = "Breakpoint";
    /** Annotation type constant. */
    public static final String DISABLED_BREAKPOINT_ANNOTATION_TYPE = "DisabledBreakpoint";
    /** Annotation type constant. */
    public static final String CONDITIONAL_BREAKPOINT_ANNOTATION_TYPE = "CondBreakpoint";
    /** Annotation type constant. */
    public static final String DISABLED_CONDITIONAL_BREAKPOINT_ANNOTATION_TYPE = "DisabledCondBreakpoint";
    /** Annotation type constant. */
    public static final String FIELD_BREAKPOINT_ANNOTATION_TYPE = "FieldBreakpoint";
    /** Annotation type constant. */
    public static final String CLASS_BREAKPOINT_ANNOTATION_TYPE = "ClassBreakpoint";
    /** Annotation type constant. */
    public static final String DISABLED_FIELD_BREAKPOINT_ANNOTATION_TYPE = "DisabledFieldBreakpoint";
    /** Annotation type constant. */
    public static final String METHOD_BREAKPOINT_ANNOTATION_TYPE = "MethodBreakpoint";
    /** Annotation type constant. */
    public static final String DISABLED_METHOD_BREAKPOINT_ANNOTATION_TYPE = "DisabledMethodBreakpoint";
    /** Annotation type constant. */
    public static final String DISABLED_CLASS_BREAKPOINT_ANNOTATION_TYPE = "DisabledClassBreakpoint";
    /** Annotation type constant. */
    public static final String CURRENT_LINE_ANNOTATION_TYPE = "CurrentPC";
    /** Annotation type constant. */
    public static final String CALL_STACK_FRAME_ANNOTATION_TYPE = "CallSite";
    /** Annotation type constant. */
    public static final String CURRENT_LAST_OPERATION_ANNOTATION_TYPE = "LastOperation";
    /** Annotation type constant. */
    public static final String CURRENT_OUT_OPERATION_ANNOTATION_TYPE = "StepOutOperation";
    /** Annotation type constant. */
    public static final String CURRENT_EXPRESSION_SECONDARY_LINE_ANNOTATION_TYPE = "CurrentExpression";
    /** Annotation type constant. */
    public static final String CURRENT_EXPRESSION_CURRENT_LINE_ANNOTATION_TYPE = "CurrentExpressionLine";
    /** Annotation type constant.
     * @since 2.16     */
    public static final String OTHER_THREAD_ANNOTATION_TYPE = "OtherThread";

    /** Property name constant. */
    public static final String PROP_LINE_NUMBER = "lineNumber";


    /**
     * Shows source with given url on given line number.
     *
     * @param url a url of source to be shown
     * @param lineNumber a number of line to be shown
     * @param timeStamp a time stamp to be used
     */
    public abstract boolean showSource (
        String url,
        int lineNumber, 
        Object timeStamp
    );

    /**
     * Creates a new time stamp.
     *
     * @param timeStamp a new time stamp
     */
    public abstract void createTimeStamp (Object timeStamp);

    /**
     * Disposes given time stamp.
     *
     * @param timeStamp a time stamp to be disposed
     */
    public abstract void disposeTimeStamp (Object timeStamp);
    
    /**
     * Updates timeStamp for gived url.
     *
     * @param timeStamp time stamp to be updated
     * @param url an url
     */
    public abstract void updateTimeStamp (Object timeStamp, String url);

    /**
     * Adds annotation to given url on given line.
     *
     * @param url a url of source annotation should be set into
     * @param lineNumber a number of line annotation should be set into
     * @param annotationType a type of annotation to be set
     * @param timeStamp a time stamp to be used
     *
     * @return annotation or <code>null</code>, when the annotation can not be
     *         created at the given URL or line number.
     */
    public abstract Object annotate (
        String url, 
        int lineNumber, 
        String annotationType,
        Object timeStamp
    );

    /**
     * Adds annotation to given url on given line.
     *
     * @param url a url of source annotation should be set into
     * @param lineNumber a number of line annotation should be set into
     * @param annotationType a type of annotation to be set
     * @param timeStamp a time stamp to be used
     * @param thread the thread to annotate
     *
     * @return annotation or <code>null</code>, when the annotation can not be
     *         created at the given URL or line number.
     * @since 2.16
     */
    public Object annotate (
        String url, 
        int lineNumber, 
        String annotationType,
        Object timeStamp,
        JPDAThread thread
    ) {
        return null;
    }

    /**
     * Adds annotation to given url on given character range.
     *
     * @param url a url of source annotation should be set into
     * @param startPosition the offset of the starting position of the annotation
     * @param endPosition the offset of the ending position of the annotation
     * @param annotationType a type of annotation to be set

     * @return annotation or <code>null</code>, when the annotation can not be
     *         created at the given URL or line number.
     */
    public Object annotate (
        String url, 
        int startPosition, 
        int endPosition, 
        String annotationType,
        Object timeStamp
    ) {
        return null;
    }

    /**
     * Returns line number given annotation is associated with.
     *
     * @param annotation a annotation
     * @param timeStamp a time stamp to be used
     *
     * @return line number given annotation is associated with
     */
    public abstract int getLineNumber (
        Object annotation,
        Object timeStamp
    );

    /**
     * Removes given annotation.
     */
    public abstract void  removeAnnotation (
        Object annotation
    );

    /**
     * Returns number of line currently selected in editor or <code>-1</code>.
     *
     * @return number of line currently selected in editor or <code>-1</code>
     */
    public abstract int getCurrentLineNumber ();

    /**
     * Returns name of class currently selected in editor or empty string.
     *
     * @return name of class currently selected in editor or empty string
     */
    public abstract String getCurrentClassName ();

    /**
     * Returns URL of source currently selected in editor or empty string.
     *
     * @return URL of source currently selected in editor or empty string
     */
    public abstract String getCurrentURL ();

    /**
     * Returns name of method currently selected in editor or empty string.
     *
     * @return name of method currently selected in editor or empty string
     */
    public abstract String getCurrentMethodName ();

    /**
     * Returns name of field currently selected in editor or <code>null</code>.
     *
     * @return name of field currently selected in editor or <code>null</code>
     */
    public abstract String getCurrentFieldName ();

    /**
     * Returns identifier currently selected in editor or <code>null</code>.
     *
     * @return identifier currently selected in editor or <code>null</code>
     */
    public abstract String getSelectedIdentifier ();

    /**
     * Returns method name currently selected in editor or empty string.
     *
     * @return method name currently selected in editor or empty string
     */
    public abstract String getSelectedMethodName ();
    
    /**
     * Returns line number of given field in given class.
     *
     * @param url the url of source file the class is deined in
     * @param className the name of class (or innerclass) the field is 
     *                  defined in
     * @param fieldName the name of field
     *
     * @return line number or -1
     */
    public abstract int getFieldLineNumber (
        String url, 
        String className, 
        String fieldName
    );
    
    /**
     * Returns line number of given method in given class.
     *
     * @param url the url of source file the class is deined in
     * @param className the name of class (or innerclass) the method is 
     *                  defined in
     * @param methodName the name of the method
     * @param methodSignature the JNI-style signature of the method.
     *        If <code>null</code>, then the first method found is returned.
     *
     * @return line number or -1
     */
    public int getMethodLineNumber (
        String url, 
        final String className, 
        final String methodName,
        final String methodSignature
    ) {
        return -1;
    }
    
    
    /**
     * Returns name and signature of method declaration currently selected in editor,
     * or <code>null</code>.
     *
     * @return name and signature of the method, or <code>null</code>.
     */
    public String[] getCurrentMethodDeclaration() {
        return null;
    }

    
    /**
     * Returns class name for given url and line number or null.
     *
     * @param url a url
     * @param lineNumber a line number
     *
     * @return class name for given url and line number or null
     */
    public abstract String getClassName (
        String url, 
        int lineNumber
    );
    
    /**
     * Returns list of imports for given source url.
     *
     * @param url the url of source file
     *
     * @return list of imports for given source url
     */
    public abstract String[] getImports (String url);
    
    /**
     * Creates an operation which is determined by starting and ending position.
     *
    protected final Operation createOperation(Position startPosition,
                                              Position endPosition,
                                              int bytecodeIndex) {
        return new Operation(startPosition, endPosition, bytecodeIndex);
    }
     */
    
    /**
     * Creates a method operation.
     * @param startPosition The starting position of the operation
     * @param endPosition The ending position of the operation
     * @param methodStartPosition The starting position of the method name
     * @param methodEndPosition The ending position of the method name
     * @param methodName The string representation of the method name
     * @param methodClassType The class type, which defines this method
     * @param bytecodeIndex The bytecode index of this method call
     */
    protected final Operation createMethodOperation(Position startPosition,
                                                    Position endPosition,
                                                    Position methodStartPosition,
                                                    Position methodEndPosition,
                                                    String methodName,
                                                    String methodClassType,
                                                    int bytecodeIndex) {
        return new Operation(startPosition, endPosition,
                             methodStartPosition, methodEndPosition,
                             methodName, methodClassType, bytecodeIndex, false);
    }
    
    /**
     * Creates a method operation.
     * @param startPosition The starting position of the operation
     * @param endPosition The ending position of the operation
     * @param methodStartPosition The starting position of the method name
     * @param methodEndPosition The ending position of the method name
     * @param methodName The string representation of the method name
     * @param methodClassType The class type, which defines this method
     * @param bytecodeIndex The bytecode index of this method call
     * @param isNative <code>true</code> when the method is determined as a native
     *                 method by the parser.
     * @since 2.51
     */
    protected final Operation createMethodOperation(Position startPosition,
                                                    Position endPosition,
                                                    Position methodStartPosition,
                                                    Position methodEndPosition,
                                                    String methodName,
                                                    String methodClassType,
                                                    int bytecodeIndex,
                                                    boolean isNative) {
        return new Operation(startPosition, endPosition,
                             methodStartPosition, methodEndPosition,
                             methodName, methodClassType, bytecodeIndex,
                             isNative);
    }
    
    /**
     * Assign a next operation, concatenates operations.
     * @param operation The first operation
     * @param next The next operation
     */
    protected final void addNextOperationTo(Operation operation, Operation next) {
        operation.addNextOperation(next);
    }
    
    /**
     * Creates a new {@link Position} object.
     * @param offset The offset
     * @param line The line number
     * @param column The column number
     */
    protected final Position createPosition(
            int offset, int line, int column) {
        
        return new Position(offset, line, column);
    }
    
    /**
     * Get the list of operations that are in expression(s) located at the given line.
     * @param url The file's URL
     * @param lineNumber The line number
     * @param bytecodeProvider The provider of method bytecodes.
     */
    public Operation[] getOperations(String url, int lineNumber,
                                     BytecodeProvider bytecodeProvider) {
        throw new UnsupportedOperationException("This method is not implemented.");
    }
    
    /**
     * Get a list of arguments to the given operation.
     * @param url The URL of the source file with the operation
     * @param operation The operation
     */
    public MethodArgument[] getArguments(String url, Operation operation) {
        throw new UnsupportedOperationException("This method is not implemented by "+this);
    }
    
    /**
     * Get a list of arguments passed to method located at the given line.
     * @param url The URL of the source file
     * @param methodLineNumber The line number of the method header
     */
    public MethodArgument[] getArguments(String url, int methodLineNumber) {
        throw new UnsupportedOperationException("This method is not implemented by "+this);
    }
    
    /**
     * Adds a property change listener.
     *
     * @param l the listener to add
     */
    public abstract void addPropertyChangeListener (PropertyChangeListener l);
    
    /**
     * Removes a property change listener.
     *
     * @param l the listener to remove
     */
    public abstract void removePropertyChangeListener (PropertyChangeListener l);
    
    /**
     * Adds a property change listener.
     *
     * @param propertyName the name of property
     * @param l the listener to add
     */
    public abstract void addPropertyChangeListener (
        String propertyName,
        PropertyChangeListener l
    );
    
    /**
     * Removes a property change listener.
     *
     * @param propertyName the name of property
     * @param l the listener to remove
     */
    public abstract void removePropertyChangeListener (
        String propertyName,
        PropertyChangeListener l
    );
    
    /**
     * A provider of method bytecode information.
     */
    public interface BytecodeProvider {
        
        /**
         * Retrieve the class' constant pool.
         */
        byte[] constantPool();
        
        /**
         * Retrieve the bytecodes of the method.
         */
        byte[] byteCodes();
        
        /**
         * Get an array of bytecode indexes of operations between the starting
         * and ending line.
         * @param startLine The starting line
         * @param endLine The ending line
         */
        int[] indexAtLines(int startLine, int endLine);
        
    }
    
    /**
     * The operation definition.
     */
    public static final class Operation {
        
        private final Position startPosition;
        private final Position endPosition;
        private final int bytecodeIndex;

        private Position methodStartPosition;
        private Position methodEndPosition;
        private String methodName;
        private String methodDescriptor; // TODO: Add API get/set, accessed through reflection in the meantime.
        private String methodClassType;
        private Variable returnValue;
        private boolean isNative;
        
        private List<Operation> nextOperations;
        
        /*
        Operation(Position startPosition, Position endPosition,
                  int bytecodeIndex) {
            this.startPosition = startPosition;
            this.endPosition = endPosition;
            this.bytecodeIndex = bytecodeIndex;
        }
         */
        
        /**
         * Creates a new method operation.
         */
        Operation(Position startPosition, Position endPosition,
                  Position methodStartPosition, Position methodEndPosition,
                  String methodName, String methodClassType,
                  int bytecodeIndex, boolean isNative) {
            this.startPosition = startPosition;
            this.endPosition = endPosition;
            this.bytecodeIndex = bytecodeIndex;
            this.methodStartPosition = methodStartPosition;
            this.methodEndPosition = methodEndPosition;
            this.methodName = methodName;
            this.methodClassType = methodClassType;
            this.isNative = isNative;
        }
        
        synchronized void addNextOperation(Operation next) {
            if (nextOperations == null) {
                nextOperations = new ArrayList<Operation>();
            }
            nextOperations.add(next);
        }
        
        /**
         * Get the starting position of this operation.
         */
        public Position getStartPosition() {
            return startPosition;
        }
        
        /**
         * Get the ending position of this operation.
         */
        public Position getEndPosition() {
            return endPosition;
        }
        
        /**
         * Get the starting position of the method call of this operation.
         */
        public Position getMethodStartPosition() {
            return methodStartPosition;
        }

        /**
         * Get the ending position of the method call of this operation.
         */
        public Position getMethodEndPosition() {
            return methodEndPosition;
        }

        /**
         * Get the method name.
         */
        public String getMethodName() {
            return methodName;
        }
        
        /**
         * Get the class type declaring the method.
         */
        public String getMethodClassType() {
            return methodClassType;
        }
        
        /**
         * Indicates whether the method was determined as native by the parser.
         * It can return <code>false</code> for native methods that are resolved
         * during runtime.
         * @return <code>true</code> when the method is determined as native by
         * the parser.
         * @since 2.51
         */
        public boolean isNative() {
            return isNative;
        }
        
        /**
         * Get the bytecode index of this operation.
         */
        public int getBytecodeIndex() {
            return bytecodeIndex;
        }

        /**
         * Set the return value of this operation.
         */
        public void setReturnValue(Variable returnValue) {
            this.returnValue = returnValue;
        }
        
        /**
         * Get the return value of this operation.
         */
        public Variable getReturnValue() {
            return returnValue;
        }
        
        /**
         * Get the list of following operations.
         */
        public List<Operation> getNextOperations() {
            if (nextOperations == null) {
                return Collections.emptyList();
            } else {
                synchronized (this) {
                    return Collections.unmodifiableList(nextOperations);
                }
            }
        }

        public boolean equals(Object obj) {
            if (obj instanceof Operation) {
                Operation op2 = (Operation) obj;
                return bytecodeIndex == op2.bytecodeIndex &&
                        ((startPosition == null) ?
                            op2.startPosition == null :
                            startPosition.equals(op2.startPosition)) &&
                        ((endPosition == null) ?
                            op2.endPosition == null :
                            endPosition.equals(op2.endPosition));
            }
            return false;
        }

        public int hashCode() {
            return bytecodeIndex;
        }

    }
    
    /**
     * Representation of a position in a source code.
     */
    public static final class Position {

        private final int offset;
        private final int line;
        private final int column;

        Position(int offset, int line, int column) {
            this.offset = offset;
            this.line = line;
            this.column = column;
        }

        /**
         * Get the offset of this position.
         */
        public int getOffset() {
            return offset;
        }

        /**
         * Get the line number of this position.
         */
        public int getLine() {
            return line;
        }

        /**
         * Get the column number of this position.
         */
        public int getColumn() {
            return column;
        }

        public boolean equals(Object obj) {
            if (obj instanceof Position) {
                Position pos = (Position) obj;
                return pos.offset == offset;
            }
            return false;
        }

        public int hashCode() {
            return offset;
        }
        
    }
    
    /**
     * Representation of an argument to a method.
     * @since 2.10
     */
    public static final class MethodArgument {
        
        private String name;
        private String type;
        private Position startPos;
        private Position endPos;
        
        /**
         * Creates a new argument.
         * @param name The argument name
         * @param type The declared type of the argument
         * @param startPos Starting position of the argument in the source code
         * @param endPos Ending position of the argument in the source code
         */
        public MethodArgument(String name, String type, Position startPos, Position endPos) {
            assert name != null;
            assert type != null;
            this.name = name;
            this.type = type;
            this.startPos = startPos;
            this.endPos = endPos;
        }
        
        /**
         * Get the name of this argument.
         * @return The name.
         */
        public String getName() {
            return name;
        }
        
        /**
         * Get the declared type of this argument.
         * @return The declared type.
         */
        public String getType() {
            return type;
        }
        
        /**
         * Get the starting position of this argument in the source code.
         * @return The starting position.
         */
        public Position getStartPosition() {
            return startPos;
        }
        
        /**
         * Get the ending position of this argument in the source code.
         * @return The ending position.
         */
        public Position getEndPosition() {
            return endPos;
        }
        
    }

    
    /**
     * Declarative registration of a EditorContext implementation.
     * By marking the implementation class with this annotation,
     * you automatically register that implementation for use by debugger.
     * The class must be public and have a public constructor which takes
     * no arguments or takes {@link ContextProvider} as an argument.
     *
     * @author Martin Entlicher
     * @since 2.19
     */
    @Retention(RetentionPolicy.SOURCE)
    @Target({ElementType.TYPE})
    public @interface Registration {
        /**
         * An optional path to register this implementation in.
         * Usually the session ID.
         */
        String path() default "";

    }

    static class ContextAware extends EditorContext implements ContextAwareService<EditorContext> {

        private String serviceName;

        private ContextAware(String serviceName) {
            this.serviceName = serviceName;
        }

        public EditorContext forContext(ContextProvider context) {
            return (EditorContext) ContextAwareSupport.createInstance(serviceName, context);
        }

        @Override
        public boolean showSource(String url, int lineNumber, Object timeStamp) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void createTimeStamp(Object timeStamp) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void disposeTimeStamp(Object timeStamp) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void updateTimeStamp(Object timeStamp, String url) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Object annotate(String url, int lineNumber, String annotationType, Object timeStamp) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public int getLineNumber(Object annotation, Object timeStamp) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void removeAnnotation(Object annotation) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public int getCurrentLineNumber() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public String getCurrentClassName() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public String getCurrentURL() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public String getCurrentMethodName() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public String getCurrentFieldName() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public String getSelectedIdentifier() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public String getSelectedMethodName() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public int getFieldLineNumber(String url, String className, String fieldName) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public String getClassName(String url, int lineNumber) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public String[] getImports(String url) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void addPropertyChangeListener(PropertyChangeListener l) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void removePropertyChangeListener(PropertyChangeListener l) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void addPropertyChangeListener(String propertyName, PropertyChangeListener l) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void removePropertyChangeListener(String propertyName, PropertyChangeListener l) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        /**
         * Creates instance of <code>ContextAwareService</code> based on layer.xml
         * attribute values
         *
         * @param attrs attributes loaded from layer.xml
         * @return new <code>ContextAwareService</code> instance
         */
        static ContextAwareService createService(Map attrs) throws ClassNotFoundException {
            String serviceName = (String) attrs.get(DebuggerProcessor.SERVICE_NAME);
            return new ContextAware(serviceName);
        }

    }

}

