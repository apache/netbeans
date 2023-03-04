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
package org.netbeans.modules.groovy.editor.compiler;

import groovy.lang.GroovyClassLoader;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.security.CodeSource;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.GroovyClassVisitor;
import org.codehaus.groovy.ast.decompiled.AsmReferenceResolver;
import org.codehaus.groovy.classgen.GeneratorContext;
import org.codehaus.groovy.classgen.VariableScopeVisitor;
import org.codehaus.groovy.control.CompilationFailedException;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.Phases;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.tools.GroovyClass;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.modules.groovy.editor.api.parser.GroovyParser;
import org.netbeans.modules.groovy.editor.compiler.ClassNodeCache.ParsingClassLoader;
import org.netbeans.modules.parsing.api.Snapshot;
import org.openide.util.Exceptions;

/**
 * A helper class that adds performance statistics for Groovy compilation.
 * This class is in between {@link CompilationUnit} and GroovyParser and after
 * the perf optimizations are finished, it could be removed from the hierarchy.
 * <p/>
 * All perf counter logic, that would otherwise polute basic Groovy Parser should
 * be centralized here.
 *
 * @author sdedic
 */
public class PerfStatCompilationUnit extends CompilationUnit {
    private static final Logger PERFLOG = PerfData.LOG;

    final long parsingStartTime;
    final PerfData perfData;

    int lastPhaseSeen = 0;
    SortedMap<Integer, Long> phaseStartTime = new TreeMap<>();

    public PerfStatCompilationUnit(PerfData perfData, GroovyParser parser, CompilerConfiguration configuration,
            CodeSource security,
            @NonNull final GroovyClassLoader loader,
            @NonNull final GroovyClassLoader transformationLoader,
            @NonNull final ClasspathInfo cpInfo,
            @NonNull final ClassNodeCache classNodeCache, boolean isIndexing, Snapshot snapshot) {
        super(parser, configuration, security, loader, transformationLoader, cpInfo, classNodeCache, isIndexing, snapshot);
        this.parsingStartTime = System.currentTimeMillis();
        this.perfData = perfData;
        if (classLoader instanceof ParsingClassLoader) {
            ((ParsingClassLoader)classLoader).setPerfData(perfData);
        }
        overrideClassNodeResolver();
    }
    
    static final Method doPhaseOperation;
    static final Field resolverVisitor;
    
    static {
        Method m = null;
        Field f = null;
        try {
            // PhaseOperation is private
            Class c = Class.forName("org.codehaus.groovy.control.CompilationUnit$PhaseOperation"); // NOI18N
            m = c.getDeclaredMethod("doPhaseOperation", org.codehaus.groovy.control.CompilationUnit.class); // NOI18N
            m.setAccessible(true);
            
            c = org.codehaus.groovy.control.CompilationUnit.class;
            f = c.getDeclaredField("resolve"); // NOI18N
            f.setAccessible(true);
            Field modifiersField = Field.class.getDeclaredField("modifiers");
            modifiersField.setAccessible(true);
            modifiersField.setInt(f, f.getModifiers() & ~Modifier.FINAL);
        } catch (ReflectiveOperationException | SecurityException ex) {
            if (PERFLOG.isLoggable(Level.FINER)) {
                PERFLOG.log(Level.WARNING, "Unable to patch Groovy compiler for timimg. Use JDK 11.", ex);
            }
        }
        doPhaseOperation = m;
        resolverVisitor = f;
    }

    private void overrideClassNodeResolver() {
        // revolve visitor override does not work on JDK17
        if (!PERFLOG.isLoggable(Level.FINER) || resolverVisitor == null) {
            return;
        }
        classNodeResolver = new NbClassNodeResolver() {
            @Override
            protected AsmReferenceResolver createReferencesResolver(org.codehaus.groovy.control.CompilationUnit unit) {
                return new AsmReferenceResolver(this, PerfStatCompilationUnit.this) {
                    @Override
                    public ClassNode resolveClassNullable(String className) {
                        long t = System.currentTimeMillis();
                        ClassNode c;
                        try {
                            c = super.resolveClassNullable(className);
                            return c;
                        } finally {
                            long t2 = System.currentTimeMillis();
                            perfData.addVisitorTime(phase, "AsmReferenceResolver", t2 - t); // NOI18N
                        }
                    }
                };
            }
        };
        try {
            resolverVisitor.set(this, createResolve());
        } catch (ReflectiveOperationException ex) {
            // will not be collected
            ex.printStackTrace();
        }
    }

