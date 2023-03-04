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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.netbeans.api.annotations.common.NonNull;

/**
 * Represents an group element of the 'use' declaration.
 * <pre>
 * some\namespace\{ClassA, sub\ClassB, ClassC as C};
 * some\namespace\{ClassA, function sub\myfnc, const MY_CONST as C};
 * some\namespace\{ClassA,}; // PHP7.2
 * </pre>
 */
public class GroupUseStatementPart extends UseStatementPart {

    @NonNull
    private final NamespaceName baseNamespaceName;
    @NonNull
    private final List<SingleUseStatementPart> items;


    public GroupUseStatementPart(int start, int end, NamespaceName baseNamespaceName, List items) {
        super(start, end);
        this.baseNamespaceName = baseNamespaceName;
        this.items = new ArrayList<>(items);
    }

    @NonNull
    public NamespaceName getBaseNamespaceName() {
        return baseNamespaceName;
    }

    @NonNull
    public List<SingleUseStatementPart> getItems() {
        return Collections.unmodifiableList(items);
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(baseNamespaceName);
        sb.append('{'); // NOI18N
        boolean first = true;
        for (SingleUseStatementPart item : items) {
            if (first) {
                first = false;
            } else {
                sb.append(", "); // NOI18N
            }
            sb.append(item);
        }
        sb.append('}'); // NOI18N
        return sb.toString();
    }



}
