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
package org.netbeans.modules.java.source.save;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.java.source.save.CasualDiff.Diff;

/**
 *
 * @author Pavel Flaska
 */
class DiffFacility {
    private final Collection<Diff> gdiff;
    private int[] sections;
    private int lineStart;
    
    public DiffFacility(Collection<Diff> diff) {
        this.gdiff = diff;
    }
    
    private static class Line {
        Line(String data, int start, int end) {
            this.start = start;
            this.end = end;
            this.data = data;
        }
        
        @Override
        public String toString() {
            return data.toString();
        }
        
        @Override
        public boolean equals(Object o) {
            if (o instanceof Line) {
                return data.equals(((Line) o).data);
            } else {
                return false;
            }
        }
        
        @Override
        public int hashCode() {
            return data.hashCode();
        }
        
        String data;
        int end;
        int start;
    }
    
    private static List<Line> getLines(String text) {
        char[] chars = text.toCharArray();
        List<Line> list = new ArrayList<Line>();
        int pointer = 0;
        for (int i = 0; i < chars.length; i++) {
            if (chars[i] == '\n') {
                list.add(new Line(new String(chars, pointer, i-pointer+1), pointer, i+1));
                pointer = i+1;
            }
        }
        if (pointer < chars.length) {
            list.add(new Line(new String(chars, pointer, chars.length-pointer), pointer, chars.length));
        }
        return list;
    }
    
    public DiffFacility withSections(int[] sections, int lineStart) {
        this.sections = sections;
        this.lineStart = lineStart;
        return this;
    }
    
    /**
     * Converts character offsets found in this.sections into line indexes.
     * Assumes that a section spans whole lines (currently true with the guarded block implementation). Returns an array
     * of line index pairs (lines1Idx, lines2Idx) for lines that form boundaries of readonly/writable areas in the text.
     * 
     * @param lines1 original lines
     * @param lines2 generated lines
     * @param offset offset of the lines1[0]'s start
     * @return array that contain pairs of matching lines for individual boundaries
     */
    private int[] computeLineSections(Line[] lines1, Line[] lines2, int offset) {
        int i1 = 0, i2 = 0;
        int delta = Math.max(0, offset - lineStart);
        int res[] = new int[sections.length];
        for (int p = 0; p < sections.length; p += 2) {
            int orig = sections[p] - offset;
            int nue = sections[p + 1] - delta;
            while (i1 < lines1.length && lines1[i1].end <= orig) {
                i1++;
            }
            while (i2 < lines2.length && lines2[i2].end <= nue) {
                i2++;
            }
            
            if (i1 < lines1.length && i2 < lines2.length) {
                if ((lines1[i1].start >= orig) != lines2[i2].start >= nue) {
                    // some error, better discard the whole boundary
                    continue;
                }
            } else {
                return (p == res.length) ?
                        res  : Arrays.copyOf(res, p);
            }
            res[p] = i1;
            res[p + 1] = i2;
        }
        return res;
    }
    
    public List<Diff> makeListMatch(String text1, String text2, int offset) {
        if (sections == null) {
            sections = new int[] { text1.length(), text2.length() };
        }
        List<Line> list1 = getLines(text1);
        List<Line> list2 = getLines(text2);
        Line[] lines1 = list1.toArray(new Line[0]);
        Line[] lines2 = list2.toArray(new Line[0]);
        
        List<Difference> diffs = new ComputeDiff<Line>(lines1, lines2, computeLineSections(lines1, lines2, offset)).diff();
        for (Difference diff : diffs) {
            int delStart = diff.getDeletedStart();
            int delEnd   = diff.getDeletedEnd();
            int addStart = diff.getAddedStart();
            int addEnd   = diff.getAddedEnd();
            
            char type = delEnd != Difference.NONE && addEnd != Difference.NONE ? 'c' : (delEnd == Difference.NONE ? 'a' : 'd');

            // addition
            if (type == 'a') {
                StringBuilder builder = new StringBuilder();
                for (int i = addStart; i <= addEnd; i++) {
                    builder.append(lines2[i].data);
                }
                gdiff.add(Diff.insert(delEnd == Difference.NONE ?
                        delStart < lines1.length ? lines1[delStart].start + offset : (lines1.length != 0 ? lines1[lines1.length-1].end + offset : offset)
                        : lines1[delEnd].end + offset,
                        builder.toString()));
                
            }

            // deletion
            else if (type == 'd') {
                gdiff.add(Diff.delete(lines1[delStart].start + offset, lines1[delEnd].end + offset));
            }
            
            // change
            else { // type == 'c'
                if (addEnd-addStart>delEnd-delStart) {
                    //change will be performed in 2 steps:
                    //1. change lines
                    //2. add lines
                    StringBuilder builder = new StringBuilder();
                    for (int i = delStart; i <= delEnd; i++) {
                        builder.append(lines1[i].data);
                    }
                    String match1 = builder.toString();
                    builder = new StringBuilder();
                    for (int i = addStart; i <= addStart + delEnd - delStart; i++) {
                        builder.append(lines2[i].data);
                    }
                    String match2 = builder.toString();
                    makeTokenListMatch(match1, match2, lines1[delStart].start + offset);
                    builder = new StringBuilder();
                    for (int i = addStart + delEnd - delStart + 1; i <= addEnd; i++) {
                        builder.append(lines2[i].data);
                    }
                    String s = builder.toString();
                    if (!"".equals(s)) {
                        gdiff.add(Diff.insert(lines1[delEnd].end + offset, s));
                    }
                } else {
                    //one step change
                    StringBuilder builder = new StringBuilder();
                    for (int i = delStart; i <= delEnd; i++) {
                        builder.append(lines1[i].data);
                    }
                    String match1 = builder.toString();
                    builder = new StringBuilder();
                    for (int i = addStart; i <= addEnd; i++) {
                        builder.append(lines2[i].data);
                    }
                    String match2 = builder.toString();
                    makeTokenListMatch(match1, match2, lines1[delStart].start + offset);
                }
            }
        }
        return null;
    }
    
