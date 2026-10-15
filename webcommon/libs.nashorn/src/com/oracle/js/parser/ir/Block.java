/*
 * Copyright (c) 2010, 2018, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * The Universal Permissive License (UPL), Version 1.0
 *
 * Subject to the condition set forth below, permission is hereby granted to any
 * person obtaining a copy of this software, associated documentation and/or
 * data (collectively the "Software"), free of charge and under any and all
 * copyright rights in the Software, and any and all patent rights owned or
 * freely licensable by each licensor hereunder covering either (i) the
 * unmodified Software as contributed to or provided by such licensor, or (ii)
 * the Larger Works (as defined below), to deal in both
 *
 * (a) the Software, and
 *
 * (b) any piece of software and/or hardware listed in the lrgrwrks.txt file if
 * one is included with the Software each a "Larger Work" to which the Software
 * is contributed by such licensors),
 *
 * without restriction, including without limitation the rights to copy, create
 * derivative works of, display, perform, and distribute the Software and make,
 * use, sell, offer for sale, import, export, have made, and have sold the
 * Software and the Larger Work(s), and to sublicense the foregoing rights on
 * either these or other terms.
 *
 * This license is subject to the following condition:
 *
 * The above copyright notice and either this complete permission notice or at a
 * minimum a reference to the UPL must be included in all copies or substantial
 * portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.oracle.js.parser.ir;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.oracle.js.parser.ir.visitor.NodeVisitor;
import com.oracle.js.parser.ir.visitor.TranslatorNodeVisitor;

// @formatter:off
/**
 * IR representation for a list of statements.
 */
public class Block extends Node implements BreakableNode, Terminal, Flags<Block> {
    /** List of statements */
    protected final List<Statement> statements;

    /** Symbol table - keys must be returned in the order they were put in. */
    protected final Map<String, Symbol> symbols;

    private int blockScopedOrRedeclaredSymbols;
    private int declaredNames;

    /** Entry label. */
    private final Label entryLabel;

    /** Break label. */
    private final Label breakLabel;

    /** Does the block/function need a new scope? Is this synthetic? */
    protected final int flags;

    /** Flag indicating that this block needs scope */
    public static final int NEEDS_SCOPE = 1 << 0;

    /**
     * Is this block tagged as terminal based on its contents
     * (usually the last statement)
     */
    public static final int IS_TERMINAL = 1 << 2;

    /**
     * Is this block the eager global scope - i.e. the original program. This isn't true for the
     * outermost level of recompiles
     */
    public static final int IS_GLOBAL_SCOPE = 1 << 3;

    /**
     * Is this block a synthetic one introduced by Parser?
     */
    public static final int IS_SYNTHETIC = 1 << 4;

    /**
     * Is this the function body block? May not be the first, if parameter list contains expressions.
     */
    public static final int IS_BODY = 1 << 5;

    /**
     * Is this the parameter initialization block? If present, must be the first block, immediately wrapping the function body block.
     */
    public static final int IS_PARAMETER_BLOCK = 1 << 6;

    /**
     * Marks the variable declaration block for case clauses of a switch statement.
     */
    public static final int IS_SWITCH_BLOCK = 1 << 7;

    /**
     * Constructor
     *
     * @param token      The first token of the block
     * @param finish     The index of the last character
     * @param flags      The flags of the block
     * @param statements All statements in the block
     */
    public Block(final long token, final int finish, final int flags, final Statement... statements) {
        super(token, finish);
        assert start <= finish;

        this.statements = Arrays.asList(statements);
        this.symbols    = new LinkedHashMap<>();
        this.entryLabel = new Label("block_entry");
        this.breakLabel = new Label("block_break");
        final int len = statements.length;
        final int terminalFlags = len > 0 && statements[len - 1].hasTerminalFlags() ? IS_TERMINAL : 0;
        this.flags = terminalFlags | flags;
    }

