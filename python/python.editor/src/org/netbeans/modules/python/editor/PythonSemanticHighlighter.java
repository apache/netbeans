/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.python.editor;

import org.netbeans.modules.python.source.PythonParserResult;
import org.netbeans.modules.python.source.PythonAstUtils;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.csl.api.ColoringAttributes;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.api.SemanticAnalyzer;
import org.netbeans.modules.parsing.spi.Scheduler;
import org.netbeans.modules.parsing.spi.SchedulerEvent;
import org.netbeans.modules.python.api.Util;
import org.netbeans.modules.python.source.scopes.ScopeInfo;
import org.netbeans.modules.python.source.scopes.SymbolTable;
import org.openide.util.Exceptions;
import org.python.antlr.PythonTree;
import org.python.antlr.Visitor;
import org.python.antlr.ast.ClassDef;
import org.python.antlr.ast.FunctionDef;
import org.python.antlr.ast.Module;
import org.python.antlr.ast.Name;

/**
 * Semantic highlighter for Python.
 *
 */
public class PythonSemanticHighlighter extends SemanticAnalyzer<PythonParserResult> {
    private boolean cancelled;
    private Map<OffsetRange, Set<ColoringAttributes>> semanticHighlights;

    @Override
    public Map<OffsetRange, Set<ColoringAttributes>> getHighlights() {
        return semanticHighlights;
    }

    protected final synchronized boolean isCancelled() {
        return cancelled;
    }

    protected final synchronized void resume() {
        cancelled = false;
    }

    @Override
    public final synchronized void cancel() {
        cancelled = true;
    }

    @Override
    public int getPriority() {
        return 0;
    }

    @Override
    public Class<? extends Scheduler> getSchedulerClass() {
        return Scheduler.EDITOR_SENSITIVE_TASK_SCHEDULER;
    }
    
    @Override
    public void run(PythonParserResult pr, SchedulerEvent event) {
        resume();

        if (isCancelled()) {
            return;
        }

        PythonTree root = PythonAstUtils.getRoot(pr);
        if (root == null) {
            return;
        }

        SymbolTable symbolTable = pr.getSymbolTable();

        SemanticVisitor visitor = new SemanticVisitor(pr, symbolTable);
        try {
            visitor.visit(root);
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }
        semanticHighlights = visitor.getHighlights();
    }

    private static class SemanticVisitor extends Visitor {
        private static final Logger LOGGER = Logger.getLogger(Util.class.getName());
        private final PythonParserResult info;
        private Map<OffsetRange, Set<ColoringAttributes>> highlights =
                new HashMap<>(100);
        private final SymbolTable symbolTable;
        private ScopeInfo scope;

        SemanticVisitor(PythonParserResult info, SymbolTable symbolTable) {
            this.info = info;
            this.symbolTable = symbolTable;
        }

        @Override
        public Object visitModule(Module node) throws Exception {
            ScopeInfo oldScope = scope;
            scope = symbolTable.getScopeInfo(node);
            Object ret;
            try {
                ret = super.visitModule(node);
            } catch(RuntimeException ex) {
                // Fix for https://netbeans.org/bugzilla/show_bug.cgi?id=255247
                if (ex.getMessage().startsWith("Unexpected node: <mismatched token: [@")) {
                   ret = null;
                   LOGGER.log(Level.FINE, ex.getMessage());
                } else {
                    throw ex;
                }
            }
            scope = oldScope;

            return ret;
        }

        @Override
        public Object visitClassDef(ClassDef node) throws Exception {
            OffsetRange range = PythonAstUtils.getNameRange(info, node);
            highlights.put(range, ColoringAttributes.CLASS_SET);

            ScopeInfo oldScope = scope;
            scope = symbolTable.getScopeInfo(node);
            Object ret = super.visitClassDef(node);
            scope = oldScope;

            return ret;
        }

        public Map<OffsetRange, Set<ColoringAttributes>> getHighlights() {
            return highlights;
        }

        @Override
        public Object visitFunctionDef(FunctionDef def) throws Exception {
            OffsetRange range = PythonAstUtils.getNameRange(info, def);
            highlights.put(range, ColoringAttributes.METHOD_SET);

            // vararg and kwarg from the function definition line must be handled here, since they
            // are passed to visit name unlike ordinary arguments.
            highlightVarargAndKwargs(def);

            ScopeInfo oldScope = scope;
            scope = symbolTable.getScopeInfo(def);
            Object result = super.visitFunctionDef(def);
            scope = oldScope;
            return result;
        }

        private void highlightVarargAndKwargs(FunctionDef def) {
            Name vararg = def.getInternalArgs().getInternalVarargName();
            highlightName(vararg, ColoringAttributes.PARAMETER_SET);
            Name kwarg = def.getInternalArgs().getInternalKwargName();
            highlightName(kwarg, ColoringAttributes.PARAMETER_SET);
        }

        private void highlightName(Name name, EnumSet<ColoringAttributes> color) {
            if (name != null && !color.isEmpty()) {
                OffsetRange range = PythonAstUtils.getNameRange(info, name);
                highlights.put(range, color);
            }
        }

        @Override
        public Object visitName(Name node) throws Exception {
            String name = node.getInternalId();
            if (scope != null) {
                EnumSet<ColoringAttributes> color = EnumSet.noneOf(ColoringAttributes.class);
                if (scope.isUnused(name)) {
                    color.add(ColoringAttributes.UNUSED);
                }

                if (scope.isParameter(name) && !name.equals("self")) {
                    color.add(ColoringAttributes.PARAMETER);
                }

                highlightName(node, color);
            }

            return super.visitName(node);
        }
    }
}
