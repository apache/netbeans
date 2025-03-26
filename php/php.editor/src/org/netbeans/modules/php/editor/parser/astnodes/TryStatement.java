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

/**
 * Represents the try statement
 * <pre>e.g.<pre>
 * try {
 *   statements...
 * } catch (Exception $e) {
 *   statements...
 * } catch (AnotherException $ae) {
 *   statements...
 * }
 */
public class TryStatement extends Statement {

    private Block tryStatement;
    private ArrayList<CatchClause> catchClauses = new ArrayList<>();
    private final FinallyClause finallyClause;

    private TryStatement(int start, int end, Block tryStatement, CatchClause[] catchClauses, FinallyClause finallyClause) {
        super(start, end);
        if (tryStatement == null) {
            throw new IllegalArgumentException();
        }
        this.tryStatement = tryStatement;
        if (catchClauses != null) {
            this.catchClauses.addAll(Arrays.asList(catchClauses));
        }
        this.finallyClause = finallyClause;
    }

    public TryStatement(int start, int end, Block tryStatement, List<CatchClause> catchClauses, FinallyClause finallyClause) {
        this(start, end, tryStatement, catchClauses == null ? null : catchClauses.toArray(new CatchClause[0]), finallyClause);
    }

    /**
     * Returns the body of this try statement.
     *
     * @return the try body
     */
    public Block getBody() {
        return this.tryStatement;
    }

    /**
     * Returns the live ordered list of catch clauses for this try statement.
     *
     * @return the live list of catch clauses
     *    (element type: <code>CatchClause</code>)
     */
    public List<CatchClause> getCatchClauses() {
        return this.catchClauses;
    }

    public FinallyClause getFinallyClause() {
        return finallyClause;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (CatchClause catchClause : getCatchClauses()) {
            sb.append(catchClause);
        }
        return "try" + getBody() + sb.toString(); //NOI18N
    }

}