    /**
     * Constructs a new block
     *
     * @param token The first token of the block
     * @param finish The index of the last character
     * @param statements All statements in the block
     */
    public Block(final long token, final int finish, final List<Statement> statements) {
        this(token, finish, IS_SYNTHETIC, statements);
    }

    /**
     * Constructor
     *
     * @param token      The first token of the block
     * @param finish     The index of the last character
     * @param flags      The flags of the block
     * @param statements All statements in the block
     */
    public Block(final long token, final int finish, final int flags, final List<Statement> statements) {
        this(token, finish, flags, statements.toArray(new Statement[0]));
    }

    private Block(final Block block, final int finish, final List<Statement> statements, final int flags, final Map<String, Symbol> symbols) {
        super(block, finish);
        this.statements = statements;
        this.flags      = flags;
        this.symbols    = new LinkedHashMap<>(symbols);
        this.entryLabel = new Label(block.entryLabel);
        this.breakLabel = new Label(block.breakLabel);

        this.declaredNames = block.declaredNames;
        this.blockScopedOrRedeclaredSymbols = block.blockScopedOrRedeclaredSymbols;
    }

    /**
     * Is this block the outermost eager global scope - i.e. the primordial program?
     * Used for global anchor point for scope depth computation for recompilation code
     * @return true if outermost eager global scope
     */
    public boolean isGlobalScope() {
        return getFlag(IS_GLOBAL_SCOPE);
    }

    /**
     * Assist in IR navigation.
     *
     * @param visitor IR navigating visitor.
     * @return new or same node
     */
    @Override
    public Node accept(final LexicalContext lc, final NodeVisitor<? extends LexicalContext> visitor) {
        if (visitor.enterBlock(this)) {
            return visitor.leaveBlock(setStatements(lc, Node.accept(visitor, statements)));
        }

        return this;
    }

    @Override
    public <R> R accept(LexicalContext lc, TranslatorNodeVisitor<? extends LexicalContext, R> visitor) {
        return visitor.enterBlock(this);
    }

    /**
     * Get a copy of the list for all the symbols defined in this block
     * @return symbol iterator
     */
    public List<Symbol> getSymbols() {
        return Collections.unmodifiableList(new ArrayList<>(symbols.values()));
    }

    /**
     * Retrieves an existing symbol defined in the current block.
     * @param name the name of the symbol
     * @return an existing symbol with the specified name defined in the current block, or null if this block doesn't
     * define a symbol with this name.T
     */
    public Symbol getExistingSymbol(final String name) {
        return symbols.get(name);
    }

    /**
     * Test if this block represents a {@code true} block in a {@code try} statement.
     * This is used by the Splitter as catch blocks are not be subject to splitting.
     *
     * @return true if this block represents a catch block in a try statement.
     */
    public boolean isCatchBlock() {
        return statements.size() == 1 && statements.get(0) instanceof CatchNode;
    }

    @Override
    public void toString(final StringBuilder sb, final boolean printType) {
        for (final Node statement : statements) {
            statement.toString(sb, printType);
            sb.append(';');
        }
    }

    @Override
    public int getFlags() {
        return flags;
    }

    /**
     * Is this a terminal block, i.e. does it end control flow like ending with a throw or return?
     *
     * @return true if this node statement is terminal
     */
    @Override
    public boolean isTerminal() {
        return getFlag(IS_TERMINAL);
    }

    /**
     * Get the entry label for this block
     * @return the entry label
     */
    public Label getEntryLabel() {
        return entryLabel;
    }

    @Override
    public Label getBreakLabel() {
        return breakLabel;
    }

    /**
     * Get the list of statements in this block
     *
     * @return a list of statements
     */
    public List<Statement> getStatements() {
        return Collections.unmodifiableList(statements);
    }

    /**
     * Returns the number of statements in the block.
     * @return the number of statements in the block.
     */
    public int getStatementCount() {
        return statements.size();
    }

