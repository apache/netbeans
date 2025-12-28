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
 * Represents array creation
 * <pre>e.g.<pre> array(1,2,3,),
 * array('Dodo'=>'Golo','Dafna'=>'Dodidu')
 * array($a, $b=>foo(), 1=>$myClass->getFirst())
 */
public class ArrayCreation extends Expression {
    public enum Type {
        OLD {
            @Override
            String toString(String innerElements) {
                return "array(" + innerElements + ")"; //NOI18N
            }
        },
        NEW {
            @Override
            String toString(String innerElements) {
                return "[" + innerElements + "]"; //NOI18N
            }
        };

        abstract String toString(String innerElements);
    }
    private final Type type;
    private final ArrayList<ArrayElement> elements = new ArrayList<>();

    private ArrayCreation(int start, int end, ArrayElement[] elements, Type type) {
        super(start, end);

        if (elements == null) {
            throw new IllegalArgumentException();
        }
        this.elements.addAll(Arrays.asList(elements));
        this.type = type;
    }

    public ArrayCreation(int start, int end, List<ArrayElement> elements, Type type) {
        this(start, end, elements == null ? null : elements.toArray(new ArrayElement[0]), type);
    }

    /**
     * Retrieves elements parts of array creation
     * @return elements
     */
    public List<ArrayElement> getElements() {
        return this.elements;
    }

    public Type getType() {
        return type;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (ArrayElement arrayElement : getElements()) {
            sb.append(arrayElement).append(","); //NOI18N
        }
        return type.toString(sb.toString());
    }

}
