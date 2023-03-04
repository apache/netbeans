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

import java.util.List;

/**
 *
 * @author Ondrej Brejla <obrejla@netbeans.org>
 */
public class TraitConflictResolutionDeclaration extends Statement {
    private final Expression preferredTraitName;
    private final Identifier methodName;
    private final List<Expression> suppressedTraitNames;

    public TraitConflictResolutionDeclaration(final int start, final int end, final Expression preferredTraitName, final Identifier methodName, final List<Expression> suppressedTraitNames) {
        super(start, end);
        this.preferredTraitName = preferredTraitName;
        this.methodName = methodName;
        this.suppressedTraitNames = suppressedTraitNames;
    }

    public Expression getPreferredTraitName() {
        return preferredTraitName;
    }

    public Identifier getMethodName() {
        return methodName;
    }

    public List<Expression> getSuppressedTraitNames() {
        return suppressedTraitNames;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Expression expression : getSuppressedTraitNames()) {
            sb.append(expression).append(","); //NOI18N
        }
        return getPreferredTraitName() + "::" + getMethodName() + " insteadof " + sb.toString(); //NOI18N
    }

}
