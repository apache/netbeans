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
package org.netbeans.modules.db.sql.analyzer;

import java.util.List;
import java.util.SortedMap;

/**
 *
 * @author Jiri Skrivanek
 */
public class CreateStatement extends SelectStatement {

    private int bodyStartOffset;
    private int bodyEndOffset;

    CreateStatement(int startOffset, int endOffset, SortedMap<Integer, Context> offset2Context, int bodyStartOffset, int bodyEndOffset, TablesClause tablesClause, List<SelectStatement> subqueries) {
        super(startOffset, endOffset, null, tablesClause, subqueries, offset2Context);
        this.bodyStartOffset = bodyStartOffset;
        this.bodyEndOffset = bodyEndOffset;
        this.kind = SQLStatementKind.CREATE;
    }

    /** Returns true if statement has some body after BEGIN. */
    public boolean hasBody() {
        return this.bodyEndOffset > this.bodyStartOffset;
    }

    /** Returns start offset of script after BEGIN. */
    public int getBodyStartOffset() {
        return this.bodyStartOffset;
    }

    /** Returns end offset of script after BEGIN. */
    public int getBodyEndOffset() {
        return this.bodyEndOffset;
    }
}
