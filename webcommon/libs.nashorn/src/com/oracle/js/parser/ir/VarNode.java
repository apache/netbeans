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

import com.oracle.js.parser.ir.visitor.NodeVisitor;
import com.oracle.js.parser.ir.visitor.TranslatorNodeVisitor;

// @formatter:off
/**
 * Node represents a var/let declaration.
 */
public final class VarNode extends Statement implements Assignment<IdentNode> {
    /** Var name. */
    private final IdentNode name;

    /** Initialization expression. */
    private final Expression init;

    /** Is this a var statement (as opposed to a "var" in a for loop statement) */
    private final int flags;

    /**
     * source order id to be used for this node. If this is -1, then we
     * the default which is start position of this node. See also the
     * method Node::getSourceOrder.
     */
    private final int sourceOrder;

    /** Flag for ES6 LET declaration */
    public static final int IS_LET                       = 1 << 0;

    /** Flag for ES6 CONST declaration */
    public static final int IS_CONST                     = 1 << 1;

    /** Flag that determines if this is the last function declaration in a function
     *  This is used to micro optimize the placement of return value assignments for
     *  a program node */
    public static final int IS_LAST_FUNCTION_DECLARATION = 1 << 2;

    /** Flag synthetic export var node */
    public static final int IS_EXPORT = 1 << 3;

    /** Flag synthetic destructuring var node */
    public static final int IS_DESTRUCTURING = 1 << 4;

    /**
     * Constructor
     *
     * @param lineNumber line number
     * @param token      token
     * @param finish     finish
     * @param name       name of variable
     * @param init       init node or null if just a declaration
     */
    public VarNode(final int lineNumber, final long token, final int finish, final IdentNode name, final Expression init) {
        this(lineNumber, token, finish, name, init, 0);
    }

    private VarNode(final VarNode varNode, final IdentNode name, final Expression init, final int flags) {
        super(varNode);
        this.sourceOrder = -1;
        this.name = init == null ? name : name.setIsInitializedHere();
        this.init = init;
        this.flags = flags;
    }

    /**
     * Constructor
     *
     * @param lineNumber  line number
     * @param token       token
     * @param finish      finish
     * @param name        name of variable
     * @param init        init node or null if just a declaration
     * @param flags       flags
     */
    public VarNode(final int lineNumber, final long token, final int finish, final IdentNode name, final Expression init, final int flags) {
        this(lineNumber, token, -1, finish, name, init, flags);
    }

    /**
     * Constructor
     *
     * @param lineNumber  line number
     * @param token       token
     * @param sourceOrder source order
     * @param finish      finish
     * @param name        name of variable
     * @param init        init node or null if just a declaration
     * @param flags       flags
     */
    public VarNode(final int lineNumber, final long token, final int sourceOrder, final int finish, final IdentNode name, final Expression init, final int flags) {
        super(lineNumber, token, finish);
        this.sourceOrder = sourceOrder;
        this.name  = init == null ? name : name.setIsInitializedHere();
        this.init  = init;
        this.flags = flags;
    }

    @Override
    public int getSourceOrder() {
        return sourceOrder == -1 ? super.getSourceOrder() : sourceOrder;
    }

    @Override
    public boolean isAssignment() {
        return hasInit();
    }

    @Override
    public IdentNode getAssignmentDest() {
        return isAssignment() ? name : null;
    }

    @Override
    public Expression getAssignmentSource() {
        return isAssignment() ? getInit() : null;
    }

    /**
     * Is this a VAR node block scoped? This returns true for ECMAScript 6 LET and CONST nodes.
     * @return true if an ES6 LET or CONST node
     */
    public boolean isBlockScoped() {
        return getFlag(IS_LET) || getFlag(IS_CONST);
    }

    /**
     * Is this an ECMAScript 6 LET node?
     * @return true if LET node
     */
    public boolean isLet() {
        return getFlag(IS_LET);
    }

    /**
     * Is this an ECMAScript 6 CONST node?
     * @return true if CONST node
     */
    public boolean isConst() {
        return getFlag(IS_CONST);
    }

    /**
     * Return the flags to use for symbols for this declaration.
     * @return the symbol flags
     */
    public int getSymbolFlags() {
        if (isLet()) {
            return Symbol.IS_VAR | Symbol.IS_LET;
        } else if (isConst()) {
            return Symbol.IS_VAR | Symbol.IS_CONST;
        }
        return Symbol.IS_VAR;
    }

    /**
     * Does this variable declaration have an init value
     * @return true if an init exists, false otherwise
     */
    public boolean hasInit() {
        return init != null;
    }

    /**
     * Assist in IR navigation.
     * @param visitor IR navigating visitor.
     */
    @Override
    public Node accept(final NodeVisitor<? extends LexicalContext> visitor) {
        if (visitor.enterVarNode(this)) {
            // var is right associative, so visit init before name
            final Expression newInit = init == null ? null : (Expression)init.accept(visitor);
            final IdentNode  newName = (IdentNode)name.accept(visitor);
            final VarNode    newThis;
            if (name != newName || init != newInit) {
                newThis = new VarNode(this, newName, newInit, flags);
            } else {
                newThis = this;
            }
            return visitor.leaveVarNode(newThis);
        }
        return this;
    }

    @Override
    public <R> R accept(TranslatorNodeVisitor<? extends LexicalContext, R> visitor) {
        return visitor.enterVarNode(this);
    }

    @Override
    public void toString(final StringBuilder sb, final boolean printType) {
        sb.append(tokenType().getName()).append(' ');
        name.toString(sb, printType);

        if (init != null) {
            sb.append(" = ");
            init.toString(sb, printType);
        }
    }

    /**
     * If this is an assignment of the form {@code var x = init;}, get the init part.
     * @return the expression to initialize the variable to, null if just a declaration
     */
    public Expression getInit() {
        return init;
    }

    /**
     * Reset the initialization expression
     * @param init new initialization expression
     * @return a node equivalent to this one except for the requested change.
     */
    public VarNode setInit(final Expression init) {
        if (this.init == init) {
            return this;
        }
        return new VarNode(this, name, init, flags);
    }

    /**
     * Get the identifier for the variable
     * @return IdentNode representing the variable being set or declared
     */
    public IdentNode getName() {
        return name;
    }

    /**
     * Reset the identifier for this VarNode
     * @param name new IdentNode representing the variable being set or declared
     * @return a node equivalent to this one except for the requested change.
     */
    public VarNode setName(final IdentNode name) {
        if (this.name == name) {
            return this;
        }
        return new VarNode(this, name, init, flags);
    }

    private VarNode setFlags(final int flags) {
        if (this.flags == flags) {
            return this;
        }
        return new VarNode(this, name, init, flags);
    }

    /**
     * Check if a flag is set for this var node
     * @param flag flag
     * @return true if flag is set
     */
    public boolean getFlag(final int flag) {
        return (flags & flag) == flag;
    }

    /**
     * Set a flag for this var node
     * @param flag flag
     * @return new node if flags changed, same otherwise
     */
    public VarNode setFlag(final int flag) {
        return setFlags(flags | flag);
    }

    /**
     * Returns true if this is a function declaration.
     * @return true if this is a function declaration.
     */
    public boolean isFunctionDeclaration() {
        return init instanceof FunctionNode && ((FunctionNode)init).isDeclared();
    }

    public boolean isExport() {
        return getFlag(IS_EXPORT);
    }

    public boolean isDestructuring() {
        return getFlag(IS_DESTRUCTURING);
    }
}
