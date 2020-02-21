/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
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
