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

/**
 * Represents an expression error. If a whole statement is handled as ASTError,
 * we cannot scan valid nodes(e.g. parameters) even if there are them. So, some
 * features(e.g. code completion) may not work correctly.
 *
 * e.g.
 * <pre>
 * fn($x) =>;
 * fn($x) => $;
 * </pre>
 */
public class ASTErrorExpression extends Expression {

    public ASTErrorExpression(int start, int end) {
        super(start, end);
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

}
