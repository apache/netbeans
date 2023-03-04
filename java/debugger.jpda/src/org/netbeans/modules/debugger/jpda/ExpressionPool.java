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

import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.IncompatibleThreadStateException;
import com.sun.jdi.Location;
import com.sun.jdi.Method;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.StackFrame;
import com.sun.jdi.ThreadReference;

import com.sun.jdi.VirtualMachine;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.Session;
import org.netbeans.modules.debugger.jpda.jdi.IllegalThreadStateExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.InternalExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.InvalidStackFrameExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.LocationWrapper;
import org.netbeans.modules.debugger.jpda.jdi.MethodWrapper;
import org.netbeans.modules.debugger.jpda.jdi.MirrorWrapper;
import org.netbeans.modules.debugger.jpda.jdi.ObjectCollectedExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.ReferenceTypeWrapper;
import org.netbeans.modules.debugger.jpda.jdi.StackFrameWrapper;
import org.netbeans.modules.debugger.jpda.jdi.ThreadReferenceWrapper;
import org.netbeans.modules.debugger.jpda.jdi.VMDisconnectedExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.VirtualMachineWrapper;
import org.netbeans.spi.debugger.jpda.EditorContext;
import org.netbeans.spi.debugger.jpda.EditorContext.Operation;

/**
 * The pool of operations, which are used for expression stepping.
 * 
 * @author Martin Entlicher
 */
public class ExpressionPool {
    
    private static Logger logger = Logger.getLogger("org.netbeans.modules.debugger.jpda.step"); // NOI18N
    
    private Map<ExpressionLocation, Expression> expressions = new HashMap<ExpressionLocation, Expression>();
    
    /**
     * Creates a new instance of ExpressionPool
     */
    ExpressionPool() {
    }
    
    public synchronized Expression getExpressionAt(Location loc, String url) {
        try {
            ExpressionLocation exprLocation = new ExpressionLocation(LocationWrapper.method(loc), LocationWrapper.lineNumber(loc));
            if (!expressions.containsKey(exprLocation)) {
                LinkedHashSet<Location> lineLocationsInExpression = new LinkedHashSet<Location>();
                lineLocationsInExpression.add(loc);
                Expression expr = createExpressionAt(loc, url, lineLocationsInExpression);
                expressions.put(exprLocation, expr);
                // Add the rest of in-expression locations:
                Iterator<Location> locIt = lineLocationsInExpression.iterator();
                locIt.next(); // Skip the first one
                while (locIt.hasNext()) {
                    loc = locIt.next();
                    exprLocation = new ExpressionLocation(LocationWrapper.method(loc), LocationWrapper.lineNumber(loc));
                    expressions.put(exprLocation, expr);
                }
            }
            return expressions.get(exprLocation);
        } catch (InternalExceptionWrapper ex) {
            logger.log(Level.INFO, "No expression at '"+url+"', JDI internal error", ex);
            return null;
        } catch (ObjectCollectedExceptionWrapper ex) {
            return null;
        } catch (VMDisconnectedExceptionWrapper ex) {
            return null;
        }
    }
    
    // TODO: Clean unnecessray expressions:
    /*
    public synchronized void removeExpressionAt(Location loc) {
        expressions.remove(new ExpressionLocation(loc.method(), loc.lineNumber()));
    }
     */
    public void cleanUnusedExpressions(ThreadReference thr) {
        synchronized (this) {
            if (expressions.size() == 0) {
                return ;
            }
        }
        List<StackFrame> stackFrames;
        try {
            stackFrames = ThreadReferenceWrapper.frames(thr);
            synchronized (this) {
                for (Iterator<ExpressionLocation> locIt = expressions.keySet().iterator(); locIt.hasNext(); ) {
                    ExpressionLocation exprLoc = locIt.next();
                    // TODO: Check the correct thread.
                    Method method = exprLoc.getMethod();
                    //int line = exprLoc.getLine();
                    for (Iterator<StackFrame> it = stackFrames.iterator(); it.hasNext(); ) {
                        StackFrame sf = it.next();
                        if (method.equals(LocationWrapper.method(StackFrameWrapper.location(sf)))) {
                            //&& line == sf.location().lineNumber()) {
                            method = null;
                            break;
                        }
                    }
                    if (method != null) {
                        locIt.remove();
                    }
                }
            }
        } catch (InternalExceptionWrapper ex) {
        } catch (ObjectCollectedExceptionWrapper ex) {
        } catch (VMDisconnectedExceptionWrapper ex) {
        } catch (IncompatibleThreadStateException ex) {
        } catch (IllegalThreadStateExceptionWrapper ex) {
        } catch (InvalidStackFrameExceptionWrapper ex) {
            // Ignore
        }
    }
    
