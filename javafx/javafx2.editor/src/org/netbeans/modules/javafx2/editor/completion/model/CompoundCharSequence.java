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
package org.netbeans.modules.javafx2.editor.completion.model;

import java.util.Collection;
import java.util.List;

/**
 *
 * @author sdedic
 */
public final class CompoundCharSequence implements CharSequence {
    private List<CharSequence> parts;
    private int[] partBoundaries;
    private int startOffset;
    private volatile int partNo;
    private int len;
    
    public CompoundCharSequence(int startOffset, List<CharSequence> parts, int len) {
        this.startOffset = startOffset;
        this.parts = parts;
        this.partBoundaries = new int[parts.size()];
        
        if (startOffset >= parts.get(0).length()) {
            throw new IllegalArgumentException();
        }
        
        int pos = -startOffset;
        for (int i = 0; pos < len && i < parts.size(); i++) {
            partBoundaries[i] = (pos += parts.get(i).length());
        }
        if (len == -1) {
            this.len = partBoundaries[partBoundaries.length - 1];   
        } else {
            this.len = len;
        }
    }

    @Override
    public int length() {
        return len;
    }

    @Override
    public char charAt(int index) {
        if (index < 0 || index >= len) {
            throw new StringIndexOutOfBoundsException(index);
        }
        int pn = partNo;
        if (partBoundaries[pn] > index && (pn == 0 || partBoundaries[pn - 1] <= index)) {
            int start = pn == 0 ? 0 : partBoundaries[pn - 1];
            return parts.get(pn).charAt(index - start);
        } else {
            for (int i = 0; i < parts.size(); i++) {
                if (partBoundaries[i] > index) {
                    int start = i == 0 ? startOffset : partBoundaries[i - 1];
                    partNo = i;
                    return parts.get(i).charAt(index - start);
                }
            }
        }
        // should never happen.
        throw new StringIndexOutOfBoundsException(index);
    }

    @Override
    public CharSequence subSequence(int start, int end) {
        if (end > len) {
            throw new StringIndexOutOfBoundsException(end);
        }
        for (int i = 0; i < parts.size(); i++) {
            if (partBoundaries[i] > start) {
                int sO = i == 0 ? 0 : partBoundaries[i - 1];
                return new CompoundCharSequence(start - sO, parts.subList(i, parts.size()), end - start);
            }
        }
        throw new StringIndexOutOfBoundsException(start);
    }
}