    /**
     * Returns the line number of the first statement in the block.
     * @return the line number of the first statement in the block, or -1 if the block has no statements.
     */
    public int getFirstStatementLineNumber() {
        if (statements == null || statements.isEmpty()) {
            return -1;
        }
        return statements.get(0).getLineNumber();
    }


    /**
     * Returns the first statement in the block.
     * @return the first statement in the block, or null if the block has no statements.
     */
    public Statement getFirstStatement() {
        return statements.isEmpty() ? null : statements.get(0);
    }

    /**
     * Returns the last statement in the block.
     * @return the last statement in the block, or null if the block has no statements.
     */
    public Statement getLastStatement() {
        return statements.isEmpty() ? null : statements.get(statements.size() - 1);
    }

    /**
     * Reset the statement list for this block
     *
     * @param lc lexical context
     * @param statements new statement list
     * @return new block if statements changed, identity of statements == block.statements
     */
    public Block setStatements(final LexicalContext lc, final List<Statement> statements) {
        if (this.statements == statements) {
            return this;
        }
        int lastFinish = 0;
        if (!statements.isEmpty()) {
            lastFinish = statements.get(statements.size() - 1).getFinish();
        }
        return Node.replaceInLexicalContext(lc, this, new Block(this, Math.max(finish, lastFinish), statements, flags, symbols));
    }

    /**
     * Add or overwrite an existing symbol in the block
     *
     * @param lc     get lexical context
     * @param symbol symbol
     */
    public void putSymbol(final LexicalContext lc, final Symbol symbol) {
        symbols.put(symbol.getName(), symbol);
        if (symbol.isBlockScoped() || symbol.isVarRedeclaredHere()) {
            blockScopedOrRedeclaredSymbols++;
        }
        if (symbol.isBlockScoped() || symbol.isVarDeclaredHere()) {
            declaredNames++;
        }
    }

    /**
     * Check whether scope is necessary for this Block
     *
     * @return true if this function needs a scope
     */
    public boolean needsScope() {
        return (flags & NEEDS_SCOPE) == NEEDS_SCOPE;
    }

    /**
     * Check whether this block is synthetic or not.
     *
     * @return true if this is a synthetic block
     */
    public boolean isSynthetic() {
        return (flags & IS_SYNTHETIC) == IS_SYNTHETIC;
    }

    @Override
    public Block setFlags(final LexicalContext lc, final int flags) {
        if (this.flags == flags) {
            return this;
        }
        return Node.replaceInLexicalContext(lc, this, new Block(this, finish, statements, flags, symbols));
    }

    @Override
    public Block setFlag(final LexicalContext lc, final int flag) {
        return setFlags(lc, flags | flag);
    }

    @Override
    public boolean getFlag(final int flag) {
        return (flags & flag) == flag;
    }

    @Override
    public boolean isBreakableWithoutLabel() {
        return false;
    }

    @Override
    public List<Label> getLabels() {
        return Collections.unmodifiableList(Arrays.asList(entryLabel, breakLabel));
    }

    @Override
    public Node accept(final NodeVisitor<? extends LexicalContext> visitor) {
        return Acceptor.accept(this, visitor);
    }

    @Override
    public <R> R accept(TranslatorNodeVisitor<? extends LexicalContext, R> visitor) {
        return Acceptor.accept(this, visitor);
    }

    public Map<String, Symbol> getSymbolMap() {
        return symbols;
    }

    public boolean hasBlockScopedOrRedeclaredSymbols() {
        return blockScopedOrRedeclaredSymbols != 0;
    }

    public boolean hasDeclarations() {
        return declaredNames != 0;
    }

    public boolean isFunctionBody() {
        return getFlag(IS_BODY);
    }

    public boolean isParameterBlock() {
        return getFlag(IS_PARAMETER_BLOCK);
    }

    public boolean isSwitchBlock() {
        return getFlag(IS_SWITCH_BLOCK);
    }
}
