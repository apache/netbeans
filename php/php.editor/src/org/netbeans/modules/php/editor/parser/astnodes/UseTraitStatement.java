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
public class UseTraitStatement extends Statement {
    private final Block body;
    private final List<UseTraitStatementPart> parts;

    public UseTraitStatement(int start, int end, List<UseTraitStatementPart> parts, final Block body) {
        super(start, end);
        if (parts == null || parts.isEmpty()) {
            throw new IllegalArgumentException();
        }
        this.body = body;
        this.parts = parts;
    }

    public Block getBody() {
        return body;
    }

    public List<UseTraitStatementPart> getParts() {
        return parts;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (UseTraitStatementPart useTraitStatementPart : parts) {
            if (sb.length() > 0) {
                sb.append(","); // NOI18N
            }
            sb.append(useTraitStatementPart);
        }
        if (body != null) {
            sb.append(body);
        }
        return "use " + sb.toString(); //NOI18N
    }

}