    /**
     * Clean cached expressions.
     */
    synchronized void clear() {
        expressions.clear();
    }

    private Expression createExpressionAt(final Location loc, final String url,
                                          final Set<Location> lineLocationsInExpression)
                throws InternalExceptionWrapper, VMDisconnectedExceptionWrapper, ObjectCollectedExceptionWrapper {
        VirtualMachine vm = MirrorWrapper.virtualMachine(loc);
        if (!VirtualMachineWrapper.canGetBytecodes(vm)) {
            // Can not analyze expressions without bytecode
            return null;
        }
        ReferenceType clazzType = LocationWrapper.declaringType(loc);
        final Method method = LocationWrapper.method(loc);
        final byte[] bytecodes = MethodWrapper.bytecodes(method);
        byte[] constantPool = null;
        if (VirtualMachineWrapper.canGetConstantPool(vm)) {
            constantPool = ReferenceTypeWrapper.constantPool(clazzType);
        }
        final byte[] theConstantPool = constantPool;
        Session currentSession = DebuggerManager.getDebuggerManager().getCurrentSession();
        final String language = currentSession == null ? null : currentSession.getCurrentLanguage();
        
        int line = LocationWrapper.lineNumber(loc, language);
        
        final List<Location> methodLocations;
        try {
            methodLocations = MethodWrapper.allLineLocations(method, language, null);
        } catch (AbsentInformationException aiex) {
            logger.log(Level.FINE, aiex.getLocalizedMessage());
            return null;
        }
        
        final int[] boundingLines = new int[2];
        final int[][] codeIndexIntervalsPtr = new int[1][];
        Operation[] ops = EditorContextBridge.getContext().getOperations(
                url, line, new EditorContext.BytecodeProvider() {
            public byte[] constantPool() {
                return theConstantPool;
            }

            public byte[] byteCodes() {
                return bytecodes;
            }

            public int[] indexAtLines(int startLine, int endLine) {
                int[] indexes = getIndexesAtLines(methodLocations, language, startLine, endLine,
                                                  bytecodes.length, lineLocationsInExpression);
                boundingLines[0] = startLine;
                boundingLines[1] = endLine;
                codeIndexIntervalsPtr[0] = indexes;
                return indexes;
            }
            
        });
        logger.fine("Operations:");
        if (ops == null) {
            logger.log(Level.FINE, "Unsuccessfull bytecode matching.");
            return null;
        }
        if (ops.length == 0) { // No operations - do a line step instead
            return null;
        }
        if (logger.isLoggable(Level.FINE)) {
            for (Operation op : ops) {
                logger.fine("  "+op.getMethodName()+"():"+op.getMethodStartPosition().getLine()+", bci = "+op.getBytecodeIndex());
            }
        }
        Location[] locations = new Location[ops.length];
        for (int i = 0; i < ops.length; i++) {
            int codeIndex = ops[i].getBytecodeIndex();
            locations[i] = MethodWrapper.locationOfCodeIndex(method, codeIndex);
            if (locations[i] == null) {
                logger.log(Level.FINE, "Location of the operation not found.");
                return null;
            }
        }
        Expression expr = new Expression(new ExpressionLocation(method, line), ops, locations,
                                         new Interval(boundingLines[0], boundingLines[1]),
                                         codeIndexIntervalsPtr[0]);
        return expr;
    }
    
