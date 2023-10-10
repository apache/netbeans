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
package org.netbeans.modules.html.knockout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Petr Hejl
 */
public class KODataBindContext {

    private static final Logger LOGGER = Logger.getLogger(KODataBindContext.class.getName());

    private final KODataBindContext original;

    private final List<ParentContext> parents;

    private String data;

    private boolean inForEach;

    private String alias;

    public KODataBindContext() {
        this.original = null;
        this.parents = new ArrayList<>();
    }

    public KODataBindContext(KODataBindContext context) {
        this.original = context;
        this.parents = new ArrayList<>(context.parents);
        this.data = context.data;
        this.inForEach = context.inForEach;
        this.alias = context.alias;
    }

    // XXX we are intetionaly ignoring root of the child
    public static KODataBindContext combine(KODataBindContext parent, KODataBindContext child) {
        KODataBindContext result = new KODataBindContext(parent);
        for (int i = 1; i < child.parents.size(); i++) {
            ParentContext c = child.parents.get(i);
            result.push(c.getValue(), c.isInForEach(), c.getAlias(), true);
        }
        result.push(child.getData(), child.isInForEach(), child.getAlias(), true);
        return result;
    }

    public void push(String newData, boolean foreach, String alias) {
        push(newData, foreach, alias, false);
    }

    private void push(String newData, boolean foreach, String alias, boolean noExpansion) {
        assert !foreach || newData != null;
        assert alias == null || foreach;

        String replacement = (data == null || data.equals("$root")) ? "ko.$bindings" : data; // NOI18N
        String toAdd = newData.replace("$data", replacement); // NOI18N

        if (!noExpansion && foreach) {
            toAdd = "(" + toAdd + ")[0]"; // NOI18N
        }
        if (data == null || "$root".equals(data)) { // NOI18N
            parents.add(new ParentContext("ko.$bindings", false, null)); // NOI18N
        } else {
            parents.add(new ParentContext(this.data, this.inForEach, this.alias));
        }
        this.data = toAdd;
        this.inForEach = foreach;
        this.alias = alias;
    }

    public void pop() {
        if (parents.isEmpty()) {
            LOGGER.log(Level.WARNING, "Invalid state detected. Please check the HTML document validity."); //NOI18N
            return;
        }
        ParentContext context = parents.remove(parents.size() - 1);
        data = context.getValue();
        inForEach = context.isInForEach();
        alias = context.getAlias();
    }

    public KODataBindContext getOriginal() {
        return original;
    }

    public void clear() {
        inForEach = false;
        data = null;
        alias = null;
        parents.clear();
    }

    public List<ParentContext> getParents() {
        return Collections.unmodifiableList(parents);
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public boolean isInForEach() {
        return inForEach;
    }

    public String getAlias() {
        return alias;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 53 * hash + Objects.hashCode(this.original);
        hash = 53 * hash + Objects.hashCode(this.parents);
        hash = 53 * hash + Objects.hashCode(this.data);
        hash = 53 * hash + (this.inForEach ? 1 : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final KODataBindContext other = (KODataBindContext) obj;
        if (!Objects.equals(this.original, other.original)) {
            return false;
        }
        if (!Objects.equals(this.parents, other.parents)) {
            return false;
        }
        if (!Objects.equals(this.data, other.data)) {
            return false;
        }
        if (this.inForEach != other.inForEach) {
            return false;
        }
        if (!Objects.deepEquals(this.parents.toArray(), other.parents.toArray())) {
            return false;
        }
        return true;
    }

    public static class ParentContext {

        private final String value;

        private final boolean inForEach;

        private final String alias;

        public ParentContext(String value, boolean inForEach, String alias) {
            this.value = value;
            this.inForEach = inForEach;
            this.alias = alias;
        }

        public String getValue() {
            return value;
        }

        public boolean isInForEach() {
            return inForEach;
        }

        public String getAlias() {
            return alias;
        }
    }
}
