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
 * Represents a list expression. The list contains key => value list, variables
 * and/or other lists.<br>
 * <b>Note:</b>Can be contained ArrayCreation instead of ListVariable when list
 * is nested with new syntax. Because it is the same syntax pattern as short
 * array syntax.
 *
 * <pre>e.g.
 * list($a,$b) = array (1,2),
 * list($a, list($b, $c)),
 * list("id" => $id, "name" => $name) = $data[0]; // PHP7.1,
 * list($a, &$b) = $array; // PHP 7.3
 * [$a, $b, $c] = [1, 2, 3]; // PHP7.1,
 * ["a" => $a, "b" => $b, "c" => $c] = ["a" => 1, "b" => 2, "c" => 3]; // PHP7.1
 * [$a, &$b] = $array; // PHP 7.3
 * </pre>
 */
public class ListVariable extends VariableBase {

    public enum SyntaxType {
        OLD {
            @Override
            String toString(String innerElements) {
                return "list(" + innerElements + ")"; // NOI18N
            }
        },
        NEW {
            @Override
            String toString(String innerElements) {
                return "[" + innerElements + "]"; // NOI18N
            }
        };

        abstract String toString(String innerElements);
    }

    private final List<ArrayElement> elements = new ArrayList<>();
    private final SyntaxType syntaxType;

    private ListVariable(int start, int end, ArrayElement[] elements, SyntaxType syntaxType) {
        super(start, end);

        if (elements == null) {
            throw new IllegalArgumentException();
        }
        this.elements.addAll(Arrays.asList(elements));
        this.syntaxType = syntaxType;
    }

    public ListVariable(int start, int end, List<ArrayElement> elements, SyntaxType syntaxType) {
        this(start, end, elements == null ? null : elements.toArray(new ArrayElement[0]), syntaxType);
    }

    /**
     * @return the list of elements
     */
    public List<ArrayElement> getElements() {
        return Collections.unmodifiableList(elements);
    }

    public SyntaxType getSyntaxType() {
        return syntaxType;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        getElements().forEach((element) -> {
            sb.append(element).append(","); //NOI18N
        });
        return syntaxType.toString(sb.toString());
    }

}
