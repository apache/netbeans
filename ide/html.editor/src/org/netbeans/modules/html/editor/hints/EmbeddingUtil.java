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
package org.netbeans.modules.html.editor.hints;

import org.netbeans.modules.csl.api.Error;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.parsing.api.Snapshot;

/**
 *
 * @author marekfukala
 */
public class EmbeddingUtil {

    /** tweak the error position if close to embedding boundary */
    public static OffsetRange getErrorOffsetRange(Error e, Snapshot snapshot) {
        int astFrom = e.getStartPosition();
        int astTo = e.getEndPosition();

        return convertToDocumentOffsets(astFrom, astTo, snapshot);
    }
    
    public static OffsetRange convertToDocumentOffsets(int astFrom, int astTo, Snapshot snapshot) {
        int from = snapshot.getOriginalOffset(astFrom);
        int to = snapshot.getOriginalOffset(astTo);

        if (from == -1 && to == -1) {
            //completely unknown position, give up
            return OffsetRange.NONE;
        } else if (from == -1 && to != -1) {
            from = to;
        } else if (from != -1 && to == -1) {
            to = from;
        }
        return new OffsetRange(from, to);
    }
}
