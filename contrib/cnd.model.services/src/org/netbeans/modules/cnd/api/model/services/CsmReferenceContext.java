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

package org.netbeans.modules.cnd.api.model.services;

import org.netbeans.cnd.api.lexer.CppTokenId;
import org.netbeans.modules.cnd.api.model.xref.CsmReference;

/**
 * Represents a reference with its lexical context.
 *
 */
public interface CsmReferenceContext {

    /**
     * The context size, i.e. depth of the reference
     * in lexical structure. Examples:<br />
     * <code>func(A)</code> -- depth of func is 1<br />
     * <code>func(A)</code> -- depth of A is 2<br />
     * <code>func([A])</code> -- depth of A is 3<br />
     * <code>func(A::B)</code> -- depth of B is 3<br />
     * <code>func(A, B)</code> -- depth of B is 2
     *
     * @return context size - depth of current reference in lexical structure
     */
    int size();

    /**
     * @return current reference
     */
    CsmReference getReference();

    /**
     * @param i
     * @return reference by index
     */
    CsmReference getReference(int i);

    /**
     * @return current token
     */
    CppTokenId getToken();

    /**
     * @param i
     * @return token by index
     */
    CppTokenId getToken(int i);

}