    /**
     * 
     * @param allLocations all locations in the method
     * @param language
     * @param startLine expression start line
     * @param endLine expression end line
     * @param methodEndIndex the last code index in the method + 1
     * @return pairs of code indexes on individual lines.
     *         Every pair is an interval of code indexes on the respective lines.
     */
    private static int[] getIndexesAtLines(List<Location> allLocations, String language,
                                           int startLine, int endLine, int methodEndIndex,
                                           Set<Location> lineLocationsInExpression) {
        
        
        int startlocline = 0;
        int endlocline;
        try {
            Location startLocation;
            int firstLine = LocationWrapper.lineNumber(allLocations.get(0), language);
            do {
                startLocation = getLocationOfLine(allLocations, language, startLine - startlocline++);
            } while (startLocation == null && (startLine - (startlocline - 1)) >= firstLine);
        } catch (VMDisconnectedExceptionWrapper e) {
            return null;
        } catch (InternalExceptionWrapper e) {
            return null;
        }
        startLine -= (startlocline - 1);
        if (endLine > startLine) {
            endlocline = 0;
        } else {
            endlocline = 1;
        }
        endLine += endlocline;
        List<int[]> indexes = new ArrayList<int[]>();
        int startIndex = -1;
        Location locInExpression = null;
        try {
            for (Location l : allLocations) {
                int line = LocationWrapper.lineNumber(l, language);
                if (startIndex == -1 && startLine <= line && line < endLine) {
                    startIndex = (int) LocationWrapper.codeIndex(l);
                    locInExpression = l;
                } else if (startIndex >= 0) {
                    int ci = (int) LocationWrapper.codeIndex(l);
                    lineLocationsInExpression.add(locInExpression);
                    if (startLine <= line && line <= (endLine - endlocline)) {
                        indexes.add(new int[] { startIndex, ci });
                        startIndex = ci;
                        locInExpression = l;
                    } else {
                        indexes.add(new int[] { startIndex, ci });
                        startIndex = -1;
                        locInExpression = null;
                    }
                }
            }
        } catch (VMDisconnectedExceptionWrapper e) {
            return null;
        } catch (InternalExceptionWrapper e) {
            return null;
        }
        if (indexes.size() == 0) {
            if (startIndex >= 0) {
                // End of the method
                if (logger.isLoggable(Level.FINE)) {
                    logger.fine("getIndexesAtLines("+startLine+", "+endLine+") = "+
                                Arrays.toString(new int[] { startIndex, methodEndIndex }));
                }
                lineLocationsInExpression.add(locInExpression);
                return new int[] { startIndex, methodEndIndex };
            }
            return null;
        } else if (indexes.size() == 1) {
            if (logger.isLoggable(Level.FINE)) {
                logger.fine("getIndexesAtLines("+startLine+", "+endLine+") = "+
                            Arrays.toString(indexes.get(0)));
            }
            return indexes.get(0);
        } else {
            int[] arr = new int[2*indexes.size()];
            for (int i = 0; i < indexes.size(); i++) {
                arr[2*i] = indexes.get(i)[0];
                arr[2*i + 1] = indexes.get(i)[1];
            }
            if (logger.isLoggable(Level.FINE)) {
                logger.fine("getIndexesAtLines("+startLine+", "+endLine+") = "+
                            Arrays.toString(arr));
            }
            return arr;
        }
    }
    
    private static Location getLocationOfLine(List<Location> allLocations, String language, int line) throws InternalExceptionWrapper, VMDisconnectedExceptionWrapper {
        for (Location l : allLocations) {
            if (LocationWrapper.lineNumber(l, language) == line) {
                return l;
            }
        }
        return null;
    }
    
    //private int[] singleIndexHolder = new int[1]; // Perf. optimization only
    
    public static final class Expression {
        
        private ExpressionLocation location;
        private Operation[] operations;
        private Location[] locations;
        private Interval lines;
        private int[] codeIndexIntervals;
        
        Expression(ExpressionLocation location, Operation[] operations, Location[] locations,
                   Interval lines, int[] codeIndexIntervals) {
            this.location = location;
            this.operations = operations;
            this.locations = locations;
            this.lines = lines;
            this.codeIndexIntervals = codeIndexIntervals;
        }
        
        public Operation[] getOperations() {
            return operations;
        }
        
        public Location[] getLocations() {
            return locations;
        }
        
        public Interval getInterval() {
            return lines;
        }
        
        int[] getCodeIndexIntervals() {
            return codeIndexIntervals;
        }
        
