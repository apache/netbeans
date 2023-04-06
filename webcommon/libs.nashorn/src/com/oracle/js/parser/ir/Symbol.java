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

// @formatter:off
/**
 * Symbol is a symbolic address for a value ("variable" if you wish). Identifiers in JavaScript
 * source, as well as certain synthetic variables created by the compiler are represented by Symbol
 * objects. Symbols can address either local variable slots in bytecode ("slotted symbol"), or
 * properties in scope objects ("scoped symbol"). A symbol can also end up being defined but then
 * not used during symbol assignment calculations; such symbol will be neither scoped, nor slotted;
 * it represents a dead variable (it might be written to, but is never read). Finally, a symbol can
 * be both slotted and in scope. This special case can only occur with bytecode method parameters.
 * They all come in as slotted, but if they are used by a nested function (or eval) then they will
 * be copied into the scope object, and used from there onwards. Two further special cases are
 * parameters stored in {@code NativeArguments} objects and parameters stored in {@code Object[]}
 * parameter to variable-arity functions. Those use the {@code #getFieldIndex()} property to refer
 * to their location.
 */
public final class Symbol implements Comparable<Symbol> {
    /** Is this Global */
    public static final int IS_GLOBAL = 1;
    /** Is this a variable */
    public static final int IS_VAR = 2;
    /** Is this a parameter */
    public static final int IS_PARAM = 3;
    /** Mask for kind flags */
    public static final int KINDMASK = (1 << 2) - 1; // Kinds are represented by lower three bits

    /** Is this symbol in scope */
    public static final int IS_SCOPE = 1 << 2;
    /** Is this a this symbol */
    public static final int IS_THIS = 1 << 3;
    /** Is this a let */
    public static final int IS_LET = 1 << 4;
    /** Is this a const */
    public static final int IS_CONST = 1 << 5;
    /** Is this an internal symbol, never represented explicitly in source code */
    public static final int IS_INTERNAL = 1 << 6;
    /** Is this a function self-reference symbol */
    public static final int IS_FUNCTION_SELF = 1 << 7;
    /** Is this a function declaration? */
    public static final int IS_FUNCTION_DECLARATION = 1 << 8;
    /** Is this a program level symbol? */
    public static final int IS_PROGRAM_LEVEL = 1 << 9;
    /** Is this symbol seen a declaration? Used for block scoped LET and CONST symbols only. */
    public static final int HAS_BEEN_DECLARED = 1 << 10;
    /**
     * Is this symbol a var declaration instantiated in this block?
     * If not, this is a var declaration hoisted to another block.
     * Used for duplicate checking with block scoped symbols.
     */
    public static final int IS_VAR_DECLARED_HERE = 1 << 11;
    /**
     * Is this symbol a var declaration binding that needs to be initialized with the value of the parent's scope's binding with the same name?
     * Used for parameter bindings that are replicated in the body's VariableEnvironment.
     */
    public static final int IS_VAR_REDECLARED_HERE = 1 << 12;
    /** Is this symbol declared in an unprotected switch case context? */
    public static final int IS_DECLARED_IN_SWITCH_BLOCK = 1 << 13;

    /** Null or name identifying symbol. */
    private final String name;

    /** Symbol flags. */
    private int flags;

    /** Number of times this symbol is used in code */
    private int useCount;

    /**
     * Constructor
     *
     * @param name name of symbol
     * @param flags symbol flags
     */
    public Symbol(final String name, final int flags) {
        this.name = name;
        this.flags = flags;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();

        sb.append(name);

        if (isScope()) {
            if (isGlobal()) {
                sb.append(" G");
            } else {
                sb.append(" S");
            }
        }

        return sb.toString();
    }

    @Override
    public int compareTo(final Symbol other) {
        return name.compareTo(other.name);
    }

    /**
     * Check if this is a symbol in scope. Scope symbols cannot, for obvious reasons be stored in
     * byte code slots on the local frame
     *
     * @return true if this is scoped
     */
    public boolean isScope() {
        assert (flags & KINDMASK) != IS_GLOBAL || (flags & IS_SCOPE) == IS_SCOPE : "global without scope flag";
        return (flags & IS_SCOPE) != 0;
    }

    /**
     * Check if this symbol is a function declaration
     *
     * @return true if a function declaration
     */
    public boolean isFunctionDeclaration() {
        return (flags & IS_FUNCTION_DECLARATION) != 0;
    }

    /**
     * Check if this symbol is a variable
     *
     * @return true if variable
     */
    public boolean isVar() {
        return (flags & KINDMASK) == IS_VAR;
    }

