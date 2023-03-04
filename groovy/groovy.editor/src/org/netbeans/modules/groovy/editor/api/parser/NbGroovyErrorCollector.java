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
package org.netbeans.modules.groovy.editor.api.parser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.expr.ClosureExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.control.CompilationFailedException;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.ErrorCollector;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.control.messages.Message;
import org.codehaus.groovy.control.messages.SyntaxErrorMessage;
import org.codehaus.groovy.transform.StaticTypesTransformation;
import org.codehaus.groovy.transform.stc.StaticTypeCheckingVisitor;

/**
 * Since the parsing uses static typing, some of the reported errors must be filtered
 * out.
 * <p/>
 * We want the parsing to fail with errors and report static type errors - but at the same time
 * we need the later compilation phases to proceed. So this Collector filters out static
 * typing errors, does not report them until told at the end of the compilation.
 * <p/>
 * Some of the errors are really artifact of static compilation and must be filtered out completely.
 * They are collected, but not reported anywhere at this moment. The list of errors to be 
 * filtered out si in resource {@code FilteredStaticErrors.lst}, see comments in there for details.
 * 
 * @author sdedic
 */
class NbGroovyErrorCollector extends ErrorCollector {

    /**
     * Resource that defines error messages that should be filtered out completely. Groovy rejects some
     * constructions for static compilation, but we only use static compile to force attribution of the
     * AST.
     */
    private static final String RESOURCE_FILTERED_ERRORS = "FilteredStaticErrors.lst"; // NOI18N
    
    private List<Message> filteredErrors = new ArrayList<>();
    private List<Message> staticAnalysisErrors = new ArrayList<>();
    private Map<Message, ASTNode> errorSources;
    
    /**
     * If true, {@link #$getErrors} and the like return all errors, including the
     * ones filtered out.
     */
    private boolean showStaticCompileErrors;
    private boolean showFilteredErrors;
    
    private boolean disableErrors;
    
    /**
     * Cached combined errors.s
     */
    private List<Message> combinedErrors;
    
    /**
     * Maps an error message to the AST node. The message itself contains just line:column info
     */
    private Map<Message, ASTNode>   error2Node = new HashMap<>();
    
    /**
     * Reverse map that holds err messages for an AST node
     */
    private Map<ASTNode, List<Message>>   node2Errors = new HashMap<>();
    
    /**
     * Ambiguous references reported for a constructor or a method call, May be useful in
     * code completion
     */
    private Map<ASTNode, AmbiguousReference> amiguousCandidates = new HashMap<>();
    
    /**
     * True, if in closure context. In this case, undefined symbol errors will be ignored,
     * as they may be in the delegate.
     */
    private ASTNode closureContext;
    
    /**
     * Not used at the moment, but when ambiguous reference is reported, the reporter passes 
     * in candidate symbols, so they are saved & attached to the error and the referencing
     * AST node.
     */
    static class AmbiguousReference {
        /**
         * The referencing node
         */
        private final Expression astNode;
        
        /**
         * Candidate methods
         */
        private final List<MethodNode> candidates;
        
        /**
         * Actual parameter types
         */
        private final List<ClassNode> parameterTypes;

        public AmbiguousReference(Expression astNode, List<MethodNode> candidates, List<ClassNode> parameterTypes) {
            this.astNode = astNode;
            this.candidates = candidates;
            this.parameterTypes = parameterTypes;
        }

        public Expression getAstNode() {
            return astNode;
        }

        public List<MethodNode> getCandidates() {
            return candidates;
        }

        public List<ClassNode> getParameterTypes() {
            return parameterTypes;
        }
    }

    public NbGroovyErrorCollector(CompilerConfiguration configuration) {
        super(configuration);
        initErrorFilter();
    }

    public List<Message> getFilteredErrors() {
        return filteredErrors;
    }

    public void setFilteredErrors(List<Message> filteredErrors) {
        this.filteredErrors = filteredErrors;
        combinedErrors = null;
    }

    public boolean isShowAllErrors() {
        return showStaticCompileErrors;
    }

    public void setShowAllErrors(boolean showAllErrors) {
        this.showStaticCompileErrors = showAllErrors;
        combinedErrors = null;
    }
    
     protected void failIfErrors() throws CompilationFailedException {
        super.failIfErrors();
    }

    public boolean isDisableErrors() {
        return disableErrors;
    }

    public void setDisableErrors(boolean disableErrors) {
        this.disableErrors = disableErrors;
    }

    @Override
    public boolean hasErrors() {
        if (disableErrors) {
            return false;
        }
        List<? extends Message> errs = getErrors();
        return errs != null && !errs.isEmpty();
    }

    @Override
    public List<? extends Message> getErrors() {
        if (disableErrors) {
            return Collections.emptyList();
        }
        return showStaticCompileErrors ? getAllErrors() : super.getErrors();
    }

    @Override
    public Message getError(int index) {
        List<? extends Message> errs = getErrors();
        return index >= errs.size() ? null : errs.get(index);
    }

    @Override
    public int getErrorCount() {
        if (disableErrors) {
            return 0;
        }
        List<? extends Message> errs = getErrors();
        return errs.size();
    }

