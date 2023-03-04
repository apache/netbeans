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
import java.util.Collections;
import java.util.List;
import org.netbeans.modules.csl.api.OffsetRange;

/**
 * Represents a match expression.
 *
 * PHP 8.0:https://wiki.php.net/rfc/match_expression_v2
 *
 * e.g.
 * <pre>
 * match ($x) {
 *     1 => '1',
 *     2, 3 => '2, 3',
 *     foo() => 'foo()',
 *     default => getDefault(),
 * }
 * </pre>
 */
public class MatchExpression extends Expression {

    private final Expression expression;
    private final List<MatchArm> matchArms;
    private final OffsetRange blockRange;

    public MatchExpression(int start, int end, OffsetRange blockRange, Expression expression, List<MatchArm> matchArms) {
        super(start, end);
        assert blockRange != null;
        this.blockRange = blockRange;
        this.expression = expression;
        this.matchArms = new ArrayList<>(matchArms);
    }

    /**
     * Get a block range of match expression.
     *
     * <pre>
     * {condition => expression,}
     * </pre>
     *
     * @return a block range
     */
    public OffsetRange getBlockRange() {
        return blockRange;
    }

    /**
     * Get an expression.
     *
     * <pre>
     * match (expression) {};
     * </pre>
     *
     * @return Expression
     */
    public Expression getExpression() {
        return expression;
    }

    /**
     * Get MatchArms.
     *
     * <pre>
     * match (expression) {
     *     condition => expression, // match arm
     *     condition => expression, // match arm
     * }
     * </pre>
     *
     * @return MatchArms
     */
    public List<MatchArm> getMatchArms() {
        return Collections.unmodifiableList(matchArms);
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

}
