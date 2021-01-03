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
package org.netbeans.modules.python.hints;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.netbeans.modules.python.source.PythonAstUtils;
import org.python.antlr.PythonTree;
import org.python.antlr.Visitor;
import org.python.antlr.ast.Assign;
import org.python.antlr.ast.Call;
import org.python.antlr.ast.FunctionDef;
import org.python.antlr.ast.Name;
import org.python.antlr.base.expr;

/** 
 * This visitor computes the set of input and output variables required by
 * a code block for extract method.
 * In particular, it tracks the local variable assignments inside the method,
 * and checks which are used outside of the method (which would make it an
 * output variable) and similarly, which variables are used inside the method
 * before getting assigned (which would make it an input variable).
 * 
 */
class InputOutputFinder extends Visitor {
    //private enum When { BEFORE, DURING, AFTER };
    private static final int WHEN_BEFORE = 0;
    private static final int WHEN_DURING = 1;
    private static final int WHEN_AFTER = 2;
    private final PythonTree startNode;
    private final PythonTree endNode;
    private final int startPos;
    private final int endPos;
    private final List<PythonTree> applicableBlocks;
    private int when = WHEN_BEFORE;
    private int ifs;
    //private PythonTree currentBlock;
    //private final List<PythonTree> blockStack = new ArrayList<PythonTree>(); // JDK16: Use Deque
    private Map<PythonTree, UsageScope> blockScopes = new HashMap<>();
    private UsageScope methodScope = new UsageScope(null);
    //private UsageScope blockScope;
    private PythonTree parent;
    private boolean isWriting;

    /** The node ranges are inclusive */
    InputOutputFinder(PythonTree startNode, PythonTree endNode, List<PythonTree> applicableBlocks) {
        this.startNode = startNode;
        this.endNode = endNode;
        this.applicableBlocks = applicableBlocks;

        startPos = startNode.getCharStartIndex();
        endPos = endNode.getCharStopIndex();
    }

    public Set<String> getInputVars() {
        UsageScope scope = methodScope;
        for (UsageScope s : blockScopes.values()) {
            if (s.block != null && !applicableBlocks.contains(s.block)) {
                continue;
            }
            scope.merge(s);
        }

        Set<String> inputs = new HashSet<>(scope.readDuring);
        // But not read before
        inputs.removeAll(scope.writtenBeforeReadDuring);

        // Also need to pass in any variables I'm modifying that are read after
        Set<String> outputs = new HashSet<>(scope.writtenDuring);
        outputs.retainAll(scope.readAfter);
        Set<String> extraOutputs = new HashSet<>(scope.writtenBefore);
        extraOutputs.retainAll(outputs);
        // unless they are written before read
        extraOutputs.removeAll(scope.writtenBeforeReadDuring);
        inputs.addAll(extraOutputs);

        return inputs;
    }

    public Set<String> getOutputVars() {
        UsageScope scope = methodScope;
        for (UsageScope s : blockScopes.values()) {
            if (s.block != null && !applicableBlocks.contains(s.block)) {
                continue;
            }
            scope.merge(s);
        }

        Set<String> outputs = new HashSet<>(scope.writtenDuring);
        outputs.retainAll(scope.readAfter);

        return outputs;
    }

