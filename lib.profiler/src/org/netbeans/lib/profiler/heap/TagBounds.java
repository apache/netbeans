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

package org.netbeans.lib.profiler.heap;


/**
 *
 * @author Tomas Hurka
 */
class TagBounds {
    //~ Instance fields ----------------------------------------------------------------------------------------------------------

    final int tag;
    final long startOffset;
    long endOffset;

    //~ Constructors -------------------------------------------------------------------------------------------------------------

    TagBounds(int t, long start, long end) {
        tag = t;
        startOffset = start;
        endOffset = end;
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    TagBounds union(TagBounds otherTagBounds) {
        if (otherTagBounds == null) {
            return this;
        }

        long start = Math.min(startOffset, otherTagBounds.startOffset);
        long end = Math.max(endOffset, otherTagBounds.endOffset);

        return new TagBounds(-1, start, end);
    }
}
