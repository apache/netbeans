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
 * Represents a string in the PHP Doc. Usually it used for referring type or
 * variable in tags llike @param, @return @throws, @see etc.
 * <pre>@return int|string</pre>
 * @author Petr Pisl
 */
public class PHPDocNode extends ASTNode {

    private final String value;

    public PHPDocNode(int start, int end, String value) {
        super(start, end);
        this.value = value;
    }

    /**
     *
     * @return a part of the PHP Doc, which is represented by this node
     */
    public String getValue() {
        return value;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }


}