    private void removeOrStripLastNewline(List<Line> list1) {
        int idx = list1.size() - 1;
        Line last1 = list1.remove(idx);
        int firstNewline = last1.data.indexOf('\n');
        int lastNewline = last1.data.lastIndexOf('\n');
        if (firstNewline != lastNewline) { // NOI18N
            String stripped = last1.data.substring(0, last1.data.lastIndexOf('\n')); // NOI18N
            list1.add(new Line(stripped, last1.start, last1.start + stripped.length()));
        }
    }
    
    /**
     * If trailing line comment tokens have the same text, remove them both from the list1/list2. This is a fix equivalent to
     * bugfix #90424, but the original code discards changes to line comment contents made by the code generator. It should
     * be sufficient to just ignore changes in non-meaningful whitespace.
     * 
     * See defects #90424, #125385, #208270 for details
     * 
     * @param list1 original lines
     * @param list2 newly inserted lines
     */
    private void removeSameTrailingLineComments(List<Line> list1, List<Line> list2) {
        String s1 = list1.get(list1.size() - 1).data;
        String s2 = list2.get(list2.size() - 1).data;
        assert s1.startsWith("//"); // NOI18N
        assert s2.startsWith("//"); // NOI18N
        s1 = s1.substring(2).trim();
        s2 = s2.substring(2).trim();
        if (s1.equals(s2)) {
            list1.remove(list1.size() - 1);
            list2.remove(list2.size() - 1);
        }
    }
    
    public List<Diff> makeTokenListMatch(String text1, String text2, int currentPos) {
        TokenSequence<JavaTokenId> seq1 = TokenHierarchy.create(text1, JavaTokenId.language()).tokenSequence(JavaTokenId.language());
        TokenSequence<JavaTokenId> seq2 = TokenHierarchy.create(text2, JavaTokenId.language()).tokenSequence(JavaTokenId.language());
        List<Line> list1 = new ArrayList<Line>();
        List<Line> list2 = new ArrayList<Line>();
        JavaTokenId lastId1 = null;
        while (seq1.moveNext()) {
            String data = seq1.token().text().toString();
            lastId1 = seq1.token().id();
            list1.add(new Line(data, seq1.offset(), seq1.offset() + data.length()));
        }
        JavaTokenId lastId2 = null;
        while (seq2.moveNext()) {
            String data = seq2.token().text().toString();
            lastId2 = seq2.token().id();
            list2.add(new Line(data, seq2.offset(), seq2.offset() + data.length()));
        }
        if (lastId1 != null && lastId1 == lastId2) {
            if (lastId1 == JavaTokenId.WHITESPACE && (list1.get(list1.size() - 1).data.endsWith("\n") == list2.get(list2.size() - 1).data.endsWith("\n"))) {
                removeOrStripLastNewline(list1);
                removeOrStripLastNewline(list2);
            } else if (lastId1 == JavaTokenId.LINE_COMMENT) { // implies both end with a newline
                removeSameTrailingLineComments(list1, list2);
            }
        }
        Line[] lines1 = list1.toArray(new Line[0]);
        Line[] lines2 = list2.toArray(new Line[0]);
        List<Difference> diffs = new ComputeDiff<Line>(lines1, lines2, null).diff();
        for (Difference diff : diffs) {
            int delStart = diff.getDeletedStart();
            int delEnd   = diff.getDeletedEnd();
            int addStart = diff.getAddedStart();
            int addEnd   = diff.getAddedEnd();
            
            char type = delEnd != Difference.NONE && addEnd != Difference.NONE ? 'c' : (delEnd == Difference.NONE ? 'a' : 'd');

            // addition
            if (type == 'a') {
                StringBuilder builder = new StringBuilder();
                for (int i = addStart; i <= addEnd; i++) {
                    builder.append(lines2[i].data);
                }
                gdiff.add(Diff.insert(currentPos + (delEnd == Difference.NONE ?
                        delStart < lines1.length ? lines1[delStart].start : (lines1.length > 0 ? lines1[lines1.length-1].end : 0)
                        : lines1[delEnd].end),
                        builder.toString()));
            }
            
            // deletion
            else if (type == 'd') {
                gdiff.add(Diff.delete(currentPos + lines1[delStart].start, currentPos + lines1[delEnd].end));
            }
            
            // change
            else { // type == 'c'
                // Note: it's better to first REMOVE then INSERT to avoid insertion attempts into readonly (guarded)
                // part of text (the writabl area was removed at the 1st step of the change operation). But remove-then-insert is interpreted 
                // as 'change' by DiffUtilities.diff2ModificationResultDifference. I only wonder why the 'change' type is discarded here,
                // then reinvented later.
                StringBuilder builder = new StringBuilder();
                /*for (int i = delStart; i <= delEnd; i++) {
                    builder.append(lines1[i].data);
                }*/
                gdiff.add(Diff.delete(currentPos + lines1[delStart].start, currentPos + lines1[delEnd].end));
                //builder = new StringBuilder();
                for (int i = addStart; i <= addEnd; i++) {
                    builder.append(lines2[i].data);
                }
                // delEnd must not be NONE here, see `type' computation
                gdiff.add(Diff.insert(currentPos + lines1[delEnd].end, builder.toString()));
            }
                    
        }
        return null;
    }
}
