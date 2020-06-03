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

package org.netbeans.modules.cnd.classview;

import java.util.Comparator;
import org.openide.util.CharSequences;

/**
 *
 */
public class SortedName implements Comparable<SortedName> {
    private byte prefix;
    private CharSequence name;
    private byte suffix;
    public SortedName(int prefix, CharSequence name, int suffix){
        this.prefix = (byte)prefix;
        this.name = name;
        this.suffix = (byte)suffix;
    }
    
    public byte getPrefix(){
        return prefix;
    }
    
    @Override
    public int compareTo(SortedName o) {
        int i = prefix - o.prefix;
        if (i == 0){
            i = getCharSequenceComparator().compare(name, o.name);
            if (i == 0){
                i = suffix - o.suffix;
            }
        }
        return i;
    }

    protected Comparator<CharSequence> getCharSequenceComparator() {
        return CharSequences.comparator();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final SortedName other = (SortedName) obj;
        if (this.prefix != other.prefix) {
            return false;
        }
        if (this.name != other.name && (this.name == null || !this.name.equals(other.name))) {
            return false;
        }
        if (this.suffix != other.suffix) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 61 * hash + this.prefix;
        hash = 61 * hash + (this.name != null ? this.name.hashCode() : 0);
        hash = 61 * hash + this.suffix;
        return hash;
    }
    
}

