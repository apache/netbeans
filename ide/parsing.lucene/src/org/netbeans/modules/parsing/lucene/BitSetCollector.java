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

package org.netbeans.modules.parsing.lucene;

import java.util.BitSet;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.Collector;
import org.apache.lucene.search.Scorer;

/**
 *
 * @author Tomas Zezula
 */
class BitSetCollector extends Collector {

    private int docBase;
    public final BitSet bits;

    BitSetCollector(final BitSet bitSet) {
        assert bitSet != null;
        bits = bitSet;
    }

    // ignore scorer
    @Override
    public void setScorer(Scorer scorer) {
    }

    // accept docs out of order (for a BitSet it doesn't matter)
    @Override
    public boolean acceptsDocsOutOfOrder() {
      return true;
    }

    @Override
    public void collect(int doc) {
      bits.set(doc + docBase);
    }

    @Override
    public void setNextReader(IndexReader reader, int docBase) {
      this.docBase = docBase;
    }

}