    /**
     * Inject static compilation transofmrations; apply them in the
     *
     * @param phase
     * @throws CompilationFailedException
     */
    @Override
    public void gotoPhase(int phase) throws CompilationFailedException {
        super.gotoPhase(phase);
        recordPhaseStart(phase);
    }

    protected void runSourceVisitor(String visitorName, Consumer<SourceUnit> callback) {
        long t = System.currentTimeMillis();
        try {
            super.runSourceVisitor(visitorName, callback);
        } finally {
            long t2 = System.currentTimeMillis();
            perfData.addVisitorTime(phase, visitorName, t2 - t);
        }
    }
    
    protected org.codehaus.groovy.control.CompilationUnit.ISourceUnitOperation createResolve() {
        return (final SourceUnit source) -> {
            int phase = getPhase();
            recordPhaseStart(phase);
            for (ClassNode classNode : source.getAST().getClasses()) {
                long t = System.currentTimeMillis();
                GroovyClassVisitor visitor = new VariableScopeVisitor(source);
                visitor.visitClass(classNode);
                long t2 = System.currentTimeMillis();
                long d = t2 - t;
                perfData.addVisitorTime(phase, visitor.getClass().getName(), d);

                resolveVisitor.setClassNodeResolver(classNodeResolver);
                resolveVisitor.startResolving(classNode, source);
                long t3 = System.currentTimeMillis();
                d = t3 -t2;
                perfData.addVisitorTime(phase, resolveVisitor.getClass().getName(), d);
            }
        };
    }

    protected void recordPhaseStart(int phase) {
        if (phase > lastPhaseSeen) {
            // start of phase
            long l = System.currentTimeMillis();
            phaseStartTime.put(phase, l);
            for (int i = phase - 1; i > lastPhaseSeen; i++) {
                phaseStartTime.putIfAbsent(i, l);
            }
            lastPhaseSeen = phase;
        }
    }
    
    void logAndCollectPhaseStats() {
        long end = System.currentTimeMillis();
        int lastPhase = 0;

        for (int i = Phases.INITIALIZATION; i < Phases.ALL; i++) {
            Long phaseStart = phaseStartTime.get(i);
            Long phaseEnd = phaseStartTime.get(i + 1);
            if (phaseStart == null) {
                if (lastPhase > 0) {
                    continue;
                }
                phaseStart = parsingStartTime;
            }
            if (phaseStart != null) {
                lastPhase = i;
            }
            if (phaseEnd == null || phaseStart == null) {
                continue;
            }
            long diff = phaseEnd - phaseStart;
            perfData.addParserPhase(i, diff);
        }
        if (lastPhase > 0) {
            Long lastStart = phaseStartTime.get(lastPhase);
            long diff = end - lastStart;
            perfData.addParserPhase(lastPhase, diff);
        }
    }
    
    static Map<String, Object> astTransformLambdaMap = new HashMap<>();

        
    class TimingOp implements ISourceUnitOperation, IPrimaryClassNodeOperation, IGroovyClassOperation {

        private final Object delegate;
        private final String key;
        private final int phase;

        public TimingOp(Object delegate, int phase) {
            this.delegate = delegate;
            this.phase = phase;

            String cn = delegate.getClass().getName();
            if (cn.contains("$$Lambda")) { // NOI18N
                // Lambda class names are not quite descriptive. For lambdas, traverse the stacktrace back to the
                // calling code and present as stackframe (usually class.method (class:line)
                StackTraceElement invokedFrom = null;
                for (StackTraceElement ele : new Throwable().getStackTrace()) {
                    boolean myClass = ele.getClassName().startsWith("org.netbeans.modules.groovy.editor.");
                    if (!myClass) {
                        invokedFrom = ele;
                        break;
                    }

                }
                // Special case that covers org.codehaus.groovy.transform.ASTTransformationVisitor that adds global transformations
                // to phaseOperations. Here the lambda stackframe is the same for all visitors, so I can try more hacky way - to pry out
                // the implicit reference to the delegate from the lambda. Naturally this will break as soon as implementation of 
                // ASTTransformationVisitor changes...
                if (cn.contains("ASTTransformationVisitor$$Lambda$")) {
                    String k = invokedFrom.getMethodName() + ":" + invokedFrom.getLineNumber();
                    Object ref = astTransformLambdaMap.computeIfAbsent(k, (x) -> {
                        try {
                            Field f = delegate.getClass().getDeclaredField("arg$1");
                            f.setAccessible(true);
                            return f;
                        } catch (ReflectiveOperationException ex) {
                            // ignore error, the impl may have change and the field is no longer there. Mark with String, so
                            // further attempts will find an entry != Field and will ignore the lambda.
                            return "NONE"; // NOI18N
                        }
                    });
                    // ref is posibly a marker String
                    if (ref instanceof Field) {
                        Object o = null;
                        try {
                            o = ((Field)ref).get(delegate);
                        } catch (ReflectiveOperationException ex) {
                        }
                        
                        if (o != null) {
                            key = o.getClass().getName();
                            return;
                        }
                    }
                } 
                key = invokedFrom != null ? invokedFrom.toString() : cn;
            } else {
                key = cn;
            }
        }