        public int findNextOperationIndex(int codeIndex) {
            for (int i = 0; i < operations.length; i++) {
                int operationIndex = operations[i].getBytecodeIndex();
                if (operationIndex > codeIndex) {
                    return i;
                }
            }
            return -1;
        }
        
        int[] findNextOperationIndexes(int codeIndex) {
            for (int i = 0; i < operations.length; i++) {
                int operationIndex = operations[i].getBytecodeIndex();
                if (operationIndex == codeIndex) {
                    List<Operation> nextOperations = operations[i].getNextOperations();
                    if (!nextOperations.isEmpty()) {
                        int l = nextOperations.size();
                        int[] indexes = new int[l];
                        for (int ni = 0; ni < l; ni++) {
                            Operation op = nextOperations.get(ni);
                            int j;
                            for (j = 0; j < operations.length; j++) {
                                if (op == operations[j]) break;
                            }
                            if (j < operations.length) {
                                indexes[ni] = j;
                            } else {
                                indexes[ni] = -1;
                            }
                        }
                        return indexes;
                    }
                }
                if (operationIndex > codeIndex) {
                    return new int[] { i };
                }
            }
            return null;
        }
        
        OperationLocation[] findNextOperationLocations(int codeIndex) {
            for (int i = 0; i < operations.length; i++) {
                int operationIndex = operations[i].getBytecodeIndex();
                if (operationIndex == codeIndex) {
                    List<Operation> nextOperations = operations[i].getNextOperations();
                    if (!nextOperations.isEmpty()) {
                        int l = nextOperations.size();
                        OperationLocation[] opLocations = new OperationLocation[l];
                        for (int ni = 0; ni < l; ni++) {
                            Operation op = nextOperations.get(ni);
                            int j;
                            for (j = 0; j < operations.length; j++) {
                                if (op == operations[j]) break;
                            }
                            if (j < operations.length) {
                                opLocations[ni] = //locations[j];
                                        new OperationLocation(operations[j], locations[j], j);
                            } else {
                                int ci = op.getBytecodeIndex();
                                Location loc;
                                try {
                                    loc = MethodWrapper.locationOfCodeIndex(location.getMethod(), ci);
                                } catch (InternalExceptionWrapper ex) {
                                    return null;
                                } catch (VMDisconnectedExceptionWrapper ex) {
                                    return null;
                                }
                                if (loc == null) {
                                    logger.log(Level.FINE, "Location of the operation not found.");
                                    return null;
                                }
                                opLocations[ni] = //loc;
                                        new OperationLocation(op, loc, -1);
                            }
                        }
                        return opLocations;
                    }
                }
                if (operationIndex > codeIndex) {
                    return new OperationLocation[] { new OperationLocation(
                                operations[i],
                                locations[i],
                                i
                            ) };
                }
            }
            return null;
        }
        
    }

    public static final class ExpressionLocation {

        private Method method;
        private int line;

        public ExpressionLocation(Method method, int line) {
            this.method = method;
            this.line = line;
        }
        
        public Method getMethod() {
            return method;
        }
        
        public int getLine() {
            return line;
        }

        public boolean equals(Object obj) {
            if (!(obj instanceof ExpressionLocation)) {
                return false;
            }
            return ((ExpressionLocation) obj).line == line && ((ExpressionLocation) obj).method.equals(method);
        }

        public int hashCode() {
            return method.hashCode() + line;
        }

    }
    
    public static final class OperationLocation {
        
        private Operation op;
        private Location loc;
        private int index;
        
        OperationLocation(Operation op, Location loc, int index) {
            this.op = op;
            this.loc = loc;
            this.index = index;
        }

        public Operation getOperation() {
            return op;
        }

        public Location getLocation() {
            return loc;
        }
        
        public int getIndex() {
            return index;
        }

    }
    
    public static final class Interval {
        
        private int i1;
        private int i2;
        
        Interval(int i1, int i2) {
            this.i1 = i1;
            this.i2 = i2;
        }
        
        public int begin() {
            return i1;
        }
        
        public int end() {
            return i2;
        }
        
        public boolean contains(int i) {
            return i1 <= i && i <= i2;
        }
    }

}