    @Override
    public Object visitFunctionDef(FunctionDef node) throws Exception {
        // Record the parameters
//        assert when == WHEN_BEFORE; // Is this true when I extract a whole method? I can't do that, right?
        boolean x = true;
        assert x;

        for (String param : PythonAstUtils.getParameters(node)) {
            methodScope.write(param);
        }

        return super.visitFunctionDef(node);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Object visitAssign(Assign node) throws Exception {
        // Visit the right hand side of the assignment first, such
        // that with for example
        //    x = x + 1
        // we treat this as a read of x, before a write of x.
        // The Assign.traverse() implementation will do the targets first,
        // so we explicitly do it here in the opposite order instead...

        if (when == WHEN_BEFORE && node.getCharStartIndex() >= startPos) {
            when = WHEN_DURING;
        }
        int oldWhen = when;
        
        expr nodeValue = node.getInternalValue();
        if (nodeValue != null) {
            nodeValue.accept(this);
        }
        int newWhen = when;
        when = oldWhen;

        boolean oldWriting = isWriting;
        try {
            isWriting = true;
            List<expr> targets = node.getInternalTargets();
            if (targets != null) {
                for (expr expr : targets) {
                    if (expr != null) {
                        expr.accept(this);
                    }
                }
            }
        } finally {
            isWriting = oldWriting;
        }

        when = newWhen;

        return node;
    }

    @Override
    public Object visitName(Name node) throws Exception {
        if (parent instanceof Call && ((Call)parent).getInternalFunc() == node) { // Name in a call is the call name, not a variable
            return super.visitName(node);
        }

        methodScope.read(node.getInternalId());

        return super.visitName(node);
    }

    @Override
    public void traverse(PythonTree node) throws Exception {
        if (node == startNode) {
            when = WHEN_DURING;
        }

        PythonTree oldParent = parent;
        parent = node;
        super.traverse(node);
        parent = oldParent;

        if (node == endNode) {
            when = WHEN_AFTER;
        }

    }

    private class UsageScope {
        UsageScope(PythonTree block) {
            this.block = block;
        }

        private void read(String name) {
            // No need to pass class references or constants in/out
            // TODO: Make this smarter such that what it really does
            // is ignore any variables that aren't defined locally - so
            // global variables for example aren't passed in since they
            // can -also- be accessed from the extracted method.
            if (Character.isUpperCase(name.charAt(0))) {
                return;
            }

            if (isWriting) {
                // A read in the AST for example on the left hand side of an
                // assignment is really a write
                write(name);
                return;
            }

            if (when == WHEN_DURING) {
                if (!writtenBeforeReadDuring.contains(name)) {
                    readDuring.add(name);
                }
            } else if (when == WHEN_AFTER) {
                // I don't want a reassignment of the variable before it's been
                // read to count as a usage of the result from the fragment
                if (!writtenAfter.contains(name)) {
                    readAfter.add(name);
                }
            }
        }

        private void write(String name) {
            // No need to pass class references or constants in/out
            // TODO: Make this smarter such that what it really does
            // is ignore any variables that aren't defined locally - so
            // global variables for example aren't passed in since they
            // can -also- be accessed from the extracted method.
            if (Character.isUpperCase(name.charAt(0))) {
                return;
            }

            if (when == WHEN_BEFORE) {
                writtenBefore.add(name);
            } else if (when == WHEN_DURING) {
                writtenDuring.add(name);
                if (ifs == 0 && !readDuring.contains(name)) {
                    writtenBeforeReadDuring.add(name);
                }
            } else if (when == WHEN_AFTER) {
                if (ifs == 0 && !readAfter.contains(name)) {
                    writtenAfter.add(name);
                }
            }
        }

        private void merge(UsageScope other) {
            writtenBefore.addAll(other.writtenBefore);
            readDuring.addAll(other.readDuring);
            writtenDuring.addAll(other.writtenDuring);
            writtenBeforeReadDuring.addAll(other.writtenBeforeReadDuring);
            writtenAfter.addAll(other.writtenAfter);
            readAfter.addAll(other.readAfter);
        }
        /** Block, or null if it's the local method */
        private PythonTree block;
        /** Variables that exist in scope before the code fragment */
        private final Set<String> writtenBefore = new HashSet<>();
        /** Variables that are read during the code fragment */
        private final Set<String> readDuring = new HashSet<>(); // rename readBeforeWrittenDuring
        /** Variables that are written to during the code fragment */
        private final Set<String> writtenDuring = new HashSet<>();
        /** Variables that are written to during the code fragment */
        private final Set<String> writtenBeforeReadDuring = new HashSet<>();
        /** Variables that are written PRIOR TO A READ OF THE SAME VAR after the code fragment */
        private final Set<String> writtenAfter = new HashSet<>(); // rename writtenBeforeReadAfter
        /** Variables that are read (prior to a write) after the code fragment */
        private final Set<String> readAfter = new HashSet<>(); // rename readBeforeWrittenAfter
    }
}
