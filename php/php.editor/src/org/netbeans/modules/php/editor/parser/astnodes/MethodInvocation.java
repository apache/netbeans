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
package org.netbeans.modules.php.editor.parser.astnodes;

/**
 * Represents a dispaching expression.
 *
 * e.g.
 * <pre>
 * foo()->bar(),
 * $myClass->foo()->bar(),
 * A::$a->foo(),
 * foo()?->bar() // NETBEANS-4443 PHP 8.0
 * </pre>
 */
public class MethodInvocation extends Dispatch {

    private final FunctionInvocation method;

    public MethodInvocation(int start, int end, VariableBase dispatcher, FunctionInvocation method, boolean isNullsafe) {
        super(start, end, dispatcher, isNullsafe);
        this.method = method;
    }

    /**
     * The method component of this method invocation expression.
     *
     * @return method component of this method invocation expression
     */
    public FunctionInvocation getMethod() {
        return method;
    }

    @Override
    public FunctionInvocation getMember() {
        return getMethod();
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }
}