    private void addStaticTypingError(SyntaxErrorMessage m) {
        String msg = m.getCause().getMessage();
        if (!msg.startsWith(StaticTypesTransformation.STATIC_ERROR_PREFIX)) {
            super.addErrorAndContinue(m);
            return;
        }
        if (filtersError(msg)) {
            filteredErrors.add(m);
            return;
        }
        // special case: do not report unknown symbols in closure context:
        // the Closure may dispatch to its (mutable) delegate at runtime, so the code
        // might be eventually correct.
        if (getClosureContext() != null) {
            if (msg.startsWith("[Static type checking] - No such ") || // NOI18N
                msg.startsWith("[Static type checking] - The variable ")) { // NOI18N
                // suppress
                return;
            }
        }
        staticAnalysisErrors.add(m);
    }
    
    public List<Message> getAllErrors() {
        if (disableErrors) {
            return Collections.emptyList();
        }
        if (combinedErrors == null) {
            List<? extends Message> base = super.getErrors();
            combinedErrors = new ArrayList<>();
            if (base != null) {
                combinedErrors.addAll(base);
            }
            combinedErrors.addAll(staticAnalysisErrors);
            if (showFilteredErrors) {
                combinedErrors.addAll(filteredErrors);
            }
        }
        return combinedErrors;
    }
    
    private ASTNode errNode = null;

    @Override
    public void addErrorAndContinue(String error, ASTNode node, SourceUnit source) {
        ASTNode n = errNode;
        errNode = node;
        try {
            super.addErrorAndContinue(error, node, source); 
        } finally {
            errNode = n;
        }
    }

    @Override
    public void addErrorAndContinue(Message message) {
        combinedErrors = null;
        if (errNode != null) {
            error2Node.put(message, errNode);
            node2Errors.computeIfAbsent(errNode, (n) -> new ArrayList<>(2)).add(message);
        }
        if (message instanceof SyntaxErrorMessage) {
            SyntaxErrorMessage sm = (SyntaxErrorMessage) message;
            addStaticTypingError(sm);
            return;
        }
        super.addErrorAndContinue(message);
    }

    /**
     * Returns the nearest enclosing Closure node
     * @return nearest Closure, or {@code null} if no closure is present
     */
    public ASTNode getClosureContext() {
        return closureContext;
    }

    /**
     * Establishes closure context, returns the previous value. The caller is responsible
     * for saving the previous value and restoring it after the closure is processed.
     * @param closureContext new closure context
     * @return previous value.
     */
    public ASTNode setClosureContext(ASTNode closureContext) {
        ASTNode save = this.closureContext;
        this.closureContext = closureContext;
        return save;
    }
    
    private static Pattern errorFilter;
    
    private void initErrorFilter() {
        if (errorFilter != null) {
            return;
        }
        StringBuilder sb = new StringBuilder();
        try (InputStream is = NbGroovyErrorCollector.class.getResourceAsStream(RESOURCE_FILTERED_ERRORS);
             BufferedReader r = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            String line;
            
            while ((line = r.readLine()) != null) {
                if (line.startsWith("#")) {
                    continue;
                }
                line = line.trim();
                if (line.isEmpty()) {
                    continue;
                }
                if (sb.length() > 0) {
                    sb.append("|");
                }
                sb.append(line.trim());
            }
            errorFilter = Pattern.compile(sb.toString());
        } catch (IOException ex) {
            throw new IllegalStateException(ex);
        }
    }
    
    void markAmbiguous(Expression node, List<MethodNode> candidates, List<ClassNode> types) {
        amiguousCandidates.put(errNode, new AmbiguousReference(node, candidates, types));
    }

    public boolean filtersError(String msg) {
        if (msg.startsWith(StaticTypesTransformation.STATIC_ERROR_PREFIX)) {
            msg = msg.substring(StaticTypesTransformation.STATIC_ERROR_PREFIX.length()).trim();
        } else {
            return false;
        }
        return errorFilter.matcher(msg).matches();
    }

    /**
     * Visitor that reports candidates for an ambiguous call to the collector. The original
     * method just prettyprints them.
     */
    static class NbStaticTypeCheckingVisitor extends StaticTypeCheckingVisitor {
        private final NbGroovyErrorCollector errCollector;
        
        NbStaticTypeCheckingVisitor(SourceUnit source, ClassNode classNode, NbGroovyErrorCollector errorCollector) {
            super(source, classNode);
            this.errCollector = errorCollector;
        }

        @Override
        public void visitClosureExpression(ClosureExpression expression) {
            ASTNode save = errCollector.getClosureContext();
            try {
                errCollector.setClosureContext(expression);
                super.visitClosureExpression(expression); 
            } finally {
                errCollector.setClosureContext(save);
            }
        }

        @Override
        protected void addAmbiguousErrorMessage(List<MethodNode> foundMethods, String name, ClassNode[] args, Expression expr) {
            errCollector.markAmbiguous(expr, foundMethods, Arrays.asList(args));
            super.addAmbiguousErrorMessage(foundMethods, name, args, expr);
        }
    }
}
