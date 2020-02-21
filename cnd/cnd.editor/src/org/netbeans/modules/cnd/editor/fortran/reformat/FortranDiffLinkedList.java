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

package org.netbeans.modules.cnd.editor.fortran.reformat;

import java.util.Iterator;
import java.util.LinkedList;
import org.netbeans.modules.cnd.editor.fortran.reformat.FortranReformatter.Diff;

/**
 *
 */
/*package local*/ class FortranDiffLinkedList {
    private final LinkedList<Diff> storage = new LinkedList<Diff>();
    
    /*package local*/ Diff addFirst(int start, int end, int newLines, int spaces, boolean isIndent){
        Diff diff = new Diff(start, end, newLines, spaces, isIndent);
        storage.add(getIndex(start, end), diff);
        return diff;
    }

    private int getIndex(int start, int end) {
        int res = 0;
        Iterator<Diff> it = storage.iterator();
        while(it.hasNext()) {
            Diff diff = it.next();
            if (diff.getStartOffset()<=start) {
                if (diff.getStartOffset() == start &&
                    diff.getEndOffset() == end) {
                    it.remove();
                }
                break;
            }
            res++;
        }
        return res;
    }
    
    /*package local*/ DiffResult getDiffs(FortranExtendedTokenSequence ts, int shift){
        int start;
        int end;
        if (shift != 0) {
            int index = ts.index();
            try {
                if (shift > 0){
                    while(ts.moveNext()) {
                        shift--;
                        if (shift == 0){
                            break;
                        }
                    }
                } else {
                    while(ts.movePrevious()) {
                        shift++;
                        if (shift == 0){
                            break;
                        }
                    }
                }
                start = ts.offset();
                end = ts.offset()+ts.token().length();
            } finally {
                ts.moveIndex(index);
                ts.moveNext();
            }
        } else {
            start = ts.offset();
            end = ts.offset()+ts.token().length();
        }
        return getDiffs(start, end);
    }

    /*package local*/ DiffResult getDiffs(int start, int end){
        DiffResult result = null;
        Iterator<Diff> it = storage.iterator();
        while(it.hasNext()) {
            Diff diff = it.next();
            if (diff.getStartOffset() == start) {
                if (diff.getEndOffset() == end) {
                    if (result == null) {
                        result = new DiffResult();
                    }
                    result.replace = diff;
                } else {
                    if (result == null) {
                        result = new DiffResult();
                    }
                    result.before = diff;
                }
            } else if (diff.getEndOffset() == end) {
                if (result == null) {
                    result = new DiffResult();
                }
                result.after = diff;
            }
            if (diff.getEndOffset() < start) {
                return result;
            }
        }
        return result;
    }
    
    /*package local*/ Diff getFirst(){
        if (storage.isEmpty()){
            return null;
        }
        return storage.getFirst();

    }
    /*package local*/ LinkedList<Diff> getStorage(){
        return storage;
    }
    
    static class DiffResult{
        Diff before;
        Diff replace;
        Diff after;
    }
}
