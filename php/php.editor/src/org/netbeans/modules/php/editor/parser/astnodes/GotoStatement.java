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
 * Holds a goto statement.
 * <pre>e.g.<pre>
 *goto START;
 */
public class GotoStatement extends Statement {

    private Identifier label;

    public GotoStatement(int start, int end, Identifier label) {
        super(start, end);

        if (label == null) {
            throw new IllegalArgumentException();
        }
        this.label = label;
    }

    /**
     * Returns the label of this goto label.
     *
     * @return the label label
     */
    public Identifier getLabel() {
        return this.label;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return "goto " + getLabel(); //NOI18N
    }

}
