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
import java.util.Iterator;
import java.util.List;

/**
 * Represents namespace name:
 * <pre>e.g.<pre>MyNamespace;
 *MyProject\Sub\Level;
 *namespace\MyProject\Sub\Level;
 */
public class NamespaceName extends Expression {

    protected List<Identifier> segments = new ArrayList<>();
    /** Whether the namespace name has '\' prefix, which means it relates to the global scope */
    private boolean global;
    /** Whether the namespace name has 'namespace' prefix, which means it relates to the current namespace scope */
    private boolean current;

    public NamespaceName(int start, int end, Identifier[] segments, boolean global, boolean current) {
        super(start, end);

        if (segments == null) {
            throw new IllegalArgumentException();
        }
        this.segments.addAll(Arrays.asList(segments));

        this.global = global;
        this.current = current;
    }

    public NamespaceName(int start, int end, List segments, boolean global, boolean current) {
        super(start, end);

        if (segments == null) {
            throw new IllegalArgumentException();
        }
        Iterator<Identifier> it = segments.iterator();
        while (it.hasNext()) {
            this.segments.add(it.next());
        }

        this.global = global;
        this.current = current;
    }

    /**
     * Returns whether this namespace name has global context (starts with '\')
     * @return
     */
    public boolean isGlobal() {
        return global;
    }

    /**
     * Returns whether this namespace name has current namespace context (starts with 'namespace')
     * @return
     */
    public boolean isCurrent() {
        return current;
    }

    /**
     * Retrieves names parts of the namespace
     * @return segments. If names list is empty, that means that this namespace is global.
     */
    public List<Identifier> getSegments() {
        return this.segments;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Identifier identifier : getSegments()) {
            sb.append(identifier).append("\\"); //NOI18N
        }
        return (isGlobal() ? "\\" : (isCurrent() ? "namespace " : "")) + sb.toString(); //NOI18N
    }

}
