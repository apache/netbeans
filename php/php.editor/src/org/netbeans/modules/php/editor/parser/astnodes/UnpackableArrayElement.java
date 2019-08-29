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
 * Represents a single element of array with spread operator(...).
 *
 * PHP 7.4: Spread Operator in Array Expression
 * <pre>
 * e.g.
 * $array1 = [1, 2, 3];
 * $array2 = [0, ...$array1]; // [0, 1, 2, 3]
 * $array3 = array(...$array1, ...$array2, 4); // [1, 2, 3, 0, 1, 2, 3, 4]
 * $array4 = [...myFunc()];
 * $array5 = [...new ArrayIterator(['x', 'y', 'z'])];
 * const CONSTANT = [0, 1, 2, 3];
 * const CONSTANT1 = [...CONSTANT];
 * class Unpack {
 *     public const CONSTANT1 = [1, 2, 3];
 *     public const CONSTANT2 = [...self::CONSTANT1];
 * }
 * </pre>
 *
 * @see https://wiki.php.net/rfc/spread_operator_for_array
 */
public class UnpackableArrayElement extends ArrayElement {

    public UnpackableArrayElement(int start, int end, Expression value) {
        super(start, end, value);
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return "..." + getValue(); // NOI18N
    }

}
