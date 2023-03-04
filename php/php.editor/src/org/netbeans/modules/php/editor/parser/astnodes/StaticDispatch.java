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
 * base class for all the static access
 */
public abstract class StaticDispatch extends VariableBase {

    private final Expression dispatcher;


    public StaticDispatch(int start, int end, Expression dispatcher) {
        super(start, end);
        assert dispatcher != null;
        this.dispatcher = dispatcher;
    }

    /**
     * The dispatcher component of this dispatch expression.
     *
     * @return dispatcher component of this dispatch expression
     */
    public Expression getDispatcher() {
        return dispatcher;
    }

    public abstract ASTNode getMember();

    @Override
    public String toString() {
        return getDispatcher() + "::" + getMember(); //NOI18N
    }

}
