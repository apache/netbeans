/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
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
