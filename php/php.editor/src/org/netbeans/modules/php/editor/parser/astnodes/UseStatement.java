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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Represents a 'use' statement
 * <pre>e.g.
 * use MyNamespace;
 * use MyNamespace as MyAlias;
 * use MyProject\Sub\Level as MyAlias;
 * use \MyProject\Sub\Level as MyAlias;
 * use \MyProject\Sub\Level as MyAlias, MyNamespace as OtherAlias, MyOtherNamespace;
 * use some\namespace\{ClassA, sub\ClassB, ClassC as C};
 * </pre>
 */
public class UseStatement extends Statement {
    private final List<UseStatementPart> parts;
    private final Type type;

    public enum Type {
        TYPE("TYPE"), //NOI18N
        CONST("CONST"), //NOI18N
        FUNCTION("FUNCTION"); //NOI18N

        private final String type;

        private Type(String type) {
            this.type = type;
        }

        @Override
        public String toString() {
            return type;
        }

    };

    public UseStatement(int start, int end, List parts, Type type) {
        super(start, end);

        if (parts == null || parts.isEmpty() || type == null) {
            throw new IllegalArgumentException();
        }

        this.parts = new ArrayList<>(parts);
        this.type = type;
    }

    public UseStatement(int start, int end, List parts) {
        this(start, end, parts, Type.TYPE);
    }

    public UseStatement(int start, int end, SingleUseStatementPart[] parts, Type type) {
        this(start, end, Arrays.asList(parts), type);
    }

    public UseStatement(int start, int end, SingleUseStatementPart[] parts) {
        this(start, end, parts, Type.TYPE);
    }

    /**
     * Returns the list of parts of this 'use' statement.
     * @return list of this statement parts
     */
    public List<UseStatementPart> getParts() {
        return Collections.unmodifiableList(parts);
    }

    public Type getType() {
        return type;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (UseStatementPart useStatementPart : parts) {
            if (sb.length() > 0) {
                sb.append(", "); // NOI18N
            }
            sb.append(useStatementPart);
        }
        return "use " + type.toString() + " " + sb.toString(); //NOI18N
    }

}
