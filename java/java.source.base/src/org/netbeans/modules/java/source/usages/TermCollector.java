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

package org.netbeans.modules.java.source.usages;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.apache.lucene.index.Term;
import org.netbeans.api.annotations.common.NonNull;

/**
 *
 * @author Tomas Zezula
 */
final class TermCollector {
    
    private final Map<Integer, Set<Term>> doc2Terms;
    
    TermCollector() {
        doc2Terms = new HashMap<Integer, Set<Term>>();
    }
    
    void add (final int docId, final @NonNull Term term) {
        Set<Term> slot = doc2Terms.get(docId);
        if (slot == null) {
            slot = new HashSet<Term>();
            doc2Terms.put(docId, slot);
        }
        slot.add(term);
    }
    
    Set<Term> get(final int docId) {
        return doc2Terms.get(docId);
    }
    
    
    static interface TermCollecting {
        void attach (TermCollector collector);
    }

}