        @Override
        public void call(SourceUnit source) throws CompilationFailedException {
            throw new UnsupportedOperationException("Should not be called."); // NOI18N
        }

        @Override
        public void call(SourceUnit source, GeneratorContext context, ClassNode classNode) throws CompilationFailedException {
            throw new UnsupportedOperationException("Should not be called."); // NOI18N
        }

        @SuppressWarnings("unchecked")
        private <T extends Throwable> void sneakyThrow(Throwable exception) throws T {
                throw (T) exception;
        }        
        
        @Override
        public void doPhaseOperation(org.codehaus.groovy.control.CompilationUnit unit) throws CompilationFailedException {
            long t = System.currentTimeMillis();
            recordPhaseStart(phase);
            try {
                try {
                    doPhaseOperation.invoke(delegate, unit);
                } catch (InvocationTargetException ex) {
                    sneakyThrow(ex.getTargetException());
                } catch (ReflectiveOperationException ex) {
                    Exceptions.printStackTrace(ex);
                }
            } finally {
                long t2 = System.currentTimeMillis();
                perfData.addVisitorTime(getPhase(), key, t2 - t);
            }
        }

        @Override
        public boolean needSortedInput() {
            if (delegate instanceof IPrimaryClassNodeOperation) {
                return ((IPrimaryClassNodeOperation)delegate).needSortedInput();
            }
            throw new UnsupportedOperationException("Should not be called."); // NOI18N
        }

        @Override
        public void call(GroovyClass groovyClass) throws CompilationFailedException {
            throw new UnsupportedOperationException("Should not be called."); // NOI18N
        }
    }

    static boolean shouldCollect() {
        return PERFLOG.isLoggable(Level.FINER) && resolverVisitor != null;
    } 

    @Override
    public void addNewPhaseOperation(ISourceUnitOperation op, int phase) {
        super.addNewPhaseOperation(
                shouldCollect() ? new TimingOp(op, phase) : op, 
                phase);
    }

    @Override
    public void addFirstPhaseOperation(IPrimaryClassNodeOperation op, int phase) {
        super.addFirstPhaseOperation(
                shouldCollect() ? new TimingOp(op, phase) : op, 
                phase);
    }

    @Override
    public void addPhaseOperation(IPrimaryClassNodeOperation op, int phase) {
        super.addPhaseOperation(
                shouldCollect() ? (IPrimaryClassNodeOperation)new TimingOp(op, phase) : op, 
                phase);
    }

    @Override
    public void addPhaseOperation(ISourceUnitOperation op, int phase) {
        super.addPhaseOperation(
                shouldCollect() ? (ISourceUnitOperation)new TimingOp(op, phase) : op, 
                phase);
    }

    @Override
    public void addPhaseOperation(IGroovyClassOperation op) {
        super.addPhaseOperation(
                shouldCollect() ? new TimingOp(op, phase) : op
        );
    }

    @Override
    public void compile(int throughPhase) throws CompilationFailedException {
        String path = "";
        if (mainSnapshot.getSource().getFileObject() != null) {
            path = mainSnapshot.getSource().getFileObject().getPath();
        }
        PERFLOG.log(Level.FINER, "Parsing: {0}", path);
        PERFLOG.log(Level.FINER, "Time from CU create up to now: {0}", System.currentTimeMillis() - parsingStartTime);
        try {
            super.compile(throughPhase);
        } finally {
            if (PERFLOG.isLoggable(Level.FINER)) {
                PERFLOG.log(Level.FINER, "End Parsing: {0}", path);
                logAndCollectPhaseStats();
            }
        }
    }
}
