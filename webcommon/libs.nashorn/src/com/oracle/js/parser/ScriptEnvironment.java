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

package com.oracle.js.parser;

import java.io.PrintWriter;

/**
 * Parser environment consists of command line options, and output and error writers, etc.
 */
public final class ScriptEnvironment {
    /** Error writer for this environment */
    private final PrintWriter err;

    /** Top level namespace. */
    private final Namespace namespace;

    /** Accept "const" keyword and treat it as variable. Interim feature */
    final boolean constAsVar;

    /** Display stack trace upon error, default is false */
    final boolean dumpOnError;

    /** Invalid lvalue expressions should be reported as early errors */
    final boolean earlyLvalueError;

    /** Empty statements should be preserved in the AST */
    final boolean emptyStatements;

    /** ecmascriptEdition to support */
    final int ecmascriptEdition;

    /** Enable JSX extension. */
    final boolean jsx;

    /**
     * Behavior when encountering a function declaration in a lexical context where only statements
     * are acceptable (function declarations are source elements, but not statements).
     */
    public enum FunctionStatementBehavior {
        /**
         * Accept the function declaration silently and treat it as if it were a function expression
         * assigned to a local variable.
         */
        ACCEPT,
        /**
         * Log a parser warning, but accept the function declaration and treat it as if it were a
         * function expression assigned to a local variable.
         */
        WARNING,
        /**
         * Raise a {@code SyntaxError}.
         */
        ERROR
    }

    /**
     * Behavior when encountering a function declaration in a lexical context where only statements
     * are acceptable (function declarations are source elements, but not statements).
     */
    final FunctionStatementBehavior functionStatement;

    /** Do not support non-standard syntax extensions. */
    final boolean syntaxExtensions;

    /** is this environment in scripting mode? */
    final boolean scripting;

    /** does the environment support shebang? */
    final boolean shebang;

    /** is this environment in strict mode? */
    final boolean strict;

    final boolean functionDeclarationHoisting;

    private ScriptEnvironment(
            boolean strict, int ecmascriptEdition, boolean jsx,
            boolean earlyLvalueError, boolean emptyStatements,
            boolean syntaxExtensions, boolean scripting, boolean shebang,
            boolean constAsVar, boolean functionDeclarationHoisting,
            FunctionStatementBehavior functionStatementBehavior,
            PrintWriter dumpOnError) {
        this.namespace = new Namespace();
        this.err = dumpOnError;

        this.constAsVar = constAsVar;
        this.dumpOnError = dumpOnError != null;
        this.earlyLvalueError = earlyLvalueError;
        this.emptyStatements = emptyStatements;
        this.functionStatement = functionStatementBehavior;
        this.syntaxExtensions = syntaxExtensions;
        this.strict = strict;
        this.scripting = scripting;
        this.shebang = shebang;
        this.ecmascriptEdition = ecmascriptEdition;
        this.jsx = jsx;
        this.functionDeclarationHoisting = functionDeclarationHoisting;
    }

    /**
     * Get the error stream for this environment
     *
     * @return error print writer
     */
    PrintWriter getErr() {
        return err;
    }

    /**
     * Get the namespace for this environment
     *
     * @return namespace
     */
    Namespace getNamespace() {
        return namespace;
    }

    public boolean isStrict() {
        return strict;
    }

    public static Builder builder() {
        return new Builder();
    }

    @SuppressWarnings("hiding")
    public static final class Builder {
        private boolean constAsVar;
        private boolean earlyLvalueError = true;
        private boolean emptyStatements;
        private int ecmacriptEdition = 6;
        private boolean jsx = false;
        private boolean syntaxExtensions = true;
        private boolean scripting;
        private boolean shebang;
        private boolean strict;
        private boolean functionDeclarationHoisting;
        private FunctionStatementBehavior functionStatementBehavior = FunctionStatementBehavior.ERROR;
        private PrintWriter dumpOnError;

        private Builder() {
        }

        public Builder constAsVar(boolean constAsVar) {
            this.constAsVar = constAsVar;
            return this;
        }

        public Builder earlyLvalueError(boolean earlyLvalueError) {
            this.earlyLvalueError = earlyLvalueError;
            return this;
        }

        public Builder emptyStatements(boolean emptyStatements) {
            this.emptyStatements = emptyStatements;
            return this;
        }

        public Builder ecmacriptEdition(int ecmacriptEdition) {
            this.ecmacriptEdition = ecmacriptEdition;
            return this;
        }

        public Builder jsx(boolean jsx) {
            this.jsx = jsx;
            return this;
        }

        public Builder syntaxExtensions(boolean syntaxExtensions) {
            this.syntaxExtensions = syntaxExtensions;
            return this;
        }

        public Builder scripting(boolean scripting) {
            this.scripting = scripting;
            return this;
        }

        public Builder shebang(boolean shebang) {
            this.shebang = shebang;
            return this;
        }

        public Builder strict(boolean strict) {
            this.strict = strict;
            return this;
        }

        public Builder functionStatementBehavior(FunctionStatementBehavior functionStatementBehavior) {
            this.functionStatementBehavior = functionStatementBehavior;
            return this;
        }

        public Builder dumpOnError(PrintWriter dumpOnError) {
            this.dumpOnError = dumpOnError;
            return this;
        }

        public Builder functionDeclarationHoisting(boolean functionDeclarationHoisting) {
            this.functionDeclarationHoisting = functionDeclarationHoisting;
            return this;
        }

        public ScriptEnvironment build() {
            return new ScriptEnvironment(strict, ecmacriptEdition, jsx,
                    earlyLvalueError, emptyStatements, syntaxExtensions,
                    scripting, shebang, constAsVar, functionDeclarationHoisting,
                    functionStatementBehavior, dumpOnError);
        }
    }
}