    /**
     * Check if this symbol is a global (undeclared) variable
     *
     * @return true if global
     */
    public boolean isGlobal() {
        return (flags & KINDMASK) == IS_GLOBAL;
    }

    /**
     * Check if this symbol is a function parameter
     *
     * @return true if parameter
     */
    public boolean isParam() {
        return (flags & KINDMASK) == IS_PARAM;
    }

    /**
     * Check if this is a program (script) level definition
     *
     * @return true if program level
     */
    public boolean isProgramLevel() {
        return (flags & IS_PROGRAM_LEVEL) != 0;
    }

    /**
     * Check if this symbol is a constant
     *
     * @return true if a constant
     */
    public boolean isConst() {
        return (flags & IS_CONST) != 0;
    }

    /**
     * Check if this is an internal symbol, without an explicit JavaScript source code equivalent
     *
     * @return true if internal
     */
    public boolean isInternal() {
        return (flags & IS_INTERNAL) != 0;
    }

    /**
     * Check if this symbol represents {@code this}
     *
     * @return true if this
     */
    public boolean isThis() {
        return (flags & IS_THIS) != 0;
    }

    /**
     * Check if this symbol is a let
     *
     * @return true if let
     */
    public boolean isLet() {
        return (flags & IS_LET) != 0;
    }

    /**
     * Flag this symbol as a function's self-referencing symbol.
     *
     * @return true if this symbol as a function's self-referencing symbol.
     */
    public boolean isFunctionSelf() {
        return (flags & IS_FUNCTION_SELF) != 0;
    }

    /**
     * Is this a block scoped symbol
     * @return true if block scoped
     */
    public boolean isBlockScoped() {
        return isLet() || isConst();
    }

    /**
     * Has this symbol been declared
     * @return true if declared
     */
    public boolean hasBeenDeclared() {
        return (flags & HAS_BEEN_DECLARED) != 0;
    }

    /**
     * Mark this symbol as declared
     */
    public void setHasBeenDeclared() {
        if (!hasBeenDeclared()) {
            flags |= HAS_BEEN_DECLARED;
        }
    }

    public boolean isVarDeclaredHere() {
        return (flags & IS_VAR_DECLARED_HERE) != 0;
    }

    public boolean isVarRedeclaredHere() {
        return (flags & IS_VAR_REDECLARED_HERE) != 0;
    }

    /**
     * Get the symbol flags
     *
     * @return flags
     */
    public int getFlags() {
        return flags;
    }

    /**
     * Get the name of this symbol
     *
     * @return symbol name
     */
    public String getName() {
        return name;
    }

    /**
     * Increase the symbol's use count by one.
     */
    public void increaseUseCount() {
        if (isScope()) { // Avoid dirtying a cache line; we only need the use count for scoped symbols
            useCount++;
        }
    }

    /**
     * Get the symbol's use count
     *
     * @return the number of times the symbol is used in code.
     */
    public int getUseCount() {
        return useCount;
    }

    /**
     * Flag this symbol as scope as described in {@link Symbol#isScope()}
     *
     * @return the symbol
     */
    public Symbol setIsScope() {
        if (!isScope()) {
            flags |= IS_SCOPE;
        }
        return this;
    }

    /**
     * Mark this symbol as a function declaration.
     */
    public void setIsFunctionDeclaration() {
        if (!isFunctionDeclaration()) {
            flags |= IS_FUNCTION_DECLARATION;
        }
    }

    /**
     * Set the symbol flags
     *
     * @param flags flags
     * @return the symbol
     */
    public Symbol setFlags(final int flags) {
        if (this.flags != flags) {
            this.flags = flags;
        }
        return this;
    }

    /**
     * Set a single symbol flag
     *
     * @param flag flag to set
     * @return the symbol
     */
    public Symbol setFlag(final int flag) {
        if ((this.flags & flag) == 0) {
            this.flags |= flag;
        }
        return this;
    }

    /**
     * Clears a single symbol flag
     *
     * @param flag flag to set
     * @return the symbol
     */
    public Symbol clearFlag(final int flag) {
        if ((this.flags & flag) != 0) {
            this.flags &= ~flag;
        }
        return this;
    }

    /**
     * Has this symbol been declared
     * @return true if declared
     */
    public boolean isDeclaredInSwitchBlock() {
        return (flags & IS_DECLARED_IN_SWITCH_BLOCK) != 0;
    }

    /**
     * Mark this symbol as declared
     */
    public void setDeclaredInSwitchBlock() {
        if (!isDeclaredInSwitchBlock()) {
            flags |= IS_DECLARED_IN_SWITCH_BLOCK;
        }
    }
}
