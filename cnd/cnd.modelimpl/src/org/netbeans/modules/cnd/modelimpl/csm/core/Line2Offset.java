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
package org.netbeans.modules.cnd.modelimpl.csm.core;

import java.io.IOException;
import java.util.ArrayList;
import org.netbeans.modules.cnd.modelimpl.platform.ModelSupport;

/**
 *
 */
public class Line2Offset {

    private String text;
    private char[] buffer;
    private final int[] lines;

    Line2Offset(String text) {
        this.text = text;
        lines = init();
    }

    public Line2Offset(char[] buffer) {
        this.buffer = buffer;
        lines = init();
    }

    Line2Offset(char[] buffer, int lines[]) {
        this.buffer = buffer;
        this.lines = lines;
    }

    public int getLineCount() {
        return lines.length;
    }

    public int[] getLineColumnByOffset(int offset) throws IOException {
        int[] lineCol = new int[]{1, 1};
        if (offset >= 0) {
            int line = _getLineByOffset(offset);
            int start = _getStartLineOffset(line);
            lineCol[0] = line;
            // find line and column
            int TABSIZE = ModelSupport.getTabSize();
            int length = getLength();
            for (int curOffset = start; curOffset < offset && curOffset < length; curOffset++) {
                char curChar = getCharAt(curOffset);
                switch (curChar) {
                    case '\n':
                        // just increase line number
                        lineCol[0] = lineCol[0] + 1;
                        lineCol[1] = 1;
                        break;
                    case '\t':
                        int col = lineCol[1];
                        int newCol = (((col - 1) / TABSIZE) + 1) * TABSIZE + 1;
                        lineCol[1] = newCol;
                        break;
                    default:
                        lineCol[1]++;
                        break;
                }
            }
        }
        return lineCol;
    }

    public int getOffsetByLineColumn(int line, int column) throws IOException {
        int startOffset = _getStartLineOffset(line);
        int TABSIZE = ModelSupport.getTabSize();
        int currCol = 1;
        int outOffset;
        int length = getLength();
        loop:
        for (outOffset = startOffset; outOffset < length; outOffset++) {
            if (currCol >= column) {
                break;
            }
            char curChar = getCharAt(outOffset);
            switch (curChar) {
                case '\n':
                    break loop;
                case '\t':
                    int col = currCol;
                    int newCol = (((col - 1) / TABSIZE) + 1) * TABSIZE + 1;
                    currCol = newCol;
                    break;
                default:
                    currCol++;
                    break;
            }
        }
        return outOffset;
    }

    private int[] init() {
        int length = getLength();
        ArrayList<Integer> list = new ArrayList<>(length / 10);
        list.add(Integer.valueOf(0));
        for (int curOffset = 0; curOffset < length; curOffset++) {
            if (getCharAt(curOffset) == '\n') {
                list.add(Integer.valueOf(curOffset + 1));
            }
        }
        int[] aLines = new int[list.size()];
        for (int i = 0; i < list.size(); i++) {
            aLines[i] = list.get(i);
        }
        return aLines;
    }

    private char getCharAt(int pos) {
        if (text != null) {
            return text.charAt(pos);
        } else {
            return buffer[pos];
        }
    }

    private int getLength() {
        if (text != null) {
            return text.length();
        } else {
            return buffer.length;
        }
    }

    private int _getStartLineOffset(int line) throws IOException {
        line--;
        if (line < lines.length) {
            return lines[line];
        }
        return lines[lines.length - 1];
    }

    private int _getLineByOffset(int offset) throws IOException {
        int low = 0;
        int high = lines.length - 1;
        while (low <= high) {
            int mid = (low + high) >>> 1;
            int midVal = lines[mid];
            if (midVal < offset) {
                if (low == high) {
                    return low + 1;
                }
                low = mid + 1;
            } else if (midVal > offset) {
                if (low == high) {
                    return low;
                }
                high = mid - 1;
            } else {
                return mid + 1;
            }
        }
        return low;
    }
}
