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

package org.netbeans.modules.cnd.api.model;

import java.util.Comparator;

/**
 * An object, which has correspondent file and a pair of offsets (start and end)
 */
public interface CsmOffsetable extends CsmObject {

    interface Position {
        int getOffset();
        int getLine();
        int getColumn();
    }

    /** gets the file, which contains the given object */
    CsmFile getContainingFile();

    /** gets the offset of the 1-st character of the object */
    int getStartOffset();

    /** gets the offset of the character, following by the last character of the object */
    int getEndOffset();
    
    /** gets the position of the 1-st character of the object */ 
    Position getStartPosition();
    
    /** gets the position of the character, following by the last character of the object */
    Position getEndPosition();
    
    /** gets this object's text */
    CharSequence getText();

    public static final Comparator<? super CsmOffsetable> OFFSET_COMPARATOR = new OffsetableComparator<>();

    static final class OffsetableComparator<T extends CsmOffsetable> implements Comparator<T> {

        @Override
        public int compare(CsmOffsetable o1, CsmOffsetable o2) {
            int diff = o1.getStartOffset() - o2.getStartOffset();
            if (diff == 0) {
                return o1.getEndOffset() - o2.getEndOffset();
            } else {
                return diff;
            }
        }
    };
}
