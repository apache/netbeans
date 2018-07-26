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
package org.netbeans.modules.php.editor.parser.astnodes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NullAllowed;

/**
 * Represents a function declaration
 * <pre>e.g.
 * function foo() {}
 *
 * function &foo() {}
 *
 * function foo($a, int $b, $c = 5, int $d = 6) {}
 *
 * function foo($a, int $b, $c = 5, int $d = 6): string {}
 *
 * function foo(); -abstract function in class declaration
 * </pre>
 */
public class FunctionDeclaration extends Statement {

    private final boolean isReference;
    private final Identifier name;
    private final ArrayList<FormalParameter> formalParameters = new ArrayList<>();
    @NullAllowed
    private final Expression returnType;
    private final Block body;


    private FunctionDeclaration(int start, int end, Identifier functionName, FormalParameter[] formalParameters, Expression returnType, Block body, boolean isReference) {
        super(start, end);
        this.isReference = isReference;
        this.name = functionName;
        this.formalParameters.addAll(Arrays.asList(formalParameters));
        this.returnType = returnType;
        this.body = body;
    }

    public FunctionDeclaration(int start, int end, Identifier functionName, List<FormalParameter> formalParameters, Expression returnType, Block body, boolean isReference) {
        this(start, end, functionName, (FormalParameter[]) formalParameters.toArray(new FormalParameter[formalParameters.size()]), returnType, body, isReference);
    }

    /**
     * Body of this function declaration
     *
     * @return Body of this function declaration
     */
    public Block getBody() {
        return body;
    }

    /**
     * List of the formal parameters of this function declaration
     *
     * @return the parameters of this declaration
     */
    public List<FormalParameter> getFormalParameters() {
        return this.formalParameters;
    }

    /**
     * Function name of this declaration
     *
     * @return Function name of this declaration
     */
    public Identifier getFunctionName() {
        return name;
    }

    /**
     * Return type of this function declaration, can be {@code null}.
     *
     * @return return type of this function declaration, can be {@code null}
     */
    @CheckForNull
    public Expression getReturnType() {
        return returnType;
    }

    /**
     * True if this function's return variable will be referenced
     * @return True if this function's return variable will be referenced
     */
    public boolean isReference() {
        return isReference;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (FormalParameter formalParameter : getFormalParameters()) {
            sb.append(formalParameter).append(","); //NOI18N
        }
        return "function " + (isReference() ? "&" : "") + getFunctionName() + "(" + sb.toString() + ")" // NOI18N
                + (getReturnType() != null ? ": " + getReturnType() : "") + getBody(); // NOI18N
    }

}
