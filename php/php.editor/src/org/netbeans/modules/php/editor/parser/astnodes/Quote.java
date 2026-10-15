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
import java.util.Collections;
import java.util.List;

/**
 * Represents complex qoute(i.e. qoute that includes string and variables).
 * Also represents heredoc
 * <pre>e.g.
 * "this is $a quote",
 * "'single ${$complex->quote()}'"
 * >>>Heredoc\n  This is here documents \nHeredoc;\n
 * </pre>
 * Note: "This is".$not." a quote node",
 *       'This is $not a quote too'
 */
public class Quote extends Expression {

    public enum Type {
        QUOTE,
        SINGLE,
        HEREDOC
    }

    private final ArrayList<Expression> expressions = new ArrayList<>();
    private Quote.Type quoteType;

    public Quote(int start, int end, Expression[] expressions, Quote.Type type) {
        super(start, end);
        this.expressions.addAll(Arrays.asList(expressions));
        this.quoteType = type;
    }

    public Quote(int start, int end, List<Exception> expressions, Quote.Type type) {
        this(start, end, expressions == null ? new Expression[0] : expressions.toArray(new Expression[0]), type);
    }

    /**
     * @return expression list of the echo statement
     */
    public List<Expression> getExpressions() {
        return Collections.unmodifiableList(this.expressions);
    }

    /**
     * The quote type
     * @return quote type
     */
    public Quote.Type getQuoteType() {
        return quoteType;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Expression expression : getExpressions()) {
            sb.append(expression).append(","); //NOI18N
        }
        return getQuoteType() + " " + sb.toString(); //NOI18N
    }

}
