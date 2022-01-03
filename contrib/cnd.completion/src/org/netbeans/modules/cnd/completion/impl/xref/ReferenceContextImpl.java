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
package org.netbeans.modules.cnd.completion.impl.xref;

import java.util.ArrayList;
import java.util.List;
import org.netbeans.cnd.api.lexer.CppTokenId;
import org.netbeans.modules.cnd.api.model.services.CsmReferenceContext;
import org.netbeans.modules.cnd.api.model.xref.CsmReference;

/**
 * Implementation of <code>CsmReferenceContext</code>.
 *
 *
 */
public class ReferenceContextImpl implements CsmReferenceContext {

    private final CsmReferenceContext parent;
    private final int parentSize; // cached value of parent.size()
    private final List stack;
    private int popCount;

    public ReferenceContextImpl() {
        this(null, false);
    }

    public ReferenceContextImpl(CsmReferenceContext parent) {
        this(parent, false);
    }

    @SuppressWarnings("unchecked")
    public ReferenceContextImpl(CsmReferenceContext parent, boolean fullcopy) {
        if (fullcopy && parent != null) {
            this.parent = null;
            this.parentSize = 0;
            this.stack = new ArrayList();
            for (int i = 0; i < parent.size(); ++i) {
                stack.add(parent.getToken(i));
                stack.add(parent.getReference(i));
            }
        } else {
            this.parent = parent;
            this.parentSize = parent == null ? 0 : parent.size();
            this.stack = new ArrayList();
        }
    }

    @SuppressWarnings("unchecked")
    public ReferenceContextImpl(ReferenceContextImpl c) {
        this.parentSize = c.parentSize;
        if(c.parent != null) {
            this.parent = new ReferenceContextImpl(c.parent);
        } else {
            parent =  null;
        }
        this.stack = new ArrayList(c.stack);
        this.popCount = c.popCount;
    }

    @Override
    public int size() {
        return parentSize - popCount + stack.size() / 2;
    }

    @Override
    public CsmReference getReference() {
        return getReference(size() - 1);
    }

    @Override
    public CsmReference getReference(int i) {
        if (0 <= i && i < parentSize - popCount) {
            return parent.getReference(i);
        } else {
            return (CsmReference) stack.get(2 * (i - parentSize + popCount) + 1);
        }
    }

    @Override
    public CppTokenId getToken() {
        return getToken(size() - 1);
    }

    @Override
    public CppTokenId getToken(int i) {
        if (0 <= i && i < parentSize - popCount) {
            return parent.getToken(i);
        } else {
            return (CppTokenId) stack.get(2 * (i - parentSize + popCount));
        }
    }

    /*package*/
    @SuppressWarnings("unchecked")
    void push(CppTokenId token, CsmReference ref) {
        stack.add(token);
        stack.add(ref);
    }

    /*package*/ void pop() {
        if (stack.isEmpty()) {
            if (popCount < parentSize) {
                ++popCount;
            } else {
                throw new IllegalStateException("Stack underflow"); // NOI18N
            }
        } else {
            stack.remove(stack.size() - 1);
            stack.remove(stack.size() - 1);
        }
    }

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        for (int i = 0; i < size(); ++i) {
            if (0 < i) {
                buf.append(' '); // NOI18N
            }
            buf.append('('); // NOI18N
            buf.append(getToken(i));
            buf.append(','); // NOI18N
            CsmReference ref = getReference(i);
            buf.append(ref == null ? null : ref.getText());
            buf.append(')'); // NOI18N
        }
        return buf.toString();
    }
}
