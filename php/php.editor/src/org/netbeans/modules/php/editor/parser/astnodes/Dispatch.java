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
 * Represents a base class for method invocation and field access.
 *
 * e.g.
 * <pre>
 * $a->$b,
 * foo()->bar(),
 * $myClass->foo()->bar(),
 * A::$a->foo(),
 * $a?->$b // NETBEANS-4443 PHP 8.0
 * </pre>
 */
public abstract class Dispatch extends VariableBase {

    private final VariableBase dispatcher;
    private final boolean isNullsafe; // NETBEANS-4443 PHP 8.0 Support

    public Dispatch(int start, int end, VariableBase dispatcher) {
        this(start, end, dispatcher, false);
    }

    public Dispatch(int start, int end, VariableBase dispatcher, boolean isNullsafe) {
        super(start, end);
        this.dispatcher = dispatcher;
        this.isNullsafe = isNullsafe;
    }

    /**
     * The dispatcher component of this dispatch expression.
     *
     * @return dispatcher component of this dispatch expression
     */
    public VariableBase getDispatcher() {
        return dispatcher;
    }

    /**
     * Check whether the access operator is the nullsafe.
     *
     * @return {@code true} if the operator is the nullsafe, {@code false}
     * otherwise
     */
    public boolean isNullsafe() {
        return isNullsafe;
    }

    /**
     * @return the property of the dispatch
     */
    public abstract VariableBase getMember();

    @Override
    public String toString() {
        return getDispatcher() + (isNullsafe ? "?->" : "->") + getMember(); //NOI18N
    }

}
