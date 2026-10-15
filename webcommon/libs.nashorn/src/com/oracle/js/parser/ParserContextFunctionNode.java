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

import java.util.HashSet;
import java.util.List;

import com.oracle.js.parser.ir.FunctionNode;
import com.oracle.js.parser.ir.IdentNode;
import com.oracle.js.parser.ir.Module;

/**
 * ParserContextNode that represents a function that is currently being parsed
 */
class ParserContextFunctionNode extends ParserContextBaseNode {

    /** Function name */
    private final String name;

    /** Function identifier node */
    private final IdentNode ident;

    /** Name space for function */
    private final Namespace namespace;

    /** Line number for function declaration */
    private final int line;

    /**
     * Function node kind, see FunctionNode.Kind
     */
    private final FunctionNode.Kind kind;

    /** List of parameter identifiers for function */
    private List<IdentNode> parameters;

    /** Token for function start */
    private final long token;

    /** Last function token */
    private long lastToken;

    /** Opaque node for parser end state, see {@link Parser} */
    private Object endParserState;

    private HashSet<String> parameterBoundNames;
    private IdentNode duplicateParameterBinding;
    private boolean simpleParameterList = true;

    private Module module;

    /**
     * @param token The token for the function
     * @param ident External function name
     * @param name Internal name of the function
     * @param namespace Function's namespace
     * @param line The source line of the function
     * @param kind Function kind
     * @param parameters The parameters of the function
     */
    ParserContextFunctionNode(final long token, final IdentNode ident, final String name, final Namespace namespace, final int line, final FunctionNode.Kind kind,
                    final List<IdentNode> parameters) {
        this.ident = ident;
        this.namespace = namespace;
        this.line = line;
        this.kind = kind;
        this.name = name;
        this.parameters = parameters;
        this.token = token;
    }

    /**
     * @return Internal name of the function
     */
    public String getName() {
        return name;
    }

    /**
     * @return The external identifier for the function
     */
    public IdentNode getIdent() {
        return ident;
    }

    /**
     *
     * @return true if function is the program function
     */
    public boolean isProgram() {
        return getFlag(FunctionNode.IS_PROGRAM) != 0;
    }

    /**
     * @return if function in strict mode
     */
    public boolean isStrict() {
        return getFlag(FunctionNode.IS_STRICT) != 0;
    }

    /**
     * @return true if the function has nested evals
     */
    public boolean hasNestedEval() {
        return getFlag(FunctionNode.HAS_NESTED_EVAL) != 0;
    }

    /**
     * Returns true if any of the blocks in this function create their own scope.
     *
     * @return true if any of the blocks in this function create their own scope.
     */
    public boolean hasScopeBlock() {
        return getFlag(FunctionNode.HAS_SCOPE_BLOCK) != 0;
    }

    /**
     * Create a unique name in the namespace of this FunctionNode
     *
     * @param base prefix for name
     * @return base if no collision exists, otherwise a name prefix with base
     */
    public String uniqueName(final String base) {
        return namespace.uniqueName(base);
    }

    /**
     * @return line number of the function
     */
    public int getLineNumber() {
        return line;
    }

    /**
     * @return The kind if function
     */
    public FunctionNode.Kind getKind() {
        return kind;
    }

    /**
     * Get parameters
     *
     * @return The parameters of the function
     */
    public List<IdentNode> getParameters() {
        return parameters;
    }

    void setParameters(List<IdentNode> parameters) {
        this.parameters = parameters;
    }

    /**
     * Set last token
     *
     * @param token New last token
     */
    public void setLastToken(final long token) {
        this.lastToken = token;

    }

    /**
     * @return lastToken Function's last token
     */
    public long getLastToken() {
        return lastToken;
    }

    /**
     * Returns the ParserState of when the parsing of this function was ended
     *
     * @return endParserState The end parser state
     */
    public Object getEndParserState() {
        return endParserState;
    }

    /**
     * Sets the ParserState of when the parsing of this function was ended
     *
     * @param endParserState The end parser state
     */
    public void setEndParserState(final Object endParserState) {
        this.endParserState = endParserState;
    }

    /**
     * Returns the if of this function
     *
     * @return The function id
     */
    public int getId() {
        return isProgram() ? -1 : Token.descPosition(token);
    }

    public boolean isMethod() {
        return getFlag(FunctionNode.IS_METHOD) != 0;
    }

    public boolean isClassConstructor() {
        return getFlag(FunctionNode.IS_CLASS_CONSTRUCTOR) != 0;
    }

    public boolean isSubclassConstructor() {
        return getFlag(FunctionNode.IS_SUBCLASS_CONSTRUCTOR) != 0;
    }

    boolean addParameterBinding(IdentNode bindingIdentifier) {
        if (Parser.isArguments(bindingIdentifier)) {
            setFlag(FunctionNode.DEFINES_ARGUMENTS);
        }

        if (parameterBoundNames == null) {
            parameterBoundNames = new HashSet<>();
        }
        if (parameterBoundNames.add(bindingIdentifier.getName())) {
            return true;
        } else {
            duplicateParameterBinding = bindingIdentifier;
            return false;
        }
    }

    public IdentNode getDuplicateParameterBinding() {
        return duplicateParameterBinding;
    }

    public boolean isSimpleParameterList() {
        return simpleParameterList;
    }

    public void setSimpleParameterList(boolean simpleParameterList) {
        this.simpleParameterList = simpleParameterList;
    }

    public Module getModule() {
        return module;
    }

    public void setModule(Module module) {
        this.module = module;
    }

    public boolean isAsync() {
        return getFlag(FunctionNode.IS_ASYNC) != 0;
    }
}
