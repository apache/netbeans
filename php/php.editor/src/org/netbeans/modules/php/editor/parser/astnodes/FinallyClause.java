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
 * Represents a finally clause (as part of a try statement)
 * <pre>e.g.<pre> finally { body; },
 *
 * @author Ondrej Brejla <obrejla@netbeans.org>
 */
public class FinallyClause extends Statement {
    private final Block body;

    public FinallyClause(int start, int end, Block body) {
        super(start, end);

        assert body != null;
        this.body = body;
    }

    /**
     * Returns the body of this finally clause.
     *
     * @return the finally clause body
     */
    public Block getBody() {
        return body;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return "finally " + getBody(); //NOI18N
    }

}
