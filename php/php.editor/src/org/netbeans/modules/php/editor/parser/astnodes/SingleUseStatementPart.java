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

import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;

/**
 * Represents a single element of the 'use' declaration.
 * <pre>e.g.
 * MyNamespace;
 * MyNamespace as MyAlias;
 * MyProject\Sub\Level as MyAlias;
 * \MyProject\Sub\Level as MyAlias;
 * myfnc, // part of group use
 * function myfnc, // part of group use
 * </pre>
 */
public class SingleUseStatementPart extends UseStatementPart {

    @NonNull
    private final NamespaceName name;
    @NullAllowed
    private final Identifier alias;
    @NullAllowed
    private final UseStatement.Type type;


    public SingleUseStatementPart(int start, int end, @NonNull NamespaceName name, @NullAllowed Identifier alias) {
        this(start, end, null, name, alias);
    }

    public SingleUseStatementPart(int start, int end, UseStatement.Type type, @NonNull NamespaceName name, @NullAllowed Identifier alias) {
        super(start, end);
        if (name == null) {
            throw new IllegalArgumentException();
        }
        this.name = name;
        this.alias = alias;
        this.type = type;
    }

    @CheckForNull
    public UseStatement.Type getType() {
        return type;
    }

    /**
     * Returns the name of this element.
     * @return the name of the element
     */
    @NonNull
    public NamespaceName getName() {
        return name;
    }

    /**
     * Returns the alias expression of this element.
     * @return the alias expression of this element, can be {@code null}
     */
    @CheckForNull
    public Identifier getAlias() {
        return this.alias;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return (type != null ? type + " " : "") // NOI18N
                + getName()
                + (getAlias() == null ? "" : " as " + getAlias()); // NOI18N
    }

}
