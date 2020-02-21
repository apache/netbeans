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

package org.netbeans.cnd.api.lexer;

import org.netbeans.api.lexer.Token;

/**
 *
 */
public abstract class CndAbstractTokenProcessor<T extends Token> implements CndTokenProcessor<T> {
    public void start(int startOffset, int firstTokenOffset, int endOffset) {}

    public void end(int offset, int lastTokenOffset) {}

    public boolean isStopped() {
        return false;
    }
    /**
     *
     * @param token
     * @param tokenOffset
     * @return true if token processor is interested in getting embedding of input token as well
     */
    public abstract boolean token(T token, int tokenOffset);
}
