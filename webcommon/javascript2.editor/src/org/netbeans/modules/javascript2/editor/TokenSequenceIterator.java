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
package org.netbeans.modules.javascript2.editor;

import java.util.List;
import org.netbeans.api.lexer.TokenSequence;

/**
 *
 * @author Petr Hejl
 */
final class TokenSequenceIterator {
    private final List<TokenSequence<?>> list;
    private final boolean backward;
    private int index;

    public TokenSequenceIterator(List<TokenSequence<?>> list, boolean backward) {
        this.list = list;
        this.backward = backward;
        this.index = -1;
    }

    public boolean hasMore() {
        return backward ? hasPrevious() : hasNext();
    }

    public TokenSequence<?> getSequence() {
        assert index >= 0 && index < list.size() : "No sequence available, call hasMore() first."; //NOI18N
        return list.get(index);
    }

    private boolean hasPrevious() {
        boolean anotherSeq = false;
        if (index == -1) {
            index = list.size() - 1;
            anotherSeq = true;
        }
        for (; index >= 0; index--) {
            TokenSequence<?> seq = list.get(index);
            if (anotherSeq) {
                seq.moveEnd();
            }
            if (seq.movePrevious()) {
                return true;
            }
            anotherSeq = true;
        }
        return false;
    }

    private boolean hasNext() {
        boolean anotherSeq = false;
        if (index == -1) {
            index = 0;
            anotherSeq = true;
        }
        for (; index < list.size(); index++) {
            TokenSequence<?> seq = list.get(index);
            if (anotherSeq) {
                seq.moveStart();
            }
            if (seq.moveNext()) {
                return true;
            }
            anotherSeq = true;
        }
        return false;
    }

}
