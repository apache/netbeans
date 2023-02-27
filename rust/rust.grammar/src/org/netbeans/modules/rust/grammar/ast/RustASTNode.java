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
package org.netbeans.modules.rust.grammar.ast;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.TreeMap;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.netbeans.modules.csl.api.OffsetRange;

/**
 * A node in the Rust AST. This can be a Rust crate, a Rust enum, for instance.
 *
 * @see <a href="https://en.wikipedia.org/wiki/Composite_pattern">Composite
 * Pattern</a>
 */
public class RustASTNode {

    /**
     * The kind of this node.
     */
    private final RustASTNodeKind kind;
    /**
     * The index where this node starts in the text.
     */
    private int start;
    /**
     * The index where this node ends in the text.
     */
    private int stop;
    /**
     * The text/name of this node. This can be the name of a function, for
     * instance.
     */
    private String name;
    /**
     * The inner functions in this node.
     */
    private final TreeMap<String, RustASTNode> functions;
    /**
     * The inner structs in this node.
     */
    private final TreeMap<String, RustASTNode> structs;
    /**
     * The inner impls in this node.
     */
    private final TreeMap<String, RustASTNode> impls;
    /**
     * The inner traits in this node.
     */
    private final TreeMap<String, RustASTNode> traits;
    /**
     * The inner enums in this node.
     */
    private final TreeMap<String, RustASTNode> enums;
    /**
     * The inner macros in this node.
     */
    private final TreeMap<String, RustASTNode> macros;
    /**
     * The modules in this node.
     */
    private final TreeMap<String, RustASTNode> modules;
    /**
     * Any nested folds this node may have.
     */
    private final List<OffsetRange> codeblockFolds;

    /**
     * Our own fold.
     */
    private OffsetRange fold;

    /**
     * This node's parent.
     */
    private RustASTNode parent;

    public RustASTNode(RustASTNodeKind kind) {
        this.kind = kind;
        this.enums = new TreeMap<>();
        this.functions = new TreeMap<>();
        this.impls = new TreeMap<>();
        this.macros = new TreeMap<>();
        this.modules = new TreeMap<>();
        this.structs = new TreeMap<>();
        this.traits = new TreeMap<>();
        this.codeblockFolds = new ArrayList<>();
    }

    /**
     * Recursively visits this node's children with a visitor.
     *
     * @param visitor The visitor.
     */
    public void visit(Consumer<RustASTNode> visitor) {
        enums().forEach(visitor);
        functions().forEach(visitor);
        impls().forEach(visitor);
        macros().forEach(visitor);
        modules().forEach(visitor);
        structs().forEach(visitor);
        traits().forEach(visitor);
    }

    private void setParent(RustASTNode parent) {
        this.parent = parent;
    }

    public RustASTNode getParent() {
        return parent;
    }

    public RustASTNodeKind getKind() {
        return kind;
    }

    /**
     * Sets the range of offsets for this node.
     *
     * @param startToken The first token of this node.
     * @param stopToken The last token of this node.
     */
    public void setRange(Token startToken, Token stopToken) {
        start = startToken.getStartIndex();
        stop = stopToken.getStopIndex() + 1;
        if (stop <= start) {
            stop = start;
        }
    }

    /**
     * Sets the fold for this node.
     */
    public void setFold(TerminalNode start, TerminalNode stop) {
        if (start != null && stop != null) {
            int startOffset = start.getSymbol().getStartIndex();
            int stopOffset = stop.getSymbol().getStopIndex() + 1;
            if (stopOffset <= startOffset) {
                stopOffset = startOffset;
            }
            if (startOffset > 0) {
                this.fold = new OffsetRange(startOffset, stopOffset);
            } else {
                this.fold = new OffsetRange(0, 1);
            }
        }
    }

    /**
     * Returns this node's fold, if any.
     *
     * @return The fold for this node, or null.
     */
    public OffsetRange getFold() {
        return fold;
    }

    /**
     * Adds a fold for a child node. For instance, a function may have multiple
     * "if" statements, and these may have folds grouping statements between '{'
     * and '}'.
     *
     * @param startNode a Terminal node where the nested fold starts.
     * @param stopNode a Terminal node where the nested fold ends.
     */
    public void addCodeblockFold(TerminalNode startNode, TerminalNode stopNode) {
        if (startNode != null && stopNode != null) {
            Token startToken = startNode.getSymbol();
            Token stopToken = stopNode.getSymbol();
            int foldStart = startToken.getStartIndex();
            int foldStop = stopToken.getStartIndex() + 1;
            if (foldStart > 0 && foldStart <= foldStop) {
                OffsetRange range = new OffsetRange(foldStart, foldStop);
                if (!codeblockFolds.contains(range)) {
                    codeblockFolds.add(range);
                }
            }
        }
    }

    public List<OffsetRange> codeblockFolds() {
        return codeblockFolds;
    }

    public int getStop() {
        return stop;
    }

    public int getStart() {
        return start;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Collection<String> structNames() {
        return structs.keySet();
    }

    public Collection<RustASTNode> structs() {
        return structs.values();
    }

    public void addStruct(RustASTNode struct) {
        struct.setParent(this);
        structs.put(struct.getName(), struct);
    }

    public RustASTNode getStruct(String name) {
        return structs.get(name);
    }

    public Collection<String> functionNames() {
        return functions.keySet();
    }

    public void addFunction(RustASTNode function) {
        function.setParent(this);
        functions.put(function.getName(), function);
    }

    public RustASTNode getFunction(String name) {
        return functions.get(name);
    }

    public Collection<RustASTNode> functions() {
        return functions.values();
    }

    public Collection<String> implNames() {
        return impls.keySet();
    }

    public void addImpl(RustASTNode impl) {
        impl.setParent(this);
        impls.put(impl.getName(), impl);
    }

    public RustASTNode getImpl(String name) {
        return impls.get(name);
    }

    public Collection<RustASTNode> impls() {
        return impls.values();
    }

    public void addTrait(RustASTNode trait) {
        trait.setParent(this);
        traits.put(trait.getName(), trait);
    }

    public RustASTNode getTrait(String name) {
        return traits.get(name);
    }

    public Collection<RustASTNode> traits() {
        return traits.values();
    }

    public void addEnum(RustASTNode e) {
        e.setParent(this);
        enums.put(e.getName(), e);
    }

    public RustASTNode getEnum(String name) {
        return enums.get(name);
    }

    public Collection<RustASTNode> enums() {
        return enums.values();
    }

    public void addMacro(RustASTNode macro) {
        macro.setParent(this);
        macros.put(macro.getName(), macro);
    }

    public RustASTNode getMacro(String name) {
        return macros.get(name);
    }

    public Collection<RustASTNode> macros() {
        return macros.values();
    }

    public void addModule(RustASTNode module) {
        module.setParent(this);
        modules.put(module.getName(), module);
    }

    public RustASTNode getModule(String name) {
        return modules.get(name);
    }

    public Collection<RustASTNode> modules() {
        return modules.values();
    }

    @Override
    public String toString() {
        return String.format("[%s:%s]", kind.name(), name);
    }

}
